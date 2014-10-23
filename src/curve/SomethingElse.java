package curve;

import java.util.Vector;

public class SomethingElse
{
	public float BigSize;
	public float SmallSize=15;
	
	//纹理片段的数目
	public int pieceNum;
	//公差
	public float commonDifference;
	//长度
	public float length;
	//各个片段的大小
	public Vector<Float> sizes;
	//各个片段到起始点的距离
	public Vector<Float> distances;
	public Point beginPoint;
	public Point endPoint;
	
	public Vector<Curve> curves;
	
	public SomethingElse(Point beginPoint,Point endPoint,float BigSize)
	{
		// TODO Auto-generated constructor stub
		this.beginPoint = beginPoint;
		this.endPoint = endPoint;
		this.BigSize = BigSize;
		this.cal();
	}	
	
	public SomethingElse(Point beginPoint,Point endPoint,float BigSize,float SmallSize)
	{
		// TODO Auto-generated constructor stub

		this.beginPoint = beginPoint;
		this.endPoint = endPoint;
		this.BigSize = BigSize;
		this.SmallSize = SmallSize;
		this.cal();
	}	
	
	public static Vector<Curve> getCurves(Point beginPoint,Point endPoint,float BigSize)
	{
		SomethingElse somethingElse = new SomethingElse(beginPoint, endPoint,BigSize);
		return somethingElse.curves;
	}
	public static Vector<Curve> getCurves(Point beginPoint,Point endPoint,float BigSize,float SmallSize)
	{
		SomethingElse somethingElse = new SomethingElse(beginPoint, endPoint,BigSize,SmallSize);
		return somethingElse.curves;
	}
	
	public void cal()
	{
		length = beginPoint.sub(endPoint).length();
		pieceNum = (int) (length/(BigSize+SmallSize)/2);
		commonDifference = (BigSize-SmallSize)/pieceNum;
		
		
		System.out.println("length : " + length + "pieceNum : " + pieceNum + " commonDifference : " +commonDifference);
		
		
		sizes = new Vector<Float>();
		distances = new Vector<Float>();
		sizes.add(BigSize);
		distances.add((float) 0);
		for (int i = 0; i < pieceNum; i++)
		{
			distances.add(distances.lastElement() + sizes.lastElement());
			sizes.add(BigSize - commonDifference*i);
			
			
			System.out.println("sizes : " + sizes.get(i) + "distances : " + distances.get(i));
		}
		while (distances.lastElement()<length)
		{
			distances.add(distances.lastElement()+SmallSize);
			sizes.add(SmallSize);
			
		}
//		for (int i = pieceNum; i < 4*pieceNum; i++)
//		{
//			distances.add(distances.lastElement()+SmallSize);
//			sizes.add(SmallSize);
//			
//			
//			System.out.println("sizes : " + sizes.get(i) + "distances : " + distances.get(i));
//		}
		
		
		Point currPoint= beginPoint;
		Point nextPoint = Point.getPointBetweenTweenPoint(endPoint,beginPoint,0);
		
		curves = new Vector<Curve>();
		for (int i = 0; i < distances.size(); i++)
		{
			
			nextPoint = Point.getPointBetweenTweenPoint(endPoint,beginPoint,distances.get(i)/length);
			
			System.out.println("next point :" + nextPoint.x + " " + nextPoint.y + "endPoint : " +  endPoint.x + " " + endPoint.y);
			
			Vector<Point> points = new Vector<Point>();
			for (int j = 0; j < 30; j++)
			{
				points.add(Point.getPointBetweenTweenPoint(nextPoint,currPoint,j/29f));
			}
			if (i<=pieceNum)
			{
				curves.add(new Curve(points, sizes.get(i), sizes.get(i+1)));
			}
			else
			{
				curves.add(new Curve(points,SmallSize,SmallSize));
			}
			
			currPoint = nextPoint;
		}
	}
}
