package li3.grupo54.Models;

import li3.grupo54.Models.Exceptions.BusinessNotFoundException;
import li3.grupo54.Models.Exceptions.InvalidBusinessLineException;
import li3.grupo54.Models.Interfaces.IBusiness;
import li3.grupo54.Models.Interfaces.ICatalog;

import java.util.*;

public class CatalogoBusinesses implements ICatalog<IBusiness> {
  // todos os negocios  vao ser colocados aqui numa fase inicial e depois passados para o outro map  caso se encontre uma review
  private final TreeMap<String, IBusiness> businessesById; // ordenados por ordem alfabetica
  private int invalidBusinesses;


  public CatalogoBusinesses() {
    this.businessesById = new TreeMap<>();
    this.invalidBusinesses = 0;
  }

  public CatalogoBusinesses(TreeMap<String, IBusiness> businesses, int invalidBusinesses) {
    this.businessesById = new TreeMap<>();
    for( Map.Entry<String,IBusiness> entry: businesses.entrySet()) {
        this.businessesById.put(entry.getKey(),entry.getValue().clone());
    }
    this.invalidBusinesses = invalidBusinesses;
  }
  public void addInvalid(){
    this.invalidBusinesses++;
  }

  public int getInvalidBusinesses() {
    return invalidBusinesses;
  }

  @Override
  public Business callConstructor(String[] line) throws InvalidBusinessLineException {
    return new Business(line);
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public void add(IBusiness iBusiness) {

  }

  @Override
  public IBusiness getById(String id) throws BusinessNotFoundException {
    return null;
  }

  @Override
  public void delete(String id) throws BusinessNotFoundException {
      if(this.businessesById.get(id) == null) throw new BusinessNotFoundException();
      else {
        this.businessesById.remove(id);
      }
  }

  @Override
  public String toString() {
    return "CatalogoBusinesses{" +
            "businesses=" + businessesById +
            ", invalidBusinesses=" + invalidBusinesses +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CatalogoBusinesses that = (CatalogoBusinesses) o;
    return getInvalidBusinesses() == that.getInvalidBusinesses() && Objects.equals(businessesById, that.businessesById);
  }

  @Override
  public int hashCode() {
    return Objects.hash(businessesById, getInvalidBusinesses());
  }
}