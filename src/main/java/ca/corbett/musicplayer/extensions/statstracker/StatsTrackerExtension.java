package ca.corbett.musicplayer.extensions.statstracker;

import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.musicplayer.extensions.MusicPlayerExtension;
import ca.corbett.musicplayer.ui.AudioPanel;
import ca.corbett.musicplayer.ui.AudioPanelListener;
import ca.corbett.musicplayer.ui.MainWindow;
import ca.corbett.musicplayer.ui.VisualizationTrackInfo;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Logger;

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
    protected List<AbstractProperty> createConfigProperties() {
        return List.of();
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

    @Override
    public boolean handleKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.isControlDown() && keyEvent.getKeyCode() == KeyEvent.VK_T) {
            List<StatsDb.Entry> top10 = statsDb.getTop10();
            if (top10.isEmpty()) {
                getMessageUtil().info("No statistics data yet!");
                return true;
            }
            new Top10Dialog(statsDb, top10).setVisible(true);
            return true;
        }

        return false;
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(MainWindow.getInstance(), log);
        }
        return messageUtil;
    }
}
