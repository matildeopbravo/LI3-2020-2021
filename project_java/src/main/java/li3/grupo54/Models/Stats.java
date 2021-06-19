package li3.grupo54.Models;

import li3.grupo54.Models.Exceptions.NullReviewException;
import li3.grupo54.Models.Interfaces.IBusiness;
import li3.grupo54.Utils.MyPair;

import li3.grupo54.Utils.MyTriple;
import java.util.*;
import java.util.stream.Collectors;

public class Stats {
  // User id ->  mes a mes -> UserStarsTuple
  private Map<String, List<UserStarsTuple>> averageByUserId;
  // Business id ->  mes a mes -> BusinessStarsTuple
  private Map<String, List<BusinessStarsTuple>> averageByBusinessId;
  // State -> City -> BusinessId -> Average
  private Map<String, Map<String, Map<String, StarsTuple>>> averageByStateBusiness;
  private TreeMap<String, IBusiness> negociosNuncaAvaliados; // ordenados por ordem alfabetica
  private Map<String, IBusiness> negociosAvaliados;



  public Stats() {
    averageByUserId = new HashMap<>();
    averageByBusinessId = new HashMap<>();
    averageByStateBusiness = new HashMap<>();
    negociosAvaliados = new HashMap<>();
    negociosNuncaAvaliados = new TreeMap<>();
  }

  public void updateStats(Review review, Business business) throws NullReviewException {
    if(review == null)
      throw new NullReviewException("Null Review Exception");
    updateAverageBusiness(review);
    updateAverageUser(review);
    updateAveragebyState(review, business);
  }

  // TODO rever
  public void updateAveragebyState(Review review, IBusiness business) {
    if (!review.getBusinessId().equals(business.getId()))
        return;
      String state = business.getState();
      String city = business.getCity();
      Map<String, Map<String, StarsTuple>> map;

    if ((map = this.averageByStateBusiness.get(state)) == null) {
        map = new HashMap<>();
        Map<String, StarsTuple> mapCity = new HashMap<>();
        mapCity.put(business.getId(), new BusinessStarsTuple(review));
        map.put(city, mapCity);
        this.averageByStateBusiness.put(state, map);
      } else {
        Map<String, StarsTuple> mapCity = map.get(city);
        if (mapCity == null) {
          mapCity = new HashMap<>();
          mapCity.put(business.getId(), new BusinessStarsTuple(review));
          map.put(city, mapCity);
        } else {
          StarsTuple starTuple = mapCity.get(business.getId());
          if (starTuple == null)
            mapCity.put(business.getId(), new BusinessStarsTuple(review));
          else
            starTuple.update(review.getStars());
        }
      }
  }

  public void updateAverageUser(Review review) {
    String userId = review.getUserId();
    int month = review.getDate().getMonthValue() - 1;
    List<UserStarsTuple> list = null;

    if ((list = this.averageByUserId.get(userId)) == null) {
      list = new ArrayList<>(12);
      for(int i  = 0; i < 12; i++) {
        list.add(i,null);
      }
      list.set(month, new UserStarsTuple(review));
      this.averageByUserId.put(userId, list);

    } else {
      UserStarsTuple userStarsTuple = list.get(month);
      if (userStarsTuple == null)
        userStarsTuple = new UserStarsTuple();
      userStarsTuple.updateAverage(review);
    }
  }

  public void updateAverageBusiness(Review review) {
    String businessId = review.getBusinessId();
    int month = review.getDate().getMonthValue() - 1;
    List<BusinessStarsTuple> list = null;

    if ((list = this.averageByBusinessId.get(businessId)) == null) {
      list = new ArrayList<>(12);
      for (int i = 0; i < 12 ; i++ ) {
        list.add(null);
      }
      list.set(month, new BusinessStarsTuple(review));
      this.averageByBusinessId.put(businessId, list);

    } else {
      BusinessStarsTuple userStarsTuple = list.get(month);
      if (userStarsTuple == null)
        userStarsTuple = new BusinessStarsTuple();
      userStarsTuple.updateAverage(review);
    }
  }

