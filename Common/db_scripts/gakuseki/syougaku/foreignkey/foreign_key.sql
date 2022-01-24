ALTER TABLE otheranswer_dat ADD CONSTRAINT FK_ttd_oaD \
FOREIGN KEY (studyyear, studysemester, classcd, subclasscd, studyclasscd, testkindcd, testitemcd, testbasedate, quizno)  \
REFERENCES testbase_dat(studyyear, studysemester, classcd, subclasscd, studyclasscd, testkindcd, testitemcd, testbasedate, quizno) \

ALTER TABLE testscore_hdat ADD CONSTRAINT FK_tiM_rhd \
FOREIGN KEY (subclasscd, testkindcd, testitemcd) REFERENCES testitem_mst(subclasscd, testkindcd, testitemcd) \

ALTER TABLE testitem_mst ADD CONSTRAINT FK_tkM_tim \
FOREIGN KEY (testkindcd) REFERENCES testkind_mst(testkindcd) \

ALTER TABLE groupauth_dat ADD CONSTRAINT FK_mM_gaD \
FOREIGN KEY (menuid) REFERENCES menu_mst(menuid) ON DELETE CASCADE \

ALTER TABLE userauth_dat ADD CONSTRAINT FK_mM_uaD \
FOREIGN KEY (menuid) REFERENCES menu_mst(menuid) ON DELETE CASCADE \

-- error -- \
ALTER TABLE testitem_mst ADD CONSTRAINT FK_scM_tiM \
FOREIGN KEY (subclasscd) REFERENCES subclass_mst(subclasscd) \

-- error -- \
ALTER TABLE subclassyear_dat ADD CONSTRAINT FK_scM_scyD \
FOREIGN KEY (subclasscd) REFERENCES subclass_mst(subclasscd) \

-- error -- \
ALTER TABLE bschedule_dat ADD CONSTRAINT FK_scM_bsD \ 
FOREIGN KEY (subclasscd) REFERENCES subclass_mst(subclasscd) \

ALTER TABLE schedule_dat ADD CONSTRAINT FK_scM_sD \
FOREIGN KEY (subclasscd) REFERENCES subclass_mst(subclasscd) \

ALTER TABLE attend_dat ADD CONSTRAINT FK_scM_aD \
FOREIGN KEY (subclasscd) REFERENCES subclass_mst(subclasscd) \

-- error -- \
ALTER TABLE attend_subclass_dat ADD CONSTRAINT FK_scM_ascD \
FOREIGN KEY (subclasscd) REFERENCES subclass_mst(subclasscd) \

-- error -- \
ALTER TABLE record_dat ADD CONSTRAINT FK_scM_recD \
FOREIGN KEY (subclasscd) REFERENCES subclass_mst(subclasscd) \

-- error --\
ALTER TABLE record_hdat ADD CONSTRAINT FK_scM_rechD \
FOREIGN KEY (subclasscd) REFERENCES subclass_mst(subclasscd) \

-- error --\
ALTER TABLE testscore_hdat ADD CONSTRAINT FK_scM_tshD \
FOREIGN KEY (subclasscd) REFERENCES subclass_mst(subclasscd) \

-- error --\
ALTER TABLE testscore_dat ADD CONSTRAINT FK_scM_tsD \
FOREIGN KEY (subclasscd) REFERENCES subclass_mst(subclasscd) \

ALTER TABLE electclassstaff_dat ADD CONSTRAINT FK_scM_ecsD \
FOREIGN KEY (subclasscd) REFERENCES subclass_mst(subclasscd) \

-- error --\
ALTER TABLE credit_mst ADD CONSTRAINT FK_scM_cM \
FOREIGN KEY (subclasscd) REFERENCES subclass_mst(subclasscd) \

--ALTER TABLE Ä´ºº½ñ¥Ç¡¼¥¿ ADD CONSTRAINT FK_scM_repD \
--FOREIGN KEY (subclasscd) REFERENCES subclass_mst(subclasscd) \
-- there's no such table --\

