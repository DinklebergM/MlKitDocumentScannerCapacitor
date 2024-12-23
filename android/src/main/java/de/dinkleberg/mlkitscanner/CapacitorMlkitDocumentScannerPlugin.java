package de.dinkleberg.mlkitscanner;

import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import com.google.android.gms.mlkit.scanner.document.GmsDocumentScanner;
import com.google.android.gms.mlkit.scanner.document.GmsDocumentScannerOptions;
import com.google.android.gms.mlkit.scanner.document.GmsDocumentScanning;
import com.google.android.gms.mlkit.scanner.document.GmsDocumentScanningResult;
import com.google.android.gms.mlkit.scanner.document.Page;
import com.google.android.gms.mlkit.scanner.document.Pdf;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

@CapacitorPlugin(name = "CapacitorMlkitDocumentScanner")
public class CapacitorMlkitDocumentScannerPlugin extends Plugin {

    // Activity Result Launcher, um nach dem Scan die Ergebnisse zurückzubekommen
    private ActivityResultLauncher<Intent> scannerLauncher;

    @Override
    public void load() {
        super.load();

        // Registriere einen ActivityResultLauncher, um das Ergebnis zu erhalten
        scannerLauncher = getActivity().registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                            // Hier verarbeiten wir das Scanning-Ergebnis
                            Intent data = result.getData();
                            if (data != null) {
                                PluginCall savedCall = getSavedCall();
                                if (savedCall == null) {
                                    return;
                                }
                                GmsDocumentScanningResult scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(data);
                                if (scanningResult != null) {
                                    handleScanResult(scanningResult, savedCall);
                                } else {
                                    savedCall.reject("No scanning result found.");
                                }
                            }
                        } else {
                            PluginCall savedCall = getSavedCall();
                            if (savedCall != null) {
                                savedCall.reject("User cancelled or no result.");
                            }
                        }
                    }
                }
        );
    }

    // Methode zum Aufruf aus dem JavaScript
    @PluginMethod
    public void startScan(PluginCall call) {
        // 1. Check: Android-Version >= 21?
        if (Build.VERSION.SDK_INT < 21) {
            call.reject("Android version too low");
            return;
        }

        // 2. Check: RAM >= 1.7GB? (Hier nur als Beispiel, real check is more complicated)
        // Du kannst das Plugin gern so anpassen, dass Du z.B. (Runtime.getRuntime().maxMemory() / 1024 / 1024) etc. prüfst.
        // Nur ein grobes Beispiel:
        long maxMemory = Runtime.getRuntime().maxMemory();
        if (maxMemory < 1700L * 1024L * 1024L) {
            call.reject("Device not supported. At least 1.7GB RAM required.");
            return;
        }

        // 3. Parameter aus dem JS holen
        //    z.B.: pageLimit, galleryImportAllowed, scannerMode, resultFormats, ...
        //    call.getInt("pageLimit") => ...
        int pageLimit = call.getInt("pageLimit", 0);
        boolean galleryImportAllowed = call.getBoolean("galleryImportAllowed", true);
        String scannerMode = call.getString("scannerMode", "SCANNER_MODE_FULL");
        // resultFormats z. B. als Array?
        // => call.getArray("resultFormats") ...
        // (Fürs Beispiel tun wir so, als sei es ein simpler String "JPEG_PDF")

        // 4. GmsDocumentScannerOptions definieren
        GmsDocumentScannerOptions.Builder builder = new GmsDocumentScannerOptions.Builder();

        // galleryImport
        builder.setGalleryImportAllowed(galleryImportAllowed);

        // pageLimit
        if (pageLimit > 0) {
            builder.setPageLimit(pageLimit);
        }

        // resultFormats - hier exemplarisch
        // builder.setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
        //                          GmsDocumentScannerOptions.RESULT_FORMAT_PDF);
        // oder flexible Implementierung anhand JS-Array

        // scannerMode
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

        GmsDocumentScannerOptions options = builder.build();

        GmsDocumentScanner scannerClient = GmsDocumentScanning.getClient(options);

        // 5. Intent holen und starten
        scannerClient.getStartScanIntent(getActivity())
            .addOnSuccessListener(new OnSuccessListener<android.content.IntentSender>() {
                @Override
                public void onSuccess(android.content.IntentSender intentSender) {
                    saveCall(call); // Merke den PluginCall, damit wir nach Rückkehr die Ergebnisse zurückgeben können
                    try {
                        Intent i = new Intent(getContext(), getActivity().getClass());
                        i.setAction("android.intent.action.MAIN");
                        i.addCategory("android.intent.category.LAUNCHER");

                        // IntentSender aufrufen
                        getActivity().startIntentSenderForResult(
                                intentSender,
                                9876,
                                null,
                                0,
                                0,
                                0,
                                null
                        );
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
        // Bereite die Rückgabe fürs JS auf
        JSObject resultObj = new JSObject();

        // Alle Seiten durchgehen
        List<JSObject> pagesArray = new ArrayList<>();
        for (Page page : scanningResult.getPages()) {
            JSObject pageObj = new JSObject();
            pageObj.put("imageUri", page.getImageUri().toString());
            pagesArray.add(pageObj);
        }
        resultObj.put("pages", pagesArray);

        // PDF, falls gewünscht
        Pdf pdf = scanningResult.getPdf();
        if (pdf != null && pdf.getUri() != null) {
            JSObject pdfObj = new JSObject();
            pdfObj.put("pdfUri", pdf.getUri().toString());
            pdfObj.put("pageCount", pdf.getPageCount());
            resultObj.put("pdf", pdfObj);
        }

        call.resolve(resultObj);
    }
}
