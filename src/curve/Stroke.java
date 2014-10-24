package curve;

import java.util.Vector;

import org.omg.CORBA.PUBLIC_MEMBER;

import util.BezierCurve;

public class Stroke
{
	public static int threshold = 30;

	// 控制点收缩系数 ，经调试0.6较好，Point是opencv的，可自行定义结构体(x,y)
	public static float scale = 0.6f;

	public Vector<Curve> curves;

	public Vector<Point> originPoints;
	public Vector<Point> middlePoints;
	public Vector<Point> extraPoints;
	public int originPointCount;

	private Point controlPoint[];
	private float beginSize = 70;
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
	 * 加入点并更新曲线
	 * */
	public void addPointsAndUpdateCarve(Point point)
	{
		addPoints(point);
		updateCurve();
	}

	/**
	 * 将点加入到原始点集合中 如果点和集合中最后一个点相等 就不添加进去
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
	 * 更新曲线
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
	 * 当前和后面一个的中点
	 * */
	public void updateMidPoints()
	{
		assert (originPoints.size() > 1);

		addTempPoints();

		createMidPoints();

		removeTempPoints();
	}

	/**
	 * 在头部获取一个新的临时点
	 * */
	public Point getTempFirstPoint()
	{
		Point firstPoint = originPoints.firstElement();
		Point secondPoint = originPoints.get(1);
		Point tempFirstPoint = Point.getSymmetryPoint(firstPoint, secondPoint);

		return tempFirstPoint;
	}

	
	/**
	 * 在尾部获取一个新的临时点
	 * */
	public Point getTempLastPoint()
	{
		Point lastPoint = originPoints.lastElement();
		Point lastSecondPoint = originPoints.get(originPoints.size() - 2);
		Point tempFirstPoint = Point.getSymmetryPoint(lastPoint, lastSecondPoint);

		return tempFirstPoint;
	}

	
	/**
	 * 获取最后一个点
	 * */
	public Point getLastPoint()
	{
		if(originPoints.isEmpty()) return null;
		return originPoints.lastElement();
	}
	
	
	/**
	 * 获取倒数第二个点
	 * */
	public Point getLastSecondPoint()
	{
		if(originPoints.size()<2) return null;
		return originPoints.get(originPoints.size()-2);
	}
	
	
	/**
	 * 获取最后两个点之间的距离
	 * */
	public float getLastDistance()
	{
		Point beginPoint = getLastPoint();
		Point endPoint = getLastSecondPoint();
		return beginPoint.sub(endPoint).length();
	}
	
	/**
	 * 生成中点
	 * */
	public void createMidPoints()
	{
		middlePoints.clear();

		// 生成中点
		for (int i = 0; i < originPoints.size() - 1; i++)
		{
			Point currPoint = originPoints.get(i);
			Point nextPoint = originPoints.get(i+1);
			Point midPoint = Point.getMidPoint(currPoint, nextPoint);
			middlePoints.add(midPoint);
		}
	}
	
	
	/**
	 * 加入临时点
	 * */
	public void addTempPoints()
	{
		originPoints.add(0, getTempFirstPoint());
		originPoints.add(getTempLastPoint());
	}
	
	
	/**
	 * 删除临时点
	 * */
	public void removeTempPoints()
	{
		originPoints.remove(0);
		originPoints.remove(originPoints.size() - 1);
	}

	/**
	 * 获取额外的控制点
	 * */
	public void updateExtraPoints()
	{
		extraPoints.clear();
		for (int i = 0; i < middlePoints.size() - 1; i++)
		{
			// 当前原始点
			Point currOriginPoint = originPoints.get(i);

			// 当前的中点
			Point currMiddlePoint = middlePoints.get(i);
			Point nextMiddlePoint = middlePoints.get(i + 1);

			// 两个中点的中点
			Point midInMidPoint = Point.getMidPoint(currMiddlePoint, nextMiddlePoint);
			Point offsetPoint = currOriginPoint.sub(midInMidPoint);

			// 当前的控制点
			Point currExtraPoint = currMiddlePoint.add(offsetPoint);
			Point nextExtraPoint = nextMiddlePoint.add(offsetPoint);

			currExtraPoint = Point.getPointBetweenTweenPoint(currExtraPoint, currOriginPoint, scale);
			nextExtraPoint = Point.getPointBetweenTweenPoint(nextExtraPoint, currOriginPoint, scale);

			extraPoints.add(currExtraPoint);
			extraPoints.add(nextExtraPoint);
		}
	}

	/**
	 *加上收尾的点
	 **/
	public void  addTailCurvePoints()
	{
		if(originPointCount>2)
		{
			//addTempPoints();
		
			//originPoints.add(Point.getPointBetweenTweenPoint(getTempLastPoint(),getLastPoint(),(float) 0.2));
			//originPoints.add(getTempLastPoint());
			//originPointCount++;
			//Point beginPoint = originPoints.get(originPointCount-3);
			Point beginPoint = originPoints.get(originPointCount-2);
			Point endPoint = originPoints.get(originPointCount-1);
			
			
//			if (beginPoint.sub(endPoint).length()>50 &&
//					beginPoint.sub(endPoint).length()<150)
//			{
//				curves.remove(curves.size()-1);
//				addCurve2(beginPoint, endPoint);
//			}
		}
	}
	
	

