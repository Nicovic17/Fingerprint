package com.example.fingerprint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {

    private TextView mHeadinLabel;
    private ImageView mFingerPrintImage;
    private TextView mParaLabel;

    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    private KeyStore keyStore;

    private Cipher cipher;
    private String KEY_NAME= "AndroidKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHeadinLabel=findViewById(R.id.headingLabel);
        mFingerPrintImage=findViewById(R.id.fingerprintImage);
        mParaLabel=findViewById(R.id.paraLabel);


        // CHECK 1: Android version should be greather or equal to marshmellow
        // CHECK 2: Devide has fingerprint scanner or not
        // CHECK 3: Have permission to use fingerprint in app
        // CHECK 4: Lock screen is secured with at least 1 type of lock
        //TODO CHECK 5: At least 1 fingerprint is registered

         if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
         {
                fingerprintManager=(FingerprintManager) getSystemService((FINGERPRINT_SERVICE));
                keyguardManager=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);

                if(!fingerprintManager.isHardwareDetected())
                {
                    mParaLabel.setText("Fingerprint scanner not detected");
                }
                else if(ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){

                    mParaLabel.setText("No permission");
                } else if(!keyguardManager.isKeyguardSecure())
                {
                    mParaLabel.setText("Add lock to your phone");
                } else if(!fingerprintManager.hasEnrolledFingerprints()){
                    mParaLabel.setText("Add at least one fringerprint");

                }else {
                    mParaLabel.setText("Place your finger on scanner");

                    generateKey();

                    if(cipherInit()){

                        FingerprintManager.CryptoObject cryptoObject= new FingerprintManager.CryptoObject(cipher);
                        FingerPrintHandler fingerPrintHandler=new FingerPrintHandler(this);
                        fingerPrintHandler.startAuth(fingerprintManager,cryptoObject);
                    }


                }

         }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void generateKey() {

        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();

        } catch (KeyStoreException | IOException | CertificateException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | NoSuchProviderException e) {

            e.printStackTrace();

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {

            keyStore.load(null);

            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }

    }

}
