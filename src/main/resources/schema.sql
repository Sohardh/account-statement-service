drop table if exists account_service.statements;
create table if not exists account_service.statements
(
    id_statement       bigserial        not null
        constraint statements_pk
            primary key,
    ts_date            date             not null,
    tx_description     text             not null,
    ts_value_date      date             not null,
    tx_ref_no          text             not null,
    nu_debit           double precision,
    nu_credit          double precision,
    nu_closing_balance double precision not null,
    constraint statements_description_date_unique
        unique (tx_ref_no)
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
        constraint job_statement_urls_tx_url_unique
            unique (tx_url)
);