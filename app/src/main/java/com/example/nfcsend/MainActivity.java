package com.example.nfcsend;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;

    private NdefMessage ndefMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // configura o pending intent para ser chamado quando um cartão NFC é tocado
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // configura os filtros de intenção para detectar cartões NDEF
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[]{ndef};

        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if (tag != null) {
                    String action = intent.getAction();
                    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                        readTag(tag);
                    } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
                        writeTag(tag);
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            String action = intent.getAction();
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                readTag(tag);
            } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
                writeTag(tag);
            }
        }
    }

    private void readTag(Tag tag) {
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            try {
                ndef.connect();
                ndefMessage = ndef.getNdefMessage();
                if (ndefMessage != null) {
// faça alguma coisa com a mensagem lida
                    Toast.makeText(this, "Mensagem lida: " + new String(ndefMessage.getRecords()[0].getPayload()), Toast.LENGTH_SHORT).show();
                }
                ndef.close();
            } catch (IOException | FormatException e) {
                e.printStackTrace();
                Toast.makeText(this, "Falha ao ler a tag", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Tag não suporta NDEF", Toast.LENGTH_SHORT).show();
        }
    }
    private void writeTag(Tag tag) {
        NdefMessage message = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            message = new NdefMessage(new NdefRecord[]{NdefRecord.createMime("text/plain", "Exemplo de mensagem NFC".getBytes())});
        }
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            try {
                ndef.connect();
                if (ndef.isWritable()) {
                    int size = message.toByteArray().length;
                    if (ndef.getMaxSize() < size) {
                        Toast.makeText(this, "Tag não tem espaço suficiente", Toast.LENGTH_SHORT).show();
                    } else {
                        ndef.writeNdefMessage(message);
                        Toast.makeText(this, "Mensagem escrita com sucesso", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Tag não é gravável", Toast.LENGTH_SHORT).show();
                }
                ndef.close();
            } catch (IOException | FormatException e) {
                e.printStackTrace();
                Toast.makeText(this, "Falha ao escrever na tag", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Tag não suporta NDEF", Toast.LENGTH_SHORT).show();
        }
    }

}
