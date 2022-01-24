-- $Id: de8800f91bf8e86d4cb8ac7a7c2e7093fca6fa50 $

DROP TABLE GRD_HTRAINREMARK_HDAT_OLD
RENAME TABLE GRD_HTRAINREMARK_HDAT TO GRD_HTRAINREMARK_HDAT_OLD
create table GRD_HTRAINREMARK_HDAT ( \
     SCHREGNO             varchar(8) not null, \
     TOTALSTUDYACT        varchar(534), \
     TOTALSTUDYVAL        varchar(802), \
     TOTALSTUDYACT2       varchar(534), \
     TOTALSTUDYVAL2       varchar(802), \
     CREDITREMARK         varchar(802), \
     REGISTERCD           varchar(10), \
     UPDATED              timestamp default current timestamp \
    ) in usr1dms index in idx1dms

ALTER TABLE GRD_HTRAINREMARK_HDAT ADD CONSTRAINT PK_HTRAINR_D PRIMARY KEY (SCHREGNO)

INSERT INTO GRD_HTRAINREMARK_HDAT \
    SELECT \
        SCHREGNO,                   \
        TOTALSTUDYACT,              \
        TOTALSTUDYVAL,              \
        CAST(NULL AS varchar(534)), \
        CAST(NULL AS varchar(802)), \
        CAST(NULL AS varchar(802)), \
        REGISTERCD,                 \
        UPDATED                     \
    FROM \
        GRD_HTRAINREMARK_HDAT_OLD

