package com.itp.glevinzon.capstone;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.itp.glevinzon.capstone.models.Datum;
import com.itp.glevinzon.capstone.models.Record;
import com.itp.glevinzon.capstone.models.Tag;
import com.itp.glevinzon.capstone.utils.PaginationAdapterCallback;

import java.util.ArrayList;
import java.util.List;

import fisk.chipcloud.ChipCloud;
import fisk.chipcloud.ChipCloudConfig;
import katex.hourglass.in.mathlib.MathView;

/**
 * Created by glen on 3/31/17.
 */

public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final String TAG = "ADAPTER";

    private List<Datum> equationResults;
    private List<Tag> equationTags;
    private List<Record> equationRecords;
    private ArrayList<String> arrList;
    private Context context;

    private ItemClickListener clickListener;

    private boolean isLoadingAdded = false;

    private boolean retryPageLoad = false;

    private PaginationAdapterCallback mCallback;

    private String errorMsg;

    private ChipCloud chipCloud;

    public PaginationAdapter(Context context) {
        this.context = context;
        this.mCallback = (PaginationAdapterCallback) context;
        equationResults = new ArrayList<>();
        equationTags = new ArrayList<>();
        equationRecords = new ArrayList<>();
    }

    public List<Datum> getEquations() {
        return equationResults;
    }

    public void setTags(List<Tag> tags) {
        this.equationTags = tags ;
    }

    public void setRecords(List<Record> records) {
        this.equationRecords = records ;
    }

//
//    public PaginationAdapter(List<Datum> equationResults, int rowLayout, Context context) {
//        this.equationResults = equationResults;
//        this.rowLayout = rowLayout;
//        this.context = context;
//    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                viewHolder = getViewHolder(parent, inflater);
                break;
            case LOADING:
                View v2 = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(v2);
                break;
        }
        return viewHolder;
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        RecyclerView.ViewHolder viewHolder;
        View v1 = inflater.inflate(R.layout.item_list, parent, false);
        viewHolder = new ViewHolder(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + position);
        Datum data = equationResults.get(position);

        switch (getItemViewType(position)) {
            case ITEM:
                final ViewHolder viewHolder = (ViewHolder) holder;

                String mode;
                if(data.getAudioUrl() != null) {
                    mode = "AUDIO";
                } else {
                    mode = "TTS";
                }

                viewHolder.mYear.setText(
                        data.getCreatedAt().substring(0, 4)  // we want the year only
                                + " | "
                                + mode
                );

                arrList = new ArrayList<String>();

                if(equationRecords != null) {
                    for(Record record : equationRecords) {
                        if(data.getId().equals(record.getEqId())){
                            if(equationTags != null) {
                                for(Tag tag : equationTags) {
                                    if(record.getTagId().equals(tag.getId())){
                                        arrList.add(tag.getName());
                                    }
                                }
                            }
                        }
                    }

                    chipCloud.addChips(arrList);
                    arrList = null;
                }


                viewHolder.mMovieTitle.setVisibility(View.GONE);
                String laTex = "" + data.getName();
                String tex = "$ "+ laTex +" $";
                if(!laTex.isEmpty()){
                    viewHolder.mathView.setDisplayText(tex);
                }

                break;

            case LOADING:
                LoadingVH loadingVH = (LoadingVH) holder;

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);
                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));
                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }

                break;
        }

    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + equationResults.size());
        return equationResults == null ? 0 : equationResults.size();
    }

    public int getItemCountByTag() {
        return equationResults == null ? 0 : equationResults.size();
    }

    public int getItemCountByRecord() {
        return equationRecords == null ? 0 : equationRecords.size();
    }


    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == equationResults.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    /*
   Helpers
   _________________________________________________________________________________________________
    */

    public void add(Datum r) {
        Log.d(TAG, "add: " + r);
        equationResults.add(r);
        notifyItemInserted(equationResults.size() - 1);
    }

    public void addAll(List<Datum> moveResults) {
        for (Datum data : moveResults) {
            add(data);
        }
    }

    public void remove(Datum r) {
        int position = equationResults.indexOf(r);
        if (position > -1) {
            equationResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void removeByTag(Tag r) {
        int position = equationTags.indexOf(r);
        if (position > -1) {
            equationTags.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void removeByRecord(Record r) {
        int position = equationRecords.indexOf(r);
        if (position > -1) {
            equationRecords.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
        while (getItemCountByTag() > 0) {
            removeByTag(getItemByTag(0));
        }
        while (getItemCountByRecord() > 0) {
            removeByRecord(getItemByRecord(0));
        }
        arrList = null;
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Datum());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = equationResults.size() - 1;
        Datum data = getItem(position);

        if (data != null) {
            equationResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Datum getItem(int position) {
        return equationResults.get(position);
    }

    public Tag getItemByTag(int position) {
        return equationTags.get(position);
    }

    public Record getItemByRecord(int position) {
        return equationRecords.get(position);
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(equationResults.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    private int getMatColor(String typeColor) {
        int returnColor = Color.BLACK;
        int arrayId = this.context.getResources().getIdentifier("mdcolor_" + typeColor, "array", this.context.getPackageName());

        if (arrayId != 0) {
            TypedArray colors = this.context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.BLACK);
            colors.recycle();
        }
        return returnColor;
    }

    /**
     * Main list's content ViewHolder
     */
    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mMovieTitle;
        private TextView mYear;
        private MathView mathView;

        public ViewHolder(View itemView) {
            super(itemView);

            mMovieTitle = (TextView) itemView.findViewById(R.id.movie_title);
            mathView = (MathView) itemView.findViewById(R.id.equationView);
            mYear = (TextView) itemView.findViewById(R.id.movie_year);

            FlexboxLayout flexbox = (FlexboxLayout) itemView.findViewById(R.id.flexbox);

            ChipCloudConfig config = new ChipCloudConfig()
                    .selectMode(ChipCloud.SelectMode.multi)
                    .checkedChipColor(Color.parseColor("#ddaa00"))
                    .checkedTextColor(Color.parseColor("#ffffff"))
                    .uncheckedChipColor(Color.parseColor("#efefef"))
                    .uncheckedTextColor(Color.parseColor("#666666"));

            chipCloud = new ChipCloud(context, flexbox, config);

            itemView.setTag(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }
    }

    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ProgressBar mProgressBar;
        private ImageButton mRetryBtn;
        private TextView mErrorTxt;
        private LinearLayout mErrorLayout;

        public LoadingVH(View itemView) {
            super(itemView);

            mProgressBar = (ProgressBar) itemView.findViewById(R.id.loadmore_progress);
            mRetryBtn = (ImageButton) itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt = (TextView) itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout = (LinearLayout) itemView.findViewById(R.id.loadmore_errorlayout);

            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:

                    showRetry(false, null);
                    mCallback.retryPageLoad();

                    break;
            }
        }
    }
}
