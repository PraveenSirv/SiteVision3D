package com.example.sitevision;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;



public class PlanViewerActivity extends AppCompatActivity {


    WebView pdfWebView;
    ImageView imageView, qrImageView;
    Button btn3DView, btnFullModel;
    String fileName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plan_viewer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imageView = findViewById(R.id.imageView);
        qrImageView = findViewById(R.id.qrImageView);
        btn3DView = findViewById(R.id.btn3DView);
        btnFullModel = findViewById(R.id.btnFullModel);
        pdfWebView = findViewById(R.id.pdfWebView);

        fileName = getIntent().getStringExtra("file_name");
        if (fileName == null) {
            Toast.makeText(this, "No file selected!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(fileName);
        }

        // Setup WebView for model-viewer overlay
//        setupModelWebView();

        // Show the 2D content (PNG or PDF)
        if (fileName.endsWith(".png")) {
            showImageFromAssets(fileName);
        } else if (fileName.endsWith(".pdf")) {
            showPdfFromAssets(fileName);
        } else {
            Toast.makeText(this, "Unsupported file type!", Toast.LENGTH_SHORT).show();
        }
        // Generate QR code for this plan
        generateQrCode(fileName);


        // button: toggle model overlay for this plan
//        btn3DView.setOnClickListener(v -> {
//            String modelPath = map2DFileTo3DModelPath(fileName); // e.g., models/level1.glb
//            toggle3DOverlay(modelPath);
//        });
//
        // button: open full model overlay
//        btnFullModel.setOnClickListener(v -> {
//            String fullModelPath = "models/complete_model.glb";
//            toggle3DOverlay(fullModelPath);
//        });

        btn3DView.setOnClickListener(v -> {
            String modelPath = map2DFileTo3DModelPath(fileName);
            String modelName = fileName.substring(0, fileName.lastIndexOf('.'));
            modelName = modelName.toLowerCase(); // ensure lowercase
            Intent intent = new Intent(PlanViewerActivity.this, ModelViewerActivity.class);
            intent.putExtra("model_name", modelName);
            startActivity(intent);
        });

        btnFullModel.setOnClickListener(v -> {
            Intent intent = new Intent(PlanViewerActivity.this, ModelViewerActivity.class);
            intent.putExtra("model_name", "complete_model"); // direct from res/raw/complete_model.glb
            startActivity(intent);
        });


    }

