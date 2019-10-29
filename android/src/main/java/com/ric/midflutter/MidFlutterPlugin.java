package com.ric.midflutter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.midtrans.sdk.corekit.callback.CardTokenCallback;
import com.midtrans.sdk.corekit.core.MidtransSDK;
import com.midtrans.sdk.corekit.core.SdkCoreFlowBuilder;
import com.midtrans.sdk.corekit.models.CardTokenRequest;
import com.midtrans.sdk.corekit.models.TokenDetailsResponse;

import org.json.JSONException;
import org.json.JSONObject;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.app.Activity.RESULT_OK;

/**
 * MidFlutterPlugin
 */
public class MidFlutterPlugin implements MethodCallHandler, PluginRegistry.ActivityResultListener {
    private Context context;
    private Activity activity;
    private static final int REQUEST_RENT_FEE = 4569;
    private String token;
    private Result result;

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "mid_flutter");
        channel.setMethodCallHandler(new MidFlutterPlugin());
        MidFlutterPlugin plugin = new MidFlutterPlugin();
        channel.setMethodCallHandler(plugin);

        plugin.context = registrar.context();
        plugin.activity = registrar.activity();

        registrar.addActivityResultListener(plugin);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("configure")) {
            String clientKey = call.argument("clientKey");
            Object isProductionRaw = call.argument("isProduction");
            if (isProductionRaw == null) isProductionRaw = "false";
            boolean isProduction = Boolean.valueOf(isProductionRaw.toString());
            this.result = result;

            configure(clientKey, isProduction);
        } else if (call.method.equals("generateCreditCardToken")) {
            String creditCardNumber = call.argument("creditCardNumber");
            String expiryMonth = call.argument("expiryMonth").toString();
            String expiryYear = call.argument("expiryYear").toString();
            String cvv = call.argument("cvv").toString();
            Object amountRaw = call.argument("amount");
            if (amountRaw == null) amountRaw = "0.0";
            double amount = Double.valueOf(amountRaw.toString());
            this.result = result;

            submitCreditCard(creditCardNumber, cvv, expiryMonth, expiryYear, amount);
        } else {
            result.notImplemented();
        }
    }

    private void configure(String clientKey, boolean isProduction) {
        String merchantBaseUrl;
        if (!isProduction) {
            merchantBaseUrl = "https://api.sandbox.veritrans.co.id/v2/transactions";
        } else {
            merchantBaseUrl = "https://api.veritrans.co.id/v2/transactions";
        }

        SdkCoreFlowBuilder.init()
                .setContext(this.context)
                .setClientKey(clientKey)
                .setMerchantBaseUrl(merchantBaseUrl)
                .enableLog(true)
                .buildSDK();

        this.result.success("");
    }

    private void submitCreditCard(String cardNumber, String cvv, String expireMonth,
                                  String expireYear, double totalPrice) {
        CardTokenRequest cardTokenRequest = new CardTokenRequest(
                // Card number
                cardNumber,
                cvv,
                expireMonth,
                expireYear,
                MidtransSDK.getInstance().getClientKey());

        cardTokenRequest.setGrossAmount(totalPrice);

        cardTokenRequest.setSecure(true);

        MidtransSDK.getInstance().getCardToken(cardTokenRequest, new CardTokenCallback() {
            @Override
            public void onSuccess(TokenDetailsResponse tokenDetailsResponse) {
                String token = tokenDetailsResponse.getTokenId();
                String url = tokenDetailsResponse.getRedirectUrl();

                MidFlutterPlugin.this.token = token;
                Intent intent = new Intent(activity, WebviewVerifyActivity.class);
                intent.putExtra(WebviewVerifyActivity.EXTRA_URL, url);
                activity.startActivityForResult(intent, REQUEST_RENT_FEE);
            }

            @Override
            public void onFailure(TokenDetailsResponse tokenDetailsResponse, String errorMessage) {
                result.error("Error: " + errorMessage, "", "");
            }

            @Override
            public void onError(Throwable throwable) {
                result.error("Error: " + throwable.getMessage(), "", "");
            }
        });
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RENT_FEE) {
            if (resultCode == RESULT_OK) {
                if (result != null) result.success("Token: " + (token == null ? "" : token));
            } else {
                String cancelMessage = "3D Secure transaction canceled by user";
                String message = data == null ? cancelMessage : data.getStringExtra("message");
                result.error("Error", message, "");
            }
        } else {
            result.success("Failed!");
        }

        return false;
    }
}
