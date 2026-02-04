package org.tolven.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/** 
 * This Class is used to generate PDF report contet
 * @author Suja Sundaresan
 */
public class ContentCaptureServletResponse extends HttpServletResponseWrapper {
	
	private ByteArrayOutputStream contentBuffer;
	private PrintWriter writer;
	
	public ContentCaptureServletResponse(HttpServletResponse originalResponse) {
		super(originalResponse); 
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if(writer == null){
			contentBuffer = new ByteArrayOutputStream();
			writer = new PrintWriter(contentBuffer);
		}
		return writer;
	}
	
	/**
	 * Generate PDF Report data
	 * added on 10/12/09
	 * @param path
	 * @return String
	 */
	public String getContent(String path) {
		if(null != writer){
			writer.flush(); 
		}else{
			try {
				writer = getWriter();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		String xhtmlContent = new String(contentBuffer.toByteArray());
		xhtmlContent = "<html><head></head>"+ 
						"<body>"+xhtmlContent+"</body></html>";
		xhtmlContent = xhtmlContent.replaceAll("<thead>|</thead>|"+ 
											   "<tbody>|</tbody>","");
		return xhtmlContent; 
	}
}








