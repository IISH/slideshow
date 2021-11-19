package org.iish.slideshow.configuration;

import java.util.List;

public record Blacklist(List<String> barcodes, List<String> organizations) {
}
