package GUI.SessionManager;

import Logic.DTOs.User;

public class SessionManager {
    private static SessionManager instance;
    private User user;

    private SessionManager(){}

    public static SessionManager getInstance() {
        if(instance == null){
            instance = new SessionManager();
        }
        return instance;
    }

    public User getUsuario(){
        return user;
    }

    public void login(User user){
        this.user = user;
    }

    public void logout(){
        this.user = null;
    }
}
