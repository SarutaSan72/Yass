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

package yass.renderer;

import yass.YassProperties;
import yass.screen.YassTheme;

import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * Description of the Interface
 *
 * @author Saruta
 */
public interface YassPlaybackRenderer {

    /**
     * Description of the Field
     */
    public final static int FOUR_TO_THREE = 0;
    /**
     * Description of the Field
     */
    public final static int SIXTEEN_TO_NINE = 1;


    /**
     * Gets the iD attribute of the YassPlaybackRenderer object
     *
     * @return The iD value
     */
    public String getID();


    /**
     * Description of the Method
     *
     * @param s    Description of the Parameter
     * @param prop Description of the Parameter
     * @param t    Description of the Parameter
     */
    public void init(YassSession s, YassTheme t, YassProperties prop);


    /**
     * Gets the session attribute of the YassPlaybackRenderer object
     *
     * @return The session value
     */
    public YassSession getSession();


    /**
     * Sets the ratio attribute of the YassPlaybackRenderer object
     *
     * @param ratio The new ratio value
     */
    public void setRatio(int ratio);


    /**
     * Sets the pause attribute of the YassPlaybackRenderer object
     *
     * @param onoff The new pause value
     */
    public void setPause(boolean onoff);


    /**
     * Sets the message attribute of the PlaybackRenderer object
     *
     * @param msg The new message value
     */
    public void setMessage(String msg);


    /**
     * Sets the errorMessage attribute of the YassPlaybackRenderer object
     *
     * @param msg The new errorMessage value
     */
    public void setErrorMessage(String msg);


    /**
     * Description of the Method
     *
     * @param currentMillis Description of the Parameter
     * @param endMillis     Description of the Parameter
     * @return Description of the Return Value
     */
    public boolean preparePlayback(long currentMillis, long endMillis);


    /**
     * Description of the Method
     */
    public void init();


    /**
     * Description of the Method
     */
    public void startPlayback();


    /**
     * Description of the Method
     *
     * @param currentMillis Description of the Parameter
     */
    public void updatePlayback(long currentMillis);


    /**
     * Description of the Method
     */
    public void finishPlayback();

    /**
     * Gets the playbackInterrupted attribute of the PlaybackRenderer object
     *
     * @return The playbackInterrupted value
     */
    public boolean isPlaybackInterrupted();

    /**
     * Sets the playbackInterrupted attribute of the PlaybackRenderer object
     *
     * @param onoff The new playbackInterrupted value
     */
    public void setPlaybackInterrupted(boolean onoff);

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean showVideo();


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public boolean showBackground();


    /**
     * Sets the videoFrame attribute of the YassPlaybackRenderer object
     *
     * @param videoFrame The new videoFrame value
     */
    public void setVideoFrame(BufferedImage videoFrame);


    /**
     * Sets the backgroundImage attribute of the YassPlaybackRenderer object
     *
     * @param i The new backgroundImage value
     */
    public void setBackgroundImage(BufferedImage i);


    /**
     * Gets the component attribute of the YassPlaybackRenderer object
     *
     * @return The component value
     */
    public JComponent getComponent();
}

