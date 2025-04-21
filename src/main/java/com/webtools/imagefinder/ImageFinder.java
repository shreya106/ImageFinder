package com.webtools.imagefinder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.webtools.imagefinder.database.DatabaseManager;
import com.webtools.imagefinder.extractor.BackgroundImageExtractor;
import com.webtools.imagefinder.extractor.ImageExtractor;
import com.webtools.imagefinder.extractor.ImgTagExtractor;
import com.webtools.imagefinder.scanner.ImageURLScanner;

/*
 * 
Handles HTTP requests to extract image URLs from a given webpage

*/

@WebServlet(name = "ImageFinder", urlPatterns = { "/main" })
public class ImageFinder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected static final Gson GSON = new GsonBuilder().create();
	private static final Logger logger = Logger.getLogger(ImageFinder.class.getName());
	private final ImageURLScanner scanner;
	private final DatabaseManager databaseManager;

	public ImageFinder() {
		List<ImageExtractor> extractors = Arrays.asList(new ImgTagExtractor(), new BackgroundImageExtractor());
		this.scanner = new ImageURLScanner(extractors);
		this.databaseManager = new DatabaseManager();
	}

	public ImageFinder(ImageURLScanner mockScanner) {
		this.scanner = mockScanner;
		this.databaseManager = new DatabaseManager();
		List<ImageExtractor> extractors = Arrays.asList(new ImgTagExtractor(), new BackgroundImageExtractor());
	}

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");

		String url = req.getParameter("url");

		if (url == null || url.trim().isEmpty() || !isValidURL(url)) {
			resp.getWriter().print(GSON.toJson(new String[] { "Error: Invalid URL provided" }));
			return;
		}

		if (!scanner.isCrawlingAllowed(url)) {
			resp.getWriter().print(GSON.toJson(new String[] { "Crawling is not allowed for this URL" }));
			return;
		}

		if (scanner.isUrlAlreadyVisited(url)) {
			resp.getWriter().print(GSON.toJson(new String[] { "URL already visited" }));
			return;
		}

		try {
			Set<String> imageUrls = scanner.getURLs(url);
			Set<String> iconUrls = scanner.getIconUrls();

			if (imageUrls.isEmpty()) {
				resp.getWriter().print(GSON.toJson(new String[] { "No images found" }));
				return;
			}

			JsonObject jsonResponse = new JsonObject();
			jsonResponse.add("images", GSON.toJsonTree(imageUrls));
			jsonResponse.add("icons", GSON.toJsonTree(iconUrls));

			resp.getWriter().print(GSON.toJson(jsonResponse));
		} catch (IOException e) {
			resp.getWriter()
					.print(GSON.toJson(new String[] { "Error: Unable to fetch images due to site restrictions" }));
		} catch (Exception e) {
			resp.getWriter().print(GSON.toJson(new String[] { "Error: An unexpected error occurred" }));
		}
	}

	private boolean isValidURL(String url) {
		try {
			new java.net.URL(url).toURI();
			return true;
		} catch (Exception e) {
			logger.severe("Failed to check if the url is valid " + e.toString());
			return false;
		}
	}

}
