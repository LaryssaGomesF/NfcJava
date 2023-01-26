package com.example.nfcsend;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class ReceiveActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            nfcAdapter.disableReaderMode(this);
        }
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            try {
                ndef.connect();
                NdefMessage ndefMessage = ndef.getNdefMessage();
                String message = new String(ndefMessage.getRecords()[0].getPayload());
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                Log.d("NFC", "Received NFC message: "+message);
                ndef.close();
            } catch (IOException | FormatException e) {
                e.printStackTrace();
            }
        }
    }
}
