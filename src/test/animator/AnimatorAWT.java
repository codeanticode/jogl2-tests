package test.animator;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.nio.FloatBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLUniformData;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.GLArrayDataServer;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;

public class AnimatorAWT {
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
  
  private Frame frame;
  private GLCanvas canvas;
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
    frame.setTitle("NEWT Animator Test - frame: " + animator.getTotalFPSFrames() +" - fps: " + animator.getLastFPS());
    
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
    frame = new Frame("Animator Test");
    frame.setLocation(100, 100);
    frame.setSize(width, height);    
    
    GLProfile profile = GLProfile.getDefault();
    GLCapabilities capabilities = new GLCapabilities(profile);
    
    canvas = new GLCanvas(capabilities);
    canvas.setBounds(0, 0, width, height);
    canvas.setFocusable(true);    
    
    frame.setLayout(new BorderLayout());
    frame.add(canvas, BorderLayout.CENTER);
    
    TestGLListener glListener = new TestGLListener();
    canvas.addGLEventListener(glListener);    
    animator = new FPSAnimator(canvas, 60);
    animator.setUpdateFPSFrames(10, System.out);
    animator.start();
    
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        animator.stop();
        System.exit(0);
      }
    }); 
    
    EventQueue.invokeAndWait(new Runnable() {
      public void run() {
        frame.validate();                
        frame.setVisible(true);
    }});
  }
  
  public static void main(String[] args) {
    AnimatorAWT test;
    try {
      Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(AnimatorAWT.class.getName());
      test = (AnimatorAWT) c.newInstance();
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
