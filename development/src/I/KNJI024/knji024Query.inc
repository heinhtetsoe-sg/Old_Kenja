<?php

require_once('for_php7.php');

class knji024Query extends Query {

    //年度取得
    function getYear()
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC";

        return $query;
    }

    //日付取得
    function getDate($year)
    {
        $query  = " SELECT SDATE,EDATE ";
        $query .= " FROM SEMESTER_MST ";
        $query .= " WHERE YEAR = '".$year."' ";
        $query .= "       AND SEMESTER = '9'";

        return $query;
    }

    //卒業生の人数
    function get_grd_student($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     GRD_DATE BETWEEN '".$model->Sdate."' AND '".$model->Edate."' ";

        return $query;
    }

    //卒業生学籍番号
    function get_grd_schno($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     GRD_DATE BETWEEN '".$model->Sdate."' AND '".$model->Edate."' ";

        return $query;
    }
    //学校区分・学年数上限・創立年度を取得
    function getSch_Information($year)
    {
        $query .= " SELECT ";
        $query .= "     schooldiv,grade_hval,integer(foundedyear) as founde ";
        $query .= " FROM ";
        $query .= "     school_mst ";
        $query .= " WHERE ";
        $query .= "     year = '".$year."' ";
        return $query;
    }

    //MAX学期取得
    function getMaxSemester($year)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SEMESTER < '9'";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $maxSemester = $db->getOne(knji024Query::getMaxSemester($model->Year));
        $db->autoCommit(false);
        //卒業生データ移行(17テーブル移行)

        //卒業生住所履歴データ
        $db->query("DELETE FROM grd_address_dat WHERE SCHREGNO in $model->schno ");
        $db->query("INSERT INTO grd_address_dat SELECT SCHREGNO,ISSUEDATE,EXPIREDATE,ZIPCD,AREACD,ADDR1,ADDR2,ADDR_FLG,ADDR1_ENG,ADDR2_ENG,TELNO,FAXNO,EMAIL,'".STAFFCD."',sysdate() FROM schreg_address_dat WHERE SCHREGNO in $model->schno ");
        //卒業生出欠記録データ
        $db->query("DELETE FROM grd_attendrec_dat WHERE SCHREGNO in $model->schno ");
        $db->query("INSERT INTO grd_attendrec_dat SELECT SCHOOLCD,YEAR,SCHREGNO,ANNUAL,SUMDATE,CLASSDAYS,OFFDAYS,ABSENT,SUSPEND,MOURNING,ABROAD,REQUIREPRESENT,SICK,ACCIDENTNOTICE,NOACCIDENTNOTICE,PRESENT,'".STAFFCD."',sysdate() FROM schreg_attendrec_dat WHERE SCHREGNO in $model->schno ");
        //卒業生保護者データ
        $db->query("DELETE FROM grd_guardian_dat WHERE SCHREGNO in $model->schno ");
        $db->query("INSERT INTO grd_guardian_dat SELECT SCHREGNO,RELATIONSHIP,GUARD_NAME,GUARD_KANA,GUARD_REAL_NAME,GUARD_REAL_KANA,GUARD_SEX,GUARD_BIRTHDAY,GUARD_ZIPCD,GUARD_ADDR1,GUARD_ADDR2,GUARD_TELNO,GUARD_FAXNO,GUARD_E_MAIL,GUARD_JOBCD,GUARD_WORK_NAME,GUARD_WORK_TELNO,GUARANTOR_RELATIONSHIP,GUARANTOR_NAME,GUARANTOR_KANA,GUARANTOR_REAL_NAME,GUARANTOR_REAL_KANA,GUARANTOR_SEX,GUARANTOR_ZIPCD,GUARANTOR_ADDR1,GUARANTOR_ADDR2,GUARANTOR_TELNO,GUARANTOR_JOBCD,PUBLIC_OFFICE,'".STAFFCD."',sysdate() FROM guardian_dat WHERE SCHREGNO in $model->schno ");
        //卒業生調査書就職用所見テーブル
        $db->query("DELETE FROM grd_hexam_empremark_dat WHERE SCHREGNO in $model->schno ");
        $db->query("INSERT INTO grd_hexam_empremark_dat SELECT SCHREGNO,JOBHUNT_REC,JOBHUNT_RECOMMEND,JOBHUNT_ABSENCE,JOBHUNT_HEALTHREMARK,'".STAFFCD."',sysdate() FROM hexam_empremark_dat WHERE SCHREGNO in $model->schno ");
        //卒業生調査書進学用所見テーブル
        $db->query("DELETE FROM grd_hexam_entremark_dat WHERE SCHREGNO in $model->schno ");
        $db->query("INSERT INTO grd_hexam_entremark_dat SELECT YEAR,SCHREGNO,ANNUAL,ATTENDREC_REMARK,SPECIALACTREC,TRAIN_REF,TRAIN_REF1,TRAIN_REF2,TRAIN_REF3,TOTALSTUDYACT,TOTALSTUDYVAL,CALSSACT,STUDENTACT,CLUBACT,SCHOOLEVENT,'".STAFFCD."',sysdate() FROM hexam_entremark_dat WHERE SCHREGNO in $model->schno ");
        //卒業生調査書進学用所見データ
        $db->query("DELETE FROM grd_hexam_entremark_hdat WHERE SCHREGNO in $model->schno ");
        $db->query("INSERT INTO grd_hexam_entremark_hdat SELECT SCHREGNO,COMMENTEX_A_CD,DISEASE,DOC_REMARK,TR_REMARK,TOTALSTUDYACT,TOTALSTUDYVAL,REMARK,'".STAFFCD."',sysdate() FROM hexam_entremark_hdat WHERE SCHREGNO in $model->schno ");
        //卒業生指導要録所見データ
        $db->query("DELETE FROM grd_htrainremark_dat WHERE SCHREGNO in $model->schno ");
        $db->query("INSERT INTO grd_htrainremark_dat SELECT YEAR,SCHREGNO,ANNUAL,TOTALSTUDYACT,TOTALSTUDYVAL,SPECIALACTREMARK,TOTALREMARK,ATTENDREC_REMARK,VIEWREMARK,BEHAVEREC_REMARK,CLASSACT,STUDENTACT,CLUBACT,SCHOOLEVENT,'".STAFFCD."',sysdate() FROM htrainremark_dat WHERE SCHREGNO in $model->schno ");
        //指導要録所見データ
        $db->query("DELETE FROM grd_htrainremark_hdat WHERE SCHREGNO in $model->schno ");
        $db->query("INSERT INTO grd_htrainremark_hdat SELECT SCHREGNO,TOTALSTUDYACT,TOTALSTUDYVAL,'".STAFFCD."',sysdate() FROM htrainremark_hdat WHERE SCHREGNO in $model->schno ");
        //卒業生健康診断履歴データ
        $db->query("DELETE FROM grd_medexam_det_dat WHERE SCHREGNO in $model->schno ");
        $db->query("INSERT INTO grd_medexam_det_dat SELECT YEAR,SCHREGNO,HEIGHT,WEIGHT,SITHEIGHT,CHEST,R_BAREVISION,R_BAREVISION_MARK,L_BAREVISION,L_BAREVISION_MARK,R_VISION,R_VISION_MARK,L_VISION,L_VISION_MARK,R_EAR,R_EAR_DB,L_EAR,L_EAR_DB,ALBUMINURIA1CD,URICSUGAR1CD,URICBLEED1CD,ALBUMINURIA2CD,URICSUGAR2CD,URICBLEED2CD,URICOTHERTEST,URI_ADVISECD,NUTRITIONCD,SPINERIBCD,SPINERIBCD_REMARK,EYEDISEASECD,EYE_TEST_RESULT,NOSEDISEASECD,NOSEDISEASECD_REMARK,SKINDISEASECD,HEART_MEDEXAM,HEART_MEDEXAM_REMARK,HEARTDISEASECD,HEARTDISEASECD_REMARK,MANAGEMENT_DIV,MANAGEMENT_REMARK,TB_DATE,TB_REACT,TB_RESULT,TB_BCGDATE,TB_FILMDATE,TB_FILMNO,TB_REMARKCD,TB_OTHERTESTCD,TB_NAMECD,TB_ADVISECD,TB_X_RAY,ANEMIA_REMARK,HEMOGLOBIN,PARASITE,OTHERDISEASECD,OTHER_ADVISECD,OTHER_REMARK,DOC_CD,DOC_REMARK,DOC_DATE,TREATCD,REMARK,NUTRITION_RESULT,EYEDISEASE_RESULT,SKINDISEASE_RESULT,SPINERIB_RESULT,NOSEDISEASE_RESULT,OTHERDISEASE_RESULT,HEARTDISEASE_RESULT,GUIDE_DIV,JOINING_SPORTS_CLUB,MEDICAL_HISTORY1,MEDICAL_HISTORY2,MEDICAL_HISTORY3,DIAGNOSIS_NAME,'".STAFFCD."',sysdate() FROM medexam_det_dat WHERE SCHREGNO in $model->schno ");
        //卒業生健康診断履歴ヘッダデータ
        $db->query("DELETE FROM grd_medexam_hdat WHERE SCHREGNO in $model->schno ");
        $db->query("INSERT INTO grd_medexam_hdat SELECT YEAR,SCHREGNO,DATE,TOOTH_DATE,'".STAFFCD."',sysdate() FROM medexam_hdat WHERE SCHREGNO in $model->schno ");
        //卒業生歯口腔
        $db->query("DELETE FROM grd_medexam_tooth_dat WHERE SCHREGNO in $model->schno ");
        $db->query("INSERT INTO grd_medexam_tooth_dat SELECT YEAR,SCHREGNO,JAWS_JOINTCD,JAWS_JOINTCD2,PLAQUECD,GUMCD,CALCULUS,ORTHODONTICS,UP_R_BABY5,UP_R_BABY4,UP_R_BABY3,UP_R_BABY2,UP_R_BABY1,UP_L_BABY1,UP_L_BABY2,UP_L_BABY3,UP_L_BABY4,UP_L_BABY5,LW_R_BABY5,LW_R_BABY4,LW_R_BABY3,LW_R_BABY2,LW_R_BABY1,LW_L_BABY1,LW_L_BABY2,LW_L_BABY3,LW_L_BABY4,LW_L_BABY5,BABYTOOTH,REMAINBABYTOOTH,TREATEDBABYTOOTH,BRACK_BABYTOOTH,UP_R_ADULT8,UP_R_ADULT7,UP_R_ADULT6,UP_R_ADULT5,UP_R_ADULT4,UP_R_ADULT3,UP_R_ADULT2,UP_R_ADULT1,UP_L_ADULT1,UP_L_ADULT2,UP_L_ADULT3,UP_L_ADULT4,UP_L_ADULT5,UP_L_ADULT6,UP_L_ADULT7,UP_L_ADULT8,LW_R_ADULT8,LW_R_ADULT7,LW_R_ADULT6,LW_R_ADULT5,LW_R_ADULT4,LW_R_ADULT3,LW_R_ADULT2,LW_R_ADULT1,LW_L_ADULT1,LW_L_ADULT2,LW_L_ADULT3,LW_L_ADULT4,LW_L_ADULT5,LW_L_ADULT6,LW_L_ADULT7,LW_L_ADULT8,ADULTTOOTH,REMAINADULTTOOTH,TREATEDADULTTOOTH,LOSTADULTTOOTH,BRACK_ADULTTOOTH,CHECKADULTTOOTH,OTHERDISEASECD,OTHERDISEASE,DENTISTREMARKCD,DENTISTREMARK,DENTISTREMARKDATE,DENTISTTREATCD,DENTISTTREAT,'".STAFFCD."',sysdate() FROM medexam_tooth_dat WHERE SCHREGNO in $model->schno ");
        //卒業生在籍データ
        $db->query("DELETE FROM grd_regd_dat WHERE SCHREGNO in $model->schno ");
        $db->query("INSERT INTO grd_regd_dat SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO,ANNUAL,SEAT_ROW,SEAT_COL,COURSECD,MAJORCD,COURSECODE,'".STAFFCD."',sysdate() FROM schreg_regd_dat WHERE SCHREGNO in $model->schno ");

        //卒業生在籍ヘッダデータ
            $query  = " DELETE FROM grd_regd_hdat ";
            $query .= "  WHERE ";
            $query .= "     exists(SELECT DISTINCT ";
            $query .= "                 year, ";
            $query .= "                 semester, ";
            $query .= "                 grade, ";
            $query .= "                 hr_class ";
            $query .= "             FROM ";
            $query .= "                 schreg_regd_dat ";
            $query .= "             WHERE ";
            $query .= "                 SCHREGNO in $model->schno ";
            $query .= "             GROUP BY ";
            $query .= "                 year, ";
            $query .= "                 semester, ";
            $query .= "                 grade, ";
            $query .= "                 hr_class ";
            $query .= "             ) ";
            $query .= "    AND year = '".$model->Year."' ";
        $db->query($query);

            $query  = " INSERT INTO grd_regd_hdat ";
            $query .= " SELECT ";
            $query .= "     YEAR,SEMESTER,GRADE,HR_CLASS,HR_NAME,HR_NAMEABBV,GRADE_NAME,HR_CLASS_NAME1,HR_CLASS_NAME2,HR_FACCD,TR_CD1,TR_CD2,TR_CD3,SUBTR_CD1,SUBTR_CD2,SUBTR_CD3,CLASSWEEKS,CLASSDAYS,'".STAFFCD."',sysdate() ";
            $query .= "   FROM ";
            $query .= "     schreg_regd_hdat ";
            $query .= "  WHERE ";
            $query .= "     exists(SELECT DISTINCT ";
            $query .= "                 year, ";
            $query .= "                 semester, ";
            $query .= "                 grade, ";
            $query .= "                 hr_class ";
            $query .= "             FROM ";
            $query .= "                 schreg_regd_dat ";
            $query .= "             WHERE ";
            $query .= "                 SCHREGNO in $model->schno ";
            $query .= "             GROUP BY ";
            $query .= "                 year, ";
            $query .= "                 semester, ";
            $query .= "                 grade, ";
            $query .= "                 hr_class ";
            $query .= "             ) ";
            $query .= "    AND year = '".$model->Year."' ";
        $db->query($query);

        //卒業生学習記録備考データ
        $db->query("DELETE FROM grd_studyrecremark_dat WHERE SCHREGNO in $model->schno ");
        if ($model->Properties["useCurriculumcd"] == '1') {
            $db->query("INSERT INTO grd_studyrecremark_dat SELECT YEAR,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,REMARK,'".STAFFCD."',sysdate() FROM studyrecremark_dat WHERE SCHREGNO in $model->schno ");
        } else {
            $db->query("INSERT INTO grd_studyrecremark_dat SELECT YEAR,SCHREGNO,CLASSCD,SUBCLASSCD,REMARK,'".STAFFCD."',sysdate() FROM studyrecremark_dat WHERE SCHREGNO in $model->schno ");
        }
        //卒業生学習記録データ
        $db->query("DELETE FROM grd_studyrec_dat WHERE SCHREGNO in $model->schno ");
        if ($model->Properties["useCurriculumcd"] == '1') {
            $db->query("INSERT INTO grd_studyrec_dat SELECT SCHOOLCD,YEAR,SCHREGNO,ANNUAL,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,CLASSNAME,CLASSABBV,CLASSNAME_ENG,CLASSABBV_ENG,SUBCLASSES,SUBCLASSNAME,SUBCLASSABBV,SUBCLASSNAME_ENG,SUBCLASSABBV_ENG,VALUATION,GET_CREDIT,ADD_CREDIT,COMP_CREDIT,PRINT_FLG,'".STAFFCD."',sysdate() FROM schreg_studyrec_dat WHERE SCHREGNO in $model->schno ");
        } else {
            $db->query("INSERT INTO grd_studyrec_dat SELECT SCHOOLCD,YEAR,SCHREGNO,ANNUAL,CLASSCD,SUBCLASSCD,CLASSNAME,CLASSABBV,CLASSNAME_ENG,CLASSABBV_ENG,SUBCLASSES,SUBCLASSNAME,SUBCLASSABBV,SUBCLASSNAME_ENG,SUBCLASSABBV_ENG,VALUATION,GET_CREDIT,ADD_CREDIT,COMP_CREDIT,PRINT_FLG,'".STAFFCD."',sysdate() FROM schreg_studyrec_dat WHERE SCHREGNO in $model->schno ");
        }
        //卒業生異動データ
        $db->query("DELETE FROM grd_transfer_dat WHERE SCHREGNO in $model->schno ");
        $db->query("INSERT INTO grd_transfer_dat SELECT SCHREGNO,TRANSFERCD,TRANSFER_SDATE,TRANSFER_EDATE,TRANSFERREASON,TRANSFERPLACE,TRANSFERADDR,ABROAD_CLASSDAYS,ABROAD_CREDITS,'".STAFFCD."',sysdate() FROM schreg_transfer_dat WHERE SCHREGNO in $model->schno ");

        //卒業生基礎データ
        $query  = "DELETE FROM GRD_BASE_MST ";
        $query .= "WHERE  SCHREGNO IN $model->schno ";

        $db->query($query);

        $query  = "INSERT INTO grd_base_mst ";
        $query .= "SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.INOUTCD, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.REAL_NAME, ";
        $query .= "     T1.REAL_NAME_KANA, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BLOODTYPE, ";
        $query .= "     T1.BLOOD_RH, ";
        $query .= "     T1.HANDICAP, ";
        $query .= "     T1.NATIONALITY, ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     T1.finish_date, ";
        $query .= "     T1.PRISCHOOLCD, ";
        $query .= "     T1.ent_date, ";
        $query .= "     T1.ent_div, ";
        $query .= "     T1.ENT_REASON, ";
        $query .= "     T1.ENT_SCHOOL, ";
        $query .= "     T1.ENT_ADDR, ";
        $query .= "     T1.ENT_ADDR2, ";
        $query .= "     T1.GRD_DATE, ";
        $query .= "     T1.GRD_DIV, ";
        $query .= "     T1.GRD_REASON, ";
        $query .= "     T1.GRD_SCHOOL, ";
        $query .= "     T1.GRD_ADDR, ";
        $query .= "     T1.GRD_ADDR2, ";
        $query .= "     T1.GRD_NO, ";
        $query .= "     '".$model->term."', ";
        $query .= "     '".$maxSemester."', ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     T3.ATTENDNO, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.REMARK3, ";
        $query .= "     T1.EMERGENCYCALL, ";
        $query .= "     T1.EMERGENCYNAME, ";
        $query .= "     T1.EMERGENCYRELA_NAME, ";
        $query .= "     T1.EMERGENCYTELNO, ";
        $query .= "     T1.EMERGENCYCALL2, ";
        $query .= "     T1.EMERGENCYNAME2, ";
        $query .= "     T1.EMERGENCYRELA_NAME2, ";
        $query .= "     T1.EMERGENCYTELNO2, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.AREACD, ";
        $query .= "     T2.ADDR1, ";
        $query .= "     T2.ADDR2, ";
        $query .= "     T2.ADDR_FLG, ";
        $query .= "     T2.ADDR1_ENG, ";
        $query .= "     T2.ADDR2_ENG, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T2.FAXNO, ";
        $query .= "     T2.EMAIL, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.AREACD, ";
        $query .= "     T2.ADDR1, ";
        $query .= "     T2.ADDR2, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T2.FAXNO, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= "  FROM ";
        $query .= "     schreg_base_mst T1 ";
        $query .= "         LEFT JOIN (SELECT * FROM schreg_address_dat ";
        $query .= "                     WHERE (schregno,issuedate) ";
        $query .= "                        IN (select schregno,max(issuedate) from schreg_address_dat group by schregno)) T2 ";
        $query .= "           ON T1.schregno = T2.schregno ";
        $query .= "         LEFT JOIN (SELECT grade, hr_class, attendno, schregno ";
        $query .= "                      FROM schreg_regd_dat ";
        $query .= "                     WHERE year      = '".$model->Year."' ";
        $query .= "                       AND semester  = '".$maxSemester."') T3 ";
        $query .= "           ON T1.schregno = T3.schregno ";
        $query .= " WHERE T1.SCHREGNO IN $model->schno ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

}
?>
