
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;

public class Cliente {

	public static void main(String[] args) {

		MarcoCliente mimarco = new MarcoCliente();

		mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mimarco.pack();
		mimarco.setVisible(true);

	}

}

class MarcoCliente extends JFrame {

	public MarcoCliente() {

		setBounds(600, 300, 280, 350);

		LaminaMarcoCliente milamina = new LaminaMarcoCliente();

		add(milamina);

		pack();

		addWindowListener(new EnvioOnline());
	}

}
// ------------ENVIO DE SEÑAL ONLINE----------------------
// Heredando de WindowAdapter logramos poder añadir eventos en la ventana
class EnvioOnline extends WindowAdapter {
	// Con WindowOpened hara que se ejecute el código al abrir la ventana
	public void windowOpened(WindowEvent e) {
		try {
			Socket misocket = new Socket("192.168.1.100", 55011);

			PaqueteEnvio datos = new PaqueteEnvio();
			datos.setMensaje(" online");

			ObjectOutputStream paquete_datos = new ObjectOutputStream(misocket.getOutputStream());
			paquete_datos.writeObject(datos);
			misocket.close();
		} catch (Exception e2) {
		}
	}
}
// ------------------------------------------------------------------

class LaminaMarcoCliente extends JPanel implements Runnable {

	public LaminaMarcoCliente() {

		setLayout(new BorderLayout());
		String nick_usuario = JOptionPane.showInputDialog("Nick:");
		JLabel n_nick = new JLabel("Nick: ");

		nick = new JLabel();
		nick.setText(nick_usuario);
		JLabel texto = new JLabel("   Online: ");
		ip = new JComboBox();

		/* 
		ip.addItem("Usuario 1");
		ip.addItem("Usuario 2");
		ip.addItem("Usuario 3");
		*/

		// ip.addItem("192.168.88.128");
		// ip.addItem("192.168.88.130");

		JPanel panelTitulo = new JPanel();
		panelTitulo.setLayout(new FlowLayout());
		panelTitulo.add(n_nick);
		panelTitulo.add(nick);
		panelTitulo.add(texto);
		panelTitulo.add(ip);
		add(panelTitulo, BorderLayout.NORTH);

		campoChat = new JTextArea(12, 20);
		JScrollPane scroll = new JScrollPane(campoChat);
		add(campoChat, BorderLayout.CENTER);

		JPanel panelInferior = new JPanel();
		campo1 = new JTextField(20);
		panelInferior.add(campo1);

		miboton = new JButton("Enviar");
		EnviaTexto mievento = new EnviaTexto();
		miboton.addActionListener(mievento);
		panelInferior.add(miboton);
		add(panelInferior, BorderLayout.SOUTH);

		Thread mihilo = new Thread(this);
		mihilo.start();

	}

	private class EnviaTexto implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			campoChat.append("\n" + campo1.getText());

			try {
				// Crear un socket para conectarse al servidor en la dirección IP
				// "192.168.1.100" y el puerto 55011
				Socket misocket = new Socket("192.168.1.100", 55011);

				PaqueteEnvio datos = new PaqueteEnvio();
				datos.setNick(nick.getText());
				datos.setIp(ip.getSelectedItem().toString());
				datos.setMensaje(campo1.getText());

				ObjectOutputStream paquete_datos = new ObjectOutputStream(misocket.getOutputStream());
				paquete_datos.writeObject(datos);
				misocket.close();

				/*
				 * // Crear un flujo de salida para enviar datos al servidor
				 * DataOutputStream flujo_salida = new
				 * DataOutputStream(misocket.getOutputStream());
				 * // Escribir y enviar el texto del campo de texto 'campo1' al servidor
				 * flujo_salida.writeUTF(campo1.getText());
				 * // Cerrar el flujo de salida una vez que se ha enviado el mensaje
				 * flujo_salida.close();
				 */

			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				System.out.println(e1.getMessage());
			}

		}
	}

	private JTextField campo1;
	private JComboBox ip;
	private JTextArea campoChat;
	private JButton miboton;
	private JLabel nick;

	@Override
	public void run() {
		try {

			ServerSocket servidor_cliente = new ServerSocket(9090);
			Socket cliente;

			PaqueteEnvio paqueteRecibido;

			while (true) {
				cliente = servidor_cliente.accept();

				ObjectInputStream flujo_entrada = new ObjectInputStream(cliente.getInputStream());
				paqueteRecibido = (PaqueteEnvio) flujo_entrada.readObject();

				if(!paqueteRecibido.getMensaje().equals(" online")) {
					campoChat.append("\n" + paqueteRecibido.getNick() + ": " + paqueteRecibido.getMensaje());
				}else{
					// campoChat.append("\n" + paqueteRecibido.getIps());
					ArrayList<String> IpsMenu = new ArrayList<String>();
					IpsMenu = paqueteRecibido.getIps();

					ip.removeAllItems();

					for(String z:IpsMenu){
						ip.addItem(z);
					}
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}