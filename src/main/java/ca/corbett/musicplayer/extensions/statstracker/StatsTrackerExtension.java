package ca.corbett.musicplayer.extensions.statstracker;

import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.musicplayer.extensions.MusicPlayerExtension;

import java.util.List;
import java.util.logging.Logger;

public class StatsTrackerExtension extends MusicPlayerExtension {
    private static final Logger log = Logger.getLogger(StatsTrackerExtension.class.getName());

    private final AppExtensionInfo extInfo;

    public StatsTrackerExtension() {
        extInfo = AppExtensionInfo.fromExtensionJar(getClass(), "/ca/corbett/musicplayer/extensions/statstracker/extInfo.json");
        if (extInfo == null) {
            throw new RuntimeException("StatsTrackerExtension: can't parse extInfo.json from jar resources!");
        }

    }

    @Override
    public AppExtensionInfo getInfo() {
        return extInfo;
    }

    @Override
    public List<AbstractProperty> getConfigProperties() {
        return List.of();
    }

    @Override
    public void onActivate() {

    }

    @Override
    public void onDeactivate() {

    }
}
