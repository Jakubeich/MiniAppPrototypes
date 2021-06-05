package com.jakubeich.paint;

import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Paint extends JFrame {
	public static final int CANVAS_WIDTH = 500;
	public static final int CANVAS_HEIGHT = 300;
	public static final Color LINE_COLOR = Color.RED;

	private List<PolyLine> lines = new ArrayList<PolyLine>();
	private PolyLine currentLine;

	public Paint() {
		DrawCanvas canvas = new DrawCanvas();
		canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		canvas.addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent evt) {
				currentLine = new PolyLine();
				lines.add(currentLine);
				currentLine.addPoint(evt.getX(), evt.getY());
			}
		});
		canvas.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent evt) {
				currentLine.addPoint(evt.getX(), evt.getY());
				repaint();
			}
		});

		setContentPane(canvas);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Paint");
		pack();
		setVisible(true);
	}

	private class DrawCanvas extends JPanel {
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(LINE_COLOR);
			for (PolyLine line : lines) {
				line.draw(g);
			}
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Paint();
			}
		});
	}
}
