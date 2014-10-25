package gui;
import java.awt.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import constants.Global;
import util.BezierCurve;
import util.RotateImage;
import curve.Calligraphy;
import curve.Canvas;
import curve.Curve;
import curve.Point;
import curve.PolygonLine;
import curve.FirstStroke;

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
	private Graphics graphics;
	
	private int pointNumber;
	private Vector<PolygonLine> polygonLines;
	private PolygonLine polygonLine;
	
	Vector<Point> points;
	public Main()
	{
		this.setLayout(null);
		initPanel();
		initButton();
		initPolygonLine();
		this.setBackground(Color.white);
	}

	/**
	 * ��ʼ���������*
	 */
	public void initPanel()
	{
		paintPanel = new JPanel();
		paintPanel.setBackground(Color.WHITE);
		paintPanel.setBounds(0, 50, 1280, 720);
		paintPanel.addMouseListener(this);
		paintPanel.addMouseMotionListener(this);
		
		this.add(paintPanel);
	}

	/**
	 * ��ʼ����尴ť*/
	public void initButton()
	{
		JButton btn = new JButton("����");
		btn.setBounds(200, 10, 70, 30);
		btn.addActionListener(this);
		this.add(btn);
		
		JButton reset = new JButton("�ػ�");
		reset.setBounds(370, 10, 70, 30);
		reset.addActionListener(this);
		this.add(reset);
		
		JToggleButton isModifyBeginButton = new JToggleButton("�Ƿ��Ż���ʴ�");
		isModifyBeginButton.setBounds(540, 10, 170, 30);
		isModifyBeginButton.addActionListener(this);
		this.add(isModifyBeginButton);
		
		JToggleButton isModifyEndButton = new JToggleButton("�Ƿ��Ż��ձʴ�");
		isModifyEndButton.setBounds(710, 10, 170, 30);
		isModifyEndButton.addActionListener(this);
		this.add(isModifyEndButton);
	}
	
	/**
	 * ��ť����Ӧ�¼������ܴ���ʲô�¼�������������
	 * ����B�������ߡ�������ʱ������֮ǰ����¼��õ��ĵ���������д������飬������BLine�е�drawBLine��������B��������
	 * ���ػ�������������pointNumber����
	 */
	public void actionPerformed(ActionEvent e)
	{
		String arg = e.getActionCommand();
		graphics = paintPanel.getGraphics();
		//graphics.clearRect(0, 0, 1280, 720);
		
		if (arg.equals("����"))
		{
			cancelAction();
		} 
		else if (arg.equals("�ػ�"))
		{
			redrawAction();
		}
		else if (arg.equals("�Ƿ��Ż���ʴ�"))
		{
			Global.isModifyBegin = !Global.isModifyBegin;
		}
		else if (arg.equals("�Ƿ��Ż��ձʴ�"))
		{
			Global.isModifyEnd = !Global.isModifyEnd;
		}
	}
	
	
	/**
	 * ��ʼ���켣��
	 */
	public void initPolygonLine()
	{
		points = new Vector<Point>();
		pointNumber = 0;
		polygonLines = new Vector<PolygonLine>();
		polygonLine = new PolygonLine();
	} 
	

	public static void main(String args[])
	{
		Main mainFrame = new Main();
		mainFrame.setSize(1280, 800);
		mainFrame.setTitle("����ë��");
		mainFrame.setVisible(true);
	}

	/**
	 * ��B�������ߡ���Ҫ����֮ǰ����¼��ĸ���
	 * 
	 * @param g
	 */
	public void drawRectAndLine(Graphics g)
	{

		for (int i = 0; i < polygonLines.size(); i++)
		{
			drawRect(g, Color.red, polygonLines.get(i).points);
			drawLine(g, Color.red, polygonLines.get(i).points);
		}
		
		drawRect(g, Color.red, polygonLine.points);
		drawLine(g, Color.red, polygonLine.points);
	}

	/**
	 * ��ָ����λ�û��ƾ���
	 * */
	public void drawRect(Graphics g, Color color, Vector<Point> points)
	{
		g.setColor(color);
		int[] xPoints = new int[points.size()];
		int[] yPoints = new int[points.size()];

		for (int i = 0; i < points.size() - 1; i++)
		{
			xPoints[i] = (int) points.elementAt(i).x;
			yPoints[i] = (int) points.elementAt(i).y;
			g.fillArc(xPoints[i] - 5, yPoints[i] - 5, 10, 10, 0, 360);
		}
	}

	/**
	 *��ָ���ĵ�����������
	 **/
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
	 * �������һ���ʻ�
	 **/
	public void cancelAction()
	{
		if(polygonLines.isEmpty()) return;
		graphics.clearRect(0, 0, 1280, 720);
		polygonLines.remove(polygonLines.size()-1);
		Canvas.getInstance().calligraphy.strokes.remove(polygonLines.size());
		
		Canvas.getInstance().draw(graphics);
		drawRectAndLine(graphics);
	}
	
	/**
	 * �������бʻ�*/
	public void redrawAction()
	{
		graphics.clearRect(0, 0, 1280, 720);
		
		pointNumber = 0;
		polygonLines.clear();
		Canvas.getInstance().calligraphy = new Calligraphy();
	}
	
	
	

	/**
	 * ����϶��¼��� �ж����������ĸ��㣬һ���ж��������򽫸õ��������������λ�øı䣬��ˢ���ػ�
	 */
	public void mouseDragged(MouseEvent e)
	{ // MouseMotionListener
		addNewPointAndDraw(new Point(e.getX(),e.getY()),false);
	}
	
	
	/**
	 *��ʼһ���µıʻ�
	 **/
	public void mousePressed(MouseEvent e)
	{ // MouseListener
		
		System.out.println("start stroke");
		Canvas.getInstance().calligraphy.startStroke();
	
		//addNewPointAndDraw(new Point(e.getX(),e.getY()),false);
		
	}
	
	/**
	 * ����һ���µĵ㲢���»���
	 * */
	public void addNewPointAndDraw(Point point,boolean isForce)
	{
		System.out.println("Point: "+ point.x + " " + point.y);
		
		graphics = paintPanel.getGraphics();
		if (++pointNumber % 5 == 1)
		{
			polygonLine.points.addElement(point);
			Canvas.getInstance().calligraphy.addPoint(point);
		}

		drawRectAndLine(graphics);
		graphics.clearRect(0, 0, 1280, 720);
		Canvas.getInstance().draw(graphics);

	}
	

	/**
	 *����һ���ʻ�
	 **/
	public void mouseReleased(MouseEvent e)
	{ // MouseListener
		
		addNewPointAndDraw(new Point(e.getX(),e.getY()),true);
		
		Canvas.getInstance().calligraphy.endStroke();
		polygonLines.add(polygonLine);
		polygonLine =  new PolygonLine();
		System.out.println("end stroke " + polygonLines.size());
		
		drawRectAndLine(graphics);
		//graphics.clearRect(0, 0, 1280, 720);
		//Canvas.getInstance().draw(graphics);
	}
	
	

	/**
	 * ����ڻ����ϵ�������һ����ɫԲ�㣬�����������洢��һ����ʱ�����У�ͬʱ�������pointNumber+1
	 */
	public void mouseClicked(MouseEvent e)
	{ // MouseListener

		drawRectAndLine(graphics);
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



}
