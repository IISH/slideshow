package org.iish.slideshow.web;

import org.iish.slideshow.service.RecordExtractor;
import org.iish.slideshow.service.RecordHolder;
import org.marc4j.marc.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Controller
public class SlideshowController {
    @Value("${slideshow.timeout}") private Integer timeout;
    @Value("${sor.accessToken}") private   String  accessToken;

    @Autowired private RecordHolder recordHolder;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String main(Model model) {
        int timeoutMilliSeconds = -1;
        if ((timeout != null) && (timeout > 0)) {
            timeoutMilliSeconds = timeout * 1000;
        }

        model.addAttribute("timeout", timeoutMilliSeconds);
        return "main";
    }

    @RequestMapping(value = "/nextSlide", method = RequestMethod.GET)
    public
    @ResponseBody
    Slide nextSlide(Model model) {
        Record record = recordHolder.getCurRecord();
        RecordExtractor recordExtractor = new RecordExtractor(record);

        return new Slide(recordExtractor.getImageBarcode(), recordExtractor.getMetadata());
    }

    @RequestMapping(value = "/image", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> image(@RequestParam("barcode") String barcode) {
        try {
            // TODO: "https//hdl.handle.net/10622" + barcode + "?locatt=view:level2" + "&urlappend=?access_token=" + accessToken;
            URL url = new URL("http://disseminate.objectrepository.org/file/level2/10622/" +
                    URLEncoder.encode(barcode, "UTF-8")
                    + "?access_token=" + URLEncoder.encode(accessToken, "UTF-8"));
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(new InputStreamResource(url.openStream()));
        } catch (MalformedURLException murle) {
            murle.printStackTrace();
            return null;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    private final class Slide {
        public String                    barcode;
        public Map<String, List<String>> metadata;

        public Slide(String barcode, Map<String, List<String>> metadata) {
            this.barcode = barcode;
            this.metadata = metadata;
        }
    }
}
