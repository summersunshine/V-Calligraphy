package Curve;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;

public class Canvas
{

	BufferedImage pointBrush;
	BufferedImage halfDryBrush;

	public Calligraphy calligraphy;

	private static Canvas instance;

	public Canvas()
	{
		File pointBrushFile = new File("C:/Users/xiaya_000/Desktop/point.png");
		File halfDryBrushFile = new File("C:/Users/xiaya_000/Desktop/point8.png");
		try
		{
			pointBrush = ImageIO.read(new FileInputStream(pointBrushFile));
			halfDryBrush = ImageIO.read(new FileInputStream(halfDryBrushFile));
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		calligraphy = new Calligraphy();
	}

	public static Canvas getInstance()
	{
		if (instance == null)
		{
			instance = new Canvas();
		}
		return instance;
	}

	public void draw(Graphics graphics)
	{
		for (int i = 0; i < calligraphy.strokes.size(); i++)
		{
			for (int j = 0; j < calligraphy.strokes.get(i).curves.size(); j++)
			{
				drawBitmap(graphics, calligraphy.strokes.get(i).curves.get(j));
			}
		}
		//
		//
		for (int i = 0; i < calligraphy.currStroke.curves.size(); i++)
		{
			drawBitmap(graphics, calligraphy.currStroke.curves.get(i));
		}
	}

	public void drawBitmap(Graphics graphics, Curve curve)
	{
		float interval;
		if (curve.points.size() > 1)
		{
			interval = (curve.endSize - curve.beginSize) / (curve.points.size() - 1);
		} else
		{
			interval = 0;
		}

		for (int i = 0; i < curve.points.size(); i++)
		{
			int size = (int) (curve.beginSize + i * interval);
			size = (int) (size * 0.8);
			int x = (int) curve.points.elementAt(i).x - size / 2;
			int y = (int) curve.points.elementAt(i).y - size / 2;

			System.out.println("size : " + size + " interval: " + interval);

			int threshold = 24;
			if (size > threshold)
			{
				graphics.drawImage(pointBrush, x, y, size, size, null);
			} else
			{
				graphics.drawImage(halfDryBrush, x, y, size, size, null);
			}

		}
	}
}
