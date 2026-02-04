package test.org.tolven;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.tolven.doc.bean.DocProtectionBean;

import junit.framework.TestCase;

public class TestImageScaler extends TestCase {
	public void testImageScaler() throws IOException{
		String source = "src/test/org/tolven/testImage.jpg";
		String dest = "src/test/org/tolven/testImage.jpg";
		File f = new File(source);
		System.out.println(f.getAbsolutePath());		
		FileInputStream fis = new FileInputStream(f);
		FileOutputStream fos = new FileOutputStream(dest);
		byte[] content = new byte[fis.available()];
		DocProtectionBean.streamJPEGThumbnail(content,200,200,fos);
		fis.close();
		fos.close();
		fis = new FileInputStream(f);
		BufferedImage bufferedImage=ImageIO.read(new FileInputStream(dest));
		assertEquals(bufferedImage.getHeight(),200);
		assertEquals(bufferedImage.getWidth(),200);				 
	}
}
