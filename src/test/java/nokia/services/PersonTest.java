package nokia.services;

import nokia.app.NokiaApp;
import nokia.entities.Person;
import nokia.utils.Configuration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = NokiaApp.class)
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PersonTest {


    @Autowired
    private PersonManager personManager;


    @Test
    public void addPerson_Test() {
        boolean isAdded = personManager.addPerson("1", "Person");
        Assert.assertTrue("unable to add person", isAdded);
    }


    @Test
    public void addExistingPerson_Test() {
        personManager.addPerson("1", "Person1");
        boolean isExistingAdded = personManager.addPerson("1", "Person2");

        Assert.assertFalse("person is overridden", isExistingAdded);
        List<Person> personList = personManager.searchPerson("Person1");
        Assert.assertEquals("person is override", 1, personList.size());
    }

    @Test
    public void searchPersonTest() {
        personManager.addPerson("1", "PersonA");
        personManager.addPerson("2", "PersonA");
        personManager.addPerson("3", "PersonC");

        List<Person> personA = personManager.searchPerson("PersonA");
        Assert.assertEquals(2, personA.size());
    }

    @Test
    public void deletePersonTest() {
        personManager.addPerson("1", "PersonA");
        personManager.addPerson("2", "PersonA");
        personManager.addPerson("3", "PersonC");
        int personDeletedSize = personManager.deletePerson("PersonA");
        Assert.assertEquals(2, personDeletedSize);
    }


    @Test
    public void notValidPersonTest() {
        boolean isAdded = personManager.addPerson(null, "PersonA");
        Assert.assertFalse(isAdded);
        isAdded = personManager.addPerson("1", null);
        Assert.assertFalse(isAdded);
        List<Person> personList = personManager.searchPerson(null);
        Assert.assertTrue(personList.isEmpty());
        int deletePersonCount = personManager.deletePerson(null);
        Assert.assertEquals(0, deletePersonCount);

    }

    @Test
    public void multiPersonTest() throws InterruptedException {

        Configuration.maxPersonSize = 300000;
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("addPerson");
        int totalIterations = 300000;

        int totalPerson = Math.min(totalIterations, Configuration.maxPersonSize);

        for (int i = 0; i < totalIterations; i++) {
            int personId = i;
            executorService.submit(() -> {
                personManager.addPerson(String.valueOf(personId), "Person");
                personManager.searchPerson("Person");
            });
//            executorService.submit(() -> {
//            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        stopWatch.stop();

        stopWatch.start("searchPerson beforeDeleting:");
        List<Person> beforeDeleting = personManager.searchPerson("Person");
        System.out.println("searchPerson beforeDeleting: " + beforeDeleting.size());
        stopWatch.stop();

        stopWatch.start("personDeletedSize: ");
        int personDeletedSize = personManager.deletePerson("Person");
        System.out.println("personDeletedSize: " + personDeletedSize);
        stopWatch.stop();

        stopWatch.start("searchPerson afterDeleting: ");
        List<Person> afterDeleting = personManager.searchPerson("Person");
        System.out.println("searchPerson afterDeleting: " + afterDeleting.size());
        stopWatch.stop();


        System.out.println("prettyPrint: " + stopWatch.prettyPrint());
        System.out.println("Time Elapsed: " + stopWatch.getTotalTimeSeconds());

        Assertions.assertAll(
                () -> Assert.assertEquals("total person beforeDeleting not as expected", totalPerson, beforeDeleting.size()),
                () -> Assert.assertEquals("total personDeletedSize not as expected", totalPerson, personDeletedSize),
                () -> Assert.assertEquals("total person afterDeleting not as expected", 0, afterDeleting.size()));
    }

    @Test
    public void multiPersonReadLockOptimizationBeforeTryToAddPersonTest() throws InterruptedException {
        /**
         * in this scenario - by using ReadLockOptimizationBeforeTryToAddPerson the total time is 25% performance VS only check in the write block.
         */
        Configuration.maxPersonSize = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("littleAddPersonAndManySearchPerson");
        int totalIterations = 1000000;
        int totalPerson = Math.min(totalIterations, Configuration.maxPersonSize);

        for (int i = 0; i < totalIterations; i++) {
            int finalI = i;
            executorService.submit(() -> {
                personManager.addPerson(String.valueOf(finalI), "Person");
                personManager.searchPerson("Person");
            });
//            executorService.submit(() -> {
//                personManager.searchPerson("Person");
//            });
        }


        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
        stopWatch.stop();
        List<Person> personList = personManager.searchPerson("Person");
        System.out.println(personList.size());
        Assert.assertEquals("totalPerson not as expected:", totalPerson, personList.size());
        System.out.println("prettyPrint: " + stopWatch.prettyPrint());
        System.out.println("Time Elapsed: " + stopWatch.getTotalTimeSeconds());

    }

}
