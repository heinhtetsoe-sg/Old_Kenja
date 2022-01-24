-- kanji=´Á»ú
-- $Id: 6d37b765467282b5be77ef495502e7a6ed2d7bf3 $

DROP TABLE HEALTH_QUESTION_MST_OLD
RENAME TABLE HEALTH_QUESTION_MST TO HEALTH_QUESTION_MST_OLD
create table HEALTH_QUESTION_MST ( \
    QUESTIONCD      varchar(2)  not null, \
    CONTENTS        varchar(120), \
    SORT            varchar(2), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

INSERT INTO HEALTH_QUESTION_MST \
    SELECT \
        QUESTIONCD, \
        CONTENTS, \
        SORT, \
        REGISTERCD, \
        UPDATED \
    FROM \
        HEALTH_QUESTION_MST_OLD

alter table HEALTH_QUESTION_MST add constraint pk_hea_que_mst \
      primary key (QUESTIONCD)
