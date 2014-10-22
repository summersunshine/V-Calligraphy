package Curve;

import java.io.Console;
import java.util.Vector;

public class BezierCurve
{
	public static int threshold = 50;

	public Vector<Point> originPoints;

	public void AddOriginPoints(Point point)
	{
		if (originPoints.lastElement() != point)
			originPoints.add(point);
	}

	public static Vector<Curve> createCurve(Vector<Point> originPoints)
	{
		// 第一步获取定点的中点
		Point[] midPoints = getMidPoints(originPoints);

		// 又中点和原始点得到辅助控制点
		Point[] extrapoints = getExtraPoint(originPoints, midPoints);

		// 由辅助控制点得到曲线路径点
		return getCurvePoints(originPoints, extrapoints);

	}

	/**
	 * 获取额外的控制点
	 * */
	public static Point[] getExtraPoint(Vector<Point> originPoints, Point[] midPoints)
	{
		// 控制点收缩系数 ，经调试0.6较好，Point是opencv的，可自行定义结构体(x,y)
		float scale = 0.6f;

		int originCount = originPoints.size();
		Point[] extrapoints = new Point[2 * originCount];
		for (int i = 0; i < originCount; i++)
		{
			int nexti = i + 1;

			// 两个中点的中点
			Point midinmid = Point.getMidPoint(midPoints[i], midPoints[nexti]);
			Point offsetPoint = originPoints.get(i).sub(midinmid);

			// 第一个辅助点
			int extraindex = 2 * i;
			extrapoints[extraindex] = midPoints[i].add(offsetPoint);

			// 朝 originPoints.get(i)方向收缩
			Point add = extrapoints[extraindex].sub(originPoints.get(i)).mul(scale);
			extrapoints[extraindex] = originPoints.get(i).add(add);

			// 第二个辅助点
			int extranexti = extraindex + 1;
			extrapoints[extranexti] = midPoints[nexti].add(offsetPoint);

			// 朝 originPoints.get(i)方向收缩
			add = extrapoints[extranexti].sub(originPoints.get(i)).mul(scale);
			extrapoints[extranexti] = originPoints.get(i).add(add);

		}
		return extrapoints;
	}

	/**
	 * 由原始点与辅助控制点生成曲线的点
	 **/
	public static Vector<Curve> getCurvePoints(Vector<Point> originPoints, Point[] extrapoints)
	{
		Point controlPoint[] = new Point[4];

		Vector<Curve> curves = new Vector<Curve>();

		float beginSize = 60;
		float endSize = 60;

		// 生成4控制点，产生贝塞尔曲线
		for (int i = 0; i < originPoints.size() - 1; i++)
		{
			controlPoint[0] = originPoints.get(i);
			controlPoint[1] = extrapoints[2 * i + 1];
			controlPoint[2] = extrapoints[2 * i + 2];
			controlPoint[3] = originPoints.get(i + 1);

			Vector<Point> curvePoints = new Vector<Point>();
			Vector<Float> angles = new Vector<Float>();
			float length = adjoinDistance(originPoints, i);
			boolean isConcentrated = length < threshold;
			float interval;

			beginSize = endSize;

			if (isConcentrated)
			{
				interval = 0.01f;
				endSize = 60;
			} else
			{
				interval = 0.01f + (threshold - length) / 50000f;

				if (interval < 0.005f)
				{
					interval = 0.005f;
				}

				endSize = 10 + 2500 / length;

				if (length * interval > 60)
				{
					interval = endSize / length;
				}

			}

			for (float j = 1; j >= 0; j -= interval)
			{
				float px = bezier3funcX(j, controlPoint);
				float py = bezier3funcY(j, controlPoint);
				Point tempP = new Point(px, py);

				// if(curvePoints.size()>0)
				// {
				// angles.add(getAngle(tempP, curvePoints.lastElement()));
				// }
				// 存入曲线点
				curvePoints.addElement(tempP);

			}
			// angles.add(angles.lastElement());

			curves.add(new Curve(curvePoints, getAngle(curvePoints.firstElement(), curvePoints.lastElement()), isConcentrated, beginSize, endSize));

		}
		return curves;
	}

	public static float getAngle(Point point1, Point point2)
	{
		Point offsetPoint = point1.sub(point2);

		return (float) Math.atan2(-offsetPoint.y, offsetPoint.x);
	}

	public static float adjoinDistance(Vector<Point> originPoints, int index)
	{
		return originPoints.get(index + 1).sub(originPoints.get(index)).length();

	}

	/**
	 * @param originPoints
	 *            原始点集合
	 * @param interval
	 *            选取点的间隔
	 * @param isSvaeLastPoint
	 *            是否保留最后一个点
	 */
	public static Vector<Point> getIntervalPoints(Vector<Point> originPoints, int interval, boolean isSaveLastPoint)
	{
		Vector<Point> points = new Vector<Point>();
		int i;
		for (i = 0; i < originPoints.size(); i += interval)
		{
			points.add(originPoints.get(i));
		}
		if ((i != originPoints.size() - 1) && isSaveLastPoint)
		{
			points.add(originPoints.lastElement());
		}
		return points;
	}

	/**
	 * 当前和后面一个的中点
	 * */
	public static Point[] getMidPoints(Vector<Point> originPoints)
	{
		Point[] midPoints = new Point[originPoints.size() + 1];
		Point firstPoint = originPoints.firstElement();
		Point secondPoint = originPoints.get(1);

		Point lastPoint = originPoints.lastElement();
		Point lastSecondPoint = originPoints.get(originPoints.size() - 2);

		Point newFirstPoint = Point.getSymmetryPoint(firstPoint, secondPoint);
		Point newLastPoint = Point.getSymmetryPoint(lastPoint, lastSecondPoint);

		originPoints.add(0, newFirstPoint);
		originPoints.add(newLastPoint);

		// 生成中点
		for (int i = 0; i < originPoints.size() - 1; i++)
		{
			midPoints[i] = Point.getMidPoint(originPoints.get(i), originPoints.get(i + 1));
		}

		originPoints.remove(0);
		originPoints.remove(originPoints.size() - 1);
		return midPoints;
	}

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
