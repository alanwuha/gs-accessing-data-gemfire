package hello;

import static java.util.Arrays.asList;
import static java.util.stream.StreamSupport.stream;

import java.io.IOException;

import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

@SpringBootApplication
@ClientCacheApplication(name = "AccessingDataGemFireApplication", logLevel = "config") 
// a "client" cache instance, which has the ability to connect to and communicate with a cluster of servers. Client will just store data locally using a LOCAL, client Region, without the need to setup or run any servers.
// set logLevel to "config" to see the Pivotal Gemfire OQL queries that are generated by SGD.
// Because the query methods (e.g. findByName) are annotated with SDG's @Trace annotation, this turns on Pivotal GemFire's OQL query tracing (query-level logging), which shows you the generated OQL, execution time, whether any Pivotal GemFire Indexed were used by the query together the results, and the number of rows returned by the query.
@EnableEntityDefinedRegions(basePackageClasses = Person.class, clientRegionShortcut = ClientRegionShortcut.LOCAL)
@EnableGemfireRepositories
public class Application {

    // The public static void main method uses Spring Boot's SpringApplication.run() to launch the application and invoke the ApplicationRunner (another bean definition) that performs the data access operations on Pivotal GemFire using the application's Spring Data Repository.
    public static void main(String[] args) throws IOException {
        SpringApplication.run(Application.class, args);
    }

    // The application autowires an instance of PersonRepository that you just defined. Spring Data for Pivotal Gemfire will dynamically create a concrete class that implements this interface and plug in the needed query code to meet the interface's obligations. This Repository instance is then used by the run() method to demonstrate the functionality.
    @Bean
    ApplicationRunner run(PersonRepository personRepository) {

        return args -> {

            // In this guide, you are creating three local Person objects, Alice, Baby Bob, and Teen Carol. Initially, they only exist in memory. After creating them, you have to save them to Pivotal GemFire.
            
            // Create local Person objects in memory
            Person alice = new Person("Adult Alice", 40);
            Person bob = new Person("Baby Bob", 1);
            Person carol = new Person("Teen Carol", 13);
            
            System.out.println("Before accessing data in Pivotal GemFire...");
            asList(alice, bob, carol).forEach(person -> System.out.println("\t" + person));

            // Save to Pivoral Gemfire cache
            System.out.println("Saving Alice, Bob and Carol to Pivotal Gemfire...");
            personRepository.save(alice);
            personRepository.save(bob);
            personRepository.save(carol);

            // Find by name
            System.out.println("Lookup each person by name...");
            asList(alice.getName(), bob.getName(), carol.getName()).forEach(name -> System.out.println("\t" + personRepository.findByName(name)));

            // Find adults
            System.out.println("Query adults (over 18):");
            stream(personRepository.findByAgeGreaterThan(18).spliterator(), false).forEach(person -> System.out.println("\t" + person));

            // Find babies
            System.out.println("Query babies (less than 5):");
            stream(personRepository.findByAgeLessThan(5).spliterator(), false).forEach(person -> System.out.println("\t" + person));

            // Find teens
            System.out.println("Query teens (between 12 and 20):");
            stream(personRepository.findByAgeGreaterThanAndAgeLessThan(12, 20).spliterator(), false).forEach(person -> System.out.println("\t" + person));

        };

    }

}

