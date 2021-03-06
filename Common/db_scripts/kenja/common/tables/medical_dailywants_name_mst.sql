-- $Id: 41ab76940371147558baca7a749ba2199d11bc9a $

DROP TABLE MEDICAL_DAILYWANTS_NAME_MST
CREATE TABLE MEDICAL_DAILYWANTS_NAME_MST( \
    NAMECD                  VARCHAR(3)    NOT NULL, \
    NAME                    VARCHAR(90), \
    DIV                     VARCHAR(5), \
    REGISTERCD              VARCHAR(8), \
    UPDATED                 TIMESTAMP DEFAULT CURRENT TIMESTAMP \ 
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE MEDICAL_DAILYWANTS_NAME_MST ADD CONSTRAINT PK_MED_DA_NAME_MST PRIMARY KEY (NAMECD)
