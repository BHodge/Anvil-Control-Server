import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Extended functionality for the Robot Class
 * 
 * @author Demiurg
 * @author Bryan Hodge
 */
public class SmartRobot extends Robot {
	private Map<Character, ShiftIndex> keyMap;
	
	private class ShiftIndex{ 
		public int keyVal; 
		public boolean shift;
		public ShiftIndex(int val, boolean needShift){
			keyVal = val;
			shift = needShift;
		}
	}
	
	public SmartRobot() throws AWTException {
		super();
		
		// Setup the map for characters that need
		// to use VK_ keycodes or need to hold shift
		// when pressing an associated key
		
		keyMap = new HashMap<Character, ShiftIndex>();
		
		// Add Uppercase letter keys (sent as negatives)
		for(int x = -65; x > -91; x--){
			keyMap.put((char)x, new ShiftIndex((0-x),true));
		}
		
		// Add all shifted numsymbol keys
		keyMap.put('!', new ShiftIndex(KeyEvent.VK_1,true));
		keyMap.put('@', new ShiftIndex(KeyEvent.VK_2,true));
		keyMap.put('#', new ShiftIndex(KeyEvent.VK_3,true));
		keyMap.put('$', new ShiftIndex(KeyEvent.VK_4,true));
		keyMap.put('%', new ShiftIndex(KeyEvent.VK_5,true));
		keyMap.put('^', new ShiftIndex(KeyEvent.VK_6,true));
		keyMap.put('&', new ShiftIndex(KeyEvent.VK_7,true));
		keyMap.put('*', new ShiftIndex(KeyEvent.VK_8,true));
		keyMap.put('(', new ShiftIndex(KeyEvent.VK_9,true));
		keyMap.put(')', new ShiftIndex(KeyEvent.VK_0,true));
		keyMap.put('_', new ShiftIndex(KeyEvent.VK_MINUS,true));
		keyMap.put('+', new ShiftIndex(KeyEvent.VK_EQUALS,true));
		
		keyMap.put('~', new ShiftIndex(KeyEvent.VK_BACK_QUOTE,true));
		keyMap.put('|', new ShiftIndex(KeyEvent.VK_BACK_SLASH,true));
		keyMap.put(':', new ShiftIndex(KeyEvent.VK_SEMICOLON,true));
		keyMap.put('"', new ShiftIndex(KeyEvent.VK_QUOTE,true));
		keyMap.put('<', new ShiftIndex(KeyEvent.VK_COMMA,true));
		keyMap.put('>', new ShiftIndex(KeyEvent.VK_PERIOD,true));
		keyMap.put('?', new ShiftIndex(KeyEvent.VK_SLASH,true));
		keyMap.put('{', new ShiftIndex(KeyEvent.VK_OPEN_BRACKET,true));
		keyMap.put('}', new ShiftIndex(KeyEvent.VK_CLOSE_BRACKET,true));
		
		// Lower case symbols
		keyMap.put(',', new ShiftIndex(KeyEvent.VK_COMMA,false));
		keyMap.put('.', new ShiftIndex(KeyEvent.VK_PERIOD,false));
		keyMap.put('/', new ShiftIndex(KeyEvent.VK_SLASH,false));
		keyMap.put(';', new ShiftIndex(KeyEvent.VK_SEMICOLON,false));
		keyMap.put('\'', new ShiftIndex(KeyEvent.VK_QUOTE,false));
		keyMap.put('[', new ShiftIndex(KeyEvent.VK_OPEN_BRACKET,false));
		keyMap.put(']', new ShiftIndex(KeyEvent.VK_CLOSE_BRACKET,false));
		keyMap.put('\\', new ShiftIndex(KeyEvent.VK_BACK_SLASH,false));
		keyMap.put('`', new ShiftIndex(KeyEvent.VK_BACK_QUOTE,false));

		// Control keys sent as negative ASCII keycode
		keyMap.put((char)-8, new ShiftIndex(KeyEvent.VK_BACK_SPACE,false));
		keyMap.put((char)-13, new ShiftIndex(KeyEvent.VK_ENTER,false));
		keyMap.put((char)-16, new ShiftIndex(KeyEvent.VK_SHIFT,false));
		keyMap.put((char)-32, new ShiftIndex(KeyEvent.VK_SPACE,false));
		keyMap.put((char)-20, new ShiftIndex(KeyEvent.VK_CAPS_LOCK,false));
		keyMap.put((char)-9, new ShiftIndex(KeyEvent.VK_TAB,false));
		keyMap.put((char)-27, new ShiftIndex(KeyEvent.VK_ESCAPE,false));
		keyMap.put((char)-17, new ShiftIndex(KeyEvent.VK_CONTROL,false));
		keyMap.put((char)-18, new ShiftIndex(KeyEvent.VK_ALT,false));
		keyMap.put((char)-45, new ShiftIndex(KeyEvent.VK_INSERT,false));
		keyMap.put((char)-46, new ShiftIndex(KeyEvent.VK_DELETE,false));
		keyMap.put((char)-36, new ShiftIndex(KeyEvent.VK_HOME,false));
		keyMap.put((char)-35, new ShiftIndex(KeyEvent.VK_END,false));
		keyMap.put((char)-33, new ShiftIndex(KeyEvent.VK_PAGE_UP,false));
		keyMap.put((char)-34, new ShiftIndex(KeyEvent.VK_PAGE_DOWN,false));
		keyMap.put((char)-145, new ShiftIndex(KeyEvent.VK_SCROLL_LOCK,false));
		keyMap.put((char)-19, new ShiftIndex(KeyEvent.VK_PAUSE,false));
		
		keyMap.put((char)-38, new ShiftIndex(KeyEvent.VK_UP,false));
		keyMap.put((char)-40, new ShiftIndex(KeyEvent.VK_DOWN,false));
		keyMap.put((char)-37, new ShiftIndex(KeyEvent.VK_LEFT,false));
		keyMap.put((char)-39, new ShiftIndex(KeyEvent.VK_RIGHT,false));
		
		// F1 - F12 keys dont seem to change codes
		// Numbers dont seem to change codes
		// Lower case letters map to uppercase codes 
	}

