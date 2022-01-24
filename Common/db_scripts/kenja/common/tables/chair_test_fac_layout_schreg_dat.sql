drop table CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT

create table CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT ( \
    EXECUTEDATE date        not null, \
    PERIODCD    varchar(1)  not null, \
    CHAIRCD     varchar(7)  not null, \
    ROWS        smallint    not null, \
    COLUMNS     smallint    not null, \
    FACCD       varchar(4)  not null, \
    TESTKINDCD  varchar(2)  not null, \
    TESTITEMCD  varchar(2)  not null, \
    SCHREGNO    varchar(8)  not null, \
    SEAT_NO     smallint    not null, \
    YEAR        varchar(4)  not null, \
    SEMESTER    varchar(1)  not null, \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

COMMENT ON TABLE CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT IS 'CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT'

COMMENT ON CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT \
    (EXECUTEDATE    IS '実施日', \
     PERIODCD       IS '校時', \
     CHAIRCD        IS '講座CD', \
     ROWS           IS '行', \
     COLUMNS        IS '列', \
     FACCD          IS '施設CD', \
     TESTKINDCD     IS 'テスト種別', \
     TESTITEMCD     IS 'テスト項目', \
     SCHREGNO       IS '学籍番号', \
     SEAT_NO        IS '座席番号', \
     YEAR           IS '年度', \
     SEMESTER       IS '学期', \
     REGISTERCD     IS '最終更新者', \
     UPDATED        IS '最終更新日時' \
     )

alter table CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT add constraint PK_CHAIR_TEST_FAC_LAYOUT_SCHREG_DAT primary key (EXECUTEDATE, PERIODCD, CHAIRCD, ROWS, COLUMNS)

