package li3.grupo54.Models.Exceptions;

public class UserNotFoundException extends Exception {
  public UserNotFoundException() {
    super();
  }

  public UserNotFoundException(String s) {
    super("User com id: " + s + " nao existe");
  }
}

