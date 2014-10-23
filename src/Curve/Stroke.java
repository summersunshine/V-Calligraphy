package curve;

import java.util.Vector;

import util.BezierCurve;

public class Stroke
{
	public static int threshold = 30;

	// ���Ƶ�����ϵ�� ��������0.6�Ϻã�Point��opencv�ģ������ж���ṹ��(x,y)
	public static float scale = 0.6f;

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

	/**
	 * ��������
	 * */
	public void updateCurve()
	{
		if (originPointCount == 1)
		{
			createCurveWithSingePoint();
		}
		else
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

		createMidPoints();

		removeTempPoints();
	}

	/**
	 * ��ͷ����ȡһ���µ���ʱ��
	 * */
	public Point getTempFirstPoint()
	{
		Point firstPoint = originPoints.firstElement();
		Point secondPoint = originPoints.get(1);
		Point tempFirstPoint = Point.getSymmetryPoint(firstPoint, secondPoint);

		return tempFirstPoint;
	}

	
	/**
	 * ��β����ȡһ���µ���ʱ��
	 * */
	public Point getTempLastPoint()
	{
		Point lastPoint = originPoints.lastElement();
		Point lastSecondPoint = originPoints.get(originPoints.size() - 2);
		Point tempFirstPoint = Point.getSymmetryPoint(lastPoint, lastSecondPoint);

		return tempFirstPoint;
	}

	
	/**
	 * �����е�
	 * */
	public void createMidPoints()
	{
		middlePoints.clear();

		// �����е�
		for (int i = 0; i < originPoints.size() - 1; i++)
		{
			Point currPoint = originPoints.get(i);
			Point nextPoint = originPoints.get(i+1);
			Point midPoint = Point.getMidPoint(currPoint, nextPoint);
			middlePoints.add(midPoint);
		}
	}
	
	
	/**
	 * ������ʱ��
	 * */
	public void addTempPoints()
	{
		originPoints.add(0, getTempFirstPoint());
		originPoints.add(getTempLastPoint());
	}
	
	
	/**
	 * ɾ����ʱ��
	 * */
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
	 *������β�ĵ�
	 **/
	public void  addTailCurvePoints()
	{
		if(originPointCount>2)
		{
			
			
			
			Point beginPoint = originPoints.get(originPointCount-2);
			Point endPoint = originPoints.get(originPointCount-1);
			
			
			if (beginPoint.sub(endPoint).length()>250)
			{
				curves.remove(curves.lastElement());
				addCurve2(beginPoint, endPoint);
			}
			
//			if (getAdjoinDistance(originPoints, originPointCount-2)>100)
//			{
//				Vector<Point> curvePoints = new Vector<Point>();
//				Point lastSecondPoint = originPoints.lastElement();
//				Point lastPoint = getTempLastPoint();
//				originPoints.add(lastPoint);
//				
//				// ����4���Ƶ㣬��������������
//				for (float i = 0; i < 1; i+=0.1)
//				{
//					Point point = Point.getPointBetweenTweenPoint(lastSecondPoint, lastPoint, i);
//
//					curvePoints.add(point);
//
//					curves.add(new Curve(curvePoints, false, endSize, 10));
//
//				}
//			}
		}
	}
	
	
	/**
	 * ��ԭʼ���븨�����Ƶ��������ߵĵ�
	 **/
	public void updateCurvePoints()
	{

		if (originPointCount==2)
		{
			createCurveWithTwoPoints();
			return;
		}
		
		int begin,end;
		begin = originPointCount-3;
		end = originPointCount-1;
		curves.remove(curves.size()-1);
		
		Point beginPoint = originPoints.get(originPointCount-2);
		Point endPoint = originPoints.get(originPointCount-1);
		
		
		if (beginPoint.sub(endPoint).length()>250)
		{
			addCurve(originPoints.get(begin), beginPoint);
			addCurve(beginPoint, endPoint);
			
			return;
		}
		else
		{
			// ����4���Ƶ㣬��������������
			for (int i = begin; i < end; i++)
			{
				updateCurveInfo(i);
				
				
				controlPoint = getControlPoints(i);

				Vector<Point> curvePoints = new Vector<Point>();
				for (float j = 1; j >= 0; j -= interval)
				{
					curvePoints.addElement(BezierCurve.bezier3func(j, controlPoint));
				}
				curves.add(new Curve(curvePoints, isConcentrated, beginSize, endSize));

			}
		}



	}
	
	
	public void addCurve(Point beginPoint,Point endPoint)
	{
		Vector<Curve> sthCurves = SomethingElse.getCurves(beginPoint, endPoint,endSize);
		for (int i = 0; i < sthCurves.size(); i++)
		{
			curves.add(sthCurves.get(i));
		}
		endSize = 15;
	}
	
