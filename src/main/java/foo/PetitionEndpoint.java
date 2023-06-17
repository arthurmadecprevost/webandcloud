package foo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.google.appengine.api.datastore.Entity;
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
     
public class PetitionEndpoint {

	@ApiMethod(name = "top100", httpMethod = HttpMethod.GET)
	public List<Entity> top100() {
		Query q = new Query("Petition").addSort("nbVotants", SortDirection.DESCENDING);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
  
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
	 */
	@ApiMethod(name = "mesPetitions", httpMethod = HttpMethod.GET)
	public List<Entity> mesPetitions(User user) {
		Query q = new Query("Petition").setFilter(new Query.FilterPredicate("owner",
                Query.FilterOperator.EQUAL, user.getId()));
		

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
		return result;
	}

    /**
     * 
     
    @ApiMethod(name = "getPetition", httpMethod = HttpMethod.GET)
    //public List<Entity> getPetition(@Named("petition_id") String petitionid) {
    public List<Entity> getPetition() {
		Query q = new Query("Petition").setFilter(new Query.FilterPredicate("name", 
                Query.FilterOperator.EQUAL, "Ceci est une pétition de TEST."));
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(1));
		return result;
    }*/

    @ApiMethod(name = "getPetition", path="getPetition/{petitionId}", httpMethod = HttpMethod.GET)
    public Entity getPetition(@Named("petitionId") String petId) {
            
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Key petitionKey = KeyFactory.createKey("Petition", Long.parseLong(petId));
        Query.Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, petitionKey);
        Query petitionQuery = new Query("Petition").setFilter(keyFilter);
    
        Entity petition = datastore.prepare(petitionQuery).asSingleEntity();
    
        if (petition == null) {
            return null;
        }
    
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
}