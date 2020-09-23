package nokia.services;

import nokia.entities.Person;

import java.util.List;

public interface PersonManager {

    boolean addPerson(String id, String name);

    int deletePerson(String name);

    List<Person> searchPerson(String name);
}
