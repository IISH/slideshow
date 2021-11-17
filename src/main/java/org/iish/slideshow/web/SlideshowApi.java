package org.iish.slideshow.web;

import org.iish.slideshow.configuration.Config;
import org.iish.slideshow.service.RecordExtractor;
import org.iish.slideshow.service.RecordHolder;
import org.marc4j.marc.Record;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class SlideshowApi {
    private final RecordHolder recordHolder;

    public SlideshowApi(Config config) {
        recordHolder = new RecordHolder(config.getApi(), config.getBlacklist());
    }

    public Slide nextSlide() {
        Record record = recordHolder.getCurRecord();
        RecordExtractor recordExtractor = new RecordExtractor(record);

        return new Slide(recordExtractor.getImageBarcode(), recordExtractor.getMetadata());
    }

    public InputStream image(String barcode) {
        try {
            final URL url = new URL("https://hdl.handle.net/10622/" +
                    URLEncoder.encode(barcode, StandardCharsets.UTF_8)
                    + "?locatt=view:level1");

            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);

            return connection.getInputStream();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    private static final class Slide {
        public String barcode;
        public Map<String, List<String>> metadata;

        public Slide(String barcode, Map<String, List<String>> metadata) {
            this.barcode = barcode;
            this.metadata = metadata;
        }
    }
}
