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

import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 * Description of the Interface
 *
 * @author Saruta
 */
public interface YassPlaybackRenderer {

    /**
     * Description of the Method
     *
     * @param s    Description of the Parameter
     */
    void init(YassSession s);


    /**
     * Gets the session attribute of the YassPlaybackRenderer object
     *
     * @return The session value
     */
    YassSession getSession();

    /**
     * Sets the pause attribute of the YassPlaybackRenderer object
     *
     * @param onoff The new pause value
     */
    void setPause(boolean onoff);


    /**
     * Sets the message attribute of the PlaybackRenderer object
     *
     * @param msg The new message value
     */
    void setMessage(String msg);


    /**
     * Sets the errorMessage attribute of the YassPlaybackRenderer object
     *
     * @param msg The new errorMessage value
     */
    void setErrorMessage(String msg);


    /**
     * Description of the Method
     *
     * @param currentMillis Description of the Parameter
     * @param endMillis     Description of the Parameter
     * @return Description of the Return Value
     */
    boolean preparePlayback(long currentMillis, long endMillis);


    /**
     * Description of the Method
     */
    void init();


    /**
     * Description of the Method
     */
    void startPlayback();


    /**
     * Description of the Method
     *
     * @param currentMillis Description of the Parameter
     */
    void updatePlayback(long currentMillis);


    /**
     * Description of the Method
     */
    void finishPlayback();

    /**
     * Gets the playbackInterrupted attribute of the PlaybackRenderer object
     *
     * @return The playbackInterrupted value
     */
    boolean isPlaybackInterrupted();

    /**
     * Sets the playbackInterrupted attribute of the PlaybackRenderer object
     *
     * @param onoff The new playbackInterrupted value
     */
    void setPlaybackInterrupted(boolean onoff);

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    boolean showVideo();


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    boolean showBackground();


    /**
     * Sets the videoFrame attribute of the YassPlaybackRenderer object
     *
     * @param videoFrame The new videoFrame value
     */
    void setVideoFrame(BufferedImage videoFrame);


    /**
     * Sets the backgroundImage attribute of the YassPlaybackRenderer object
     *
     * @param i The new backgroundImage value
     */
    void setBackgroundImage(BufferedImage i);


    /**
     * Gets the component attribute of the YassPlaybackRenderer object
     *
     * @return The component value
     */
    JComponent getComponent();
}

