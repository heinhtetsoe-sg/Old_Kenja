-- kanji=漢字
-- $Id: ac83b782e5cb985518c160dd88d1adf9da00f263 $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--

drop table GRD_HEXAM_ENTREMARK_J_HDAT

create table GRD_HEXAM_ENTREMARK_J_HDAT \
    (SCHREGNO           varchar(8) not null, \
     COMMENTEX_A_CD     varchar(1), \
     DISEASE            varchar(259), \
     DOC_REMARK         varchar(90), \
     TR_REMARK          varchar(159), \
     TOTALSTUDYACT      varchar(746), \
     TOTALSTUDYVAL      varchar(845), \
     BEHAVEREC_REMARK   varchar(845), \
     HEALTHREC          varchar(845), \
     SPECIALACTREC      varchar(845), \
     TRIN_REF           varchar(1248), \
     REMARK             varchar(1500), \
     REGISTERCD         varchar(8), \
     UPDATED            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table GRD_HEXAM_ENTREMARK_J_HDAT add constraint PK_GHX_ENTRMRK_J_H primary key (SCHREGNO)

