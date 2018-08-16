/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.inventory;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.MotionEvent;

import com.example.android.inventory.data.ItemContract;
import com.example.android.inventory.data.ItemDbHelper;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;
    private Uri currentItemUri;

    private EditText nameEditText;
    private EditText quantityEditText;
    private EditText priceEditText;
    private EditText phoneNumberEditText;
    private Spinner supplierSpinner;


    private int supplier = 0;

    private boolean itemHasChanged = false;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.android.inventory.R.layout.activity_editor);

        Intent intent = getIntent();
        currentItemUri = intent.getData();

        if (currentItemUri == null){
            setTitle(R.string.editor_activity_title_new_item);
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_item));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        nameEditText = (EditText) findViewById(com.example.android.inventory.R.id.edit_name);
        quantityEditText = (EditText) findViewById(com.example.android.inventory.R.id.edit_quantity);
        phoneNumberEditText = (EditText) findViewById(com.example.android.inventory.R.id.edit_phone_number);
        priceEditText = (EditText) findViewById(com.example.android.inventory.R.id.edit_price);
        supplierSpinner = (Spinner) findViewById(com.example.android.inventory.R.id.spinner_supplier);

        nameEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        phoneNumberEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        supplierSpinner.setOnTouchListener(touchListener);

        setupSpinner();
    }

    private void setupSpinner() {
        ArrayAdapter supplierSpinnerAdapter = ArrayAdapter.createFromResource(this,
                com.example.android.inventory.R.array.array_supplier_options, android.R.layout.simple_spinner_item);

        supplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        supplierSpinner.setAdapter(supplierSpinnerAdapter);

        supplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(com.example.android.inventory.R.string.supplier_kvartett))) {
                        supplier = ItemContract.ItemEntry.SUPPLIER_KVARETT; // Kvartett
                    } else if (selection.equals(getString(com.example.android.inventory.R.string.supplier_meinl))) {
                        supplier = ItemContract.ItemEntry.SUPPLIER_MEINL; // Meinl
                    } else {
                        supplier = ItemContract.ItemEntry.SUPPLIER_UNKNOWN; // Unknown
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                supplier = 0; // Unknown
            }
        });
    }

    private void saveItem() {
        String nameString =  nameEditText.getText().toString().trim();
        String quantityString =  quantityEditText.getText().toString().trim();
        String priceString =  priceEditText.getText().toString().trim();
        String phoneString =  phoneNumberEditText.getText().toString().trim();

        if (currentItemUri == null) {
            if (TextUtils.isEmpty(nameString)) {
                Toast.makeText(this, getString(R.string.name_missing), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(quantityString)) {
                Toast.makeText(this, getString(R.string.quantity_missing), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(phoneString)) {
                Toast.makeText(this, getString(R.string.phone_missing), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(priceString)) {
                Toast.makeText(this, getString(R.string.price_missing), Toast.LENGTH_SHORT).show();
                return;
            }
            if (supplier == ItemContract.ItemEntry.SUPPLIER_UNKNOWN) {
                Toast.makeText(this, getString(R.string.supplier_missing), Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();

            values.put(ItemContract.ItemEntry.COLUMN_NAME, nameString);
            values.put(ItemContract.ItemEntry.COLUMN_PRICE, quantityString);
            values.put(ItemContract.ItemEntry.COLUMN_QUANTITY, priceString);
            values.put(ItemContract.ItemEntry.COLUMN_SUPPLIER, supplier);
            values.put(ItemContract.ItemEntry.COLUMN_PHONE, phoneString);

            Uri newUri = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }else{

            if (TextUtils.isEmpty(nameString)) {
                Toast.makeText(this, getString(R.string.name_missing), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(quantityString)) {
                Toast.makeText(this, getString(R.string.quantity_missing), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(phoneString)) {
                Toast.makeText(this, getString(R.string.phone_missing), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(priceString)) {
                Toast.makeText(this, getString(R.string.price_missing), Toast.LENGTH_SHORT).show();
                return;
            }
            if (supplier == ItemContract.ItemEntry.SUPPLIER_UNKNOWN) {
                Toast.makeText(this, getString(R.string.supplier_missing), Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();

            values.put(ItemContract.ItemEntry.COLUMN_NAME, nameString);
            values.put(ItemContract.ItemEntry.COLUMN_PRICE, quantityString);
            values.put(ItemContract.ItemEntry.COLUMN_QUANTITY, priceString);
            values.put(ItemContract.ItemEntry.COLUMN_SUPPLIER, supplier);
            values.put(ItemContract.ItemEntry.COLUMN_PHONE, phoneString);


            int rowsAffected = getContentResolver().update(currentItemUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.example.android.inventory.R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                return true;
            case android.R.id.home:
                if (!itemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            case com.example.android.inventory.R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (!itemHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_NAME,
                ItemContract.ItemEntry.COLUMN_QUANTITY,
                ItemContract.ItemEntry.COLUMN_PHONE,
                ItemContract.ItemEntry.COLUMN_PRICE,
                ItemContract.ItemEntry.COLUMN_SUPPLIER};
        return new CursorLoader(this,
                currentItemUri,
                projection,
                null,
                null,
                null);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_SUPPLIER);
            int phoneColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_PHONE);
            int priceColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_PRICE);

            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int phone = cursor.getInt(phoneColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int supplier = cursor.getInt(supplierColumnIndex);

            nameEditText.setText(name);
            quantityEditText.setText(Integer.toString(quantity));
            phoneNumberEditText.setText(Integer.toString(phone));
            priceEditText.setText(Integer.toString(price));

            switch (supplier) {
                case ItemContract.ItemEntry.SUPPLIER_KVARETT:
                    supplierSpinner.setSelection(1);
                    break;
                case ItemContract.ItemEntry.SUPPLIER_MEINL:
                    supplierSpinner.setSelection(2);
                    break;
                default:
                    supplierSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        quantityEditText.setText("");
        phoneNumberEditText.setText("");
        priceEditText.setText("");
        supplierSpinner.setSelection(0);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (currentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(currentItemUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

}