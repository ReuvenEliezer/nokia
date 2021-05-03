package nokia.services;

import nokia.entities.Person;
import nokia.utils.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Component
public class PersonManagerImpl implements PersonManager {
    private final static Logger logger = LogManager.getLogger(PersonManagerImpl.class);

    private HashMap<String, Person> idToPersonHashMap = new HashMap<>();

    /**
     * used for quickly return result to delete/search person methods.
     * the current elements in list are unique (by id), because of we adding the Person entity only if it is not existing in main map (idToPersonHashMap)
     */
    private HashMap<String, List<Person>> nameToPersonHashMap = new HashMap<>();
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();


    @Override
    public boolean addPerson(String id, String name) {
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(name)) {
            logger.debug("the person id or name is null");
            return false;
        }


        /**
         * this is only optimization
         * use reader lock for optimization in exceeded persons size or already existing.
         * if not exceeded and not exist -
         * we acquire writer lock and double check these conditions again maybe another thread update the size map, between readLock.unlock() until writeLock.lock();
         */
        try {
            readLock.lock();
            if (!canToAdd(id)) return false;
        } finally {
            readLock.unlock();
        }


        try {
            writeLock.lock();

            if (!canToAdd(id)) return false;

            Person person = new Person(id, name);
            idToPersonHashMap.put(person.getId(), person);
//            if (nameToPersonHashMap.containsKey(person.getName())) {
//                nameToPersonHashMap.put(person.getName(), nameToPersonHashMap.get(person.getName())).add(person);
//            } else {
//                ArrayList<Person> personsList = new ArrayList<>();
//                personsList.add(person);
//                nameToPersonHashMap.put(person.getName(), personsList);
            nameToPersonHashMap.computeIfAbsent(person.getName(), k -> new ArrayList<>()).add(person);
//            }
            return true;
        } finally {
            writeLock.unlock();
        }

    }

    private boolean canToAdd(String personId) {
        if (idToPersonHashMap.size() == Configuration.maxPersonSize)
            return false;
        if (idToPersonHashMap.containsKey(personId))
            return false;
        return true;
    }

    @Override
    public int deletePerson(String name) {
        if (StringUtils.isEmpty(name))
            return 0;
        try {
            writeLock.lock();
            List<Person> personToDelete = getAllPersonByName(name);
            for (Person person : personToDelete) {
                idToPersonHashMap.remove(person.getId());
                nameToPersonHashMap.remove(person.getName());
            }
            return personToDelete.size();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<Person> searchPerson(String name) {
        if (StringUtils.isEmpty(name))
            return new ArrayList<>();
        try {
            readLock.lock();
            return getAllPersonByName(name);
        } finally {
            readLock.unlock();
        }
    }

    private List<Person> getAllPersonByName(String name) {
        List<Person> personList = nameToPersonHashMap.get(name);
        if (personList == null)
            return new ArrayList<>();
        return new ArrayList<>(personList);
    }

}
