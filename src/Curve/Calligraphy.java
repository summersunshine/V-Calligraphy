package Curve;

import java.awt.Graphics;
import java.awt.Image;
import java.util.Vector;

public class Calligraphy
{
	public Vector<Stroke> strokes;
	public Stroke currStroke;

	public Calligraphy()
	{
		strokes = new Vector<Stroke>();
	}

	public void startStroke()
	{
		currStroke = new Stroke();
	}

	public void addPoint(Point point)
	{
		currStroke.addPointsAndUpdateCarve(point);
	}

	public void endStroke()
	{
		strokes.add(currStroke);
	}

}
