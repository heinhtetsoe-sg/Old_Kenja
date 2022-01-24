-- kanji=漢字
-- $Id: b2402c9508a7b7eba7b8f30561bea615fff8f7fd $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

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
