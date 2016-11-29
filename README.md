# SmartDisplay

To compile and get this code running (following this tutorial https://developers.google.com/google-apps/calendar/quickstart/android):

First acquire an SHA1 fingerprint with this KeyTool utility (https://developer.android.com/studio/publish/index.html) command:

keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore -list -v

Keystore password is 'android'.

Copy the SHA1 fingerprint.

Then use this wizard: https://console.developers.google.com/flows/enableapi?apiid=calendar

1. Click Continue, then Go to credentials.
2. On the Add credentials to your project page, click the Cancel button.
3. At the top of the page, select the OAuth consent screen tab. Select an Email address, enter a Product name if not already set, and click the Save button.
4. Select the Credentials tab, click the Create credentials button and select OAuth client ID.
5. Select the application type Android.
6. Copy the SHA1 fingerprint from Step 1 into the Signing-certificate fingerprint field.
7. In the Package name field, enter com.example.dianaalgma.smartdisplay.
8. Click the Create button.
