package com.example.application.services;

import com.example.application.data.SamplePersonType;
import com.example.application.data.SamplePersonTypeRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class SamplePersonTypeService {

    private final SamplePersonTypeRepository repository;

    public SamplePersonTypeService(SamplePersonTypeRepository repository) {
        this.repository = repository;
    }

    public Optional<SamplePersonType> get(Long id) {
        return repository.findById(id);
    }

    public SamplePersonType save(SamplePersonType entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<SamplePersonType> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<SamplePersonType> list(Pageable pageable, Specification<SamplePersonType> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
