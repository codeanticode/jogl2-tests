package test.windows;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JFileChooser;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;

public class DialogBehindNEWT {
  private int width = 500;
  private int height = 290;
  
  private Display display;
  private GLWindow window;
  private FPSAnimator animator;
  
  void draw(GL2ES2 gl) {    
    gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
    gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);
  }
  
  public void selectInput(String prompt, String callback) {
    selectImpl(prompt, callback, null, this, null, FileDialog.LOAD);
  } 
  
  public void test(File input) {
    window.setTitle(input.getAbsolutePath());
  }  
  
  class TestGLListener implements GLEventListener {
    public void display(GLAutoDrawable drawable) {
      draw(drawable.getGL().getGL2ES2());
    }
    public void dispose(GLAutoDrawable drawable) { }
    public void init(GLAutoDrawable drawable) { 
      display.getEDTUtil().invoke(false, new Runnable() {
        @Override
        public void run() {
          window.setAlwaysOnTop(true);
        }
      });      
    }
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) { 
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
      selectInput("test", "test");
    }
    @Override
    public void mouseClicked(com.jogamp.newt.event.MouseEvent e) {
    }
    @Override
    public void mouseDragged(com.jogamp.newt.event.MouseEvent e) {}
    @Override
    public void mouseMoved(com.jogamp.newt.event.MouseEvent e) {
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
    display = NewtFactory.createDisplay(null);
    display.addReference();
    
    GLProfile profile = GLProfile.getDefault();
    GLCapabilities capabilities = new GLCapabilities(profile);
    window = GLWindow.create(capabilities); 
    
    window.setTitle("DialogBehind test");
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
  
  static final int OTHER   = 0;
  static final int WINDOWS = 1;
  static final int MACOSX  = 2;
  static final int LINUX   = 3;
  
  static public int platform;

  static {
    String osname = System.getProperty("os.name");

    if (osname.indexOf("Mac") != -1) {
      platform = MACOSX;

    } else if (osname.indexOf("Windows") != -1) {
      platform = WINDOWS;

    } else if (osname.equals("Linux")) {  // true for the ibm vm
      platform = LINUX;

    } else {
      platform = OTHER;
    }
  }  
  static public boolean useNativeSelect = (platform != LINUX);
  
  static protected void selectImpl(final String prompt,
      final String callbackMethod,
      final File defaultSelection,
      final Object callbackObject,
      final Frame parentFrame,
      final int mode) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        File selectedFile = null;

        if (useNativeSelect) {
          FileDialog dialog = new FileDialog(parentFrame, prompt, mode);
          if (defaultSelection != null) {
            dialog.setDirectory(defaultSelection.getParent());
            dialog.setFile(defaultSelection.getName());
          }
          dialog.setVisible(true);
          String directory = dialog.getDirectory();
          String filename = dialog.getFile();
          if (filename != null) {
            selectedFile = new File(directory, filename);
          }

        } else {
          JFileChooser chooser = new JFileChooser();
          chooser.setDialogTitle(prompt);
          if (defaultSelection != null) {
            chooser.setSelectedFile(defaultSelection);
          }

          int result = -1;
          if (mode == FileDialog.SAVE) {
            result = chooser.showSaveDialog(parentFrame);
          } else if (mode == FileDialog.LOAD) {
            result = chooser.showOpenDialog(parentFrame);
          }
          if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
          }
        }
        selectCallback(selectedFile, callbackMethod, callbackObject);
      }
    });
  }  
  
  static private void selectCallback(File selectedFile,
      String callbackMethod,
      Object callbackObject) {
    try {
      Class<?> callbackClass = callbackObject.getClass();
      Method selectMethod =
          callbackClass.getMethod(callbackMethod, new Class[] { File.class });
      selectMethod.invoke(callbackObject, new Object[] { selectedFile });

    } catch (IllegalAccessException iae) {
      System.err.println(callbackMethod + "() must be public");

    } catch (InvocationTargetException ite) {
      ite.printStackTrace();

    } catch (NoSuchMethodException nsme) {
      System.err.println(callbackMethod + "() could not be found");
    }
  }
  
  public static void main(String[] args) {
    DialogBehindNEWT test;
    try {
      Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(DialogBehindNEWT.class.getName());
      test = (DialogBehindNEWT) c.newInstance();
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

