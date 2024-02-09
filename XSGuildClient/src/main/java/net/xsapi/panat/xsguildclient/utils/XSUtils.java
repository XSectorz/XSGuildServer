package net.xsapi.panat.xsguildclient.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.xsapi.panat.xsguildclient.config.messagesConfig;

import java.util.Objects;

public class XSUtils {

    public static String decodeText(String str) {
        Component parsedMessage = MiniMessage.builder().build().deserialize(str);
        String legacy = LegacyComponentSerializer.legacyAmpersand().serialize(parsedMessage);
        return legacy.replace('&', '§');
    }

    public static String decodeTextFromConfig(String section) {
        String text = Objects.requireNonNull(messagesConfig.customConfig.getString("system." + section));
        text = text.replace("%prefix%", Objects.requireNonNull(messagesConfig.customConfig.getString("system.prefix")));
        Component parsedMessage = MiniMessage.builder().build().deserialize(text);
        String legacy = LegacyComponentSerializer.legacyAmpersand().serialize(parsedMessage);
        return legacy.replace('&', '§');
    }

    public static String decodeTextNotReplace(String str) {
        Component parsedMessage = MiniMessage.builder().build().deserialize(str);
        return LegacyComponentSerializer.legacyAmpersand().serialize(parsedMessage);
    }
}
