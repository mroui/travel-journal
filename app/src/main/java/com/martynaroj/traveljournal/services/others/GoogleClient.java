package com.martynaroj.traveljournal.services.others;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

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
    private Context context;


    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }


    public GoogleSignInAccount getGoogleSignInAccount() {
        return googleSignInAccount;
    }


    public void initGoogleSignInClient(Context context) {
        this.context = context;
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions);
    }


    public String getGoogleSignInAccount(int requestCode, Intent data) {
        if (requestCode == Constants.RC_SIGN_IN && context != null) {
            Resources resources = context.getResources();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                googleSignInAccount = task.getResult(ApiException.class);
                if (googleSignInAccount != null)
                    return Constants.SUCCESS;
            } catch (ApiException e) {
                String statusCode = CommonStatusCodes.getStatusCodeString(e.getStatusCode());
                String message = resources.getString(R.string.messages_error_failed_google_services);
                if (statusCode.equals(Constants.NETWORK_ERROR))
                    message = resources.getString(R.string.messages_error_network_connection);
                else if (statusCode.equals(Constants.TIMEOUT))
                    message = resources.getString(R.string.messages_error_timed_out);
                return message;
            }
        }
        return "ERROR: Activity request error, please try again later";
    }


    public AuthCredential getGoogleAuthCredential(GoogleSignInAccount googleSignInAccount) {
        String googleTokenId = googleSignInAccount.getIdToken();
        return GoogleAuthProvider.getCredential(googleTokenId, null);
    }

}
