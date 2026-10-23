CREATE TABLE simulations
(
    id                       UUID PRIMARY KEY,
    property_value           NUMERIC(15, 2)           NOT NULL,
    down_payment             NUMERIC(15, 2)           NOT NULL,
    financed_amount          NUMERIC(15, 2)           NOT NULL,
    term_months              INTEGER                  NOT NULL,
    annual_interest_rate     NUMERIC(6, 4)            NOT NULL,
    first_installment_amount NUMERIC(15, 2)           NOT NULL,
    last_installment_amount  NUMERIC(15, 2)           NOT NULL,
    total_interest           NUMERIC(15, 2)           NOT NULL,
    total_paid               NUMERIC(15, 2)           NOT NULL,
    state                    VARCHAR(2)               NOT NULL,
    city                     VARCHAR(100),
    is_first_property        BOOLEAN                  NOT NULL,
    itbi_estimated           NUMERIC(15, 2)           NOT NULL,
    registry_estimated       NUMERIC(15, 2)           NOT NULL,
    bank_appraisal_fee       NUMERIC(15, 2)           NOT NULL,
    total_acquisition_costs  NUMERIC(15, 2)           NOT NULL,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL
);