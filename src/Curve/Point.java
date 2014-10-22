package Curve;

public class Point
{
	public float x;
	public float y;

	public Point(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public Point(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public Point()
	{
		this.x = 0;
		this.y = 0;
	}

	public float length()
	{
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * ´¹Ö±
	 */
	public Point perpendicularPoint()
	{
		return new Point(-y, x);
	}

	public Point normalizePoint()
	{
		if (length() == 0)
		{
			return new Point();
		} else
		{
			return div(length());
		}
	}

	Point add(Point point)
	{
		return new Point(this.x + point.x, this.y + point.y);
	}

	Point sub(Point point)
	{
		return new Point(this.x - point.x, this.y - point.y);
	}

	Point mul(float m)
	{
		return new Point(m * this.x, m * this.y);
	}

	Point div(float m)
	{
		return new Point(this.x / m, this.y / m);
	}

	public static Point getMidPoint(Point point1, Point point2)
	{
		return new Point((point1.x + point2.x) / 2.0f, (point1.y + point2.y) / 2.0f);
	}

	public static Point getPointBetweenTweenPoint(Point point1, Point point2, float percentage1)
	{
		float percentage2 = 1 - percentage1;
		float x = point1.x * percentage1 + point2.x * percentage2;
		float y = point1.y * percentage1 + point2.y * percentage2;
		return new Point(x, y);
	}

	public static Point getSymmetryPoint(Point midPoint, Point point)
	{
		return midPoint.mul(2).sub(point);
	}
}
