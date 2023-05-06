package cloud.project;

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
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import cloud.project.model.Petition;
import cloud.project.util.*;

@Api(name = "myApi",
     version = "v1",
     audiences = "29840377210-rjvba9f5adbkpo93d83lquo5ater0b5q.apps.googleusercontent.com",
  	 clientIds = {"29840377210-rjvba9f5adbkpo93d83lquo5ater0b5q.apps.googleusercontent.com"},
     namespace =
     @ApiNamespace(
		   ownerDomain = "helloworld.example.com",
		   ownerName = "helloworld.example.com",
		   packagePath = "")
     )

public class PetitionEndpoint {

	@ApiMethod(name = "top100", httpMethod = HttpMethod.GET)
	public List<Entity> top100() {
		Query q = new Query("Petition").addSort("nbvotants", SortDirection.DESCENDING);
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
		Query q = new Query("Petition").addSort("nbvotants", SortDirection.DESCENDING);

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
	 * @param currentUser
	 * @return
	 */
	@ApiMethod(name = "mesPetitions", httpMethod = HttpMethod.GET)
	public List<Entity> mesPetitions(Petition petition) {
		Query q = new Query("Petition").setFilter(new Query.FilterPredicate("creator",
                Query.FilterOperator.EQUAL, petition.currentUser.toString()));
		

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
		return result;
	}
	
	@ApiMethod(name = "mesSignatures", httpMethod = HttpMethod.GET)
	public List<Entity> mesSignatures(Petition petition) {
		Query q = new Query("Petition").setFilter(new Query.FilterPredicate("votants",
                Query.FilterOperator.EQUAL, petition.currentUser.toString()));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
		return result;
	}
	
	@ApiMethod(name = "search", httpMethod = HttpMethod.GET)
	public List<Entity> search(Petition petition) {
		Query q = new Query("Petition").setFilter(new Query.FilterPredicate("titre",
                Query.FilterOperator.EQUAL, petition.titre.toString()));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());
		return result;
	}
	
	
	@ApiMethod(name = "addPetition", httpMethod = HttpMethod.POST)
	public Entity addPetition(User utilisateur, Petition petition) throws Exception{ 
		if (utilisateur == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		Entity e = new Entity("Petition", Long.MAX_VALUE-(new Date()).getTime() +":" + petition.currentUser);
		e.setProperty("description", petition.description);
		e.setProperty("creator", petition.currentUser);
		e.setProperty("nbvotants", 0);
		e.setProperty("votants", new ArrayList<>());
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
	    Date date = new Date();  
	    e.setProperty("datetri", date.getTime());
		e.setProperty("date", formatter.format(date));
		String separateur = ";";
		String[] tagsSep = petition.tags.split(separateur);
			
		ArrayList<String> tags = new ArrayList<String>();
		for (int i = 0; i <tagsSep.length; i++) {
			tags.add(tagsSep[i]);
		}
		e.setProperty("tags", tags);
		
		/**		
		//crée des signataire
		ArrayList<String> fset = new ArrayList<String>();
		fset.add(user.getEmail());
		e.setProperty("signatory",fset);
		e.setProperty("nbSignatory",305);//fset.size());
				
		//crée des tags
		HashSet<String> fset2 = new HashSet<String>();
		e.setProperty("tag", fset2);
		*/		
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
		  if(!votants.contains(petition.currentUser)) {
			  votants.add(petition.currentUser);
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