package li3.grupo54.Models;


import javafx.util.Pair;
import li3.grupo54.Models.Exceptions.DateNotFoundException;

import java.io.IOException;
import java.net.URISyntaxException;

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
      catalogoUsers.populateFromFile(stats,users);
      System.out.println("Finished reading users\n");
      this.catalogoBusinesses = new CatalogoBusinesses();
      catalogoBusinesses.populateFromFile(stats,businesses);
      System.out.println("Finished reading businesses\n");
      this.catalogoReviews = new CatalogoReviews();
      // atualiza negocios nao avaliados e tal
      catalogoReviews.populateFromFile(stats,reviews);
      System.out.println("Finished reading reviews\n");
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

}

