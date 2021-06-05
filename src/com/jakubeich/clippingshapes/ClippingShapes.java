package com.jakubeich.clippingshapes;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

class Surface extends JPanel implements ActionListener {

	private Timer timer;
	private double rotate = 1;
	private int x = 8;
	private int y = 8;
	private final double delta[] = { 1, 1 };

	private final int RADIUS = 60;

	public Surface() {

		initTimer();
	}

	private void initTimer() {

		timer = new Timer(10, this);
		timer.start();
	}

	private void doDrawing(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		Shape oldClip = g2d.getClip();

		int w = getWidth();
		int h = getHeight();

		Rectangle rect = new Rectangle(0, 0, 200, 80);

		AffineTransform tx = new AffineTransform();
		tx.rotate(Math.toRadians(rotate), w / 2, h / 2);
		tx.translate(w / 2 - 100, h / 2 - 40);

		Ellipse2D circle = new Ellipse2D.Double(x, y, RADIUS, RADIUS);

		GeneralPath path = new GeneralPath();
		path.append(tx.createTransformedShape(rect), false);

		g2d.clip(circle);
		g2d.clip(path);

		g2d.setPaint(new Color(110, 110, 110));
		g2d.fill(circle);

		g2d.setClip(oldClip);

		g2d.draw(circle);
		g2d.draw(path);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		doDrawing(g);
	}

	public void step() {

		int w = getWidth();
		int h = getHeight();

		rotate += 1;

		if (x < 0) {

			delta[0] = 1;
		} else if (x > w - RADIUS) {

			delta[0] = -1;
		}

		if (y < 0) {

			delta[1] = 1;
		} else if (y > h - RADIUS) {

			delta[1] = -1;
		}

		x += delta[0];
		y += delta[1];
	}

	public void actionPerformed(ActionEvent e) {

		step();
		repaint();
	}
}

public class ClippingShapes extends JFrame {

	public ClippingShapes() {

		initUI();
	}

	private void initUI() {

		setTitle("Clipping Shapes");

		add(new Surface());

		setSize(350, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				ClippingShapes ex = new ClippingShapes();
				ex.setVisible(true);
			}
		});
	}
}
