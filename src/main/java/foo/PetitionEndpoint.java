package foo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.UnauthorizedException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Transaction;

@Api(name = "petiQuik",
     version = "v1",
     audiences = "115500541537-rapfqet1t2qmbv79oqc5dcfqdebopj1g.apps.googleusercontent.com",
  	 clientIds = {"115500541537-rapfqet1t2qmbv79oqc5dcfqdebopj1g.apps.googleusercontent.com"},
     namespace =
     @ApiNamespace(
		   ownerDomain = "helloworld.example.com",
		   ownerName = "helloworld.example.com",
		   packagePath = "")
     )
@SuppressWarnings("unchecked")
public class PetitionEndpoint {

    @ApiMethod(name = "checkCreatedUser", httpMethod = HttpMethod.GET)
	public Entity checkCreatedUser (User user, PostMessage pm) throws UnauthorizedException {
        if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key utilisateurId = KeyFactory.createKey("Utilisateur", user.getId()); 
        Entity utilisateurEntity;
        try {
            utilisateurEntity = datastore.get(utilisateurId);
        } catch (EntityNotFoundException e) {
            utilisateurEntity = new Entity("Utilisateur", user.getId());
            utilisateurEntity.setProperty("nomComplet", pm.body);
            utilisateurEntity.setProperty("signatures", new ArrayList<String>());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");  
            Date date = new Date();
            utilisateurEntity.setProperty("creationDate", formatter.format(date));
            datastore.put(utilisateurEntity);
        }

		return utilisateurEntity;
	}

    @ApiMethod(name = "top50Paginated", path="getPetitions/{pageId}", httpMethod = HttpMethod.GET)
    public List<Entity> top50Paginated(@Named("pageId") int page) {
        int index = (page - 1) * 50;
        Query query = new Query("Petition").addSort("nbVotants", SortDirection.DESCENDING);

        // Mise en place de projection pour optimiser la requete
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        query.addProjection(new PropertyProjection("nom", String.class));
        query.addProjection(new PropertyProjection("description", String.class));
        query.addProjection(new PropertyProjection("image", String.class));
        query.addProjection(new PropertyProjection("nbVotants", Long.class));

        PreparedQuery pq = datastore.prepare(query);
        List<Entity> result = pq.asList(FetchOptions.Builder.withOffset(index).limit(50));
        return result;
    }
	
