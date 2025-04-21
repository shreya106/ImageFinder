package com.webtools.imagefinder.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.util.logging.Logger;

import com.webtools.imagefinder.ImageFinder;

/*
 * Handles Database connection
 */

public class DatabaseManager {

	private static String databaseUrl;
	private static String username;
	private static String password;
	private static final Properties configProperties = new Properties();
	private static final Properties queryProperties = new Properties();
	private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());

	static {
		loadProperties();
	}

	private static void loadProperties() {
		try (FileInputStream configInput = new FileInputStream("src/main/resources/config.properties");
				FileInputStream queryInput = new FileInputStream("src/main/resources/query.properties")) {

			configProperties.load(configInput);
			queryProperties.load(queryInput);

			databaseUrl = configProperties.getProperty("db.url");
			username = configProperties.getProperty("db.username");
			password = configProperties.getProperty("db.password");

		} catch (IOException e) {
			logger.severe("Failed to load database details" + e.toString());
		}
	}

	public static Connection getConnection() throws Exception {
		if (databaseUrl == null || username == null || password == null) {
			throw new IllegalStateException("Database credentials are not set properly.");
		}

		Class.forName(configProperties.getProperty("db.driver"));
		return DriverManager.getConnection(databaseUrl, username, password);
	}

	public static void saveImageUrl(String inputUrl, String imageUrl) {
		String query = queryProperties.getProperty("INSERT_IMAGE_URL");

		if (query == null || query.isEmpty()) {
			throw new IllegalStateException("Query is missing in query.properties");
		}

		try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setString(1, inputUrl);
			stmt.setString(2, imageUrl);
			stmt.executeUpdate();

			logger.info("Image URL saved successfully!");

		} catch (Exception e) {
			logger.severe("Error saving image URL: " + e);
		}
	}
}
