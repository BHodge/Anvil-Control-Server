import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Extended keyboard functionality for the Robot Class
 * 
 * @author Demiurg
 * @author Bryan Hodge
 */
public class SmartRobot extends Robot {
	private Map<Character, ShiftIndex> keyMap;
	
	
	/**
	 *  Data Structure for Keyboard Keycode
	 *  and flag for shift requirement.
	 *  (Some keys on a keyboard share the same keycode,
	 *  for example: ';' and ':')
	 * @author Bryan Hodge
	 */
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

		// The anvil client sends over some non standard keycodes
		// (Android does not include the awt package)
		keyMap = new HashMap<Character, ShiftIndex>();
		
		// Add Uppercase letter keys (sent as negatives of the lowercase)
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
		// Lower case letters map to the uppercase codes (uppercase require shift key)
	}
	
	
	/**
	 * Press a key on the 'keyboard', the keycode is checked against
	 * the key map for non standard keys.
	 * @param keyCode - Keycode of the key to type
	 */
	public void typeKeycode(int keyCode) {
		
		if( keyMap.containsKey((char)keyCode) ){
			ShiftIndex shi = keyMap.get((char)keyCode);
			if(shi.shift){
				keyType(shi.keyVal, KeyEvent.VK_SHIFT);
			}else{
				keyType(shi.keyVal);
			}
		} else {
			keyType(keyCode);
		}
		
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
	
}
