package com.webtools.imagefinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.webtools.imagefinder.ImageFinder;
import com.webtools.imagefinder.scanner.ImageURLScanner;

public class ImageFinderTest {

	private HttpServletRequest request;
	private HttpServletResponse response;
	private StringWriter sw;
	private ImageURLScanner mockScanner;
	private ImageFinder imageFinder;

	@Before
	public void setUp() throws Exception {
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
		mockScanner = Mockito.mock(ImageURLScanner.class);
		sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		when(response.getWriter()).thenReturn(pw);

		imageFinder = new ImageFinder(mockScanner);
	}

	// Test Valid Image Extraction
	@Test
	public void testValidImageExtraction() throws IOException, ServletException {
		Set<String> mockImages = new HashSet<>();
		mockImages.add("https://www.w3schools.com/images/colorpicker2000.png");
		mockImages.add("https://www.w3schools.com/html/images/13_html_images.png");
		mockImages.add("https://www.w3schools.com/html/img_girl.jpg");
		mockImages.add("https://www.w3schools.com/images/img_fa_up_300.png");
		mockImages.add("https://www.w3schools.com/html/pic_trulli.jpg");
		mockImages.add("https://www.w3schools.com/signup/lynxlogo.svg");
		mockImages.add("https://www.w3schools.com/html/images/yt_logo_rgb_dark.png");
		mockImages.add("https://www.w3schools.com/html/img_chania.jpg");

		when(request.getParameter("url")).thenReturn("https://www.w3schools.com/html/html_images.asp");
		when(mockScanner.isCrawlingAllowed("https://www.w3schools.com/html/html_images.asp")).thenReturn(true);
		when(mockScanner.getURLs("https://www.w3schools.com/html/html_images.asp")).thenReturn(mockImages);
		when(mockScanner.getIconUrls()).thenReturn(new HashSet<>()); // No icons found

		imageFinder.doPost(request, response);

		JsonObject expectedJson = new JsonObject();
		expectedJson.add("images", new Gson().toJsonTree(mockImages));
		expectedJson.add("icons", new Gson().toJsonTree(new HashSet<>()));

		// Validate JSON output
		assertEquals(expectedJson.toString(), sw.toString().trim());
	}

	// Empty URL Should Return Empty Set
	@Test
	public void testEmptyUrlShouldReturnEmptySet() throws IOException, ServletException {
		when(request.getParameter("url")).thenReturn("");

		imageFinder.doPost(request, response);
		assertEquals(new Gson().toJson(new String[] { "Error: Invalid URL provided" }), sw.toString().trim());
	}

	// Invalid URL Should Return Empty Set
	@Test
	public void testInvalidUrlShouldReturnEmptySet() throws IOException, ServletException {
		when(request.getParameter("url")).thenReturn("invalid-url");

		imageFinder.doPost(request, response);
		assertEquals(new Gson().toJson(new String[] { "Error: Invalid URL provided" }), sw.toString().trim());
	}

	// Test Crawling Not Allowed
	@Test
	public void testCrawlingNotAllowed() throws IOException, ServletException {
		when(request.getParameter("url")).thenReturn("https://picsum.photos/");
		when(mockScanner.isCrawlingAllowed("https://picsum.photos/")).thenReturn(false);

		imageFinder.doPost(request, response);

		assertEquals(new Gson().toJson(new String[] { "Crawling is not allowed for this URL" }), sw.toString().trim());
	}

	// Integration test for ImageFinder.doPost()
	@Test
	public void testRealImageExtraction() throws IOException, ServletException {
		ImageFinder realImageFinder = new ImageFinder();

		when(request.getParameter("url")).thenReturn("https://www.w3schools.com/html/html_images.asp");

		realImageFinder.doPost(request, response);

		String responseOutput = sw.toString().trim();
		assertTrue(responseOutput.contains("https://www.w3schools.com/images/colorpicker2000.png"));
		assertTrue(responseOutput.contains("https://www.w3schools.com/html/images/13_html_images.png"));
		assertTrue(responseOutput.contains("https://www.w3schools.com/html/img_girl.jpg"));
		assertTrue(responseOutput.contains("https://www.w3schools.com/images/img_fa_up_300.png"));
		assertTrue(responseOutput.contains("https://www.w3schools.com/html/pic_trulli.jpg"));
		assertTrue(responseOutput.contains("https://www.w3schools.com/signup/lynxlogo.svg"));
		assertTrue(responseOutput.contains("https://www.w3schools.com/html/images/yt_logo_rgb_dark.png"));
		assertTrue(responseOutput.contains("https://www.w3schools.com/html/img_chania.jpg"));

	}

	// Test for 403 error
	@Test
	public void testHttpErrorHandling() throws IOException, ServletException {
		when(request.getParameter("url")).thenReturn("https://example.com/protected");

		when(mockScanner.isCrawlingAllowed("https://example.com/protected")).thenReturn(true);
		when(mockScanner.getURLs("https://example.com/protected")).thenThrow(new IOException("403 Forbidden"));

		imageFinder.doPost(request, response);

		assertEquals(new Gson().toJson(new String[] { "Error: Unable to fetch images due to site restrictions" }),
				sw.toString().trim());
	}

}
