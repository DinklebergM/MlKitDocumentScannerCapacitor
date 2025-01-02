package de.dinkleberg.mlkitscanner;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;

import java.util.ArrayList;
import java.util.List;

@CapacitorPlugin(name = "CapacitorMlkitDocumentScanner")
public class CapacitorMlkitDocumentScannerPlugin extends Plugin {

    private ActivityResultLauncher<IntentSenderRequest> scannerLauncher;

    @Override
    public void load() {
        super.load();

        scannerLauncher = getActivity().registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        PluginCall savedCall = getSavedCall();
                        if (savedCall == null) {
                            return;
                        }

                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                GmsDocumentScanningResult scanningResult = GmsDocumentScanningResult
                                        .fromActivityResultIntent(data);
                                if (scanningResult != null) {
                                    handleScanResult(scanningResult, savedCall);
                                } else {
                                    savedCall.reject("No scanning result found.");
                                }
                            } else {
                                savedCall.reject("No data returned from scanner.");
                            }
                        } else {

                            savedCall.reject("User cancelled or no result.");
                        }
                    }
                });
    }

    @PluginMethod
    public void startScan(PluginCall call) {

        int pageLimit = call.getInt("pageLimit", 0);
        boolean galleryImportAllowed = call.getBoolean("galleryImportAllowed", true);
        String scannerMode = call.getString("scannerMode", "SCANNER_MODE_FULL");
        String resultFormat = call.getString("resultFormats", "PDF");

        GmsDocumentScannerOptions.Builder builder = new GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(galleryImportAllowed);

        if (pageLimit > 0) {
            builder.setPageLimit(pageLimit);
        }

        switch (scannerMode) {
            case "SCANNER_MODE_BASE":
                builder.setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE);
                break;
            case "SCANNER_MODE_BASE_WITH_FILTER":
                builder.setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER);
                break;
            case "SCANNER_MODE_FULL":
            default:
                builder.setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL);
                break;
        }

        if ("JPEG".equalsIgnoreCase(resultFormat)) {
            builder.setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG);
        } else {
            builder.setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF);
        }

        GmsDocumentScanner scannerClient = GmsDocumentScanning.getClient(builder.build());

        scannerClient.getStartScanIntent(getActivity())
                .addOnSuccessListener(new OnSuccessListener<android.content.IntentSender>() {
                    @Override
                    public void onSuccess(android.content.IntentSender intentSender) {

                        saveCall(call);

                        try {
                            IntentSenderRequest request = new IntentSenderRequest.Builder(intentSender)
                                    .build();
                            scannerLauncher.launch(request);
                        } catch (Exception e) {
                            call.reject("Failed to start scanner intent: " + e.getMessage(), e);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        call.reject("Failed to get scanner intent: " + e.getMessage(), e);
                    }
                });
    }

    private void handleScanResult(GmsDocumentScanningResult scanningResult, PluginCall call) {
        if (scanningResult == null) {
            call.reject("No scanning result found or result is null.");
            return;
        }

        JSObject resultObject = new JSObject();

        int pageCount = scanningResult.getPages().size();
        resultObject.put("pageCount", pageCount);

        if (scanningResult.getPdf() != null) {
            String pdfPath = scanningResult.getPdf().getUri().toString();
            moveFileToDownloads(pdfPath, "scanned_document.pdf");
            resultObject.put("pdfUri", pdfPath);
        } else {
            List<JSObject> pagesArray = new ArrayList<>();
            for (int i = 0; i < pageCount; i++) {
                String imagePath = scanningResult.getPages().get(i).getImageUri().toString();
                moveFileToDownloads(imagePath, "scanned_page_" + (i + 1) + ".jpg");
                JSObject pageData = new JSObject();
                pageData.put("imageUri", imagePath);
                pagesArray.add(pageData);
            }
            resultObject.put("pages", pagesArray);
        }

        Log.d("CapacitorMlkitDocScan", "Scan result: " + resultObject.toString());

        call.resolve(resultObject);
    }

    private void moveFileToDownloads(String sourcePath, String fileName) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            File sourceFile = new File(sourcePath.replace("file://", ""));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                inputStream = getActivity().getContentResolver().openInputStream(Uri.fromFile(sourceFile));

                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                values.put(MediaStore.MediaColumns.MIME_TYPE, fileName.endsWith(".jpg") ? "image/jpeg" : "application/pdf");

                Uri uri = getActivity().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    outputStream = getActivity().getContentResolver().openOutputStream(uri);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }

                    Log.d("CapacitorMlkitDocScan", "File moved to Downloads: " + uri.toString());
                }
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File destinationFile = new File(downloadsDir, fileName);

                inputStream = getActivity().getContentResolver().openInputStream(Uri.fromFile(sourceFile));
                outputStream = new FileOutputStream(destinationFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                Log.d("CapacitorMlkitDocScan", "File moved to: " + destinationFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e("CapacitorMlkitDocScan", "Failed to move file: " + e.getMessage(), e);
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                Log.e("CapacitorMlkitDocScan", "Failed to close streams: " + e.getMessage(), e);
            }
        }
    }
}
