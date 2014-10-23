package curve;

import java.util.Vector;

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
		else
		{
			updateMidPoints();
			updateExtraPoints();
			updateCurvePoints();
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
//				// 生成4控制点，产生贝塞尔曲线
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
	 * 由原始点与辅助控制点生成曲线的点
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
	 * 生成一个点的curve
	 **/
	public void createCurveWithSingePoint()
	{

		//curves.clear();
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
		
		if (beginPoint.sub(endPoint).length()>250)
		{
			addCurve(beginPoint, endPoint);
		}
		else
		{
			// 生成4控制点，产生贝塞尔曲线
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
	 * 更新采样的间隔
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
