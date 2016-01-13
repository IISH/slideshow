package org.iish.slideshow.service;

import org.marc4j.marc.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RecordHolder {
    // Removed: album
    private static final List<String> FORMATS = Arrays.asList(
            "poster", "photo", "print", "drawing", "half-tone photo", "picture postcard", "button", "badge", "sticker",
            "small printed matter", "newspaper poster", "design", "object", "painting", "montage", "flag", "banner",
            "textile", "comic strip", "photocopy", "calendar", "digital photo", "map", "admission ticket", "leaflet",
            "charter", "paper money", "sheet of stickers", "plaquette", "medal", "cutting", "game");

    private RandomRecord randomRecord;
    private Record       curRecord;

    @Autowired
    public RecordHolder(@Value("${api.url}") String apiUrl, @Value("${sor.accessToken}") String accessToken) {
        this.randomRecord = new RandomRecord(apiUrl, accessToken, FORMATS);
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
