import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client {
	public static void main(String[] args) {
		Gui g = new Gui();
		g.setVisible(true);
	}
}

@SuppressWarnings("serial")
class Gui extends JFrame {
	public static final int WIDTH = 400;
	public static final int HEIGHT = 400;
	private final JTextField sn;
	private final JTextField port;
	private final JTextArea post;
	private final JTextArea get;
	private final JTextArea response;
	private Socket client;
	private OutputStream outToServer;
	private DataOutputStream out;
	private InputStream inFromServer;
	private DataInputStream in;

	public Gui() {
		super();
		this.setResizable(false);
		setSize(WIDTH, HEIGHT);
		setTitle("");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new FlowLayout(10, 10, 10));
		setBackground(Color.WHITE);
		sn = new JTextField(10);
		port = new JTextField(10);
		post = new JTextArea(2, 20);
		get = new JTextArea(2, 20);
		response = new JTextArea(10, 20);
		response.setLineWrap(true);
		response.setWrapStyleWord(true);
		// Font f = new Font("Comic Sans MS", Font.PLAIN, 24);
		// response.setFont(f);
		final JLabel ipL = new JLabel("Server Name: ");
		final JButton con = new JButton("Connect/Disconnect");
		con.addActionListener(new ActionListener() {

			private int clicked;

			@Override
			public void actionPerformed(final ActionEvent event) {
				clicked++;

				if (clicked % 2 == 1) {
					try {
						System.out.println("Connecting to " + sn.getText()
								+ " on port " + port.getText());
						client = new Socket(sn.getText(), Integer.parseInt(port
								.getText()));
						System.out.println("Just connected to "
								+ client.getRemoteSocketAddress());
						outToServer = client.getOutputStream();
						out = new DataOutputStream(outToServer);
						inFromServer = client.getInputStream();
						in = new DataInputStream(inFromServer);

					} catch (NumberFormatException | IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						System.out.println("Disconnecting from " + sn.getText()
								+ " on port " + port.getText());
						out.close();
						in.close();
						outToServer.close();
						inFromServer.close();
						client.close();
						System.exit(0);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			}
		});
		final JButton postB = new JButton("Post");
		postB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {

					System.out.println("Sending: " + post.getText());
					out.writeUTF(post.getText());
					String re = in.readUTF();
					System.out.println("Server says: " + re);
					response.setText("Server says: " + re);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
		final JButton getB = new JButton("Get");
		getB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {

					System.out.println("Sending: " + get.getText());
					out.writeUTF(get.getText());
					String re = in.readUTF();
					System.out.println("Server says: " + re);
					response.setText("Server says: " + re);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
		sn.setText("localhost");
		port.setText("44444");
		add(ipL);
		add(sn);
		add(con);
		add(new JLabel("    "));
		final JLabel portL = new JLabel("Port #             : ");
		add(portL);
		add(port);
		add(new JLabel("                                                  "));
		final JLabel po = new JLabel("Post    :");
		final JLabel ge = new JLabel("Get       :");
		final JLabel resp = new JLabel("Response:");
		add(po);
		add(post);
		add(postB);
		add(new JLabel("          "));
		add(ge);
		add(get);
		add(getB);
		add(resp);
		add(response);

	}
}
