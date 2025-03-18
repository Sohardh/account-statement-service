drop table if exists account_service.statements;
create table if not exists account_service.statements
(
    id_statement          bigserial        not null
        constraint statements_pk
            primary key,
    ts_date               date             not null,
    tx_internal_reference text             not null,
    tx_description        text             not null,
    ts_value_date         date             not null,
    tx_ref_no             text             not null,
    nu_debit              double precision,
    nu_credit             double precision,
    nu_closing_balance    double precision not null,
    bl_processed          boolean          not null,
    ts_processed_at       timestamp without time zone,
    constraint statements_description_ref_no_unique
        unique (tx_description, tx_ref_no),
    constraint statements_internal_reference_unique
        unique (tx_internal_reference)
);

create index if not exists statements_ts_date_index
    on account_service.statements (ts_value_date);


drop table if exists account_service.job_statement_urls;
create table if not exists account_service.job_statement_urls
(
    id_job_statement_urls bigserial                   not null
        constraint job_statement_urls_pk
            primary key,
    tx_url                text                        not null,
    ts_created_at         timestamp without time zone not null,
    bl_processed          boolean                     not null,
    ts_processed_at       timestamp without time zone,
    tx_html_body          text,
    constraint job_statement_urls_tx_url_unique
        unique (tx_url)
);


drop table if exists account_service.job_statement_context;
create table if not exists account_service.job_statement_context
(
    tx_job_name text not null
        constraint job_statement_context_pk
            primary key,
    js_context  text
);


drop table if exists account_service.firefly_statements;
create table if not exists account_service.firefly_statements
(
    id_firefly_statements bigserial not null
        constraint firefly_statements_pk
            primary key,
    ts_date               date      not null,
    tx_description        text      not null,
    tx_internal_reference text      not null,
    nu_debit              double precision,
    nu_credit             double precision,
    bl_processed          boolean   not null,
    ts_processed_at       timestamp without time zone,
    arr_tags              text[],
    tx_category           text,
    tx_opposing_account   text      not null,
    tx_asset_account      text,

    constraint firefly_statements_internal_reference_unique
        unique (tx_internal_reference)
);

create index if not exists statements_ts_date_index
    on account_service.statements (ts_value_date);