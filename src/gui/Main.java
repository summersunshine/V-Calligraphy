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
 * 实验七 B样条曲线，主要加入鼠标操作，特征点的拖动
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
	 * 初始化绘制面板*
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
	 * 初始化面板按钮*/
	public void initButton()
	{
		JButton btn = new JButton("撤销");
		btn.setBounds(200, 10, 70, 30);
		btn.addActionListener(this);
		this.add(btn);
		
		JButton reset = new JButton("重画");
		reset.setBounds(370, 10, 70, 30);
		reset.addActionListener(this);
		this.add(reset);
		
		JToggleButton isModifyBeginButton = new JToggleButton("是否优化起笔处");
		isModifyBeginButton.setBounds(540, 10, 170, 30);
		isModifyBeginButton.addActionListener(this);
		this.add(isModifyBeginButton);
		
		JToggleButton isModifyEndButton = new JToggleButton("是否优化收笔处");
		isModifyEndButton.setBounds(710, 10, 170, 30);
		isModifyEndButton.addActionListener(this);
		this.add(isModifyEndButton);
	}
	
	/**
	 * 按钮的相应事件（不管触发什么事件，都先清屏）
	 * “画B样条曲线”：将临时数组中之前鼠标事件得到的点的坐标序列存入数组，并调用BLine中的drawBLine方法，画B样条曲线
	 * “重画”：将计数器pointNumber清零
	 */
	public void actionPerformed(ActionEvent e)
	{
		String arg = e.getActionCommand();
		graphics = paintPanel.getGraphics();
		//graphics.clearRect(0, 0, 1280, 720);
		
		if (arg.equals("撤销"))
		{
			cancelAction();
		} 
		else if (arg.equals("重画"))
		{
			redrawAction();
		}
		else if (arg.equals("是否优化起笔处"))
		{
			Global.isModifyBegin = !Global.isModifyBegin;
		}
		else if (arg.equals("是否优化收笔处"))
		{
			Global.isModifyEnd = !Global.isModifyEnd;
		}
	}
	
	
	/**
	 * 初始化轨迹线
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
		mainFrame.setTitle("虚拟毛笔");
		mainFrame.setVisible(true);
	}

	/**
	 * 画B样条曲线。还要画出之前鼠标事件的各点
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
	 * 在指定的位置绘制矩形
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
	 *将指定的点用线连起来
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
	 * 撤销最后一个笔划
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
	 * 撤销所有笔划*/
	public void redrawAction()
	{
		graphics.clearRect(0, 0, 1280, 720);
		
		pointNumber = 0;
		polygonLines.clear();
		Canvas.getInstance().calligraphy = new Calligraphy();
	}
	
	
	

	/**
	 * 鼠标拖动事件。 判断鼠标点中了哪个点，一旦判定下来，则将该点的坐标随着鼠标的位置改变，并刷新重画
	 */
	public void mouseDragged(MouseEvent e)
	{ // MouseMotionListener
		addNewPointAndDraw(new Point(e.getX(),e.getY()),false);
	}
	
	
	/**
	 *开始一个新的笔划
	 **/
	public void mousePressed(MouseEvent e)
	{ // MouseListener
		
		System.out.println("start stroke");
		Canvas.getInstance().calligraphy.startStroke();
	
		//addNewPointAndDraw(new Point(e.getX(),e.getY()),false);
		
	}
	
	/**
	 * 加入一个新的点并重新绘制
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
	 *结束一个笔划
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
	 * 鼠标在画布上单击，画一个红色圆点，并将点的坐标存储于一个临时数组中，同时点的数量pointNumber+1
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
