import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Server {
	public static void main(String argv[]) throws Exception {
		System.out.println("Server starting...\n");
		final ArrayList<Triangle> triList = new ArrayList<Triangle>();
		final ArrayList<Quad> quadList = new ArrayList<Quad>();
		// Get the port number from the command line.
		int port = Integer.parseInt(argv[0]);// new Integer(argv[0]).intValue();
		// System.out.println(sss.charAt(0));
		// Establish the listen socket.
		@SuppressWarnings("resource")
		ServerSocket socket = new ServerSocket(port);

		// Process HTTP service requests in an infinite loop.
		while (true) {
			// Listen for a TCP connection request.
			Socket connection = socket.accept();
			System.out.println("Accepting Connection...\n");
			// Create a new thread to process the request.
			new ServerThread(connection, triList, quadList).start();

		}
	}
}

class ServerThread extends Thread {
	private Socket socket;
	final ArrayList<Triangle> triList;
	final ArrayList<Quad> quadList;

	ServerThread(Socket socket, ArrayList<Triangle> triList, ArrayList<Quad> quadList) {
		this.socket = socket;
		this.triList = triList;
		this.quadList = quadList;
	}

	@Override
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void processRequest() throws Exception {

		OutputStream outToClient = socket.getOutputStream();
		DataOutputStream out = new DataOutputStream(outToClient);
		InputStream inFromClient = socket.getInputStream();
		DataInputStream in = new DataInputStream(inFromClient);
		while (true) {
			// Get the request line of the message.
			String input = in.readUTF().trim();
			System.out.println("Client writes: " + input);
			String sendBack = process(input);
			out.writeUTF(sendBack);
			System.out.println("Sent: " + sendBack);

		}
	}

	// this is where we receive messages from clients, we send the appropriate
	// response after processing it.
	private String process(String input) {

		if (input.startsWith("POST ")) {
			int spaces = 0;
			for (int i = 0; i < input.length(); i++) {
				if (input.charAt(i) == ' ') {
					spaces++;
				}
			}
			// System.out.println("Spaces: " + spaces);
			if (spaces == 2)
				return "Error, that's just a point!";
			else if (spaces == 3 || spaces == 5 || spaces == 7)
				return "Incomplete points!";
			else if (spaces == 4)
				return "Error, that's defines a line!";
			else if (spaces == 6) {

				return createTriangle(input.substring(5));
			} else if (spaces == 8) {
				return createQuad(input.substring(5));
			} else
				return "You just sent me garbage.";
		} else if (input.startsWith("GET T ")) {

			return getT(input.substring(6));

		} else if (input.startsWith("GET Q ")) {

			return getQ(input.substring(6));

		} else {
			return "You sent me some garbage somewhere.";
		}

	}

