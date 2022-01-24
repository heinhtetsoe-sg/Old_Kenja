<?php

require_once('for_php7.php');
class knji100c_11Query extends Query
{

    //CSV
    public function selectCsvQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.SEX, ";
        $query .= "     HEIGHT, ";
        $query .= "     WEIGHT, ";
        $query .= "     SITHEIGHT, ";
        $query .= "     CHEST, ";
        $query .= "     R_BAREVISION, ";
        $query .= "     R_BAREVISION_MARK, ";
        $query .= "     L_BAREVISION, ";
        $query .= "     L_BAREVISION_MARK, ";
        $query .= "     R_VISION, ";
        $query .= "     R_VISION_MARK, ";
        $query .= "     L_VISION, ";
        $query .= "     L_VISION_MARK, ";
        $query .= "     R_EAR, ";
        $query .= "     R_EAR_DB, ";
        $query .= "     L_EAR, ";
        $query .= "     L_EAR_DB, ";
        $query .= "     ALBUMINURIA1CD, ";
        $query .= "     URICSUGAR1CD, ";
        $query .= "     URICBLEED1CD, ";
        $query .= "     MED_DD017.DET_REMARK1 AS URICPH1, ";
        $query .= "     MED_DD017.DET_REMARK2 AS URICPH2, ";
        $query .= "     ALBUMINURIA2CD, ";
        $query .= "     URICSUGAR2CD, ";
        $query .= "     URICBLEED2CD, ";
        $query .= "     URICOTHERTEST, ";
        $query .= "     URI_ADVISECD, ";
        $query .= "     NUTRITIONCD, ";
        $query .= "     SPINERIBCD, ";
        $query .= "     SPINERIBCD_REMARK, ";
        $query .= "     EYEDISEASECD, ";
        $query .= "     EYE_TEST_RESULT, ";
        $query .= "     NOSEDISEASECD, ";
        $query .= "     NOSEDISEASECD_REMARK, ";
        $query .= "     SKINDISEASECD, ";
        $query .= "     HEART_MEDEXAM, ";
        $query .= "     HEART_MEDEXAM_REMARK, ";
        $query .= "     HEARTDISEASECD, ";
        $query .= "     HEARTDISEASECD_REMARK, ";
        $query .= "     MANAGEMENT_DIV, ";
        $query .= "     MANAGEMENT_REMARK, ";
        $query .= "     TB_DATE, ";
        $query .= "     TB_REACT, ";
        $query .= "     TB_RESULT, ";
        $query .= "     TB_BCGDATE, ";
        $query .= "     TB_FILMDATE, ";
        $query .= "     TB_FILMNO, ";
        $query .= "     TB_REMARKCD, ";
        $query .= "     TB_OTHERTESTCD, ";
        $query .= "     TB_NAMECD, ";
        $query .= "     TB_ADVISECD, ";
        $query .= "     TB_X_RAY, ";
        $query .= "     ANEMIA_REMARK, ";
        $query .= "     HEMOGLOBIN, ";
        $query .= "     PARASITE, ";
        $query .= "     MED_D.OTHERDISEASECD, ";
        $query .= "     OTHER_ADVISECD, ";
        $query .= "     OTHER_REMARK, ";
        $query .= "     DOC_CD, ";
        $query .= "     DOC_REMARK, ";
        $query .= "     DOC_DATE, ";
        $query .= "     TREATCD, ";
        $query .= "     REMARK, ";
        $query .= "     NUTRITION_RESULT, ";
        $query .= "     EYEDISEASE_RESULT, ";
        $query .= "     SKINDISEASE_RESULT, ";
        $query .= "     SPINERIB_RESULT, ";
        $query .= "     NOSEDISEASE_RESULT, ";
        $query .= "     OTHERDISEASE_RESULT, ";
        $query .= "     HEARTDISEASE_RESULT, ";
        $query .= "     GUIDE_DIV, ";
        $query .= "     JOINING_SPORTS_CLUB, ";
        $query .= "     MEDICAL_HISTORY1, ";
        $query .= "     MEDICAL_HISTORY2, ";
        $query .= "     MEDICAL_HISTORY3, ";
        $query .= "     DIAGNOSIS_NAME, ";
        $query .= "     JAWS_JOINTCD, ";
        $query .= "     JAWS_JOINTCD2, ";
        $query .= "     JAWS_JOINTCD3, ";
        $query .= "     PLAQUECD, ";
        $query .= "     GUMCD, ";
        $query .= "     CALCULUS, ";
        $query .= "     ORTHODONTICS, ";
        $query .= "     UP_R_BABY5, ";
        $query .= "     UP_R_BABY4, ";
        $query .= "     UP_R_BABY3, ";
        $query .= "     UP_R_BABY2, ";
        $query .= "     UP_R_BABY1, ";
        $query .= "     UP_L_BABY1, ";
        $query .= "     UP_L_BABY2, ";
        $query .= "     UP_L_BABY3, ";
        $query .= "     UP_L_BABY4, ";
        $query .= "     UP_L_BABY5, ";
        $query .= "     LW_R_BABY5, ";
        $query .= "     LW_R_BABY4, ";
        $query .= "     LW_R_BABY3, ";
        $query .= "     LW_R_BABY2, ";
        $query .= "     LW_R_BABY1, ";
        $query .= "     LW_L_BABY1, ";
        $query .= "     LW_L_BABY2, ";
        $query .= "     LW_L_BABY3, ";
        $query .= "     LW_L_BABY4, ";
        $query .= "     LW_L_BABY5, ";
        $query .= "     BABYTOOTH, ";
        $query .= "     REMAINBABYTOOTH, ";
        $query .= "     TREATEDBABYTOOTH, ";
        $query .= "     BRACK_BABYTOOTH, ";
        $query .= "     UP_R_ADULT8, ";
        $query .= "     UP_R_ADULT7, ";
        $query .= "     UP_R_ADULT6, ";
        $query .= "     UP_R_ADULT5, ";
        $query .= "     UP_R_ADULT4, ";
        $query .= "     UP_R_ADULT3, ";
        $query .= "     UP_R_ADULT2, ";
        $query .= "     UP_R_ADULT1, ";
        $query .= "     UP_L_ADULT1, ";
        $query .= "     UP_L_ADULT2, ";
        $query .= "     UP_L_ADULT3, ";
        $query .= "     UP_L_ADULT4, ";
        $query .= "     UP_L_ADULT5, ";
        $query .= "     UP_L_ADULT6, ";
        $query .= "     UP_L_ADULT7, ";
        $query .= "     UP_L_ADULT8, ";
        $query .= "     LW_R_ADULT8, ";
        $query .= "     LW_R_ADULT7, ";
        $query .= "     LW_R_ADULT6, ";
        $query .= "     LW_R_ADULT5, ";
        $query .= "     LW_R_ADULT4, ";
        $query .= "     LW_R_ADULT3, ";
        $query .= "     LW_R_ADULT2, ";
        $query .= "     LW_R_ADULT1, ";
        $query .= "     LW_L_ADULT1, ";
        $query .= "     LW_L_ADULT2, ";
        $query .= "     LW_L_ADULT3, ";
        $query .= "     LW_L_ADULT4, ";
        $query .= "     LW_L_ADULT5, ";
        $query .= "     LW_L_ADULT6, ";
        $query .= "     LW_L_ADULT7, ";
        $query .= "     LW_L_ADULT8, ";
        $query .= "     ADULTTOOTH, ";
        $query .= "     REMAINADULTTOOTH, ";
        $query .= "     TREATEDADULTTOOTH, ";
        $query .= "     LOSTADULTTOOTH, ";
        $query .= "     BRACK_ADULTTOOTH, ";
        $query .= "     CHECKADULTTOOTH, ";
        $query .= "     MED_T.OTHERDISEASECD AS T_OTHERDISEASECD, ";
        $query .= "     OTHERDISEASE AS T_OTHERDISEASE, ";
        $query .= "     DENTISTREMARKCD, ";
        $query .= "     DENTISTREMARK, ";
        $query .= "     DENTISTREMARKDATE, ";
        $query .= "     DENTISTTREATCD, ";
        $query .= "     DENTISTTREAT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_DET_DAT MED_D ON REGD.YEAR = MED_D.YEAR ";
        $query .= "          AND REGD.SCHREGNO = MED_D.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_TOOTH_DAT MED_T ON REGD.YEAR = MED_T.YEAR ";
        $query .= "          AND REGD.SCHREGNO = MED_T.SCHREGNO ";
        $query .= "     LEFT JOIN MEDEXAM_DET_DETAIL_DAT MED_DD017 ON REGD.YEAR = MED_DD017.YEAR ";
        $query .= "          AND REGD.SCHREGNO = MED_DD017.SCHREGNO ";
        $query .= "          AND MED_DD017.DET_SEQ = '017' ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= "ORDER BY ";
        $query .= "  REGD.GRADE, ";
        $query .= "  REGD.HR_CLASS, ";
        $query .= "  REGD.ATTENDNO ";

