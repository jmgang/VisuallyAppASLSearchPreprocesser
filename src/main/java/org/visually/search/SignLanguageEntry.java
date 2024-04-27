package org.visually.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SignLanguageEntry(String url, String title, String description,
                               @JsonProperty("video_url") String videoUrl){

}
