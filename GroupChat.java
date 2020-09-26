/*Preethi Ann Jacob 26-09-2020
Group Chat application in Java

Run in command prompt:
For example:
	javac GroupChat.java
	java GroupChat 239.0.0.0 1234
Use 'exit' to get out of the chatroom
*/
import java.net.*;
import java.io.*;
import java.util.*;
public class GroupChat{
	static String name;
	static boolean left=false;
	public static void main(String args[]){
		if(args.length!=2)
			System.out.println("Two arguments needed: 1.Multicast host IP address and 2. Port Number");
		else{
			try{
				BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
				System.out.print("\nEnter your name(This will be visible to all the members in the chatroom):");
				name=br.readLine();

				InetAddress chatroomIP=InetAddress.getByName(args[0]);
				int port=Integer.parseInt(args[1]);
				MulticastSocket socket =new MulticastSocket(port);
				socket.setTimeToLive(0);//Set time to live as 0 for localhost and greater for other networks
				socket.joinGroup(chatroomIP);
				
				//Start another thread at the same time for reading the messages
				Thread t=new Thread(new ReadThread(socket,chatroomIP,port));
				t.start();

				//For the current user
				System.out.println("\nYou are registered successfully. You may start typing messages:");
				while(true){
					String message=br.readLine();
					if (message.equalsIgnoreCase("exit")){
						left=true;
						socket.leaveGroup(chatroomIP);
						socket.close();
						break;
					}
					message=name+": "+message;
					byte buffer[]=message.getBytes();
					DatagramPacket datagram=new DatagramPacket(buffer,buffer.length,chatroomIP,port);
					socket.send(datagram);
				}
			}
			catch(SocketException se){
				System.out.println("\nError in socket creation");
				se.printStackTrace();
			}
			catch(IOException ioe){
				System.out.println("\nError in reading/sending messages");
				ioe.printStackTrace();
			}
		}
	}
}
//Class for reading messages
class ReadThread implements Runnable{
	private MulticastSocket socket;
	private InetAddress chatroomIP;
	private int port;
	ReadThread(MulticastSocket socket,InetAddress chatroomIP,int port){
		this.socket=socket;
		this.chatroomIP=chatroomIP;
		this.port=port;
	}
	public void run(){
		while(!GroupChat.left){
			byte buffer[]=new byte[1000];//maximum size of messages set to 1000 characters
			DatagramPacket datagram=new DatagramPacket(buffer,buffer.length,chatroomIP,port);
			String message;
			try{
				socket.receive(datagram);
				/*String(byte[] bytes, int offset, int length, Charset charset) - Constructs a 
				new String by decoding the specified subarray of bytes using specified charset*/
				message=new String(buffer,0,datagram.getLength(),"UTF-8");
				System.out.println(message);
			}
			catch (IOException ie){
				System.out.println("\nSocket closed.See you later :)");
			}
		}
	}
}
