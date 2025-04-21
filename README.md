Image Finder

Features Implemented

1. Built a web crawler that can find all images on the web page(s) that it crawls.

2. Crawls sub-pages to find more images within the same domain.

3. Implements multi-threading to efficiently crawl multiple sub-pages simultaneously.

4. Ensures the crawler stays within the same domain as the input URL.

5. Avoids re-crawling pages that have already been visited.

6. Implements friendly crawling techniques to prevent bans from websites.

7. Detects and categorizes logo images separately.

8. Developed an engaging front-end using JavaScript, HTML, and CSS.

9. Detects and prints logos separately from other images.

10. Added logging functionality for better debugging and monitoring.

11. Integrated MySQL to store extracted image URLs.

12. Configurable settings stored in config.properties.

13. SQL queries managed using query.properties.

SETUP INSTRUCTIONS:


-> mvn package

If the build is successful, you should see "BUILD SUCCESS" in the terminal.

To clean the project (optional):

-> mvn clean

Run the project using Jetty server:

-> mvn clean test package jetty:run

Once the server starts, open your browser and navigate to:

http://localhost:8080

You can now enter a website URL in the input box to fetch and display image URLs!
