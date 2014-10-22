package Curve;

import java.util.Vector;

public class Stroke
{
	public static int threshold = 50;

	// ���Ƶ�����ϵ�� ��������0.6�Ϻã�Point��opencv�ģ������ж���ṹ��(x,y)
	public static float scale = 0.6f;

	//
	public Vector<Curve> curves;

	public Vector<Point> originPoints;
	public Vector<Point> middlePoints;
	public Vector<Point> extraPoints;
	public int originPointCount;

	private Point controlPoint[];
	private float beginSize = 60;
	private float endSize = 60;
	private float interval = 0.01f;
	private float distance;
	private boolean isConcentrated;

	public Stroke()
	{
		curves = new Vector<Curve>();
		originPoints = new Vector<Point>();
		middlePoints = new Vector<Point>();
		extraPoints = new Vector<Point>();
	}

	/**
	 * ����㲢��������
	 * */
	public void addPointsAndUpdateCarve(Point point)
	{
		addPoints(point);
		updateCurve();
	}

	/**
	 * ������뵽ԭʼ�㼯���� �����ͼ��������һ������� �Ͳ���ӽ�ȥ
	 * */
	public void addPoints(Point point)
	{
		if (originPoints.isEmpty())
		{
			originPoints.add(point);
			originPointCount = originPoints.size();
			return;
		}

		if (originPoints.lastElement() != point)
		{
			originPoints.add(point);
			originPointCount = originPoints.size();
		}

	}

	public void updateCurve()
	{
		if (originPointCount > 1)
		{
			updateMidPoints();
			updateExtraPoints();
			updateCurvePoints();
		}

	}

	/**
	 * ��ǰ�ͺ���һ�����е�
	 * */
	public void updateMidPoints()
	{
		assert (originPoints.size() > 1);

		addTempPoints();

		middlePoints.clear();

		// �����е�
		for (int i = 0; i < originPoints.size() - 1; i++)
		{
			Point midPoint = Point.getMidPoint(originPoints.get(i), originPoints.get(i + 1));
			middlePoints.add(midPoint);
		}

		removeTempPoints();
	}

	public Point getTempFirstPoint()
	{
		Point firstPoint = originPoints.firstElement();
		Point secondPoint = originPoints.get(1);
		Point tempFirstPoint = Point.getSymmetryPoint(firstPoint, secondPoint);

		return tempFirstPoint;
	}

	public Point getTempLastPoint()
	{
		Point lastPoint = originPoints.lastElement();
		Point lastSecondPoint = originPoints.get(originPoints.size() - 2);
		Point tempFirstPoint = Point.getSymmetryPoint(lastPoint, lastSecondPoint);

		return tempFirstPoint;
	}

	public void addTempPoints()
	{
		originPoints.add(0, getTempFirstPoint());
		originPoints.add(getTempLastPoint());
	}

	public void removeTempPoints()
	{
		originPoints.remove(0);
		originPoints.remove(originPoints.size() - 1);
	}

	/**
	 * ��ȡ����Ŀ��Ƶ�
	 * */
	public void updateExtraPoints()
	{
		extraPoints.clear();
		for (int i = 0; i < middlePoints.size() - 1; i++)
		{
			// ��ǰԭʼ��
			Point currOriginPoint = originPoints.get(i);

			// ��ǰ���е�
			Point currMiddlePoint = middlePoints.get(i);
			Point nextMiddlePoint = middlePoints.get(i + 1);

			// �����е���е�
			Point midInMidPoint = Point.getMidPoint(currMiddlePoint, nextMiddlePoint);
			Point offsetPoint = currOriginPoint.sub(midInMidPoint);

			// ��ǰ�Ŀ��Ƶ�
			Point currExtraPoint = currMiddlePoint.add(offsetPoint);
			Point nextExtraPoint = nextMiddlePoint.add(offsetPoint);

			currExtraPoint = Point.getPointBetweenTweenPoint(currExtraPoint, currOriginPoint, scale);
			nextExtraPoint = Point.getPointBetweenTweenPoint(nextExtraPoint, currOriginPoint, scale);

			extraPoints.add(currExtraPoint);
			extraPoints.add(nextExtraPoint);
		}
	}

	/**
	 * ��ԭʼ���븨�����Ƶ��������ߵĵ�
	 **/
	public void updateCurvePoints()
	{

		curves.clear();

		// ����4���Ƶ㣬��������������
		for (int i = 0; i < originPointCount - 1; i++)
		{
			controlPoint = getControlPoints(i);

			Vector<Point> curvePoints = new Vector<Point>();

			updateCurveInfo(i);

			for (float j = 1; j >= 0; j -= interval)
			{
				float px = BezierCurve.bezier3funcX(j, controlPoint);
				float py = BezierCurve.bezier3funcY(j, controlPoint);
				// �������ߵ�
				curvePoints.addElement(new Point(px, py));

			}

			curves.add(new Curve(curvePoints, isConcentrated, beginSize, endSize));

		}

	}

	public void updateCurveInfo(int index)
	{
		distance = adjoinDistance(originPoints, index);
		isConcentrated = distance < threshold;
		beginSize = endSize;
		endSize = getEndSize(distance);
		interval = getInterval(beginSize, endSize, distance);
	}

	public float getInterval(float beginSize, float endSize, float distance)
	{
		float interval = 0.05f + (threshold - distance) / 10000f;

		if (interval < 0.025f)
		{
			interval = 0.025f;
		}

		return interval;
	}

	public float getEndSize(float distance)
	{
		if (isConcentrated)
		{
			return 60;
		} else
		{
			return 20 + 2000 / distance;
		}
	}

	public Point[] getControlPoints(int index)
	{
		Point controlPoint[] = new Point[4];
		controlPoint[0] = originPoints.get(index);
		controlPoint[1] = extraPoints.get(2 * index + 1);
		controlPoint[2] = extraPoints.get(2 * index + 2);
		controlPoint[3] = originPoints.get(index + 1);
		return controlPoint;
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
	 *            ԭʼ�㼯��
	 * @param interval
	 *            ѡȡ��ļ��
	 * @param isSvaeLastPoint
	 *            �Ƿ������һ����
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

}
