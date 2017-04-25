/*
 * Yass - Karaoke Editor
 * Copyright (C) 2009 Saruta
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package yass;

import com.jhlabs.image.TransformFilter;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.util.Enumeration;
import java.util.Map;

/**
 * Description of the Class
 *
 * @author Saruta
 */
public class YassSongFlow extends JPanel {
    private static final long serialVersionUID = 4307686015910924L;
    YassActions actions = null;
    YassSongList list = null;
    YassProperties prop = null;
    YassSongListModel sm = null;

    int pos = 0;
    int lastx = -1;
    long lasttime = 0;
    VolatileImage backVolImage = null, plainVolImage;
    private BufferedImage image;
    private boolean imageChanged = false;


    /**
     * Constructor for the YassSongFlow object
     *
     * @param a Description of the Parameter
     * @param l Description of the Parameter
     */
    public YassSongFlow(YassActions a, YassSongList l) {
        actions = a;
        list = l;
        prop = a.getProperties();

        setFocusable(true);
        sm = (YassSongListModel) list.getModel();

        addMouseMotionListener(
                new MouseMotionAdapter() {
                    public void mouseMoved(MouseEvent e) {
                        if (!hasFocus()) {
                            requestFocus();
                        }
                    }


                    public void mouseDragged(MouseEvent e) {
                        int x = e.getX();
                        long t = System.currentTimeMillis();
                        if ((t - lasttime) < 100) {
                            lastx = x;
                            return;
                        }

                        if (x < lastx) {
                            right();
                        } else if (x > lastx) {
                            left();
                        }
                        lasttime = t;
                        lastx = x;
                    }
                });
        addMouseListener(
                new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if (!hasFocus()) {
                            requestFocus();
                        }
                        lastx = e.getX();

                        animate();
                    }
                });

        addMouseWheelListener(
                new MouseWheelListener() {
                    public void mouseWheelMoved(MouseWheelEvent e) {
                        int count = e.getWheelRotation();
                        if (Math.abs(count) > 0) {
                            left();
                        } else {
                            right();
                        }
                    }
                });

        addKeyListener(
                new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                            left();
                            return;
                        }
                        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            right();
                            return;
                        }
                        list.dispatchEvent(e);
                        pos = list.getSelectedRow();
                        imageChanged = true;
                        repaint();
                    }


                    public void keyTyped(KeyEvent e) {
                        list.dispatchEvent(e);
                        repaint();
                    }
                });
    }

    /**
     * Gets the focusTraversable attribute of the YassSongFlow object
     *
     * @return The focusTraversable value
     */
    @SuppressWarnings("deprecation")
    public boolean isFocusTraversable() {
        return true;
    }

    /**
     * Description of the Method
     */
    public void left() {
        pos--;
        if (pos < 0) {
            pos = sm.getData().size() - 1;
        }
        list.setRowSelectionInterval(pos, pos);
        imageChanged = true;
        repaint();
    }

    /**
     * Description of the Method
     */
    public void right() {
        pos++;
        if (pos >= sm.getData().size()) {
            pos = 0;
        }
        list.setRowSelectionInterval(pos, pos);
        imageChanged = true;
        repaint();
    }

    /**
     * Description of the Method
     *
     * @param gr Description of the Parameter
     */
    public void paintComponent(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        Dimension clip = getSize();
        if (image == null || image.getWidth() != clip.width || image.getHeight() != clip.height) {
            // do not use INT_RGB for width>2000 (sun bug_id=5005969)
            // image = new BufferedImage(clip.width, clip.height, BufferedImage.TYPE_INT_ARGB);

            image = g.getDeviceConfiguration().createCompatibleImage(clip.width, clip.height, Transparency.TRANSLUCENT);
            backVolImage = g.getDeviceConfiguration().createCompatibleVolatileImage(clip.width, clip.height);
            plainVolImage = g.getDeviceConfiguration().createCompatibleVolatileImage(clip.width, clip.height);
            imageChanged = true;
        }
        refreshBackBuffer();
        Graphics2D gb = getBackBuffer().createGraphics();
        gb.drawImage(getPlainBuffer(), 0, 0, null);
        //if (getPlainBuffer().contentsLost())
        gb.dispose();
        paintBackBuffer(g);
    }

    /**
     * Description of the Method
     */
    public void paintImage() {
        int w = image.getWidth();
        int h = image.getHeight();

        Graphics2D g = image.createGraphics();

        g.setColor(getBackground());
        g.fillRect(0, 0, w, h);
        g.setColor(Color.black);

        if (sm.getData().size() < 1) {
            String s = I18.get("flow_empty");
            FontMetrics fm = g.getFontMetrics();
            g.drawString(s, w / 2 - fm.stringWidth(s) / 2, h / 2);
            return;
        }

        pos = list.getSelectedRow();
        if (pos < 0) {
            pos = 0;
        }

        Enumeration<YassSong> en = sm.getData().elements();
        int i = -1;
        while (en.hasMoreElements()) {
            YassSong s = en.nextElement();
            i++;

            if (i < pos - 2 || i > pos + 2) {
                continue;
            }

            ImageIcon icon = s.getIcon();
            if (icon == null) {
                continue;
            }

            Image img;
            try {
                File coverFile = new File(s.getDirectory() + File.separator + s.getCover());
                img = YassUtils.readImage(coverFile);
            } catch (Exception e) {
                img = null;
            }

            //Image img = icon.getImage();
            if (img == null) {
                continue;
            }
            int imgw = img.getWidth(null);
            int imgh = img.getHeight(null);

            if (i == (pos + 2)) {
                int ih = h - 80;
                int iw = 4 / 3 * ih;
                int w2 = 40;
                int h2 = 20;

                BufferedImage buf = (BufferedImage) img;

                com.jhlabs.image.GrayFilter gop = new com.jhlabs.image.GrayFilter();
                BufferedImage buf2 = gop.filter(buf, null);

                Graphics gb = buf2.getGraphics();
                gb.setColor(getBackground());
                gb.drawRect(0, 0, buf2.getWidth() - 1, buf2.getHeight() - 1);

                com.jhlabs.image.PerspectiveFilter op = new com.jhlabs.image.PerspectiveFilter(0, 0, w2, h2, w2, iw - h2, 0, ih);
                op.setEdgeAction(TransformFilter.CLAMP);
                op.setInterpolation(TransformFilter.BILINEAR);
                BufferedImage buf3 = op.filter(buf2, null);

                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(buf3, w - w2, 20, null);

                float opacity = 0.2f;
                float fadeHeight = 0.25f;
                BufferedImage reflection = new BufferedImage(imgw, imgh, BufferedImage.TYPE_INT_ARGB);
                Graphics2D rg = reflection.createGraphics();
                rg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                rg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                rg.drawRenderedImage((RenderedImage) img, null);
                rg.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
                rg.setPaint(new GradientPaint(
                        0, imgh * fadeHeight, new Color(0.0f, 0.0f, 0.0f, 0.0f),
                        0, imgh, new Color(0.0f, 0.0f, 0.0f, opacity)));
                rg.fillRect(0, 0, imgw, imgh);
                rg.dispose();

                AffineTransform aff = new AffineTransform(w2 / (float) imgw, 0, 0, -ih / (float) (3 * imgh), w - w2, 20 + ih + ih / 3);
                aff.shear(0, 20 * 3 / (float) ih);
                g.drawRenderedImage(reflection, aff);
            }
            if (i == (pos - 2)) {
                int ih = h - 80;
                int iw = 4 / 3 * ih;
                int w2 = 40;
                int h2 = 20;

                BufferedImage buf = (BufferedImage) img;

                com.jhlabs.image.GrayFilter gop = new com.jhlabs.image.GrayFilter();
                BufferedImage buf2 = gop.filter(buf, null);

                Graphics gb = buf2.getGraphics();
                gb.setColor(getBackground());
                gb.drawRect(0, 0, buf2.getWidth() - 1, buf2.getHeight() - 1);

                com.jhlabs.image.PerspectiveFilter op = new com.jhlabs.image.PerspectiveFilter(0, h2, w2, 0, w2, iw, 0, ih - h2);
                op.setEdgeAction(TransformFilter.CLAMP);
                op.setInterpolation(TransformFilter.BILINEAR);
                BufferedImage buf3 = op.filter(buf2, null);

                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(buf3, 0, 20, null);

                float opacity = 0.2f;
                float fadeHeight = 0.25f;
                BufferedImage reflection = new BufferedImage(imgw, imgh, BufferedImage.TYPE_INT_ARGB);
                Graphics2D rg = reflection.createGraphics();
                rg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                rg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                rg.drawRenderedImage((RenderedImage) img, null);
                rg.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
                rg.setPaint(new GradientPaint(
                        0, imgh * fadeHeight, new Color(0.0f, 0.0f, 0.0f, 0.0f),
                        0, imgh, new Color(0.0f, 0.0f, 0.0f, opacity)));
                rg.fillRect(0, 0, imgw, imgh);
                rg.dispose();

                AffineTransform aff = new AffineTransform(w2 / (float) imgw, 0, 0, -ih / (float) (3 * imgh), 0, ih + ih / 3);
                aff.shear(0, -20 * 3 / (float) ih);
                g.drawRenderedImage(reflection, aff);
            }

            if (i == (pos + 1)) {
                int ih = h - 60;
                int iw = 4 / 3 * ih;
                int w2 = w / 2 - iw / 2 - 40;
                int h2 = 10;

                BufferedImage buf = (BufferedImage) img;

                com.jhlabs.image.GrayFilter gop = new com.jhlabs.image.GrayFilter();
                BufferedImage buf2 = gop.filter(buf, null);

                float opacity = 0.2f;
                float fadeHeight = 0.25f;
                BufferedImage reflection = new BufferedImage(imgw, imgh, BufferedImage.TYPE_INT_ARGB);
                Graphics2D rg = reflection.createGraphics();
                rg.drawRenderedImage((RenderedImage) img, null);
                rg.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
                rg.setPaint(new GradientPaint(
                        0, imgh * fadeHeight, new Color(0.0f, 0.0f, 0.0f, 0.0f),
                        0, imgh, new Color(0.0f, 0.0f, 0.0f, opacity)));
                rg.fillRect(0, 0, imgw, imgh);
                rg.dispose();

                Graphics gb = buf2.getGraphics();
                gb.setColor(getBackground());
                gb.drawRect(0, 0, buf2.getWidth() - 1, buf2.getHeight() - 1);

                com.jhlabs.image.PerspectiveFilter op = new com.jhlabs.image.PerspectiveFilter(0, 0, w2, h2, w2, ih - h2, 0, ih);
                op.setEdgeAction(TransformFilter.CLAMP);
                op.setInterpolation(TransformFilter.BILINEAR);
                BufferedImage buf3 = op.filter(buf2, null);

                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(buf3, w / 2 + iw / 2, 10, null);

                AffineTransform aff = new AffineTransform(w2 / (float) imgw, 0, 0, -ih / (float) (3 * imgh), w / 2 + iw / 2, 10 + ih + ih / 3);
                aff.shear(0, 10 * 3 / (float) ih);
                g.drawRenderedImage(reflection, aff);
            }
            if (i == (pos - 1)) {
                int ih = h - 60;
                int iw = 4 / 3 * ih;
                int w2 = w / 2 - iw / 2 - 40;
                int h2 = 10;

                BufferedImage buf = (BufferedImage) img;

                com.jhlabs.image.GrayFilter gop = new com.jhlabs.image.GrayFilter();
                BufferedImage buf2 = gop.filter(buf, null);

                float opacity = 0.2f;
                float fadeHeight = 0.25f;
                BufferedImage reflection = new BufferedImage(imgw, imgh, BufferedImage.TYPE_INT_ARGB);
                Graphics2D rg = reflection.createGraphics();
                rg.drawRenderedImage((RenderedImage) img, null);
                rg.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
                rg.setPaint(new GradientPaint(
                        0, imgh * fadeHeight, new Color(0.0f, 0.0f, 0.0f, 0.0f),
                        0, imgh, new Color(0.0f, 0.0f, 0.0f, opacity)));
                rg.fillRect(0, 0, imgw, imgh);
                rg.dispose();

                Graphics gb = buf2.getGraphics();
                gb.setColor(getBackground());
                gb.drawRect(0, 0, buf2.getWidth() - 1, buf2.getHeight() - 1);
                gb.dispose();

                com.jhlabs.image.PerspectiveFilter op = new com.jhlabs.image.PerspectiveFilter(0, h2, w2, 0, w2, ih, 0, ih - h2);
                op.setEdgeAction(TransformFilter.CLAMP);
                op.setInterpolation(TransformFilter.BILINEAR);
                BufferedImage buf3 = op.filter(buf2, null);

                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(buf3, 40, 10, null);

                AffineTransform aff = new AffineTransform(w2 / (float) imgw, 0, 0, -ih / (float) (3 * imgh), 40, ih + ih / 3);
                aff.shear(0, -10 * 3 / (float) ih);
                g.drawRenderedImage(reflection, aff);
            }

            if (i == pos) {
                String t = s.getTitle();
                String a = s.getArtist();

                int ih = h - 60;
                int iw = 4 / 3 * ih;
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(img, w / 2 - iw / 2, 10, iw, ih, null);

                float opacity = 0.4f;
                float fadeHeight = 0.25f;
                BufferedImage reflection = new BufferedImage(imgw, imgh, BufferedImage.TYPE_INT_ARGB);
                Graphics2D rg = reflection.createGraphics();
                rg.drawRenderedImage((RenderedImage) img, null);
                rg.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
                rg.setPaint(new GradientPaint(
                        0, imgh * fadeHeight, new Color(0.0f, 0.0f, 0.0f, 0.0f),
                        0, imgh, new Color(0.0f, 0.0f, 0.0f, opacity)));
                rg.fillRect(0, 0, imgw, imgh);
                rg.dispose();
                g.drawRenderedImage(reflection, new AffineTransform(iw / (float) imgw, 0, 0, -ih / (float) (3 * imgh), w / 2 - iw / 2, 10 + ih + ih / 3));

                Font f = g.getFont();
                g.setFont(f.deriveFont(Font.BOLD));
                int sw = g.getFontMetrics().stringWidth(t);
                g.drawString(t, w / 2 - sw / 2, h - 25);

                g.setFont(f);
                sw = g.getFontMetrics().stringWidth(a);
                g.drawString(a, w / 2 - sw / 2, h - 14);

                boolean video = s.getComplete().equals("V");
                boolean perfect = s.getEdition().contains("[SC]");
                if (perfect && YassSongList.perfectIcon != null) {
                    g.drawImage(YassSongList.perfectIcon, w - 32, h - 32, null);
                }
                if (video && YassSongList.videoIcon != null) {
                    g.drawImage(YassSongList.videoIcon, w - 48, h - 32, null);
                }

                long duration = -1;
                String filename = s.getDirectory() + File.separator + s.getMP3();
                File file = new File(filename);
                if (file.exists()) {
                    try {
                        AudioInputStream in = AudioSystem.getAudioInputStream(file);
                        AudioFileFormat baseFileFormat = AudioSystem.getAudioFileFormat(file);
                        AudioFormat baseFormat = in.getFormat();
                        if (baseFileFormat instanceof TAudioFileFormat) {
                            Map<?, ?> properties = baseFileFormat.properties();
                            String key = "duration";
                            duration = ((Long) properties.get(key)).longValue();
                        }
                        in.close();
                    } catch (Exception ignored) {
                    }
                }
                if (duration > 0) {
                    int sec = (int) Math.round(duration / 1000000.0);
                    int min = sec / 60;
                    sec = sec - min * 60;
                    String dString = (sec < 10) ? min + ":0" + sec : min + ":" + sec;
                    g.drawString(dString, w - 48, h - 4);
                }
                String no = (i + 1) + "/" + sm.getData().size();
                g.drawString(no, 48, h - 4);

            }
        }
        g.dispose();
    }

    /**
     * Description of the Method
     */
    public void animate() {
        Graphics2D g = (Graphics2D) getGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (pos < 0) {
            return;
        }

        YassSong s = sm.getData().elementAt(pos);
        Image img;
        try {
            File coverFile = new File(s.getDirectory() + File.separator + s.getCover());
            img = YassUtils.readImage(coverFile);
        } catch (Exception e) {
            img = null;
        }
        if (img == null) {
            return;
        }

        BufferedImage buf = (BufferedImage) img;

        Dimension d = getSize();
        int w = d.width;
        int h = d.height;

        int ih = h - 60;
        int iw = 4 / 3 * ih;

        boolean notInterrupted = true;
        long nanoStart = System.nanoTime() / 1000L;
        long duration = 1000000L;

        int i = 0;
        while (notInterrupted) {
            long position = System.nanoTime() / 1000 - nanoStart;
            if (position >= duration) {
                break;
            }

            i += 2;
            if (i > 360) {
                break;
            }

            Graphics2D gb = getBackBuffer().createGraphics();
            gb.drawImage(getPlainBuffer(), 0, 0, null);

            gb.translate(w / 2, 10 + ih / 2);
            AffineTransform aff = new AffineTransform();
            if (i < 180) {
                aff.scale(1 + i / 180.0, 1 + i / 180.0);
            } else {
                aff.scale(2 - (i - 180) / 180.0, 2 - (i - 180) / 180.0);
            }
            aff.rotate((i % 360) / 360.0 * 2 * Math.PI);
            aff.translate(-iw / 2, -ih / 2);
            aff.scale(iw / (float) buf.getWidth(), ih / (float) buf.getHeight());
            gb.drawRenderedImage(buf, aff);
            gb.dispose();
            paintBackBuffer(g);

            try {
                Thread.currentThread();
                Thread.sleep(1);
            } catch (InterruptedException e) {
                notInterrupted = false;
            }
        }

        repaint();
    }

    /**
     * Gets the backBuffer attribute of the YassSongFlow object
     *
     * @return The backBuffer value
     */
    public VolatileImage getBackBuffer() {
        return backVolImage;
    }


    /**
     * Gets the plainBuffer attribute of the YassSongFlow object
     *
     * @return The plainBuffer value
     */
    public VolatileImage getPlainBuffer() {
        return plainVolImage;
    }


    /**
     * Description of the Method
     */
    public void refreshBackBuffer() {
        Graphics2D db = image.createGraphics();
        db.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //db.setTransform(identity);

        if (imageChanged) {
            paintImage();
            imageChanged = false;
        }

        Graphics2D gc = backVolImage.createGraphics();
        gc.drawImage(image, 0, 0, null);
        gc.dispose();

        gc = plainVolImage.createGraphics();
        gc.drawImage(image, 0, 0, null);
        gc.dispose();
    }


    /**
     * Description of the Method
     *
     * @param g Description of the Parameter
     */
    public void paintBackBuffer(Graphics2D g) {
        final int MAX_TRIES = 10;
        for (int i = 0; i < MAX_TRIES; i++) {
            g.drawImage(backVolImage, 0, 0, this);
            if (!backVolImage.contentsLost()) {
                return;
            }
            switch (backVolImage.validate(g.getDeviceConfiguration())) {
                case VolatileImage.IMAGE_INCOMPATIBLE:
                    backVolImage.flush();
                    backVolImage = g.getDeviceConfiguration().createCompatibleVolatileImage(image.getWidth(), image.getHeight());
                case VolatileImage.IMAGE_RESTORED:
                    Graphics2D gc = backVolImage.createGraphics();
                    gc.drawImage(image, 0, 0, Color.white, null);
                    gc.dispose();
                    break;
            }
        }
        g.drawImage(image, 0, 0, Color.white, null);
    }
}