    private void generateQrCode(String fileName) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            String qrText = "https://sitevision.app/qr/" + fileName; // Example QR content
            int size = 512;
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(qrText, BarcodeFormat.QR_CODE, size, size);


            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            qrImageView.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "QR generation failed", Toast.LENGTH_SHORT).show();
        }


    }


    // Show PNG from assets
    private void showImageFromAssets(String fileName) {
        try {
            AssetManager assetManager = getAssets();

            // Step 1: Just read the dimensions, not the actual bitmap yet
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream boundsStream = assetManager.open(fileName);
            BitmapFactory.decodeStream(boundsStream, null, options);
            boundsStream.close();

            int reqWidth = 1080;   // target width for phone display
            int reqHeight = 1920;  // target height for phone display

            // Step 2: Calculate inSampleSize (how much to shrink)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;

            // Step 3: Decode the actual bitmap with scaling
            InputStream inputStream = assetManager.open(fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // Step 4: Display it safely
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(ImageView.VISIBLE);
            pdfWebView.setVisibility(WebView.GONE);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }


    // Helper: dynamically calculate how much to shrink the image
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            // Keep dividing until it's smaller than requested size
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    // Display PDF directly using PdfRenderer
    private void showPdfFromAssets(String fileName) {
        try {
            imageView.setVisibility(ImageView.VISIBLE);
            pdfWebView.setVisibility(WebView.GONE);

            ParcelFileDescriptor fileDescriptor;
            AssetManager assetManager = getAssets();

            // Copy file from assets to a temp file (PdfRenderer can't read assets directly)
            InputStream inputStream = assetManager.open(fileName);
            File tempFile = new File(getCacheDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            inputStream.close();
            outputStream.close();

            // Open and render first page of the PDF
            fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(fileDescriptor);

            if (renderer.getPageCount() > 0) {
                PdfRenderer.Page page = renderer.openPage(0);

                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                imageView.setImageBitmap(bitmap);

                page.close();
            }

            renderer.close();
            fileDescriptor.close();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading PDF", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- WebView setup ----------
//    private void setupModelWebView() {
//        WebSettings ws = modelWebView.getSettings();
//        ws.setJavaScriptEnabled(true);
//        ws.setDomStorageEnabled(true);
//
//        // Allow local file access so model-viewer can load assets
//        ws.setAllowFileAccess(true);
//        ws.setAllowFileAccessFromFileURLs(true);
//        ws.setAllowUniversalAccessFromFileURLs(true);
//
//        // Important: enable mixed content if using CDN script + local content on Android N+
//        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//
//        // Make background transparent
//        modelWebView.setBackgroundColor(0x00000000);
//
//        // Keep webview hidden initially
//        modelWebView.setVisibility(View.GONE);
//
//        // Enable Web Debugging
//        WebView.setWebContentsDebuggingEnabled(true);
//
//    }

    // Toggle overlay: if not visible load the HTML with model param; otherwise hide
//    private void toggle3DOverlay(String modelFileName) {
//        WebView modelWebView = findViewById(R.id.modelWebView);
//        if (modelWebView.getVisibility() == View.VISIBLE) {
//            Log.d("ContentValues", "Hiding 3D overlay");
//            modelWebView.setVisibility(View.GONE);
//            modelWebView.loadUrl("about:blank");
//            return;
//        }
//
//        Log.d("ContentValues", "Loading model overlay: " + modelFileName);
//
//        modelWebView.setVisibility(View.VISIBLE);
//
//        modelWebView.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                Log.d("ContentValues", "WebView loaded: " + url);
//                try {
//                    String base64Model = loadModelFromAssets(modelFileName);
//                    if (base64Model != null) {
//                        String js = "javascript:loadModel('" + base64Model + "')";
//                        view.evaluateJavascript(js, null);
//                    }
//                } catch (Exception e) {
//                    Log.e("ContentValues", "Failed to inject model: " + e.getMessage());
//                }
//            }
//
//            @Override
//            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//                Log.e("ContentValues", "WebView error: " + error.getDescription());
//            }
//        });
//
//        modelWebView.bringToFront();
//        modelWebView.setAlpha(1.0f);
//        // Load the base HTML
//        modelWebView.loadUrl("file:///android_asset/model_viewer.html");
//    }
//
//
//    // simple URL encoding for spaces etc (basic)
//    private String encodeForUrl(String s) {
//        return s.replace(" ", "%20");
//    }
//

    // ---------- mapping 2D filename to 3D model path ----------
    // Default: replace .pdf/.png with .glb and assume models live under assets/models/
    private String map2DFileTo3DModelPath(String twoDFileName) {
        String base = twoDFileName;
        if (base.endsWith(".pdf") || base.endsWith(".png")) {
            int dot = base.lastIndexOf('.');
            base = base.substring(0, dot);
        }
        // example: "Level1" -> "models/level1.glb"
        return "models/" + base + ".glb";
    }
//
//    private String loadModelFromAssets(String fileName) {
//        try {
//            InputStream inputStream = getAssets().open(fileName);
//            byte[] buffer = new byte[inputStream.available()];
//            inputStream.read(buffer);
//            inputStream.close();
//
//            String base64Data = Base64.encodeToString(buffer, Base64.NO_WRAP);
//            return "data:model/gltf-binary;base64," + base64Data;
//        } catch (IOException e) {
//            Log.e("ContentValues", "Error loading model: " + e.getMessage());
//            return null;
//        }
//    }
//
//
//    // lifecycle for WebView
//    @Override
//    protected void onResume() {
//        super.onResume();
//        // nothing specific required for WebView
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        // nothing specific required
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (modelWebView != null) {
//            modelWebView.loadUrl("about:blank");
//            modelWebView.removeAllViews();
//            modelWebView.destroy();
//        }
//        super.onDestroy();
//    }


}

