package com.webtools.imagefinder.extractor;

import java.util.Set;

import org.jsoup.nodes.Document;

/*
 * Interface for extracting image URLs from an HTML document
 */
public interface ImageExtractor {
	Set<String> extractImageUrls(Document document);
	
}
