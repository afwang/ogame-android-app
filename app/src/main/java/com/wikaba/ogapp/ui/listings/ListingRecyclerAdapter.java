package com.wikaba.ogapp.ui.listings;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.models.AbstractItemInformation;
import com.wikaba.ogapp.events.abstracts.OnAbstractListInformationLoaded;
import com.wikaba.ogapp.utils.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by kevinleperf on 23/07/15.
 */
public class ListingRecyclerAdapter extends RecyclerView.Adapter {
    public OnAbstractListInformationLoaded _loaded_information;
    private final static int TEXTVIEWHOLDER = 0;
    private final static int LOADINGHOLDER = 1;
    private final static int ERRORHOLDER = 2;

    public class ListingHolder extends RecyclerView.ViewHolder {
        private AbstractItemInformation _item;

        @Bind(R.id.header)
        public View _header;

        @Bind(R.id.name)
        public TextView _name;

        @Bind(R.id.cost_metal)
        public TextView _metal;

        @Bind(R.id.cost_crystal)
        public TextView _crystal;

        @Bind(R.id.cost_deuterium)
        public TextView _deuterium;

        @Bind(R.id.level)
        public TextView _level;


        public ListingHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setAbstractItemInformation(AbstractItemInformation item) {
            _item = item;
        }
    }

    public class SimpleHolder extends RecyclerView.ViewHolder {

        public SimpleHolder(View itemView) {
            super(itemView);
        }
    }

    public ListingRecyclerAdapter() {
        _loaded_information = null;
    }

    public ListingRecyclerAdapter(OnAbstractListInformationLoaded loaded_information) {
        this();
        _loaded_information = loaded_information;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        switch (type) {
            case TEXTVIEWHOLDER:
                return new ListingHolder(LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.view_buildable, viewGroup, false));
            case ERRORHOLDER:
                return new SimpleHolder(LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.view_buildable_error, viewGroup, false));
            default:
                return new SimpleHolder(LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.view_buildable_loading, viewGroup, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (_loaded_information != null
                && Constants.Status.LOADED.equals(_loaded_information.getStatus())) {
            return TEXTVIEWHOLDER;
        } else if (_loaded_information != null
                && Constants.Status.LOADING.equals(_loaded_information.getStatus())) {
            return LOADINGHOLDER;
        }
        return ERRORHOLDER;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ListingHolder) {
            ListingHolder v = (ListingHolder) viewHolder;

            AbstractItemInformation information = _loaded_information.getListOfItemRetrieved()
                    .get(position);

            v.setAbstractItemInformation(information);
            v._name.setText(information.getItemRepresentation().getResourceString());
            v._level.setText(Long.toString(information.getLevelOrCount()));
            v._header.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            v._metal.setText(v.itemView.getContext().getString(R.string.cost_metal,
                    information.getMetalCost()));
            v._crystal.setText(v.itemView.getContext().getString(R.string.cost_crystal,
                    information.getCrystalCost()));
            v._deuterium.setText(v.itemView.getContext().getString(R.string.cost_deuterium,
                    information.getDeuteriumCost()));
        }
    }

    @Override
    public int getItemCount() {
        if (_loaded_information != null && _loaded_information.getListOfItemRetrieved() != null) {
            return _loaded_information.getListOfItemRetrieved().size();
        }
        return 1;
    }
}