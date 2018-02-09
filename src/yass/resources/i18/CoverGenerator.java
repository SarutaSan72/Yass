package i18;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.*;
import javax.imageio.stream.*;

public class CoverGenerator {
	private static boolean standalone = false;

	public static void main(String argv[]) {
		standalone = true;
		System.out.println("Defaults:");
		System.out.println("   -size 100");
		System.out.println("   -x 0 -y 0");
		System.out.println("   -image image.png");
		System.out.println("   -overlay overlay.png -opacity 0.9");
		System.out.println("   -overlay2 overlay2.png -opacity2 1");
		System.out.println("   -background ECECEC -foreground CCCCCC");
		System.out.println("   -font SansSerif -fontsize 72 -bold true");
		System.out.println("   -fontsize 0 (autoscale)");
		System.out.println("   -fontscale 1.5");
		System.out.println("   -quality 0.95");
		System.out.println("   -template [group|cover]");
		System.out.println("   -dir .");
		System.out.println("Examples:");
		System.out.println("   Label1 Label2 Label3_With_Spaces");
		System.out.println("   To print A-Z: -from 65 -to 90");
		System.out.println("   To print 0-9: -from 48 -to 57");
		System.out.println("   To print 10-20: -from 10 -to 20 -unicode false");
		System.out.println("   To print unicode check mark: -from u2713");
	      System.out.println();
	      System.out.print("Commands: ");
	      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
         		String cmd = br.readLine();
			createImages(argv, cmd);
      	} catch (Exception e) {}
	}

	public static void createImages(String argv[], String cmd) {
		if (cmd.length()>1) argv = cmd.split(" ");
		if (argv==null || argv.length<1) return;

		int ICON_WIDTH = 100;
		int ICON_HEIGHT = 100;
		Color fgColor = new Color(204,204,204);
		Color bgColor = new Color(236,236,236);
		String image = "image.png";
		String overlay = "overlay.png";
		String overlay2 = "overlay2.png";
		String fontname = "SansSerif";
		String dir  = null;
		int fontsize = 72;
		boolean bold=true;
		boolean unicode=true;
		double quality = 0.95;
		double opacity = 0.9;
		double opacity2 = 1;
		double fontscale = 1.5;
		boolean fontmatch = true;
		int from=-1; 
		int to=-1;
		int x=0, y=0;

		for (int i=0; i<argv.length; i++) {
		if (argv[i].equals("-size")) {
			ICON_WIDTH = ICON_HEIGHT = Integer.parseInt(argv[i + 1]);
		}
		if (argv[i].equals("-background")) {
			int val = 0; 
			try { val = Integer.parseInt(argv[i+1],16); }catch(Exception e){}
			bgColor = new Color(val);
		}
		if (argv[i].equals("-foreground")) {
			int val = 0; 
			try { val = Integer.parseInt(argv[i+1],16); }catch(Exception e){}
			fgColor = new Color(val);
		}
		if (argv[i].equals("-image")) {
			image=argv[i+1];
		}
		if (argv[i].equals("-overlay")) {
			overlay=argv[i+1];
		}
		if (argv[i].equals("-overlay2")) {
			overlay2=argv[i+1];
		}
		if (argv[i].equals("-font")) {
			fontname=argv[i+1];
		}
		if (argv[i].equals("-fontsize")) {
			fontsize= Integer.parseInt(argv[i + 1]);
			fontmatch = (fontsize==0);
		}
		if (argv[i].equals("-dir")) {
			dir=argv[i+1];
		}
		if (argv[i].equals("-x")) {
			x= Integer.parseInt(argv[i + 1]);
		}
		if (argv[i].equals("-y")) {
			y= Integer.parseInt(argv[i + 1]);
		}
		if (argv[i].equals("-bold")) {
			bold=argv[i+1].equals("true");
		}
		if (argv[i].equals("-unicode")) {
			unicode=argv[i+1].equals("true");
		}
		if (argv[i].equals("-quality")) {
			try { quality= Double.parseDouble(argv[i + 1]); }catch(Exception e){}
		}
		if (argv[i].equals("-opacity")) {
			try { opacity= Double.parseDouble(argv[i + 1]); }catch(Exception e){}
		}
		if (argv[i].equals("-fontscale")) {
			try { fontscale= Double.parseDouble(argv[i + 1]); }catch(Exception e){}
		}
		if (argv[i].equals("-opacity2")) {
			try { opacity2= Double.parseDouble(argv[i + 1]); }catch(Exception e){}
		}
		if (argv[i].equals("-template")) {
			if (argv[i+1].equals("group")) {
				ICON_WIDTH = ICON_HEIGHT = 100;
				fontname="Verdana";
				fontsize=60;
				bold=true;
				x=0;
				y=0;
				fgColor = new Color(153,153,153);
				bgColor = new Color(236,236,236);
				fontscale=1;
				image="group_image.jpg";
				overlay="group_overlay.png";
				overlay2="group_overlay2.png";
			}
			if (argv[i+1].equals("cover")) {
				ICON_WIDTH = ICON_HEIGHT = 500;
				fontname="Verdana";
				fontsize=32;
				bold=false;
				x=196;
				y=418;
				fgColor = new Color(0,0,0);
				bgColor = new Color(236,236,236);
				fontscale=1.5;
				image="cover_image.png";
				overlay="cover_overlay.png";
				overlay2="cover_overlay2.png";
			}
		}
		if (argv[i].equals("-from")) {
			if (argv[i+1].startsWith("u")) {
				try { from = Integer.parseInt(argv[i+1].substring(1),16); }catch(Exception e){}
			} else {from = Integer.parseInt(argv[i + 1]);}
		}
		if (argv[i].equals("-to")) {
			if (argv[i+1].startsWith("u")) {
				try { to = Integer.parseInt(argv[i+1].substring(1),16); }catch(Exception e){}
			} else {to = Integer.parseInt(argv[i + 1]);}
		}
		}
		
		if (from>=0) {
			if (to<0) to=from;
			int len = to-from+1;
			int last= argv.length;
			String[] tmp = new String[last+len];
			System.arraycopy(argv, 0, tmp, 0, argv.length);
   			argv = tmp;
   			for (int i=0; i<len;i++) {
				if (unicode) argv[last+i]=new String(Character.toChars(from+i));
				else argv[last+i] = ""+(from+i);
			}
		}

		boolean skip=false;
		for (int i=0; i<argv.length; i++) {
			if (argv[i].startsWith("-")) {
				skip=true;
				continue;
			}
			if (skip) {
				skip=false;
				continue;
			}
			String label = argv[i].replace('_',' ');
						
			BufferedImage img = null;
			BufferedImage img2 = null;
			BufferedImage img3 = null;
			try {
				img = ImageIO.read(new File(image));
			} catch(Exception e){}			
			try {
				img2 = ImageIO.read(new File(overlay));
			} catch(Exception e){}			
			try {
				img3 = ImageIO.read(new File(overlay2));
			} catch(Exception e){}			


			BufferedImage bufferedImage = new BufferedImage(ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bufferedImage.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);		
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			g2d.setColor(bgColor);
			g2d.fillRect(0,0,ICON_WIDTH,ICON_HEIGHT);
			if (img!=null) {
				g2d.drawImage(img, 0,0, ICON_WIDTH, ICON_HEIGHT, null);
			      img.flush();
			}
			if (img2!=null) {
				float[] scales = { 1f, 1f, 1f, (float)opacity };
				float[] offsets = new float[4];
   				RescaleOp op = new RescaleOp(scales, offsets, null);
				BufferedImage dimg2 = op.filter(img2,null);

				g2d.drawImage(dimg2, 0,0, ICON_WIDTH, ICON_HEIGHT, null);
			      dimg2.flush();
			}
			if (img3!=null) {
				float[] scales = { 1f, 1f, 1f, (float)opacity2 };
				float[] offsets = new float[4];
   				RescaleOp op = new RescaleOp(scales, offsets, null);
				BufferedImage dimg3 = op.filter(img3,null);

				g2d.drawImage(dimg3, 0,0, ICON_WIDTH, ICON_HEIGHT, null);
			      dimg3.flush();
			}


			g2d.setColor(fgColor);
			Font font = null;
			if (!fontmatch) font = new Font(fontname, bold?Font.BOLD:Font.PLAIN, fontsize);
			else {
				Rectangle2D box=null;
				fontsize = ((int)((ICON_HEIGHT * 3/4) / 8))*8 + 4;
				do {
				fontsize -=4;
				font = new Font(fontname, bold?Font.BOLD:Font.PLAIN, fontsize);

				g2d.setFont(font);
				FontMetrics metrics = g2d.getFontMetrics();
				box = metrics.getStringBounds(label, g2d);
				} while (box.getWidth() > ICON_WIDTH-20 && fontsize>=16);						
			}


			g2d.setFont(font);
			FontMetrics metrics = g2d.getFontMetrics();
			Rectangle2D box = metrics.getStringBounds(label, g2d);
			if (y==0) {
				int xx= (int)(ICON_WIDTH/2-box.getWidth()/2);
				int yy= (int)(ICON_HEIGHT/2-box.getHeight()/2+metrics.getAscent());
				g2d.drawString(label, xx,yy);
			}
			else {
				g2d.translate(x,y);
				g2d.scale(fontscale,1);
				g2d.drawString(label, 0,0);
			}

			g2d.dispose();

			try{	
				Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
				ImageWriter writer = (ImageWriter)iter.next();
				ImageWriteParam iwp = writer.getDefaultWriteParam();
				iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				iwp.setCompressionQuality((float)quality);
				
				label = label.replace('?','_');
				label = label.replace('*','_');
				label = label.replace('<','_');
				label = label.replace('|','_');
				label = label.replace('/','_');
				label = label.replace('\\','_');
				label = label.replace(':','_');
				label = label.replace('\"','_');

				String filename = label+".jpg";
				File file = new File(dir, filename);
				if (file.exists()) file.delete();
				
				FileImageOutputStream output = new FileImageOutputStream(new File(dir, filename));
				writer.setOutput(output);
				IIOImage ioimage = new IIOImage(bufferedImage, null, null);
				writer.write(null, ioimage, iwp);
				writer.dispose();
				output.close();
				
				if (standalone) System.out.println(filename);
			}catch(Exception e){}			
		}
	}	
}