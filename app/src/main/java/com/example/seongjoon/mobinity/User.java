package com.example.seongjoon.mobinity;

public class User {
    private String userName;
    private String profileImagePath;
    private String userEmail;
    private long userID;

    private User(){
        userName = "Android";
        profileImagePath = "@mipmap/ic_launcher_round";
        userEmail = "google@gmail.com";
        userID = 12345;
    }


    // User를 singleton 클래스로 만들기 위한 클래스
    private static class Holder{
        public static final User instance = new User();
    }

    public static User getInstance(){
        return Holder.instance;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }
    public void setProfileImagePath(String profileImagePath){
        this.profileImagePath = profileImagePath;
    }
    public void setUserEmail(String userEmail){
        this.userEmail = userEmail;
    }
    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }
    public String getProfileImagePath() {
        return profileImagePath;
    }
    public String getUserEmail() {
        return userEmail;
    }
    public long getUserID() {
        return userID;
    }
}
