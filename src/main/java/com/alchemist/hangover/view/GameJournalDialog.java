package com.alchemist.hangover.view;

import com.alchemist.hangover.model.GameState;
import com.alchemist.hangover.model.Item;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Diario contestuale: ricostruito dai progressi gia' presenti in GameState.
 * Non introduce nuovi dati da salvare e non rivela automaticamente le ricette.
 */
public final class GameJournalDialog extends JDialog {
    private final GameState state = GameState.getInstance();

    public GameJournalDialog(Frame owner) {
        super(owner, "Diario di Arthur", true);
        setSize(780, 560);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(new Color(27, 22, 30));
        content.setBorder(new EmptyBorder(24, 30, 20, 30));

        JLabel header = new JLabel("DIARIO DI ARTHUR", SwingConstants.CENTER);
        header.setFont(new Font("Georgia", Font.BOLD, 27));
        header.setForeground(new Color(239, 203, 129));
        content.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Georgia", Font.BOLD, 15));
        tabs.addTab("Obiettivi", page(goals()));
        tabs.addTab("Indizi e ricette", page(recipes()));
        tabs.addTab("Cronache", page(story()));
        content.add(tabs, BorderLayout.CENTER);

        JButton close = new JButton("Chiudi il diario");
        close.setFont(new Font("Georgia", Font.BOLD, 15));
        close.addActionListener(e -> dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(close);
        content.add(footer, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JScrollPane page(String body) {
        JTextArea text = new JTextArea(body);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setMargin(new Insets(20, 24, 20, 24));
        text.setFont(new Font("Georgia", Font.PLAIN, 17));
        text.setBackground(new Color(48, 41, 44));
        text.setForeground(new Color(238, 223, 194));
        text.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(text);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    private String goals() {
        if (state.getChapter() >= 3) {
            return "Una nuova tappa del viaggio mi attende. Devo osservare il mondo e capire come proseguire.";
        }
        if (state.getChapter() == 2) {
            return state.isBossDefeated()
                    ? "La dea Rán è stata placata. Ora posso tornare al calderone e completare la preparazione."
                    : "Esplorare il regno sommerso, raccogliere ingredienti e scoprire il segreto di Rán."
                    + (state.hasItem("DIARIO_CAPITANO") ? "\n\nIl diario del Capitano contiene informazioni da rileggere." : "");
        }
        if (!state.hasGivenMapToMartino()) {
            return "La nave vola sopra le nuvole e Martino sembra conoscere la rotta."
                    + "\n\nDevo esplorare la stiva e trovare la Mappa del Cielo per il capitano.";
        }
        if (!state.isBossDefeated()) {
            return "Martino ha ricevuto la mappa. Ora possiedo la Chiave del Drago."
                    + "\n\nDevo raggiungere la polena e scoprire cosa nasconde, poi affrontare la tempesta.";
        }
        return "Ho superato la prova di Hræsvelgr. Mi resta da preparare la miscela nel calderone.";
    }

    private String recipes() {
        StringBuilder text = new StringBuilder("Annotazioni alchemiche\n\n");
        boolean found = false;
        for (Item item : state.getInventory()) {
            if ("CHIAVE_DRAGO".equals(item.getId()) || "MAPPA_CIELO".equals(item.getId())) continue;
            text.append("• ").append(item.getName()).append("\n");
            text.append("   ").append(item.getDescription()).append("\n\n");
            found = true;
        }
        if (!found) text.append("Non ho ancora trovato ingredienti utili.\n\n");
        text.append("Gli ingredienti reagiscono tra loro: devo fare attenzione alle proprietà descritte.");
        return text.toString();
    }

    private String story() {
        StringBuilder text = new StringBuilder();
        text.append("CAPITOLO I — Il segreto del calderone\n")
                .append("Mi sono risvegliato sullo Skíðblaðnir, un veliero impossibile.\n");
        if (state.hasGivenMapToMartino()) {
            text.append("\nHo consegnato a Martino la Mappa del Cielo e ottenuto la Chiave del Drago.\n");
        }
        if (state.getChapter() >= 2) {
            text.append("\nCAPITOLO II — Un nuovo regno\n")
                    .append("Il viaggio mi ha trascinato in un luogo sommerso, dominato dalla dea Rán.\n");
        }
        if (state.getChapter() >= 2 && state.isBossDefeated()) {
            text.append("\nRán è stata sconfitta; il suo dono è entrato nel mio inventario.\n");
        }
        return text.toString();
    }
}
