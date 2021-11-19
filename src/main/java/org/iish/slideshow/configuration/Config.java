package org.iish.slideshow.configuration;

public record Config(int timeout, String api, Blacklist blacklist) {
}
