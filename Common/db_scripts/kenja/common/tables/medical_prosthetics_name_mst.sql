-- $Id: ddcb80c3455e14aba19de9f08335979b89b1d5e0 $

DROP TABLE MEDICAL_PROSTHETICS_NAME_MST
CREATE TABLE MEDICAL_PROSTHETICS_NAME_MST( \
    NAMECD                  VARCHAR(3)    NOT NULL, \
    NAME                    VARCHAR(90), \
    DIV                     VARCHAR(1), \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE MEDICAL_PROSTHETICS_NAME_MST ADD CONSTRAINT PK_MED_PR_NAME_MST PRIMARY KEY (NAMECD)