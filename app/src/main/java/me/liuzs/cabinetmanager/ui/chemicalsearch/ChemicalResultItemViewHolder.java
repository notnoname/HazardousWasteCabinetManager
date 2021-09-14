package me.liuzs.cabinetmanager.ui.chemicalsearch;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import me.liuzs.cabinetmanager.ChemicalSearchActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.Chemical;

public class ChemicalResultItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static Gson mGson = new Gson();

    TextView chemicalName, chemicalType, cas;
    Chemical chemical;
    ChemicalSearchActivity activity;


    public ChemicalResultItemViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        chemicalName = itemView.findViewById(R.id.tvChemicalName);
        chemicalType = itemView.findViewById(R.id.tvChemicalType);
        cas = itemView.findViewById(R.id.tvCAS);
    }

    @Override
    public void onClick(View v) {
        String chemicalJson = mGson.toJson(chemical);
        Intent data = new Intent();
        data.putExtra(ChemicalSearchActivity.KEY_CHEMICAL_JSON, chemicalJson);
        activity.setResult(Activity.RESULT_OK, data);
        activity.finish();
    }
}