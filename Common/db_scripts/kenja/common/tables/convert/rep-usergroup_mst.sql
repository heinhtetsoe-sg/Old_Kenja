-- $Id: fb7f187d7a65b99c2dec8b14cdc2413b13682642 $

drop table USERGROUP_MST_OLD
create table USERGROUP_MST_OLD like USERGROUP_MST
insert into USERGROUP_MST_OLD select * from USERGROUP_MST

DROP TABLE USERGROUP_MST

CREATE TABLE USERGROUP_MST( \
    SCHOOLCD       VARCHAR(12) NOT NULL, \
    SCHOOL_KIND    VARCHAR(2)  NOT NULL, \
    GROUPCD        VARCHAR(4)  NOT NULL, \
    GROUPNAME      VARCHAR(60), \
    REGISTERCD     VARCHAR(10), \
    UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
) IN USR1DMS INDEX IN IDX1DMS

INSERT INTO USERGROUP_MST \
    SELECT \
        '000000000000', \
        'H', \
        GROUPCD, \
        GROUPNAME, \
        REGISTERCD, \
        UPDATED \
    FROM \
        USERGROUP_MST_OLD

ALTER TABLE USERGROUP_MST ADD CONSTRAINT PK_USERGROUP_MST PRIMARY KEY (SCHOOLCD, SCHOOL_KIND, GROUPCD)