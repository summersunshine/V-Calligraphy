package curve;

import java.util.Vector;

import org.omg.CORBA.PUBLIC_MEMBER;

import constants.Global;
import util.BezierCurve;

public class Stroke
{
	public static int threshold = 30;

	// ���Ƶ�����ϵ�� ��������0.6�Ϻã�Point��opencv�ģ������ж���ṹ��(x,y)
	public static float scale = 0.6f;

	public Vector<Curve> curves;
	public Vector<Curve> beginCurves;
	public Vector<Curve> modifyBeginCurves;
	public Vector<Curve> endCurves;
	public Vector<Curve> modifyEndCurves;
	
	public Vector<Point> originPoints;
	public Vector<Point> middlePoints;
	public Vector<Point> extraPoints;
	public int originPointCount;

	private Point controlPoint[];
	private float beginSize = 70;
	private float endSize = 60;
	private float interval = 0.01f;
	private float distance;
	private boolean isTail;
	private boolean isBeginModified;
	private boolean isEndModified;

	public Stroke()
	{
		isTail = false;
		isBeginModified = false;
		isEndModified = false;
		curves = new Vector<Curve>();
		beginCurves = new Vector<Curve>();
		endCurves = new Vector<Curve>();
		modifyBeginCurves = new Vector<Curve>();
		modifyEndCurves = new Vector<Curve>();
		originPoints = new Vector<Point>();
		middlePoints = new Vector<Point>();
		extraPoints = new Vector<Point>();
		controlPoint = new Point[4];

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

		if (originPoints.lastElement() != point || true)
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
		else if (originPointCount == 2)
		{
			createCurveWithTwoPoints();
		}
		else
		{
			createCurveWithMorePoints();
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
	 * ��ȡ���һ����
	 * */
	public Point getLastPoint()
	{
		if(originPoints.isEmpty()) return null;
		return originPoints.lastElement();
	}
	
	
	/**
	 * ��ȡ�����ڶ�����
	 * */
	public Point getLastSecondPoint()
	{
		if(originPoints.size()<2) return null;
		return originPoints.get(originPoints.size()-2);
	}
	
	
	/**
	 * ��ȡ���������֮��ľ���
	 * */
	public float getLastDistance()
	{
		Point beginPoint = getLastPoint();
		Point endPoint = getLastSecondPoint();
		return beginPoint.sub(endPoint).length();
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
	public void  modifyTailCurves()
	{
		if(originPointCount>2 && Global.isModifyEnd)
		{
			if (getLastDistance()>50)
			{
				endCurves.add(curves.lastElement());
				curves.remove(curves.lastElement());
				this.isTail = true;
				updateMidPoints();
				updateExtraPoints();
				samleForBezier( originPointCount-2,originPointCount-1);
			}
		}
	}
	
	

	/**
	 * ��һ��������curve��ʵ���Ͼ���һ���㣩
	 **/
	public void createCurveWithSingePoint()
	{
		Vector<Point> curvePoints = new Vector<Point>();
		for (int i = 0; i < 10; i++)
		{
			curvePoints.add(originPoints.firstElement());
		}
		curves.add(new Curve(curvePoints, beginSize, endSize));

	}

	/**
	 * ������������curve
	 **/
	public void createCurveWithTwoPoints()
	{
		curves.clear();
		Point beginPoint = originPoints.get(0);
		Point endPoint = originPoints.get(1);
		
		if (getLastDistance()>150 && Global.isModifyBegin)
		{
			isBeginModified = true;
			createMixCurves(beginPoint, endPoint);
			endSize = 25;
		}
		else if (getLastDistance()>100 && Global.isModifyBegin)
		{
			isBeginModified = true;
			createTransitionCurves(beginPoint, endPoint);
		}
		else
		{
			updateMidPoints();
			updateExtraPoints();
			samleForBezier(0,1);
		}
		
		beginCurves.addAll(curves);
	}
	
	
	/**
	 * ����������2��ʱ�����ɵ�curve
	 **/
	public void createCurveWithMorePoints()
	{
		curves.remove(curves.lastElement());
		
		updateMidPoints();
		updateExtraPoints();
		samleForBezier( originPointCount-3,originPointCount-1);

	}
	
	
	
	
	/**
	 * �������ϲ���
	 * @param beginIndx 
	 * @param endIndx 
	 * */
	public void samleForBezier(int beginIndx,int endIndx)
	{
		// ����4���Ƶ㣬��������������
		for (int i = beginIndx; i < endIndx; i++)
		{
			updateCurveInfo(i);
			updateControlPoints(i);
			createBezierCurves();

		}
	}
	
	
	/**
	 * �ڿ��Ƶ�����*��bezier�����Ͻ��в���
	 * */
	public void createBezierCurves()
	{
		Vector<Point> curvePoints = new Vector<Point>();
		for (float j = 1; j >= 0; j -= interval)
		{
			curvePoints.addElement(BezierCurve.bezier3func(j, controlPoint));
		}
		curves.add(new Curve(curvePoints, beginSize, endSize));
	}
	

	/**
	 * ���ɻ�����Σ�����һ�ι��ȶκ�һ���ȶ���
	 * */
	public void  createMixCurves(Point beginPoint,Point endPoint)
	{
		curves.addAll(FirstStroke.getCurves(beginPoint, endPoint,endSize));
		
	}
	
	/**
	 * ���ɹ��ȶ�
	 * */
	public void createTransitionCurves(Point beginPoint,Point endPoint)
	{
		curves.addAll(FirstStroke.getCurves(beginPoint, endPoint,endSize,endSize*0.5f));
		
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
			beginSize = Global.BigSize;
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
		
		if(distance > 250)
		{
			endSize =  20;
		}
		else
		{
			endSize =  20 + (250-distance)/5;
		}
		
		endSize = endSize>60?60:endSize;
		
		if (isTail)
		{
			float factor = (200-getLastDistance())/200f;
			factor = factor>0.1?factor:0.1f;
			endSize*=factor;
		}
		
	}
	
	
	/**
	 * ���²����ļ��
	 * */
	public void updateInterval()
	{
		//interval = 0.025f;

		interval = 0.04f + (threshold - distance) /10000f;

		System.out.println("distance: " + distance + "interval :" + interval);
		if (interval < 0.01f)
		{
			interval = 0.01f;
		}

	}


	/**
	 * ��ȡ���Ƶ�
	 **/
	public void updateControlPoints(int index)
	{
		controlPoint[0] = originPoints.get(index);
		controlPoint[1] = extraPoints.get(2 * index + 1);
		controlPoint[2] = extraPoints.get(2 * index + 2);
		controlPoint[3] = originPoints.get(index + 1);
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
