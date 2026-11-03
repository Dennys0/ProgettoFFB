package com.alchemist.hangover.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/** Cache LRU delle sole immagini ridimensionate, limitata a 32 MiB di pixel ARGB. */
public final class ImageAssets {
    /** Identifica una variante grafica attraverso percorso e dimensioni richieste. */
    private record Key(String path, int width, int height) { }
    private static final long LIMIT = 32L * 1024 * 1024;
    private static final Map<Key, ImageIcon> CACHE = new LinkedHashMap<>(16, .75f, true);
    private static long bytes;
    private ImageAssets() { }

    /**
     * Restituisce una variante ridimensionata condivisa; width=-1 conserva le proporzioni.
     * Una risorsa assente o illeggibile produce null. Il primo caricamento è sincrono:
     * la cache evita le elaborazioni successive, ma non crea un thread per la lettura.
     */
    public static synchronized ImageIcon scaled(String path, int width, int height) {
        if (path == null || path.isBlank()) return null;
        if ((width != -1 && width <= 0) || height <= 0) throw new IllegalArgumentException("Dimensioni non valide");
        Key key = new Key(path, width, height);
        ImageIcon cached = CACHE.get(key);
        if (cached != null) return cached;
        java.net.URL resource = ImageAssets.class.getResource(path);
        if (resource == null) return null;
        try {
            BufferedImage source = ImageIO.read(resource);
            if (source == null) return null;
            int targetWidth = width == -1 ? Math.max(1, (int)Math.round((double)source.getWidth() * height / source.getHeight())) : width;
            BufferedImage image = new BufferedImage(targetWidth, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.drawImage(source, 0, 0, targetWidth, height, null);
            } finally { g.dispose(); }
            ImageIcon result = new ImageIcon(image);
            long size = (long)targetWidth * height * 4;
            // Elimina prima le immagini usate meno di recente; il conto riguarda i pixel,
            // non tutta la memoria occupata dagli oggetti Java o dalle immagini originali.
            while (!CACHE.isEmpty() && bytes + size > LIMIT) {
                var iterator = CACHE.entrySet().iterator();
                ImageIcon removed = iterator.next().getValue();
                bytes -= (long)removed.getIconWidth() * removed.getIconHeight() * 4;
                iterator.remove();
            }
            // Un’immagine troppo grande viene restituita senza trattenerla nella cache.
            if (size <= LIMIT) { CACHE.put(key, result); bytes += size; }
            return result;
        } catch (java.io.IOException ex) {
            System.err.println("Immagine non leggibile: " + path);
            return null;
        }
    }
}
