import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import net.miginfocom.swing.MigLayout;

//TODO: Extend from something other than JFrame, make this a JPanel
public class ACSView extends JFrame implements ModelChangeListener {

	private static final long serialVersionUID = 4624734164668268444L;

	// Constants
	public static final String TITLE = "Anvil Control Server";
	public static final String STATUS_CONNECTED = "Connected to client";
	public static final String STATUS_WAITING = "Waiting on connection";
	public static final String STATUS_DISABLED = "Server disabled";
	
	private static final String RED_ICON = "/ic_launcher_2_red.png"; 
	private static final String YELLOW_ICON = "/ic_launcher_2.png"; 
	private static final String GREEN_ICON = "/ic_launcher_2_green.png"; 

	// Password field sometimes defaults with fake text
	// when it's changed we need to know (so the fake text isn't submitted)
	public boolean passChanged;
	
	// Components
	public JCheckBox broadcastChkbox;
	public JCheckBox passwordChkbox;
	public JTextField serv_name;
	public JPasswordField serv_pswd;
	private JButton[] quick_btns;
	private JButton toggleBtn;
	private JButton saveBtn;
	private JLabel status;
	private JLabel client_ip;
	private JLabel serv_ip;
	private MenuItem disableAction;
	
	private ACSModel model;
	private JLabel pswdLbl;
	private JLabel nameLbl;
	private TrayIcon trayIcon;
	
