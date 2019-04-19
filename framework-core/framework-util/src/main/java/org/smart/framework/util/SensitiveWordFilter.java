package org.smart.framework.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensitiveWordFilter {
	private static Logger logger = LoggerFactory.getLogger(SensitiveWordFilter.class);
	@SuppressWarnings("rawtypes")
	private static Map sensitiveWordMap = new HashMap();
	public static int minMatchTYpe = 1;      //最小匹配规则
	public static int maxMatchType = 2;      //最大匹配规则

	private static final String replaceStr = "*";
	static final char[] specials =
		{' ' };

	static final char space = ' ';


	/**
	 * 构造函数，初始化敏感词库
	 */
	private SensitiveWordFilter(){
	}

	/**
	 * 判断文字是否包含敏感字符
	 * @param txt  文字
	 * @param matchType  匹配规则&nbsp;1：最小匹配规则，2：最大匹配规则
	 * @return 若包含返回true，否则返回false
	 */
	public static boolean isContainsSensitiveWord(String txt,int matchType){
		boolean flag = false;
		for(int i = 0 ; i < txt.length() ; i++){
			int matchFlag = checkSensitiveWord(txt, i, matchType); //判断是否包含敏感字符
			if(matchFlag > 0){    //大于0存在，返回true
				flag = true;
			}
		}
		return flag;
	}

	public static boolean isContainsSensitiveWord(String txt){
		return isContainsSensitiveWord(txt,maxMatchType);
	}

	/**
	 * 获取文字中的敏感词
	 * @param txt 文字
	 * @param matchType 匹配规则&nbsp;1：最小匹配规则，2：最大匹配规则
	 * @return
	 */
	public static Set<String> getSensitiveWord(String txt , int matchType){
		Set<String> sensitiveWordList = new HashSet<String>();

		for(int i = 0 ; i < txt.length() ; i++){
			int length = checkSensitiveWord(txt, i, matchType);    //判断是否包含敏感字符
			if(length > 0){    //存在,加入list中
				sensitiveWordList.add(txt.substring(i, i+length));
				i = i + length - 1;    //减1的原因，是因为for会自增
			}
		}

		return sensitiveWordList;
	}

	public static Set<String> getSensitiveWord(String txt){
		return getSensitiveWord(txt,minMatchTYpe);
	}

	/**
	 * 替换敏感字字符
	 * @param txt
	 * @param matchType
	 * @param replaceChar 替换字符，默认*
	 */
	public static String replaceSensitiveWord(String txt,int matchType,String replaceChar){
		String resultTxt = txt;
		Set<String> set = getSensitiveWord(txt, matchType);     //获取所有的敏感词
		Iterator<String> iterator = set.iterator();
		String word = null;
		String replaceString = null;
		while (iterator.hasNext()) {
			word = iterator.next();
			replaceString = getReplaceChars(replaceChar, word.length());
			resultTxt = resultTxt.replaceAll(word, replaceString);
		}

		return resultTxt;
	}

	public static String replaceSensitiveWord(String txt,String replaceChar){
		return replaceSensitiveWord(txt,minMatchTYpe,replaceChar);
	}

	public static String replaceSensitiveWord(String txt){
		return replaceSensitiveWord(txt,minMatchTYpe,replaceStr);
	}


	/**
	 * 获取替换字符串
	 * @param replaceChar
	 * @param length
	 * @return
	 */
	private static String getReplaceChars(String replaceChar,int length){
		String resultReplace = replaceChar;
		for(int i = 1 ; i < length ; i++){
			resultReplace += replaceChar;
		}

		return resultReplace;
	}

	/**
	 * 检查文字中是否包含敏感字符，检查规则如下：<br>
	 * @param txt
	 * @param beginIndex
	 * @param matchType
	 * @return，如果存在，则返回敏感词字符的长度，不存在返回0
	 */
	@SuppressWarnings({ "rawtypes"})
	public static int checkSensitiveWord(String txt, int beginIndex, int matchType){
		boolean  flag = false;    //敏感词结束标识位：用于敏感词只有1位的情况
		int matchFlag = 0;     //匹配标识数默认为0
		char word = 0;
		Map nowMap = sensitiveWordMap;

		for(int i = beginIndex; i < txt.length() ; i++){
			word = txt.charAt(i);
			if(matchFlag >= 1 && word == space){
				matchFlag++;
				continue;
			}
			nowMap = (Map) nowMap.get(word);     //获取指定key
			if(nowMap != null){     //存在，则判断是否为最后一个
				matchFlag++;     //找到相应key，匹配标识+1
				if("1".equals(nowMap.get("isEnd"))){       //如果为最后一个匹配规则,结束循环，返回匹配标识数
					flag = true;       //结束标志位为true
					if(SensitiveWordFilter.minMatchTYpe == matchType){    //最小规则，直接返回,最大规则还需继续查找
						break;
					}
				}
			}
			else{     //不存在，直接返回
				break;
			}
		}
		if(matchFlag < 1 || !flag){        //长度必须大于等于1，为词
			matchFlag = 0;
		}
		return matchFlag;
	}

//	private static boolean hasSpecial(char word){
//		boolean has = false;
//		for(char c : specials) {
//			if (word == c) {
//				has = true;
//				break;
//			}
//		}
//		return has;
//	}

	public static int checkSensitiveWord(String txt, int beginIndex){
		return checkSensitiveWord(txt,beginIndex,minMatchTYpe);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addSensitiveWordToHashMap(Set<String> keyWordSet) {
		sensitiveWordMap = new HashMap(keyWordSet.size());
		String key = null;
		Map nowMap = null;
		Map<String, String> newWorMap = null;
		//迭代keyWordSet
		Iterator<String> iterator = keyWordSet.iterator();
		while(iterator.hasNext()){
			key = iterator.next();    //关键字
			nowMap = sensitiveWordMap;
			for(int i = 0 ; i < key.length() ; i++){
				char keyChar = key.charAt(i);       //转换成char型
				Object wordMap = nowMap.get(keyChar);       //获取

				if(wordMap != null){        //如果存在该key，直接赋值
					nowMap = (Map) wordMap;
				}
				else{     //不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
					newWorMap = new HashMap<String,String>();
					newWorMap.put("isEnd", "0");     //不是最后一个
					nowMap.put(keyChar, newWorMap);
					nowMap = newWorMap;
				}

				if(i == key.length() - 1){
					nowMap.put("isEnd", "1");    //最后一个
				}
			}
		}
	}


	private static Set<String> readSensitiveWordFile(String words,String split) {
		Set<String> set = new HashSet<>();
		String[] all = words.split(split);
		for (String string : all) {
			String tmp = string.trim();
			if (!tmp.isEmpty()){
				set.add(tmp);
			}
		}
		return set;
	}
	
	public static void init(String words,String split) {
		Set<String> keyWordSet = readSensitiveWordFile(words,split);
		addSensitiveWordToHashMap(keyWordSet);
		logger.debug("word filter complete, word size:{}", keyWordSet.size());
	}
	


}
