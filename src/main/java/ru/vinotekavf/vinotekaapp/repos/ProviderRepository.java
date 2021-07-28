package ru.vinotekavf.vinotekaapp.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vinotekavf.vinotekaapp.entities.Provider;

import java.util.List;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Provider findByName(String name);

    List<Provider> findAllByNameContaining(String filter);

    List<Provider> findByIsActiveTrueOrderByName();

    List<Provider> findByIsActiveFalseOrderByName();
}
