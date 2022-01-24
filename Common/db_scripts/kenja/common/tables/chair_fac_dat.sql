drop table CHAIR_FAC_DAT

create table CHAIR_FAC_DAT( \
    YEAR        varchar(4)  not null, \
    SEMESTER    varchar(1)  not null, \
    CHAIRCD     varchar(7)  not null, \
    FACCD       varchar(4)  not null, \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

COMMENT ON TABLE CHAIR_FAC_DAT IS 'CHAIR_FAC_DAT'

COMMENT ON CHAIR_FAC_DAT \
    (YEAR IS '年度', \
     SEMESTER IS '学期', \
     CHAIRCD IS '講座CD', \
     FACCD IS '施設CD', \
     REGISTERCD IS '最終更新者', \
     UPDATED IS '最終更新日時' \
     )

alter table CHAIR_FAC_DAT add constraint PK_CHAIR_FAC_DAT primary key (YEAR, SEMESTER, CHAIRCD, FACCD)