	public void addCurve2(Point beginPoint,Point endPoint)
	{
		Vector<Curve> sthCurves = SomethingElse.getCurves(beginPoint, endPoint,beginSize,beginSize*0.8f);
		for (int i = 0; i < sthCurves.size(); i++)
		{
			curves.add(sthCurves.get(i));
		}
		endSize = 15;
	}
	
	/**
	 * ����һ�����curve
	 **/
	public void createCurveWithSingePoint()
	{

		//curves.clear();
		curves.add(new Curve(originPoints, beginSize, endSize));

	}

	/**
	 * �����������curve
	 **/
	public void createCurveWithTwoPoints()
	{
		curves.clear();
		Point beginPoint = originPoints.get(0);
		Point endPoint = originPoints.get(1);
		
		if (beginPoint.sub(endPoint).length()>250)
		{
			addCurve(beginPoint, endPoint);
		}
		else
		{
			// ����4���Ƶ㣬��������������
			for (int i = 0; i < 1; i++)
			{
				updateCurveInfo(i);
				controlPoint = getControlPoints(i);

				Vector<Point> curvePoints = new Vector<Point>();
				for (float j = 1; j >= 0; j -= interval)
				{
					curvePoints.addElement(BezierCurve.bezier3func(j, controlPoint));
				}
				curves.add(new Curve(curvePoints, isConcentrated, beginSize, endSize));
			}
		}


	}
	
	
	/**
	 * ���¹���curve��Ҫ����Ϣ
	 * */
	public void updateCurveInfo(int index)
	{
		updateDistance(index);
		updateBeginSize();
		updateEndSize();
		updateInterval();
	}
	
	
	/**
	 * ���µ�ǰԭʼ�㵽��һԭʼ��ľ���
	 * */
	public void updateDistance(int index)
	{
		distance = getAdjoinDistance(originPoints, index);
	}

	/**
	 * ����beginSize
	 * */
	public void updateBeginSize()
	{
		if (curves.isEmpty())
		{
			beginSize = endSize;
		}
		else
		{
			beginSize = curves.lastElement().endSize;
		}
	}
	
	
	/**
	 * ����endSize
	 * */
	public void updateEndSize()
	{
		
		if(distance > 500)
		{
			endSize =  15;
		}
		else
		{
			endSize =  15 + (500-distance)/10;
		}
		
		endSize = endSize>60?60:endSize;
		
	}
	
	
	/**
	 * ���²����ļ��
	 * */
	public void updateInterval()
	{
		//interval = (beginSize+endSize)/distance/50;
		//System.out.println(interval);
		//float imgSize = distance*interval;
		float interval = 0.05f + (threshold - distance) / 50000f;

		System.out.println("distance: " + distance + "interval :" + interval);
		if (interval < 0.025f)
		{
			interval = 0.025f;
		}

	}


	/**
	 * ��ȡ���Ƶ�
	 **/
	public Point[] getControlPoints(int index)
	{
		Point controlPoint[] = new Point[4];
		controlPoint[0] = originPoints.get(index);
		controlPoint[1] = extraPoints.get(2 * index + 1);
		controlPoint[2] = extraPoints.get(2 * index + 2);
		controlPoint[3] = originPoints.get(index + 1);
		return controlPoint;
	}



	/**
	 * ��ȡ���ڵĵĵ�֮��ľ���
	 * */
	public float getAdjoinDistance(Vector<Point> points, int index)
	{
		if(index >= points.size()-1) return 0;
		
		return points.get(index + 1).sub(points.get(index)).length();
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
