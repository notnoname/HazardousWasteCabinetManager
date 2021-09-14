package me.liuzs.cabinetmanager.ui.chemicalsearch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.ChemicalSearchActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.Chemical;

public class ChemicalSearchResultAdapter extends RecyclerView.Adapter<ChemicalResultItemViewHolder> {

    private List<Chemical> mResultList = new LinkedList<>();

    private ChemicalSearchActivity mActivity;

    public ChemicalSearchResultAdapter(ChemicalSearchActivity activity) {
        super();
        mActivity = activity;
    }

    @NonNull
    @NotNull
    @Override
    public ChemicalResultItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_chemical_search, parent, false);
        return new ChemicalResultItemViewHolder(itemView);
    }

    public void addResult(List<Chemical> chemicals) {
        mResultList.addAll(chemicals);
        notifyDataSetChanged();
    }

    public void addResult(Chemical chemical) {
        mResultList.add(chemical);
        notifyDataSetChanged();
    }

    public void setResult(List<Chemical> chemicals) {
        mResultList.clear();
        mResultList.addAll(chemicals);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ChemicalResultItemViewHolder holder, int position) {

        holder.activity = mActivity;
        Chemical chemical = mResultList.get(position);
        holder.chemical = chemical;
        holder.chemicalName.setText(chemical.chineseName);
        holder.chemicalType.setText("分类：" + chemical.chemicalType);
        holder.cas.setText("CAS：" + chemical.casNo);
    }

    @Override
    public int getItemCount() {
        return mResultList.size();
    }
}