	private String getQ(String s) {
		String out;

		if (s.equals("share vertex")) {
			Quad a;
			Quad b;
			out = "Here is a list of all quadrilaterals that share a vertex: ";
			for (int i = 0; i < quadList.size(); i++) {
				a = quadList.get(i);
				for (int j = i + 1; j < quadList.size(); j++) {
					b = quadList.get(j);
					if (a.one.equals(b.one) || a.one.equals(b.two) || a.one.equals(b.three)
							|| a.one.equals(b.four) || a.two.equals(b.one) || a.two.equals(b.two)
							|| a.two.equals(b.three) || a.two.equals(b.four)
							|| a.three.equals(b.one) || a.three.equals(b.two)
							|| a.three.equals(b.three) || a.three.equals(b.four)
							|| a.four.equals(b.one) || a.four.equals(b.two)
							|| a.four.equals(b.three) || a.four.equals(b.four)) {
						out = out + "Q:  P1 = [" + a.one.x + "," + a.one.y + "] P2 = [" + a.two.x
								+ "," + a.two.y + "] P3 = [" + a.three.x + "," + a.three.y
								+ "] P4 = [" + a.four.x + "," + a.four.y
								+ "] shares a vertex with Q:  P1 = [" + b.one.x + "," + b.one.y
								+ "] P2 = [" + b.two.x + "," + b.two.y + "] P3 = [" + b.three.x
								+ "," + b.three.y + "] P4 = [" + b.four.x + "," + b.four.y + "], ";
					}
				}
			}
			out = out.substring(0, out.length() - 2);
			out = out.concat(".");

		} else if (s.equals("rectangular")) {
			out = "Here is a list of all quadrilaterals that are rectangular: ";
			for (int j = 0; j < quadList.size(); j++) {

				if (quadList.get(j).isRectangle == true) {
					out = out + "Q:  P1 = [" + quadList.get(j).one.x + "," + quadList.get(j).one.y
							+ "] P2 = [" + quadList.get(j).two.x + "," + quadList.get(j).two.y
							+ "] P3 = [" + quadList.get(j).three.x + "," + quadList.get(j).three.y
							+ "] P4 = [" + quadList.get(j).four.x + "," + quadList.get(j).four.y
							+ "], ";
				}
			}
			out = out.substring(0, out.length() - 2);
			out = out.concat(".");

		} else if (s.equals("square")) {
			out = "Here is a list of all the quadrilaterals that are square: ";

			for (int j = 0; j < quadList.size(); j++) {
				if (quadList.get(j).isSquare == true) {
					out = out + "Q :  P1 = [" + quadList.get(j).one.x + "," + quadList.get(j).one.y
							+ "] P2 = [" + quadList.get(j).two.x + "," + quadList.get(j).two.y
							+ "] P3 = [" + quadList.get(j).three.x + "," + quadList.get(j).three.y
							+ "] P4 = [" + quadList.get(j).four.x + "," + quadList.get(j).four.y
							+ "], ";
				}
			}

			out = out.substring(0, out.length() - 1);
			out = out.concat(".");
		} else if (s.equals("rhombus")) {
			out = "Here is a list of all the quadrilaterals that are rhombus: ";

			for (int j = 0; j < quadList.size(); j++) {
				if (quadList.get(j).isRhombus == true) {
					out = out + "Q :  P1 = [" + quadList.get(j).one.x + "," + quadList.get(j).one.y
							+ "] P2 = [" + quadList.get(j).two.x + "," + quadList.get(j).two.y
							+ "] P3 = [" + quadList.get(j).three.x + "," + quadList.get(j).three.y
							+ "] P4 = [" + quadList.get(j).four.x + "," + quadList.get(j).four.y
							+ "], ";
				}
			}

			out = out.substring(0, out.length() - 1);
			out = out.concat(".");
		} else if (s.equals("parallelogram")) {
			out = "Here is a list of all the quadrilaterals that are parallelograms: ";

			for (int j = 0; j < quadList.size(); j++) {
				if (quadList.get(j).isParalellagram == true) {
					out = out + "Q :  P1 = [" + quadList.get(j).one.x + "," + quadList.get(j).one.y
							+ "] P2 = [" + quadList.get(j).two.x + "," + quadList.get(j).two.y
							+ "] P3 = [" + quadList.get(j).three.x + "," + quadList.get(j).three.y
							+ "] P4 = [" + quadList.get(j).four.x + "," + quadList.get(j).four.y
							+ "], ";
				}
			}

			out = out.substring(0, out.length() - 1);
			out = out.concat(".");
		} else if (s.equals("convex")) {
			out = "Here is a list of all the quadrilaterals that are convex: ";

			for (int j = 0; j < quadList.size(); j++) {
				if (quadList.get(j).isConvex == true) {
					out = out + "Q :  P1 = [" + quadList.get(j).one.x + "," + quadList.get(j).one.y
							+ "] P2 = [" + quadList.get(j).two.x + "," + quadList.get(j).two.y
							+ "] P3 = [" + quadList.get(j).three.x + "," + quadList.get(j).three.y
							+ "] P4 = [" + quadList.get(j).four.x + "," + quadList.get(j).four.y
							+ "], ";
				}
			}

			out = out.substring(0, out.length() - 1);
			out = out.concat(".");
		} else if (Integer.parseInt(s) >= 0 && Integer.parseInt(s) <= 100) {
			out = "Here is a list of all the quadrilaterals that occured at least " + s
					+ " times since the server started: ";
			int num = Integer.parseInt(s);
			if (num == 0) {
				for (int j = 0; j < quadList.size(); j++) {
					out = out + "Q :  P1 = [" + quadList.get(j).one.x + "," + quadList.get(j).one.y
							+ "] P2 = [" + quadList.get(j).two.x + "," + quadList.get(j).two.y
							+ "] P3 = [" + quadList.get(j).three.x + "," + quadList.get(j).three.y
							+ "] P4 = [" + quadList.get(j).four.x + "," + quadList.get(j).four.y
							+ "], ";
				}
			} else {
				for (int j = 0; j < quadList.size(); j++) {
					if (quadList.get(j).count >= num) {
						out = out + "Q :  P1 = [" + quadList.get(j).one.x + ","
								+ quadList.get(j).one.y + "] P2 = [" + quadList.get(j).two.x + ","
								+ quadList.get(j).two.y + "] P3 = [" + quadList.get(j).three.x
								+ "," + quadList.get(j).three.y + "] P4 = ["
								+ quadList.get(j).four.x + "," + quadList.get(j).four.y + "], ";
					}
				}

				out = out.substring(0, out.length() - 1);
				out = out.concat(".");
			}
		} else
			out = "I'm sorry. What you asked for did not make sense.";
		return out;
	}

