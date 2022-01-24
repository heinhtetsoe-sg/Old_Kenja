
drop table medexam_det_dat

create table medexam_det_dat \
      (year                     varchar(4)      not null, \
       schregno                 varchar(8)      not null, \
       height                   decimal(4,1), \
       weight                   decimal(4,1), \
       sitheight                decimal(4,1), \
       r_barevision             varchar(1), \
       l_barevision             varchar(1), \
       r_vision                 varchar(1), \
       l_vision                 varchar(1), \
       r_ear                    varchar(2), \
       r_ear_db                 smallint, \
       l_ear                    varchar(2), \
       l_ear_db                 smallint, \
       albuminuria1cd           varchar(2), \
       uricsugar1cd             varchar(2), \
       uricbleed1cd             varchar(2), \
       albuminuria2cd           varchar(2), \
       uricsugar2cd             varchar(2), \
       uricbleed2cd             varchar(2), \
       uricothertest            varchar(30), \
       nutritioncd              varchar(2), \
       spineribcd               varchar(2), \
       eyediseasecd             varchar(2), \
       nosediseasecd            varchar(2), \
       skindiseasecd            varchar(2), \
       heart_medexam            varchar(2), \
       heartdiseasecd           varchar(2), \
       tb_date                  date, \
       tb_react                 decimal(3,1), \
       tb_result                varchar(2), \
       tb_bcgdate               date, \
       tb_filmdate              date, \
       tb_filmno                varchar(6), \
       tb_remarkcd              varchar(2), \
       tb_othertestcd           varchar(2), \
       tb_namecd                varchar(2), \
       tb_advisecd              varchar(2), \
       anemia_remark            varchar(30), \
       hemoglobin               decimal(3,1), \
       otherdiseasecd           varchar(2), \
       doc_remark               varchar(30), \
       doc_date                 date, \
       treatcd                  varchar(2), \
       remark                   varchar(30), \
       nutrition_result         varchar(60), \
       eyedisease_result        varchar(60), \
       skindisease_result       varchar(60), \
       spinerib_result          varchar(60), \
       nosedisease_result       varchar(60), \
       otherdisease_result      varchar(60), \
       heartdisease_result      varchar(60), \
       registercd               varchar(8), \
       updated                  timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table medexam_det_dat add constraint pk_medexam_det_dat primary key \
      (year,schregno)
