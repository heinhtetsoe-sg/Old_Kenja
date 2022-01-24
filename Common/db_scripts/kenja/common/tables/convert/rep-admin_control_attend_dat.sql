-- $Id: be84790847a701bd5fee902727e09b9543a2af94 $

DROP TABLE ADMIN_CONTROL_ATTEND_DAT_OLD
CREATE TABLE ADMIN_CONTROL_ATTEND_DAT_OLD LIKE ADMIN_CONTROL_ATTEND_DAT
INSERT INTO ADMIN_CONTROL_ATTEND_DAT_OLD SELECT * FROM ADMIN_CONTROL_ATTEND_DAT

drop table ADMIN_CONTROL_ATTEND_DAT
create table ADMIN_CONTROL_ATTEND_DAT ( \
    "YEAR"          VARCHAR(4)      NOT NULL, \
    "SCHOOL_KIND"   VARCHAR(2)      NOT NULL, \
    "CONTROL_DIV"   VARCHAR(1)      NOT NULL, \
    "ATTEND_DIV"    VARCHAR(1)      NOT NULL, \
    "PROGRAMID"     VARCHAR(10)     NOT NULL, \
    "GROUPCD"       VARCHAR(4)      NOT NULL, \
    "GRADE"         VARCHAR(2)      NOT NULL, \
    "COURSECD"      VARCHAR(1)      NOT NULL, \
    "MAJORCD"       VARCHAR(3)      NOT NULL, \
    "ATTEND_ITEM"   VARCHAR(20)     NOT NULL, \
    "SHOWORDER"     VARCHAR(2), \
    "INPUT_FLG"     VARCHAR(1), \
    "REGISTERCD"    VARCHAR(10), \
    "UPDATED"       timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ADMIN_CONTROL_ATTEND_DAT add constraint PK_ADMIN_CTRL_ATT \
primary key (YEAR, SCHOOL_KIND, CONTROL_DIV, ATTEND_DIV, PROGRAMID, GROUPCD, GRADE, COURSECD, MAJORCD, ATTEND_ITEM)

INSERT INTO ADMIN_CONTROL_ATTEND_DAT \
SELECT   \
    T1.YEAR,  \
    T1.SCHOOL_KIND,  \
    T1.CONTROL_DIV,  \
    T1.ATTEND_DIV,  \
    T1.PROGRAMID,  \
    T1.GROUPCD,  \
    REGD.GRADE,  \
    REGD.COURSECD,  \
    REGD.MAJORCD,  \
    T1.ATTEND_ITEM,  \
    MAX(T1.SHOWORDER),  \
    MAX(T1.INPUT_FLG),  \
    'ALPOKI',  \
    MAX(T1.UPDATED)  \
FROM  \
    ADMIN_CONTROL_ATTEND_DAT_OLD T1  \
    LEFT JOIN (SELECT   \
                LREGD.YEAR,  \
                LREGD.GRADE,  \
                LREGD.COURSECD,  \
                LREGD.MAJORCD  \
               FROM  \
                SCHREG_REGD_DAT LREGD  \
               WHERE  \
                LREGD.COURSECD IS NOT NULL  \
                AND LREGD.MAJORCD IS NOT NULL  \
               GROUP BY  \
                LREGD.YEAR,  \
                LREGD.GRADE,  \
                LREGD.COURSECD,  \
                LREGD.MAJORCD  \
            ) REGD ON T1.YEAR = REGD.YEAR \
GROUP BY \
    T1.YEAR, \
    T1.SCHOOL_KIND, \
    T1.CONTROL_DIV, \
    T1.ATTEND_DIV, \
    T1.PROGRAMID, \
    T1.GROUPCD, \
    REGD.GRADE, \
    REGD.COURSECD, \
    REGD.MAJORCD, \
    T1.ATTEND_ITEM, \
    'ALPOKI'
