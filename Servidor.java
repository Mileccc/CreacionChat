
import javax.swing.*;

import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Servidor {

	public static void main(String[] args) {

		MarcoServidor mimarco = new MarcoServidor();

		mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}
}

class MarcoServidor extends JFrame implements Runnable {

	public MarcoServidor() {

		setBounds(1200, 300, 280, 350);

		JPanel milamina = new JPanel();

		milamina.setLayout(new BorderLayout());

		areatexto = new JTextArea();

		milamina.add(areatexto, BorderLayout.CENTER);

		add(milamina);

		setVisible(true);

		Thread mihilo = new Thread(this);
		mihilo.start();

	}

	private JTextArea areatexto;

	@Override
	public void run() {
		try {
			// Inicializar un ServerSocket para escuchar en el puerto 55011
			ServerSocket servidor = new ServerSocket(55011);

			String nick, ip, mensaje;
			ArrayList<String> listaIp = new ArrayList<String>();
			PaqueteEnvio paquete_recibido;

			while (true) {

				// Aceptar una conexión entrante; 'misocket' representa esta conexión
				Socket misocket = servidor.accept();

				ObjectInputStream paquete_datos = new ObjectInputStream(misocket.getInputStream());

				paquete_recibido = (PaqueteEnvio) paquete_datos.readObject();

				nick = paquete_recibido.getNick();
				ip = paquete_recibido.getIp();
				mensaje = paquete_recibido.getMensaje();

				if (!mensaje.equals(" online")) {

					areatexto.append("\n" + nick + ": " + mensaje + " para " + ip);

					Socket enviaDestinatario = new Socket(ip, 9090);
					ObjectOutputStream paqueteReenvio = new ObjectOutputStream(enviaDestinatario.getOutputStream());
					paqueteReenvio.writeObject(paquete_recibido);

					paquete_datos.close();
					paqueteReenvio.close();
					enviaDestinatario.close();
					misocket.close();

				} else {
					// ------------DETECTA ONLINE---------------------
					InetAddress localizacion = misocket.getInetAddress();
					String IpRemota = localizacion.getHostAddress();
					System.out.println("Online " + IpRemota);

					listaIp.add(IpRemota);

					paquete_recibido.setIps(listaIp);

					for (String z : listaIp) {
						System.out.println("Array: " + z);

						Socket enviaDestinatario = new Socket(z, 9090);
						ObjectOutputStream paqueteReenvio = new ObjectOutputStream(enviaDestinatario.getOutputStream());
						paqueteReenvio.writeObject(paquete_recibido);

						paquete_datos.close();
						paqueteReenvio.close();
						enviaDestinatario.close();
						misocket.close();

					}

					// -----------------------------------------------
				}

				/*
				 * // Crear un flujo de entrada para recibir datos del cliente
				 * DataInputStream flujo_entrada = new
				 * DataInputStream(misocket.getInputStream());
				 * // Leer el mensaje enviado por el cliente y almacenarlo en 'mensaje_texto'
				 * String mensaje_texto = flujo_entrada.readUTF();
				 * // Añadir el mensaje recibido al área de texto de la interfaz gráfica
				 * areatexto.append("\n" + mensaje_texto);
				 * // Cerrar el socket después de recibir el mensaje
				 * misocket.close();
				 */
			}

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
}
