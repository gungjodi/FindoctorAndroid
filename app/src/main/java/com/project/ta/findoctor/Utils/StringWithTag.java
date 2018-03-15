package com.project.ta.findoctor.Utils;

/**
 * Created by GungJodi on 6/18/2017.
 */

public class StringWithTag {
    public String string;
    public long id;

    public StringWithTag(String stringPart, long tagPart) {
        string = stringPart;
        id = tagPart;
    }

    @Override
    public String toString() {
        return string;
    }
}