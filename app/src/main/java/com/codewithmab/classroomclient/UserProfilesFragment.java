package com.codewithmab.classroomclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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



public class UserProfilesFragment extends Fragment {

    List<UserProfileItem> userProfileItems;
    RecyclerView recyclerView;
    String courseId;
    LinearLayoutManager lin;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(getActivity() != null)
            getActivity().setTitle("Users");
        View v = inflater.inflate(R.layout.course_details_fragment, container, false);

        courseId = getArguments().getString("courseId");
        userProfileItems = new ArrayList<>();

        recyclerView = v.findViewById(R.id.course_details_recyclerView);
        lin = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(lin);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);
        popupLayout();
        return v;
    }


    public void popupLayout(){
        List<UserProfileItem> userProfileItems = DbHelper.getInstance(getContext()).getAllUsersProfile(courseId);
        if(this.userProfileItems.size() == userProfileItems.size())
            return;
        List<PeopleItem> peopleItems = new ArrayList<>();

        List<UserProfileItem> teachers = DbHelper.getInstance(getContext()).getTeacherProfiles(courseId);
        List<UserProfileItem> students = DbHelper.getInstance(getContext()).getStudentProfiles(courseId);

        peopleItems.add(new PeopleItem(PeopleItem.CardType.Header, new UserProfileItem(null,"Teachers",null)));

        for (UserProfileItem teacher: teachers){
            peopleItems.add(new PeopleItem(PeopleItem.CardType.SubItem, teacher));
        }
        peopleItems.add(new PeopleItem(PeopleItem.CardType.Header, new UserProfileItem(null,"Students",null)));
        for (UserProfileItem student: students){
            peopleItems.add(new PeopleItem(PeopleItem.CardType.SubItem, student));
        }

        this.userProfileItems = userProfileItems;
        recyclerView.setAdapter(new PeopleProfileAdapter((CourseDetailsActivity) getActivity(), peopleItems));
        if(getActivity() !=null)
            ((CourseDetailsActivity)getActivity()).showMsg("Fetching new User Profiles ...");
    }

}

class PeopleProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<PeopleItem> peopleItems;
    CourseDetailsActivity courseDetailsActivity;
    private static final String CARD_COLOR = "card_color";
    SharedPreferences sharedPreferences;
    String color;
    public PeopleProfileAdapter(CourseDetailsActivity courseDetailsActivity, List<PeopleItem> peopleItems){
        this.peopleItems = peopleItems;
        this.courseDetailsActivity = courseDetailsActivity;
        sharedPreferences = courseDetailsActivity.getSharedPreferences(CARD_COLOR, Context.MODE_PRIVATE);
        color = sharedPreferences.getString(CARD_COLOR, "White");
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        switch (viewType){
            case 0:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.userprofile_layout_header, parent, false);
                return new PeopleViewHolder(v);
            case 1:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.userprofile_layout_subitem, parent, false);
                return new PeopleViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((PeopleViewHolder) holder).title.setText(peopleItems.get(position).getUserProfileItem().getName());


        switch (color) {
            case "White":
                ((PeopleViewHolder) holder).cardView.setCardBackgroundColor(courseDetailsActivity.getResources().getColor(R.color.white));
                break;
            case "Green":
                ((PeopleViewHolder) holder).cardView.setCardBackgroundColor(courseDetailsActivity.getResources().getColor(R.color.green));
                ((PeopleViewHolder) holder).title.setTextColor(courseDetailsActivity.getResources().getColor(R.color.black));
                break;
            case "Yellow":
                ((PeopleViewHolder) holder).cardView.setCardBackgroundColor(courseDetailsActivity.getResources().getColor(R.color.yellow));
                ((PeopleViewHolder) holder).title.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                break;
            case "Blue":
                ((PeopleViewHolder) holder).cardView.setCardBackgroundColor(courseDetailsActivity.getResources().getColor(R.color.blue));
                ((PeopleViewHolder) holder).title.setTextColor(courseDetailsActivity.getResources().getColor(R.color.white));
                break;
        }

    }

    @Override
    public int getItemCount() {
        return peopleItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (peopleItems.get(position).getType()){
            case Header:
                return 0;
            case SubItem:
                return 1;
            default:
                return -1;
        }
    }

    public static class PeopleViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        CardView cardView;

        public PeopleViewHolder(View v){
            super(v);

            cardView = v.findViewById(R.id.user_profile_cardview);
            title=v.findViewById(R.id.text);

        }

    }
}
