package com.alchemist.hangover.view;

import com.alchemist.hangover.controller.AudioManager;
import com.alchemist.hangover.controller.SceneManager;
import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.YmirRuneRitual;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.List;

/**
 * Scontro runico di Ymir.
 *
 * In ogni fase Arthur deve prima posizionarsi su una delle tre piattaforme,
 * poi scegliere la runa. La soluzione viene dalla stele letta in precedenza
 * e resta invariata in tutta la partita grazie al seed persistito.
 */
public final class YmirBossOverlay extends JDialog {
    private static final Color GOLD = new Color(245, 187, 91);
    private final GameState state = GameState.getInstance();
    private final List<YmirRuneRitual.Step> ritual;
    private final JLabel phaseLabel = new JLabel();
    private final JLabel hintLabel = new JLabel();
    private final JLabel feedbackLabel = new JLabel();
    private final JButton[] platformButtons = new JButton[3];
    private final JButton[] runeButtons = new JButton[3];
    private final HealthDisplayPanel hearts = new HealthDisplayPanel(35);
    private int phase = 0;
    private YmirRuneRitual.Zone selectedZone;

    public YmirBossOverlay(Frame owner) {
        super(owner, "Ymir — Il Rituale del Sonno", true);
        ritual = YmirRuneRitual.current(state);
        setSize(1030, 650);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(new Color(20, 18, 28));
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 109, 61), 3),
                new EmptyBorder(19, 22, 16, 22)));
        setContentPane(root);

        JLabel title = new JLabel("YMIR — RITUALE DEL SONNO PRIMORDIALE", SwingConstants.CENTER);
        title.setForeground(GOLD);
        title.setFont(new Font("Georgia", Font.BOLD, 25));
        root.add(title, BorderLayout.NORTH);

        JLabel image = new JLabel("YMIR", SwingConstants.CENTER);
        image.setForeground(GOLD);
        image.setPreferredSize(new Dimension(395, 450));
        URL url = getClass().getResource("/images/characters/boss_ymir.png");
        if (url != null) {
            image.setText("");
            image.setIcon(new ImageIcon(new ImageIcon(url).getImage()
                    .getScaledInstance(395, 450, Image.SCALE_SMOOTH)));
        }
        root.add(image, BorderLayout.WEST);

        JPanel interaction = new JPanel(new BorderLayout(0, 12));
        interaction.setOpaque(false);
        root.add(interaction, BorderLayout.CENTER);

        phaseLabel.setForeground(new Color(255, 222, 160));
        phaseLabel.setFont(new Font("Georgia", Font.BOLD, 20));
        interaction.add(phaseLabel, BorderLayout.NORTH);

        JPanel middle = new JPanel(new GridLayout(4, 1, 0, 10));
        middle.setOpaque(false);

        hintLabel.setForeground(new Color(219, 205, 184));
        hintLabel.setFont(new Font("Georgia", Font.PLAIN, 16));
        middle.add(hintLabel);

        JPanel platforms = new JPanel(new GridLayout(1, 3, 8, 0));
        platforms.setOpaque(false);
        for (YmirRuneRitual.Zone zone : YmirRuneRitual.Zone.values()) {
            JButton button = runeButton(zone.label().toUpperCase());
            int index = zone.ordinal();
            platformButtons[index] = button;
            button.addActionListener(e -> choosePlatform(zone));
            platforms.add(button);
        }
        middle.add(platforms);

        JLabel runeHint = new JLabel("Scegli la runa indicata dal verso della Rupe:", SwingConstants.CENTER);
        runeHint.setForeground(new Color(220, 189, 147));
        runeHint.setFont(new Font("Georgia", Font.PLAIN, 15));
        middle.add(runeHint);

        JPanel runes = new JPanel(new GridLayout(1, 3, 8, 0));
        runes.setOpaque(false);
        for (YmirRuneRitual.Rune rune : YmirRuneRitual.Rune.values()) {
            JButton button = runeButton(rune.label().toUpperCase());
            runeButtons[rune.ordinal()] = button;
            button.addActionListener(e -> tryRune(rune));
            runes.add(button);
        }
        middle.add(runes);
        interaction.add(middle, BorderLayout.CENTER);

        JPanel status = new JPanel(new BorderLayout(8, 8));
        status.setOpaque(false);
        JPanel health = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 2));
        health.setOpaque(false);
        JLabel healthLabel = new JLabel("VITA DI ARTHUR");
        healthLabel.setForeground(new Color(235, 219, 197));
        healthLabel.setFont(new Font("Georgia", Font.BOLD, 14));
        health.add(healthLabel);
        health.add(hearts);
        status.add(health, BorderLayout.NORTH);

        feedbackLabel.setHorizontalAlignment(SwingConstants.CENTER);
        feedbackLabel.setForeground(new Color(244, 222, 182));
        feedbackLabel.setFont(new Font("Georgia", Font.PLAIN, 15));
        status.add(feedbackLabel, BorderLayout.CENTER);

        JPanel tools = new JPanel(new GridLayout(1, 2, 10, 0));
        tools.setOpaque(false);
        JButton verses = runeButton("RILEGGI I VERSI");
        verses.addActionListener(e -> readClues());
        tools.add(verses);
        JButton retreat = runeButton("RITIRATI");
        retreat.addActionListener(e -> dispose());
        tools.add(retreat);
        status.add(tools, BorderLayout.SOUTH);
        interaction.add(status, BorderLayout.SOUTH);

        refreshPhase("Il cuore di Ymir scuote le pareti. Rileggi i versi per capire il rituale.");
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void readClues() {
        String text = YmirRuneRitual.steleInscription(ritual)
                + "\n\n--- L'INSEGNAMENTO DI GUNNAR ---\n"
                + "RADICE: la radice trattiene il mondo.\n"
                + "BATTITO: il cuore pulsa nel granito.\n"
                + "RESPIRO: il vento torna al silenzio.\n\n"
                + "Per ogni verso scegli la piattaforma e poi la runa.";
        JTextArea message = new JTextArea(text);
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setFont(new Font("Georgia", Font.PLAIN, 17));
        message.setBackground(new Color(35, 31, 44));
        message.setForeground(new Color(238, 224, 191));
        message.setMargin(new Insets(15, 17, 15, 17));
        JScrollPane scroll = new JScrollPane(message);
        scroll.setPreferredSize(new Dimension(650, 390));
        JOptionPane.showMessageDialog(this, scroll,
                "Il Diario di Arthur: la Rupe dell'Eco", JOptionPane.PLAIN_MESSAGE);
    }

    private static JButton runeButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Georgia", Font.BOLD, 14));
        button.setBackground(new Color(63, 43, 46));
        button.setForeground(new Color(248, 215, 157));
        button.setBorder(BorderFactory.createLineBorder(new Color(175, 115, 62), 2));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        return button;
    }

    private void choosePlatform(YmirRuneRitual.Zone zone) {
        selectedZone = zone;
        for (YmirRuneRitual.Zone other : YmirRuneRitual.Zone.values()) {
            platformButtons[other.ordinal()].setBackground(other == zone
                    ? new Color(129, 83, 49) : new Color(63, 43, 46));
        }
        feedbackLabel.setText("Piattaforma " + zone.label()
                + " selezionata. Traccia ora il sigillo corretto.");
        AudioManager.getInstance().playEffect(AudioManager.Effect.CLICK);
        for (JButton rune : runeButtons) rune.setEnabled(true);
    }

    private void tryRune(YmirRuneRitual.Rune rune) {
        if (selectedZone == null || phase >= ritual.size()) return;
        YmirRuneRitual.Step current = ritual.get(phase);
        if (!YmirRuneRitual.isCorrectStep(current, rune, selectedZone)) {
            state.takeDamage();
            AudioManager.getInstance().playEffect(AudioManager.Effect.DAMAGE);
            if (state.getCurrentHealth() <= 0) {
                state.setBossRetryAvailable(true);
                dispose();
                SceneManager manager = SceneManager.getInstance();
                GameOverPanel gameOver = (GameOverPanel) manager.getPanel("GAMEOVER");
                if (gameOver != null) {
                    gameOver.showReason("Ymir si è svegliato. La terra ha travolto Arthur.");
                }
                manager.showScene("GAMEOVER");
            } else {
                refreshPhase("La roccia si solleva! Combinazione errata: un cuore perduto.");
            }
            return;
        }
        phase++;
        AudioManager.getInstance().playEffect(AudioManager.Effect.SUCCESS);
        if (phase == ritual.size()) {
            state.setYmirPacified(true);
            state.setBossRetryAvailable(false);
            dispose();
            JOptionPane.showMessageDialog(getOwner(),
                    "Arthur traccia l'ultima runa.\n"
                    + "Le stalattiti si fermano. Ymir inspira lentamente e torna a dormire.\n"
                    + "Una Lacrima cristallina si deposita nella cavità del Cuore di Roccia.\n\n"
                    + "Raccoglila e completa l'Ymir's Old Fashioned nella Forgia!",
                    "Il gigante dorme ancora", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        refreshPhase("La runa risuona nella pietra. Il prossimo verso è pronto.");
    }

    private void refreshPhase(String feedback) {
        selectedZone = null;
        phaseLabel.setText("VERSO " + (phase + 1) + " / " + ritual.size());
        hintLabel.setText("<html><center>La terra trema: trova prima la <b>piattaforma sicura</b>,"
                + "<br>poi traccia il <b>sigillo</b> corrispondente al verso.</center></html>");
        feedbackLabel.setText("<html><center>" + feedback + "</center></html>");
        for (JButton button : platformButtons) button.setBackground(new Color(63, 43, 46));
        for (JButton button : runeButtons) button.setEnabled(false);
        hearts.refresh();
    }
}
