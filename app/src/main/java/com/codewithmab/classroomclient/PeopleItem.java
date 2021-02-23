package com.codewithmab.classroomclient;

public class PeopleItem {
    public enum CardType{Header, SubItem}

    private final CardType type;
    private final UserProfileItem userProfileItem;


    public PeopleItem(CardType type, UserProfileItem userProfileItem){
        this.type = type;
        this.userProfileItem=userProfileItem;
    }

    public CardType getType() {
        return type;
    }

    public UserProfileItem getUserProfileItem() {
        return userProfileItem;
    }
}
