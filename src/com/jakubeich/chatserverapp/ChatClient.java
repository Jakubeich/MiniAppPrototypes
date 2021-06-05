package com.jakubeich.chatserverapp;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.out;

public class ChatClient extends JFrame implements ActionListener {
	String uname;
	PrintWriter pw;
	BufferedReader br;
	JTextArea taMessages;
	JTextField tfInput;
	JButton btnSend, btnExit;
	Socket client;

	public ChatClient(String uname, String servername) throws Exception {
		super(uname);
		this.uname = uname;
		client = new Socket(servername, 9999);
		br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		pw = new PrintWriter(client.getOutputStream(), true);
		pw.println(uname);
		buildInterface();
		new MessagesThread().start();
	}

	public void buildInterface() {
		btnSend = new JButton("Send");
		btnExit = new JButton("Exit");
		taMessages = new JTextArea();
		taMessages.setRows(10);
		taMessages.setColumns(50);
		taMessages.setEditable(false);
		tfInput = new JTextField(50);
		JScrollPane sp = new JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(sp, "Center");
		JPanel bp = new JPanel(new FlowLayout());
		bp.add(tfInput);
		bp.add(btnSend);
		bp.add(btnExit);
		add(bp, "South");
		btnSend.addActionListener(this);
		btnExit.addActionListener(this);
		setSize(500, 300);
		setVisible(true);
		pack();
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == btnExit) {
			pw.println("end");
			System.exit(0);
		} else {
			pw.println(tfInput.getText());
		}
	}

	public static void main(String... args) {

		String name = JOptionPane.showInputDialog(null, "Enter your name :", "Username", JOptionPane.PLAIN_MESSAGE);
		String servername = "localhost";
		try {
			new ChatClient(name, servername);
		} catch (Exception ex) {
			out.println("Error --> " + ex.getMessage());
		}

	}

	class MessagesThread extends Thread {
		public void run() {
			String line;
			try {
				while (true) {
					line = br.readLine();
					taMessages.append(line + "\n");
				}
			} catch (Exception ex) {
			}
		}
	}
}