	public ACSView(ACSModel mod){
		model = mod;
		trayIcon = null;
		
		// Set Default Look and Feel
		try{ 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e){
			System.err.println("Could not set look and feel for MCView");
		}
		
		setMinimumSize(new Dimension(418, 280));
		
		// Initialize components
		JPanel content = new JPanel();
        
		//---quick panel
		JPanel quick_panel = new JPanel();
		TitledBorder qltitle = BorderFactory.createTitledBorder("Modify Quick Launch Buttons");
        quick_panel.setBorder(qltitle);
        quick_panel.setLayout(new FlowLayout());
		quick_btns = new JButton[ACSModel.QUICK_SLOTS];
		for(int i = 0; i < ACSModel.QUICK_SLOTS; i++){
			quick_btns[i] = new JButton("+");
			quick_btns[i].setActionCommand("QL"+i);
			quick_btns[i].setPreferredSize(new Dimension(40,40) );
			quick_btns[i].setFocusPainted(false);
			quick_btns[i].setToolTipText("Drag somthing here OR Click to set");
			quick_panel.add(quick_btns[i]);
		}	
	
		//---info panel
		JPanel infoPan = new JPanel();
		infoPan.setBorder(BorderFactory.createTitledBorder("Server Info"));
		infoPan.setLayout(new MigLayout("", "[grow]", "[][][][grow]"));
		status = new JLabel("Status: " + STATUS_DISABLED);
		client_ip = new JLabel("Client IP: ---.---.---.---");
		serv_ip = new JLabel("Server IP: ---.---.---.---");
		toggleBtn = new JButton("Disable");
		
		infoPan.add(status,"cell 0 0");
		infoPan.add(client_ip,"cell 0 1");
		infoPan.add(serv_ip,"cell 0 2");
		infoPan.add(toggleBtn, "cell 0 3, aligny bottom");
		
			
		//---settings panel
		JPanel settingPan = new JPanel();
		settingPan.setBorder(BorderFactory.createTitledBorder("Server Settings"));
		settingPan.setLayout(new MigLayout("", "[][105.00,grow]", "[][][grow]"));
		nameLbl = new JLabel("Server Name");
        pswdLbl = new JLabel("Server Password");
        serv_pswd = new JPasswordField();
        
        serv_pswd.addKeyListener(new KeyListener(){
			@Override
			public void keyPressed(KeyEvent arg0) {		
				passChanged = true;
			}
			@Override
			public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyTyped(KeyEvent arg0) {}
        });
        
        serv_name = new JTextField();
        saveBtn = new JButton("Save");
        
        JLabel enable_pswd = new JLabel("Enable Password");
        passwordChkbox = new JCheckBox();
        passwordChkbox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				togglePassword(passwordChkbox.isSelected());
			}
        	
        });
        
        JLabel enable_bcast = new JLabel("Broadcast Server");
        broadcastChkbox = new JCheckBox();
        
        settingPan.add(nameLbl,"cell 0 0,alignx trailing");
        settingPan.add(serv_name,"cell 1 0,growx,width 90:90:");

        settingPan.add(pswdLbl,"cell 0 1,alignx trailing");
        settingPan.add(serv_pswd,"cell 1 1,growx,width 90:90:");
        
        settingPan.add(enable_pswd,"cell 0 2");
        settingPan.add(passwordChkbox, "cell 1 2"); //align left?
        
        settingPan.add(enable_bcast,"cell 0 3");
        settingPan.add(broadcastChkbox,"cell 1 3");
        
        settingPan.add(saveBtn, "cell 1 4, alignx right, aligny bottom");
       
		
		// Layout components     
        content.setLayout(new MigLayout("", "[188.00,fill][104.00,grow]", "[][grow]"));
        content.add(quick_panel, "cell 0 0 2 1,alignx left,hmin 75");
        content.add(infoPan, "cell 0 1,alignx leading,growy");
        content.add(settingPan,"cell 1 1,growx");
        

        this.setContentPane(content);
        this.pack();
        this.setTitle(TITLE);
        
        //TODO: Will eventually want to save on close -- save model data
        
        createSysTray();
        // set default close action (if tray supported, hide instead)
        //if(trayIcon != null){
        //	setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //}else{
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //}
        
        ImageIcon im = new ImageIcon(getClass().getResource(YELLOW_ICON));
        setIconImage(im.getImage());
        
        //add listeners to model
        model.addModelChangeListener(this);
        model.requestUpdate();
	}
	
	
	/**
	 * Creates a system tray menu (if supported) 
	 */
	protected void createSysTray(){
		if(SystemTray.isSupported()){
			SystemTray tray = SystemTray.getSystemTray();
			
			BufferedImage bufImage = null;
			try {
				bufImage = ImageIO.read(getClass().getResourceAsStream(RED_ICON));
			} catch (IOException e1) {
				System.err.println("Couldn't load systray image");
			}
			int trayIconWidth = new TrayIcon(bufImage).getSize().width;
			
		    disableAction = new MenuItem("Disable Server");
		    
		    MenuItem exitAction = new MenuItem("Exit");
		    exitAction.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO: this should not tell the model what to do!
					model.saveConfig();
					System.exit(0);
				}
		    });
		    
		    
		    PopupMenu popup = new PopupMenu();
		    popup.add(disableAction);
		    popup.add(exitAction);
		    
		    trayIcon = new TrayIcon(bufImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH), TITLE, popup);
		    trayIcon.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(ACSView.this.isVisible()){
						ACSView.this.setVisible(false);
					} else {
						ACSView.this.setVisible(true);
					}
				}
		    });
		    
		    
		    try {
		        tray.add(trayIcon);
		    } catch (AWTException e) {
		    	System.err.println("Couldn't add systray icon - " + e);
		    }
		    
		}
		
	}
	
	protected void togglePassword(boolean enabled){
		if(enabled){
			passwordChkbox.setSelected(true);
			serv_pswd.setEnabled(true);
		} else {
			passwordChkbox.setSelected(false);
			serv_pswd.setEnabled(false);
		}
	}
	
	//***************************************
	//******************** Register Listeners
	//********************

	/**
	 * Registers an action listener to the Quick Launch buttons 
	 * @param listen - The ActionListener to register
	 */
	public void addQLButtonListener(ActionListener listen){
		for(JButton btn : quick_btns){
			btn.addActionListener(listen);
		}
	}
	
	/**
	 * Adds DropTargets to the Quick Launch buttons built from the passed in factory
	 * @param dtf - The DropTargetFactory used to create the DropTargets
	 */
	public void addQLDropTargets(DropTargetFactory dtf){
		for(JButton btn : quick_btns){
			btn.setDropTarget(dtf.makeDropTarget());
		}
	}
	
	/**
	 * Registers an action listener to the Toggle Button
	 * @param listener - The ActionListener to register
	 */
	public void addToggleButtonListener(ActionListener listener){
		toggleBtn.addActionListener(listener);
		if(disableAction != null) disableAction.addActionListener(listener);
	}
	
	/**
	 * Registers an action listener to the Save Button
	 * @param listener - The ActionListener to register
	 */
	public void addSaveButtonListener(ActionListener listener){
		saveBtn.addActionListener(listener);
	}
	
	
	//***************************************
	//******************** Callback updates from model
	//********************

	public void changedServerName(String sname){
		serv_name.setText(sname);
	}
	
	public void changedServerPswd(boolean psw_set){
		togglePassword(psw_set);
		if(psw_set){
			// When password is first loaded set fake text
			serv_pswd.setText("fake");
			passChanged=false;
		} else {
			serv_pswd.setText("");
		}
	}
	
	/**
	 * Changes were made to the models Quick Launch file list
	 * @param files - The Array of files representing the Quick Launch list
	 */
	@Override
	public void changedQLFiles(File[] files){
		int i = 0;
		for(JButton btn : quick_btns){
			if(files[i] != null){
				FileSystemView fsv = FileSystemView.getFileSystemView();
				Icon ico = fsv.getSystemIcon(files[i]);

				btn.setText("");
				btn.setToolTipText(files[i].getName() + " - Drag somthing here OR Click to change");
				btn.setIcon(ico);
			} else {
				JButton tempBtn = new JButton();
				btn.setIcon(tempBtn.getIcon());
				btn.setText("+");
				btn.setToolTipText("Drag somthing here OR Click to set");
			}
			i++;
		}
	}
	
	/**
	 * Changes were made to the models Status
	 * @param running - Whether the server is running
	 * @param connected - Whether a client is connected
	 */
	@Override
	public void changedStatus(boolean running, boolean connected){
		String icon;
		
		if(running){
			if(connected){
				this.status.setText("Status: " + STATUS_CONNECTED);
				icon = GREEN_ICON;
			}else{
				this.status.setText("Status: " + STATUS_WAITING);
				icon = YELLOW_ICON;
			}
			toggleBtn.setText("Disable");
		}else{
			this.status.setText("Status: " + STATUS_DISABLED);
			icon = RED_ICON;
			toggleBtn.setText("Enable");
		}
		
		// Change Frame Icon
        ImageIcon im = new ImageIcon(getClass().getResource(icon));
        setIconImage(im.getImage());
		
		// Change the system tray icon if it is supported
	    if (trayIcon != null) {
	    	
	    	if(running){
	    		disableAction.setLabel("Disable");
	    	}else{
	    		disableAction.setLabel("Enable");
	    	}
	    	
	    	BufferedImage bufImage = null;
			try {
				bufImage = ImageIO.read(getClass().getResourceAsStream(icon));
			} catch (IOException e) {
				System.err.println("Could Not Change SysTray icon");
			}
	    	int trayIconWidth = new TrayIcon(bufImage).getSize().width;
	        trayIcon.setImage(bufImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH));
	    }
	}

	/**
	 * Changed were made to the model clientIP
	 * @param client_ip - The IP of the connected client
	 */
	@Override
	public void changedClientIP(String clientIp) {
		client_ip.setText("Client IP: " + clientIp);
	}
	
	@Override
	public void changedServerIP(String server_ip){
		serv_ip.setText("Server IP: " + server_ip);
	}
	
	@Override
	public void changedBroadcast(boolean enabled){
		if(enabled){
			broadcastChkbox.setSelected(true);
		} else {
			broadcastChkbox.setSelected(false);
		}
	}

}
