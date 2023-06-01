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
	
	@ApiMethod(name = "old4", httpMethod = HttpMethod.GET)
	public List<Entity> old4() {
		Query q = new Query("Petition").addSort("datetri", SortDirection.ASCENDING);

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
	
	@ApiMethod(name = "last4", httpMethod = HttpMethod.GET)
	public List<Entity> last4() {
		Query q = new Query("Petition").addSort("nbvotants", SortDirection.ASCENDING);

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
	
	@ApiMethod(name = "mesSignatures", httpMethod = HttpMethod.GET)
	public List<Entity> mesSignatures(Petition petition) {
		Query q = new Query("Petition").setFilter(new Query.FilterPredicate("votants",
                Query.FilterOperator.EQUAL, petition.createur.getId().toString()));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
		return result;
	}
	
	@ApiMethod(name = "search", httpMethod = HttpMethod.GET)
	public List<Entity> search(Petition petition) {
		Query q = new Query("Petition").setFilter(new Query.FilterPredicate("nom",
                Query.FilterOperator.EQUAL, petition.nom.toString()));

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
		Entity e = new Entity("Petition", Long.MAX_VALUE-(new Date()).getTime() +":" + user.getId());
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
		  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		  Key id = KeyFactory.createKey("Petition", petition.id); 
		  Entity pet = new Entity("Petition","a"); 
		  Transaction transact =datastore.beginTransaction(); 
		  pet = datastore.get(id); 
		  int nbVotants = Integer.parseInt(pet.getProperty("nbvotants").toString());
		  @SuppressWarnings("unchecked")
		  ArrayList<String> votants = (ArrayList<String>) pet.getProperty("votants");
		  if(votants == null) {
			  votants = new ArrayList<>();
		  }
		  if(!votants.contains(petition.createur.getId())) {
			  votants.add(petition.createur.getId());
			  pet.setProperty("votants", votants); 
			  pet.setProperty("nbvotants", nbVotants + 1 ); 
		  }
		  datastore.put(pet); 
		  transact.commit(); 
		  return pet;
	 }
	 
	/**
	private void changeNumberPetition(Key userKey, int increment) throws Exception {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity e = datastore.get(userKey);
		Long numberPetitions = (Long)e.getProperty("numberPosts") + increment;
		e.setProperty("numberPetitions", numberPetitions);
		datastore.put(e);
	}
	 */
}