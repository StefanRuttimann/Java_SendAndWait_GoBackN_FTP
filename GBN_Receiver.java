/* CP372 Networks Assignment 2
 * Stefan Ruttimann and Hasan Cheaib
 * 110708680 & 091727720
 * Go-Back-N Receiver
 * --receives a specific file from a sender
 */
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GBN_Receiver {
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
		byte[] data = new byte[124];
		byte[] fdata = new byte[115];
		byte[] ack = new byte[6];
		byte[] lNum = new byte[3];
		byte[] las = new byte[3];
		try {
			InetAddress address = InetAddress.getByName(args[0]);
			socket = new DatagramSocket(Integer.parseInt(args[2]));
			RandomAccessFile f = new RandomAccessFile(args[3], "rw");
			System.out.println("Waiting for a packet!\n");
			while (true) {

				recievePacket = new DatagramPacket(data, data.length);
				socket.receive(recievePacket);
				dat = new String(recievePacket.getData(), 0, recievePacket.getLength());
				System.out.println(dat);
				System.arraycopy(data, 115, las, 0, las.length);
				if (dat.endsWith("EOF") && Integer.parseInt(new String(las, "UTF-8")) == seqNum) {
					System.arraycopy(data, 118, lNum, 0, lNum.length);
					left = Integer.parseInt(new String(lNum, "UTF-8"));
					System.out.println(Integer.toString(left));
					fdata = new byte[left];
					System.arraycopy(data, 0, fdata, 0, left);
					f.write(fdata);
					ack = ("ACK" + Integer.toString(seqNum)).getBytes();
					sendPacket = new DatagramPacket(ack, ack.length, address,
							Integer.parseInt(args[1]));
					socket.send(sendPacket);
					break;
				} else if (dat.endsWith(Integer.toString(seqNum))) {
					ack = ("ACK" + Integer.toString(seqNum)).getBytes();
					dat = dat.substring(0, dat.length() - Integer.toString(seqNum).length());
					fdata = new byte[115];
					System.arraycopy(data, 0, fdata, 0, fdata.length);
					f.write(fdata);
					seqNum++;
					seqNum = seqNum % 128;
				} else {
					ack = ("ACK" + Integer.toString(seqNum - 1)).getBytes();
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