	/**
	 * 生成一个点的curve
	 **/
	public void createCurveWithSingePoint()
	{
		curves.add(new Curve(originPoints, beginSize, endSize));

	}

	/**
	 * 生成两个点的curve
	 **/
	public void createCurveWithTwoPoints()
	{
		curves.clear();
		Point beginPoint = originPoints.get(0);
		Point endPoint = originPoints.get(1);
		
		if (getLastDistance()>150)
		{
			addCurve(beginPoint, endPoint);
		}
		else if (getLastDistance()>100)
		{
			addCurve2(beginPoint, endPoint);
		}
		else
		{
			updateMidPoints();
			updateExtraPoints();
			samleForBezier(0,1);
		}
	}
	
	
	/**
	 * 生成多点的curve
	 **/
	public void createCurveWithMorePoints()
	{
		curves.remove(curves.size()-1);
		
		updateMidPoints();
		updateExtraPoints();
		samleForBezier( originPointCount-3,originPointCount-1);

	}
	
	
	
	
	/**
	 * */
	public void samleForBezier(int begin,int end)
	{
		// 生成4控制点，产生贝塞尔曲线
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
	
	/**
	 * */
	public void samleForBezier2(int begin,int end)
	{
		// 生成4控制点，产生贝塞尔曲线
		for (int i = begin; i < end; i++)
		{
			updateCurveInfo(i);
			controlPoint = getControlPoints(i);
			addCurves();
		}
	}
	
	
	public void addCurves()
	{
		Vector<Point> curvePoints = new Vector<Point>();
		curvePoints.add(BezierCurve.bezier3func(1, controlPoint));
		Vector<Float> distances = new Vector<Float>();
		distances.add((float) 0);
		float totalDistance = 0;
		float distance = 0;
		
		Point currPoint = BezierCurve.bezier3func(1, controlPoint);
		Point nextPoint;
		
		for (float j = 0.9f; j >= 0; j -= 0.1)
		{
			nextPoint =  BezierCurve.bezier3func(j, controlPoint);
			distance = Point.getDistance(currPoint, nextPoint);
			totalDistance +=distance;
			distances.add(totalDistance);
			curvePoints.addElement(BezierCurve.bezier3func(j, controlPoint));
		}
		
		
		//curves = new Vector<Curve>();
		for (int j = 0; j < distances.size()-1; j++)
		{
			Vector<Point> points = new Vector<Point>();
			Point point;
			for (int k = 0; k < 10; k++)
			{
				point = Point.getPointBetweenTweenPoint(curvePoints.get(j),curvePoints.get(j+1),k/9f);
				points.add(point);
			}
			float size1 = beginSize+distances.get(j)*(endSize-beginSize)/(totalDistance);
			float size2 = beginSize+distances.get(j+1)*(endSize-beginSize)/(totalDistance);
			curves.add(new Curve(points,size1,size2));

		}
	}
	
	public void addCurve(Point beginPoint,Point endPoint)
	{
		Vector<Curve> sthCurves = FirstStroke.getCurves(beginPoint, endPoint,endSize);
		for (int i = 0; i < sthCurves.size(); i++)
		{
			curves.add(sthCurves.get(i));
		}
		endSize = 25;
	}
	
	public void addCurve2(Point beginPoint,Point endPoint)
	{
		Vector<Curve> sthCurves = FirstStroke.getCurves(beginPoint, endPoint,endSize,endSize*0.5f);
		for (int i = 0; i < sthCurves.size(); i++)
		{
			curves.add(sthCurves.get(i));
		}
		//endSize*=0.5;
	}
	

	
	/**
	 * 跟新构建curve需要的信息
	 * */
	public void updateCurveInfo(int index)
	{
		updateDistance(index);
		updateBeginSize();
		updateEndSize();
		updateInterval();
	}
	
	
	/**
	 * 更新当前原始点到下一原始点的距离
	 * */
	public void updateDistance(int index)
	{
		distance = getAdjoinDistance(originPoints, index);
	}

	/**
	 * 更新beginSize
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
	 * 更新endSize
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
		
	}
	
	
	/**
	 * 更新采样的间隔
	 * */
	public void updateInterval()
	{

		float interval = 0.04f + (threshold - distance) / 50000f;

		System.out.println("distance: " + distance + "interval :" + interval);
		if (interval < 0.025f)
		{
			interval = 0.025f;
		}

	}


	/**
	 * 获取控制点
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
	 * 获取相邻的的点之间的距离
	 * */
	public float getAdjoinDistance(Vector<Point> points, int index)
	{
		if(index >= points.size()-1) return 0;
		
		return points.get(index + 1).sub(points.get(index)).length();
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

	
	
}
