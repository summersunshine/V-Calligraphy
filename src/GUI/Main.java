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
 * 实验七 B样条曲线，主要加入鼠标操作，特征点的拖动
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
		JButton btn = new JButton("画B样条曲线");
		btn.setBounds(200, 10, 150, 30);
		btn.addActionListener(this);
		this.add(btn);
		JButton reset = new JButton("重画");
		reset.setBounds(370, 10, 70, 30);
		reset.addActionListener(this);
		this.add(reset);
	}

	public static void main(String args[])
	{
		Main mainFrame = new Main();
		mainFrame.setSize(800, 550);
		mainFrame.setTitle("B样条曲线");
		mainFrame.setVisible(true);
	}

	/**
	 * 画B样条曲线。还要画出之前鼠标事件的各点
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
	 * 按钮的相应事件（不管触发什么事件，都先清屏）
	 * “画B样条曲线”：将临时数组中之前鼠标事件得到的点的坐标序列存入数组，并调用BLine中的drawBLine方法，画B样条曲线
	 * “重画”：将计数器pointNumber清零
	 */
	public void actionPerformed(ActionEvent e)
	{
		Graphics g = paintPanel.getGraphics();
		String arg = e.getActionCommand();
		g.clearRect(0, 0, 800, 500);
		if (arg.equals("画B样条曲线"))
		{
			drawRectAndLine(g);
		} else if (arg.equals("重画"))
		{
			pointNumber = 0;
			points.clear();
			Canvas.getInstance().calligraphy = new Calligraphy();
		}
	}

	/**
	 * 鼠标拖动事件。 判断鼠标点中了哪个点，一旦判定下来，则将该点的坐标随着鼠标的位置改变，并刷新重画
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
	 * 鼠标在画布上单击，画一个红色圆点，并将点的坐标存储于一个临时数组中，同时点的数量pointNumber+1
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
