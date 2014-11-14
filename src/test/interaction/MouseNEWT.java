package test.interaction;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLUniformData;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.GLArrayDataServer;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;

public class MouseNEWT {
  private int width = 4000;
  private int height = 2000;
  
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
    window.setTitle("NEWT Animator Test - frame: " + animator.getTotalFPSFrames() +" - fps: " + animator.getLastFPS());

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
    window = GLWindow.create(capabilities); 
    
    window.setTitle("NEWT Animator Test");
    window.setPosition(100, 100);
    window.setSize(width, height);    
    
    TestGLListener glListener = new TestGLListener();
    window.addGLEventListener(glListener);    
    NEWTMouseListener mouseListener = new NEWTMouseListener();
    window.addMouseListener(mouseListener);
    
    animator = new FPSAnimator(window, 60);
    animator.setUpdateFPSFrames(10, System.out);
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
