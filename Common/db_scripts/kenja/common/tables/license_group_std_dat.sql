-- kanji=´Á»ú
-- $Id: e681e42cb04b2655c5041bc98a86721bcbceb67e $

DROP TABLE LICENSE_GROUP_STD_DAT
CREATE TABLE LICENSE_GROUP_STD_DAT \
(  \
        "GROUP_DIV"         VARCHAR(2) NOT NULL, \
        "SCHREGNO"          VARCHAR(8) NOT NULL, \
        "SELECT_DIV"        VARCHAR(1) NOT NULL, \
        "REMARK1"           VARCHAR(1), \
        "REMARK2"           VARCHAR(1), \
        "REMARK3"           VARCHAR(1), \
        "REMARK4"           VARCHAR(1), \
        "REMARK5"           VARCHAR(1), \
        "REMARK6"           VARCHAR(1), \
        "REMARK7"           VARCHAR(1), \
        "REMARK8"           VARCHAR(1), \
        "REMARK9"           VARCHAR(1), \
        "REMARK10"          VARCHAR(1), \
        "REGISTERCD"        VARCHAR(10), \
        "UPDATED"           TIMESTAMP DEFAULT CURRENT TIMESTAMP  \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE LICENSE_GROUP_STD_DAT ADD CONSTRAINT PK_LI_GP_STD_DAT PRIMARY KEY (GROUP_DIV, SCHREGNO, SELECT_DIV)
