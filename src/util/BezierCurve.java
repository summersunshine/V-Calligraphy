package util;

import java.util.Vector;

import curve.Point;

public class BezierCurve
{

	/**
	 * 判断point1和point2组成的线段与point3和point4组成的线段是否相交
	 * */
	public static boolean isIntersect(Point point1, Point point2, Point point3, Point point4)
	{

		boolean flag = false;
		double d = (point2.x - point1.x) * (point4.y - point3.y) - (point2.y - point1.y) * (point4.x - point3.x);
		if (d != 0)
		{
			double r = ((point1.y - point3.y) * (point4.x - point3.x) - (point1.x - point3.x) * (point4.y - point4.y)) / d;
			double s = ((point1.y - point3.y) * (point2.x - point1.x) - (point1.x - point3.x) * (point2.y - point1.y)) / d;
			if ((r >= 0) && (r <= 1) && (s >= 0) && (s <= 1))
			{
				flag = true;
			}
		}
		return flag;
	}

	
	/**
	 * 三次贝塞尔曲线
	 * */
	public static Point bezier3func(float uu, Point[] controlP)
	{
		float x = bezier3funcX(uu, controlP);
		float y = bezier3funcY(uu, controlP);
		return new Point(x,y);
	}
	
	
	/**
	 * 三次贝塞尔曲线
	 * */
	public static float bezier3funcX(float uu, Point[] controlP)
	{
		float part0 = controlP[0].x * uu * uu * uu;
		float part1 = 3 * controlP[1].x * uu * uu * (1 - uu);
		float part2 = 3 * controlP[2].x * uu * (1 - uu) * (1 - uu);
		float part3 = controlP[3].x * (1 - uu) * (1 - uu) * (1 - uu);
		return part0 + part1 + part2 + part3;
	}

	/**
	 * 三次贝塞尔曲线
	 * */
	public static float bezier3funcY(float uu, Point[] controlP)
	{
		float part0 = controlP[0].y * uu * uu * uu;
		float part1 = 3 * controlP[1].y * uu * uu * (1 - uu);
		float part2 = 3 * controlP[2].y * uu * (1 - uu) * (1 - uu);
		float part3 = controlP[3].y * (1 - uu) * (1 - uu) * (1 - uu);
		return part0 + part1 + part2 + part3;
	}
}
