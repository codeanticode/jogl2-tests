package test.windows;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;

public class WindowFocusNEWT {
  private int width = 500;
  private int height = 290;
  
  private GLWindow window;
  private FPSAnimator animator;
  
  void draw(GL2ES2 gl) {    
    gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
    gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);
  }
  
  class TestGLListener implements GLEventListener {
    public void display(GLAutoDrawable drawable) {
      draw(drawable.getGL().getGL2ES2());
    }
    public void dispose(GLAutoDrawable drawable) { }
    public void init(GLAutoDrawable drawable) { 
    }
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) { 
      System.out.println("ReshapeEvent " + x + ", " + y + ", " + w + ", " + h);
    }
  }
  
  protected class NEWTMouseListener extends com.jogamp.newt.event.MouseAdapter {
    public NEWTMouseListener() {
      super();
    }
    @Override
    public void mousePressed(com.jogamp.newt.event.MouseEvent e) {
    }
    @Override
    public void mouseReleased(com.jogamp.newt.event.MouseEvent e) {
    }
    @Override
    public void mouseClicked(com.jogamp.newt.event.MouseEvent e) {
    }
    @Override
    public void mouseDragged(com.jogamp.newt.event.MouseEvent e) {}
    @Override
    public void mouseMoved(com.jogamp.newt.event.MouseEvent e) {
      System.out.println(e);
    }
    @Override
    public void mouseWheelMoved(com.jogamp.newt.event.MouseEvent e) {
    }
    @Override
    public void mouseEntered(com.jogamp.newt.event.MouseEvent e) {
    }
    @Override
    public void mouseExited(com.jogamp.newt.event.MouseEvent e) {
    }
  }  
  
  public void run() throws InterruptedException, InvocationTargetException {
    GLProfile profile = GLProfile.getDefault();
    GLCapabilities capabilities = new GLCapabilities(profile);
    window = GLWindow.create(capabilities); 
    
    window.setTitle("Focus test");
    window.setPosition(100, 100);
    window.setSize(width, height);    
    
    NEWTMouseListener mouseListener = new NEWTMouseListener();
    window.addMouseListener(mouseListener);
 
    TestGLListener glListener = new TestGLListener();
    window.addGLEventListener(glListener);    
    animator = new FPSAnimator(window, 60);
    animator.start();
    
    window.addWindowListener(new WindowAdapter() {
      @Override
      public void windowDestroyNotify(final WindowEvent e) {
        animator.stop();
        System.exit(0);
      }
    });
    
    EventQueue.invokeAndWait(new Runnable() {
      public void run() {
        window.setVisible(true);
    }});
  }
  
  public static void main(String[] args) {
    WindowFocusNEWT test;
    try {
      Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(WindowFocusNEWT.class.getName());
      test = (WindowFocusNEWT) c.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }    
    if (test != null) {
      try {
        test.run();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  } 
}
