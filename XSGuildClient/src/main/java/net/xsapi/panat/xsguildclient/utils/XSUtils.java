package net.xsapi.panat.xsguildclient.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class XSUtils {

    public static String decodeText(String str) {
        Component parsedMessage = MiniMessage.builder().build().deserialize(str);
        String legacy = LegacyComponentSerializer.legacyAmpersand().serialize(parsedMessage);
        return legacy.replace('&', '§');
    }

    public static String decodeTextNotReplace(String str) {
        Component parsedMessage = MiniMessage.builder().build().deserialize(str);
        return LegacyComponentSerializer.legacyAmpersand().serialize(parsedMessage);
    }
}
