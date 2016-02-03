/* CP372 Networks Assignment 2
 * Stefan Ruttimann and Hasan Cheaib
 * 110708680 & 091727720
 * Send-And-Wait Receiver
 * --receives a specific file from a sender
 */

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class S_A_W_Reciever {
	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.exit(0);
		}
		DatagramSocket socket = null;
		DatagramPacket sendPacket = null;
		DatagramPacket recievePacket = null;
		int seqNum = 0;
		String dat;
		int left;
		byte[] data = new byte[1024];
		byte[] fdata = new byte[1016];
		byte[] ack = new byte[3];
		byte[] lNum = new byte[4];
		try {
			InetAddress address = InetAddress.getByName(args[0]);
			socket = new DatagramSocket(Integer.parseInt(args[2]));
			RandomAccessFile f = new RandomAccessFile(args[3], "rw");
			System.out.println("Waiting for a packet!\n");
			while (true) {

				recievePacket = new DatagramPacket(data, data.length);
				socket.receive(recievePacket);
				data = AES.decrypt(data);
				dat = new String(data, "UTF-8");
				System.out.println(dat);
				if (dat.endsWith("EOF")) {
					System.arraycopy(data, 1017, lNum, 0, lNum.length);
					left = Integer.parseInt(new String(lNum, "UTF-8"));
					System.out.println(Integer.toString(left));
					fdata = new byte[left];
					System.arraycopy(data, 0, fdata, 0, left);
					f.write(fdata);
					ack = "ACK".getBytes();
					sendPacket = new DatagramPacket(ack, ack.length, address,
							Integer.parseInt(args[1]));
					socket.send(sendPacket);
					break;
				} else if (dat.endsWith(Integer.toString(seqNum))) {
					ack = "ACK".getBytes();
					dat = dat.substring(0, dat.length() - Integer.toString(seqNum).length());
					fdata = new byte[1016];
					System.arraycopy(data, 0, fdata, 0, fdata.length);
					f.write(fdata);
					seqNum++;
					seqNum = seqNum % 2;
				} else {
					ack = "NAK".getBytes();
				}
				sendPacket = new DatagramPacket(ack, ack.length, address, Integer.parseInt(args[1]));
				socket.send(sendPacket);

			}
			f.close();
			System.out.println("All data received.");

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			try {
				socket.close();

			} catch (Exception e) {
			}
		}
	}
}