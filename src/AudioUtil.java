import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.Control.Type;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class AudioUtil {
	
	
	public static void main(String[] args){
//		setMasterOutputVolume(.2f);
//		
//		try {
//			System.in.read();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		new GetKeycode();
	}
	
	
	
	
	
	public static class GetKeycode implements KeyListener{

	    private JFrame f;
	    private JLabel feld;

	    public GetKeycode(){
	        f = new JFrame("GetKeycode");
	        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        f.addKeyListener(this);
	        feld = new JLabel();
	        f.add(feld);
	        f.pack();
	        f.setVisible(true);
	    }

	    @Override
	    public void keyReleased(KeyEvent e) {
	        feld.setText(e.getKeyCode()+"");        
	    }

	    // Unused:
	    @Override public void keyPressed(KeyEvent e) {}

	    @Override public void keyTyped(KeyEvent arg0) {}

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public static void setMasterOutputVolume(float value) {
		if (value < 0 || value > 1)
			throw new IllegalArgumentException(
					"Volume can only be set to a value from 0 to 1. Given value is illegal: " + value);
		Line line = getMasterOutputLine();
		if (line == null) throw new RuntimeException("Master output port not found");
		boolean opened = open(line);
		try {
			FloatControl control = getVolumeControl(line);
			if (control == null)
				throw new RuntimeException("Volume control not found in master port: " + toString(line));
			control.setValue(value);
		} finally {
			if (opened) line.close();
		}
	}
	
	
	public static Float getMasterOutputVolume() {
		Line line = getMasterOutputLine();
		if (line == null) return null;
		boolean opened = open(line);
		try {
			FloatControl control = getVolumeControl(line);
			if (control == null) return null;
			return control.getValue();
		} finally {
			if (opened) line.close();
		}
	}
	
	
	public static Line getMasterOutputLine() {
		for (Mixer mixer : getMixers()) {
			for (Line line : getAvailableOutputLines(mixer)) {
				if (line.getLineInfo().toString().contains("Master")) return line;
			}
		}
		return null;
	}
	
	public static List<Mixer> getMixers() {
		Info[] infos = AudioSystem.getMixerInfo();
		List<Mixer> mixers = new ArrayList<Mixer>(infos.length);
		for (Info info : infos) {
			Mixer mixer = AudioSystem.getMixer(info);
			mixers.add(mixer);
		}
		return mixers;
	}
	
	public static List<Line> getAvailableOutputLines(Mixer mixer) {
		return getAvailableLines(mixer, mixer.getTargetLineInfo());
	}
	
	public static FloatControl getVolumeControl(Line line) {
		if (!line.isOpen()) throw new RuntimeException("Line is closed: " + toString(line));
		return (FloatControl) findControl(FloatControl.Type.VOLUME, line.getControls());
	}
	
	private static Control findControl(Type type, Control... controls) {
		if (controls == null || controls.length == 0) return null;
		for (Control control : controls) {
			if (control.getType().equals(type)) return control;
			if (control instanceof CompoundControl) {
				CompoundControl compoundControl = (CompoundControl) control;
				Control member = findControl(type, compoundControl.getMemberControls());
				if (member != null) return member;
			}
		}
		return null;
	}
	
	private static List<Line> getAvailableLines(Mixer mixer, Line.Info[] lineInfos) {
		List<Line> lines = new ArrayList<Line>(lineInfos.length);
		for (Line.Info lineInfo : lineInfos) {
			Line line;
			line = getLineIfAvailable(mixer, lineInfo);
			if (line != null) lines.add(line);
		}
		return lines;
	}
	
	public static Line getLineIfAvailable(Mixer mixer, Line.Info lineInfo) {
		try {
			return mixer.getLine(lineInfo);
		} catch (LineUnavailableException ex) {
			return null;
		}
	}
	
	public static boolean open(Line line) {
		if (line.isOpen()) return false;
		try {
			line.open();
		} catch (LineUnavailableException ex) {
			return false;
		}
		return true;
	}
	
	public static String toString(Line line) {
		if (line == null) return null;
		Line.Info info = line.getLineInfo();
		return info.toString();// + " (" + line.getClass().getSimpleName() + ")";
	}
	
}
