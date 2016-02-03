/* CP372 Networks Assignment 2
 * Stefan Ruttimann and Hasan Cheaib
 * 110708680 & 091727720
 * Send-And-Wait Sender
 * --sends a specific file to a receiver
 */

import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class S_A_W_Sender {

	public static void main(String[] args) {

		if (args.length != 5) {
			System.exit(0);
		}
		if (Integer.parseInt(args[4]) < 0) {
			System.out.println("RN number of " + args[4]
					+ " is invalid. It must be greater than or equal to 0.");
			System.exit(0);
		}
		DatagramSocket socket = null;
		DatagramPacket sendPacket = null;
		DatagramPacket recievePacket = null;
		int rn = Integer.parseInt(args[4]);
		System.out.println(Integer.toString(rn));
		int sn = 0;
		byte[] seqNum = new byte[1];
		byte[] temp = new byte[1016];
		byte[] buf;
		byte[] ack = new byte[3];
		byte[] lNum = new byte[4];
		byte[] eOfF = new byte[3];
		eOfF = "EOF".getBytes();
		String ak;
		long tTT = 0;
		boolean continueSending = true;
		long start = System.currentTimeMillis();
		int c = 0, a = 1;

		boolean eof = false;
		int left;
		try {

			RandomAccessFile f = new RandomAccessFile(args[3], "r");
			byte[] file = new byte[(int) f.length()];
			int l = (int) f.length();
			int curL = 0;
			f.readFully(file);
			f.close();
			InetAddress address = InetAddress.getByName(args[0]);
			socket = new DatagramSocket(Integer.parseInt(args[2]));

			while (!eof) {
				temp = new byte[1016];

				seqNum = Integer.toString(sn).getBytes();
				left = l - curL;
				if (left < 1016) {
					if (left < 10) {
						lNum = ("000" + Integer.toString(left)).getBytes();
					} else if (left < 100 && left > 9) {
						lNum = ("00" + Integer.toString(left)).getBytes();
					} else if (left < 1000 && left > 99) {
						lNum = ("0" + Integer.toString(left)).getBytes();
					} else {
						lNum = Integer.toString(left).getBytes();
					}
					System.arraycopy(file, 1016 * c, temp, 0, left);
					buf = new byte[temp.length + seqNum.length + lNum.length + eOfF.length];
					System.arraycopy(temp, 0, buf, 0, temp.length);
					System.arraycopy(seqNum, 0, buf, temp.length, seqNum.length);
					System.arraycopy(lNum, 0, buf, temp.length + seqNum.length, lNum.length);
					System.arraycopy(eOfF, 0, buf, temp.length + seqNum.length + lNum.length,
							eOfF.length);
					eof = true;
				} else {
					System.arraycopy(file, 1016 * c, temp, 0, temp.length);
					buf = new byte[1024];
					System.arraycopy(temp, 0, buf, 0, temp.length);
					System.arraycopy(seqNum, 0, buf, temp.length + 7, seqNum.length);
				}
				buf = AES.encrypt(buf);

				sendPacket = new DatagramPacket(buf, buf.length, address, Integer.parseInt(args[1]));
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

				socket.setSoTimeout(300);
				while (continueSending) {
					recievePacket = new DatagramPacket(ack, ack.length);
					try {
						socket.receive(recievePacket);
						ak = new String(recievePacket.getData(), 0, recievePacket.getLength());
						System.out.println(ak);
						if (ak.equals("ACK") || ak.equals("NAK")) {
							continueSending = false;
						}

					} catch (SocketTimeoutException e) {
						socket.send(sendPacket); // send again when timeout
						socket.setSoTimeout(300);
						continue;
					}
				}
				sn++;
				sn %= 2;
				c++;
				curL += 1016;
				continueSending = true;
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
}
