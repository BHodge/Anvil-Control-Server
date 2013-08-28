import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.Component;
import javax.swing.border.TitledBorder;
import java.awt.Dimension;


public class test extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1591985513637731233L;
	private JPanel contentPane;
	private JTextField txtName;
	private JPasswordField pwdPswd;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					test frame = new test();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public test() {
		setMaximumSize(new Dimension(408, 248));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 408, 248);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[188.00,fill][104.00,grow]", "[][grow]"));
		
		JPanel panel = new JPanel();
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setBorder(new TitledBorder(null, "Test", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		//FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		contentPane.add(panel, "cell 0 0 2 1,alignx left");
		
		JButton button = new JButton("+");
		panel.add(button);
		
		JButton button_1 = new JButton("+");
		panel.add(button_1);
		
		JButton button_2 = new JButton("+");
		panel.add(button_2);
		
		JButton button_3 = new JButton("+");
		panel.add(button_3);
		
		JButton button_4 = new JButton("+");
		panel.add(button_4);
		
		JButton button_5 = new JButton("+");
		panel.add(button_5);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(panel_1, "cell 0 1,alignx leading,growy");
		panel_1.setLayout(new MigLayout("", "[grow]", "[][][][grow]"));
		
		JLabel lblStatusServerConnected = new JLabel("Status: Server Connected");
		panel_1.add(lblStatusServerConnected, "cell 0 0");
		
		JLabel lblClientIp = new JLabel("Client IP: 255.255.255.255");
		panel_1.add(lblClientIp, "cell 0 1");
		
		JLabel lblServerIp = new JLabel("Server IP: 127.0.0.1");
		panel_1.add(lblServerIp, "cell 0 2");
		
		JButton btnDisable = new JButton("Disable");
		panel_1.add(btnDisable, "cell 0 3,aligny bottom");
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Server Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		contentPane.add(panel_2, "cell 1 1,grow");
		panel_2.setLayout(new MigLayout("", "[][105.00,grow]", "[][][grow]"));
		
		JLabel lblServerName = new JLabel("Server Name");
		panel_2.add(lblServerName, "cell 0 0,alignx trailing");
		
		txtName = new JTextField();
		txtName.setText("name");
		panel_2.add(txtName, "cell 1 0,growx,width 90:90:");
		txtName.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password");
		panel_2.add(lblPassword, "cell 0 1,alignx trailing");
		
		pwdPswd = new JPasswordField();
		pwdPswd.setText("pswd");
		panel_2.add(pwdPswd, "cell 1 1,growx");
		
		JButton btnSave = new JButton("Save");
		panel_2.add(btnSave, "cell 0 2,aligny bottom");
	}

}
