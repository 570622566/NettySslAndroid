package com.lhsg.nettyapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.security.KeyChainException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class MainActivity extends Activity {

    /**
     * The file name of the PKCS12 file used
     */
    public static final String PKCS12_FILENAME = "keystore.p12";

    /**
     * The pass phrase of the PKCS12 file
     */
    public static final String PKCS12_PASSWORD = "changeit";

    /**
     * Intent extra name to indicate to stop server
     */
    public static final String EXTRA_STOP_SERVER = "stop_server";

    // Log tag for this class
    private static final String TAG = "KeyChainApiActivity";

    // Alias for certificate
    private static final String DEFAULT_ALIAS = "test";

    // Name of the application preference
    private static final String KEYCHAIN_PREF = "keychain";

    // Name of preference name that saves the alias
    private static final String KEYCHAIN_PREF_ALIAS = "alias";

    // Request code used when starting the activity using the KeyChain install
    // intent
    private static final int INSTALL_KEYCHAIN_CODE = 1;

    public static final int SERVER_PORT = 9090;

    // Test SSL URL
    private static final String TEST_SSL_URL = "https://localhost:"+SERVER_PORT;

    // Button to start/stop the simple SSL web server
    private Button serverButton;

    // Button to install the key chain
    private Button keyChainButton;

    // Button to launch the browser for testing https://localhost:8080
    private Button testSslButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the view using the main.xml layout
        setContentView(R.layout.activity_main);

        // Setup the simple SSL web server start/stop button
        serverButton = (Button) findViewById(R.id.server_button);
        serverButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serverButton.getText().equals(
                        getResources().getString(R.string.server_start))) {
                    serverButton.setText(R.string.server_stop);
                    startServer();
                } else {
                    serverButton.setText(R.string.server_start);
                    stopServer();
                }
            }
        });

        // Setup the test SSL page button
        testSslButton = (Button) findViewById(R.id.test_ssl_button);
        testSslButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri
                        .parse(TEST_SSL_URL));
                startActivity(i);
            }
        });
    }

    /**
     * This will be called when the user click on the notification to stop the
     * SSL server
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "In onNewIntent()");
        super.onNewIntent(intent);
        boolean isStopServer = intent.getBooleanExtra(EXTRA_STOP_SERVER, false);
        if (isStopServer) {
            serverButton.setText(R.string.server_start);
            stopServer();
        }
    }

    /**
     * This method returns the alias of the key chain from the application
     * preference
     *
     * @return The alias of the key chain
     */
    private String getAlias() {
        SharedPreferences pref = getSharedPreferences(KEYCHAIN_PREF,
                MODE_PRIVATE);
        return pref.getString(KEYCHAIN_PREF_ALIAS, DEFAULT_ALIAS);
    }

    /**
     * This method sets the alias of the key chain to the application preference
     */
    private void setAlias(String alias) {
        SharedPreferences pref = getSharedPreferences(KEYCHAIN_PREF,
                MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.putString(KEYCHAIN_PREF_ALIAS, alias);
        editor.commit();
    }

    private X509Certificate[] getCertificateChain(String alias) {
        Log.d(TAG, "getCertificateChain alias : "+alias);
        try {
            return KeyChain.getCertificateChain(this, alias);
        } catch (KeyChainException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private PrivateKey getPrivateKey(String alias) {
        Log.d(TAG, "getPrivateKey alias : "+alias);
        try {
            return KeyChain.getPrivateKey(this, alias);
        } catch (KeyChainException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method checks if the key chain is installed
     *
     * @return true if the key chain is not installed or allowed
     */
    private boolean isKeyChainAccessible() {
        return getCertificateChain(getAlias()) != null
                && getPrivateKey(getAlias()) != null;
    }

    /**
     * This method starts the background service of the simple SSL web server
     */
    private void startServer() {
        Intent secureWebServerIntent = new Intent(this,
                SecureWebServerService.class);
        startService(secureWebServerIntent);
    }

    /**
     * This method stops the background service of the simple SSL web server
     */
    private void stopServer() {
        Intent secureWebServerIntent = new Intent(this,
                SecureWebServerService.class);
        stopService(secureWebServerIntent);
    }

    /**
     * This is a convenient method to disable the key chain install button
     */
    private void disableKeyChainButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                keyChainButton.setText(R.string.keychain_installed);
                keyChainButton.setEnabled(false);
            }
        });
    }

}
