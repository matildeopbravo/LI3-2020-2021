package li3.grupo54.Models;


import li3.grupo54.Models.Exceptions.BusinessNotFoundException;
import li3.grupo54.Models.Interfaces.IBusiness;
import li3.grupo54.Utils.Crono;
import li3.grupo54.Utils.MyPair;
import li3.grupo54.Utils.MyTriple;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GestReviews {
  private CatalogoUsers catalogoUsers;
  private CatalogoBusinesses catalogoBusinesses;
  private CatalogoReviews catalogoReviews;
  private Stats stats;

  public GestReviews() {}

  public GestReviews(FileTriple triple) throws  IOException, URISyntaxException {
    this(triple.getUsersFile(), triple.getBusinessesFile(), triple.getReviewsFile());
  }

  public GestReviews(String users, String businesses, String reviews) throws  IOException, URISyntaxException {
    this();
    this.load(users, businesses, reviews);
  }

  public void load(String users, String businesses, String reviews) throws IOException, URISyntaxException {
    this.stats = new Stats();
    try {
      this.catalogoUsers = new CatalogoUsers();
      this.catalogoBusinesses = new CatalogoBusinesses();
      this.catalogoReviews = new CatalogoReviews();
      Crono.start();
      catalogoUsers.populateFromFile(stats, users, null,null);
      double timeTotal=0;
      double time =Crono.stop();
      timeTotal+=time;
      System.out.println("\nFinished reading users");
      System.out.println("Time: "+time+"\n");

      Crono.start();
      catalogoBusinesses.populateFromFile(stats, businesses ,null,null);
      time =Crono.stop();
      timeTotal+=time;
      System.out.println("Finished reading businesses");
      System.out.println("Time: "+time+"\n");

      // atualiza negocios nao avaliados e tal
      Crono.start();
      catalogoReviews.populateFromFile(stats,reviews,catalogoUsers,catalogoBusinesses);
      time =Crono.stop();
      timeTotal+=time;
      System.out.println("Finished reading businesses");
      System.out.println("Time: "+time+"\n");
      System.out.println("\nTotal Time: "+timeTotal+"\n");
    }
   catch (Exception e){

    }

  }

  public CatalogoUsers getCatalogoUsers() {
    return catalogoUsers;
  }

  public void setCatalogoUsers(CatalogoUsers catalogoUsers) {
    this.catalogoUsers = catalogoUsers;
  }

  public CatalogoBusinesses getCatalogoBusinesses() {
    return catalogoBusinesses;
  }

  public void setCatalogoBusinesses(CatalogoBusinesses catalogoBusinesses) {
    this.catalogoBusinesses = catalogoBusinesses;
  }

  public CatalogoReviews getCatalogoReviews() {
    return catalogoReviews;
  }

  public void setCatalogoReviews(CatalogoReviews catalogoReviews) {
    this.catalogoReviews = catalogoReviews;
  }

  public Stats getStats() {
    return stats;
  }

  public void setStats(Stats stats) {
    this.stats = stats;
  }

  // nomes de negocios por ordem decrescente de numero de avaliacoes que o user fez e quantos no total
  // para quantidades iguais, ordem alfabetica de negocios
  public List<MyPair<String,Integer>> query5(String userId){
    Comparator<MyPair<String,Integer>> c = (par1,par2) ->  {
      if(par1.getY().equals(par2.getY())) {
        return par1.getX().compareTo(par2.getX());
      }
      else {
        return par1.getY().compareTo(par2.getY());
      }
    };
    return stats.pairBusinessIdAndTheirReviews(userId).stream().map(par -> {
      try {
        par.setX(this.catalogoBusinesses.getName(par.getX()));
      } catch (BusinessNotFoundException e) {
        e.printStackTrace();
      }
      return new MyPair<String, Integer>(par);
    }).sorted(c).collect(Collectors.toList());
  }

 public int getNumberReviewsInYearBusiness(String businessId, int ano) {
    return Math.toIntExact(this.stats.getAllReviews(businessId).stream().map(r -> catalogoReviews.getReviewById(r)).filter(r -> r.getDate().getYear() == ano).count());
  }

  public List<MyTriple<Integer,Integer,Float>> query3(String userId){
    return  this.stats.query3(userId);
  }

  public List<MyTriple<Integer,Integer,Float>> query4(String businessId){
    return this.stats.query4(businessId);
  }

  public List<MyTriple<String,String,Integer>> query7(){
    return stats.query7();
  }

    public Map<Integer,List<MyPair<Business,Integer>>> query6 (int n) {
    Map<Integer,List<MyPair<Business,Integer>>>  r  = new HashMap<>();
    for(int ano : this.catalogoReviews.getAnoToReviewsPerMonth().keySet() ) {
        List<IBusiness> negociosOrdenados = catalogoBusinesses.getBusinessesById().entrySet().stream().sorted((a,b) -> {
          return getNumberReviewsInYearBusiness(b.getValue().getId(), ano) - getNumberReviewsInYearBusiness(a.getValue().getId(), ano);
        }).limit(n).map(Map.Entry::getValue).collect(Collectors.toList());
      //Integer distinctUsers = negociosOrdenados.stream().map(

       //r.put(ano,new MyPair<>(negociosOrdenados,distinctUsers));
    }


      return null;
  }

  public Stream<Review> getReviewsOfBusiness(String businessId) {
    return this.catalogoReviews.getByReviewId()
            .values()
            .stream()
            .filter(r -> r.getBusinessId().equals(businessId));
  }

  public List<MyPair<String,Integer>>  query8(Integer x){
    return stats.query8(x);
  }
}