ALTER TABLE courseyear_dat ADD CONSTRAINT FK_coM_coyD \
FOREIGN KEY (coursecd) REFERENCES course_mst(coursecd) \ 

ALTER TABLE major_mst ADD CONSTRAINT FK_coM_maM \
FOREIGN KEY (coursecd) REFERENCES course_mst(coursecd) \

ALTER TABLE schreg_base_mst ADD CONSTRAINT FK_coM_srbM \
FOREIGN KEY (coursecd) REFERENCES course_mst(coursecd) \

ALTER TABLE limit_num_mst ADD CONSTRAINT FK_coM_lnM \
FOREIGN KEY (coursecd) REFERENCES course_mst(coursecd) \

ALTER TABLE majoryear_dat ADD CONSTRAINT FK_maM_mayD \
FOREIGN KEY (coursecd, majorcd) REFERENCES major_mst(coursecd, majorcd) \

ALTER TABLE schreg_base_mst ADD CONSTRAINT FK_maM_srbM \
FOREIGN KEY (coursecd, majorcd) REFERENCES major_mst(coursecd, majorcd) \

ALTER TABLE fee_mst ADD CONSTRAINT FK_maM_feeM \
FOREIGN KEY (coursecd, majorcd) REFERENCES major_mst(coursecd, majorcd) \

ALTER TABLE credit_mst ADD CONSTRAINT FK_maM_cM \
FOREIGN KEY (coursecd, majorcd) REFERENCES major_mst(coursecd, majorcd) \

ALTER TABLE limit_num_mst ADD CONSTRAINT FK_maM_lnM \
FOREIGN KEY (coursecd, majorcd) REFERENCES major_mst(coursecd, majorcd) \

-- error --\
ALTER TABLE PTAexec_dat ADD CONSTRAINT FK_srbM_peD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE cardanswer_dat ADD CONSTRAINT FK_srbM_caD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE testmark_dat ADD CONSTRAINT FK_srbM_tmD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE plague_dat ADD CONSTRAINT FK_srbM_plagD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE schreg_transfer_dat ADD CONSTRAINT FK_srbM_srtD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE schreg_envir_dat ADD CONSTRAINT FK_srbM_sreD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

-- error -- \
ALTER TABLE schreg_regd_dat ADD CONSTRAINT FK_srbM_srrD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

--ALTER TABLE ³ØÀÒ»ØÆ³ÍúÎò¥Ç¡¼¥¿ ADD CONSTRAINT FK_³ØÀÒ´ðÁÃM_³ØÀÒ»ØÆ³ÍúÎòD \
--FOREIGN KEY (³ØÀÒÈÖ¹æ) REFERENCES ³ØÀÒ´ðÁÃ¥Þ¥¹¥¿(³ØÀÒÈÖ¹æ) \
-- there's no such file -- \
 \
ALTER TABLE schreg_address_dat ADD CONSTRAINT FK_srbM_sraD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE schregremark_dat ADD CONSTRAINT FK_srM_srremD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE schreg_award_dat ADD CONSTRAINT FK_srbM_srawD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE schreg_rela_hdat ADD CONSTRAINT FK_srbM_srrhD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE club_dispatch_dat ADD CONSTRAINT FK_srbM_cldD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE club_history_dat ADD CONSTRAINT FK_srbM_clhD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE progress_dat ADD CONSTRAINT FK_srbM_proD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE healthcounsel_dat ADD CONSTRAINT FK_srbM_hcD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

-- error --\
ALTER TABLE payment_dat ADD CONSTRAINT FK_srbM_payD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

-- error --\
ALTER TABLE nonpayment_dat ADD CONSTRAINT FK_srbM_nopayD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE disease_dat ADD CONSTRAINT FK_srbM_disD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE attendclass_dat ADD CONSTRAINT FK_srbM_atcD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE jobhuntresult_dat ADD CONSTRAINT FK_srbM_jhrD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE attend_dat ADD CONSTRAINT FK_srbM_atD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE attend_subclass_dat ADD CONSTRAINT FK_srbM_atscD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE attend_month_dat ADD CONSTRAINT FK_srbM_atmD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

