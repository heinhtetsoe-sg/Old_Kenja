-- kanji=����
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop table AFT_RECOMMENDATION_RANK_HEAD_DAT

create table AFT_RECOMMENDATION_RANK_HEAD_DAT ( \
    YEAR                   varchar(4) not null, \
    GRADE                  varchar(2) not null, \
    TENTATIVE_FLG          varchar(1), \
    PERCENTAGE             smallint, \
    REGISTERCD             varchar(10), \
    UPDATED                timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_RECOMMENDATION_RANK_HEAD_DAT add constraint PK_AFT_RECOMMENDATION_RANK_HEAD_DAT primary key (YEAR, GRADE)
