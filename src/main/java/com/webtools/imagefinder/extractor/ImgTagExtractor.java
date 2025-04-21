package com.webtools.imagefinder.extractor;

import java.util.HashSet;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/*
 * Extracts image URLs from <img> tags in an HTML document.
 */
public class ImgTagExtractor implements ImageExtractor {

	// Extracts all image URLs from <img> tags within the given document
	@Override
	public Set<String> extractImageUrls(Document document) {
		Set<String> imageUrls = new HashSet<>();
		Elements imgTags = document.select("img");

		imgTags.stream().map(img -> img.absUrl("src")).filter(imgUrl -> !imgUrl.isEmpty()).forEach(imgUrl -> {
			synchronized (imageUrls) {
				imageUrls.add(imgUrl);
			}
		});
		return imageUrls;
	}

}
