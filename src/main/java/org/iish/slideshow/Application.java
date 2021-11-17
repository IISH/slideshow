package org.iish.slideshow;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.iish.slideshow.configuration.Config;
import org.iish.slideshow.web.SlideshowApi;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.InputStream;

import static io.javalin.apibuilder.ApiBuilder.get;

public class Application {
    public static void main(String[] args) throws Exception {
        final InputStream configStream = System.getProperty("config") != null
                ? new FileInputStream(System.getProperty("config"))
                : Application.class.getClassLoader().getResourceAsStream("config.yaml");

        final Yaml yaml = new Yaml(new Constructor(Config.class));
        final Config config = yaml.load(configStream);

        final SlideshowApi slideshowApi = new SlideshowApi(config);
        final Javalin app = Javalin.create(cfg -> {
            cfg.showJavalinBanner = false;
            cfg.addStaticFiles("/static", Location.CLASSPATH);
        }).start(Integer.parseInt(System.getProperty("port", "8080")));

        app.routes(() -> {
            get("/timeout", context -> context.result(String.valueOf(config.getTimeout())));
            get("/nextSlide", context -> context.json(slideshowApi.nextSlide()));
            get("/image", context -> {
                context.contentType("image/jpeg");
                context.result(slideshowApi.image(context.queryParam("barcode")));
            });
        });
    }
}
