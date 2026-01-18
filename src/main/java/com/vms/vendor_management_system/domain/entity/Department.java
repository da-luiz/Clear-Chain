package com.vms.vendor_management_system.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Department entity representing organizational departments
 */
@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Department {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "code", nullable = false, unique = true)
    private String code;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();
    
    @OneToMany(mappedBy = "requestingDepartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VendorCreationRequest> vendorRequests = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Department(String name, String description, String code) {
        this.name = name;
        this.description = description;
        this.code = code;
    }

    public void addUser(User user) {
        if (!users.contains(user)) {
            users.add(user);
            user.setDepartment(this);
        }
    }

    public void removeUser(User user) {
        users.remove(user);
        user.setDepartment(null);
    }

    public void deactivate() {
        this.isActive = false;
        // Deactivate all users in this department
        users.forEach(User::deactivate);
    }

    public void activate() {
        this.isActive = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
