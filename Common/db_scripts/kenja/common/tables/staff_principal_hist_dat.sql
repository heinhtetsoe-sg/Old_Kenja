-- $Id: 943eb8e57aa139549fe6f6bdab8ae0a71b450b8a $

DROP TABLE STAFF_PRINCIPAL_HIST_DAT

CREATE TABLE STAFF_PRINCIPAL_HIST_DAT( \
    SCHOOL_KIND            varchar(2)    not null, \
    FROM_DATE              date          not null, \
    TO_DATE                date, \
    STAFFCD                varchar(8), \
    REGISTERCD             varchar(8), \
    UPDATED                timestamp default current timestamp \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE STAFF_PRINCIPAL_HIST_DAT ADD CONSTRAINT PK_PRINCIPAL_HIST PRIMARY KEY (SCHOOL_KIND, FROM_DATE)