	private String createQuad(String input) {
		StringTokenizer tokens = new StringTokenizer(input);
		int p[] = new int[8];
		int x = 0;
		while (tokens.hasMoreTokens()) {
			p[x] = Integer.parseInt(tokens.nextToken());
			x++;
		}

		Point pts[] = { new Point(p[0], p[1]), new Point(p[2], p[3]), new Point(p[4], p[5]),
				new Point(p[6], p[7]) };

		Point a = new Point(p[0], p[1]), b = new Point(p[2], p[3]), c = new Point(p[4], p[5]), d = new Point(
				p[6], p[7]);
		if (a.equals(b) || a.equals(c) || a.equals(d) || b.equals(c) || b.equals(d) || c.equals(d)) {
			return "Error, two or more points are the same and does not define a quadrilateral";
		}
		if (a.y == b.y && a.y == c.y || b.y == c.y && b.y == d.y || c.y == d.y && c.y == a.y) {
			return "Error, three points are in a horizontal line.";
		}
		if (a.x == b.x && a.x == c.x || b.x == c.x && b.x == d.x || c.x == d.x && c.x == a.x) {
			return "Error, three points are in a verticle line.";
		}
		int low = pts[0].y;

		for (int i = 1; i < pts.length; i++) {
			if (pts[i].y <= low) {
				low = pts[i].y;
				swap(pts, 0, i);
			}
		}

		if (pts[0] == pts[1]) {
			if (pts[0].x > pts[1].x) {
				swap(pts, 0, 1);
				if (pts[2].x < pts[3].x) {
					swap(pts, 2, 3);
				}
			} else {
				if (pts[2].x < pts[3].x) {
					swap(pts, 2, 3);
				}
			}
		} else if (pts[0] == pts[2]) {
			swap(pts, 1, 2);
			if (pts[0].x > pts[1].x) {
				swap(pts, 0, 1);
				if (pts[2].x < pts[3].x) {
					swap(pts, 2, 3);
				}
			} else {
				if (pts[2].x < pts[3].x) {
					swap(pts, 2, 3);
				}
			}
		} else if (pts[0] == pts[3]) {
			swap(pts, 1, 3);
			if (pts[0].x > pts[1].x) {
				swap(pts, 0, 1);
				if (pts[2].x < pts[3].x) {
					swap(pts, 2, 3);
				}
			} else {
				if (pts[2].x < pts[3].x) {
					swap(pts, 2, 3);
				}

			}
		} else {
			float m1, m2, m3;
			if (pts[0].x == pts[1].x) {
				m2 = pts[2].y - pts[0].y / pts[2].x - pts[0].x;
				m3 = pts[3].y - pts[0].y / pts[3].x - pts[0].x;
				if (Math.abs(m2 - m3) < 0.000001)
					return "Error, three points are in a line.";
				if (m2 > 0 && m3 < 0) {
					swap(pts, 1, 2);
				} else if (m2 < 0 && m3 > 0) {
					swap(pts, 2, 3);
					swap(pts, 1, 2);
				} else if (m2 > 0 && m3 > 0) {
					if (m2 < m3) {
						swap(pts, 1, 2);
						swap(pts, 2, 3);
					} else if (m2 > m3) {
						swap(pts, 1, 3);
					}
				} else if (m2 < 0 && m3 < 0) {
					if (m2 > m3) {
						swap(pts, 2, 3);
					}
				}
			} else if (pts[0].x == pts[2].x) {
				m1 = pts[1].y - pts[0].y / pts[1].x - pts[0].x;
				m3 = pts[3].y - pts[0].y / pts[3].x - pts[0].x;
				if (Math.abs(m1 - m3) < 0.000001)
					return "Error, three points are in a line.";

				if (m1 < 0 && m3 > 0) {
					swap(pts, 1, 3);
				} else if (m1 > 0 && m3 > 0) {
					if (m1 < m3) {
						swap(pts, 2, 3);
					} else if (m1 > m3) {
						swap(pts, 1, 3);
						swap(pts, 2, 3);
					}
				} else if (m1 < 0 && m3 < 0) {
					if (m1 > m3) {
						swap(pts, 1, 2);
						swap(pts, 2, 3);
					} else if (m1 < m3) {
						swap(pts, 1, 2);
					}
				}
			} else if (pts[0].x == pts[3].x) {
				m1 = pts[1].y - pts[0].y / pts[1].x - pts[0].x;
				m2 = pts[2].y - pts[0].y / pts[2].x - pts[0].x;
				if (Math.abs(m1 - m2) < 0.000001)
					return "Error, three points are in a line.";

				if (m2 > 0 && m1 < 0) {
					swap(pts, 2, 3);
				} else if (m2 < 0 && m1 > 0) {
					swap(pts, 1, 2);
					swap(pts, 2, 3);
				} else if (m2 > 0 && m1 > 0) {
					if (m2 < m1) {
						swap(pts, 1, 2);
					}
				} else if (m2 < 0 && m1 < 0) {
					if (m2 > m1) {
						swap(pts, 2, 3);
						swap(pts, 1, 2);
					} else if (m2 < m1) {
						swap(pts, 1, 3);
					}
				}
			} else {

				m1 = (pts[1].y - pts[0].y) / (pts[1].x - pts[0].x);

				m2 = (pts[2].y - pts[0].y) / (pts[2].x - pts[0].x);

				m3 = (pts[3].y - pts[0].y) / (pts[3].x - pts[0].x);

				if (Math.abs(m1 - m2) < 0.000001 || Math.abs(m1 - m3) < 0.000001
						|| Math.abs(m2 - m3) < 0.000001) {
					return "Error, at least three points are in a line.";
				}
				if (m1 > 0 && m2 > 0 && m3 > 0) {
					if (m1 < m2 && m1 < m3 && m3 < m2) {
						swap(pts, 2, 3);
					} else if (m2 < m1 && m2 < m3 && m3 < m1) {
						swap(pts, 1, 2);
						swap(pts, 2, 3);
					} else if (m2 < m1 && m2 < m3 && m3 > m1) {
						swap(pts, 1, 2);
					} else if (m3 < m2 && m3 < m1 && m2 < m1) {
						swap(pts, 1, 3);
					} else if (m3 < m2 && m3 < m1 && m2 > m1) {
						swap(pts, 1, 3);
						swap(pts, 2, 3);
					}

				} else if (m1 < 0 && m2 < 0 && m3 < 0) {
					if (m1 < m2 && m1 < m3 && m3 < m2) {
						swap(pts, 2, 3);
					} else if (m2 < m1 && m2 < m3 && m3 < m1) {
						swap(pts, 1, 2);
						swap(pts, 2, 3);
					} else if (m2 < m1 && m2 < m3 && m3 > m1) {
						swap(pts, 1, 2);
					} else if (m3 < m2 && m3 < m1 && m2 < m1) {
						swap(pts, 1, 3);
					} else if (m3 < m2 && m3 < m1 && m2 > m1) {
						swap(pts, 1, 3);
						swap(pts, 2, 3);
					}
				} else if (m1 > 0 && m2 < 0 && m3 < 0) {
					if (m3 < m2) {
						swap(pts, 2, 3);
					}
				} else if (m2 > 0 && m1 < 0 && m3 < 0) {
					if (m1 < m3) {
						swap(pts, 1, 2);
					} else if (m3 < m1) {
						swap(pts, 1, 2);
						swap(pts, 2, 3);
					}
				} else if (m3 > 0 && m1 < 0 && m2 < 0) {
					if (m2 < m1) {
						swap(pts, 1, 3);
					} else if (m1 < m2) {
						swap(pts, 1, 3);
						swap(pts, 2, 3);
					}
				} else if (m1 < 0 && m2 > 0 && m3 > 0) {
					if (m2 < m3) {
						swap(pts, 1, 2);
						swap(pts, 2, 3);
					} else if (m3 < m2) {
						swap(pts, 1, 3);
					}
				} else if (m2 < 0 && m3 > 0 && m1 > 0) {
					if (m3 < m1) {
						swap(pts, 1, 3);
						swap(pts, 2, 3);
					} else if (m1 < m3) {
						swap(pts, 2, 3);
					}
				} else if (m3 < 0 && m1 > 0 && m2 > 0) {
					if (m2 < m1) {
						swap(pts, 1, 2);
					}
				}
			}
		}

		Quad q = new Quad(pts[0], pts[1], pts[2], pts[3]);
		for (int i = 0; i < quadList.size(); i++) {
			if (quadList.get(i).one.equals(q.one) && quadList.get(i).two.equals(q.two)
					&& quadList.get(i).three.equals(q.three) && quadList.get(i).four.equals(q.four)) {

				quadList.get(i).count++;
				return "Sorry. That quadrilateral was previously defined.";

			}

		}
		quadList.add(new Quad(pts[0], pts[1], pts[2], pts[3]));
		quadList.get(quadList.size() - 1).checkAll();
		return "The quadrilateral you entered was stored in the data structure.";

	}

