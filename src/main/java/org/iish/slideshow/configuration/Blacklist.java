package org.iish.slideshow.configuration;

import java.util.List;

public class Blacklist {
    private List<String> barcodes;
    private List<String> organizations;

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
