-- kanji=漢字
-- $Id: 9c6de018fefe684b20d7cdbe7eddc8461a41590c $

-- 注意:このファイルは EUC/LFのみ でなければならない。
-- 適用方法:
--    1.データベース接続
--    2.db2 +c -f <このファイル>
--    3.コミットするなら、db2 +c commit。やり直すなら、db2 +c rollback
--
drop   table ENTEXAM_QUESTION_ANS_DAT

create table ENTEXAM_QUESTION_ANS_DAT ( \
    ENTEXAMYEAR             varchar(4)  not null, \
    APPLICANTDIV            varchar(1)  not null, \
    SUBCLASS_CD             varchar(1)  not null, \
    SEQ                     smallint    not null, \
    EXAMNO                  varchar(10), \
    ANS1                    varchar(3), \
    ANS2                    varchar(3), \
    ANS3                    varchar(3), \
    ANS4                    varchar(3), \
    ANS5                    varchar(3), \
    ANS6                    varchar(3), \
    ANS7                    varchar(3), \
    ANS8                    varchar(3), \
    ANS9                    varchar(3), \
    ANS10                   varchar(3), \
    ANS11                   varchar(3), \
    ANS12                   varchar(3), \
    ANS13                   varchar(3), \
    ANS14                   varchar(3), \
    ANS15                   varchar(3), \
    ANS16                   varchar(3), \
    ANS17                   varchar(3), \
    ANS18                   varchar(3), \
    ANS19                   varchar(3), \
    ANS20                   varchar(3), \
    ANS21                   varchar(3), \
    ANS22                   varchar(3), \
    ANS23                   varchar(3), \
    ANS24                   varchar(3), \
    ANS25                   varchar(3), \
    ANS26                   varchar(3), \
    ANS27                   varchar(3), \
    ANS28                   varchar(3), \
    ANS29                   varchar(3), \
    ANS30                   varchar(3), \
    ANS31                   varchar(3), \
    ANS32                   varchar(3), \
    ANS33                   varchar(3), \
    ANS34                   varchar(3), \
    ANS35                   varchar(3), \
    ANS36                   varchar(3), \
    ANS37                   varchar(3), \
    ANS38                   varchar(3), \
    ANS39                   varchar(3), \
    ANS40                   varchar(3), \
    ANS41                   varchar(3), \
    ANS42                   varchar(3), \
    ANS43                   varchar(3), \
    ANS44                   varchar(3), \
    ANS45                   varchar(3), \
    ANS46                   varchar(3), \
    ANS47                   varchar(3), \
    ANS48                   varchar(3), \
    ANS49                   varchar(3), \
    ANS50                   varchar(3), \
    ANS51                   varchar(3), \
    ANS52                   varchar(3), \
    ANS53                   varchar(3), \
    ANS54                   varchar(3), \
    ANS55                   varchar(3), \
    ANS56                   varchar(3), \
    ANS57                   varchar(3), \
    ANS58                   varchar(3), \
    ANS59                   varchar(3), \
    ANS60                   varchar(3), \
    ANS61                   varchar(3), \
    ANS62                   varchar(3), \
    ANS63                   varchar(3), \
    ANS64                   varchar(3), \
    ANS65                   varchar(3), \
    ANS66                   varchar(3), \
    ANS67                   varchar(3), \
    ANS68                   varchar(3), \
    ANS69                   varchar(3), \
    ANS70                   varchar(3), \
    ANS71                   varchar(3), \
    ANS72                   varchar(3), \
    ANS73                   varchar(3), \
    ANS74                   varchar(3), \
    ANS75                   varchar(3), \
    ANS76                   varchar(3), \
    ANS77                   varchar(3), \
    ANS78                   varchar(3), \
    ANS79                   varchar(3), \
    ANS80                   varchar(3), \
    ANS81                   varchar(3), \
    ANS82                   varchar(3), \
    ANS83                   varchar(3), \
    ANS84                   varchar(3), \
    ANS85                   varchar(3), \
    ANS86                   varchar(3), \
    ANS87                   varchar(3), \
    ANS88                   varchar(3), \
    ANS89                   varchar(3), \
    ANS90                   varchar(3), \
    ANS91                   varchar(3), \
    ANS92                   varchar(3), \
    ANS93                   varchar(3), \
    ANS94                   varchar(3), \
    ANS95                   varchar(3), \
    ANS96                   varchar(3), \
    ANS97                   varchar(3), \
    ANS98                   varchar(3), \
    ANS99                   varchar(3), \
    ANS100                  varchar(3), \
    REGISTERCD              varchar(10), \
    UPDATED                 timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_QUESTION_ANS_DAT add constraint PK_ENT_QUES_ANS_D \
      primary key (ENTEXAMYEAR, APPLICANTDIV, SUBCLASS_CD, SEQ)
