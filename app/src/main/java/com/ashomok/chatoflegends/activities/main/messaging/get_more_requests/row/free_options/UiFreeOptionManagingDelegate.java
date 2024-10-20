// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.row.free_options;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.ashomok.chatoflegends.R;
import com.ashomok.chatoflegends.activities.main.messaging.get_more_requests.GetMoreRequestsActivity;
import com.ashomok.chatoflegends.activities.main.messaging.update_to_premium.ChatRequestsStateModel;

/**
 * Implementations of this abstract class are responsible to render UI and handle user actions for
 * promo rows to render RecyclerView
 */
public abstract class UiFreeOptionManagingDelegate {

    private final Context context;
    private int requestsCost; //each promo gives some requests amount

    protected UiFreeOptionManagingDelegate(Context context) {
        this.context = context;
    }

    public void onBindViewHolder(PromoRowFreeOptionData item, FreeOptionRowViewHolder holder) {
        requestsCost = item.getRequestsCost();

        holder.icon.setImageDrawable(context.getResources().getDrawable(item.getDrawableIconId()));
        holder.title.setText(item.getTitleStringId());
        if (item.isSubtitleExists()) {
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(item.getSubtitleStringId());
        } else {
            holder.subtitle.setVisibility(View.GONE);
        }

        holder.requestsCost.setText(context.getString(
                R.string.tokens_cost, String.valueOf(requestsCost)));


        if (isTaskAvailable()) {
            holder.isDone.setVisibility(View.GONE);
        } else {
            holder.isDone.setVisibility(View.VISIBLE);
            holder.subtitle.setVisibility(View.GONE);
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.grey_500));
            holder.requestsCost.setTextColor(ContextCompat.getColor(context, R.color.grey_500));
        }
    }

    private void showTaskIsDoneToast() {
        Toast.makeText(context,
                R.string.task_is_done, Toast.LENGTH_SHORT).show();
    }

    /**
     * task may be done and not available any more
     *
     * @return
     */
    public boolean isTaskAvailable() {
        return true;
    }

    public void onRowClicked() {
        if (isTaskAvailable()) {
            startTask();
        } else {
            showTaskIsDoneToast();
        }
    }

    public void onTaskDone(ChatRequestsStateModel chatRequestsStateModel) {
        chatRequestsStateModel.incrementTokens(requestsCost);
        String message = String.format(context.getString(R.string.tokens_obtained), requestsCost);
        ((GetMoreRequestsActivity) context).showInfo(message);
    }

    protected abstract void startTask();
}
