package falstad;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;

public class MazePanel extends Panel  {
	/* Panel operates a double buffer see
	 * http://www.codeproject.com/Articles/2136/Double-buffer-in-standard-Java-AWT
	 * for details
	 */
	Image bufferImage ;
	
	public MazePanel() {
		super() ;
		this.setFocusable(false) ;
	}
	@Override
	public void update(Graphics g) {
		paint(g) ;
	}
	@Override
	public void paint(Graphics g) {
		g.drawImage(bufferImage,0,0,null) ;
	}
	/*
	public void setBufferImage(Image buffer) {
		bufferImage = buffer ;
	}
	*/
	public void initBufferImage() {
		bufferImage = createImage(Constants.VIEW_WIDTH, Constants.VIEW_HEIGHT);
		if (null == bufferImage)
		{
			System.out.println("Error: creation of buffered image failed, presumedly container not displayable");
		}
	}
	public Graphics getBufferGraphics() {
		if (null == bufferImage)
			initBufferImage() ;
		return bufferImage.getGraphics() ;
	}
	public void update() {
		paint(getGraphics()) ;
	}
}
