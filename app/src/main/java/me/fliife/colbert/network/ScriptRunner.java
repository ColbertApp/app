package me.fliife.colbert.network;

import android.content.Context;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import me.fliife.colbert.utils.Callbacks;

public class ScriptRunner {
    private final String script;
    private final String url;
    private final Callbacks.Callback callback;
    private WebView webView;

    public ScriptRunner(String script, String url, Context ctx, Callbacks.Callback callback) {
        this.script = script;
        this.url = url;
        this.callback = callback;
        this.webView = new WebView(ctx);
    }

    public void execute() {
        // Enable javascript -_-
        webView.getSettings().setJavaScriptEnabled(true);
        // Faster loading
        webView.getSettings().setBlockNetworkImage(true);
        // Enable localStorage
        webView.getSettings().setDomStorageEnabled(true);
        // Disable redirection to mobile website
        webView.getSettings().setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.109 Safari/537.36");
        webView.addJavascriptInterface(this, "JavaInterface");
        webView.setWebChromeClient(new WebChromeClient() {
            private boolean scriptLoaded = false;

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress >= 100 && !scriptLoaded) {
                    scriptLoaded = true;
                    view.loadUrl("javascript:" + script.replaceAll("\n", ""));
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("Chromium", consoleMessage.message() + " ----- Line " + consoleMessage.lineNumber());
                return true;
            }
        });
        webView.loadUrl(url);
    }

    @JavascriptInterface
    public void finishLogin(boolean result) {
        Log.d("JsResultLogin", result ? "true" : "false");
        callback.onCallback(new Boolean(result));
    }

    @JavascriptInterface
    public void finishFetch(String result) {
        //Log.d("JsResultScript", result);
        callback.onCallback(result);
    }
}