	@ApiMethod(name = "top4", httpMethod = HttpMethod.GET)
	public List<Entity> top4() {
		Query q = new Query("Petition").addSort("nbVotants", SortDirection.DESCENDING);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(4));
		return result;
	}
	
	/**
	 * @param user User
	 * @return Une liste de pétitions
	 * @throws UnauthorizedException
	 */
	@ApiMethod(name = "mesPetitions", httpMethod = HttpMethod.GET)
	public List<Entity> mesPetitions(User user) throws UnauthorizedException {
        if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

		Query q = new Query("Petition").setFilter(new Query.FilterPredicate("createurId",
                Query.FilterOperator.EQUAL, user.getId()));
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(50));
		return result;
	}

    @ApiMethod(name = "mesSignatures", httpMethod = HttpMethod.GET)
	public List<Entity> mesSignatures(User user) throws UnauthorizedException, EntityNotFoundException {
        if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

        Key userKey = KeyFactory.createKey("Utilisateur",user.getId());

        List<Entity> result = new ArrayList<Entity>();
		Query q = new Query("Utilisateur").setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY,
                Query.FilterOperator.EQUAL, userKey));
        
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity utilisateur = datastore.prepare(q).asSingleEntity();
        List<String> petitionSignees = (List<String>) utilisateur.getProperty("signatures");
        if (petitionSignees == null) {
            petitionSignees = new ArrayList<String>();
        }
        // Requetage avec index pour ne pas renvoyer toutes les pétitions signées
        int limit;
        if (petitionSignees.size() > 100) {
            limit = 100;
        } else {
            limit = petitionSignees.size();
        }

        for(int i = 0; i < limit; i++){
            Key key = KeyFactory.createKey("Petition", Long.parseLong(petitionSignees.get(i)));
            result.add(datastore.get(key));
        }
		return result;
	}

    //RECUPERATION D'UNE PETITION
    @ApiMethod(name = "getPetition", path="getPetition/{petitionId}", httpMethod = HttpMethod.GET)
    public Entity getPetition(@Named("petitionId") String petId) {
            
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Récuperation de la pétition
        Key petitionKey = KeyFactory.createKey("Petition", Long.parseLong(petId));
        Query.Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, petitionKey);
        Query petitionQuery = new Query("Petition").setFilter(keyFilter);
    
        // Récuperation de l'utilisateur
        Entity petition = datastore.prepare(petitionQuery).asSingleEntity();
        
        Key userKey = KeyFactory.createKey("Utilisateur", (String) petition.getProperty("createurId"));
        Query q = new Query("Utilisateur").setFilter(new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY,
                Query.FilterOperator.EQUAL, userKey));
        Entity utilisateur = datastore.prepare(q).asSingleEntity();

        String fullName;
        try {
            fullName = (String) utilisateur.getProperty("nomComplet");
        } catch (NullPointerException e) {
            fullName = "Utilisateur inexistant";
        }

        // On modifie la propriété full name pour l'afficher dans le front
        petition.setProperty("nomCreateur", fullName);

        return petition;
    }

    @ApiMethod(name = "addPetition", httpMethod = HttpMethod.POST)
	public Entity addPetition(User user, Petition petition) throws Exception{ 
		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		Entity e = new Entity("Petition");
		e.setProperty("description", petition.description);
        e.setProperty("nom", petition.nom);
        e.setProperty("image", petition.image);
		e.setProperty("createurId", user.getId());
		e.setProperty("nbVotants", 0);
		e.setProperty("objectif", petition.objectif);
		e.setProperty("votants", new ArrayList<>());
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
	    Date date = new Date();  
	    e.setProperty("datetri", date.getTime());
		e.setProperty("dateCreation", formatter.format(date));
        
        String[] valuesArray = petition.tags.split(",");
        List<String> valuesList = Arrays.asList(valuesArray);
		e.setProperty("tags", valuesList);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);
		return e;
	}

    @ApiMethod(name = "signPetition", path = "signPetition/{userID}/{petitionId}", httpMethod = HttpMethod.PUT) 
	public Entity signPetition(@Named("userID") String user, @Named("petitionId") String petId) throws Exception{
		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		if(petId == null){
		    throw new IllegalArgumentException("La pétition ne peut pas être nulle.");
		}
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Definition des clés et des entités
        Key petitionId = KeyFactory.createKey("Petition", Long.parseLong(petId)); 
        Key utilisateurId = KeyFactory.createKey("Utilisateur", user); 
        Entity petitionEntity = null; 
        Entity utilisateurEntity;

        // Début de la transaction
        Transaction transact = datastore.beginTransaction(); 
        try{
            // Récuperation des entités
            petitionEntity = datastore.get(petitionId); 
            utilisateurEntity = datastore.get(utilisateurId);

            // Récuperation des propriétés à modifier
            ArrayList<String> signatures = (ArrayList<String>)utilisateurEntity.getProperty("signatures");          
            long nbVotants = (long) petitionEntity.getProperty("nbVotants");
            ArrayList<String> votants = (ArrayList<String>) petitionEntity.getProperty("votants");
            if(votants == null) {
                votants = new ArrayList<>();
            }
            if(signatures == null) {
                signatures = new ArrayList<>();
            }

            // Modification des propriétés des pétitions si l'utilisateur n'a pas déjà signé
            if(!votants.contains(user)) {
                votants.add(user);
                petitionEntity.setProperty("votants", votants); 
                petitionEntity.setProperty("nbVotants", nbVotants + 1 ); 
            }

            // Modification des propriétés de l'utilisateur si la pétition n'est pas déjà dans ses signatures
            if(!signatures.contains(petId)) {
                signatures.add(petId);
                utilisateurEntity.setProperty("signatures", signatures);
            }

            datastore.put(utilisateurEntity);
            datastore.put(petitionEntity); 
            transact.commit(); 
        } catch(Exception e){
            e.printStackTrace();
            transact.rollback();
        }
        return petitionEntity;
	 }

     @ApiMethod(name = "searchPetition", httpMethod = HttpMethod.GET)
     public List<Entity> searchPetition(PostMessage pm) {
        String type = pm.type;
        String recherche = pm.searchText;
        int page = Integer.parseInt(pm.page);

        int index = (page - 1) * 50;
        List<Entity> result;
        Query query;
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        if(type.equals("1")) {
            query = new Query("Petition").setFilter(new Query.FilterPredicate("tags",
            Query.FilterOperator.EQUAL, recherche));
        } else {
            query = new Query("Petition").setFilter(new Query.FilterPredicate("nom",
            Query.FilterOperator.EQUAL, recherche));
        }
        
        PreparedQuery pq = datastore.prepare(query);
        result = pq.asList(FetchOptions.Builder.withOffset(index).limit(50));

        return result; 
     }
}