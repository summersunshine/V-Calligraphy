package util;

import curve.Point;

public class Geometry
{
	public static float getAngle(Point point1, Point point2)
	{
		Point offsetPoint = point1.sub(point2);

		return (float) Math.atan2(-offsetPoint.y, offsetPoint.x) + 180;
	}
}
