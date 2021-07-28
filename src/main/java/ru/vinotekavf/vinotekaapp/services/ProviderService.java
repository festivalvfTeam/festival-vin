package ru.vinotekavf.vinotekaapp.services;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vinotekavf.vinotekaapp.entities.Position;
import ru.vinotekavf.vinotekaapp.entities.Provider;
import ru.vinotekavf.vinotekaapp.repos.PositionRepository;
import ru.vinotekavf.vinotekaapp.repos.ProviderRepository;

import java.util.List;

@Service
public class ProviderService {
    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private PositionRepository positionRepository;

    public List<Provider> getAll() {
        return providerRepository.findAll();
    }

    public List<Provider> getAllActive() {
        return providerRepository.findByIsActiveTrueOrderByName();
    }

    public List<Provider> getAllDisabled() { return providerRepository.findByIsActiveFalseOrderByName(); }

    public void delete(Provider provider) {
        positionRepository.deleteAllByProvider(provider);
        providerRepository.delete(provider);
    }

    public void save(Provider provider) {
        Provider providerFromDb = providerRepository.findByName(provider.getName());
        if (ObjectUtils.isNotEmpty(providerFromDb)) {
            providerFromDb.setManagerName(provider.getManagerName());
            providerFromDb.setPhone(provider.getPhone());
            providerRepository.save(providerFromDb);
        } else {
            providerRepository.save(provider);
        }
    }

    public List<Provider> getProvidersWithFilter (String filter) {
        return providerRepository.findAllByNameContaining(filter);
    }

    public Provider getProviderById(Long id) {
        return providerRepository.findById(id).orElse(null);
    }

    public Provider getProviderByName(String name) {
        return providerRepository.findByName(name);
    }

    public Provider changeProviderStatus(Long id) {
        Provider provider = providerRepository.findById(id).orElse(new Provider());
        provider.setActive(!provider.isActive());
        for (Position position : provider.getPositions()){
            position.setActive(!position.isActive());
        }
        providerRepository.save(provider);
        return provider;
    }
}