-- error -- \
ALTER TABLE certif_issue_dat ADD CONSTRAINT FK_srbM_ceriD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE plan_stat_dat ADD CONSTRAINT FK_srbM_psD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE plancounsel_dat ADD CONSTRAINT FK_srbM_plcoD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE planresearch_dat ADD CONSTRAINT FK_srbM_prepD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE record_dat ADD CONSTRAINT FK_srbM_recD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

--ALTER TABLE Ä´ºº½ñ¥Ç¡¼¥¿ ADD CONSTRAINT FK_srbM_repD \
--FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \
-- there's no such table --\

--ALTER TABLE Ä´ºº½ñ¥Þ¥¹¥¿ ADD CONSTRAINT FK_srbM_repM \
--FOREIGN KEY (schregno) REFERENCES schreg_base_dat(schregno) \
-- there's no such table --\

ALTER TABLE medexam_dat ADD CONSTRAINT FK_srbM_medeD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

-- error -- \
ALTER TABLE nurseoffice_dat ADD CONSTRAINT FK_srbM_noD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE healthresearch_dat ADD CONSTRAINT FK_srbM_hrepD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE trialresult_dat ADD CONSTRAINT FK_srbM_trreD \
FOREIGN KEY (schregno) REFERENCES schreg_base_mst(schregno) \

ALTER TABLE schreg_rela_dat ADD CONSTRAINT FK_srrhD_srrD \
FOREIGN KEY (schregno) REFERENCES schreg_rela_hdat(schregno) \

-- error -- \
ALTER TABLE bschedule_dat ADD CONSTRAINT FK_bschHD_bschD \
FOREIGN KEY (year, SEQ) REFERENCES bschedule_hdat(year, SEQ) \

ALTER TABLE bschedule_dat ADD CONSTRAINT FK_cM_bschD \
FOREIGN KEY (classcd) REFERENCES class_mst(classcd) \

ALTER TABLE classyear_dat ADD CONSTRAINT FK_cM_clyD \
FOREIGN KEY (classcd) REFERENCES class_mst(classcd) \

ALTER TABLE schedule_dat ADD CONSTRAINT FK_cM_schD \
FOREIGN KEY (classcd) REFERENCES class_mst(classcd) \

-- error --\
ALTER TABLE attend_dat ADD CONSTRAINT FK_cM_atD \
FOREIGN KEY (classcd) REFERENCES class_mst(classcd) \

ALTER TABLE attend_subclass_dat ADD CONSTRAINT FK_cM_ascD \
FOREIGN KEY (classcd) REFERENCES class_mst(classcd) \

ALTER TABLE record_dat ADD CONSTRAINT FK_cM_recD \
FOREIGN KEY (classcd) REFERENCES class_mst(classcd) \

ALTER TABLE record_hdat ADD CONSTRAINT FK_cM_redhD \
FOREIGN KEY (classcd) REFERENCES class_mst(classcd) \

ALTER TABLE testscore_hdat ADD CONSTRAINT FK_cM_tshD \
FOREIGN KEY (classcd) REFERENCES class_mst(classcd) \

ALTER TABLE testscore_dat ADD CONSTRAINT FK_cM_tsD \
FOREIGN KEY (classcd) REFERENCES class_mst(classcd) \

ALTER TABLE credit_mst ADD CONSTRAINT FK_cM_creM \
FOREIGN KEY (classcd) REFERENCES class_mst(classcd) \

--ALTER TABLE Ã´Åö¶µ²Ê¥Þ¥¹¥¿ ADD CONSTRAINT FK_¶µ²ÊM_Ã´Åö¶µ²ÊM \
--FOREIGN KEY (¶µ²Ê¥³¡¼¥É) REFERENCES ¶µ²Ê¥Þ¥¹¥¿(¶µ²Ê¥³¡¼¥É) \
-- there's not such file --- \
 \
--ALTER TABLE Ä´ºº½ñ¥Ç¡¼¥¿ ADD CONSTRAINT FK_cM_repD \
--FOREIGN KEY (classcd) REFERENCES class_mst(classcd) \
-- there's no such table -- \

