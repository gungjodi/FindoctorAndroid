package com.project.ta.findoctor.Utils;


import com.project.ta.findoctor.Models.WaktuPraktikModel;

import java.util.Comparator;

/**
 * Created by mac on 2/11/17.
 */

public class IdHariSorter implements Comparator<WaktuPraktikModel> {

    @Override
    public int compare(WaktuPraktikModel one, WaktuPraktikModel another) {
        int returnVal = 0;

        if(one.id_hari < another.id_hari){
            returnVal =  -1;
        }else if(one.id_hari > another.id_hari){
            returnVal =  1;
        }else if(one.id_hari == another.id_hari){
            returnVal =  0;
        }
        return returnVal;
    }
}
