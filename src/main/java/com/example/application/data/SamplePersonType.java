package com.example.application.data;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity
public class SamplePersonType extends AbstractEntity {

    private String name;
    private String description;
    private boolean active;

    @OneToMany(mappedBy = "samplePersonType")
    private List<SamplePerson> samplePersonList;

    public List<SamplePerson> getSamplePersonList() {
        return samplePersonList;
    }

    public void setSamplePersonList(List<SamplePerson> samplePersonList) {
        this.samplePersonList = samplePersonList;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

}