	private void swap(Point[] pts, int i1, int i2) {
		Point temp = pts[i1];
		pts[i1] = pts[i2];
		pts[i2] = temp;

	}

	private String getT(String s) {
		String out;

		if (s.equals("share vertex")) {
			Triangle a;
			Triangle b;
			out = "Here is a list of all triangles that share a vertex: ";
			for (int i = 0; i < triList.size(); i++) {
				a = triList.get(i);
				for (int j = i + 1; j < triList.size(); j++) {
					b = triList.get(j);
					if (a.one.equals(b.one) || a.one.equals(b.two) || a.one.equals(b.three)
							|| a.two.equals(b.one) || a.two.equals(b.two) || a.two.equals(b.three)
							|| a.three.equals(b.one) || a.three.equals(b.two)
							|| a.three.equals(b.three)) {
						out = out + "Q:  P1 = [" + a.one.x + "," + a.one.y + "] P2 = [" + a.two.x
								+ "," + a.two.y + "] P3 = [" + a.three.x + "," + a.three.y
								+ "] shares a vertex with Q:  P1 = [" + b.one.x + "," + b.one.y
								+ "] P2 = [" + b.two.x + "," + b.two.y + "] P3 = [" + b.three.x
								+ "," + b.three.y + "], ";
					}
				}
			}
			out = out.substring(0, out.length() - 2);
			out = out.concat(".");

		} else if (s.equals("isosceles")) {
			out = "Here is a list of all triangles that are isosceles: ";
			for (int j = 0; j < triList.size(); j++) {

				if (triList.get(j).isIsosceles == true) {
					out = out + "T :  P1 = [" + triList.get(j).one.x + "," + triList.get(j).one.y
							+ "] P2 = [" + triList.get(j).two.x + "," + triList.get(j).two.y
							+ "] P3 = [" + triList.get(j).three.x + "," + triList.get(j).three.y
							+ "], ";
				}
			}
			out = out.substring(0, out.length() - 2);
			out = out.concat(".");

		} else if (s.equals("right")) {
			out = "Here is a list of all the triangles that are right: ";

			for (int j = 0; j < triList.size(); j++) {
				if (triList.get(j).isRight == true) {
					out = out + "T :  P1 = [" + triList.get(j).one.x + "," + triList.get(j).one.y
							+ "] P2 = [" + triList.get(j).two.x + "," + triList.get(j).two.y
							+ "] P3 = [" + triList.get(j).three.x + "," + triList.get(j).three.y
							+ "], ";
				}
			}

			out = out.substring(0, out.length() - 1);
			out = out.concat(".");
		} else if (Integer.parseInt(s) >= 0 && Integer.parseInt(s) <= 100) {
			out = "Here is a list of all the triangles that occured at least " + s
					+ " times since the server started: ";
			int num = Integer.parseInt(s);
			if (num == 0) {
				for (int j = 0; j < triList.size(); j++) {
					out = out + "T :  P1 = [" + triList.get(j).one.x + "," + triList.get(j).one.y
							+ "] P2 = [" + triList.get(j).two.x + "," + triList.get(j).two.y
							+ "] P3 = [" + triList.get(j).three.x + "," + triList.get(j).three.y
							+ "], ";
				}
			} else {
				for (int j = 0; j < triList.size(); j++) {
					if (triList.get(j).count >= num) {
						out = out + "T :  P1 = [" + triList.get(j).one.x + ","
								+ triList.get(j).one.y + "] P2 = [" + triList.get(j).two.x + ","
								+ triList.get(j).two.y + "] P3 = [" + triList.get(j).three.x + ","
								+ triList.get(j).three.y + "], ";
					}
				}

				out = out.substring(0, out.length() - 1);
				out = out.concat(".");
			}
		} else
			out = "I'm sorry. What you asked for did not make sense.";
		return out;
	}

