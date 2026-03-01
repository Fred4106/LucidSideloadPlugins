package com.fredplugins.dynamicHighlights.model;

import com.fredplugins.dynamicHighlights.FredsLootFiltersPlugin;
import com.fredplugins.dynamicHighlights.lang.ParseException;
import com.fredplugins.dynamicHighlights.lang.Token;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class SoundProvider {
    public abstract void play(FredsLootFiltersPlugin plugin);

    public static SoundProvider fromExpr(Token token) throws ParseException {
        switch (token.getType()) {
            case LITERAL_INT:
                return new SoundEffect(token.expectInt());
            case LITERAL_STRING:
                return new File(token.expectString());
            default:
                throw new ParseException("sound: unexpected token", token);
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static final class SoundEffect extends SoundProvider {
        private final int id;

        @Override
        public void play(FredsLootFiltersPlugin plugin) {
            plugin.getClientThread().invoke(() -> {
                var client = plugin.getClient();
                var userVolume = client.getPreferences().getSoundEffectVolume();
                var effectVolume = plugin.getConfig().soundVolume();

                client.getPreferences().setSoundEffectVolume(effectVolume);
                client.playSoundEffect(id, effectVolume);
                client.getPreferences().setSoundEffectVolume(userVolume);
            });
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static final class File extends SoundProvider {
        private final String filename;

        @Override
        public void play(FredsLootFiltersPlugin plugin) {
            try {
                var soundFile = new java.io.File(FredsLootFiltersPlugin.SOUND_DIRECTORY, filename);
                var gain = 20f * (float) Math.log10(plugin.getConfig().soundVolume() / 100f);
                plugin.getAudioPlayer().play(soundFile, gain);
            } catch (Exception e) {
                log.warn("play audio {}", filename, e);
            }
        }
    }
}
