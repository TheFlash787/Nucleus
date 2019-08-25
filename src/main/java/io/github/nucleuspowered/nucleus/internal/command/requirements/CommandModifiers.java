/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command.requirements;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.control.CommandControl;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.services.IEconomyServiceProvider;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;

public enum CommandModifiers implements ICommandModifier, Reloadable {

    /**
     * Command requires an economy plugin
     */
    REQUIRE_ECONOMY {

        private Text lazyLoad = null;

        @Override
        public Optional<Text> testRequirement(ICommandContext<? extends CommandSource> source, CommandControl control,
                INucleusServiceCollection serviceCollection) throws CommandException {
            if (!serviceCollection.economyServiceProvider().serviceExists()) {
                if (this.lazyLoad == null) {
                    this.lazyLoad = serviceCollection.messageProvider().getMessageFor(source.getCommandSource(), "command.economyrequired");
                }

                return Optional.of(this.lazyLoad);
            }

            return Optional.empty();
        }

        @Override
        public void onReload() {
            this.lazyLoad = null;
        }
    },

    /**
     * Command has a cost
     */
    HAS_COST {

        @Override public void setupCommand(CommandControl control) {
            control.getCommandModifiersConfig().setCostEnable(true);
        }

        @Override public boolean canExecute(INucleusServiceCollection serviceCollection, ICommandContext<? extends CommandSource> source) throws CommandException {
            return serviceCollection.economyServiceProvider().serviceExists() && source.getCommandSource() instanceof Player;
        }

        @Override public Optional<Text> testRequirement(ICommandContext<? extends CommandSource> source, CommandControl control,
                INucleusServiceCollection serviceCollection) throws CommandException {
            IEconomyServiceProvider ies = serviceCollection.economyServiceProvider();
            if (!ies.withdrawFromPlayer((Player) source.getCommandSource(), source.getCost(), false)) {
                return Optional.of(serviceCollection.messageProvider().getMessageFor(source.getCommandSource(), "cost.nofunds",
                        ies.getCurrencySymbol(source.getCost())));
            }

            return Optional.empty();
        }
    },

    /**
     * Command has a cooldown
     */
    HAS_COOLDOWN {
        @Override public void setupCommand(CommandControl control) {
            control.getCommandModifiersConfig().setCooldownEnable(true);
        }

        @Override public boolean canExecute(INucleusServiceCollection serviceCollection, ICommandContext<? extends CommandSource> source) throws CommandException {
            return source.getCommandSource() instanceof Player;
        }

        @Override public Optional<Text> testRequirement(ICommandContext<? extends CommandSource> source,
                CommandControl control,
                INucleusServiceCollection serviceCollection) throws CommandException {
            CommandSource c = source.getCommandSource();
            return serviceCollection.cooldownService().getCooldown(control.getCommand(), (Identifiable) source)
                    .map(duration -> serviceCollection.messageProvider().getMessageFor(c, "cooldown.message",
                            Util.getTimeStringFromSeconds(duration.getSeconds())));
        }
    },

    /**
     * Command has a warmup
     */
    HAS_WARMUP {
        @Override public void setupCommand(CommandControl control) {
            control.getCommandModifiersConfig().setWarmupEnable(true);
        }

        @Override public boolean canExecute(INucleusServiceCollection serviceCollection, ICommandContext<? extends CommandSource> source) throws CommandException {
            return source.getCommandSource() instanceof Player;
        }
    }
}
