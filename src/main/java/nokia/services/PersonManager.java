package nokia.services;

import nokia.entities.Person;

import java.util.List;

public interface PersonManager {

    Boolean addPerson(String id, String name);

    int deletePerson(String name);

    List<Person> searchPerson(String name);
}
