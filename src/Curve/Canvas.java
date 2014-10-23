package curve;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;

import util.Geometry;
import util.RotateImage;

public class Canvas
{
	BufferedImage pointBrush;
	BufferedImage halfDryBrush;
	BufferedImage midDryBrush;
	public Calligraphy calligraphy;

	private static Canvas instance;

	public Canvas()
	{
		File pointBrushFile = new File("res/point.png");
		File halfDryBrushFile = new File("res/halfDry.png");
		File midDryBrushFile = new File("res/midDry.png");
		try
		{
			pointBrush = ImageIO.read(new FileInputStream(pointBrushFile));
			halfDryBrush = ImageIO.read(new FileInputStream(halfDryBrushFile));
			midDryBrush = ImageIO.read(new FileInputStream(midDryBrushFile));
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

	public void draw(Graphics graphics,Vector<Curve> curves)
	{
		
		for (int i = 0; i < curves.size(); i++)
		{
			drawBitmap(graphics, curves.get(i));
		}

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

		for (int i = 0; i < calligraphy.currStroke.curves.size(); i++)
		{
			drawBitmap(graphics, calligraphy.currStroke.curves.get(i));
		}
	}
	
	public void drawLast(Graphics graphics)
	{
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

		for (int i = 0; i < curve.points.size()-1; i++)
		{
			int size =  (int) ((curve.beginSize + i * interval)*0.8);
			if (size > 0)
			{	
				int sizeY = size;
				int sizeX = size;
				int x = (int) curve.points.elementAt(i).x - sizeX / 2;
				int y = (int) curve.points.elementAt(i).y - sizeY / 2;
				graphics.drawImage(pointBrush, x, y, sizeX, sizeY, null);
				
			} 
			else
			{
				
				int sizeY = (int) (size*1.08);
				int sizeX = (int) (size*1.08);
				int x = (int) curve.points.elementAt(i).x - sizeX / 2;
				int y = (int) curve.points.elementAt(i).y - sizeY / 2;
				
				
				Point currPoint = curve.points.get(i);
				Point nextPoint = curve.points.get(i+1);
				Point offsetPoint = nextPoint.sub(currPoint);
				
				float angle = Geometry.getAngle(currPoint, nextPoint);

				if (offsetPoint.length()>sizeX)
				{
					sizeX = (int) offsetPoint.length()*2;
					x = (int) curve.points.elementAt(i).x - sizeX / 2;
				}
				System.out.println("Angle " + angle);
				graphics.drawImage(RotateImage.Rotate(midDryBrush,(int) angle) , x, y, sizeX, sizeY, null);
			}


		}
	}
}
