package at.hugo.plugin.library.command;

import cloud.commandframework.Command;
import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Function;

/**
 * This Class Manages Command creations for cloud commands
 */
public class CommandManager {
    private final PaperCommandManager<CommandSender> manager;
    private final MinecraftHelp<CommandSender> minecraftHelp;

    /**
     * Creates a CommandManager instance
     *
     * @param plugin      The plugin that creates the instance
     * @param errorPrefixer the prefix that error messages should have
     * @param helpCommand the help-command command i.e. "/plugin-name help"
     * @param confirmationManager the manager that manages the confirmations
     * @throws InstantiationException When cloud's CommandManager can't be created
     */
    public CommandManager(final JavaPlugin plugin, Function<Component, Component> errorPrefixer, String helpCommand, CommandConfirmationManager<CommandSender> confirmationManager) throws InstantiationException {
        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build();

        try {
            manager = new PaperCommandManager<>(
                    plugin,
                    executionCoordinatorFunction,
                    Function.identity(),
                    Function.identity()
            );
        } catch (Exception e) {
            throw new InstantiationException("Could not create the Command Manager: " + e.getMessage());
        }
        this.manager.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(
                FilteringCommandSuggestionProcessor.Filter.<CommandSender>contains(true).andTrimBeforeLastSpace()
        ));

        minecraftHelp = new MinecraftHelp<>(
                helpCommand,
                sender -> sender,
                manager
        );

        manager.registerBrigadier();
        manager.registerAsynchronousCompletions();

        confirmationManager.registerConfirmationProcessor(manager);

        new MinecraftExceptionHandler<CommandSender>()
                .withInvalidSyntaxHandler()
                .withInvalidSenderHandler()
                .withNoPermissionHandler()
                .withArgumentParsingHandler()
                .withCommandExecutionHandler()
                .withDecorator(errorPrefixer).apply(this.manager, sender -> sender);

    }

    /**
     * Registers a Command Builder as a new command
     *
     * @param builder the builder to register
     */
    public void command(Command.Builder<CommandSender> builder) {
        this.manager.command(builder);
    }

    /**
     * gets cloud's CommandManager in case this is needed
     *
     * @return cloud's CommandManager that this manager manages
     */
    public PaperCommandManager<CommandSender> manager() {
        return this.manager;
    }

    /**
     * This is used for the help command to query for commands
     *
     * @param s      The command that should be queried, e.g. "help" to query for the help command
     * @param sender the player who uses the help command
     */
    public void queryCommands(String s, CommandSender sender) {
        this.minecraftHelp.queryCommands(s, sender);
    }
}
