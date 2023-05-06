package foo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.api.server.spi.auth.common.User;

public class Petition {
    public int id;
    public String nom;
    public String description;
    public int nbVotants;
    public User createur;
    public Date dateCreation;
    public ArrayList<String> tags;
    public List<String> votants;
    
}
