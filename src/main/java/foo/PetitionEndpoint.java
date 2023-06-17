package foo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.server.spi.auth.EspAuthenticator;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;

import foo.Petition;

@Api(name = "petiQuik",
     version = "v1",
     audiences = "",
  	 clientIds = {""},
     namespace =
     @ApiNamespace(
		   ownerDomain = "helloworld.example.com",
		   ownerName = "helloworld.example.com",
		   packagePath = "")
     )

@SuppressWarnings("unchecked")
public class PetitionEndpoint {

    @ApiMethod(name = "checkCreatedUser", httpMethod = HttpMethod.GET)
	public Entity createUser(User user) throws UnauthorizedException, EntityNotFoundException {
        if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key utilisateurId = KeyFactory.createKey("Utilisateur", user.getId()); 
        Entity utilisateurEntity = datastore.get(utilisateurId);
        if(utilisateurEntity == null){
            utilisateurEntity = new Entity("Utilisateur", user.getId());
            utilisateurEntity.setProperty("nomComplet", user.name);
            utilisateurEntity.setProperty("signatures", new ArrayList<String>());
            datastore.put(utilisateurEntity);
        }

		return utilisateurEntity;
	}

	@ApiMethod(name = "top100", httpMethod = HttpMethod.GET)
	public List<Entity> top100() {
		Query q = new Query("Petition").addSort("nbVotants", SortDirection.DESCENDING);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
  
		return result;
	}

    @ApiMethod(name = "top50Paginated", httpMethod = HttpMethod.GET)
	public List<Entity> top50Paginated(@Named("page") int page) {
        int index = (page - 1) * 50;
		Query q = new Query("Petition").addSort("nbVotants", SortDirection.DESCENDING);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withOffset(index).limit(50));
		return result;
	}
	
	@ApiMethod(name = "new4", httpMethod = HttpMethod.GET)
	public List<Entity> new4() {
		Query q = new Query("Petition").addSort("datetri", SortDirection.DESCENDING);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(4));
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
	 * 
	 * @param user User
	 * @return Une liste de pétitions
	 * @throws UnauthorizedException
	 */
	@ApiMethod(name = "mesPetitions", httpMethod = HttpMethod.GET)
	public List<Entity> mesPetitions(User user, @Named("page") int page) throws UnauthorizedException {
        if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
        int index = (page - 1) * 50;
		Query q = new Query("Petition").setFilter(new Query.FilterPredicate("owner",
                Query.FilterOperator.EQUAL, user.getId()));
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withOffset(index).limit(50));
		return result;
	}

	@ApiMethod(name = "getPetition", path="getPetition/{petitionId}", httpMethod = HttpMethod.GET)
    public Entity getPetition(@Named("petitionId") String petId) {
            
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Récuperation de la pétition
        Key petitionKey = KeyFactory.createKey("Petition", Long.parseLong(petId));
        Query.Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, petitionKey);
        Query petitionQuery = new Query("Petition").setFilter(keyFilter);
    
        // Récuperation de l'utilisateur
        Entity petition = datastore.prepare(petitionQuery).asSingleEntity();
        Query q = new Query("Utilisateur").setFilter(new Query.FilterPredicate("id",
                Query.FilterOperator.EQUAL, petition.getProperty("createurId")));
        Entity utilisateur = datastore.prepare(q).asSingleEntity();

        // On modifie la propriété full name pour l'afficher dans le front
        petition.setProperty("nomCreateur", utilisateur.getProperty("nomComplet"));

        return petition;
    }

	@ApiMethod(name = "mesSignatures", httpMethod = HttpMethod.GET)
	public List<Entity> mesSignatures(User user, @Named("page") int page) throws UnauthorizedException {
        if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
        List<Entity> result = new ArrayList<Entity>();
		Query q = new Query("Utilisateur").setFilter(new Query.FilterPredicate("id",
                Query.FilterOperator.EQUAL, user.getId()));
        
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity utilisateur = datastore.prepare(q).asSingleEntity();
        List<String> petitionSignees = (List<String>) utilisateur.getProperty("signatures");

        // Requetage avec index pour ne pas renvoyer toutes les pétitions signées
        Query q2 = new Query("Petition").addSort("nbvotants", SortDirection.DESCENDING);
        int index = (page - 1) * 50;
        int indexPrecedent = index - 50;
        for(int i = indexPrecedent; i < index; i++)
        {
            Key key = KeyFactory.createKey("Petition", petitionSignees.get(i));
            Query petitionQuery = new Query("Petition").setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, key));
            result.add(datastore.prepare(petitionQuery).asSingleEntity());
        }
		return result;
	}
	
	@ApiMethod(name = "search", httpMethod = HttpMethod.GET)
	public List<Entity> search(Petition petition) {
		Query q = new Query("Petition").setFilter(new Query.FilterPredicate("nom",
                Query.FilterOperator., petition.nom.toString()));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
		return result;
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
		e.setProperty("tags", petition.tags);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);
		return e;
	}

	@ApiMethod(name = "signPetition", httpMethod = HttpMethod.POST) 
	public Entity signPetition(User user, Petition petition) throws Exception{
		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		if(petition == null){
		    throw new IllegalArgumentException("La pétition ne peut pas être nulle.");
		}
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Definition des clés et des entités
        Key petitionId = KeyFactory.createKey("Petition", petition.id); 
        Key utilisateurId = KeyFactory.createKey("Utilisateur", user.getId()); 
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
            int nbVotants = (int)petitionEntity.getProperty("nbVotants");
            ArrayList<String> votants = (ArrayList<String>) petitionEntity.getProperty("votants");
            if(votants == null) {
                votants = new ArrayList<>();
            }

            // Modification des propriétés des pétitions si l'utilisateur n'a pas déjà signé
            if(!votants.contains(petition.createurId)) {
                votants.add(petition.createurId);
                petitionEntity.setProperty("votants", votants); 
                petitionEntity.setProperty("nbVotants", nbVotants + 1 ); 
            }

            // Modification des propriétés de l'utilisateur si la pétition n'est pas déjà dans ses signatures
            if(!signatures.contains(petition.id)) {
                signatures.add((String)petitionEntity.getProperty("id"));
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
}