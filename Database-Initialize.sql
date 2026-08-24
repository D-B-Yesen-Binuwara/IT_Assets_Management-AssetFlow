-- AssetFlow database initialization
--
-- Run this file in a new, empty PostgreSQL database (for example, AssetsFlow).
-- It creates the complete application schema, constraints, triggers, indexes,
-- reporting views, and the default organization-settings row.
--
-- This file does not create a PostgreSQL database role or store an application
-- password. Configure DB_USERNAME and DB_PASSWORD in the backend .env file.
-- After the backend starts, call POST /api/auth/bootstrap to create the first
-- application account; the first account receives the SUPER_ADMIN role.


CREATE EXTENSION IF NOT EXISTS pgcrypto;

BEGIN;

-- ============================================================================
-- Common timestamp helper
-- ============================================================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

-- ============================================================================
-- Organization, users, roles, and permissions
-- ============================================================================

CREATE TABLE departments (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code                varchar(30) NOT NULL UNIQUE,
    name                varchar(120) NOT NULL UNIQUE,
    description         text,
    manager_employee_id uuid,
    created_at          timestamptz NOT NULL DEFAULT now(),
    updated_at          timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE employees (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_number   varchar(40) NOT NULL UNIQUE,
    first_name        varchar(80) NOT NULL,
    last_name         varchar(80) NOT NULL,
    email             varchar(255) NOT NULL UNIQUE,
    phone             varchar(40),
    address           varchar(255),
    job_title         varchar(120),
    department_id     uuid REFERENCES departments(id),
    status            varchar(20) NOT NULL DEFAULT 'ACTIVE'
                      CHECK (status IN ('ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED')),
    hire_date         date,
    termination_date  date,
    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now(),
    CHECK (termination_date IS NULL OR hire_date IS NULL OR termination_date >= hire_date)
);

ALTER TABLE employees
    ADD COLUMN IF NOT EXISTS address varchar(255);

ALTER TABLE departments
    ADD CONSTRAINT fk_departments_manager
    FOREIGN KEY (manager_employee_id) REFERENCES employees(id) ON DELETE SET NULL;

CREATE TABLE app_users (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id     uuid UNIQUE REFERENCES employees(id) ON DELETE SET NULL,
    username        varchar(80) NOT NULL UNIQUE,
    password_hash   varchar(255) NOT NULL,
    status          varchar(20) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED')),
    last_login_at   timestamptz,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE roles (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name        varchar(50) NOT NULL UNIQUE,
    description text
);

CREATE TABLE permissions (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code        varchar(100) NOT NULL UNIQUE,
    description text
);

CREATE TABLE user_roles (
    user_id uuid NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    role_id uuid NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permissions (
    role_id       uuid NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id uuid NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- ============================================================================
-- Locations, vendors, categories, and procurement
-- ============================================================================

CREATE TABLE locations (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_location_id uuid REFERENCES locations(id) ON DELETE SET NULL,
    code               varchar(40) NOT NULL UNIQUE,
    name               varchar(150) NOT NULL,
    location_type      varchar(30) NOT NULL
                       CHECK (location_type IN ('BRANCH', 'BUILDING', 'FLOOR', 'ROOM', 'WAREHOUSE', 'OTHER')),
    address_line       varchar(255),
    city               varchar(100),
    country            varchar(100),
    created_at         timestamptz NOT NULL DEFAULT now(),
    updated_at         timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE vendors (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_code    varchar(40) NOT NULL UNIQUE,
    name           varchar(180) NOT NULL UNIQUE,
    contact_name   varchar(120),
    email          varchar(255),
    phone          varchar(40),
    category       varchar(100),
    address        text,
    status         varchar(20) NOT NULL DEFAULT 'ACTIVE'
                   CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED')),
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE asset_categories (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name        varchar(100) NOT NULL UNIQUE,
    description text,
    is_active   boolean NOT NULL DEFAULT true,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE purchase_orders (
    id                       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    po_number                varchar(40) NOT NULL UNIQUE,
    vendor_id                uuid NOT NULL REFERENCES vendors(id),
    requested_by_employee_id uuid REFERENCES employees(id) ON DELETE SET NULL,
    approved_by_user_id      uuid REFERENCES app_users(id) ON DELETE SET NULL,
    order_date               date NOT NULL DEFAULT current_date,
    expected_date            date,
    received_date            date,
    status                   varchar(20) NOT NULL DEFAULT 'DRAFT'
                             CHECK (status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'ORDERED', 'PARTIALLY_RECEIVED', 'RECEIVED', 'CANCELLED')),
    currency                 char(3) NOT NULL DEFAULT 'USD',
    subtotal                 numeric(14,2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    tax_amount               numeric(14,2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    total_amount             numeric(14,2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    notes                    text,
    created_at               timestamptz NOT NULL DEFAULT now(),
    updated_at               timestamptz NOT NULL DEFAULT now(),
    CHECK (expected_date IS NULL OR expected_date >= order_date),
    CHECK (received_date IS NULL OR received_date >= order_date)
);

CREATE TABLE purchase_order_items (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    purchase_order_id  uuid NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    category_id        uuid REFERENCES asset_categories(id) ON DELETE SET NULL,
    description        varchar(255) NOT NULL,
    quantity           integer NOT NULL CHECK (quantity > 0),
    received_quantity  integer NOT NULL DEFAULT 0 CHECK (received_quantity >= 0 AND received_quantity <= quantity),
    unit_cost          numeric(14,2) NOT NULL CHECK (unit_cost >= 0),
    created_at         timestamptz NOT NULL DEFAULT now()
);

-- ============================================================================
-- Assets and assignments
-- ============================================================================

CREATE TABLE assets (
    id                       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_tag                varchar(50) NOT NULL UNIQUE,
    name                     varchar(180) NOT NULL,
    category_id              uuid NOT NULL REFERENCES asset_categories(id),
    serial_number            varchar(120) UNIQUE,
    manufacturer             varchar(120),
    model                    varchar(120),
    vendor_id                uuid REFERENCES vendors(id) ON DELETE SET NULL,
    purchase_order_item_id   uuid REFERENCES purchase_order_items(id) ON DELETE SET NULL,
    purchase_date            date,
    purchase_cost            numeric(14,2) CHECK (purchase_cost IS NULL OR purchase_cost >= 0),
    currency                 char(3) NOT NULL DEFAULT 'USD',
    location_id              uuid REFERENCES locations(id) ON DELETE SET NULL,
    department_id            uuid REFERENCES departments(id) ON DELETE SET NULL,
    status                   varchar(25) NOT NULL DEFAULT 'AVAILABLE'
                             CHECK (status IN ('AVAILABLE', 'ASSIGNED', 'UNDER_MAINTENANCE', 'IN_TRANSIT', 'RETIRED', 'DISPOSED', 'LOST')),
    asset_condition          varchar(20) NOT NULL DEFAULT 'GOOD'
                             CHECK (asset_condition IN ('NEW', 'GOOD', 'FAIR', 'POOR', 'DAMAGED')),
    retirement_date          date,
    disposal_notes           text,
    notes                    text,
    created_at               timestamptz NOT NULL DEFAULT now(),
    updated_at               timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE assignments (
    id                    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id              uuid NOT NULL REFERENCES assets(id),
    employee_id           uuid NOT NULL REFERENCES employees(id),
    assigned_by_user_id   uuid REFERENCES app_users(id) ON DELETE SET NULL,
    assigned_at           timestamptz NOT NULL DEFAULT now(),
    expected_return_date  date,
    returned_at           timestamptz,
    status                varchar(20) NOT NULL DEFAULT 'ACTIVE'
                          CHECK (status IN ('ACTIVE', 'RETURNED', 'TRANSFERRED', 'CANCELLED')),
    handover_notes        text,
    created_at            timestamptz NOT NULL DEFAULT now(),
    CHECK (returned_at IS NULL OR returned_at >= assigned_at),
    UNIQUE (id, asset_id, employee_id)
);

CREATE UNIQUE INDEX uq_one_active_assignment_per_asset
    ON assignments(asset_id)
    WHERE status = 'ACTIVE';

-- ============================================================================
-- Warranty and maintenance
-- ============================================================================

CREATE TABLE warranty_policies (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id        uuid NOT NULL REFERENCES assets(id) ON DELETE CASCADE,
    vendor_id       uuid REFERENCES vendors(id) ON DELETE SET NULL,
    policy_number   varchar(100),
    start_date      date NOT NULL,
    end_date        date NOT NULL,
    coverage        text,
    status          varchar(20) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'EXPIRING', 'EXPIRED', 'VOID')),
    is_current      boolean NOT NULL DEFAULT true,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now(),
    CHECK (end_date >= start_date)
);

CREATE UNIQUE INDEX uq_current_warranty_per_asset
    ON warranty_policies(asset_id)
    WHERE is_current = true;

CREATE TABLE warranty_claims (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    warranty_id         uuid NOT NULL REFERENCES warranty_policies(id) ON DELETE CASCADE,
    assigned_to_user_id uuid REFERENCES app_users(id) ON DELETE SET NULL,
    claim_number        varchar(80) NOT NULL UNIQUE,
    opened_at           timestamptz NOT NULL DEFAULT now(),
    resolved_at         timestamptz,
    status              varchar(20) NOT NULL DEFAULT 'OPEN'
                        CHECK (status IN ('OPEN', 'SUBMITTED', 'APPROVED', 'REJECTED', 'RESOLVED')),
    description         text NOT NULL,
    claimed_amount      numeric(14,2) CHECK (claimed_amount IS NULL OR claimed_amount >= 0),
    provider_reference  varchar(120),
    resolution_notes    text,
    updated_at          timestamptz NOT NULL DEFAULT now(),
    CHECK (resolved_at IS NULL OR resolved_at >= opened_at)
);

CREATE TABLE maintenance_tickets (
    id                       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_number            varchar(50) NOT NULL UNIQUE,
    asset_id                 uuid NOT NULL REFERENCES assets(id),
    requested_by_employee_id uuid REFERENCES employees(id) ON DELETE SET NULL,
    assigned_to_employee_id  uuid REFERENCES employees(id) ON DELETE SET NULL,
    vendor_id                uuid REFERENCES vendors(id) ON DELETE SET NULL,
    issue                    varchar(255) NOT NULL,
    description              text,
    priority                 varchar(20) NOT NULL DEFAULT 'MEDIUM'
                             CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    status                   varchar(20) NOT NULL DEFAULT 'OPEN'
                             CHECK (status IN ('OPEN', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED')),
    opened_at                timestamptz NOT NULL DEFAULT now(),
    due_date                 date,
    started_at               timestamptz,
    completed_at             timestamptz,
    cost                     numeric(14,2) NOT NULL DEFAULT 0 CHECK (cost >= 0),
    resolution               text,
    created_at               timestamptz NOT NULL DEFAULT now(),
    updated_at               timestamptz NOT NULL DEFAULT now(),
    CHECK (completed_at IS NULL OR completed_at >= opened_at)
);

-- ============================================================================
-- Software licensing
-- ============================================================================

CREATE TABLE software_licenses (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    software_name   varchar(180) NOT NULL,
    vendor_id       uuid REFERENCES vendors(id) ON DELETE SET NULL,
    license_type    varchar(30) NOT NULL
                    CHECK (license_type IN ('SUBSCRIPTION', 'PERPETUAL', 'OPEN_SOURCE', 'TRIAL')),
    license_key     text,
    seat_count      integer NOT NULL DEFAULT 1 CHECK (seat_count > 0),
    start_date      date,
    end_date        date,
    purchase_cost   numeric(14,2) CHECK (purchase_cost IS NULL OR purchase_cost >= 0),
    status          varchar(20) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'EXPIRING', 'EXPIRED', 'CANCELLED')),
    notes           text,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now(),
    CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date)
);

CREATE TABLE license_assignments (
    id                    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    license_id            uuid NOT NULL REFERENCES software_licenses(id) ON DELETE CASCADE,
    employee_id           uuid NOT NULL REFERENCES employees(id),
    assigned_by_user_id   uuid REFERENCES app_users(id) ON DELETE SET NULL,
    assigned_at           timestamptz NOT NULL DEFAULT now(),
    revoked_at            timestamptz,
    status                varchar(20) NOT NULL DEFAULT 'ACTIVE'
                          CHECK (status IN ('ACTIVE', 'REVOKED')),
    CHECK (revoked_at IS NULL OR revoked_at >= assigned_at)
);

CREATE UNIQUE INDEX uq_active_license_assignment
    ON license_assignments(license_id, employee_id)
    WHERE status = 'ACTIVE';

-- ============================================================================
-- Notifications, audit, and generic settings
-- ============================================================================

CREATE TABLE notifications (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_user_id uuid NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    notification_type varchar(50) NOT NULL,
    priority          varchar(20) NOT NULL DEFAULT 'MEDIUM'
                      CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    title             varchar(180) NOT NULL,
    message           text NOT NULL,
    read_at           timestamptz,
    created_at        timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE audit_logs (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id uuid REFERENCES app_users(id) ON DELETE SET NULL,
    entity_type   varchar(80) NOT NULL,
    entity_id     uuid,
    action        varchar(40) NOT NULL,
    old_values    jsonb,
    new_values    jsonb,
    created_at    timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE system_settings (
    setting_key   varchar(100) PRIMARY KEY,
    setting_value jsonb NOT NULL,
    updated_by    uuid REFERENCES app_users(id) ON DELETE SET NULL,
    updated_at    timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE organization_settings (
    id                    smallint PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    organization_name     varchar(180) NOT NULL DEFAULT 'AssetFlow',
    industry              varchar(120),
    primary_contact       varchar(180),
    currency              char(3) NOT NULL DEFAULT 'USD',
    timezone              varchar(80) NOT NULL DEFAULT 'UTC',
    branding              jsonb NOT NULL DEFAULT '{}'::jsonb,
    notification_settings jsonb NOT NULL DEFAULT '{}'::jsonb,
    updated_by            uuid REFERENCES app_users(id) ON DELETE SET NULL,
    created_at            timestamptz NOT NULL DEFAULT now(),
    updated_at            timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE notification_preferences (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          uuid NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    notification_type varchar(50) NOT NULL,
    in_app_enabled   boolean NOT NULL DEFAULT true,
    email_enabled    boolean NOT NULL DEFAULT true,
    created_at       timestamptz NOT NULL DEFAULT now(),
    updated_at       timestamptz NOT NULL DEFAULT now(),
    UNIQUE (user_id, notification_type)
);

CREATE TABLE email_templates (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    template_key     varchar(100) NOT NULL UNIQUE,
    subject_template varchar(255) NOT NULL,
    body_template    text NOT NULL,
    is_active        boolean NOT NULL DEFAULT true,
    updated_by       uuid REFERENCES app_users(id) ON DELETE SET NULL,
    created_at       timestamptz NOT NULL DEFAULT now(),
    updated_at       timestamptz NOT NULL DEFAULT now()
);

-- ============================================================================
-- Transfer workflow
-- ============================================================================

CREATE TABLE asset_transfers (
    id                       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id                 uuid NOT NULL REFERENCES assets(id),
    previous_assignment_id   uuid NOT NULL,
    new_assignment_id        uuid,
    previous_employee_id     uuid NOT NULL REFERENCES employees(id),
    new_employee_id          uuid NOT NULL REFERENCES employees(id),
    previous_location_id     uuid REFERENCES locations(id) ON DELETE SET NULL,
    new_location_id          uuid REFERENCES locations(id) ON DELETE SET NULL,
    requested_by_user_id     uuid NOT NULL REFERENCES app_users(id) ON DELETE RESTRICT,
    approved_by_user_id      uuid REFERENCES app_users(id) ON DELETE SET NULL,
    status                   varchar(20) NOT NULL DEFAULT 'PENDING'
                             CHECK (status IN ('PENDING', 'APPROVED', 'COMPLETED', 'REJECTED', 'CANCELLED')),
    reason                   varchar(255) NOT NULL,
    requested_at             timestamptz NOT NULL DEFAULT now(),
    approved_at              timestamptz,
    transferred_at           timestamptz,
    notes                    text,
    created_at               timestamptz NOT NULL DEFAULT now(),
    updated_at               timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_transfer_previous_assignment
        FOREIGN KEY (previous_assignment_id, asset_id, previous_employee_id)
        REFERENCES assignments(id, asset_id, employee_id),
    CONSTRAINT fk_transfer_new_assignment
        FOREIGN KEY (new_assignment_id, asset_id, new_employee_id)
        REFERENCES assignments(id, asset_id, employee_id),
    CHECK (previous_employee_id <> new_employee_id),
    CHECK (status NOT IN ('APPROVED', 'COMPLETED')
           OR (approved_by_user_id IS NOT NULL AND approved_at IS NOT NULL)),
    CHECK (status <> 'COMPLETED'
           OR (new_assignment_id IS NOT NULL AND transferred_at IS NOT NULL))
);

CREATE UNIQUE INDEX uq_open_asset_transfer
    ON asset_transfers(asset_id)
    WHERE status IN ('PENDING', 'APPROVED');

CREATE UNIQUE INDEX uq_completed_transfer_previous_assignment
    ON asset_transfers(previous_assignment_id)
    WHERE status = 'COMPLETED';

CREATE UNIQUE INDEX uq_transfer_new_assignment
    ON asset_transfers(new_assignment_id)
    WHERE new_assignment_id IS NOT NULL;

-- ============================================================================
-- Disposal, valuation, lifecycle, and department history
-- ============================================================================

CREATE TABLE asset_disposals (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id           uuid NOT NULL UNIQUE REFERENCES assets(id),
    disposal_date      date NOT NULL DEFAULT current_date,
    disposal_method    varchar(50) NOT NULL,
    reason             varchar(255),
    status             varchar(20) NOT NULL DEFAULT 'PLANNED'
                       CHECK (status IN ('PLANNED', 'COMPLETED', 'CANCELLED')),
    approved_by_user_id uuid REFERENCES app_users(id) ON DELETE SET NULL,
    proceeds           numeric(14,2) NOT NULL DEFAULT 0 CHECK (proceeds >= 0),
    currency           char(3) NOT NULL DEFAULT 'USD',
    notes              text,
    created_at         timestamptz NOT NULL DEFAULT now(),
    updated_at         timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE asset_valuations (
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id             uuid NOT NULL REFERENCES assets(id),
    valuation_date       date NOT NULL DEFAULT current_date,
    book_value           numeric(14,2) NOT NULL CHECK (book_value >= 0),
    market_value         numeric(14,2) CHECK (market_value IS NULL OR market_value >= 0),
    depreciation_amount  numeric(14,2) NOT NULL DEFAULT 0 CHECK (depreciation_amount >= 0),
    valuation_method     varchar(50),
    currency             char(3) NOT NULL DEFAULT 'USD',
    created_by_user_id   uuid REFERENCES app_users(id) ON DELETE SET NULL,
    notes                text,
    created_at           timestamptz NOT NULL DEFAULT now(),
    UNIQUE (asset_id, valuation_date)
);

CREATE TABLE asset_lifecycle_events (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id          uuid NOT NULL REFERENCES assets(id),
    event_type        varchar(50) NOT NULL,
    event_at          timestamptz NOT NULL DEFAULT now(),
    actor_user_id     uuid REFERENCES app_users(id) ON DELETE SET NULL,
    from_status       varchar(25),
    to_status         varchar(25),
    from_location_id  uuid REFERENCES locations(id) ON DELETE SET NULL,
    to_location_id    uuid REFERENCES locations(id) ON DELETE SET NULL,
    assignment_id     uuid REFERENCES assignments(id) ON DELETE SET NULL,
    notes             text,
    metadata          jsonb NOT NULL DEFAULT '{}'::jsonb,
    created_at        timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE employee_department_history (
    id                    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id           uuid NOT NULL REFERENCES employees(id),
    previous_department_id uuid REFERENCES departments(id) ON DELETE SET NULL,
    new_department_id     uuid REFERENCES departments(id) ON DELETE SET NULL,
    changed_by_user_id    uuid REFERENCES app_users(id) ON DELETE SET NULL,
    effective_from        date NOT NULL DEFAULT current_date,
    effective_to          date,
    reason                varchar(255),
    notes                 text,
    created_at            timestamptz NOT NULL DEFAULT now(),
    CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

-- ============================================================================
-- Procurement extensions
-- ============================================================================

CREATE TABLE invoices (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number    varchar(80) NOT NULL UNIQUE,
    purchase_order_id uuid REFERENCES purchase_orders(id),
    vendor_id         uuid NOT NULL REFERENCES vendors(id),
    invoice_date      date NOT NULL DEFAULT current_date,
    due_date          date,
    paid_date         date,
    status            varchar(20) NOT NULL DEFAULT 'RECEIVED'
                      CHECK (status IN ('DRAFT', 'RECEIVED', 'APPROVED', 'PAID', 'OVERDUE', 'CANCELLED')),
    currency          char(3) NOT NULL DEFAULT 'USD',
    subtotal          numeric(14,2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    tax_amount        numeric(14,2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    total_amount      numeric(14,2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    recorded_by_user_id uuid REFERENCES app_users(id) ON DELETE SET NULL,
    notes             text,
    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now(),
    CHECK (due_date IS NULL OR due_date >= invoice_date),
    CHECK (paid_date IS NULL OR paid_date >= invoice_date)
);

CREATE TABLE vendor_contracts (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id          uuid NOT NULL REFERENCES vendors(id),
    contract_number    varchar(80) NOT NULL UNIQUE,
    title              varchar(180) NOT NULL,
    start_date         date NOT NULL,
    end_date           date,
    status             varchar(20) NOT NULL DEFAULT 'DRAFT'
                       CHECK (status IN ('DRAFT', 'ACTIVE', 'EXPIRED', 'TERMINATED')),
    contract_value     numeric(14,2) CHECK (contract_value IS NULL OR contract_value >= 0),
    currency           char(3) NOT NULL DEFAULT 'USD',
    document_reference varchar(255),
    notes              text,
    created_at         timestamptz NOT NULL DEFAULT now(),
    updated_at         timestamptz NOT NULL DEFAULT now(),
    CHECK (end_date IS NULL OR end_date >= start_date)
);

-- ============================================================================
-- Indexes
-- ============================================================================

CREATE INDEX idx_employees_department ON employees(department_id);
CREATE UNIQUE INDEX uq_employees_email_lower ON employees(lower(email));
CREATE UNIQUE INDEX uq_app_users_username_lower ON app_users(lower(username));
CREATE INDEX idx_assets_status ON assets(status);
CREATE INDEX idx_assets_category ON assets(category_id);
CREATE INDEX idx_assets_location ON assets(location_id);
CREATE INDEX idx_assets_department ON assets(department_id);
CREATE INDEX idx_assignments_employee ON assignments(employee_id);
CREATE INDEX idx_assignments_asset_history ON assignments(asset_id, assigned_at DESC);
CREATE INDEX idx_maintenance_asset_status ON maintenance_tickets(asset_id, status);
CREATE INDEX idx_maintenance_due_date ON maintenance_tickets(due_date)
    WHERE status IN ('OPEN', 'IN_PROGRESS');
CREATE INDEX idx_warranty_end_date ON warranty_policies(end_date);
CREATE INDEX idx_warranty_asset_history ON warranty_policies(asset_id, start_date DESC, end_date DESC);
CREATE INDEX idx_purchase_orders_status ON purchase_orders(status);
CREATE INDEX idx_notifications_recipient_unread
    ON notifications(recipient_user_id, created_at DESC)
    WHERE read_at IS NULL;
CREATE INDEX idx_audit_logs_entity
    ON audit_logs(entity_type, entity_id, created_at DESC);
CREATE INDEX idx_notification_preferences_user
    ON notification_preferences(user_id);
CREATE INDEX idx_asset_transfers_asset_date
    ON asset_transfers(asset_id, requested_at DESC);
CREATE INDEX idx_asset_transfers_status_date
    ON asset_transfers(status, requested_at DESC);
CREATE INDEX idx_asset_transfers_previous_employee
    ON asset_transfers(previous_employee_id, requested_at DESC);
CREATE INDEX idx_asset_transfers_new_employee
    ON asset_transfers(new_employee_id, requested_at DESC);
CREATE INDEX idx_asset_disposals_status_date
    ON asset_disposals(status, disposal_date);
CREATE INDEX idx_asset_valuations_asset_date
    ON asset_valuations(asset_id, valuation_date DESC);
CREATE INDEX idx_asset_lifecycle_events_asset_date
    ON asset_lifecycle_events(asset_id, event_at DESC);
CREATE INDEX idx_asset_lifecycle_events_type_date
    ON asset_lifecycle_events(event_type, event_at DESC);
CREATE INDEX idx_employee_department_history_employee
    ON employee_department_history(employee_id, effective_from DESC);
CREATE INDEX idx_invoices_vendor_status
    ON invoices(vendor_id, status);
CREATE INDEX idx_invoices_purchase_order
    ON invoices(purchase_order_id);
CREATE INDEX idx_vendor_contracts_vendor_status
    ON vendor_contracts(vendor_id, status);

-- ============================================================================
-- Timestamp triggers
-- ============================================================================

DO $$
DECLARE
    table_name text;
BEGIN
    FOREACH table_name IN ARRAY ARRAY[
        'departments',
        'employees',
        'app_users',
        'locations',
        'vendors',
        'asset_categories',
        'purchase_orders',
        'assets',
        'warranty_policies',
        'warranty_claims',
        'maintenance_tickets',
        'software_licenses',
        'system_settings',
        'organization_settings',
        'notification_preferences',
        'email_templates',
        'asset_transfers',
        'asset_disposals',
        'invoices',
        'vendor_contracts'
    ]
    LOOP
        EXECUTE format('DROP TRIGGER IF EXISTS trg_set_updated_at ON %I', table_name);
        EXECUTE format(
            'CREATE TRIGGER trg_set_updated_at
             BEFORE UPDATE ON %I
             FOR EACH ROW EXECUTE FUNCTION set_updated_at()',
            table_name
        );
    END LOOP;
END;
$$;

-- ============================================================================
-- Warranty current-policy maintenance
-- ============================================================================

CREATE OR REPLACE FUNCTION maintain_current_warranty_policy()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.is_current THEN
        UPDATE warranty_policies
        SET is_current = false,
            updated_at = now()
        WHERE asset_id = NEW.asset_id
          AND id IS DISTINCT FROM NEW.id
          AND is_current = true;
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_maintain_current_warranty_policy
    BEFORE INSERT OR UPDATE OF asset_id, is_current ON warranty_policies
    FOR EACH ROW EXECUTE FUNCTION maintain_current_warranty_policy();

-- ============================================================================
-- Asset assignment/status consistency
-- ============================================================================

CREATE OR REPLACE FUNCTION sync_asset_assignment_status(p_asset_id uuid)
RETURNS void
LANGUAGE plpgsql
AS $$
DECLARE
    active_assignment_exists boolean;
    current_status varchar(25);
BEGIN
    SELECT status
    INTO current_status
    FROM assets
    WHERE id = p_asset_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN;
    END IF;

    SELECT EXISTS (
        SELECT 1
        FROM assignments
        WHERE asset_id = p_asset_id
          AND status = 'ACTIVE'
    )
    INTO active_assignment_exists;

    IF active_assignment_exists THEN
        IF current_status IN ('AVAILABLE', 'ASSIGNED') THEN
            IF current_status <> 'ASSIGNED' THEN
                UPDATE assets
                SET status = 'ASSIGNED'
                WHERE id = p_asset_id;
            END IF;
        ELSE
            RAISE EXCEPTION
                'Asset % cannot have an active assignment while its status is %',
                p_asset_id, current_status;
        END IF;
    ELSIF current_status = 'ASSIGNED' THEN
        UPDATE assets
        SET status = 'AVAILABLE'
        WHERE id = p_asset_id;
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION sync_asset_assignment_status_from_assignment()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND OLD.asset_id IS DISTINCT FROM NEW.asset_id THEN
        PERFORM sync_asset_assignment_status(OLD.asset_id);
    END IF;

    PERFORM sync_asset_assignment_status(
        CASE WHEN TG_OP = 'DELETE' THEN OLD.asset_id ELSE NEW.asset_id END
    );

    RETURN NULL;
END;
$$;

CREATE TRIGGER trg_sync_asset_assignment_status
    AFTER INSERT OR DELETE OR UPDATE OF asset_id, status ON assignments
    FOR EACH ROW EXECUTE FUNCTION sync_asset_assignment_status_from_assignment();

CREATE OR REPLACE FUNCTION assert_asset_assignment_consistency()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    active_assignment_exists boolean;
    asset_id_to_check uuid;
BEGIN
    IF TG_TABLE_NAME = 'assets' THEN
        asset_id_to_check := NEW.id;
    ELSIF TG_OP = 'DELETE' THEN
        asset_id_to_check := OLD.asset_id;
    ELSE
        asset_id_to_check := NEW.asset_id;
    END IF;

    IF TG_TABLE_NAME = 'assets' THEN
        SELECT EXISTS (
            SELECT 1
            FROM assignments
            WHERE asset_id = asset_id_to_check
              AND status = 'ACTIVE'
        )
        INTO active_assignment_exists;

        IF (NEW.status = 'ASSIGNED') <> active_assignment_exists THEN
            RAISE EXCEPTION
                'Asset % status and active assignment are inconsistent',
                asset_id_to_check;
        END IF;
    ELSE
        PERFORM 1
        FROM assets
        WHERE id = asset_id_to_check
          AND (
              (status = 'ASSIGNED' AND EXISTS (
                  SELECT 1 FROM assignments
                  WHERE asset_id = asset_id_to_check AND status = 'ACTIVE'
              ))
              OR
              (status <> 'ASSIGNED' AND NOT EXISTS (
                  SELECT 1 FROM assignments
                  WHERE asset_id = asset_id_to_check AND status = 'ACTIVE'
              ))
          );

        IF NOT FOUND THEN
            RAISE EXCEPTION
                'Asset % status and active assignment are inconsistent',
                asset_id_to_check;
        END IF;
    END IF;

    RETURN NULL;
END;
$$;

CREATE CONSTRAINT TRIGGER trg_assert_asset_status
    AFTER INSERT OR UPDATE OF status ON assets
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION assert_asset_assignment_consistency();

CREATE CONSTRAINT TRIGGER trg_assert_assignment_status
    AFTER INSERT OR DELETE OR UPDATE OF asset_id, status ON assignments
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION assert_asset_assignment_consistency();

-- ============================================================================
-- License seat-capacity enforcement
-- ============================================================================

CREATE OR REPLACE FUNCTION enforce_license_seat_capacity()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    license_id_to_check uuid;
    seats integer;
    active_assignment_count integer;
BEGIN
    IF TG_TABLE_NAME = 'software_licenses' THEN
        license_id_to_check := NEW.id;
    ELSE
        license_id_to_check := NEW.license_id;
    END IF;

    SELECT seat_count
    INTO seats
    FROM software_licenses
    WHERE id = license_id_to_check
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN NULL;
    END IF;

    SELECT count(*)
    INTO active_assignment_count
    FROM license_assignments
    WHERE license_id = license_id_to_check
      AND status = 'ACTIVE';

    IF active_assignment_count > seats THEN
        RAISE EXCEPTION
            'License % has % active assignments but only % seats',
            license_id_to_check, active_assignment_count, seats;
    END IF;

    RETURN NULL;
END;
$$;

CREATE TRIGGER trg_enforce_license_assignment_capacity
    AFTER INSERT OR UPDATE OF license_id, status ON license_assignments
    FOR EACH ROW EXECUTE FUNCTION enforce_license_seat_capacity();

CREATE TRIGGER trg_enforce_license_seat_count
    AFTER UPDATE OF seat_count ON software_licenses
    FOR EACH ROW EXECUTE FUNCTION enforce_license_seat_capacity();

-- ============================================================================
-- Automatic lifecycle event for asset status/location changes
-- ============================================================================

CREATE OR REPLACE FUNCTION record_asset_status_location_event()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    lifecycle_event_type varchar(50);
BEGIN
    IF OLD.status IS NOT DISTINCT FROM NEW.status
       AND OLD.location_id IS NOT DISTINCT FROM NEW.location_id THEN
        RETURN NEW;
    END IF;

    lifecycle_event_type :=
        CASE
            WHEN OLD.status IS DISTINCT FROM NEW.status
             AND OLD.location_id IS DISTINCT FROM NEW.location_id
                THEN 'STATUS_AND_LOCATION_CHANGED'
            WHEN OLD.status IS DISTINCT FROM NEW.status
                THEN 'STATUS_CHANGED'
            ELSE 'LOCATION_CHANGED'
        END;

    INSERT INTO asset_lifecycle_events (
        asset_id,
        event_type,
        event_at,
        from_status,
        to_status,
        from_location_id,
        to_location_id,
        metadata
    )
    VALUES (
        NEW.id,
        lifecycle_event_type,
        now(),
        OLD.status,
        NEW.status,
        OLD.location_id,
        NEW.location_id,
        '{}'::jsonb
    );

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_record_asset_status_location_event
    AFTER UPDATE OF status, location_id ON assets
    FOR EACH ROW EXECUTE FUNCTION record_asset_status_location_event();

-- ============================================================================
-- Automatic employee department history
-- ============================================================================

CREATE OR REPLACE FUNCTION record_employee_department_history()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF OLD.department_id IS NOT DISTINCT FROM NEW.department_id THEN
        RETURN NEW;
    END IF;

    INSERT INTO employee_department_history (
        employee_id,
        previous_department_id,
        new_department_id,
        effective_from
    )
    VALUES (
        NEW.id,
        OLD.department_id,
        NEW.department_id,
        current_date
    );

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_record_employee_department_history
    AFTER UPDATE OF department_id ON employees
    FOR EACH ROW EXECUTE FUNCTION record_employee_department_history();

-- ============================================================================
-- Reporting views
-- ============================================================================

CREATE OR REPLACE VIEW v_asset_current_assignment AS
SELECT
    asset.id AS asset_id,
    asset.asset_tag,
    asset.name AS asset_name,
    assignment.id AS assignment_id,
    assignment.employee_id,
    employee.employee_number,
    employee.first_name,
    employee.last_name,
    assignment.assigned_at,
    asset.location_id,
    asset.department_id
FROM assets asset
LEFT JOIN assignments assignment
    ON assignment.asset_id = asset.id
   AND assignment.status = 'ACTIVE'
LEFT JOIN employees employee
    ON employee.id = assignment.employee_id;

CREATE OR REPLACE VIEW v_asset_current_valuation AS
SELECT DISTINCT ON (valuation.asset_id)
    valuation.asset_id,
    valuation.valuation_date,
    valuation.book_value,
    valuation.market_value,
    valuation.depreciation_amount,
    valuation.valuation_method,
    valuation.currency
FROM asset_valuations valuation
ORDER BY valuation.asset_id, valuation.valuation_date DESC, valuation.id DESC;

CREATE OR REPLACE VIEW v_warranty_reporting_status AS
SELECT
    policy.id AS warranty_id,
    policy.asset_id,
    policy.policy_number,
    policy.start_date,
    policy.end_date,
    policy.status AS stored_status,
    policy.is_current,
    CASE
        WHEN policy.status = 'VOID' THEN 'VOID'
        WHEN current_date > policy.end_date THEN 'EXPIRED'
        WHEN policy.end_date <= current_date + 30 THEN 'EXPIRING'
        ELSE 'ACTIVE'
    END AS reporting_status
FROM warranty_policies policy;

-- Ensure the single settings row exists for the settings page.
INSERT INTO organization_settings (id)
VALUES (1)
ON CONFLICT (id) DO NOTHING;

COMMIT;

