insert into medexam_det_dat \
( \
	medexamyear, \
	schregno, \
	height	, \
	weight, \
	sitheight, \
	r_barevision, \
	l_barevision, \
	r_vision, \
	l_vision, \
	r_ear	, \
	r_ear_db, \
	l_ear	, \
	l_ear_db, \
	albuminuria1cd, \
	uricsugar1cd, \
	uricbleed1cd, \
	albuminuria2cd, \
	uricsugar2cd, \
	uricbleed2cd, \
	 uricothertest, \
	 nutritioncd, \
	 spineribcd, \
	 eyediseasecd, \
	 nosediseasecd, \
	 skindiseasecd, \
	 heart_medexam, \
	 heartdiseasecd,	\
	 tb_date, \
	 tb_react, \
	 tb_result, \
	 tb_bcgdate, \
	 tb_filmdate, \
	 tb_filmno, \
	 tb_remarkcd, \
	 tb_othertestcd	, \
	 tb_namecd, \
	 tb_advisecd, \
	 anemia_remark, \
	 hemoglobin, \
	 otherdiseasecd	, \
	 doc_remark, \
	 doc_date, \
	 treatcd, \
	 remark, \
	 updated \
) values ( \
select \
	medexamyear, \
	schregno, \
	height	, \
	weight, \
	sitheight, \
	r_barevision, \
	l_barevision, \
	r_vision, \
	l_vision, \
	r_ear	, \
	r_ear_db, \
	l_ear	, \
	l_ear_db, \
	albuminuria1cd, \
	uricsugar1cd, \
	uricbleed1cd, \
	albuminuria2cd, \
	uricsugar2cd, \
	uricbleed2cd, \
	 uricothertest, \
	 nutritioncd, \
	 spineribcd, \
	 eyediseasecd, \
	 nosediseasecd, \
	 skindiseasecd, \
	 heart_medexam, \
	 heartdiseasecd,	\
	 tb_date, \
	 tb_react, \
	 tb_result, \
	 tb_bcgdate, \
	 tb_filmdate, \
	 tb_filmno, \
	 tb_remarkcd, \
	 tb_othertestcd	, \
	 tb_namecd, \
	 tb_advisecd, \
	 anemia_remark, \
	 hemoglobin, \
	 otherdiseasecd	, \
	 doc_remark, \
	 doc_date, \
	 treatcd, \
	 remark, \
	 updated \
 from medexam_det_dat_old )
