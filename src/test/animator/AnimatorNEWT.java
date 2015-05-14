package test.animator;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.nio.FloatBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLUniformData;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.GLArrayDataServer;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;

public class AnimatorNEWT {
  private int width = 500;
  private int height = 290;
  
  private ShaderCode vertShader;
  private ShaderCode fragShader;
  private ShaderProgram shaderProg;
  private ShaderState shaderState;
  private GLUniformData resolution;
  private GLUniformData time;  
  private GLArrayDataServer vertices;
  
  private long millisInit;
  
  private GLWindow window;
  private FPSAnimator animator;
  
  private boolean resized;
  
  void setup(GL2ES2 gl) {
    vertShader = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, this.getClass(), "shaders",
        "shaders/bin", "landscape", true);
    fragShader = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, this.getClass(), "shaders",
        "shaders/bin", "landscape", true);
    shaderProg = new ShaderProgram();
    shaderProg.add(gl, vertShader, System.err);
    shaderProg.add(gl, fragShader, System.err); 
    
    shaderState = new ShaderState();
    shaderState.attachShaderProgram(gl, shaderProg, true);
    
    resolution = new GLUniformData("iResolution", 3, FloatBuffer.wrap(new float[] {width, height, 0}));
    shaderState.ownUniform(resolution);
    shaderState.uniform(gl, resolution);    
    
    time = new GLUniformData("iGlobalTime", 0.0f);
    shaderState.ownUniform(time);
        
    vertices = GLArrayDataServer.createGLSL("inVertex", 2, GL.GL_FLOAT, false, 4, GL.GL_STATIC_DRAW);
    vertices.putf(-1.0f); vertices.putf(-1.0f);
    vertices.putf(+1.0f); vertices.putf(-1.0f);
    vertices.putf(-1.0f); vertices.putf(+1.0f);
    vertices.putf(+1.0f); vertices.putf(+1.0f);
    vertices.seal(gl, true);
    shaderState.ownAttribute(vertices, true);
    
    millisInit = System.currentTimeMillis();
  }
  
  void draw(GL2ES2 gl) {    
    window.setTitle("NEWT Animator Test - frame: " + animator.getTotalFPSFrames() +" - fps: " + animator.getLastFPS());

    gl.glClearColor(0, 0, 0, 1);
    gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);
    
    shaderState.useProgram(gl, true);    
    
    time.setData((System.currentTimeMillis() - millisInit) / 1000.0f);
    shaderState.uniform(gl, time);    
    if (resized) {
      shaderState.uniform(gl, resolution);
      resized = false;
    }
    
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
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) { 
      width = w;
      height = h;
      resolution.setData(FloatBuffer.wrap(new float[] {width, height, 0}));  
      resized = true;      
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
    AnimatorNEWT test;
    try {
      Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(AnimatorNEWT.class.getName());
      test = (AnimatorNEWT) c.newInstance();
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
