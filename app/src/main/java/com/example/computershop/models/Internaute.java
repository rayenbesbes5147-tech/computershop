package com.example.computershop.models;

public class Internaute {
    private String idInt;
    private String login;
    private String password;
    private String dateInscrip;
    private String pays;

    public Internaute() {
        // Required empty constructor for Firebase
    }

    public Internaute(String idInt, String login, String password, String dateInscrip, String pays) {
        this.idInt = idInt;
        this.login = login;
        this.password = password;
        this.dateInscrip = dateInscrip;
        this.pays = pays;
    }

    public String getIdInt() { return idInt; }
    public void setIdInt(String idInt) { this.idInt = idInt; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDateInscrip() { return dateInscrip; }
    public void setDateInscrip(String dateInscrip) { this.dateInscrip = dateInscrip; }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }
}
