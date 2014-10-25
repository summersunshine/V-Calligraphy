package curve;

import java.util.Vector;

import org.omg.CORBA.PUBLIC_MEMBER;

import constants.Global;
import util.BezierCurve;

public class Stroke
{
	public static int threshold = 30;

	// 控制点收缩系数 ，经调试0.6较好，Point是opencv的，可自行定义结构体(x,y)
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

		if (originPoints.lastElement() != point || true)
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
	 * 由一个点生成curve（实际上就是一个点）
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
	 * 由两个点生成curve
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
	 * 当点数超过2的时候生成的curve
	 **/
	public void createCurveWithMorePoints()
	{
		curves.remove(curves.lastElement());
		
		updateMidPoints();
		updateExtraPoints();
		samleForBezier( originPointCount-3,originPointCount-1);

	}
	
	
	
	
	/**
	 * 在曲线上采样
	 * @param beginIndx 
	 * @param endIndx 
	 * */
	public void samleForBezier(int beginIndx,int endIndx)
	{
		// 生成4控制点，产生贝塞尔曲线
		for (int i = beginIndx; i < endIndx; i++)
		{
			updateCurveInfo(i);
			updateControlPoints(i);
			createBezierCurves();

		}
	}
	
	
	/**
	 * 在控制点生成*的bezier曲线上进行采样
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
	 * 生成混合区段，包括一段过度段和一段稳定段
	 * */
	public void  createMixCurves(Point beginPoint,Point endPoint)
	{
		curves.addAll(FirstStroke.getCurves(beginPoint, endPoint,endSize));
		
	}
	
	/**
	 * 生成过度段
	 * */
	public void createTransitionCurves(Point beginPoint,Point endPoint)
	{
		curves.addAll(FirstStroke.getCurves(beginPoint, endPoint,endSize,endSize*0.5f));
		
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
			beginSize = Global.BigSize;
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
		
		if (isTail)
		{
			float factor = (200-getLastDistance())/200f;
			factor = factor>0.1?factor:0.1f;
			endSize*=factor;
		}
		
	}
	
	
	/**
	 * 更新采样的间隔
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
	 * 获取控制点
	 **/
	public void updateControlPoints(int index)
	{
		controlPoint[0] = originPoints.get(index);
		controlPoint[1] = extraPoints.get(2 * index + 1);
		controlPoint[2] = extraPoints.get(2 * index + 2);
		controlPoint[3] = originPoints.get(index + 1);
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