	private String createTriangle(String input) {

		StringTokenizer tokens = new StringTokenizer(input);
		int p[] = new int[6];
		int x = 0;
		while (tokens.hasMoreTokens()) {
			p[x] = Integer.parseInt(tokens.nextToken());
			x++;
		}

		Point pts[] = { new Point(p[0], p[1]), new Point(p[2], p[3]), new Point(p[4], p[5]) };
		Point a = new Point(p[0], p[1]), b = new Point(p[2], p[3]), c = new Point(p[4], p[5]);
		if (a.equals(b) || a.equals(c) || b.equals(c)) {
			return "Error, two or more points are the same and does not define a triangle";
		}
		int i1, i2 = 0, i3 = 0;
		if (a.y < b.y && a.y < c.y) {
			i1 = 0;
		} else if (b.y < a.y && b.y < c.y) {
			i1 = 1;
		} else if (c.y < b.y && c.y < a.y) {
			i1 = 2;
		} else if (a.y == b.y && a.y != c.y) {
			if (b.x > a.x) {
				i1 = 0;
			} else {
				i1 = 1;
			}
		} else if (a.y == c.y && a.y != b.y) {
			if (c.x > a.x) {
				i1 = 0;
			} else {
				i1 = 2;
			}
		} else if (b.y == c.y && b.y != a.y) {
			if (c.x > b.x) {
				i1 = 1;
			} else {
				i1 = 2;
			}
		} else {
			return "This defines a horizontal line!";
		}
		float m1, m2;
		if (a.x == b.x && a.x == c.x) {
			return "Error, this is and undefined line!";
		}
		if (i1 == 0) {// for point a
			if (a.x == b.x) {
				m2 = c.y - a.y / c.x - a.x;
				if (m2 >= 0) {
					i2 = 2;
					i3 = 1;
				} else {
					i2 = 1;
					i3 = 2;
				}
			} else if (a.x == c.x) {
				m1 = b.y - a.y / b.x - a.x;
				if (m1 >= 0) {
					i2 = 1;
					i3 = 2;
				} else {
					i2 = 2;
					i3 = 1;
				}
			} else {
				m1 = b.y - a.y / b.x - a.x;
				m2 = c.y - a.y / c.x - a.x;
				if (m1 < 0 && m2 >= 0) {
					i2 = 2;
					i3 = 1;
				} else if (m1 >= 0 && m2 < 0) {
					i2 = 1;
					i3 = 2;
				} else if (m1 >= 0 && m2 >= 0) {
					if (m1 < m2) {
						i2 = 1;
						i3 = 2;
					} else {
						i2 = 2;
						i3 = 1;
					}
				} else if (m1 < 0 && m2 < 0) {
					if (m1 < m2) {
						i2 = 1;
						i3 = 2;
					} else {
						i2 = 2;
						i3 = 1;
					}
				}
			}
		}
		if (i1 == 1) { // for point b
			if (b.x == a.x) {
				m2 = c.y - b.y / c.x - b.x;
				if (m2 >= 0) {
					i2 = 2;
					i3 = 0;
				} else {
					i2 = 0;
					i3 = 2;
				}
			} else if (b.x == c.x) {
				m1 = a.y - b.y / a.x - b.x;
				if (m1 >= 0) {
					i2 = 0;
					i3 = 2;
				} else {
					i2 = 2;
					i3 = 0;
				}
			} else {
				m1 = c.y - b.y / c.x - b.x;
				m2 = a.y - b.y / a.x - b.x;
				if (m1 < 0 && m2 >= 0) {
					i2 = 0;
					i3 = 2;
				} else if (m1 >= 0 && m2 < 0) {
					i2 = 2;
					i3 = 0;
				} else if (m1 >= 0 && m2 >= 0) {
					if (m1 < m2) {
						i2 = 2;
						i3 = 0;
					} else {
						i2 = 0;
						i3 = 2;
					}
				} else if (m1 < 0 && m2 < 0) {
					if (m1 > m2) {
						i2 = 0;
						i3 = 2;
					} else {
						i2 = 2;
						i3 = 0;
					}

				}
			}
		}
		if (i1 == 2) { // for point c
			if (c.x == b.x) {
				m2 = a.y - c.y / a.x - c.x;
				if (m2 >= 0) {
					i2 = 0;
					i3 = 1;
				} else {
					i2 = 1;
					i3 = 0;
				}
			} else if (c.x == a.x) {
				m1 = b.y - c.y / b.x - c.x;
				if (m1 >= 0) {
					i2 = 1;
					i3 = 0;
				} else {
					i2 = 0;
					i3 = 1;
				}
			} else {
				m1 = b.y - c.y / b.x - c.x;
				m2 = a.y - c.y / a.x - c.x;
				if (m1 < 0 && m2 >= 0) {
					i2 = 0;
					i3 = 1;
				} else if (m1 >= 0 && m2 < 0) {
					i2 = 1;
					i3 = 0;
				} else if (m1 >= 0 && m2 >= 0) {
					if (m1 < m2) {
						i2 = 1;
						i3 = 0;
					} else {
						i2 = 0;
						i3 = 1;
					}
				} else if (m1 < 0 && m2 < 0) {
					if (m1 > m2) {
						i2 = 0;
						i3 = 1;
					} else {
						i2 = 1;
						i3 = 0;
					}

				}
			}
		}
		Triangle t = new Triangle(pts[i1], pts[i2], pts[i3]);
		for (int i = 0; i < triList.size(); i++) {
			if (triList.get(i).one.equals(t.one) && triList.get(i).two.equals(t.two)
					&& triList.get(i).three.equals(t.three)) {

				triList.get(i).count++;
				return "Sorry. That triangle was previously defined.";

			}

		}
		triList.add(new Triangle(pts[i1], pts[i2], pts[i3]));
		triList.get(triList.size() - 1).checkIsoAndRight();
		// System.out.print(triList.get(triList.size() - 1).isRight);
		return "The triangle you entered was stored in the data structure.";
	}
}

