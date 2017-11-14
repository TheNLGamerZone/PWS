package nl.hetbaarnschlyceum.pws.server.tc.client;

import java.util.UUID;

public class Client {
    private String name;            // Naam van de gebruiker
    private boolean public_cl;      // Boolean die aangeeft of een gebruiker te vinden is
    private int number;             // Nummer van de gebruiker
    private int RSA_public;         // Publieke RSA sleutel van de gebruiker
    private String hash;            // Gehashte wachtwoord van de gebruiker
    private boolean hidden;         // Boolean die aangeeft of de gebruiker een whitelist heeft
    private int[] whitelist;        // Array met nummers die op de whitelist staan
    private UUID uuid;              // Universally unique identifier van de gebruiker
    private int status;             // Status van de gebruiker die aangeeft of de gebruiker online is
    private String session_key;     // De AES-sleutel voor de huidige sessie

    public Client(int number, UUID uuid)
    {
        this.number = number;
        this.uuid = uuid;

        loadPlayer();
    }

    private void loadPlayer()
    {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublic_cl() {
        return public_cl;
    }

    public void setPublic_cl(boolean public_cl) {
        this.public_cl = public_cl;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getRSA_public() {
        return RSA_public;
    }

    public void setRSA_public(int RSA_public) {
        this.RSA_public = RSA_public;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public int[] getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(int[] whitelist) {
        this.whitelist = whitelist;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSession_key() {
        return session_key;
    }

    public void setSession_key(String session_key) {
        this.session_key = session_key;
    }
}
