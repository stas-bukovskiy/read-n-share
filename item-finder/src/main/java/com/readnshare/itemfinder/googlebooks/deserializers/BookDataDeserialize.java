package com.readnshare.itemfinder.googlebooks.deserializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.readnshare.itemfinder.googlebooks.domain.BookData;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BookDataDeserialize extends JsonDeserializer<BookData> {

    @Override
    public BookData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);

        String id = node.get("id") == null ? null : node.get("id").asText();
        JsonNode volumeInfoNode = node.get("volumeInfo");
        if (volumeInfoNode != null) {
            int pageCount = volumeInfoNode.get("pageCount") == null ? 0 : volumeInfoNode.get("pageCount").asInt(-1);
            double averageRating = volumeInfoNode.get("averageRating") == null ? 0 : volumeInfoNode.get("averageRating").asDouble(-1);
            int ratingsCount = volumeInfoNode.get("ratingsCount") == null ? 0 : volumeInfoNode.get("ratingsCount").asInt(-1);
            return new BookData(
                    id,
                    volumeInfoNode.get("title") == null ? "" : volumeInfoNode.get("title").asText(),
                    volumeInfoNode.get("subtitle") == null ? "" : volumeInfoNode.get("subtitle").asText(),
                    volumeInfoNode.get("authors") == null ? List.of() : p.getCodec().readValue(volumeInfoNode.get("authors").traverse(), new TypeReference<List<String>>() {
                    }),
                    volumeInfoNode.get("publishedDate") == null ? "" : volumeInfoNode.get("publishedDate").asText(),
                    volumeInfoNode.get("categories") == null ? List.of() : p.getCodec().readValue(volumeInfoNode.get("categories").traverse(), new TypeReference<List<String>>() {
                    }),
                    volumeInfoNode.get("imageLinks") == null ? Map.of() : p.getCodec().readValue(volumeInfoNode.get("imageLinks").traverse(), new TypeReference<Map<String, String>>() {
                    }),
                    volumeInfoNode.get("description") == null ? "" : volumeInfoNode.get("description").asText(),
                    pageCount,
                    averageRating,
                    ratingsCount,
                    volumeInfoNode.get("language") == null ? "" : volumeInfoNode.get("language").asText()
            );
        }
        return null;
    }
}