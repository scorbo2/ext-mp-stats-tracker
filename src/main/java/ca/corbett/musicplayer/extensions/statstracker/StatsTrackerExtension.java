package ca.corbett.musicplayer.extensions.statstracker;

import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.io.KeyStrokeManager;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.KeyStrokeProperty;
import ca.corbett.musicplayer.extensions.MusicPlayerExtension;
import ca.corbett.musicplayer.ui.AudioPanel;
import ca.corbett.musicplayer.ui.AudioPanelListener;
import ca.corbett.musicplayer.ui.MainWindow;
import ca.corbett.musicplayer.ui.VisualizationTrackInfo;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This is an extension for the <A HREF="https://github.com/scorbo2/musicplayer">MusicPlayer application</A>
 * which logs stats on which tracks are played, so that a stats dialog can be displayed.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class StatsTrackerExtension extends MusicPlayerExtension implements AudioPanelListener {
    private static final Logger log = Logger.getLogger(StatsTrackerExtension.class.getName());

    private MessageUtil messageUtil;

    private final AppExtensionInfo extInfo;
    private StatsDb statsDb;

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
    public void loadJarResources() {
    }

    @Override
    protected List<AbstractProperty> createConfigProperties() {
        List<AbstractProperty> props = new ArrayList<>();
        props.add(new KeyStrokeProperty("Statistics.options.viewStatsKey",
                                        "View statistics:",
                                        KeyStrokeManager.parseKeyStroke("Ctrl+T"),
                                        new LaunchDialogAction())
                      .setExposed(false)); // not user-configurable
        return props;
    }

    @Override
    public void onActivate() {
        statsDb = new StatsDb();
        AudioPanel.getInstance().addAudioPanelListener(this);
    }

    @Override
    public void onDeactivate() {
        statsDb.close();
        AudioPanel.getInstance().removeAudioPanelListener(this);
    }

    @Override
    public void stateChanged(AudioPanel sourcePanel, AudioPanel.PanelState state) {
        if (state == AudioPanel.PanelState.PLAYING) {
            statsDb.incrementPlayCount(sourcePanel.getAudioData().getSourceFile());
            log.fine("StatsTracker: Incrementing play count for " +
                         sourcePanel.getAudioData().getSourceFile().getAbsolutePath());
        }
    }

    @Override
    public void audioLoaded(AudioPanel sourcePanel, VisualizationTrackInfo trackInfo) {
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(MainWindow.getInstance(), log);
        }
        return messageUtil;
    }

    private class LaunchDialogAction extends EnhancedAction {

        public LaunchDialogAction() {
            super("View your listening stats");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<StatsDb.Entry> top10 = statsDb.getTop10();
            if (top10.isEmpty()) {
                getMessageUtil().info("No statistics data yet!");
                return;
            }
            new Top10Dialog(statsDb, top10).setVisible(true);
        }
    }
}