class Triangle {
	Point one;
	Point two;
	Point three;
	short count;
	boolean isIsosceles = false;
	boolean isRight = false;

	Triangle(Point a, Point b, Point c) {
		this.one = a;
		this.two = b;
		this.three = c;
		this.count = 1;
	}

	public void checkIsoAndRight() {
		int a1, a2, a3;

		a1 = (int) (Math.pow(two.y - one.y, 2) + Math.pow(two.x - one.x, 2));
		a2 = (int) (Math.pow(three.y - one.y, 2) + Math.pow(three.x - one.x, 2));
		a3 = (int) (Math.pow(three.y - two.y, 2) + Math.pow(three.x - two.x, 2));

		if (a1 == a2 || a1 == a3 || a2 == a3) {
			isIsosceles = true;
		}
		if (a1 + a3 == a2 || a1 + a2 == a3 || a2 + a3 == a1) {
			isRight = true;
		}
	}

}

class Quad {
	Point one;
	Point two;
	Point three;
	Point four;
	short count;
	boolean isRectangle = false;
	boolean isRhombus = false;
	boolean isSquare = false;
	boolean isParalellagram = false;
	boolean isConvex = true;

	Quad(Point a, Point b, Point c, Point d) {
		this.one = a;
		this.two = b;
		this.three = c;
		this.four = d;
		this.count = 1;
	}

