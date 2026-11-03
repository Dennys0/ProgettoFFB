package com.alchemist.hangover.view.dialogue;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

/**
 * La cornice cinematografica condivisa da intro e capitoli.
 * Mantiene l'asset originale dialogue_box.png e le medesime coordinate
 * per nome, testo e pulsante "mostra frase".
 *
 * Il pannello non conosce i contenuti o gli effetti narrativi: il
 * DialogueSystem resta la fonte dei dialoghi e del loro stato.
 */
public final class DialogueBox extends JPanel {
    private static final String ART_PATH = "/images/ui/dialogue_box.png";
    private static final Image ARTWORK = loadArtwork();
    private final JLabel speakerLabel = new JLabel("");
    private final JLabel textLabel = new JLabel("");
    private final JLabel revealLabel = new JLabel("MOSTRA FRASE", SwingConstants.RIGHT);

    public DialogueBox() {
        setLayout(null);
        setOpaque(false);
        setSize(1200, 200);

        speakerLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        speakerLabel.setForeground(new Color(255, 215, 0));
        speakerLabel.setBounds(65, 25, 500, 30);
        add(speakerLabel);

        textLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        textLabel.setForeground(Color.WHITE);
        textLabel.setVerticalAlignment(SwingConstants.TOP);
        textLabel.setBounds(65, 60, 1070, 95);
        add(textLabel);

        revealLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        revealLabel.setForeground(new Color(255, 218, 145));
        revealLabel.setBounds(950, 155, 180, 25);
        revealLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        revealLabel.setToolTipText("Mostra subito tutta la frase corrente");
        add(revealLabel);
    }

    public JLabel getSpeakerLabel() { return speakerLabel; }
    public JLabel getTextLabel() { return textLabel; }
    public JLabel getRevealLabel() { return revealLabel; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g.create();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (ARTWORK != null) {
                graphics.drawImage(ARTWORK, 0, 0, getWidth(), getHeight(), this);
            } else {
                graphics.setColor(new Color(20, 20, 20, 230));
                graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
            }
        } finally {
            graphics.dispose();
        }
    }

    /**
     * Un solo punto per caricare i ritratti, mantenendo il rapporto d'aspetto
     * gia' utilizzato dall'intro e dai primi due capitoli.
     */
    public static ImageIcon scaledPortrait(String resource, int height) {
        URL url = DialogueBox.class.getResource(resource);
        if (url == null) return null;
        Image image = new ImageIcon(url).getImage()
                .getScaledInstance(-1, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private static Image loadArtwork() {
        URL url = DialogueBox.class.getResource(ART_PATH);
        return url == null ? null : new ImageIcon(url).getImage();
    }
}
