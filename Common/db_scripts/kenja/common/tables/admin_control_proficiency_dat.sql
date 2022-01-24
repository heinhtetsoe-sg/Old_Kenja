-- $Id: f97b10f251606c4f4bac6c18649c02573ba59ebe $

DROP TABLE ADMIN_CONTROL_PROFICIENCY_DAT
CREATE TABLE ADMIN_CONTROL_PROFICIENCY_DAT  \
(  \
        "YEAR"              VARCHAR(4)      NOT NULL, \
        "PROFICIENCYDIV"    VARCHAR(2)      NOT NULL, \
        "PROFICIENCYCD"     VARCHAR(4)      NOT NULL, \
        "REGISTERCD"        VARCHAR(10),  \
        "UPDATED"           TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS


ALTER TABLE ADMIN_CONTROL_PROFICIENCY_DAT  \
ADD CONSTRAINT PK_ADMIN_CONL_PDAT  \
PRIMARY KEY  \
( \
YEAR, \
PROFICIENCYDIV, \
PROFICIENCYCD \
)
