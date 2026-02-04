package org.tolven.web.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import org.tolven.core.entity.AccountUser;

public class HTMLOutputWriter extends FormattedOutputWriter {
	public HTMLOutputWriter(Writer writer, String type,
			AccountUser accountUser, Date now) {
		super(writer, type, accountUser, now);
	}

	/**
	 * Write a character adding escape characters for things like < >
	 * 
	 * @param writer
	 * @param val
	 * @throws IOException
	 */
	@Override
	public void writeEscape(char c) throws IOException {
		switch (c) {
		case '<':
			writer.write("&lt;");
			break;
		case '>':
			writer.write("&gt;");
			break;
		case '&':
			writer.write("&amp;");
			break;
		case '"':
			writer.write("&quot;");
			break;
		default:
			writer.write(c);
		}
	}

}
