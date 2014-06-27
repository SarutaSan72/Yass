/******************************************************************************
* FOBS Java CrossPlatform JMF PlugIn
* Copyright (c) 2004 Omnividea Multimedia S.L
*
*    This file is part of FOBS.
*
*    FOBS is free software; you can redistribute it and/or modify
*    it under the terms of the GNU Lesser General Public License as
*    published by the Free Software Foundation; either version 2.1 
*    of the License, or (at your option) any later version.
*
*    FOBS is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Lesser General Public License for more details.
*
*    You should have received a copy of the GNU Lesser General Public
*    License along with FOBS; if not, write to the Free Software
*    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
******************************************************************************/

/*
 * Created on 06.02.2005 by Robert Binna
 * ChangeLog:
 *  Jose San Pedro Wandelmer 2005/02/09 - Major changes to code structure
 *      - Package changed to fit Fobs package naming
 *      - Automatic Generated comments removed
 *      - Header included with license information
 *      - Some println's introduced to help developers know the renderer is being used
 *      - Thanks to Robert Binna for his contribution to the project!!
 *  Robert Hastings 2007/01/04
 *      - Native Library Location routines
 *      - Improvements to frame buffer management
 *  Keith 2007/01/10
 *	- Renderer class now extends JPanel to manage repaint events (so video gets refreshed even when paused!)
 */

package com.omnividea.media.renderer.video;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.RGBFormat;
import javax.media.renderer.VideoRenderer;
import javax.media.util.ImageToBuffer;
import javax.swing.JPanel;

import com.omnividea.FobsConfiguration;

public class Java2DRenderer extends JPanel implements VideoRenderer, FrameGrabbingControl  {
	
    private static final long serialVersionUID = 981331562759641600L;

    public Java2DRenderer() {
        //panel.setIgnoreRepaint(true);
	    setIgnoreRepaint(true);
    }
    
	private RGBFormat vf;
	private Image lastImage = null;
        
    void setValue(Object aValue,boolean isSelected) {
        System.out.println(aValue.getClass().getName());
    }

    public Format[] getSupportedInputFormats() {return new Format[] {new RGBFormat() };}

    public Format setInputFormat(Format format) {
        System.out.println("Fobs Java2DRenderer: setInputFormat");
        FobsConfiguration.videoFrameFormat=FobsConfiguration.RGBA;

        vf = (RGBFormat) format;
	int formatWidth = (int) vf.getSize().getWidth();
	int formatHeight = (int) vf.getSize().getHeight();
	this.setPreferredSize(new Dimension(formatWidth,formatHeight));
	return format;
    }

    public void start() {
        System.out.println("Fobs Java2DRenderer: start");
    }

    public void stop() {
        System.out.println("Fobs Java2DRenderer: stop");        
    }

    public int process(Buffer buffer) {
		Graphics2D g2d = (Graphics2D) this.getGraphics();					    
				    
		if(g2d != null) {
			if(lastImage == null) lastImage = bufferToImage(buffer);
			g2d.drawImage(lastImage, 0, 0, this.getWidth(),this.getHeight(), null);
			//g2d.dispose();
		}
		
		return BUFFER_PROCESSED_OK;
    }
    
    protected void paintComponent(Graphics g) { 
        if(lastImage != null) g.drawImage(lastImage, 0, 0, this.getWidth(),this.getHeight(), null); 
    }
    
    private BufferedImage bufferToImage(Buffer buffer) {
       RGBFormat format = (RGBFormat) buffer.getFormat();
       int rMask, gMask, bMask;
       Object data = buffer.getData();
       DirectColorModel dcm;

       rMask = format.getRedMask();
       gMask = format.getGreenMask();
       bMask = format.getBlueMask();
       int [] masks = new int[3];
       masks[0] = rMask;
       masks[1] = gMask;
       masks[2] = bMask;

       DataBuffer db = new DataBufferInt((int[])data,
                         format.getLineStride() *
                         format.getSize().height);

       SampleModel sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT,
                                 format.getLineStride(),
                                 format.getSize().height,
                                 masks);
       WritableRaster wr = Raster.createWritableRaster(sm, db, new Point(0, 0));

       dcm = new DirectColorModel(24, rMask, gMask, bMask);
       return new BufferedImage(dcm, wr, true, null);
   }


    public String getName() {
        return "Fobs Java2DRenderer";
    }

    public void open() throws ResourceUnavailableException {
        
    }

    public void close() {
        lastImage = null;
    }

    public void reset() {
        lastImage = null;
    }

    public Component getComponent() {
        return this;
    }

    public boolean setComponent(Component arg0) {
        return false;
    }


/*    public void setBounds(Rectangle arg0) {
        System.out.println("set bounds :: " + arg0);
        
    }

    public Rectangle getBounds() {
        return null;
    }*/


  // support for FrameGrabbingControl 
  public Buffer grabFrame() { 
	Buffer buf = null;
	if(lastImage != null) buf = ImageToBuffer.createBuffer(lastImage,(float)0); 
	return buf; 
  } 
   
  // No awt component is needed for FrameGrabbingControl 
  public Component getControlComponent() { 
    return null; 
  } 
   
  public Object[] getControls() { 
    Object[] obj = { this }; 
    return obj; 
  } 
   
  public Object getControl(String arg) { 
    Object rc = null; 
    if(arg.equals("javax.media.control.FrameGrabbingControl")) rc = this; 
    return rc; 
  } 
	
}
