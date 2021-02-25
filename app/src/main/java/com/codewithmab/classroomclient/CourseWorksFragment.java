package com.codewithmab.classroomclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class CourseWorksFragment extends Fragment {

    List<CourseWorkItem> courseWorkItems;
    RecyclerView recyclerView;
    String courseId;

    LinearLayoutManager lin;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(getActivity() != null)
            getActivity().setTitle("Assignments");

        View v = inflater.inflate(R.layout.course_details_fragment, container, false);

        courseId = getArguments().getString("courseId");
        courseWorkItems = new ArrayList<>();

        recyclerView = v.findViewById(R.id.course_details_recyclerView);
        lin = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(lin);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);
        popupLayout();
        return v;
    }

    public void popupLayout(){
        List<CourseWorkItem> courseWorkItems = DbHelper.getInstance(getContext()).getAllCourseWorkItem(courseId);
        if(this.courseWorkItems.size() == courseWorkItems.size())
            return;
        this.courseWorkItems = courseWorkItems;
        recyclerView.setAdapter(new CourseWorkAdapter((CourseDetailsActivity) getActivity(), this.courseWorkItems));
        if(getActivity()!=null)
            ((CourseDetailsActivity)getActivity()).showMsg("Fetching new CourseWorks ...");
    }
}

class CourseWorkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<CourseWorkItem> li;
    CourseDetailsActivity courseDetailsActivity;
    private static final String CARD_COLOR = "card_color";
    SharedPreferences sharedPreferences;
    String color;
    public CourseWorkAdapter(CourseDetailsActivity courseDetailsActivity, List<CourseWorkItem> li){
        this.li = li;
        this.courseDetailsActivity = courseDetailsActivity;
        sharedPreferences = courseDetailsActivity.getSharedPreferences(CARD_COLOR, Context.MODE_PRIVATE);
        color = sharedPreferences.getString(CARD_COLOR, "White");
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.coursework_layout, parent, false);
        return new CourseWorkViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((CourseWorkViewHolder) holder).title.setText(li.get(position).getTitle());
        ((CourseWorkViewHolder) holder).creationTime.setText("Created :" + li.get(position).getFormattedCreationTime());
        ((CourseWorkViewHolder) holder).dueTime.setText("Due :" + li.get(position).getDueDate());
        ((CourseWorkViewHolder) holder).description.setText(li.get(position).getDescription());


        switch (color) {
            case "White":
                ((CourseWorkViewHolder) holder).cardView.setCardBackgroundColor(courseDetailsActivity.getResources().getColor(R.color.white));
                break;
            case "Green":
                ((CourseWorkViewHolder) holder).cardView.setCardBackgroundColor(courseDetailsActivity.getResources().getColor(R.color.green));
                ((CourseWorkViewHolder) holder).title.setTextColor(courseDetailsActivity.getResources().getColor(R.color.black));
                ((CourseWorkViewHolder) holder).creationTime.setTextColor(courseDetailsActivity.getResources().getColor(R.color.black));
                ((CourseWorkViewHolder) holder).dueTime.setTextColor(courseDetailsActivity.getResources().getColor(R.color.black));
                ((CourseWorkViewHolder) holder).description.setTextColor(courseDetailsActivity.getResources().getColor(R.color.black));
                break;
            case "Yellow":
                ((CourseWorkViewHolder) holder).cardView.setCardBackgroundColor(courseDetailsActivity.getResources().getColor(R.color.yellow));
                ((CourseWorkViewHolder) holder).title.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                ((CourseWorkViewHolder) holder).creationTime.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                ((CourseWorkViewHolder) holder).dueTime.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                ((CourseWorkViewHolder) holder).description.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                break;
            case "Blue":
                ((CourseWorkViewHolder) holder).cardView.setCardBackgroundColor(courseDetailsActivity.getResources().getColor(R.color.blue));
                ((CourseWorkViewHolder) holder).title.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                ((CourseWorkViewHolder) holder).creationTime.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                ((CourseWorkViewHolder) holder).dueTime.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                ((CourseWorkViewHolder) holder).description.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return li.size();
    }

    public static class CourseWorkViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;

        TextView title;
        TextView creationTime;
        TextView dueTime;
        TextView description;

        public CourseWorkViewHolder(View v){
            super(v);

            cardView = v.findViewById(R.id.coursework_cardview);
            title = v.findViewById(R.id.coursework_title);
            creationTime = v.findViewById(R.id.coursework_creation_time);
            dueTime = v.findViewById(R.id.coursework_due_time);
            description = v.findViewById(R.id.coursework_description);
        }
    }
}
