package com.example.application.data;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class SampleBook extends AbstractEntity {

    @Lob
    @Column(length = 1000000)
    private byte[] image;
    private String name;
    private String author;
    private LocalDate publicationDate;
    private Integer pages;
    private String isbn;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name="samplebook_sampleperson",
    joinColumns = @JoinColumn(name = "sampleBook_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "samplePerson_id", referencedColumnName = "id"))
    private List<SamplePerson> samplePersons;

    public List<SamplePerson> getSamplePersons() {
        return samplePersons;
    }

    public void setSamplePersons(List<SamplePerson> samplePersons) {
        this.samplePersons = samplePersons;
    }

    public byte[] getImage() {
        return image;
    }
    public void setImage(byte[] image) {
        this.image = image;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public LocalDate getPublicationDate() {
        return publicationDate;
    }
    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }
    public Integer getPages() {
        return pages;
    }
    public void setPages(Integer pages) {
        this.pages = pages;
    }
    public String getIsbn() {
        return isbn;
    }
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

}