	public void keyType(int keyCode) {
		try{
			keyPress(keyCode);
			delay(50);
			keyRelease(keyCode);
		}catch(IllegalArgumentException e){
			System.err.println("Error keycode: " + ((Integer)keyCode).toString() );
		}
	}

	public void keyType(int keyCode, int keyCodeModifier) {
		try{
		keyPress(keyCodeModifier);
		keyPress(keyCode);
		delay(50);
		keyRelease(keyCode);
		keyRelease(keyCodeModifier);
		} catch (IllegalArgumentException e){
			System.err.println("Error keycode: " + ((Integer)keyCode).toString() );
		}
	}

	public void type(String text) {
		String textUpper = text.toUpperCase();

		for (int i = 0; i < text.length(); ++i) {
			typeChar(textUpper.charAt(i));
		}
	}

	public void typeChar(int c) {

		boolean shift = true;
		int keyCode = c;
		
		// Uppercase characters are sent as negative values
		// of their normal uppercase value, which lower
		// case characters are instead sent as;
		// This makes the logic for applying the shift key easier.
		if(keyCode < 0){
			keyCode = 0-keyCode;
			keyType(keyCode, KeyEvent.VK_SHIFT);
			int i = KeyEvent.VK_ENTER;
			return;
		}

		switch (c) {
		case '~':
			keyCode = (int) '`';
			break;
		case '!':
			keyCode = (int) '1';
			break;
		case '@':
			keyCode = (int) '2';
			break;
		case '#':
			keyCode = (int) '3';
			break;
		case '$':
			keyCode = (int) '4';
			break;
		case '%':
			keyCode = (int) '5';
			break;
		case '^':
			keyCode = (int) '6';
			break;
		case '&':
			keyCode = (int) '7';
			break;
		case '*':
			keyCode = (int) '8';
			break;
		case '(':
			keyCode = (int) '9';
			break;
		case ')':
			keyCode = (int) '0';
			break;
		case ':':
			keyCode = (int) ';';
			break;
		case '_':
			keyCode = (int) '-';
			break;
		case '+':
			keyCode = (int) '=';
			break;
		case '|':
			keyCode = (int) '\\';
			break;
		case '"':
			keyCode = KeyEvent.VK_QUOTE;
			break;
		case '?':
			keyCode = (int) '/';
			break;
		case '{':
			keyCode = (int) '[';
			break;
		case '}':
			keyCode = (int) ']';
			break;
		case '<':
			keyCode = (int) ',';
			break;
		case '>':
			keyCode = (int) '.';
			break;
		default:
			keyCode = (int) c;
			shift = false;
		}
		if (shift) {
			keyType(keyCode, KeyEvent.VK_SHIFT);
		} else {
			keyType(keyCode);
		}
	}
	
	
	
	public void typeKeycode(int c) {
	
		if( keyMap.containsKey((char)c) ){
			ShiftIndex shi = keyMap.get((char)c);
			if(shi.shift){
				keyType(shi.keyVal, KeyEvent.VK_SHIFT);
			}else{
				keyType(shi.keyVal);
			}
		} else {
			keyType(c);
		}
		
	}

	/*
	 * private int charToKeyCode(char c) { switch (c) { case ':': return ';'; }
	 * return (int)c; }
	 */
}
