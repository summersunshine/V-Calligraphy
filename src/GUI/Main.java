package GUI;
import java.awt.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import Util.RotateImage;
import Curve.BezierCurve;
import Curve.Calligraphy;
import Curve.Canvas;
import Curve.Curve;
import Curve.Point;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

/**
 * ʵ���� B�������ߣ���Ҫ��������������������϶�
 * 
 * @author Lgw
 *
 */
public class Main extends JFrame implements MouseMotionListener, MouseListener, ActionListener
{
	private JPanel paintPanel;
	private int pointNumber;
	private Vector<Point> points;

	boolean isTouch;

	public Main()
	{
		this.setLayout(null);
		initPanel();
		initButton();
		this.setBackground(Color.white);

		isTouch = false;
		pointNumber = 0;
		points = new Vector<Point>();
	}

	public void initPanel()
	{
		paintPanel = new JPanel();
		paintPanel.setBackground(Color.WHITE);
		paintPanel.setBounds(0, 50, 800, 500);
		paintPanel.addMouseListener(this);
		paintPanel.addMouseMotionListener(this);
		this.add(paintPanel);
	}

	public void initButton()
	{
		JButton btn = new JButton("��B��������");
		btn.setBounds(200, 10, 150, 30);
		btn.addActionListener(this);
		this.add(btn);
		JButton reset = new JButton("�ػ�");
		reset.setBounds(370, 10, 70, 30);
		reset.addActionListener(this);
		this.add(reset);
	}

	public static void main(String args[])
	{
		Main mainFrame = new Main();
		mainFrame.setSize(800, 550);
		mainFrame.setTitle("B��������");
		mainFrame.setVisible(true);
	}

	/**
	 * ��B�������ߡ���Ҫ����֮ǰ����¼��ĸ���
	 * 
	 * @param g
	 */
	public void drawRectAndLine(Graphics g)
	{

		drawRect(g, Color.red, points);
		drawLine(g, Color.red, points);
	}

	public void drawRect(Graphics g, Color color, Vector<Point> points)
	{
		g.setColor(color);
		int[] xPoints = new int[points.size()];
		int[] yPoints = new int[points.size()];

		for (int i = 0; i < points.size() - 1; i++)
		{
			xPoints[i] = (int) points.elementAt(i).x;
			yPoints[i] = (int) points.elementAt(i).y;
			g.fillArc(xPoints[i] - 3, yPoints[i] - 3, 6, 6, 0, 360);
		}
	}

	public void drawLine(Graphics g, Color color, Vector<Point> points)
	{
		g.setColor(color);
		int[] xPoints = new int[points.size()];
		int[] yPoints = new int[points.size()];

		for (int i = 0; i < points.size(); i++)
		{
			xPoints[i] = (int) points.elementAt(i).x;
			yPoints[i] = (int) points.elementAt(i).y;
		}
		g.drawPolyline(xPoints, yPoints, points.size());
	}

	/**
	 * ��ť����Ӧ�¼������ܴ���ʲô�¼�������������
	 * ����B�������ߡ�������ʱ������֮ǰ����¼��õ��ĵ���������д������飬������BLine�е�drawBLine��������B��������
	 * ���ػ�������������pointNumber����
	 */
	public void actionPerformed(ActionEvent e)
	{
		Graphics g = paintPanel.getGraphics();
		String arg = e.getActionCommand();
		g.clearRect(0, 0, 800, 500);
		if (arg.equals("��B��������"))
		{
			drawRectAndLine(g);
		} else if (arg.equals("�ػ�"))
		{
			pointNumber = 0;
			points.clear();
			Canvas.getInstance().calligraphy = new Calligraphy();
		}
	}

	/**
	 * ����϶��¼��� �ж����������ĸ��㣬һ���ж��������򽫸õ��������������λ�øı䣬��ˢ���ػ�
	 */
	public void mouseDragged(MouseEvent e)
	{ // MouseMotionListener

		Graphics graphics = paintPanel.getGraphics();
		graphics.clearRect(0, 0, 800, 500);
		graphics.setColor(Color.red);
		graphics.fillArc(e.getX() - 3, e.getY() - 3, 6, 6, 0, 360);

		pointNumber++;
		if (pointNumber % 5 == 1)
		{
			Point point = new Point(e.getX(), e.getY());
			points.addElement(point);
			Canvas.getInstance().calligraphy.addPoint(point);
		}
		drawRectAndLine(graphics);

		Canvas.getInstance().draw(graphics);
	}

	/**
	 * ����ڻ����ϵ�������һ����ɫԲ�㣬�����������洢��һ����ʱ�����У�ͬʱ�������pointNumber+1
	 */
	public void mouseClicked(MouseEvent e)
	{ // MouseListener
	}

	public void mouseMoved(MouseEvent e)
	{// MouseMotionListener
		// System.out.println("mouseMoved");
	}

	public void mouseEntered(MouseEvent e)
	{ // MouseListener
		// System.out.println("mouseEntered");
	}

	public void mouseExited(MouseEvent e)
	{ // MouseListener
		// System.out.println("mouseExited");
	}

	public void mousePressed(MouseEvent e)
	{ // MouseListener
		// System.out.println("mousePressed");
		Canvas.getInstance().calligraphy.startStroke();
	}

	public void mouseReleased(MouseEvent e)
	{ // MouseListener
		// System.out.println("mouseReleased");
		Canvas.getInstance().calligraphy.endStroke();
	}

}
