//package com.example.sitevision;
//
//import android.os.Bundle;
//import android.util.Base64;
//import android.util.Log;
//import android.view.View;
//import android.webkit.ConsoleMessage;
//import android.webkit.WebChromeClient;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import java.io.InputStream;
//
//public class ModelViewerActivity extends AppCompatActivity {
//
//    WebView modelWebView;
//    private static final String TAG = "ModelViewerActivity";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_model_viewer);
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.frameViewerMain), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        modelWebView = findViewById(R.id.modelWebView);
//        // Enable hardware acceleration for WebGL
//        modelWebView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
//
//        // Get model name dynamically
//        String modelName = getIntent().getStringExtra("model_name");
//        if (modelName == null || modelName.trim().isEmpty()) {
//            modelName = "level1"; // fallback default
//        }
//
//        // Convert to lowercase and remove extension if present
//        modelName = modelName.toLowerCase().replace(".glb", "").trim();
//
//        Log.d(TAG, "🎯 Model name received: " + modelName);
//
//        setupWebView(modelName);
//    }
//
//    private void setupWebView(String modelName) {
//        WebSettings settings = modelWebView.getSettings();
//        settings.setJavaScriptEnabled(true);
//        settings.setAllowFileAccess(true);
//        settings.setAllowFileAccessFromFileURLs(true);
//        settings.setAllowUniversalAccessFromFileURLs(true);
//        settings.setDomStorageEnabled(true);
//        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//
//        // Catch console messages from WebView
//        modelWebView.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public boolean onConsoleMessage(ConsoleMessage message) {
//                Log.d("WebViewConsole", message.message() + " -- " + message.sourceId() + ":" + message.lineNumber());
//                return true;
//            }
//        });
//
//        // Load model_viewer.html and inject correct model file
//        modelWebView.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                try {
//                    // Load .glb from assets folder
//                    String modelUrl = "file:///android_asset/models/" + modelName + ".glb";
//                    String js = "window.loadModel('" + modelUrl + "')";
//                    view.evaluateJavascript(js, null);
//                    Log.d(TAG, "✅ Injected model from assets: " + modelUrl);
//                } catch (Exception e) {
//                    Log.e(TAG, "❌ Error loading model: " + e.getMessage());
//                    e.printStackTrace();
//                }
//            }
//        });
//
////        Base64 encoding
////        modelWebView.setWebViewClient(new WebViewClient() {
////            @Override
////            public void onPageFinished(WebView view, String url) {
////                try {
////                    // Load .glb file from res/raw dynamically
////                    int resId = getResources().getIdentifier(modelName, "raw", getPackageName());
////                    if (resId == 0) {
////                        Log.e("ModelViewerActivity", "❌ Model not found in res/raw: " + modelName);
////                        return;
////                    }
////
////                    InputStream inputStream = getResources().openRawResource(resId);
////                    byte[] buffer = new byte[inputStream.available()];
////                    inputStream.read(buffer);
////                    inputStream.close();
////
////                    String base64Model = Base64.encodeToString(buffer, Base64.NO_WRAP);
////                    String js = "window.loadModel('data:model/gltf-binary;base64," + base64Model + "')";
////                    view.evaluateJavascript(js, null);
////
////                    Log.d("ModelViewerActivity", "✅ Injected Base64 model: " + modelName);
////
////                } catch (Exception e) {
////                    Log.e("ModelViewerActivity", "❌ Error loading model: " + e.getMessage());
////                    e.printStackTrace();
////                }
////            }
////        });
//
//        // Load the model viewer HTML file
//        modelWebView.loadUrl("file:///android_asset/model_viewer.html");
//
//
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (modelWebView != null) {
//            modelWebView.destroy();
//        }
//        super.onDestroy();
//    }
//}


package com.example.sitevision;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ModelViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get model name dynamically
        String modelName = getIntent().getStringExtra("model_name");
        if (modelName == null || modelName.trim().isEmpty()) {
            modelName = "level1"; // default model
        }
        modelName = modelName.toLowerCase().replace(".glb", "").trim();

        // URL of hosted HTML page (replace with your server URL)
        String url = "https://google.com/model_viewer.html?model=" + modelName;

        // Launch Chrome or default browser
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);

        finish(); // close activity
    }
}

