package falstad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import android.view.View;
import android.widget.RelativeLayout;
import wsgarrison.ui.R;

public class GraphicsWrapper extends View{
	
	//Default serialVersionUID for serialization, not of consequence right now
	private static final long serialVersionUID = 1L;
	
	//Declare an field for wgc to consistently work with a graphics object
	//Graphics wgc;
	Paint redPaint;
	Paint currentPaint;
	
	Bitmap savePic;
	Canvas drawCanvas;

	public GraphicsWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

	void init(){
		//redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		//redPaint.setARGB(255, 255, 0, 0);
		
		currentPaint = new Paint();
		
		savePic = Bitmap.createBitmap(Constants.VIEW_WIDTH, Constants.VIEW_HEIGHT, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(savePic);
		
		/*Path path = new Path();
		path.moveTo(160, 160);
		path.lineTo(200, 200);
		path.lineTo(200, 240);
		path.lineTo(160, 280);
		drawCanvas.drawPath(path, redPaint);
		
		drawCanvas.drawLine(0, 0, 100, 100, redPaint);
		drawCanvas.drawRect(0, 0, 50, 50, redPaint);
		RectF oval = new RectF(60, 60, 150, 110);
		drawCanvas.drawOval(oval, redPaint);*/
		
	}
	
	@Override
	public void onDraw(Canvas c){
		super.onDraw(c);
		c.drawBitmap(savePic, 0, 0, null);
	}
	
	////////////////////////////////////////// wrapped color methods ///////////////////////////////////////////
	
	/**
	 * Encapsulation of Graphics' setcolor method that accepts strings for a specified set of colors as inputs and
	 * sets that color to the graphics object
	 * 
	 * @param inString
	 */
	public void setColorString(String inString){
		if(inString.equalsIgnoreCase("darkgray")){
			//wgc.setColor(Color.darkGray);
			currentPaint.setARGB(255, 85, 85, 85);
		}
		else if(inString.equalsIgnoreCase("black")){
			//wgc.setColor(Color.black);
			currentPaint.setARGB(255, 0, 0, 0);
		}
		else if(inString.equalsIgnoreCase("white")){
			//wgc.setColor(Color.white);
			currentPaint.setARGB(255, 255, 255, 255);
		}
		else if(inString.equalsIgnoreCase("gray")){
			//wgc.setColor(Color.gray);
			currentPaint.setARGB(255, 170, 170, 170);
		}
		else if(inString.equalsIgnoreCase("red")){
			//wgc.setColor(Color.red);
			currentPaint.setARGB(255, 255, 0, 0);
		}
		else if(inString.equalsIgnoreCase("yellow")){
			//wgc.setColor(Color.yellow);
			currentPaint.setARGB(255, 255, 255, 0);
		}
	}
	
	/**
	 * Sets color accepting a color object as a parameter, used for testing
	 * 
	 * 
	 * @param c
	 */
	//public void setColorColor(Color c){
		//wgc.setColor(c);
	//}
	
	/**
	 * returns the RGB for seg when given a 32 bit color value
	 * 
	 * @param c
	 * @return
	 */
	public static int[] getColorValues(int colNum){
		int[] i = new int[]{Color.red(colNum), Color.green(colNum), Color.blue(colNum)};
		
		return i;
	}
	
	/**
	 * Sets color using an int array of RGB values for a parameter
	 * 
	 * @param c
	 */
	public void setColorInt(int[] c){
		//Color tempCol = new Color(c[0], c[1], c[2]);
		//wgc.setColor(tempCol);
		currentPaint.setARGB(255, c[0], c[1], c[2]);
	}
	
	/**
	 * Encapsulates the getRGB method for color, used in Seg
	 * 
	 * @param c
	 * @return
	 */
	public int getRGB(int[] c){
		//Color tempCol = new Color(c[0], c[1], c[2]);
		//return tempCol.getRGB();
		
		currentPaint.setARGB(255, c[0], c[1], c[2]);
		return currentPaint.getColor();
	}
	
	/**
	 * Encapsulates the getRGB method to be accessed in a static way, used in MazeFileReader
	 * 
	 * @param c
	 * @return
	 */
	public static int getStatRGB(int[] c){
		//Color tempCol = new Color(c[0], c[1], c[2]);
		//return tempCol.getRGB();
		
		Paint newPaint = new Paint();
		newPaint.setARGB(255, c[0], c[1], c[2]);
		return newPaint.getColor();
	}
	
	////////////////////////////////// wrapped drawing methods ///////////////////////////////////////////////
	
	/**
	 * Encapsulates the fill rectangle method for graphics, accepting the same parameters as the graphics method
	 * 
	 * @param a
	 * @param b
	 * @param wid
	 * @param height
	 */
	public void wrapFillRect(int a, int b, int wid, int height){
		//wgc.fillRect(a, b, wid, height);
		drawCanvas.drawRect(a, b, a+wid, b+height, currentPaint);
		invalidate();
		requestLayout();
	}
	
	/**
	 * Encapsulates the draw line method for graphics, accepting the same parameters as the method in graphics
	 * 
	 * @param nx1
	 * @param ny1
	 * @param nx2
	 * @param ny2
	 */
	public void wrapDrawLine(int nx1, int ny1, int nx2, int ny2){
		//wgc.drawLine(nx1, ny1, nx2, ny2);
		drawCanvas.drawLine(nx1, ny1, nx2, ny2, currentPaint);
		invalidate();
		requestLayout();
	}
	
	/**
	 * Encapsulates the fill oval method in graphics, accepting the appropriate parameters
	 * 
	 * @param nx1
	 * @param ny1
	 * @param nx2
	 * @param ny2
	 */
	public void wrapFillOval(int nx1, int ny1, int nx2, int ny2){
		//wgc.fillOval(nx1, ny1, nx2, ny2);
		RectF oval = new RectF(nx1, ny1, nx1+nx2, nx1+ny2);
		drawCanvas.drawOval(oval, currentPaint);
		invalidate();
		requestLayout();
	}
	
	/**
	 * Encapsulates the fill polygon method in graphics, accepting two integer arrays and an integer for parameters
	 * 
	 * @param a
	 * @param b
	 * @param c
	 */
	public void wrapFillPolygon(int[] a, int[] b, int c){
		//general method for drawing polygons of any point count
		Path path = new Path();
		
		//instantiate count and set the first point in the polygon
		int count = 1;
		path.moveTo(a[0], b[0]);
		
		//move to the rest of the points to draw the outline
		//no need to move from the last point to the first point, fill doesn't mind
		while(count<c){
			path.lineTo(a[count], b[count]);
			count = count + 1;
		}
		
		//fill the outline
		currentPaint.setStyle(Paint.Style.FILL);
		drawCanvas.drawPath(path, currentPaint);
		invalidate();
		requestLayout();
	}
}