package com.martynaroj.traveljournal.services.others;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

public class GoogleClient {

    private GoogleSignInClient googleSignInClient;
    private GoogleSignInAccount googleSignInAccount;


    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }


    public GoogleSignInAccount getGoogleSignInAccount() {
        return googleSignInAccount;
    }


    public void initGoogleSignInClient(Context context) {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions);
    }


    public String getGoogleSignInAccount(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                googleSignInAccount = task.getResult(ApiException.class);
                if (googleSignInAccount != null)
                    return Constants.SUCCESS;
            } catch (ApiException e) {
                String statusCode = CommonStatusCodes.getStatusCodeString(e.getStatusCode());
                String message = "Error: Internal API error";
                if (statusCode.equals("NETWORK_ERROR"))
                    message = "Error: Please check your network connection";
                else if (statusCode.equals("TIMEOUT"))
                    message = "Error: Timed out while awaiting the result";
                return message;
            }
        }
        return "Error: Activity request error";
    }


    public AuthCredential getGoogleAuthCredential(GoogleSignInAccount googleSignInAccount) {
        String googleTokenId = googleSignInAccount.getIdToken();
        return GoogleAuthProvider.getCredential(googleTokenId, null);
    }

}
