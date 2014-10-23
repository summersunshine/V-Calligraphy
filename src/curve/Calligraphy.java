package curve;

import java.util.Vector;

import util.Time;

public class Calligraphy
{
	public Vector<Stroke> strokes;
	public Stroke currStroke;

	public Calligraphy()
	{
		strokes = new Vector<Stroke>();
		currStroke = new Stroke();
	}

	public void startStroke()
	{
		//currStroke = new Stroke();
	}

	public void addPoint(Point point)
	{
		Time.showTime();
		currStroke.addPointsAndUpdateCarve(point);
	}

	public void endStroke()
	{
		currStroke.addTailCurvePoints();
		strokes.add(currStroke);
		currStroke = new Stroke();
	}

}
