# Data_Sharing_Android
AIDL and Content Manager example shown.

Example of data passing between different applications.
- By Deepak Kumar Sood

AIDL_Example
1.	SimpleScreenCapture - It is the app which records the screen video using MediaProjection api and records it using MediaManager and saves it in internal storage.
2.	SimpleScreenCapture2 - It is the service running in background which will receive the location of saved video from the 1st application.

ContentManager
1.	MyApplication - This application will save the data in database and has the content manager which will provide permission using Provider to other application.
2.	RetrieveStudents - This will use permission provided by MyApplication and retrieve the students list from the database using content manager and display it.
