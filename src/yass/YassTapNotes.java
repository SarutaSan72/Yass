package yass;

import java.awt.Component;
import java.util.Vector;

public class YassTapNotes {
	public static void evaluateTaps(Component parent, YassTable table, Vector<Long> taps) {
		if (taps==null) return;
		int n = taps.size();
		if (n<2) { 
			taps.clear();
			return;
		}		
		if (n%2==1) { taps.removeElementAt(n-1); n--; }
		
		int tn=table.getRowCount(); 
		YassTableModel tm = (YassTableModel)table.getModel();		
		
		double gap = table.getGap();
		double bpm = table.getBPM();
		
		// get first note that follows selection
		int t = table.getSelectionModel().getMinSelectionIndex();
		if (t<0) t=0; 
		while (t<tn) {
			YassRow r = table.getRowAt(t);
			if (r.isNote()) break;
			t++;
		}
		
		int k=0;
		while (k<n && t<tn) {
			YassRow r = table.getRowAt(t++);
			if (r.isNote()) {
				int beat = r.getBeatInt();
				int length = r.getLengthInt();
				
				long tapBeat = ((Long)taps.elementAt(k++)).longValue();
				long tapBeat2 = ((Long)taps.elementAt(k++)).longValue();
				
				double ms = tapBeat/1000.0 - gap;
				double ms2 = tapBeat2/1000.0 - gap;
				beat = (int)Math.round((4*bpm*ms/(60*1000)));
				int beat2 = (int)Math.round((4*bpm*ms2/(60*1000)));
				
				length = beat2-beat;
				
				if (length<1) length=1;
				r.setBeat(beat);
				r.setLength(length);
			}
		}
		
		tm.fireTableDataChanged();
		table.addUndo();
		table.repaint();
		taps.clear();
	}
}