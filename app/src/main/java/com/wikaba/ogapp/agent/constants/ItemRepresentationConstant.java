package com.wikaba.ogapp.agent.constants;

import android.support.annotation.NonNull;

import com.wikaba.ogapp.agent.models.ItemRepresentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevinleperf on 22/07/15.
 */

public abstract class ItemRepresentationConstant {
    protected ItemRepresentationConstant() {

    }

    protected final List<ItemRepresentation> _items = new ArrayList<>();

    @NonNull
    public final List<ItemRepresentation> toList() {
        return _items;
    }

    public final ItemRepresentation[] toArray() {
        return (ItemRepresentation[]) toList().toArray();
    }

}
