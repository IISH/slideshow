package org.iish.slideshow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.iish.slideshow.configuration.Config;
import org.iish.slideshow.web.SlideshowApi;

import java.io.FileInputStream;
import java.io.InputStream;

import static io.javalin.apibuilder.ApiBuilder.get;

public class Application {
    public static void main(String[] args) throws Exception {
        final InputStream configStream = System.getProperty("config") != null
                ? new FileInputStream(System.getProperty("config"))
                : Application.class.getClassLoader().getResourceAsStream("config.yaml");

        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final Config config = mapper.readValue(configStream, Config.class);

        final SlideshowApi slideshowApi = new SlideshowApi(config);
        final Javalin app = Javalin.create(cfg -> {
            cfg.showJavalinBanner = false;
            cfg.addStaticFiles("/static", Location.CLASSPATH);
        }).start(Integer.parseInt(System.getProperty("port", "8080")));

        app.routes(() -> {
            get("/timeout", context -> context.result(String.valueOf(config.timeout())));
            get("/nextSlide", context -> context.json(slideshowApi.nextSlide()));
            get("/image", context -> {
                context.contentType("image/jpeg");
                context.result(slideshowApi.image(context.queryParam("barcode")));
            });
        });
    }
}
