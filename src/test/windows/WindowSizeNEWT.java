package test.windows;

import java.io.IOException;

import org.junit.Assert;

import com.jogamp.nativewindow.Capabilities;
import com.jogamp.nativewindow.CapabilitiesImmutable;
import com.jogamp.nativewindow.NativeWindowFactory;
import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.Window;

public class WindowSizeNEWT {
  static Window createWindow(final Capabilities caps, final int x, final int y, final int width, final int height, final boolean onscreen, final boolean undecorated) throws InterruptedException {
    final boolean userPos = x>=0 && y>=0 ; // user has specified a position

    Assert.assertNotNull(caps);
    caps.setOnscreen(onscreen);
    // System.out.println("Requested: "+caps);

    //
    // Create native windowing resources .. X11/Win/OSX
    //
    final Window window = NewtFactory.createWindow(caps);
    Assert.assertNotNull(window);
    final Screen screen = window.getScreen();
    final Display display = screen.getDisplay();
    window.setUndecorated(onscreen && undecorated);
    if(userPos) {
      window.setPosition(x, y);
    }
    window.setSize(width, height);
    Assert.assertEquals(false,window.isNativeValid());
    Assert.assertEquals(false,window.isVisible());
    window.setVisible(true);
    // System.err.println("************* Created: "+window);

    Assert.assertEquals(true,display.isNativeValid());
    Assert.assertEquals(true,screen.isNativeValid());
    Assert.assertEquals(true,window.isVisible());
    Assert.assertEquals(true,window.isNativeValid());
    Assert.assertEquals(width, window.getWidth());
    Assert.assertEquals(height, window.getHeight());

    final CapabilitiesImmutable chosenCapabilities = window.getGraphicsConfiguration().getChosenCapabilities();
    Assert.assertNotNull(chosenCapabilities);
    Assert.assertTrue(chosenCapabilities.getGreenBits()>=5);
    Assert.assertTrue(chosenCapabilities.getBlueBits()>=5);
    Assert.assertTrue(chosenCapabilities.getRedBits()>=5);
    Assert.assertEquals(chosenCapabilities.isOnscreen(),onscreen);
    
    return window;
  }

  static void destroyWindow(final Window window, final boolean last) {
    if(null==window) {
      return;
    }
    final Screen screen = window.getScreen();
    final Display display = screen.getDisplay();
    window.destroy();
    // System.err.println("************* Destroyed: "+window);
    if(last) {
      Assert.assertEquals(false,screen.isNativeValid());
      Assert.assertEquals(false,display.isNativeValid());
    } else {
      Assert.assertEquals(true,screen.isNativeValid());
      Assert.assertEquals(true,display.isNativeValid());
    }
    Assert.assertEquals(false,window.isNativeValid());
    Assert.assertEquals(false,window.isVisible());
  }

  static int width, height;
  static long durationPerTest = 1000; // ms
  public static void main(final String args[]) throws IOException, InterruptedException {
    width  = 8000;
    height = 6000;    
    NativeWindowFactory.initSingleton();
    
    final Capabilities caps = new Capabilities();
    Assert.assertNotNull(caps);

    final Window window = createWindow(caps, -1, -1, width, height, true /* onscreen */, false /* undecorated */);
    final CapabilitiesImmutable chosenCapabilities = window.getGraphicsConfiguration().getChosenCapabilities();
    System.err.println("XXX: "+chosenCapabilities);
    for(int state=0; state*100<durationPerTest; state++) {
      Thread.sleep(100);
    }
    destroyWindow(window, true);
  }
}