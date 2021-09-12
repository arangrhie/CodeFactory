/**
 * 
 */
package javax.arang.test;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 * @author Arang Rhie
 *
 */
public class GuiWindow {

	private JFrame frame;
	private JPanel panel_1;
	private JPanel panel_2;
	private JPanel panel_3;
	private JPanel panel_4;
	private JPanel panel_5;
	private JPanel panel_6;
	private JPanel panel_7;
	private JPanel panel_8;
	private JTextPane txtpnLogpanel;
	private JTextArea textArea;
	private JLabel lblSenderEmailAddress;
	private JTextField textField;
	private JLabel lblNewLabel;
	private JLabel lblSmtp;
	private JPasswordField passwordField;
	private JTextField textField_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiWindow window = new GuiWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GuiWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 850, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.NORTH);
		
		lblSenderEmailAddress = new JLabel("Sender Email Address");
		panel_1.add(lblSenderEmailAddress);
		
		textField = new JTextField();
		panel_1.add(textField);
		textField.setColumns(10);
		
		lblNewLabel = new JLabel("Password");
		panel_1.add(lblNewLabel);
		
		passwordField = new JPasswordField();
		passwordField.setColumns(10);
		panel_1.add(passwordField);
		
		lblSmtp = new JLabel("SMTP");
		panel_1.add(lblSmtp);
		
		textField_1 = new JTextField();
		panel_1.add(textField_1);
		textField_1.setColumns(10);
		
		panel_2 = new JPanel();
		panel.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		panel_6 = new JPanel();
		panel_2.add(panel_6, BorderLayout.NORTH);
		
		panel_7 = new JPanel();
		panel_2.add(panel_7, BorderLayout.CENTER);
		
		panel_8 = new JPanel();
		panel_2.add(panel_8, BorderLayout.SOUTH);
		
		panel_3 = new JPanel();
		panel.add(panel_3, BorderLayout.SOUTH);
		
		panel_5 = new JPanel();
		frame.getContentPane().add(panel_5, BorderLayout.CENTER);
		
		textArea = new JTextArea();
		textArea.setColumns(10);
		panel_5.add(textArea);
		
		panel_4 = new JPanel();
		frame.getContentPane().add(panel_4, BorderLayout.SOUTH);
		
		txtpnLogpanel = new JTextPane();
		txtpnLogpanel.setText("log_panel");
		panel_4.add(txtpnLogpanel);
		
		
	}

}
