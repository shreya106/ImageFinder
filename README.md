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

**OUTPUT SCREENSHOTS**
<img width="1470" height="956" alt="Screenshot 2026-01-07 at 10 14 23 pm" src="https://github.com/user-attachments/assets/4d96d480-09fa-4e57-8ded-bb02ba5b58d5" />
<img width="1470" height="956" alt="Screenshot 2026-01-07 at 10 17 22 pm" src="https://github.com/user-attachments/assets/8d4db7d0-985b-47f6-b8b7-ad3b0359a502" />
<img width="1470" height="956" alt="Screenshot 2026-01-07 at 10 17 41 pm" src="https://github.com/user-attachments/assets/9ca3aa5e-a93d-4772-8af0-1c9bf89b15c9" />



