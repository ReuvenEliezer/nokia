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
        Boolean isAdded = personManager.addPerson("1", "Person");
        Assert.assertTrue("unable to add person", isAdded);
    }


    @Test
    public void addExistingPerson_Test() {
        personManager.addPerson("1", "Person1");
        Boolean isExistingAdded = personManager.addPerson("1", "Person2");
        Assert.assertFalse("person is overridden", isExistingAdded);
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
        Boolean isAdded = personManager.addPerson(null, "PersonA");
        Assert.assertFalse(isAdded);
        isAdded = personManager.addPerson("1", null);
        Assert.assertFalse(isAdded);
        List<Person> personList = personManager.searchPerson(null);
        Assert.assertTrue(personList.isEmpty());
        int deletePersonCount = personManager.deletePerson(null);
        Assert.assertEquals(0,deletePersonCount);

    }

    @Test
    public void multiPersonTest() throws InterruptedException {

        Configuration.maxPersonSize = 1000000;
        ExecutorService executorService = Executors.newFixedThreadPool(1000);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("addPerson");
        int totalPerson = Math.min(Configuration.maxPersonSize, 100000);
        for (int i = 0; i < totalPerson; i++) {
            int finalI = i;
            executorService.submit(() -> {
                personManager.addPerson(String.valueOf(finalI), "Person");
                personManager.searchPerson("Person");
                personManager.searchPerson("Person");
                personManager.searchPerson("Person");
                personManager.searchPerson("Person");
                personManager.searchPerson("Person");
                personManager.searchPerson("Person");
                personManager.searchPerson("Person");
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
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
                () -> Assertions.assertEquals(totalPerson, beforeDeleting.size()),
                () -> Assertions.assertEquals(totalPerson, personDeletedSize),
                () -> Assertions.assertEquals(0, afterDeleting.size()));
    }

    @Test
    public void multiPersonReadLockOptimizationBeforeTryToAddPersonTest() throws InterruptedException {
        /**
         * in this scenario - by using ReadLockOptimizationBeforeTryToAddPerson the total time is 25% performance VS only check in the write block.
         */
        Configuration.maxPersonSize = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("addPerson");
        for (int i = 0; i < 1000000; i++) {
            executorService.submit(() -> {
                personManager.addPerson("1", "Person");
                personManager.searchPerson("Person");
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
        stopWatch.stop();


        System.out.println("prettyPrint: " + stopWatch.prettyPrint());
        System.out.println("Time Elapsed: " + stopWatch.getTotalTimeSeconds());

    }

}
