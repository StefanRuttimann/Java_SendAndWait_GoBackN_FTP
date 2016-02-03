/* CP372 Networks Assignment 2
 * Stefan Ruttimann and Hasan Cheaib
 * 110708680 & 091727720
 * Go-Back-N Sender
 * --sends a specific file to a receiver
 */

import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class GBN_Sender extends Thread {

	public static void main(String[] args) {

		if (args.length != 6) {
			System.exit(0);
		}
		if (Integer.parseInt(args[5]) > 128 && Integer.parseInt(args[5]) < 0) {
			System.out.println("Window size of " + args[5]
					+ " is invalid. It must be less than 129 and bigger than or equal to 0.");
			System.exit(0);
		}
		if (Integer.parseInt(args[4]) < 0) {
			System.out.println("RN number of " + args[4]
					+ " is invalid. It must be greater than or equal to 0.");
			System.exit(0);
		}
		new GBN_Sender(args[0], args[1], args[2], args[3], args[4], args[5]).start();
	}

	private DatagramSocket socket = null;
	private DatagramPacket sendPacket = null;
	private DatagramPacket recievePacket = null;
	private RandomAccessFile f;
	private byte[] file;
	private InetAddress address;
	private int rn;
	private int destp;
	private int n;
	private int sn = 0;
	private byte[] seqNum = new byte[3];
	private byte[] temp = new byte[115];
	private byte[] buf;
	private byte[] ack = new byte[6];
	private byte[] lNum = new byte[3];
	private byte[] eOfF = new byte[3];
	private String ak;
	private long tTT = 0;
	private boolean sendN = true, continueSending = true;
	private long start = System.currentTimeMillis();
	private int c = 0, a = 1, next = n, b = 0;
	private int l, lastSN = 200;
	private int curL = 0;

	boolean eof = false;
	int left;

	GBN_Sender(String ad, String dp, String sp, String filename, String rn, String n) {
		try {
			this.rn = Integer.parseInt(rn);
			this.n = Integer.parseInt(n);
			this.f = new RandomAccessFile(filename, "r");
			this.address = InetAddress.getByName(ad);
			this.socket = new DatagramSocket(Integer.parseInt(sp));
			this.destp = Integer.parseInt(dp);
			this.file = new byte[(int) f.length()];
			this.l = (int) f.length();
		} catch (Exception e) {

		}
	}

	@Override
	public void run() {
		try {

			f.readFully(file);
			f.close();
			sendN = true;
			while (continueSending) {

				while (c < next && !eof && sendN) {
					parseAndSendDatagram();

				}
				sendN = false;
				readAndParse();

			}

			System.out.println("All data sent.");
			tTT = System.currentTimeMillis() - start;
			System.out.println("Total Transmission Time(ms):" + tTT);
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				socket.close();

			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private void readAndParse() throws Exception {
		socket.setSoTimeout(200);
		int count = 0;
		while (count < n && continueSending) {
			recievePacket = new DatagramPacket(ack, ack.length);
			try {
				socket.receive(recievePacket);
				ak = new String(recievePacket.getData(), 0, recievePacket.getLength());
				System.out.println(ak);
				if (ak.equals("ACK" + Integer.toString(sn))) {
					sn++;
					if (sn % 128 == 0) {
						b++;
					}
					sn %= 128;
					if (ak.endsWith(Integer.toString(lastSN))) {
						continueSending = false;
					}
					if (!eof) {
						parseAndSendDatagram();
					}

					socket.setSoTimeout(200);
				} else {
					if (eof) {
						eof = false;
					}
				}

			} catch (SocketTimeoutException e) {
				sendN = true;
				c = (128 * b) + sn;
				next = c + n;
				curL = c * 115;
				break;
			}
			count++;
		}
	}

	private void parseAndSendDatagram() throws Exception {
		temp = new byte[115];
		seqNum = new byte[3];
		int conver = c % 128;
		if (conver < 10) {
			seqNum = ("00" + Integer.toString(conver)).getBytes();
		} else if (conver < 100 && conver > 9) {
			seqNum = ("0" + Integer.toString(conver)).getBytes();
		} else {
			seqNum = Integer.toString(conver).getBytes();
		}
		left = l - curL;

		if (left < 115) {
			lastSN = conver;
			eOfF = "EOF".getBytes();
			if (left < 10) {
				lNum = ("00" + Integer.toString(left)).getBytes();
			} else if (left < 100 && left > 9) {
				lNum = ("0" + Integer.toString(left)).getBytes();
			} else {
				lNum = Integer.toString(left).getBytes();
			}
			System.arraycopy(file, 115 * c, temp, 0, left);
			buf = new byte[temp.length + seqNum.length + lNum.length + eOfF.length];
			System.arraycopy(temp, 0, buf, 0, temp.length);
			System.arraycopy(seqNum, 0, buf, temp.length, seqNum.length);
			System.arraycopy(lNum, 0, buf, temp.length + seqNum.length, lNum.length);
			System.arraycopy(eOfF, 0, buf, temp.length + seqNum.length + lNum.length, eOfF.length);
			eof = true;
		} else {
			System.arraycopy(file, 115 * c, temp, 0, temp.length);
			buf = new byte[temp.length + seqNum.length];
			System.arraycopy(temp, 0, buf, 0, temp.length);
			System.arraycopy(seqNum, 0, buf, temp.length, seqNum.length);
		}

		sendPacket = new DatagramPacket(buf, buf.length, address, destp);
		System.out.println(new String(sendPacket.getData(), 0, sendPacket.getLength()));
		if (rn == 0) {
			socket.send(sendPacket);
		} else if (rn > 1) {
			if (c + 1 != a * rn) {
				socket.send(sendPacket);
			} else {
				a++;
			}
		}
		c++;
		curL += 115;
	}

}
