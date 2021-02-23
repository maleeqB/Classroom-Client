package com.codewithmab.classroomclient;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithmab.classroomclient.Db.DbHelper;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementsFragment extends Fragment {
    RecyclerView recyclerView;
    String courseId;

    List<CourseDetailsItem> courseDetailsItems;
    LinearLayoutManager lin;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        courseId = getArguments().getString("courseId");

        courseDetailsItems = new ArrayList<>();
        View v = inflater.inflate(R.layout.course_details_fragment, container, false);
        recyclerView = v.findViewById(R.id.course_details_recyclerView);
        lin = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(lin);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);


        popupLayout();
        return v;
    }

    public void popupLayout(){
        List<CourseDetailsItem> courseDetailsItems = DbHelper.getInstance(getContext()).getAllCourseDetailsItem(courseId);
        if(this.courseDetailsItems.size() == courseDetailsItems.size())
            return;
        this.courseDetailsItems = courseDetailsItems;

        recyclerView.setAdapter(new AnnouncementsAdapter(this.courseDetailsItems));
        if(getActivity() !=null)
            ((CourseDetailsActivity)getActivity()).showMsg("Fetching new Announcements ...");
    }
}

class AnnouncementsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<CourseDetailsItem> li;
    public AnnouncementsAdapter(List<CourseDetailsItem> li){this.li = li;}
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.announcements_layout, parent, false);
        return new AnnouncementViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ((AnnouncementViewHolder) holder).message.setText(li.get(position).getMessage());
        ((AnnouncementViewHolder) holder).time.setText(li.get(position).getFormattedTime());
        ((AnnouncementViewHolder) holder).author.setText(li.get(position).getAuthor());

        for(Material material : li.get(position).getMaterials()){
            Button btn = new Button(holder.itemView.getContext());
            btn.setText(material.getLabel());
            btn.setPaintFlags(btn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            btn.setOnClickListener((v)->{
                Uri uri = Uri.parse(material.getLink());
                holder.itemView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));

            });
            ((AnnouncementViewHolder) holder).layout.addView(btn);
        }

    }

    @Override
    public int getItemCount() {
        return li.size();
    }

    public static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        TextView time;
        TextView author;

        CardView cardView;
        LinearLayout layout;
        public AnnouncementViewHolder(View v) {
            super(v);
            message = v.findViewById(R.id.course_message);
            time = v.findViewById(R.id.course_time);
            author = v.findViewById(R.id.course_author);

            cardView = v.findViewById(R.id.announcement_cardview);
            layout = cardView.findViewById(R.id.announcement_linearlayout);
        }
    }
}