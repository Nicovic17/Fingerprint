package com.example.fingerprint;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

@TargetApi(Build.VERSION_CODES.M)
public class FingerPrintHandler extends FingerprintManager.AuthenticationCallback {

        private Context context;

        public FingerPrintHandler(Context context){

            this.context=context;
        }

        public void startAuth(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject){

            CancellationSignal cancellationSignal= new CancellationSignal();
            fingerprintManager.authenticate(cryptoObject, cancellationSignal,0, this, null);


        }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {

            this.update("Error",false);
    }

    @Override
    public void onAuthenticationFailed() {

            this.update("Dito non registrato",false);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

            this.update(""+helpString,false);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {

            this.update("Access successufull",true);
    }

    private void update(String s, boolean b){

        TextView paraLabel=(TextView)((Activity)context).findViewById(R.id.paraLabel);
        ImageView imageView=((Activity)context).findViewById(R.id.fingerprintImage);
        paraLabel.setText(s);

        if(b==false){

            paraLabel.setTextColor(ContextCompat.getColor(context,R.color.colorAccent));

        }
        else{
            paraLabel.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary));
            imageView.setImageResource(R.mipmap.action_done);
        }
    }


}
