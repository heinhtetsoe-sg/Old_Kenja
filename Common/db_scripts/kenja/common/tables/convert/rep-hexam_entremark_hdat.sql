-- kanji=漢字
-- $Id: b562c517ac27251dc6ca743c70046fc37dd05b9a $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop table HEXAM_ENTREMARK_HDAT_OLD
create table HEXAM_ENTREMARK_HDAT_OLD like HEXAM_ENTREMARK_HDAT
insert into  HEXAM_ENTREMARK_HDAT_OLD select * from HEXAM_ENTREMARK_HDAT

drop table HEXAM_ENTREMARK_HDAT

create table HEXAM_ENTREMARK_HDAT \
    (SCHREGNO                   varchar(8) not null, \
     COMMENTEX_A_CD             varchar(1), \
     DISEASE                    varchar(259), \
     DOC_REMARK                 varchar(90), \
     TR_REMARK                  varchar(159), \
     TOTALSTUDYACT              varchar(746), \
     TOTALSTUDYVAL              varchar(845), \
     BEHAVEREC_REMARK           varchar(845), \
     HEALTHREC                  varchar(845), \
     SPECIALACTREC              varchar(845), \
     TRIN_REF                   varchar(1248), \
     REMARK                     varchar(1500), \
     REMARK2                    varchar(500), \
     TOTALSTUDYACT_SLASH_FLG    varchar(1), \
     TOTALSTUDYVAL_SLASH_FLG    varchar(1), \
     REGISTERCD                 varchar(10), \
     UPDATED                    timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table HEXAM_ENTREMARK_HDAT add constraint PK_HEX_ENTREMARK_H primary key (SCHREGNO)

insert into HEXAM_ENTREMARK_HDAT \
    SELECT \
        SCHREGNO, \
        COMMENTEX_A_CD, \
        DISEASE, \
        DOC_REMARK, \
        TR_REMARK, \
        TOTALSTUDYACT, \
        TOTALSTUDYVAL, \
        BEHAVEREC_REMARK, \
        HEALTHREC, \
        SPECIALACTREC, \
        TRIN_REF, \
        REMARK, \
        cast(NULL as varchar(1)) as REMARK2, \
        TOTALSTUDYACT_SLASH_FLG, \
        TOTALSTUDYVAL_SLASH_FLG, \
        REGISTERCD, \
        UPDATED \
    FROM \
        HEXAM_ENTREMARK_HDAT_OLD
