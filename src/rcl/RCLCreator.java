package rcl;
import java.util.ArrayList;
import java.util.List;


public class RCLCreator {
	
	public static AbstractRCLExpression createFromString(String str) {
		
		str = str.trim(); // remove whitespace 
		if (str.charAt(0) == '(') {
			str = str.substring(1, str.length() - 1); // remove brackets
		}
		
		String first = "";
		String second = "";
		boolean split = false;
		for (int i = 0; i < str.length(); i++) {
			if (!split && str.charAt(i) == ':') {
				split = true;
			} else if (split) {
				second += str.charAt(i);
			} else {
				first += str.charAt(i);
			}
		}
		if (split) {
			first = first.trim();
			second = second.trim();
			List<AbstractRCLExpression> subExpressions = new ArrayList<AbstractRCLExpression>();
			int openBrackets = 0;
			String temp = "";
			for (int i = 0; i < second.length(); i++) {
				temp += second.charAt(i);
				if (second.charAt(i) == '(') {
					openBrackets++;
				} else if (second.charAt(i) == ')') {
					openBrackets--;
					if (openBrackets == 0) {
						subExpressions.add(createFromString(temp));
						temp = "";
					}
				}
			}
			if (temp.trim().length() > 0) {
				subExpressions.add(createFromString(temp));
			}
			
			
			return new RCLExpression(first, subExpressions);
		} else {
			return new RCLLiteral(str);
		}
	}

}