  // query 5
  // Lista de pares business Id e os seus pares de reviews correspondentes
  List<MyPair<String, Integer>> pairBusinessIdAndTheirReviews(String userId) {
    List<MyPair<String, Integer>> list = new ArrayList<>();
    Set<UserStarsTuple> userStarsTuples = new HashSet<>();
    // guardar todos os id's dos business correspondestes a esse user
    Set<String> businessID = new HashSet<>();
    this.averageByUserId.get(userId).forEach(e -> {
      if(e!=null)
      businessID.addAll(e.getBusiness());
    });

    // ir a cada business id e ir buscar o set de reviews
    // colocar isso num par (id,set)
    // assim depois pra query 5, bast trocar o id pelo nome e ordenar
    businessID.forEach(e -> {
      Set<String> reviews = new HashSet<>();
      this.averageByBusinessId.get(e).forEach(l->{
       if(l!=null) reviews.addAll(l.getReviews());
      });
      list.add(new MyPair<>(e, reviews.size()));
      // devolve uma listade de (business_id, numero reviews=
    });
    return list;
  }

  public <T> void atualiza(T newEntity, CatalogoUsers catalogoUsers, CatalogoBusinesses catalogoBusinesses) {
    if(newEntity instanceof Business) {
      // inicializar a lista de businesses nunca avaliados
      this.negociosNuncaAvaliados.put(((Business) newEntity).getId(), ((Business) newEntity).clone());
    }
    else if (newEntity instanceof Review) {
        String businessId = ((Review) newEntity).getBusinessId();
        IBusiness b;
        if((b = this.negociosNuncaAvaliados.remove(businessId)) != null) {
            this.negociosAvaliados.put(businessId,b);
        }

      try {
        this.updateStats((Review) newEntity, (Business) catalogoBusinesses.getBusiness(((Review) newEntity).getBusinessId()));
      } catch (Exception e) {
        e.getMessage();
      }
    }

    }


  // para a query 1
  public List<IBusiness> getNegociosNuncaAvaliadosOrdered() {
    return this.negociosNuncaAvaliados.values().stream().map(IBusiness::clone).collect(Collectors.toList());
  }

  //@Override
  //public void add(IBusiness business) {
  //  this.negociosNuncaAvaliados.put(business.getId(), business.clone());
  //}

  //public void changesBusinessAvalied(String id) throws BusinessNotFoundException {
  //  IBusiness business = negociosNuncaAvaliados.remove(id);
  //  if (business == null)
  //    throw new BusinessNotFoundException("Business Not Found, id: " + id);
  //  this.negociosAvaliados.put(id, business.clone());
  //}

  //@Override
  //public IBusiness getById(String id) throws BusinessNotFoundException {
  //  IBusiness b;
  //  if ((b = negociosAvaliados.get(id)) != null || (b = negociosNuncaAvaliados.get(id)) != null) {
  //    return b.clone();
  //  } else {
  //    throw new BusinessNotFoundException();
  //  }
  //}

  //@Override
  //public void delete(String id) throws BusinessNotFoundException {
  //  if (negociosAvaliados.get(id) != null) {
  //    negociosAvaliados.remove(id);
  //  } else if (negociosNuncaAvaliados.get(id) != null) {
  //    negociosNuncaAvaliados.remove(id);
  //  } else {
  //    throw new BusinessNotFoundException();
  //  }
  //}


  public List<MyTriple<Integer,Integer,Float>> query3(String userId){
    if(this.averageByUserId == null)
      return null;
    List<UserStarsTuple> list = this.averageByUserId.get(userId);

    if(list==null)
      return null;

    List<MyTriple<Integer,Integer,Float>> result = new ArrayList<>(12);
    for(int i = 0; i < 12; i++) {
      result.add(i,null);
    }
    int i=0;
    for(UserStarsTuple tuple : list){
      int numReviews=0;
      int businessDist =0;
      float mean =0;
      if(tuple!=null) {
        numReviews = tuple.getReviewsNumber();
        businessDist = tuple.getBusinessNumberDistint();
        mean = (float) tuple.getCurrentAverage();
      }
      //if(numReviews!=-1 && businessDist!= -1 && mean!=-1)
      result.set(i,new MyTriple<>(numReviews,businessDist,mean));
      i++;
    }
    return result;
  }
}

