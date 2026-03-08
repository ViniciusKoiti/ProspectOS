package dev.prospectos.core.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
abstract class CompanyRelationshipState extends CompanyProfileState {

    @ElementCollection
    @CollectionTable(name = "company_contacts")
    private List<Contact> contacts;

    @ElementCollection
    @CollectionTable(name = "company_technology_signals")
    private List<TechnologySignal> technologySignals;

    protected CompanyRelationshipState() {
        this.contacts = new ArrayList<>();
        this.technologySignals = new ArrayList<>();
    }

    public void addContact(Contact contact) {
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }
        validateContactUniqueness(contact);
        contacts.add(contact);
        touch();
    }

    public List<Contact> getContacts() {
        return new ArrayList<>(contacts);
    }

    public List<TechnologySignal> getTechnologySignals() {
        return new ArrayList<>(technologySignals);
    }

    public boolean hasValidContactInfo() {
        return !contacts.isEmpty() && contacts.stream().anyMatch(Contact::hasValidEmail);
    }

    public boolean hasActiveSignals() {
        return !technologySignals.isEmpty();
    }

    protected void addTechnologySignal(String technology, String description) {
        technologySignals.add(new TechnologySignal(technology, description, Instant.now()));
        touch();
    }

    private void validateContactUniqueness(Contact newContact) {
        boolean emailExists = contacts.stream().anyMatch(contact -> contact.getEmail().equals(newContact.getEmail()));
        if (emailExists) {
            throw new IllegalArgumentException("Contact with email already exists: " + newContact.getEmail());
        }
    }
}
