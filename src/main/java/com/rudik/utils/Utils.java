package com.rudik.utils;

import java.util.List;

public final class Utils {
	//check if string is in list
    public static boolean is_in_list(List<String> myList, String stringToCheck){
        for(String str: myList) {
            if(stringToCheck.contains(str.trim()))
                return true;
        }
        return false;
    }
}
