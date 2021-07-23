package com.fbu.autonote.utilities;

import java.util.List;
//Helper class
public class GetListOfKeywordsString {
    /**
     * @param keywordsList list object that contains the keywords to be concatenated
     * @param numberOfWords maximum number of words in string
     * @return string with concatenated keywords
     */
    public static String getString(List<String>keywordsList, int numberOfWords) {
        int counter = 0;
        String keywords = new String();
        for (String keyword : keywordsList) {
            keywords += keyword;
            if (counter++ > numberOfWords) {
                keywords += ".";
                break;
            } else {
                keywords += ", ";
            }
        }
        return keywords;
    }
}
