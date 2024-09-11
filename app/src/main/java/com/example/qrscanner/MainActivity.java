package com.example.qrscanner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.annotation.NonNull;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    Button btn_scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                scanCode();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            }
        });
    }

    private void scanCode() {
//        ScanOptions options = new ScanOptions();
//        options.setPrompt("Volume up to flash on");
//        options.setBeepEnabled(true);
//        options.setOrientationLocked(true);
//        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE); // Focus on QR Codes only
//        barLauncher.launch(options);

        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE); // Focus on QR Codes only
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String qrContent = result.getContents();
            Log.d("QR_SCAN_RESULT", "Scanned content: " + qrContent); // Debug log
            if (qrContent.contains("upi://pay")) {
                // Handle UPI QR Code
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("UPI QR Code Detected");
                builder.setMessage(parseUPICode(qrContent)); // Parse the UPI QR Code
                builder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss()).show();
            } else {
                // Handle non-UPI QR Codes
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Result");
                builder.setMessage(qrContent); // Display the content of the QR code
                builder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss()).show();
            }
        }
    });

    private String parseUPICode(String qrContent) {
        // Parse the UPI QR Code content
        Uri uri = Uri.parse(qrContent);
        String payeeAddress = uri.getQueryParameter("pa");
        String payeeName = uri.getQueryParameter("pn");
        String transactionId = uri.getQueryParameter("tid");
        String amount = uri.getQueryParameter("am");

        // Build the UPI data for display
        return "Payee: " + payeeName + "\nUPI ID: " + payeeAddress + "\nAmount: " + amount + "\nTransaction ID: " + transactionId;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanCode(); // Re-attempt scanning if permission is granted
            } else {
                // Permission denied, handle accordingly
                new AlertDialog.Builder(this)
                        .setTitle("Permission Denied")
                        .setMessage("Camera permission is required to scan QR codes.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        }
    }
}
