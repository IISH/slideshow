package org.iish.slideshow.service;

import org.iish.slideshow.configuration.Blacklist;
import org.marc4j.marc.Record;

import java.util.Arrays;
import java.util.List;

public class RecordHolder {
    private static final List<String> FORMATS = Arrays.asList(
            "poster", "photo", "print", "drawing", "half-tone photo", "picture postcard", "button", "badge", "sticker",
            "small printed matter", "newspaper poster", "design", "object", "painting", "montage", "flag", "banner",
            "textile", "comic strip", "photocopy", "calendar", "digital photo", "map", "admission ticket", "leaflet",
            "charter", "paper money", "sheet of stickers", "plaquette", "medal", "cutting", "game");

    private final RandomRecord randomRecord;
    private Record curRecord;

    public RecordHolder(String apiUrl, Blacklist blacklist) {
        this.randomRecord = new RandomRecord(apiUrl, FORMATS, blacklist);
    }

    public Record getCurRecord() {
        try {
            curRecord = randomRecord.getRandomRecord();
            return curRecord;
        } catch (Exception e) {
            if (curRecord != null) {
                return curRecord;
            }
            return getCurRecord();
        }
    }
}
