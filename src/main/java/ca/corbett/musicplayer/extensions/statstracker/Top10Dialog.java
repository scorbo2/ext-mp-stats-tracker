package ca.corbett.musicplayer.extensions.statstracker;

import ca.corbett.extras.properties.PropertiesDialog;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.musicplayer.ui.MainWindow;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Top10Dialog extends JDialog {

    final StatsDb statsDb;
    final List<StatsDb.Entry> top10;

    public Top10Dialog(StatsDb statsDb, List<StatsDb.Entry> top10) {
        super(MainWindow.getInstance(), "Top 10 most-played tracks", true);
        this.statsDb = statsDb;
        this.top10 = top10;
        setSize(new Dimension(450,300));
        initComponents();
    }

    private void initComponents() {
        FormPanel panel = new FormPanel();

        int place = 1;
        for (StatsDb.Entry entry : top10) {
            panel.addFormField(new LabelField(place + ":",
                                              entry.trackFile.getAbsolutePath()
                                                  + " (" + entry.playCount + ")"));
            place++;
        }

        PanelField panelField = new PanelField();
        JPanel container = panelField.getPanel();
        container.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton button = new JButton("Reset stats");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (JOptionPane.showConfirmDialog(MainWindow.getInstance(), "Really reset all stats?") == JOptionPane.YES_OPTION) {
                    statsDb.resetStats();
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), "Stats reset!");
                    dispose();
                }
            }
        });
        container.add(button);
        panel.addFormField(panelField);

        panel.render();
        setLayout(new BorderLayout());
        add(PropertiesDialog.buildScrollPane(panel), BorderLayout.CENTER);
    }
}
