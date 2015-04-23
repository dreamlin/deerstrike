package org.linjia.deerstrike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.linjia.deerstrike.model.DbCommandText;

public class DbCommandUtil {
	protected static Map<String, DbCommandText>	dbCommandTextList	= new HashMap<String, DbCommandText>();

	public static synchronized DbCommandText getDbCommandText(String sqlText) {
		if (!dbCommandTextList.containsKey(sqlText)) {
			List<String> words = extractWord(sqlText, "#");
			List<String> wordsIn = extractWord(sqlText, "$");
			for (String string : wordsIn) {
				words.add(string);
			}
			dbCommandTextList.put(sqlText, new DbCommandText(
					sqlText.replaceAll("#[A-Za-z0-9_]*#", "?"), words));

		}
		return dbCommandTextList.get(sqlText);
	}

	/**
	 * 通过正则表达式从SqlText中取#column#中的column
	 * @param inStr
	 * @param keyChar
	 * @return
	 */
	private static List<String> extractWord(String sqlText, String keyChar) {
		List<String> words = new ArrayList<String>();
		Pattern pattern = Pattern.compile("\\" + keyChar + "[A-Za-z0-9_]*" + "\\" + keyChar);
		Matcher matcher = pattern.matcher(sqlText);
		while (matcher.find()) {
			words.add(matcher.group().replace(keyChar, ""));
		}
		return words;
	}
}