        return $query;
    }

    //名称マスタ
    public function getMeisyou($namecd1, $namecd2)
    {
        $query  = "SELECT ";
        $query .= "    NAME1 ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1 = '".$namecd1."' ";
        $query .= "    AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //課程学科取得
    public function getCourseMajor($model)
    {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS VALUE, ";
        $query .= "     COURSENAME || MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    public function getGrade($model)
    {
        $query  = "SELECT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                      REGD_G.GRADE ";
                $query .= "                   FROM ";
                $query .= "                      SCHREG_REGD_GDAT REGD_G ";
                $query .= "                   WHERE ";
                $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                      AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND GRADE IN (SELECT ";
            $query .= "                      REGD_G.GRADE ";
            $query .= "                   FROM ";
            $query .= "                      SCHREG_REGD_GDAT REGD_G ";
            $query .= "                   WHERE ";
            $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                      AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //性別取得
    public function getSex($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'Z002' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    public function getAuth($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || '_' ||  HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象外の生徒取得
    public function getSchnoIdou($model)
    {
        $date = str_replace("/", "-", $model->date);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
                           //卒業(1)・転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    (S1.GRD_DIV IN('1','2','3') AND S1.GRD_DATE < ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }

    //リストtoリスト右
    public function getStudent_right($model)
    {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' ";
        if ($model->coursemajor) {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '{$model->coursemajor}' ";
        }
        if ($model->grade) {
            $query .= " AND T1.GRADE = '{$model->grade}' ";
        }
        if ($model->sex) {
            $query .= " AND L1.SEX = '{$model->sex}' ";
        }
        if ($model->grade_hr_class) {
            $query .= " AND T1.GRADE || '_' || T1.HR_CLASS = '{$model->grade_hr_class}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //リストtoリスト左
    public function getStudent_left($model)
    {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.GRADE    = '". $model->grade ."' AND ";
        $query .= "     T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //サブシステム名取得
    public function getSubSystem()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'I001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学校種別取得
    public function getSchoolKind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //選択フィールド取得
    public function getFieldSql($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '11' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //選択フィールド削除
    public function getDelFieldSql($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '11' ";

        return $query;
    }

    //選択フィールド保存
    public function getFieldInsSql($model, $setSeq, $val)
    {

        //入金予定項目データ
        $data = array();
        $data["YEAR"][TEXT]            = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]        = "11";
        $data["SEQ"][NUMBER]           = $setSeq;
        $data["FIELD_NAME"][TEXT]      = $val;
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "KNJI100C_KAKIDASHI_LIST");
        return $query;
    }
}
