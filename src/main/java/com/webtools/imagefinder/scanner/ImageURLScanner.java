package com.webtools.imagefinder.scanner;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.webtools.imagefinder.extractor.ImageExtractor;

/*
 * This class is responsible for crawling web pages and extracting image URLs
 */
public class ImageURLScanner {

	private Set<String> imageUrls = new HashSet<String>();
	private Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
	private Set<String> iconUrls = new HashSet<String>();
	private ExecutorService executorService = Executors.newFixedThreadPool(15);
	private final List<Future<?>> futures = new CopyOnWriteArrayList<>();
	private List<ImageExtractor> extractors;
	private static Properties property = new Properties();
	private static final Logger logger = Logger.getLogger(ImageURLScanner.class.getName());
	private Document doc = null;

	static {
		try (FileInputStream input = new FileInputStream("src/main/resources/config.properties")) {
			property.load(input);
		} catch (IOException e) {
			logger.severe("Failed to load the config properties file " + e.toString());
		}
	}

	public ImageURLScanner(List<ImageExtractor> extractors) {
		this.extractors = extractors;
	}

	// Checks if crawling is allowed for a given URL by reading robots.txt
	public boolean isCrawlingAllowed(String url) {
		try {
			String robotsUrl = url + "/robots.txt";
			Document robotsDoc = Jsoup.connect(robotsUrl).ignoreHttpErrors(true).timeout(3000).get();

			String robotsText = robotsDoc.body().text();

			if (robotsText.contains("Disallow: /")) {
				logger.warning("Crawling is disallowed for: " + url);
				return false;
			}

			String[] lines = robotsText.split("\n");
			for (String line : lines) {
				if (line.startsWith("Disallow:")) {
					String disallowedPath = line.split(":")[1].trim();
					if (url.contains(disallowedPath)) {
						logger.info("Disallowed path: " + disallowedPath);
						return false;
					}
				}
			}
		} catch (IOException e) {
			if (e.getMessage().contains("403")) {
				logger.severe("Access to this page is forbidden " + url);
			}
			logger.severe("No robots.txt found or error fetching: " + url);
		}
		return true;
	}

// Recursively crawls web pages to find image URLs
	private Future<?> findAllUrls(String url, int depth) {
		int maxDepth = Integer.parseInt(property.getProperty("DEPTH_OF_CRAWLING"));
		if (depth > maxDepth) {
			logger.warning("depth limit has been reached for " + url);
			return CompletableFuture.completedFuture(null);
		}

		if (visitedUrls.contains(url)) {
			return CompletableFuture.completedFuture(null);
		}
		if (!iconUrls.contains(url)) {
			visitedUrls.add(url);
		}

		Future<?> future = executorService.submit(() -> {
			try {
				logger.info("Crawling: " + url);
				doc = Jsoup.connect(url).userAgent(property.getProperty("USER_AGENT"))
						.referrer(property.getProperty("REFERRER"))
						.header("Accept-Language", property.getProperty("ACCEPT_LANGUAGE"))
						.header("Accept", property.getProperty("ACCEPT"))
						.header("Connection", property.getProperty("CONNECTION"))
						.ignoreHttpErrors(Boolean.parseBoolean(property.getProperty("IGNORE_HTTP_ERRORS")))
						.timeout(Integer.parseInt(property.getProperty("TIMEOUT_MS"))).get();

				iconDetector(doc);

				synchronized (imageUrls) {
					for (ImageExtractor extractor : extractors) {
						imageUrls.addAll(extractor.extractImageUrls(doc));
					}
				}

				doc.select("a[href]").stream().map(link -> link.absUrl("href"))
						.filter(subpageUrl -> subpageUrl.startsWith(url) && !visitedUrls.contains(subpageUrl))
						.limit(Integer.parseInt(property.getProperty("MAX_LINKS_PER_PAGE"))).forEach(subpageUrl -> {
							if (!executorService.isShutdown()) {

								futures.add(findAllUrls(subpageUrl, depth + 1));

							}
						});

				Thread.sleep(50 + (int) (Math.random() * 100));

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (UnsupportedMimeTypeException e) {
				logger.severe("This url contains non-HTML page " + url);
				return;
			} catch (IOException e) {
				logger.severe("Error fetching URL: " + e);
			}
		});
		return future;

	}

	public void iconDetector(Document document) {
		Elements icons = document.select("link[rel~=(?i)icon]");
		for (Element icon : icons) {
			String iconUrl = icon.absUrl("href");
			iconUrls.add(iconUrl);

			logger.info("icon found: " + iconUrl);
		}

	}

	public Set<String> getIconUrls() {
		return iconUrls;
	}

	public boolean isUrlAlreadyVisited(String url) {
		return visitedUrls.contains(url);
	}

	// Starts the crawling process and returns a set of image URLs found
	public Set<String> getURLs(String url) throws IOException {
		if (isCrawlingAllowed(url)) {
			try {
				if (executorService.isShutdown()) {
					executorService = Executors.newFixedThreadPool(15);
				}

				synchronized (imageUrls) {
					imageUrls.clear();
				}

				synchronized (iconUrls) {
					iconUrls.clear();
				}

				futures.clear();

				futures.add(findAllUrls(url, 1));
				if (futures.isEmpty()) {
					return null;
				}

				for (Future<?> future : futures) {
					future.get();
				}

			}

			catch (InterruptedException | ExecutionException e) {
				logger.severe(e.toString());
			} finally {
				executorService.shutdown();
			}

		} else {
			return new HashSet<>();
		}
		return imageUrls;
	}

}