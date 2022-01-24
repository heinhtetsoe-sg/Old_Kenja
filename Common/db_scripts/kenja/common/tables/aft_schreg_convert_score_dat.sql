-- kanji=����
-- $Id: 70c6b63fcfeac3cc4e1653dfb8c6ecaf1d43248f $

-- ��ǯ�٥ޥ���
-- ���:���Υե������ EUC/LF�Τ� �Ǥʤ���Фʤ�ʤ���
-- Ŭ����ˡ:
--    1.�ǡ����١�����³
--    2.db2 +c -f <���Υե�����>
--    3.���ߥåȤ���ʤ顢db2 +c commit�����ľ���ʤ顢db2 +c rollback

drop   table AFT_SCHREG_CONVERT_SCORE_DAT

create table AFT_SCHREG_CONVERT_SCORE_DAT ( \
    YEAR                            varchar(4) not null, \
    SCHREGNO                        varchar(8) not null, \
    PROFICIENCY1_SCORE1             smallint, \
    PROFICIENCY1_SCORE2             smallint, \
    PROFICIENCY1_SCORE3             smallint, \
    PROFICIENCY1_AVG                decimal(6, 2), \
    PROFICIENCY2_SCORE1             smallint, \
    PROFICIENCY2_SCORE2             smallint, \
    PROFICIENCY2_SCORE3             smallint, \
    PROFICIENCY2_AVG                decimal(6, 2), \
    GRADE1_TOTAL_SCORE              smallint, \
    GRADE2_TOTAL_SCORE              smallint, \
    GRADE3_TOTAL_SCORE              smallint, \
    TOTAL_AVG                       decimal(6, 2), \
    ATTEND_ADJUSTMENT_SCORE         smallint, \
    ADJUSTMENT_SCORE                smallint, \
    CONVERT_SCORE                   decimal(6, 2), \
    CONVERT_RANK                    smallint, \
    CONVERT_DEVIATION               decimal(6, 2), \
    RECOMMENDATION_DEPARTMENT_CD    varchar(2), \
    REGISTERCD                      varchar(10), \
    UPDATED                         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table AFT_SCHREG_CONVERT_SCORE_DAT add constraint PK_AFT_SCHREG_CONVERT_SCORE_DAT primary key (YEAR, SCHREGNO)
