package com.codewithmab.classroomclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Collections;
import java.util.List;

/**
 * Fragment that displays Course Announcement
 * The containing activity invokes its popupLayout() method display updated content to Users
 */
public class AnnouncementsFragment extends Fragment {
    RecyclerView recyclerView;
    String courseId;

    List<CourseDetailsItem> courseDetailsItems;
    LinearLayoutManager lin;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(getActivity() != null)
            getActivity().setTitle(" Classroom Announcement");
        //Identity for the given classroom course - the argument is always passed - its null safe
        courseId = getArguments().getString("courseId");

        courseDetailsItems = new ArrayList<>();
        View v = inflater.inflate(R.layout.course_details_fragment, container, false);
        recyclerView = v.findViewById(R.id.course_details_recyclerView);
        lin = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(lin);

        //The main purpose of this line is to prevent android from recycling this View
        //it being recycled disrupts the layout of the Material objects when it's being re-binded
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        popupLayout();
        return v;
    }

    public void popupLayout(){
        //Gets the course details from the Database - the database is the source all items, no item is passed directly after being fetched
        List<CourseDetailsItem> courseDetailsItems = DbHelper.getInstance(getContext()).getAllCourseDetailsItem(courseId);
        //The idea behind this is that The Announcements displayed must be Updated at this point
        if(this.courseDetailsItems.size() == courseDetailsItems.size())
            return;

        if(getActivity() !=null){
            //Only setting the adapter of the recycler once
            // as its guarantees that courseDetailsItems instance variable wouldn't 0 after this call
            if(this.courseDetailsItems.size() == 0){
                this.courseDetailsItems = courseDetailsItems;
                recyclerView.setAdapter(new AnnouncementsAdapter((CourseDetailsActivity) getActivity(), this.courseDetailsItems));
            } else {
                //Gets all newly added announcements
                //the idea is to only add newly added item to the courseDetailsItems instance variable - and then update the view
                List<CourseDetailsItem> addedItems = getAddedAnnouncementItems(this.courseDetailsItems, courseDetailsItems);
                if(addedItems.size() > 0){
                    if(addedItems.get(0).getTime() > this.courseDetailsItems.get(0).getTime())
                        this.courseDetailsItems.addAll(0,addedItems);
                    else this.courseDetailsItems.addAll(addedItems);
                    recyclerView.getAdapter().notifyDataSetChanged();
                }

            }

            ((CourseDetailsActivity)getActivity()).showMsg("Fetching new Announcements ...");
        }
    }

    public List<CourseDetailsItem> getAddedAnnouncementItems(List<CourseDetailsItem> oldList, List<CourseDetailsItem> newList){
        ArrayList<CourseDetailsItem> addedItems = new ArrayList<>(newList);
        addedItems.removeAll(oldList);
        Collections.sort(addedItems, Collections.reverseOrder());
        return addedItems;
    }
}

class AnnouncementsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<CourseDetailsItem> li;
    CourseDetailsActivity courseDetailsActivity;
    private static final String CARD_COLOR = "card_color";
    SharedPreferences sharedPreferences;
    String color;
    int backgroundColor;
    int textColor;
    public AnnouncementsAdapter(CourseDetailsActivity courseDetailsActivity, List<CourseDetailsItem> li){
        this.li = li;
        this.courseDetailsActivity = courseDetailsActivity;
        sharedPreferences = courseDetailsActivity.getSharedPreferences(CARD_COLOR, Context.MODE_PRIVATE);
        color = sharedPreferences.getString(CARD_COLOR, "White");

        backgroundColor = courseDetailsActivity.getResources().getColor(R.color.white);
        textColor = courseDetailsActivity.getResources().getColor(R.color.black);
    }
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

        switch (color) {
            case "White":
                ((AnnouncementViewHolder) holder).cardView.setCardBackgroundColor(courseDetailsActivity.getResources().getColor(R.color.white));
                break;
            case "Green":
                textColor = courseDetailsActivity.getResources().getColor(R.color.black);
                backgroundColor = courseDetailsActivity.getResources().getColor(R.color.green);
                ((AnnouncementViewHolder) holder).cardView.setCardBackgroundColor(courseDetailsActivity.getResources().getColor(R.color.green));
                ((AnnouncementViewHolder) holder).message.setTextColor(courseDetailsActivity.getResources().getColor(R.color.black));
                ((AnnouncementViewHolder) holder).time.setTextColor(courseDetailsActivity.getResources().getColor(R.color.black));
                ((AnnouncementViewHolder) holder).author.setTextColor(courseDetailsActivity.getResources().getColor(R.color.black));
                break;
            case "Yellow":
                textColor = courseDetailsActivity.getResources().getColor(R.color.white);
                backgroundColor = courseDetailsActivity.getResources().getColor(R.color.yellow);
                ((AnnouncementViewHolder) holder).cardView.setCardBackgroundColor(courseDetailsActivity.getResources().getColor(R.color.yellow));
                ((AnnouncementViewHolder) holder).message.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                ((AnnouncementViewHolder) holder).time.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                ((AnnouncementViewHolder) holder).author.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                break;
            case "Blue":
                textColor = courseDetailsActivity.getResources().getColor(R.color.white);
                backgroundColor = courseDetailsActivity.getResources().getColor(R.color.blue);
                ((AnnouncementViewHolder) holder).cardView.setCardBackgroundColor(courseDetailsActivity.getResources().getColor(R.color.blue));
                ((AnnouncementViewHolder) holder).message.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                ((AnnouncementViewHolder) holder).time.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                ((AnnouncementViewHolder) holder).author.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                break;
        }
        for(Material material : li.get(position).getMaterials()){
            Button btn = new Button(holder.itemView.getContext());
            btn.setText(material.getLabel());
            btn.setPaintFlags(btn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            btn.setTextColor(textColor);
            //btn.setBackgroundColor(backgroundColor);
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