drop table CHAIR_TEST_FAC_LAYOUT_HDAT

create table CHAIR_TEST_FAC_LAYOUT_HDAT( \
    YEAR        varchar(4)  not null, \
    SEMESTER    varchar(1)  not null, \
    CHAIRCD     varchar(7)  not null, \
    FACCD       varchar(4)  not null, \
    ROWS        smallint    not null, \
    COLUMNS     smallint    not null, \
    REGISTERCD  varchar(10), \
    UPDATED     timestamp default current timestamp \
) in usr1dms index in idx1dms

COMMENT ON TABLE CHAIR_TEST_FAC_LAYOUT_HDAT IS 'CHAIR_TEST_FAC_LAYOUT_HDAT'

COMMENT ON CHAIR_TEST_FAC_LAYOUT_HDAT \
    (YEAR           IS '年度', \
     SEMESTER       IS '学期', \
     CHAIRCD        IS '講座CD', \
     FACCD          IS '施設CD', \
     ROWS           IS '行', \
     COLUMNS        IS '列', \
     REGISTERCD     IS '最終更新者', \
     UPDATED        IS '最終更新日時' \
     )

alter table CHAIR_TEST_FAC_LAYOUT_HDAT add constraint PK_CHAIR_TEST_FAC_LAYOUT_HDAT primary key (YEAR, SEMESTER, CHAIRCD, FACCD)
