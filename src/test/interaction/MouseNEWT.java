package test.interaction;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.media.nativewindow.util.RectangleImmutable;
import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLUniformData;

import com.jogamp.newt.Display;
import com.jogamp.newt.MonitorDevice;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.GLArrayDataServer;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;

// com.jogamp.opengl.test.junit.newt.mm.TestScreenMode01cNEWT 
public class MouseNEWT {
  private int width = 1440 * 4;
  private int height = 900;

//  private int width = 640;
//  private int height = 360;

  
  private ShaderCode vertShader;
  private ShaderCode fragShader;
  private ShaderProgram shaderProg;
  private ShaderState shaderState;
  private GLUniformData mouse;
  private GLArrayDataServer vertices;
  
  private GLWindow window;
  private FPSAnimator animator;
  
  void setup(GL2ES2 gl) {
    vertShader = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, this.getClass(), "shaders",
        "shaders/bin", "mouse", true);
    fragShader = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, this.getClass(), "shaders",
        "shaders/bin", "mouse", true);
    shaderProg = new ShaderProgram();
    shaderProg.add(gl, vertShader, System.err);
    shaderProg.add(gl, fragShader, System.err); 
    
    shaderState = new ShaderState();
    shaderState.attachShaderProgram(gl, shaderProg, true);
    
    mouse = new GLUniformData("mouse", 2, FloatBuffer.wrap(new float[] {0, 0}));
    shaderState.ownUniform(mouse);
    shaderState.uniform(gl, mouse);    
        
    vertices = GLArrayDataServer.createGLSL("vertex", 2, GL.GL_FLOAT, false, 4, GL.GL_STATIC_DRAW);
    vertices.putf(50 * (-1.0f / width)); vertices.putf(50 * (-1.0f / height));
    vertices.putf(50 * (+1.0f / width)); vertices.putf(50 * (-1.0f / height));
    vertices.putf(50 * (-1.0f / width)); vertices.putf(50 * (+1.0f / height));
    vertices.putf(50 * (+1.0f / width)); vertices.putf(50 * (+1.0f / height));
    vertices.seal(gl, true);
    shaderState.ownAttribute(vertices, true);
  }
  
  void draw(GL2ES2 gl) {    
//    window.setTitle("NEWT Animator Test - frame: " + animator.getTotalFPSFrames() +" - fps: " + animator.getLastFPS());
    window.setTitle(window.getWidth() + " " + window.getHeight() + "|" + window.getSurfaceWidth() + " " + window.getSurfaceHeight());
    
    gl.glClearColor(0, 0, 0, 1);
    gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);
    
    shaderState.useProgram(gl, true);    
    shaderState.uniform(gl, mouse);
    
    vertices.enableBuffer(gl, true);
    gl.glDrawArrays(GL2ES2.GL_TRIANGLE_STRIP, 0, 4);
    vertices.enableBuffer(gl, false);
    
    shaderState.useProgram(gl, false);
  }
  
  class TestGLListener implements GLEventListener {
    public void display(GLAutoDrawable drawable) {
      draw(drawable.getGL().getGL2ES2());
    }
    public void dispose(GLAutoDrawable drawable) { }
    public void init(GLAutoDrawable drawable) { 
      setup(drawable.getGL().getGL2ES2());
    }
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) { }    
  }
  
  protected class NEWTMouseListener extends com.jogamp.newt.event.MouseAdapter {
    public NEWTMouseListener() {
      super();
    }
    @Override
    public void mouseMoved(com.jogamp.newt.event.MouseEvent e) {
      float x = ((float)e.getX() - width) / width; 
      float y = 1 - (float)e.getY() / height;
      mouse.setData(FloatBuffer.wrap(new float[] {x, y}));      
    }
  }
  
  public void run() throws InterruptedException, InvocationTargetException {
    GLProfile profile = GLProfile.getDefault();
    GLCapabilities capabilities = new GLCapabilities(profile);
    
    Display display = NewtFactory.createDisplay(null);
    display.addReference(); // trigger creation
//    System.out.println(Display.getAllDisplays());
    
    
    Screen screen = NewtFactory.createScreen(display, 0);
    //screen.addReference(); // trigger creation
    screen.createNative(); // instantiate for resolution query and keep it alive !
    final int swidth = screen.getWidth();
    final int sheight = screen.getHeight();
    System.out.println("Screen res " + swidth + "x" + sheight);
    
    
//    System.out.println(Screen.getAllScreens());
//    System.out.println(screen.getMonitorDevices().size());  
    
    ArrayList<MonitorDevice> monitors = new ArrayList<MonitorDevice>();
    for (int i = 0; i < screen.getMonitorDevices().size(); i++) {      
      MonitorDevice monitor = screen.getMonitorDevices().get(i);
      System.out.println("Monitor " + monitor.getId() + " ************");
      System.out.println(monitor.toString());
      System.out.println(monitor.getViewportInWindowUnits());
      System.out.println(monitor.getViewport());
      
      monitors.add(monitor);
    }
    System.out.println("*******************************");
    
    
//    MonitorDevice monitor0 = screen.getMonitorDevices().get(1);    
//    RectangleImmutable monitorVp = monitor0.getViewportInWindowUnits();
//    int x0 = monitorVp.getX();
//    int y0 = monitorVp.getY();
//    System.out.println(x0 + " " + y0);
    
    
//    final Display disp = screen.getDisplay();
//    System.err.println("Test.0: Window screen: "+screen);
//    System.err.println("Test.0: Window bounds (pre): screenPos "+x0+"/"+y0+" [pixels], windowSize "+width+"x"+height+" [wu] within "+screen.getViewport()+" [pixels]");
    
    window = GLWindow.create(screen, capabilities);    
    window.setTitle("NEWT Animator Test");
    window.setSize(width, height);
    window.setPosition(0, 0);
//    window.setTopLevelPosition(0, 0);
        
    
    MonitorDevice monitor = window.getMainMonitor();
    System.out.println(monitor);
    
//    boolean spanAcrossMonitors = false;
//    if (spanAcrossMonitors) {
//      window.setFullscreen(monitors);        
//    } else {
//      window.setFullscreen(true);
//    }
    
    /*
        monitor = window0.getMainMonitor();
        window0WindowBounds = window0.getBounds();
        window0SurfaceSize = new Dimension(window0.getSurfaceWidth(), window0.getSurfaceHeight());
        System.err.println("Test.1: Window bounds    : "+window0WindowBounds+" [wu] within "+screen.getViewportInWindowUnits()+" [wu]");
        System.err.println("Test.1: Window size      : "+window0SurfaceSize+" [pixels]");
        System.err.println("Test.1: Screen viewport  : "+screen.getViewport()+" [pixels]");
        System.err.println("Test.1: Monitor viewport : "+monitor.getViewport()+" [pixels], "+monitor.getViewportInWindowUnits()+" [wu]");
        if( !spanAcrossMonitors ) {
            Assert.assertEquals(monitor.getViewportInWindowUnits(), window0WindowBounds);
        } else {
            List<MonitorDevice> monitorsUsed = monitors;
            if( null == monitorsUsed ) {
                monitorsUsed = window0.getScreen().getMonitorDevices();
            }
            final Rectangle monitorsUsedViewport = new Rectangle();
            MonitorDevice.unionOfViewports(null, monitorsUsedViewport, monitorsUsed);
            Assert.assertEquals(monitorsUsedViewport,  window0WindowBounds);
        }
     */
    
    
//    System.err.println(display == disp); 
    
    
    
//    Screen screen1 = NewtFactory.createScreen(display, 1);
//    screen1.addReference(); // trigger creation
//    System.out.println(screen0.getWidth() + " " + screen0.getHeight());
//    System.out.println(screen1.getWidth() + " " + screen1.getHeight());
    
    
    
    
    
    
    
    TestGLListener glListener = new TestGLListener();
    window.addGLEventListener(glListener);    
    NEWTMouseListener mouseListener = new NEWTMouseListener();
    window.addMouseListener(mouseListener);
    
    animator = new FPSAnimator(window, 60);
//    animator.setUpdateFPSFrames(10, System.out);
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
    MouseNEWT test;
    try {
      Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(MouseNEWT.class.getName());
      test = (MouseNEWT) c.newInstance();
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
