package test.animator;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLUniformData;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.Animator;
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
  private int frameCount;
  
  private GLWindow window;
  
  private int fcount, lastm;  
  private int fint = 1; 
  private float frameRate;
  
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
    window.setTitle("NEWT Animator Test - frame: " + frameCount +" - fps: " + frameRate);
    
    gl.glClearColor(0, 0, 0, 1);
    gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);
    
    shaderState.useProgram(gl, true);    
    
    time.setData((System.currentTimeMillis() - millisInit) / 1000.0f);
    shaderState.uniform(gl, time);
    vertices.enableBuffer(gl, true);
    gl.glDrawArrays(GL2ES2.GL_TRIANGLE_STRIP, 0, 4);
    vertices.enableBuffer(gl, false);
    
    shaderState.useProgram(gl, false);
    
    fcount += 1;
    int m = (int) (System.currentTimeMillis() - millisInit);
    if (m - lastm > 1000 * fint) {
      frameRate = (float)(fcount) / fint;
      fcount = 0;
      lastm = m;
    }      
    
    frameCount++;
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
  
  public void run() throws InterruptedException, InvocationTargetException {
    GLProfile profile = GLProfile.getDefault();
    GLCapabilities capabilities = new GLCapabilities(profile);
    window = GLWindow.create(capabilities); 
    
    window.setTitle("NEWT Animator Test");
    window.setPosition(100, 100);
    window.setSize(width, height);    
    
    TestGLListener glListener = new TestGLListener();
    window.addGLEventListener(glListener);    
    final GLAnimatorControl animator = new Animator(window);
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
