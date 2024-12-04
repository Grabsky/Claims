/*
 * Claims (https://github.com/Grabsky/Claims)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.claims.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.jetbrains.annotations.NotNull;

public final class Extensions {

    public static void showRichTitle(final @NotNull Audience audience, final @NotNull Component title, final @NotNull Component subtitle, final long fadeInTicks, final long stayTicks, final long fadeOutTicks) {
        audience.showTitle(
                Title.title(title, subtitle, Title.Times.times(
                        Duration.of(fadeInTicks * 50, ChronoUnit.MILLIS),
                        Duration.of(stayTicks * 50, ChronoUnit.MILLIS),
                        Duration.of(fadeOutTicks * 50, ChronoUnit.MILLIS)
                ))
        );
    }

    public static void fadeOutTitle(final @NotNull Audience audience, final long delay, final long fadeOutTicks) {
        audience.sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ZERO, Duration.of(delay * 50, ChronoUnit.MILLIS), Duration.of(fadeOutTicks * 50, ChronoUnit.MILLIS)));
    }

}
