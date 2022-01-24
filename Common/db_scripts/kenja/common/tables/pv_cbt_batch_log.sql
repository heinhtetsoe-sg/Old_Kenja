
create table PV_CBT_BATCH_LOG( \
    UPDATED       TIMESTAMP    NOT NULL, \
    SCHOOL_CD     VARCHAR(10)  NOT NULL, \
    YEAR          VARCHAR(4)   NOT NULL, \
    FROM_DATE     DATE        , \
    TO_DATE       DATE        , \
    INSERT_COUNT  INTEGER     , \
    UPDATE_COUNT  INTEGER      \
 ) in usr1dms index in idx1dms

alter table PV_CBT_BATCH_LOG add constraint PK_PV_CBT_BTH_LOG primary key (UPDATED,SCHOOL_CD)

