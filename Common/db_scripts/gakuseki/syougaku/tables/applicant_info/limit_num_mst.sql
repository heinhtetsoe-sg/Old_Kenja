drop table limit_num_mst


create table limit_num_mst \
        (enteryear                   varchar(4)     not null, \
         coursecd                    varchar(1)     not null, \
		 majorcd                     varchar(3)     not null, \
		 recomm_num                  smallint, \
		 gene_num                    smallint, \
		 recom_avg_report            dec(4,1), \
		 recom_avg_ability           dec(4,1), \
         recom_dv_report             dec(4,1), \
		 recom_dv_ability            dec(4,1), \
		 gene_avg_report             dec(4,1), \
		 gene_avg_ability            dec(4,1), \
		 gene_dv_report              dec(4,1), \
		 gene_dv_ability             dec(4,1), \
		 rate                        dec(3,2), \
         updated                  timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table limit_num_mst add constraint pk_lmtnum_mst primary key \
         (enteryear, coursecd, majorcd)