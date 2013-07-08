package ru.mainstream.vkstream.video;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormater {

	private final String QuotMatcher = "&quot;";
	private final String BrMatcher = "(<br>|<br/>)";
	
	public String formatText(String text)
	{
		Pattern p1 = Pattern.compile(QuotMatcher);
		Matcher m1 = p1.matcher(text);
		if(m1.find())
		{
			text = m1.replaceAll("\"");
			
			Pattern p2 = Pattern.compile(BrMatcher);
			Matcher m2 = p2.matcher(text);
			
			if(m2.find())
			{
				text = m2.replaceAll("\n");
			}
		}
		return text;
	}
	
}
