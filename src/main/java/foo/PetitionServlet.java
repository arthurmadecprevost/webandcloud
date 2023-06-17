package foo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import com.google.appengine.api.datastore.Entity;

@WebServlet(name = "PetServlet", urlPatterns = { "/petition" })
public class PetitionServlet extends HttpServlet {

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        Random r = new Random();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Create petition
        for (int i = 0; i < 1000; i++) {
            Entity e = new Entity("Petition");
            int owner=r.nextInt(500);
            e.setProperty("createurId", "U"+ owner);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
            Date date = new Date();
            e.setProperty("dateCreation", formatter.format(date));
            e.setProperty("nom", "["+i+"] Pétition pour le retour de Groquik");
            e.setProperty("description", "Le Lorem Ipsum est simplement du faux texte employé dans la composition et la mise en page avant impression. Le Lorem Ipsum est le faux texte standard de l'imprimerie depuis les années 1500, quand un imprimeur anonyme assembla ensemble des morceaux de texte pour réaliser un livre spécimen de polices de texte. Il n'a pas fait que survivre cinq siècles, mais s'est aussi adapté à la bureautique informatique, sans que son contenu n'en soit modifié. Il a été popularisé dans les années 1960 grâce à la vente de feuilles Letraset contenant des passages du Lorem Ipsum, et, plus récemment, par son inclusion dans des applications de mise en page de texte, comme Aldus PageMaker.");
            e.setProperty("image", "https://picsum.photos/400/300");
            e.setProperty("objectif", i + 100);

            // Create random votants
            HashSet<String> fset = new HashSet<String>();
            for (int j=0;j<200;j++) {
                fset.add("U" + r.nextInt(500));
            }
            e.setProperty("votants", fset);
            e.setProperty("nbVotants", i);

            // Create random tags
            HashSet<String> ftags = new HashSet<String>();
            while (ftags.size() < 100) {
                ftags.add("T" + r.nextInt(200));
            }
            e.setProperty("tags", ftags);

            datastore.put(e);
        }

        // Create utilisateurs
        for(int i = 0; i < 1000; i++){
            // Create one user to initialise the table in datastore 
            Entity e = new Entity("Utilisateur");

            // Create random signatures
            HashSet<String> fSign = new HashSet<String>();
            while (fSign.size() < 500) {
                fSign.add("S" + r.nextInt(500));
            }
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
            Date date = new Date();
            e.setProperty("creationDate", formatter.format(date));
            e.setProperty("signatures", fSign);
            e.setProperty("nomComplet", "Jean Bon");
            datastore.put(e);
        }
    }
}