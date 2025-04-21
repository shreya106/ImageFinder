package com.webtools.imagefinder.extractor;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.webtools.imagefinder.ImageFinder;

/*
 * Extracts image URLs from Background-image attribute 
 */

public class BackgroundImageExtractor implements ImageExtractor {

	private static final Logger logger = Logger.getLogger(BackgroundImageExtractor.class.getName());

	// Extracts a set of image URLs from background-image styles in div elements.
	@Override
	public Set<String> extractImageUrls(Document document) {
		Set<String> imageUrls = new HashSet<>();
		Elements divs = document.select("div[style]");

		divs.stream().map(div -> div.attr("style")).filter(style -> style.contains("background-image"))
				.map(this::extractBackgroundImageUrl).filter(imageUrl -> !imageUrl.isEmpty())
				.map(imageUrl -> resolveUrl(document.baseUri(), imageUrl)).forEach(imgUrl -> {
					synchronized (imageUrls) {
						imageUrls.add(imgUrl);
					}
				});
		return imageUrls;
	}

	// Extracts the URL from a CSS background-image style string
	private String extractBackgroundImageUrl(String style) {
		int start = style.indexOf("url(");
		int end = style.indexOf(")", start);
		if (start != -1 && end != -1) {
			return style.substring(start + 4, end).replace("'", "").replace("\"", "").trim();
		}
		return "";
	}

	// Resolves a relative image URL to an absolute URL using the base URI
	private String resolveUrl(String baseUri, String imageUrl) {
		try {
			URL base = new URL(baseUri);
			return new URL(base, imageUrl).toString();
		} catch (Exception e) {
			logger.severe("Failed to resolve URL from " + imageUrl);
			return imageUrl;
		}
	}

}
