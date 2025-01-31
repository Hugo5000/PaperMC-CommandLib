package at.hugob.plugin.library.command;

import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.processors.cache.CaffeineCache;
import org.incendo.cloud.processors.confirmation.ConfirmationConfiguration;
import org.incendo.cloud.processors.confirmation.ConfirmationContext;
import org.incendo.cloud.processors.confirmation.ConfirmationManager;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This Class Manages Command creations for cloud commands
 */
public class CommandManager {
    private final PaperCommandManager<Source> manager;
    private final MinecraftHelp<Source> minecraftHelp;
    private final ConfirmationManager<Source> confirmationManager;

    /**
     * Creates a CommandManager instance
     *
     * @param plugin                        The plugin that creates the instance
     * @param errorPrefixer                 the prefix that error messages should have
     * @param helpCommand                   the help-command command i.e. "/plugin-name help"
     * @param noPendingCommandsNotifier     The message to send if one doesn't have any pending commands
     * @param confirmationRequestNotifier   The Notification to send if you need to confirm something
     */
    public CommandManager(final JavaPlugin plugin, Function<Component, ComponentLike> errorPrefixer, String helpCommand,
                          Consumer<Source> noPendingCommandsNotifier, BiConsumer<Source, ConfirmationContext<Source>> confirmationRequestNotifier) {
        ExecutionCoordinator<Source> executionCoordinatorFunction = ExecutionCoordinator.asyncCoordinator();

        manager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
            .executionCoordinator(executionCoordinatorFunction)
            .buildOnEnable(plugin);

        confirmationManager = ConfirmationManager.<Source>confirmationManager(
                ConfirmationConfiguration.<Source>builder()
                .cache(CaffeineCache.of(Caffeine.newBuilder()
                    .maximumSize(1_000)
                    .expireAfterWrite(Duration.ofMinutes(5))
                    .build()
                ))
                .noPendingCommandNotifier(noPendingCommandsNotifier)
                .confirmationRequiredNotifier(confirmationRequestNotifier)
                .build()
        );

        manager.registerCommandPostProcessor(confirmationManager.createPostprocessor());

        minecraftHelp = MinecraftHelp.<Source>create(
            helpCommand,
            manager,
            sender -> sender.source()
        );
        MinecraftExceptionHandler.<Source>create(sender -> sender.source())
            .defaultHandlers()
            .decorator(errorPrefixer)
            .registerTo(this.manager);
    }

    /**
     * Registers a Command Builder as a new command
     *
     * @param command the Command to register
     * @param <T> The Commands Source Class
     */
    public <T extends Source> void command(Command.Builder<T> command) {
        this.manager.command(command);
    }

    /**
     * gets cloud's CommandManager in case this is needed
     *
     * @return cloud's CommandManager that this manager manages
     */
    public PaperCommandManager<Source> manager() {
        return this.manager;
    }

    /**
     * This is used for the help command to query for commands
     *
     * @param s      The command that should be queried, e.g. "help" to query for the help command
     * @param source the player who uses the help command
     */
    public void queryCommands(String s, Source source) {
        this.minecraftHelp.queryCommands(s, source);
    }
}
