package com.vms.vendor_management_system.application.mapper;

import com.vms.vendor_management_system.application.dto.vendor.CreateVendorRequest;
import com.vms.vendor_management_system.application.dto.vendor.VendorResponse;
import com.vms.vendor_management_system.domain.entity.Vendor;
import com.vms.vendor_management_system.domain.entity.VendorCategory;
import com.vms.vendor_management_system.domain.valueobjects.Address;
import com.vms.vendor_management_system.domain.valueobjects.Email;

/**
 * Utility class for mapping vendor entities and requests.
 */
public final class VendorMapper {

    private VendorMapper() {
    }

    public static Vendor toEntity(CreateVendorRequest request, VendorCategory category) {
        Vendor vendor = new Vendor();
        vendor.setCompanyName(request.getCompanyName());
        vendor.setLegalName(request.getLegalName());
        vendor.setTaxId(request.getTaxId());
        if (request.getEmail() != null) {
            vendor.setEmail(new Email(request.getEmail()));
        }
        vendor.setPhone(request.getPhone());
        if (request.getStreet() != null || request.getCity() != null || request.getCountry() != null) {
            vendor.setAddress(new Address(
                    request.getStreet(),
                    request.getCity(),
                    request.getState(),
                    request.getPostalCode(),
                    request.getCountry()
            ));
        }
        vendor.setCategory(category);
        vendor.setWebsite(request.getWebsite());
        vendor.setDescription(request.getDescription());
        vendor.setNotes(request.getNotes());
        return vendor;
    }

    public static void updateEntity(Vendor vendor, CreateVendorRequest request, VendorCategory category) {
        vendor.setCompanyName(request.getCompanyName());
        vendor.setLegalName(request.getLegalName());
        vendor.setTaxId(request.getTaxId());
        if (request.getEmail() != null) {
            vendor.setEmail(new Email(request.getEmail()));
        } else {
            vendor.setEmail(null);
        }
        vendor.setPhone(request.getPhone());
        if (request.getStreet() != null || request.getCity() != null || request.getCountry() != null) {
            vendor.setAddress(new Address(
                    request.getStreet(),
                    request.getCity(),
                    request.getState(),
                    request.getPostalCode(),
                    request.getCountry()
            ));
        } else {
            vendor.setAddress(null);
        }
        vendor.setCategory(category);
        vendor.setWebsite(request.getWebsite());
        vendor.setDescription(request.getDescription());
        vendor.setNotes(request.getNotes());
    }

    public static VendorResponse toResponse(Vendor vendor) {
        if (vendor == null) {
            return null;
        }

        return VendorResponse.builder()
                .id(vendor.getId())
                .vendorCode(vendor.getVendorCode())
                .companyName(vendor.getCompanyName())
                .legalName(vendor.getLegalName())
                .taxId(vendor.getTaxId())
                .email(vendor.getEmail() != null ? vendor.getEmail().getValue() : null)
                .phone(vendor.getPhone())
                .street(vendor.getAddress() != null ? vendor.getAddress().getStreet() : null)
                .city(vendor.getAddress() != null ? vendor.getAddress().getCity() : null)
                .state(vendor.getAddress() != null ? vendor.getAddress().getState() : null)
                .postalCode(vendor.getAddress() != null ? vendor.getAddress().getPostalCode() : null)
                .country(vendor.getAddress() != null ? vendor.getAddress().getCountry() : null)
                .status(vendor.getStatus())
                .categoryId(vendor.getCategory() != null ? vendor.getCategory().getId() : null)
                .categoryName(vendor.getCategory() != null ? vendor.getCategory().getName() : null)
                .website(vendor.getWebsite())
                .description(vendor.getDescription())
                .notes(vendor.getNotes())
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt())
                .build();
    }
}


