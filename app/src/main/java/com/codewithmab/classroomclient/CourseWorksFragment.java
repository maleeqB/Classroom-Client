package com.codewithmab.classroomclient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        recyclerView.setAdapter(new CourseWorkAdapter(this.courseWorkItems));
        if(getActivity()!=null)
            ((CourseDetailsActivity)getActivity()).showMsg("Fetching new CourseWorks ...");
    }
}

class CourseWorkAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<CourseWorkItem> li;
    public CourseWorkAdapter(List<CourseWorkItem> li){
        this.li = li;
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
    }

    @Override
    public int getItemCount() {
        return li.size();
    }

    public static class CourseWorkViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView creationTime;
        TextView dueTime;
        TextView description;

        public CourseWorkViewHolder(View v){
            super(v);

            title = v.findViewById(R.id.coursework_title);
            creationTime = v.findViewById(R.id.coursework_creation_time);
            dueTime = v.findViewById(R.id.coursework_due_time);
            description = v.findViewById(R.id.coursework_description);
        }
    }
}
