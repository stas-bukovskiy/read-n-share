package com.readnshare.itemfinder.googlebooks.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.readnshare.itemfinder.googlebooks.domain.BookData;
import com.readnshare.itemfinder.googlebooks.domain.BookSearchData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookSearchDataDeserializer extends JsonDeserializer<BookSearchData> {

    @Override
    public BookSearchData deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode itemsNode = node.get("items");

        List<BookData> bookDataList = new ArrayList<>();
        if (itemsNode != null && itemsNode.isArray()) {
            for (JsonNode itemNode : itemsNode) {
                BookData bookData = jp.getCodec().treeToValue(itemNode, BookData.class);
                bookDataList.add(bookData);
            }
        }

        BookSearchData searchData = new BookSearchData();
        searchData.setResults(bookDataList);
        return searchData;
    }
}