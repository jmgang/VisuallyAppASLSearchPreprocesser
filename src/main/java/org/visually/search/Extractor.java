package org.visually.search;

import dev.langchain4j.service.UserMessage;

public interface Extractor {

    @UserMessage("Given the text, find and extract the video url which usually ends in mp4 file format and is stored in Amazon S3. " +
            "Your only response should be the video url.: {{it}}")
    String extractVideoUrl(String text);
}
