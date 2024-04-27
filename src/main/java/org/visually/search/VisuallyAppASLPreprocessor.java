package org.visually.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.transformer.HtmlTextExtractor;

import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import dev.langchain4j.service.AiServices;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.visually.search.config.ConfigLoader.getProperty;

public class VisuallyAppASLPreprocessor {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<SignLanguageEntry> entries = mapper.readValue(new File(getProperty("startasl.input.path")),
                    new TypeReference<>() {
                    });
            List<SignLanguageEntry> updatedEntries = entries.stream()
                    .map(entry -> new SignLanguageEntry(entry.url(), entry.title(), entry.description(),
                            extract_video_url(entry)))
                    .collect(Collectors.toList());

            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            mapper.writeValue(new File(getProperty("startasl.output.path")), updatedEntries);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extract_video_url(SignLanguageEntry entry) {
        Document document = UrlDocumentLoader.load(
                entry.url(),
                new TextDocumentParser());

        Document transformedDocument = new HtmlTextExtractor(".video-container", null, true)
                .transform(document);

        System.out.println(transformedDocument);

        var model = VertexAiGeminiChatModel.builder()
                .project(getProperty("googlecloud.projectid"))
                .location(getProperty("googlecloud.location"))
                .modelName(getProperty("googlecloud.vertexai.modelname"))
                .build();

//        var model = OpenAiChatModel.withApiKey(getProperty("assistant.openai.apikey"));

        var extractor = AiServices.builder(Extractor.class)
                .chatLanguageModel(model)
                .build();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return extractor.extractVideoUrl(transformedDocument.text());
    }
}
