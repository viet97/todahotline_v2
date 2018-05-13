package org.linphone.model;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.todahotline.NewMessageActivity;

import org.linphone.R;
import org.linphone.database.DbContext;
import org.linphone.network.models.ContactResponse;
import org.linphone.ultils.ContactUltils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by QuocVietDang1 on 4/29/2018.
 */

public class CustomArrayAdapter extends ArrayAdapter<ContactResponse.DSDanhBa> {
    List<ContactResponse.DSDanhBa> listDs;
    List<ContactResponse.DSDanhBa> suggestions;
    List<ContactResponse.DSDanhBa> result = new ArrayList<>();
    Context context;
    private int viewResourceId;
    private String TAG = "CustomArrayAdapter";
    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence s) {
            try {
                String keyWord;
                keyWord = ContactUltils.instance.removeAccents(s.toString());
                if (keyWord != null) {
                    result.clear();

                    for (ContactResponse.DSDanhBa ds : suggestions) {

                        if (ContactUltils.instance.removeAccents(ds.getSodienthoai()).contains(keyWord) ||
                                ContactUltils.instance.removeAccents(ds.getJob()).contains(keyWord) ||
                                ContactUltils.instance.removeAccents(ds.getTenlienhe()).contains(keyWord) ||
                                ContactUltils.instance.removeAccents(ds.getSodienthoai()).startsWith(keyWord) ||
                                ContactUltils.instance.removeAccents(ds.getJob()).startsWith(keyWord) ||
                                ContactUltils.instance.removeAccents(ds.getTenlienhe()).startsWith(keyWord)) {
                            Log.d(TAG, "performFiltering: add");
                            result.add(ds);
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    if (result.size() > 3) {
                        NewMessageActivity.inputUsers.setDropDownHeight(450);
                    } else {
                        NewMessageActivity.inputUsers.setDropDownHeight(result.size() * 150);
                    }
                    filterResults.values = result;
                    filterResults.count = result.size();
                    return filterResults;
                } else {
                    Log.d(TAG, "performFiltering: else {");
                    return new FilterResults();
                }
            } catch (Exception e) {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            List<ContactResponse.DSDanhBa> filterList = (List<ContactResponse.DSDanhBa>) filterResults.values;
            if (filterResults != null && filterResults.count > 0) {
                clear();
                for (ContactResponse.DSDanhBa ds : filterList) {
                    add(ds);

                }
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }

        }
    };

    @Nullable
    @Override
    public ContactResponse.DSDanhBa getItem(int position) {
        try {
            return result.get(position);
        } catch (Exception e) {
            return new ContactResponse.DSDanhBa();
        }

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = null;
        if (convertView != null) {
            view = convertView;
        } else {
            view = inflater.inflate(R.layout.list_auto_complete, parent, false);
        }
        if (result.size() > 0) {
            Log.d(TAG, "getView: " + view.getHeight());
            TextView nameView = view.findViewById(R.id.tv_name);
            TextView phoneView = view.findViewById(R.id.tv_ext);
            TextView job = view.findViewById(R.id.tv_job);
            ContactResponse.DSDanhBa currentDanhba = result.get(position);
            nameView.setText(currentDanhba.getTenlienhe());
            phoneView.setText(currentDanhba.getSodienthoai());
            job.setText(currentDanhba.getJob());
        }
        return view;

    }

    @Override
    public int getCount() {
        return result.size();
    }

    public CustomArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ContactResponse.DSDanhBa> objects) {
        super(context, resource, objects);
        this.viewResourceId = resource;
        this.suggestions = DbContext.getInstance().getContactResponse(context).getDsdanhba();
        this.context = context;
    }


    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }
}
