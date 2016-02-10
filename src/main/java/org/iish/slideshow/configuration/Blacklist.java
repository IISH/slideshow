package org.iish.slideshow.configuration;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "blacklist")
public class Blacklist {
    private @NotEmpty List<String> barcodes;
    private @NotEmpty List<String> organizations;

    public void setBarcodes(List<String> barcodes) {
        this.barcodes = barcodes;
    }

    public void setOrganizations(List<String> organizations) {
        this.organizations = organizations;
    }

    public List<String> getBarcodes() {
        return barcodes;
    }

    public List<String> getOrganizations() {
        return organizations;
    }
}
