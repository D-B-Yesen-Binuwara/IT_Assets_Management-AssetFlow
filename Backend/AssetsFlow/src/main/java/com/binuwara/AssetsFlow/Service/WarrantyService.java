package com.binuwara.AssetsFlow.Service;

import com.binuwara.AssetsFlow.DTO.WarrantyClaimRequest;
import com.binuwara.AssetsFlow.DTO.WarrantyClaimResponse;
import com.binuwara.AssetsFlow.DTO.WarrantyRequest;
import com.binuwara.AssetsFlow.DTO.WarrantyResponse;
import com.binuwara.AssetsFlow.Entity.AppUser;
import com.binuwara.AssetsFlow.Entity.Asset;
import com.binuwara.AssetsFlow.Entity.Vendor;
import com.binuwara.AssetsFlow.Entity.WarrantyClaim;
import com.binuwara.AssetsFlow.Entity.WarrantyClaimStatus;
import com.binuwara.AssetsFlow.Entity.WarrantyPolicy;
import com.binuwara.AssetsFlow.Entity.WarrantyStatus;
import com.binuwara.AssetsFlow.Exception.ApiException;
import com.binuwara.AssetsFlow.Repository.AppUserRepository;
import com.binuwara.AssetsFlow.Repository.AssetRepository;
import com.binuwara.AssetsFlow.Repository.AuditLogRepository;
import com.binuwara.AssetsFlow.Repository.VendorRepository;
import com.binuwara.AssetsFlow.Repository.WarrantyClaimRepository;
import com.binuwara.AssetsFlow.Repository.WarrantyPolicyRepository;
import com.binuwara.AssetsFlow.Security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class WarrantyService {
    private final WarrantyPolicyRepository policyRepository;
    private final WarrantyClaimRepository claimRepository;
    private final AssetRepository assetRepository;
    private final VendorRepository vendorRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogRepository auditLogRepository;

    public WarrantyService(WarrantyPolicyRepository policyRepository, WarrantyClaimRepository claimRepository, AssetRepository assetRepository, VendorRepository vendorRepository, AppUserRepository appUserRepository, AuditLogRepository auditLogRepository) {
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.assetRepository = assetRepository;
        this.vendorRepository = vendorRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<WarrantyResponse> list(UUID assetId, String status) {
        WarrantyStatus requested = DomainSupport.enumValue(status, WarrantyStatus.class, null);
        return policyRepository.findAllByOrderByEndDateAsc().stream()
                .filter(policy -> assetId == null || assetId.equals(policy.getAsset().getId()))
                .filter(policy -> requested == null || reportingStatus(policy) == requested)
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public WarrantyResponse get(UUID id) {
        return response(policyRepository.findWithReferencesById(id).orElseThrow(() -> DomainSupport.notFound("Warranty policy")));
    }

    @Transactional
    public WarrantyResponse create(WarrantyRequest request, AuthenticatedUser actor) {
        WarrantyPolicy policy = new WarrantyPolicy();
        policy.setAsset(resolveAsset(request.assetId()));
        apply(policy, request, true);
        demoteOtherCurrentPolicy(policy);
        WarrantyPolicy saved = policyRepository.save(policy);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "WARRANTY_POLICY", saved.getId(), "CREATED", null);
        return response(saved);
    }

    @Transactional
    public WarrantyResponse update(UUID id, WarrantyRequest request, AuthenticatedUser actor) {
        WarrantyPolicy policy = policyRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Warranty policy"));
        if (request.assetId() != null && !request.assetId().equals(policy.getAsset().getId())) policy.setAsset(resolveAsset(request.assetId()));
        apply(policy, request, false);
        demoteOtherCurrentPolicy(policy);
        WarrantyPolicy saved = policyRepository.save(policy);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "WARRANTY_POLICY", saved.getId(), "UPDATED", null);
        return response(saved);
    }

    @Transactional
    public void delete(UUID id, AuthenticatedUser actor) {
        WarrantyPolicy policy = policyRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Warranty policy"));
        policy.setStatus(WarrantyStatus.VOID);
        policy.setCurrent(false);
        policyRepository.save(policy);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "WARRANTY_POLICY", id, "VOIDED", null);
    }

    @Transactional(readOnly = true)
    public List<WarrantyClaimResponse> claims(UUID assetId) {
        List<WarrantyClaim> claims = assetId == null ? claimRepository.findAllByOrderByOpenedAtDesc() : claimRepository.findAllByWarranty_Asset_IdOrderByOpenedAtDesc(assetId);
        return claims.stream().map(this::claimResponse).toList();
    }

    @Transactional
    public WarrantyClaimResponse createClaim(WarrantyClaimRequest request, AuthenticatedUser actor) {
        WarrantyPolicy policy = policyRepository.findById(DomainSupport.required(request.warrantyId(), "Warranty policy")).orElseThrow(() -> DomainSupport.notFound("Warranty policy"));
        String number = StringUtils.hasText(request.claimNumber()) ? request.claimNumber().trim() : "CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        if (claimRepository.existsByClaimNumberIgnoreCase(number)) throw DomainSupport.conflict("Claim number is already in use.");
        WarrantyClaim claim = new WarrantyClaim();
        claim.setWarranty(policy);
        claim.setClaimNumber(number);
        claim.setDescription(DomainSupport.text(request.description(), "Claim description"));
        claim.setClaimedAmount(nonNegative(request.claimedAmount()));
        claim.setProviderReference(DomainSupport.optionalText(request.providerReference()));
        claim.setStatus(request.status() == null ? WarrantyClaimStatus.OPEN : request.status());
        claim.setAssignedToUser(request.assignedToUserId() == null ? currentUser(actor) : appUserRepository.findById(request.assignedToUserId()).orElseThrow(() -> DomainSupport.notFound("User")));
        claim.setResolutionNotes(DomainSupport.optionalText(request.resolutionNotes()));
        if (claim.getStatus() == WarrantyClaimStatus.RESOLVED) claim.setResolvedAt(Instant.now());
        WarrantyClaim saved = claimRepository.save(claim);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "WARRANTY_CLAIM", saved.getId(), "CREATED", null);
        return claimResponse(saved);
    }

    @Transactional
    public WarrantyClaimResponse updateClaim(UUID id, WarrantyClaimRequest request, AuthenticatedUser actor) {
        WarrantyClaim claim = claimRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Warranty claim"));
        if (request.description() != null) claim.setDescription(DomainSupport.text(request.description(), "Claim description"));
        if (request.claimedAmount() != null) claim.setClaimedAmount(nonNegative(request.claimedAmount()));
        if (request.providerReference() != null) claim.setProviderReference(DomainSupport.optionalText(request.providerReference()));
        if (request.status() != null) {
            claim.setStatus(request.status());
            if (request.status() == WarrantyClaimStatus.RESOLVED && claim.getResolvedAt() == null) claim.setResolvedAt(Instant.now());
        }
        if (request.assignedToUserId() != null) claim.setAssignedToUser(appUserRepository.findById(request.assignedToUserId()).orElseThrow(() -> DomainSupport.notFound("User")));
        if (request.resolutionNotes() != null) claim.setResolutionNotes(DomainSupport.optionalText(request.resolutionNotes()));
        WarrantyClaim saved = claimRepository.save(claim);
        DomainSupport.audit(auditLogRepository, appUserRepository, actor, "WARRANTY_CLAIM", saved.getId(), "UPDATED", null);
        return claimResponse(saved);
    }

    private void apply(WarrantyPolicy policy, WarrantyRequest request, boolean create) {
        if (request.startDate() != null) policy.setStartDate(request.startDate());
        if (request.endDate() != null) policy.setEndDate(request.endDate());
        if (create && (policy.getStartDate() == null || policy.getEndDate() == null)) throw new ApiException(HttpStatus.BAD_REQUEST, "Warranty start and end dates are required.");
        if (policy.getStartDate() != null && policy.getEndDate() != null && policy.getEndDate().isBefore(policy.getStartDate())) throw new ApiException(HttpStatus.BAD_REQUEST, "Warranty end date cannot be before start date.");
        if (request.vendorId() != null) policy.setVendor(vendorRepository.findById(request.vendorId()).orElseThrow(() -> DomainSupport.notFound("Vendor")));
        else if (StringUtils.hasText(request.provider())) policy.setVendor(vendorRepository.findByNameIgnoreCase(request.provider().trim()).orElseThrow(() -> DomainSupport.notFound("Vendor")));
        if (request.policyNumber() != null) policy.setPolicyNumber(DomainSupport.optionalText(request.policyNumber()));
        if (request.coverage() != null) policy.setCoverage(DomainSupport.optionalText(request.coverage()));
        if (request.current() != null) policy.setCurrent(request.current());
        if (request.status() != null) policy.setStatus(DomainSupport.enumValue(request.status(), WarrantyStatus.class, policy.getStatus()));
        else if (policy.getStatus() != WarrantyStatus.VOID && policy.getEndDate() != null) policy.setStatus(reportingStatus(policy));
        if (policy.getStatus() == WarrantyStatus.VOID) policy.setCurrent(false);
    }

    private void demoteOtherCurrentPolicy(WarrantyPolicy policy) {
        if (!policy.isCurrent() || policy.getAsset() == null) return;
        policyRepository.findByAsset_IdAndCurrentTrue(policy.getAsset().getId())
                .filter(existing -> policy.getId() == null || !existing.getId().equals(policy.getId()))
                .ifPresent(existing -> {
                    existing.setCurrent(false);
                    policyRepository.save(existing);
                });
    }

    private WarrantyStatus reportingStatus(WarrantyPolicy policy) {
        if (policy.getStatus() == WarrantyStatus.VOID) return WarrantyStatus.VOID;
        if (policy.getEndDate() == null || policy.getEndDate().isBefore(LocalDate.now())) return WarrantyStatus.EXPIRED;
        if (!policy.getEndDate().isAfter(LocalDate.now().plusDays(30))) return WarrantyStatus.EXPIRING;
        return WarrantyStatus.ACTIVE;
    }

    private Asset resolveAsset(UUID id) {
        if (id == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Asset is required.");
        return assetRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("Asset"));
    }

    private BigDecimal nonNegative(BigDecimal amount) {
        if (amount != null && amount.signum() < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "Claimed amount cannot be negative.");
        return amount;
    }

    private AppUser currentUser(AuthenticatedUser actor) {
        return actor == null ? null : appUserRepository.getReferenceById(actor.getId());
    }

    private WarrantyResponse response(WarrantyPolicy policy) {
        Asset asset = policy.getAsset();
        Vendor vendor = policy.getVendor();
        return new WarrantyResponse(policy.getId(), asset.getId(), asset.getAssetTag(), asset.getName(), vendor == null ? null : vendor.getId(), vendor == null ? null : vendor.getName(), vendor == null ? null : vendor.getName(), policy.getPolicyNumber(), policy.getStartDate(), policy.getEndDate(), policy.getCoverage(), reportingStatus(policy), policy.isCurrent(), policy.getClaims() == null ? 0 : policy.getClaims().size(), policy.getCreatedAt(), policy.getUpdatedAt());
    }

    private WarrantyClaimResponse claimResponse(WarrantyClaim claim) {
        Asset asset = claim.getWarranty().getAsset();
        return new WarrantyClaimResponse(claim.getId(), claim.getWarranty().getId(), asset.getId(), asset.getAssetTag(), claim.getClaimNumber(), claim.getOpenedAt(), claim.getResolvedAt(), claim.getStatus(), claim.getDescription(), claim.getClaimedAmount(), claim.getProviderReference(), claim.getAssignedToUser() == null ? null : claim.getAssignedToUser().getId(), claim.getResolutionNotes(), claim.getUpdatedAt());
    }
}