	public void checkAll() {
		int a1, a2, a3, a4;
		Point[] l = { one, two, three, four };
		a1 = (int) (Math.pow(two.y - one.y, 2) + Math.pow(two.x - one.x, 2));
		a2 = (int) (Math.pow(three.y - two.y, 2) + Math.pow(three.x - two.x, 2));
		a3 = (int) (Math.pow(four.y - three.y, 2) + Math.pow(four.x - three.x, 2));
		a4 = (int) (Math.pow(one.y - four.y, 2) + Math.pow(one.x - four.x, 2));
		Point p;
		Point v;
		Point u;
		int res = 0;
		for (int i = 0; i < 4; i++) {
			p = l[i];
			Point tmp = l[(i + 1) % 4];
			v = new Point();
			v.x = tmp.x - p.x;
			v.y = tmp.y - p.y;
			u = l[(i + 2) % 4];

			if (i == 0) {// in first loop direction is unknown, so save it in
				// res
				res = u.x * v.y - u.y * v.x + v.x * p.y - v.y * p.x;
			} else {
				int newres = u.x * v.y - u.y * v.x + v.x * p.y - v.y * p.x;
				if ((newres > 0 && res < 0) || (newres < 0 && res > 0)) {
					isConvex = false;
					break;
				}
			}
		}

		if (a1 == a2 && a1 == a3 && a1 == a4) {
			isRhombus = true;
			isParalellagram = true;
			if (((one.x * two.x) + (one.y * two.y)) == 0) {
				isSquare = true;
				isRectangle = true;
			}
		} else if (a1 == a3 && a2 == a4) {
			isParalellagram = true;
			if (((one.x * two.x) + (one.y * two.y)) == 0) {
				isRectangle = true;
			}
		}
	}
}