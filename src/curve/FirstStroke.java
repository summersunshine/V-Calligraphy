package curve;

import java.util.Vector;

public class FirstStroke
{
	public float BigSize;
	public float SmallSize=25;
	
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
	
	public boolean isMix;
	
	public FirstStroke(Point beginPoint,Point endPoint,float BigSize)
	{
		// TODO Auto-generated constructor stub
		this.beginPoint = beginPoint;
		this.endPoint = endPoint;
		this.BigSize = BigSize;
		this.curves = new Vector<Curve>();
		this.createMix();
	}	
	
	public FirstStroke(Point beginPoint,Point endPoint,float BigSize,float SmallSize)
	{
		// TODO Auto-generated constructor stub

		this.beginPoint = beginPoint;
		this.endPoint = endPoint;
		this.BigSize = BigSize;
		this.SmallSize = SmallSize;
		this.curves = new Vector<Curve>();
		this.createTransition();
	}	
	
	public static Vector<Curve> getCurves(Point beginPoint,Point endPoint,float BigSize)
	{
		FirstStroke somethingElse = new FirstStroke(beginPoint, endPoint,BigSize);
		return somethingElse.curves;
	}
	public static Vector<Curve> getCurves(Point beginPoint,Point endPoint,float BigSize,float SmallSize)
	{
		FirstStroke somethingElse = new FirstStroke(beginPoint, endPoint,BigSize,SmallSize);
		return somethingElse.curves;
	}
	
	public void createTransition()
	{
		length = beginPoint.sub(endPoint).length();
		pieceNum = (int) (2*length/(BigSize+SmallSize));
		commonDifference = (BigSize-SmallSize)/pieceNum;
		pretreatment();
		calTransition();
	}
	
	public void createMix()
	{
		
		length = beginPoint.sub(endPoint).length();
		float percent = 50/length;
		pieceNum = (int) (2*percent*length/(BigSize+SmallSize));
		commonDifference = (BigSize-SmallSize)/pieceNum;
		pretreatment();
		calMix();
	}
	
	public void calTransition()
	{
		Point currPoint= beginPoint,nextPoint;
		for (int i = 0; i < distances.size()-1; i++)
		{
			nextPoint = Point.getPointBetweenTweenPoint(endPoint,beginPoint,distances.get(i)/length);
			createCurve(30,currPoint,nextPoint, sizes.get(i), sizes.get(i+1));
			currPoint = nextPoint;
		}
	}
	
	public void calMix()
	{
		// TODO Auto-generated method stub
		
		while (distances.lastElement()<length)
		{
			distances.add(distances.lastElement()+SmallSize);
			sizes.add(SmallSize);
			
		}
		
		Point currPoint= beginPoint,nextPoint;
		for (int i = 0; i < distances.size()-1; i++)
		{
			nextPoint = Point.getPointBetweenTweenPoint(endPoint,beginPoint,distances.get(i)/length);
			if (i<=pieceNum)
			{
				createCurve(30,currPoint,nextPoint, sizes.get(i), sizes.get(i+1));
			}
			else
			{
				createCurve(20,currPoint,nextPoint, SmallSize, SmallSize);
			}
			currPoint = nextPoint;
		}
	}
	
	
	public void createCurve(int num,Point beginPoint,Point endPoint,float beginSize,float endSize)
	{
		Vector<Point> points = new Vector<Point>();
		for (int j = 0; j < num; j++)
		{
			points.add(Point.getPointBetweenTweenPoint(endPoint,beginPoint,j/(num-1f)));
		}
		curves.add(new Curve(points,beginSize,endSize));
	}
	
	public void pretreatment()
	{
		sizes = new Vector<Float>();
		distances = new Vector<Float>();
		sizes.add(BigSize);
		distances.add((float) 0);
		for (int i = 0; i < pieceNum; i++)
		{
			distances.add(distances.lastElement() + sizes.lastElement());
			sizes.add(BigSize - commonDifference*i);
		}
	}
	

}
