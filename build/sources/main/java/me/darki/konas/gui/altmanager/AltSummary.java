package me.darki.konas.gui.altmanager;

public class AltSummary implements Comparable<AltSummary>
{
    private String name;
    private String email;
    private String password;
    private String uuid = "";
    private String token = "";
    private boolean cracked;
    private boolean loggedIn;
    private boolean microsoft;
    private long lastTimeLoggedIn;

    public AltSummary(String email, String password, boolean cracked, boolean microsoft)
    {
        this.email = email;
        this.name = email;
        this.password = password;
        this.cracked = cracked;
        this.microsoft = microsoft;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isCracked() {
        return cracked;
    }

    public void setCracked(boolean cracked) {
        this.cracked = cracked;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public boolean isMicrosoft() {
        return microsoft;
    }

    public long getLastTimeLoggedIn() {
        return lastTimeLoggedIn;
    }

    public void setLastTimeLoggedIn(long lastTimeLoggedIn) {
        this.lastTimeLoggedIn = lastTimeLoggedIn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int compareTo(AltSummary comparison)
    {
        if (this.lastTimeLoggedIn < comparison.lastTimeLoggedIn)
        {
            return 1;
        }
        else
        {
            return this.lastTimeLoggedIn > comparison.lastTimeLoggedIn ? -1 : this.name.compareTo(comparison.name);
        }
    }
}
