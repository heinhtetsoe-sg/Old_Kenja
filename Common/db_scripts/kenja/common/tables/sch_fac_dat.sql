drop table SCH_FAC_DAT

create table SCH_FAC_DAT( \
    EXECUTEDATE date        not null, \
    PERIODCD    varchar(1)  not null, \
    CHAIRCD     varchar(7)  not null, \
    FACCD       varchar(4)  not null, \
    YEAR        varchar(4), \
    SEMESTER    varchar(1), \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

COMMENT ON TABLE SCH_FAC_DAT IS 'SCH_FAC_DAT'

COMMENT ON SCH_FAC_DAT \
    (EXECUTEDATE IS '実施日付', \
     PERIODCD IS '校時CD', \
     CHAIRCD IS '講座CD', \
     FACCD IS '施設CD', \
     YEAR IS '年度', \
     SEMESTER IS '学期', \
     REGISTERCD IS '最終更新者', \
     UPDATED IS '最終更新日時' \
     )

alter table SCH_FAC_DAT add constraint PK_SCH_FAC_DAT primary key (EXECUTEDATE, PERIODCD, CHAIRCD, FACCD)
