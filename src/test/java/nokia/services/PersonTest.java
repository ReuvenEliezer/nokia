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

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

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
    public void _Test() {
        personManager.addPerson("1", "Person");
        boolean isExistingAdded = personManager.addPerson("2", "Person");
        List<Person> personList = personManager.searchPerson("Person");
        Assert.assertEquals("person is override", 2, personList.size());
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
    public void multiPersonPerformanceTest() throws InterruptedException, ExecutionException {
        /**
         * the time complexity by use ReentrantReadWriteLock is 10% VS synchronized.
         * in this scenario - by    ReentrantReadWriteLock time is ~1.3 sec, and by synchronized is ~11 sec
         */
        Configuration.maxPersonSize = 50000;
        Duration maxWaitingDuration = Duration.ofSeconds(60);
        int readWriteRatio = 10; // more readers on writers 10 reader on each writer

        int everyMiroSec = 1;
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(30);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("addPersonAndSearchPerson");

        executorService.scheduleAtFixedRate(() -> {
            personManager.addPerson(UUID.randomUUID().toString(), "Person");
        }, 0, everyMiroSec * readWriteRatio, TimeUnit.MICROSECONDS);

        executorService.scheduleAtFixedRate(() -> {
            personManager.searchPerson("Person");
        }, 0, everyMiroSec, TimeUnit.MICROSECONDS);


        Duration elapsedTime = Duration.ZERO;
        Duration sleepDuration = Duration.ofMillis(10);

        while (true) {
            ScheduledFuture<?> result = executorService.schedule(() ->
                    personManager.searchPerson("Person"), 0, TimeUnit.MICROSECONDS);
            Thread.sleep(sleepDuration.toMillis());
            elapsedTime = elapsedTime.plus(sleepDuration);
            if (maxWaitingDuration.minus(elapsedTime).isNegative()) {
                System.out.println("elapsedTime: " + elapsedTime);
                break;
            }

            Object o = result.get();
            List<Person>  beforeDeleting = (List<Person>) o;
            if (beforeDeleting.size() == Configuration.maxPersonSize) {
                break;
            }
        }

        System.out.println("sleepDuration: " + sleepDuration);
        executorService.shutdownNow();
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
                () -> Assert.assertEquals("total person beforeDeleting not as expected", Configuration.maxPersonSize, beforeDeleting.size()),
                () -> Assert.assertEquals("total personDeletedSize not as expected", Configuration.maxPersonSize, personDeletedSize),
                () -> Assert.assertEquals("total person afterDeleting not as expected", 0, afterDeleting.size()));
    }

}
