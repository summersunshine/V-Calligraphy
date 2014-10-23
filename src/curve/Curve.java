package curve;

import java.util.Vector;

public class Curve
{

	public boolean isConcentrated;

	public float beginSize;
	public float endSize;

	public Vector<Point> points;
	public float angle;

	public Curve(Vector<Point> points, float angle, boolean isConcentrated, float beginSize, float endSize)
	{
		this.points = points;
		this.angle = angle;
		this.isConcentrated = isConcentrated;
		this.beginSize = beginSize;
		this.endSize = endSize;
	}

	public Curve(Vector<Point> points, boolean isConcentrated, float beginSize, float endSize)
	{
		this.points = points;
		this.isConcentrated = isConcentrated;
		this.beginSize = beginSize;
		this.endSize = endSize;
	}
	
	public Curve(Vector<Point> points, float beginSize, float endSize)
	{
		this.points = points;
		this.beginSize = beginSize;
		this.endSize = endSize;
	}
}
