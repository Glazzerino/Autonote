package com.fbu.autonote.utilities;

import java.util.List;

//Helper class
public class GetListOfKeywordsString {
    /**
     * @param keywordsList  list object that contains the keywords to be concatenated
     * @param numberOfWords maximum number of words in string
     * @return string with concatenated keywords
     */
    public static int MAX = 7;
    public static String getString(List<String> keywordsList, int numberOfWords) {
        String concatenatedKeywords = "";

        for (int i=0; i<numberOfWords; i++) {
            concatenatedKeywords += keywordsList.get(i);
            if (i != numberOfWords-1) {
                concatenatedKeywords += ", ";
            }
        }
        return concatenatedKeywords;
    }
}
