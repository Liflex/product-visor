package ru.dmitartur.client.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import ru.dmitartur.client.enums.Role;
import ru.dmitartur.client.validation.RussianPhone;

import java.time.LocalDate;

public class UserPatchDto {
    @Email
    private String email;
    private String photo;
    private Role role;
    private String locale;
    private String timezone;
    private String firstName;
    private String lastName;
    private String middleName;
    @Past
    private LocalDate birthDate;
    @RussianPhone
    private String phone;

    // Геттеры и сеттеры
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
} 