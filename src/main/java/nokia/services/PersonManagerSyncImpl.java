//package nokia.services;
//
//import nokia.entities.Person;
//import nokia.utils.Configuration;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//
//@Component
//public class PersonManagerSyncImpl implements PersonManager {
//    private final static Logger logger = LogManager.getLogger(PersonManagerSyncImpl.class);
//
//    private static volatile int mapSize = 0;
//    private HashMap<String, Person> idToPersonHashMap = new HashMap<>();
//
//    /**
//     * used for quickly return result to delete/search person methods.
//     * the current list is uniq (by id), because of we adding the Person entity only if it is not existing in idToPersonHashMap
//     */
//    private HashMap<String, List<Person>> nameToPersonsHashMap = new HashMap<>();
//    private final Object lock = new Object();
//
//
//    @Override
//    public boolean addPerson(String id, String name) {
//        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(name)) {
//            logger.debug("the person id or name is null");
//            return false;
//        }
//
//        /**
//         *   optimization out of sync block - the only writer to mapSize is in sync block (add/delete person methods). the reader can be out the sync with volatile
//         */
//        if (mapSize == Configuration.maxPersonSize) {
//            logger.debug("mapSize: {}", mapSize);
//            return false;
//        }
//
//        synchronized (lock) {
//            /**
//             * double check, maybe another thread update the size map.
//             */
//            if (idToPersonHashMap.size() == Configuration.maxPersonSize)
//                return false;
//
//            Person person = new Person(id, name);
//            if (idToPersonHashMap.containsKey(person.getId()))
//                return false;
//
//
//            idToPersonHashMap.put(person.getId(), person);
//            mapSize = idToPersonHashMap.size();
//            if (nameToPersonsHashMap.containsKey(person.getName())) {
//                nameToPersonsHashMap.put(person.getName(), nameToPersonsHashMap.get(person.getName())).add(person);
//            } else {
//                ArrayList<Person> personsList = new ArrayList<>();
//                personsList.add(person);
//                nameToPersonsHashMap.put(person.getName(), personsList);
//            }
//
//            return true;
//        }
//    }
//
//    @Override
//    public int deletePerson(String name) {
//        if (StringUtils.isEmpty(name))
//            return 0;
//        synchronized (lock) {
//            List<Person> personToDelete = getAllPersonByName(name);
//
//            for (Person person : personToDelete) {
//                idToPersonHashMap.remove(person.getId());
//                nameToPersonsHashMap.remove(person.getName());
//            }
//
//            /**
//             * update mapSize
//             */
//            mapSize = idToPersonHashMap.size();
//
//            return personToDelete.size();
//        }
//    }
//
//    @Override
//    public List<Person> searchPerson(String name) {
//        if (StringUtils.isEmpty(name))
//            return new ArrayList<>();
//        synchronized (lock) {
//            return getAllPersonByName(name);
//        }
//    }
//
//    private List<Person> getAllPersonByName(String name) {
////        return idToPersonHashMap.entrySet()
////                .stream()
////                .parallel()
////                .filter(o -> o.getValue().getName().equals(name))
////                .map(v -> v.getValue())
////                .collect(Collectors.toList());
//        List<Person> personList = nameToPersonsHashMap.get(name);
//        if (personList == null)
//            return new ArrayList<>();
//        return new ArrayList<>(personList);
////        return personHashSet.stream()
////                .parallel()
////                .filter(p -> p.getName().equals(name))
////                .collect(Collectors.toList());
//    }
//
//}
