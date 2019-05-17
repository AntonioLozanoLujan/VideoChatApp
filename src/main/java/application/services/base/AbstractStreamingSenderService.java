package application.services.base;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import application.services.Service;
import utils.logging.LoggingUtils;

public abstract class AbstractStreamingSenderService extends Service {

	protected int port;
	protected boolean clientConnected = false;
	protected DatagramSocket socketUDP = null;
	protected DatagramPacket peticion = null;
	protected byte[] data;
	
	protected AbstractStreamingSenderService() {
		super();
		addServiceTask(this::send);
	}
	
	private void send() {
		try {
			publish();
		} catch (Exception e) {
			System.out.println(LoggingUtils.getStackTrace(e));
		}
	}
	
	
	protected DatagramSocket initSocket() {
		DatagramSocket socket = null;
		boolean valid = false;
		while(!valid) {
			try {
				socket = new DatagramSocket(port);
				valid = true;
			} catch (Exception e) {
				valid = false;
				port++;
			}
		}
		System.out.println(getClass().getSimpleName() + " listening on port: " + port);
		return socket;
	}
	
	public final void startSender() {
		clientConnected =false;
		acceptConnection();
		try {
			if(!isAlive()) {
				start();
			}
		} catch (Exception e) {
			System.out.println(LoggingUtils.getStackTrace(e));
		}
	}
	
	private void acceptConnection() {
		new Thread(() ->  {
			try {
				byte[] bufer = new byte[1024];
				peticion = new DatagramPacket(bufer, bufer.length);
				System.err.println("listening for datagrams on "+ port +" in address " +socketUDP.getLocalAddress().getHostName());
				socketUDP.receive(peticion);
				
				System.out.println("Cliente conectado.");
				System.out.print("Datagrama recibido del host: " + peticion.getAddress());
				System.out.println(" desde el puerto remoto: " + peticion.getPort());
				
				clientConnected = true;
			} catch (SocketException e) {
				System.out.println("Socket: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("IO: " + e.getMessage());
			}
			
		}).start();
	}
	
	protected void publish() {
		if (clientConnected) {
			// Construimos el DatagramPacket para enviar la respuesta
			if (data != null) {
				DatagramPacket respuesta = new DatagramPacket(data, data.length, peticion.getAddress(), peticion.getPort());
				try {
					// Enviamos la respuesta
					socketUDP.send(respuesta);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	protected void onStopedService() {
		clientConnected =false;
	}
}
