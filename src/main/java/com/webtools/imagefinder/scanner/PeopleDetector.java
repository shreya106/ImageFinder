package com.webtools.imagefinder.scanner;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class PeopleDetector {
    
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Load OpenCV
    }

    // Method to check if an image contains people
    public static boolean containsPeople(String imagePath) {
        String modelPath = "src/main/resources/haarcascade_frontalface_alt.xml"; // Path to model file

        // Load the face detection classifier
        CascadeClassifier faceDetector = new CascadeClassifier(modelPath);

        // Read the image
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            System.out.println("Could not read the image file: " + imagePath);
            return false;
        }

        // Convert to grayscale for better accuracy
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Detect faces in the image
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(grayImage, faceDetections);

        // Return true if faces are found (indicating presence of people)
        return faceDetections.toArray().length > 0;
    }

    // Test the method
    public static void main(String[] args) {
        String testImage = "src/main/resources/test.jpg"; // Replace with your image path
        if (containsPeople(testImage)) {
            System.out.println("People detected in the image.");
        } else {
            System.out.println("❌ No people found in the image.");
        }
    }
}