ALTER TABLE dutyshareyear_dat ADD CONSTRAINT FK_dsM_dsyD \
FOREIGN KEY (dutysharecd) REFERENCES dutyshare_mst(dutysharecd) \

ALTER TABLE staffyear_dat ADD CONSTRAINT FK_dsM_styD \
FOREIGN KEY (dutysharecd) REFERENCES dutyshare_mst(dutysharecd) \

ALTER TABLE attendclass_dat ADD CONSTRAINT FK_atcHD_atcD \
FOREIGN KEY (year, attendclasscd) REFERENCES attendclass_hdat(year, attendclasscd) \

ALTER TABLE record_hdat ADD CONSTRAINT FK_atcHD_redHD \
FOREIGN KEY (year, attendclasscd) REFERENCES attendclass_hdat(year, attendclasscd) \

ALTER TABLE testscore_hdat ADD CONSTRAINT FK_atcHD_tsHD \
FOREIGN KEY (year, attendclasscd) REFERENCES attendclass_hdat(year, attendclasscd) \

ALTER TABLE record_dat ADD CONSTRAINT FK_atcHD_recD \
FOREIGN KEY (year, attendclasscd) REFERENCES attendclass_hdat(year, attendclasscd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clhsDJ0 \
FOREIGN KEY (r_classhourcd0) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clhsDJ1 \
FOREIGN KEY (r_classhourcd1) REFERENCES classhour_mst(hourcd) \

 
-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clhsDJ2 \
FOREIGN KEY (r_classhourcd2) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clhsDJ3 \
FOREIGN KEY (r_classhourcd3) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clhsDJ4 \
FOREIGN KEY (r_classhourcd4) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clhsDJ5 \
FOREIGN KEY (r_classhourcd5) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clhsDJ6 \
FOREIGN KEY (r_classhourcd6) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clhsDJ7 \
FOREIGN KEY (r_classhourcd7) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clshDY0 \
FOREIGN KEY (s_classhourcd0) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clshDY1 \
FOREIGN KEY (s_classhourcd1) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clshDY2 \
FOREIGN KEY (s_classhourcd2) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clshDY3 \
FOREIGN KEY (s_classhourcd3) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clshDY4 \
FOREIGN KEY (s_classhourcd4) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clshDY5 \
FOREIGN KEY (s_classhourcd5) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clshDY6 \
FOREIGN KEY (s_classhourcd6) REFERENCES classhour_mst(hourcd) \

-- error --\
ALTER TABLE classhourset_dat ADD CONSTRAINT FK_clhM_clshDY7 \
FOREIGN KEY (s_classhourcd7) REFERENCES classhour_mst(hourcd) \

ALTER TABLE processyear_dat ADD CONSTRAINT FK_pcM_pcyD \
FOREIGN KEY (processcd) REFERENCES process_mst(processcd) \

ALTER TABLE sectionyear_dat ADD CONSTRAINT FK_secM_secyD \
FOREIGN KEY (sectioncd) REFERENCES section_mst(sectioncd) \

ALTER TABLE staffyear_dat ADD CONSTRAINT FK_secM_stfyD \
FOREIGN KEY (staffsec_cd) REFERENCES section_mst(sectioncd) \

ALTER TABLE certif_year_dat ADD CONSTRAINT FK_cerM_ceryD \
FOREIGN KEY (certif_kindcd) REFERENCES certif_kind_mst(certif_kindcd) \

ALTER TABLE certif_issue_dat ADD CONSTRAINT FK_cerkM_ceriD \
FOREIGN KEY (certif_kindcd) REFERENCES certif_kind_mst(certif_kindcd) \

--ALTER TABLE ³ØÀÒ»ØÆ³ÍúÎò¥Ç¡¼¥¿ ADD CONSTRAINT FK_¿¦°÷M_³ØÀÒ»ØÆ³ÍúÎòD \
--FOREIGN KEY (¿¦°÷¥³¡¼¥É) REFERENCES ¿¦°÷¥Þ¥¹¥¿(¿¦°÷¥³¡¼¥É) \
-- there's no such table --- \

-- error --\
ALTER TABLE bschedule_dat ADD CONSTRAINT FK_stfM_bschD \
FOREIGN KEY (staffcd) REFERENCES staff_mst(staffcd) \

ALTER TABLE schedule_dat ADD CONSTRAINT FK_stfM_schD1 \
FOREIGN KEY (staffcd) REFERENCES staff_mst(staffcd) \

ALTER TABLE schedule_dat ADD CONSTRAINT FK_stfM_schD2 \
FOREIGN KEY (registrarcd) REFERENCES staff_mst(staffcd) \

ALTER TABLE staffyear_dat ADD CONSTRAINT FK_stfM_stfyD \
FOREIGN KEY (staffcd) REFERENCES staff_mst(staffcd) \

ALTER TABLE staffschedule_dat ADD CONSTRAINT FK_stfM_stfschD \
FOREIGN KEY (staffcd) REFERENCES staff_mst(staffcd) \

ALTER TABLE plancounsel_dat ADD CONSTRAINT FK_stfM_plcoD \
FOREIGN KEY (staffcd) REFERENCES staff_mst(staffcd) \

-- error --\
ALTER TABLE electclassstaff_dat ADD CONSTRAINT FK_stfM_ecsD \
FOREIGN KEY (staffcd) REFERENCES staff_mst(staffcd) \

--ALTER TABLE Ã´Åö¶µ²Ê¥Þ¥¹¥¿ ADD CONSTRAINT FK_¿¦°÷M_Ã´Åö¶µ²ÊM \
--FOREIGN KEY (¿¦°÷¥³¡¼¥É) REFERENCES ¿¦°÷¥Þ¥¹¥¿(¿¦°÷¥³¡¼¥É) \
-- there's no such file -- \

-- error --\
ALTER TABLE medexam_dat ADD CONSTRAINT FK_stfM_mexD \
FOREIGN KEY (dentistcd) REFERENCES staff_mst(staffcd) \ 

-- error --\
ALTER TABLE event_dat ADD CONSTRAINT FK_stfM_evDJ1 \
FOREIGN KEY (r_dayno1) REFERENCES staff_mst(staffcd) \

-- error --\
ALTER TABLE event_dat ADD CONSTRAINT FK_stfM_evDJ2 \
FOREIGN KEY (r_dayno2) REFERENCES staff_mst(staffcd) \

-- error --\
ALTER TABLE event_dat ADD CONSTRAINT FK_stfM_evDY1 \
FOREIGN KEY (s_dayno1) REFERENCES staff_mst(staffcd) \

-- error --\
ALTER TABLE event_dat ADD CONSTRAINT FK_stfM_evDY2 \
FOREIGN KEY (s_dayno2) REFERENCES staff_mst(staffcd) \

ALTER TABLE otheranswer_dat ADD CONSTRAINT FK_stfM_oaD \
FOREIGN KEY (staffcd) REFERENCES staff_mst(staffcd) \

-- error --\
ALTER TABLE user_mst ADD CONSTRAINT FK_stfM_usrM \
FOREIGN KEY (staffcd) REFERENCES staff_mst(staffcd) ON DELETE CASCADE \

ALTER TABLE staffyear_dat ADD CONSTRAINT FK_jbM_stfyD \
FOREIGN KEY (jobnamecd) REFERENCES job_mst(jobcd) \

ALTER TABLE jobyear_dat ADD CONSTRAINT FK_jbM_jbyD \
FOREIGN KEY (jobcd) REFERENCES job_mst(jobcd) \

ALTER TABLE plan_dat ADD CONSTRAINT FK_plsD_plD \
FOREIGN KEY (receiptyear, schregno) REFERENCES plan_stat_dat(receiptyear, schregno) \

ALTER TABLE cardanswer_dat ADD CONSTRAINT FK_tshD_canD \
FOREIGN KEY (studyyear, studysemester, classcd, subclasscd, studyclasscd, testkindcd, testitemcd) REFERENCES testscore_hdat(year, semester, classcd, subclasscd, attendclasscd, testkindcd, testitemcd) \

-- error --\
ALTER TABLE testscore_dat ADD CONSTRAINT FK_elcM_tsHD \
FOREIGN KEY (groupcd) REFERENCES electclass_mst(groupcd) \

ALTER TABLE electclassyear_dat ADD CONSTRAINT FK_elcM_clcyD \
FOREIGN KEY (groupcd) REFERENCES electclass_mst(groupcd) \

ALTER TABLE credityear_dat ADD CONSTRAINT FK_creM_creyD \
FOREIGN KEY (coursecd, majorcd, grade, course, classcd, subclasscd) REFERENCES credit_mst(coursecd, majorcd, grade, course, classcd, subclasscd)

ALTER TABLE schreg_base_mst ADD CONSTRAINT FK_jM_srbM \
FOREIGN KEY (j_cd) REFERENCES junior_mst(j_cd) \

ALTER TABLE junioryear_dat ADD CONSTRAINT FK_jM_jyD \
FOREIGN KEY (j_cd) REFERENCES junior_mst(j_cd) \

ALTER TABLE applicant_mst ADD CONSTRAINT FK_jM_appM \
FOREIGN KEY (j_cd) REFERENCES junior_mst(j_cd) \

ALTER TABLE classhourset_dat ADD CONSTRAINT FK_evD_clhsD \
FOREIGN KEY (eventyear, eventmonth, eventday) REFERENCES event_dat(eventyear, eventmonth, eventday) \

ALTER TABLE club_dispatch_dat ADD CONSTRAINT FK_clhD_cldD \
FOREIGN KEY (clubcd, dp_sdate) REFERENCES club_disphist_dat(clubcd, dp_sdate) \

ALTER TABLE club_history_dat ADD CONSTRAINT FK_clbM_clbhD \
FOREIGN KEY (clubcd) REFERENCES club_mst(clubcd) \

ALTER TABLE club_year_dat ADD CONSTRAINT FK_clbM_clbyD \
FOREIGN KEY (clubcd) REFERENCES club_mst(clubcd) \

ALTER TABLE club_disphist_dat ADD CONSTRAINT FK_clbM_clbhdD \
FOREIGN KEY (clubcd) REFERENCES club_mst(clubcd) \

-- error -- \
ALTER TABLE document_dat ADD CONSTRAINT FK_docHD_docD \
FOREIGN KEY (doc_year, doc_no, doc_branch) REFERENCES document_hdat(doc_year, doc_no, doc_branch) \

-- error -- \
ALTER TABLE document_hdat ADD CONSTRAINT FK_dockM_docHD \
FOREIGN KEY (doc_kindcd) REFERENCES doc_kind_mst(doc_kindcd) \

ALTER TABLE doc_kindyear_dat ADD CONSTRAINT FK_dockM_dockyD \
FOREIGN KEY (doc_kindcd) REFERENCES doc_kind_mst(doc_kindcd) \

ALTER TABLE nameyear_dat ADD CONSTRAINT FK_nmM_nmyD \
FOREIGN KEY (namecd1, namecd2) REFERENCES name_mst(namecd1, namecd2) \

ALTER TABLE usergroup_dat ADD CONSTRAINT FK_usrGM_usrGD \
FOREIGN KEY (groupcode) REFERENCES usergroup_mst(groupcode) ON DELETE CASCADE \

ALTER TABLE groupauth_dat ADD CONSTRAINT FK_userGM_grauD \
FOREIGN KEY (groupcode) REFERENCES usergroup_mst(groupcode) ON DELETE CASCADE \

-- error --\
ALTER TABLE userauth_dat ADD CONSTRAINT FK_usrM_usraD \
FOREIGN KEY (staffcd) REFERENCES user_mst(staffcd) ON DELETE CASCADE \



