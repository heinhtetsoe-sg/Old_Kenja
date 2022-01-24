-- $Id: 4be525bee2b132c0c5955fcb5c34f5454115198a $

DROP TABLE SUBCLASS_REPLACE_SUBSTITUTION_DAT_OLD
RENAME TABLE SUBCLASS_REPLACE_SUBSTITUTION_DAT TO SUBCLASS_REPLACE_SUBSTITUTION_DAT_OLD
CREATE TABLE SUBCLASS_REPLACE_SUBSTITUTION_DAT( \
    REPLACECD                   varchar(1) not null, \
    YEAR                        varchar(4) not null, \
    SUBSTITUTION_CLASSCD        VARCHAR(2) NOT NULL, \
    SUBSTITUTION_SCHOOL_KIND    VARCHAR(2) NOT NULL, \
    SUBSTITUTION_CURRICULUM_CD  VARCHAR(2) NOT NULL, \
    SUBSTITUTION_SUBCLASSCD     VARCHAR(6) NOT NULL, \
    ATTEND_CLASSCD              VARCHAR(2) NOT NULL, \
    ATTEND_SCHOOL_KIND          VARCHAR(2) NOT NULL, \
    ATTEND_CURRICULUM_CD        VARCHAR(2) NOT NULL, \
    ATTEND_SUBCLASSCD           VARCHAR(6) NOT NULL, \
    SUBSTITUTION_TYPE_FLG       varchar(1), \
    STUDYREC_CREATE_FLG         varchar(1), \
    PRINT_FLG1                  varchar(1), \
    PRINT_FLG2                  varchar(1), \
    PRINT_FLG3                  varchar(1), \
    REGISTERCD                  varchar(8), \
    UPDATED                     TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

INSERT INTO SUBCLASS_REPLACE_SUBSTITUTION_DAT \
    SELECT \
        REPLACECD, \
        YEAR, \
        LEFT(SUBSTITUTION_SUBCLASSCD, 2) AS SUBSTITUTION_CLASSCD, \
        'H' AS SUBSTITUTION_SCHOOL_KIND, \
        '2' AS SUBSTITUTION_CURRICULUM_CD, \
        SUBSTITUTION_SUBCLASSCD, \
        LEFT(ATTEND_SUBCLASSCD, 2) AS ATTEND_CLASSCD, \
        'H' AS ATTEND_SCHOOL_KIND, \
        '2' AS ATTEND_CURRICULUM_CD, \
        ATTEND_SUBCLASSCD, \
        SUBSTITUTION_TYPE_FLG, \
        STUDYREC_CREATE_FLG, \
        PRINT_FLG1, \
        PRINT_FLG2, \
        PRINT_FLG3, \
        REGISTERCD, \
        UPDATED \
    FROM \
        SUBCLASS_REPLACE_SUBSTITUTION_DAT_OLD

alter table SUBCLASS_REPLACE_SUBSTITUTION_DAT add constraint PK_SUBREPSUBST_DAT \
        primary key (YEAR, SUBSTITUTION_CLASSCD, SUBSTITUTION_SCHOOL_KIND, SUBSTITUTION_CURRICULUM_CD, SUBSTITUTION_SUBCLASSCD, ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD)
