package curve;

import java.util.Vector;

public class PolygonLine
{

	public Vector<Point> points;
	
	public PolygonLine(Vector<Point> points)
	{
		this.points = points;
	}
	
	public PolygonLine()
	{
		this.points = new Vector<Point>();
	}
}
