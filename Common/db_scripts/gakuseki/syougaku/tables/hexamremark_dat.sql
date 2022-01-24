
drop table hexamremark_dat

create table hexamremark_dat \
    ( \
     schregno                   varchar(6)  not null, \
     commentex_A_cd             varchar(1), \
     attendrec_remarkgrade1     varchar(46), \
     attendrec_remarkgrade2     varchar(46), \
     attendrec_remarkgrade3     varchar(46), \
     disease                    varchar(178), \
     doc_remark                 varchar(62), \
     tr_remark                  varchar(110), \
     specialactrec_grade1       varchar(222), \
     specialactrec_grade2       varchar(222), \
     specialactrec_grade3       varchar(222), \
     train_ref_grade1           varchar(450), \
     train_ref_grade2           varchar(450), \
     train_ref_grade3           varchar(450), \
     allactstudyremark          varchar(154), \
     allactstudyvalue           varchar(232), \
     remark                     varchar(388), \
     jobhunt_rec                varchar(334), \
     jobhunt_recommend          varchar(858), \
     jobhunt_absence            varchar(86), \
     jobhunt_healthremark       varchar(88), \
     jobhunt_allactstudyvalue   varchar(400), \
     jobhunt_allactstudyremark  varchar(400), \
     updated                    timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table hexamremark_dat add constraint pk_hexam_dat primary key (schregno) 

