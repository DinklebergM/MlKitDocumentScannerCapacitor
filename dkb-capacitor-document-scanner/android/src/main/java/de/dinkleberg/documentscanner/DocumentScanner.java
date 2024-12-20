package de.dinkleberg.documentscanner;

import android.content.Intent;
import android.net.Uri;
import android.app.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.getcapacitor.Plugin;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.JSObject;

import com.google.mlkit.docs.scanner.GmsDocumentScanning;
import com.google.mlkit.docs.scanner.GmsDocumentScanner;
import com.google.mlkit.docs.scanner.GmsDocumentScannerOptions;
import com.google.mlkit.docs.scanner.GmsDocumentScanningResult;
import com.google.mlkit.docs.scanner.Page;
import com.google.mlkit.docs.scanner.Pdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

@CapacitorPlugin(name = "DocumentScanner")
public class DocumentScannerPlugin extends Plugin {

    private ActivityResultLauncher<IntentSenderRequest> scannerLauncher;
    private PluginCall savedCall;

    @Override
    public void load() {
        super.load();

        Activity activity = getActivity();

        // Registriere einen ActivityResultLauncher, um die Ergebnisse des Document Scans zu empfangen
        scannerLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (savedCall == null) {
                    return;
                }

                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    GmsDocumentScanningResult scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(data);

                    JSObject ret = new JSObject();
                    // Seiten extrahieren
                    for (int i = 0; i < scanningResult.getPages().size(); i++) {
                        Page page = scanningResult.getPages().get(i);
                        Uri imageUri = page.getImageUri();
                        ret.put("imageUri_" + i, imageUri.toString());
                    }

                    Pdf pdf = scanningResult.getPdf();
                    if (pdf != null) {
                        Uri pdfUri = pdf.getUri();
                        int pageCount = pdf.getPageCount();
                        // PDF Infos zum JS zurückgeben
                        ret.put("pdfUri", pdfUri.toString());
                        ret.put("pdfPageCount", pageCount);
                    }

                    savedCall.resolve(ret);
                } else {
                    savedCall.reject("Scan cancelled or failed");
                }
                savedCall = null;
            }
        );
    }

    @PluginMethod
    public void startScan(@NonNull PluginCall call) {
        savedCall = call;

        // Konfiguration des Scanners
        GmsDocumentScannerOptions options = new GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(2)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build();

        GmsDocumentScanner scanner = GmsDocumentScanning.getClient(getActivity(), options);

        scanner.getStartScanIntent(getActivity())
            .addOnSuccessListener(new OnSuccessListener<android.content.IntentSender>() {
                @Override
                public void onSuccess(android.content.IntentSender intentSender) {
                    IntentSenderRequest request = new IntentSenderRequest.Builder(intentSender).build();
                    scannerLauncher.launch(request);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (savedCall != null) {
                        savedCall.reject("Failed to start scanner: " + e.getMessage());
                        savedCall = null;
                    }
                }
            });
    }
}
