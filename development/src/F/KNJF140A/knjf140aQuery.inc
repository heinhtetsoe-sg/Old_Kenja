<?php

require_once('for_php7.php');
class knjf140aquery extends Query
{
    //学校種別の取得
    public function getNameMstA023($model, $year = "")
    {
        $table = ($year) ? "V_NAME_MST" : "NAME_MST";

        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($year) {
            $query .= " AND YEAR = '".$year."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //最小学年（校種毎）
    public function getMinGrade($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     NAME2 AS MIN_GRADE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= "     AND NAME1   = '".$model->field["SCHOOL_KIND"]."' ";

        $minGrade = $db->getOne($query);
        Query::dbCheckIn($db);
        return $minGrade;
    }

    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJF140A' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //学校名取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";
        
        return $query;
    }

    //年度一覧
    public function getSelectFieldSQL($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SEMESTER_MST T2 ON  T1.YEAR     = T2.YEAR ";
        $query .= "                     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.GRADE IN (SELECT ";
        $query .= "                     G1.GRADE ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_GDAT G1 ";
        $query .= "                 WHERE ";
        $query .= "                     G1.YEAR         = T1.YEAR AND ";
        $query .= "                     G1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '{$model->field["YEAR"]}' ";
        $query .= " AND GRADE IN (  SELECT ";
        $query .= "                     G1.GRADE ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_GDAT G1 ";
        $query .= "                 WHERE ";
        $query .= "                     G1.YEAR         = T1.YEAR AND ";
        $query .= "                     G1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getNameMstDef($model, $namecd1, $flg)
    {
        $query = "SELECT NAMECD2,NAME1 FROM NAME_MST WHERE NAMECD1 = '{$namecd1}'";
        if ($flg === '1') {
            $query .= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        }
        return $query;
    }

    public function isKindai()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";
        $query .= "     AND NAME1 IN ('KINDAI', 'KINJUNIOR') ";
        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        if ($model->field["CSVCD"] == "1") { //一般
            $query  = " SELECT ";
            $query .= "     '1' AS CSVCD, ";
            $query .= "     T3.YEAR, ";
            $query .= "     T2.DATE, ";
            $query .= "     T3.SCHREGNO, ";
            $query .= "     T3.GRADE, ";
            $query .= "     T3.HR_CLASS, ";
            $query .= "     T3.ATTENDNO, ";
            $query .= "     B1.NAME, ";
            $query .= "     HEIGHT, ";
            $query .= "     WEIGHT, ";
            $query .= "     SITHEIGHT, ";
            $query .= "     NO_PRINT_OBESITY_INDEX, ";
            $query .= "     R_VISION_CANTMEASURE, ";
            $query .= "     L_VISION_CANTMEASURE, ";
            $query .= "     R_BAREVISION, ";
            $query .= "     R_BAREVISION_MARK, ";
            $query .= "     L_BAREVISION, ";
            $query .= "     L_BAREVISION_MARK, ";
            $query .= "     R_VISION, ";
            $query .= "     R_VISION_MARK, ";
            $query .= "     L_VISION, ";
            $query .= "     L_VISION_MARK, ";
            $query .= "     R_EAR_CANTMEASURE, ";
            $query .= "     R_EAR_DB, ";
            $query .= "     R_EAR_DB_1000, ";
            $query .= "     R_EAR_DB_4000, ";
            $query .= "     R_EAR, ";
            $query .= "     R_EAR_DB_IN, ";
            $query .= "     R_EAR_DB_4000_IN, ";
            $query .= "     R_EAR_IN, ";
            $query .= "     L_EAR_CANTMEASURE, ";
            $query .= "     L_EAR_DB, ";
            $query .= "     L_EAR_DB_1000, ";
            $query .= "     L_EAR_DB_4000, ";
            $query .= "     L_EAR, ";
            $query .= "     L_EAR_DB_IN, ";
            $query .= "     L_EAR_DB_4000_IN, ";
            $query .= "     L_EAR_IN, ";
            $query .= "     ALBUMINURIA1CD, ";
            $query .= "     URICSUGAR1CD, ";
            $query .= "     URICBLEED1CD, ";
            $query .= "     URICPH1, ";
            $query .= "     ALBUMINURIA2CD, ";
            $query .= "     URICSUGAR2CD, ";
            $query .= "     URICBLEED2CD, ";
            $query .= "     URICPH2, ";
            $query .= "     URICOTHERTESTCD, ";
            $query .= "     URICOTHERTEST, ";
            $query .= "     URI_ADVISECD, ";
            $query .= "     NUTRITIONCD, ";
            $query .= "     NUTRITIONCD_REMARK, ";
            $query .= "     SPINERIBCD, ";
            $query .= "     SPINERIBCD_REMARK, ";
            $query .= "     EYEDISEASECD, ";
            $query .= "     EYEDISEASECD2, ";
            $query .= "     EYEDISEASECD3, ";
            $query .= "     EYEDISEASECD4, ";
            $query .= "     EYE_TEST_RESULT, ";
            $query .= "     EYE_TEST_RESULT2, ";
            $query .= "     EYE_TEST_RESULT3, ";
            $query .= "     EYEDISEASECD5, ";
            $query .= "     NOSEDISEASECD, ";
            $query .= "     NOSEDISEASECD2, ";
            $query .= "     NOSEDISEASECD3, ";
            $query .= "     NOSEDISEASECD4, ";
            $query .= "     NOSEDISEASECD5, ";
            $query .= "     NOSEDISEASECD6, ";
            $query .= "     NOSEDISEASECD7, ";
            $query .= "     NOSEDISEASECD_REMARK, ";
            $query .= "     NOSEDISEASECD_REMARK1, ";
            $query .= "     NOSEDISEASECD_REMARK2, ";
            $query .= "     NOSEDISEASECD_REMARK3, ";
            $query .= "     SKINDISEASECD, ";
            $query .= "     SKINDISEASECD_REMARK, ";
            $query .= "     TB_FILMDATE, ";
            $query .= "     TB_FILMNO, ";
            $query .= "     TB_REMARKCD, ";
            $query .= "     TB_X_RAY, ";
            $query .= "     TB_OTHERTESTCD, ";
            $query .= "     TB_OTHERTEST_REMARK1, ";
            $query .= "     TB_NAMECD, ";
            $query .= "     TB_NAME_REMARK1, ";
            $query .= "     TB_ADVISECD, ";
            $query .= "     TB_ADVISE_REMARK1, ";
            $query .= "     HEART_MEDEXAM, ";
            $query .= "     HEART_MEDEXAM_REMARK, ";
            $query .= "     HEART_GRAPH_NO, ";
            $query .= "     HEARTDISEASECD, ";
            $query .= "     HEARTDISEASECD_REMARK, ";
            $query .= "     MANAGEMENT_DIV, ";
            $query .= "     MANAGEMENT_REMARK, ";
            $query .= "     PARASITE, ";
            $query .= "     OTHERDISEASECD, ";
            $query .= "     OTHER_ADVISECD, ";
            $query .= "     OTHER_REMARK, ";
            $query .= "     OTHER_REMARK2, ";
            $query .= "     OTHER_REMARK3, ";
            $query .= "     ANEMIA_REMARK, ";
            $query .= "     HEMOGLOBIN, ";
            $query .= "     DOC_DATE, ";
            $query .= "     DOC_CD,";
            $query .= "     DOC_REMARK, ";
            $query .= "     TREATCD, ";
            $query .= "     TREATCD2, ";
            $query .= "     TREAT_REMARK1, ";
            $query .= "     TREAT_REMARK2, ";
            $query .= "     TREAT_REMARK3, ";
            $query .= "     REMARK, ";
            $query .= "     MESSAGE, ";
            $query .= "     MEDICAL_HISTORY1, ";
            $query .= "     MEDICAL_HISTORY2, ";
            $query .= "     MEDICAL_HISTORY3, ";
            $query .= "     DIAGNOSIS_NAME, ";
            $query .= "     GUIDE_DIV, ";
            $query .= "     JOINING_SPORTS_CLUB, ";
            $query .= "     VISION_CANTMEASURE, ";
            $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
            $query .= " FROM SCHREG_REGD_DAT T3 ";
            $query .= " LEFT JOIN MEDEXAM_HDAT T2 ON T3.YEAR     = T2.YEAR ";
            $query .= "                          AND T3.SCHREGNO = T2.SCHREGNO ";
            $query .= " LEFT JOIN V_MEDEXAM_DET_DAT T1 ON T1.YEAR     = T2.YEAR ";
            $query .= "                               AND T1.SCHREGNO = T2.SCHREGNO ";
        } else { //歯・口腔
            $query  = " SELECT ";
            $query .= "     '2' AS CSVCD, ";
            $query .= "     T3.YEAR, ";
            $query .= "     T2.TOOTH_DATE, ";
            $query .= "     T3.SCHREGNO, ";
            $query .= "     T3.GRADE, ";
            $query .= "     T3.HR_CLASS, ";
            $query .= "     T3.ATTENDNO, ";
            $query .= "     B1.NAME, ";
            $query .= "     JAWS_JOINTCD, ";
            $query .= "     JAWS_JOINTCD3, ";
            $query .= "     JAWS_JOINTCD2, ";
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
            $query .= "     OTHERDISEASECD, ";
            $query .= "     OTHERDISEASE, ";
            $query .= "     OTHERDISEASECD3, ";
            if ($model->is_f020_otherdisese_hyouji2) {
                $query .= "     DT003.TOOTH_REMARK5 AS OTHERDISEASE3, ";
            }
            $query .= "     OTHERDISEASECD4, ";
            if ($model->is_f020_otherdisese_hyouji2) {
                $query .= "     DT003.TOOTH_REMARK6 AS OTHERDISEASE4, ";
            }
            $query .= "     OTHERDISEASECD2, ";
            $query .= "     OTHERDISEASE2, ";
            if ($model->is_f020_otherdisese_hyouji) {
                $query .= "     DT002.TOOTH_REMARK3 AS OTHERDISEASE_REMARK1, ";
                $query .= "     DT002.TOOTH_REMARK4 AS OTHERDISEASE_REMARK2, ";
                $query .= "     DT002.TOOTH_REMARK5 AS OTHERDISEASE_REMARK3, ";
                $query .= "     DT002.TOOTH_REMARK6 AS OTHERDISEASE_REMARK4, ";
            }
            $query .= "     DENTISTREMARKCD, ";
            $query .= "     DENTISTREMARK, ";
            if ($model->is_f020_dentistremark_hyouji) {
                $query .= "     DT005.TOOTH_REMARK1 AS DENTISTREMARK_REMARK1, ";
                $query .= "     DT005.TOOTH_REMARK2 AS DENTISTREMARK_REMARK2, ";
                $query .= "     DT005.TOOTH_REMARK3 AS DENTISTREMARK_REMARK3, ";
                $query .= "     DT005.TOOTH_REMARK4 AS DENTISTREMARK_REMARK4, ";
            }
            $query .= "     DENTISTREMARK_CO, ";
            $query .= "     DENTISTREMARK_GO, ";
            $query .= "     DENTISTREMARK_G, ";
            $query .= "     DENTISTREMARKDATE, ";
            $query .= "     DENTISTTREATCD, ";
            $query .= "     DENTISTTREAT, ";
            $query .= "     DENTISTTREAT2, ";
            $query .= "     DENTISTTREAT3, ";
            $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
            $query .= " FROM SCHREG_REGD_DAT T3 ";
            $query .= " LEFT JOIN MEDEXAM_HDAT T2 ON T3.YEAR     = T2.YEAR ";
            $query .= "                          AND T3.SCHREGNO = T2.SCHREGNO ";
            $query .= " LEFT JOIN V_MEDEXAM_TOOTH_DAT T1 ON T1.YEAR     = T2.YEAR ";
            $query .= "                                 AND T1.SCHREGNO = T2.SCHREGNO ";
            if ($model->is_f020_otherdisese_hyouji) {
                $query .= "    LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT002 ";
                $query .= "           ON DT002.YEAR      = T1.YEAR ";
                $query .= "          AND DT002.SCHREGNO  = T1.SCHREGNO ";
                $query .= "          AND DT002.TOOTH_SEQ = '002' ";
            }
            if ($model->is_f020_otherdisese_hyouji2) {
                $query .= "    LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT003 ";
                $query .= "           ON DT003.YEAR      = T1.YEAR ";
                $query .= "          AND DT003.SCHREGNO  = T1.SCHREGNO ";
                $query .= "          AND DT003.TOOTH_SEQ = '003' ";
            }
            if ($model->is_f020_dentistremark_hyouji) {
                $query .= "    LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT005 ";
                $query .= "           ON DT005.YEAR      = T1.YEAR ";
                $query .= "          AND DT005.SCHREGNO  = T1.SCHREGNO ";
                $query .= "          AND DT005.TOOTH_SEQ = '005' ";
            }
        }
        $query .= " LEFT JOIN SCHREG_BASE_MST B1 ON T3.SCHREGNO = B1.SCHREGNO ";
        $query .= " WHERE T3.YEAR || T3.SEMESTER = '{$model->field["YEAR"]}' ";
        if ($model->field["GRADE_HR_CLASS"]!="") {
            $query .= "AND T3.GRADE || T3.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        }
        $query .= " AND T3.GRADE IN (SELECT ";
        $query .= "                     G1.GRADE ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_GDAT G1 ";
        $query .= "                 WHERE ";
        $query .= "                     G1.YEAR         = T3.YEAR AND ";
        $query .= "                     G1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "                 ) ";
        if ($model->Properties["useLicenseMedexam"] == "1") {
            // LICENSE_GROUP_STD_YDATに登録されている生徒は出力しない
            $query .= " AND NOT EXISTS (SELECT ";
            $query .= "                     YDAT.SCHREGNO ";
            $query .= "                 FROM ";
            $query .= "                     LICENSE_GROUP_STD_YDAT YDAT ";
            $query .= "                 WHERE ";
            $query .= "                         YDAT.YEAR       = T3.YEAR ";
            $query .= "                     AND YDAT.GROUP_DIV  = '00' ";
            $query .= "                     AND YDAT.SELECT_DIV = '0' ";
            $query .= "                     AND YDAT.SCHREGNO   = T3.SCHREGNO ";
            $query .= "                     AND YDAT.REMARK1    = '1' ";
            $query .= "                 ) ";
        }
        $query .= "ORDER BY T3.SCHREGNO";

        return $query;
    }


    //視力も文字取得
    public function getSiryokuMark($suuji)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'F011' AND ";
        $query .= "     NAMESPARE1 <= '$suuji' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";
        $query .= "         DESC ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //視力範囲チェック用
    public function getVisionMark($model)
    {
        $year = substr($model->field["YEAR"], 0, 4);

        $query  = " SELECT ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMECD2, ";
        $query .= "     ABBV3, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        if ($model->z010name1 == "kumamoto") {
            $query .= "     NAMECD1 = 'F011' ";
        } else {
            $query .= "     NAMECD1 = 'F017' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 DESC ";

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck($model, $data, $chk_no, $namecd1 = "", $namecd2 = "")
    {
        $flg   = true;
        $query = "";
        //条件選択
        switch ($chk_no) {
            case "1":
                $where = " NAME_MST WHERE NAMECD1 = '{$namecd1}' AND NAMECD2 = '{$namecd2}' ";
                break;
            case "2":
                $where  = "     SCHREG_REGD_DAT T1 ";
                $where .= " WHERE ";
                $where .= "      SCHREGNO = '{$data["SCHREGNO"]}' ";
                $where .= " AND YEAR   = '{$data["YEAR"]}' ";
                $where .= " AND GRADE IN (  SELECT ";
                $where .= "                     G1.GRADE ";
                $where .= "                 FROM ";
                $where .= "                     SCHREG_REGD_GDAT G1 ";
                $where .= "                 WHERE ";
                $where .= "                     G1.YEAR         = T1.YEAR AND ";
                $where .= "                     G1.SCHOOL_KIND  = '". $model->field["SCHOOL_KIND"]."' ";
                $where .= "                 ) ";
                break;
            default:
                $flg = false;
                break;
        }
        if ($flg) {
            $query = "SELECT COUNT(*) FROM {$where}";
        }

        return $query;
    }

    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data, $csvcd)
    {
        if ($csvcd == "1") {
            $table_del = "MEDEXAM_DET_DAT";
            $fieldName = "DATE";
            $query  = " DELETE FROM ";
            $query .= "     MEDEXAM_DET_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$data["YEAR"]}' ";
            $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
            $query .= " AND DET_SEQ <> '010' ";
            $db->query($query);
        } else {
            $table_del = "MEDEXAM_TOOTH_DAT";
            $fieldName = "TOOTH_DATE";
            $query  = " DELETE FROM ";
            $query .= "     MEDEXAM_TOOTH_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$data["YEAR"]}' ";
            $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
            $db->query($query);
        }
        $table_exists = ($csvcd == "1") ? "MEDEXAM_TOOTH_DAT" : "MEDEXAM_DET_DAT";

        //一般または歯・口腔の削除
        $query  = " DELETE FROM ";
        $query .= "     {$table_del} ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$data["YEAR"]}' ";
        $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
        $db->query($query);

        //MEDEXAM_HDATの削除
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$table_exists} ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$data["YEAR"]}' ";
        $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
        if ($db->getOne($query) == 0) {
            $query  = " DELETE FROM ";
            $query .= "     MEDEXAM_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$data["YEAR"]}' ";
            $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
            $db->query($query);
        } else {
            $data2["YEAR"][TEXT]       = $data["YEAR"];
            $data2["SCHREGNO"][TEXT]   = $data["SCHREGNO"];
            $data2[$fieldName][TEXT]   = "";
            $data2["REGISTERCD"][TEXT] = STAFFCD;
            $data2["UPDATED"][NUMBER]  = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '{$data["YEAR"]}' ";
            $where .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";

            $query = Query::updateSQL($data2, "MEDEXAM_HDAT", $where);
            $db->query($query);
        }
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function deleteinsertQueryCsv(&$db, $data, $csvcd, $model)
    {
        $table = ($csvcd == "1") ? "MEDEXAM_DET_DAT" : "MEDEXAM_TOOTH_DAT";

        if ($csvcd == "2") { //歯・口腔
            //データ取得
            $query  = " SELECT * ";
            $query .= " FROM ";
            $query .= "     {$table} ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$data["YEAR"]}' ";
            $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
            $toothData = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        //一般または歯・口腔の削除
        $query  = " DELETE FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$data["YEAR"]}' ";
        $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
        $db->query($query);

        if ($csvcd == "1") { //一般
            $data1["YEAR"][TEXT]                 = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]             = $data["SCHREGNO"];
            $data1["HEIGHT"][NUMBER]             = $data["HEIGHT"];
            $data1["WEIGHT"][NUMBER]             = $data["WEIGHT"];
            $data1["SITHEIGHT"][NUMBER]          = $data["SITHEIGHT"];
            $data1["R_BAREVISION"][TEXT]         = $data["R_BAREVISION"];
            $data1["R_BAREVISION_MARK"][TEXT]    = $data["R_BAREVISION_MARK"];
            $data1["L_BAREVISION"][TEXT]         = $data["L_BAREVISION"];
            $data1["L_BAREVISION_MARK"][TEXT]    = $data["L_BAREVISION_MARK"];
            $data1["R_VISION"][TEXT]             = $data["R_VISION"];
            $data1["R_VISION_MARK"][TEXT]        = $data["R_VISION_MARK"];
            $data1["L_VISION"][TEXT]             = $data["L_VISION"];
            $data1["L_VISION_MARK"][TEXT]        = $data["L_VISION_MARK"];
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F010"));
            $data1["R_EAR"][TEXT]                = $data["R_EAR"] ? $data["R_EAR"] : $syokiti;
            $data1["R_EAR_DB"][NUMBER]           = $data["R_EAR_DB"];
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F010"));
            $data1["L_EAR"][TEXT]                = $data["L_EAR"] ? $data["L_EAR"] : $syokiti;
            $data1["L_EAR_DB"][NUMBER]           = $data["L_EAR_DB"];
            $syokiti020 = $db->getOne(knjf140aQuery::getSyokiti($model, "F020"));
            $syokiti019 = $db->getOne(knjf140aQuery::getSyokiti($model, "F019"));
            $syokiti018 = $db->getOne(knjf140aQuery::getSyokiti($model, "F018"));
            $data1["ALBUMINURIA1CD"][TEXT]       = $data["ALBUMINURIA1CD"] ? $data["ALBUMINURIA1CD"] : $syokiti020;
            $data1["URICSUGAR1CD"][TEXT]         = $data["URICSUGAR1CD"] ? $data["URICSUGAR1CD"] : $syokiti019;
            $data1["URICBLEED1CD"][TEXT]         = $data["URICBLEED1CD"] ? $data["URICBLEED1CD"] : $syokiti018;
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                if ($data["ALBUMINURIA2CD"] == '' && $data["URICSUGAR2CD"] == '' && $data["URICBLEED2CD"] == '') {
                    $syokiti020_98 = $db->getOne(knjf140aQuery::getSyokiti98($model, "F020"));
                    $syokiti019_98 = $db->getOne(knjf140aQuery::getSyokiti98($model, "F019"));
                    $syokiti018_98 = $db->getOne(knjf140aQuery::getSyokiti98($model, "F018"));
                    $data["ALBUMINURIA2CD"]       = $syokiti020_98;
                    $data["URICSUGAR2CD"]         = $syokiti019_98;
                    $data["URICBLEED2CD"]         = $syokiti018_98;
                }
            }
            $data1["ALBUMINURIA2CD"][TEXT]       = $data["ALBUMINURIA2CD"] ? $data["ALBUMINURIA2CD"] : $syokiti020;
            $data1["URICSUGAR2CD"][TEXT]         = $data["URICSUGAR2CD"] ? $data["URICSUGAR2CD"] : $syokiti019;
            $data1["URICBLEED2CD"][TEXT]         = $data["URICBLEED2CD"] ? $data["URICBLEED2CD"] : $syokiti018;
            $data1["URICOTHERTEST"][TEXT]        = $data["URICOTHERTEST"];
            $data1["URI_ADVISECD"][TEXT]         = $data["URI_ADVISECD"];
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F030"));
            $data1["NUTRITIONCD"][TEXT]          = $data["NUTRITIONCD"] ? $data["NUTRITIONCD"] : $syokiti;
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F040"));
            $data1["SPINERIBCD"][TEXT]           = $data["SPINERIBCD"] ? $data["SPINERIBCD"] : $syokiti;
            if (!((int)$data["SPINERIBCD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2")) {
                $data1["SPINERIBCD_REMARK"][TEXT]    = $data["SPINERIBCD_REMARK"];
            }
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F050"));
            $data1["EYEDISEASECD"][TEXT]         = $data["EYEDISEASECD"] ? $data["EYEDISEASECD"] : $syokiti;
            if (!((int)$data["EYEDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2")) {
                $data1["EYE_TEST_RESULT"][TEXT]      = $data["EYE_TEST_RESULT"];
            }
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F060"));
            $data1["NOSEDISEASECD"][TEXT]        = $data["NOSEDISEASECD"] ? $data["NOSEDISEASECD"] : $syokiti;
            if (!((int)$data["NOSEDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2")) {
                $data1["NOSEDISEASECD_REMARK"][TEXT] = $data["NOSEDISEASECD_REMARK"];
            }
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F070"));
            $data1["SKINDISEASECD"][TEXT]        = $data["SKINDISEASECD"] ? $data["SKINDISEASECD"] : $syokiti;
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F080"));
            $data1["HEART_MEDEXAM"][TEXT]        = $data["HEART_MEDEXAM"] ? $data["HEART_MEDEXAM"] : $syokiti;
            if (!((int)$data["HEART_MEDEXAM"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2")) {
                $data1["HEART_MEDEXAM_REMARK"][TEXT] = $data["HEART_MEDEXAM_REMARK"];
            }
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F090"));
            $data1["HEARTDISEASECD"][TEXT]       = $data["HEARTDISEASECD"] ? $data["HEARTDISEASECD"] : $syokiti;
            if (!((int)$data["HEARTDISEASECD"] < 2 && $model->Properties["printKenkouSindanIppan"] != "2")) {
                $data1["HEARTDISEASECD_REMARK"][TEXT]= $data["HEARTDISEASECD_REMARK"];
            }
            $data1["MANAGEMENT_DIV"][TEXT]       = $data["MANAGEMENT_DIV"];
            $data1["MANAGEMENT_REMARK"][TEXT]    = $data["MANAGEMENT_REMARK"];
            $data1["TB_FILMDATE"][TEXT]          = $data["TB_FILMDATE"];
            $data1["TB_FILMNO"][TEXT]            = $data["TB_FILMNO"];
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F100"));
            $data1["TB_REMARKCD"][TEXT]          = $data["TB_REMARKCD"] ? $data["TB_REMARKCD"] : $syokiti;
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F110"));
            $data1["TB_OTHERTESTCD"][TEXT]       = $data["TB_OTHERTESTCD"] ? $data["TB_OTHERTESTCD"] : $syokiti;
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F120"));
            $data1["TB_NAMECD"][TEXT]            = $data["TB_NAMECD"] ? $data["TB_NAMECD"] : $syokiti;
            $data1["TB_ADVISECD"][TEXT]          = $data["TB_ADVISECD"];
            $data1["TB_X_RAY"][TEXT]             = $data["TB_X_RAY"];
            if ($model->Properties["useParasite_".$model->field["SCHOOL_KIND"]] == "1") {
                $data1["PARASITE"][TEXT]        = $data["PARASITE"];
            }
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F140"));
            $data1["OTHERDISEASECD"][TEXT]       = $data["OTHERDISEASECD"] ? $data["OTHERDISEASECD"] : $syokiti;
            $data1["OTHER_ADVISECD"][TEXT]       = $data["OTHER_ADVISECD"];
            $data1["OTHER_REMARK"][TEXT]         = $data["OTHER_REMARK"];
            if ($model->isFukui) { // 福井は貧血
                $data1["ANEMIA_REMARK"][TEXT]    = $data["ANEMIA_REMARK"];
                $data1["HEMOGLOBIN"][NUMBER]     = $data["HEMOGLOBIN"];
            }
            $data1["DOC_REMARK"][TEXT]           = $data["DOC_REMARK"];
            if ($model->isMie) {
                $data1["DOC_CD"][TEXT]               = $data["DOC_CD"];
            }
            $data1["DOC_DATE"][TEXT]             = $data["DOC_DATE"];
            $syokiti = $db->getOne(knjf140aQuery::getSyokiti($model, "F150"));
            $data1["TREATCD"][TEXT]              = $data["TREATCD"] ? $data["TREATCD"] : $syokiti;
            $data1["REMARK"][TEXT]               = $data["REMARK"];
            $data1["GUIDE_DIV"][TEXT]            = $data["GUIDE_DIV"];
            $data1["JOINING_SPORTS_CLUB"][TEXT]  = $data["JOINING_SPORTS_CLUB"];
            $data1["MEDICAL_HISTORY1"][TEXT]     = $data["MEDICAL_HISTORY1"];
            $data1["MEDICAL_HISTORY2"][TEXT]     = $data["MEDICAL_HISTORY2"];
            $data1["MEDICAL_HISTORY3"][TEXT]     = $data["MEDICAL_HISTORY3"];
            $data1["DIAGNOSIS_NAME"][TEXT]       = $data["DIAGNOSIS_NAME"];
            $data1["REGISTERCD"][TEXT]           = STAFFCD;
            $data1["UPDATED"][NUMBER]            = "sysdate()";
        } else { //歯・口腔
            //「学校歯科医・日付」が設定されていないとき、「健康診断実施日付」をセットする
            //近大は「学校歯科医・日付」が設定されていないとき、nullのまま更新する
            if ($model->z010name1 !== 'KINDAI' && $model->z010 !== 'KINJUNIOR') {
                if (!$data["DENTISTREMARKDATE"]) {
                    $data["DENTISTREMARKDATE"] = $data["TOOTH_DATE"];
                }
            }

            $data1["YEAR"][TEXT]                 = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]             = $data["SCHREGNO"];
            $data1["JAWS_JOINTCD"][TEXT]         = $data["JAWS_JOINTCD"];
            if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
                $data1["JAWS_JOINTCD3"][TEXT]        = $data["JAWS_JOINTCD3"];
            } elseif ($model->Properties["printKenkouSindanIppan"] == "1" && $model->z010name1 === "mieken") {
                $data1["JAWS_JOINTCD3"][TEXT]        = $data["JAWS_JOINTCD3"];
            }
            $data1["JAWS_JOINTCD2"][TEXT]        = $data["JAWS_JOINTCD2"];
            $data1["PLAQUECD"][TEXT]             = $data["PLAQUECD"];
            $data1["GUMCD"][TEXT]                = $data["GUMCD"];
            if ($model->Properties["printKenkouSindanIppan"] != "2") {
                if ($model->z010name1 != "miyagiken") { // 宮城は歯石沈着を使用しない
                    $data1["CALCULUS"][TEXT]             = $data["CALCULUS"];
                }
            }
            if ($model->z010name1 != "miyagiken") { // 宮城は矯正を使用しない
                $data1["ORTHODONTICS"][TEXT]         = $data["ORTHODONTICS"];
            }
            $data1["UP_R_BABY5"][TEXT]           = $data["UP_R_BABY5"];
            $data1["UP_R_BABY4"][TEXT]           = $data["UP_R_BABY4"];
            $data1["UP_R_BABY3"][TEXT]           = $data["UP_R_BABY3"];
            $data1["UP_R_BABY2"][TEXT]           = $data["UP_R_BABY2"];
            $data1["UP_R_BABY1"][TEXT]           = $data["UP_R_BABY1"];
            $data1["UP_L_BABY1"][TEXT]           = $data["UP_L_BABY1"];
            $data1["UP_L_BABY2"][TEXT]           = $data["UP_L_BABY2"];
            $data1["UP_L_BABY3"][TEXT]           = $data["UP_L_BABY3"];
            $data1["UP_L_BABY4"][TEXT]           = $data["UP_L_BABY4"];
            $data1["UP_L_BABY5"][TEXT]           = $data["UP_L_BABY5"];
            $data1["LW_R_BABY5"][TEXT]           = $data["LW_R_BABY5"];
            $data1["LW_R_BABY4"][TEXT]           = $data["LW_R_BABY4"];
            $data1["LW_R_BABY3"][TEXT]           = $data["LW_R_BABY3"];
            $data1["LW_R_BABY2"][TEXT]           = $data["LW_R_BABY2"];
            $data1["LW_R_BABY1"][TEXT]           = $data["LW_R_BABY1"];
            $data1["LW_L_BABY1"][TEXT]           = $data["LW_L_BABY1"];
            $data1["LW_L_BABY2"][TEXT]           = $data["LW_L_BABY2"];
            $data1["LW_L_BABY3"][TEXT]           = $data["LW_L_BABY3"];
            $data1["LW_L_BABY4"][TEXT]           = $data["LW_L_BABY4"];
            $data1["LW_L_BABY5"][TEXT]           = $data["LW_L_BABY5"];
            $data1["BABYTOOTH"][NUMBER]          = $data["BABYTOOTH"];
            $data1["REMAINBABYTOOTH"][NUMBER]    = $data["REMAINBABYTOOTH"];
            $data1["TREATEDBABYTOOTH"][NUMBER]   = $data["TREATEDBABYTOOTH"];
            if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
                $data1["BRACK_BABYTOOTH"][NUMBER]    = $toothData["BRACK_BABYTOOTH"];
            } else {
                $data1["BRACK_BABYTOOTH"][NUMBER]    = $data["BRACK_BABYTOOTH"];
            }
            $data1["UP_R_ADULT8"][TEXT]          = $data["UP_R_ADULT8"];
            $data1["UP_R_ADULT7"][TEXT]          = $data["UP_R_ADULT7"];
            $data1["UP_R_ADULT6"][TEXT]          = $data["UP_R_ADULT6"];
            $data1["UP_R_ADULT5"][TEXT]          = $data["UP_R_ADULT5"];
            $data1["UP_R_ADULT4"][TEXT]          = $data["UP_R_ADULT4"];
            $data1["UP_R_ADULT3"][TEXT]          = $data["UP_R_ADULT3"];
            $data1["UP_R_ADULT2"][TEXT]          = $data["UP_R_ADULT2"];
            $data1["UP_R_ADULT1"][TEXT]          = $data["UP_R_ADULT1"];
            $data1["UP_L_ADULT1"][TEXT]          = $data["UP_L_ADULT1"];
            $data1["UP_L_ADULT2"][TEXT]          = $data["UP_L_ADULT2"];
            $data1["UP_L_ADULT3"][TEXT]          = $data["UP_L_ADULT3"];
            $data1["UP_L_ADULT4"][TEXT]          = $data["UP_L_ADULT4"];
            $data1["UP_L_ADULT5"][TEXT]          = $data["UP_L_ADULT5"];
            $data1["UP_L_ADULT6"][TEXT]          = $data["UP_L_ADULT6"];
            $data1["UP_L_ADULT7"][TEXT]          = $data["UP_L_ADULT7"];
            $data1["UP_L_ADULT8"][TEXT]          = $data["UP_L_ADULT8"];
            $data1["LW_R_ADULT8"][TEXT]          = $data["LW_R_ADULT8"];
            $data1["LW_R_ADULT7"][TEXT]          = $data["LW_R_ADULT7"];
            $data1["LW_R_ADULT6"][TEXT]          = $data["LW_R_ADULT6"];
            $data1["LW_R_ADULT5"][TEXT]          = $data["LW_R_ADULT5"];
            $data1["LW_R_ADULT4"][TEXT]          = $data["LW_R_ADULT4"];
            $data1["LW_R_ADULT3"][TEXT]          = $data["LW_R_ADULT3"];
            $data1["LW_R_ADULT2"][TEXT]          = $data["LW_R_ADULT2"];
            $data1["LW_R_ADULT1"][TEXT]          = $data["LW_R_ADULT1"];
            $data1["LW_L_ADULT1"][TEXT]          = $data["LW_L_ADULT1"];
            $data1["LW_L_ADULT2"][TEXT]          = $data["LW_L_ADULT2"];
            $data1["LW_L_ADULT3"][TEXT]          = $data["LW_L_ADULT3"];
            $data1["LW_L_ADULT4"][TEXT]          = $data["LW_L_ADULT4"];
            $data1["LW_L_ADULT5"][TEXT]          = $data["LW_L_ADULT5"];
            $data1["LW_L_ADULT6"][TEXT]          = $data["LW_L_ADULT6"];
            $data1["LW_L_ADULT7"][TEXT]          = $data["LW_L_ADULT7"];
            $data1["LW_L_ADULT8"][TEXT]          = $data["LW_L_ADULT8"];
            $data1["ADULTTOOTH"][NUMBER]         = $data["ADULTTOOTH"];
            $data1["REMAINADULTTOOTH"][NUMBER]   = $data["REMAINADULTTOOTH"];
            $data1["TREATEDADULTTOOTH"][NUMBER]  = $data["TREATEDADULTTOOTH"];
            $data1["LOSTADULTTOOTH"][NUMBER]     = $data["LOSTADULTTOOTH"];
            if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
                $data1["BRACK_ADULTTOOTH"][NUMBER]    = $toothData["BRACK_ADULTTOOTH"];
            } else {
                $data1["BRACK_ADULTTOOTH"][NUMBER]    = $data["BRACK_ADULTTOOTH"];
            }
            if ($model->Properties["printKenkouSindanIppan"] != "3") {
                $data1["OTHERDISEASECD"][TEXT]      = $data["OTHERDISEASECD"];
            }
            if ($model->Properties["printKenkouSindanIppan"] == "3" || $data["OTHERDISEASECD"] == '99') {
                $data1["OTHERDISEASE"][TEXT]        = $data["OTHERDISEASE"];
            }
            if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
                if ($model->Properties["printKenkouSindanIppan"] == "2") {
                    $data1["DENTISTREMARK_CO"][NUMBER]   = $toothData["DENTISTREMARK_CO"];
                } else {
                    $data1["DENTISTREMARK_CO"][NUMBER]   = $data["DENTISTREMARK_CO"];
                }
                $data1["DENTISTREMARK_GO"][TEXT]     = $data["DENTISTREMARK_GO"];
                $data1["DENTISTREMARK_G"][TEXT]      = $data["DENTISTREMARK_G"];
            } else {
                $data1["DENTISTREMARKCD"][TEXT]      = $data["DENTISTREMARKCD"];
                $data1["DENTISTREMARK"][TEXT]        = $data["DENTISTREMARK"];
            }
            $data1["DENTISTREMARKDATE"][TEXT]    = $data["DENTISTREMARKDATE"];
            if ($model->Properties["printKenkouSindanIppan"] != "3") {
                $data1["DENTISTTREATCD"][TEXT]      = $data["DENTISTTREATCD"];
            }
            $data1["DENTISTTREAT"][TEXT]         = $data["DENTISTTREAT"];
            $data1["REGISTERCD"][TEXT]           = STAFFCD;
            $data1["UPDATED"][NUMBER]            = "sysdate()";
        }
        $query = Query::insertSQL($data1, $table);

        $db->query($query);

        if ($csvcd == "1") {
            $query  = " DELETE FROM ";
            $query .= "     MEDEXAM_DET_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$data["YEAR"]}' ";
            $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
            $query .= " AND DET_SEQ <> '010' ";
            $db->query($query);

            $data1 = array();
            $data1["YEAR"][TEXT]        = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
            $data1["DET_SEQ"][TEXT]     = "009";
            if (!((int)$data["NUTRITIONCD"] <= "1")) {
                $data1["DET_REMARK1"][TEXT] = $data["NUTRITIONCD_REMARK"];
            }
            if ($model->isKoma || ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1")) {
                $data1["DET_REMARK5"][TEXT] = $data["R_EAR_DB_1000"];
                $data1["DET_REMARK6"][TEXT] = $data["L_EAR_DB_1000"];
                if ($model->isKoma) {
                    $data1["DET_REMARK7"][TEXT] = $data["HEART_GRAPH_NO"];
                }
            }
            if ($model->Properties["useEar4000Hz"] == "1") {
                $data1["DET_REMARK2"][TEXT] = $data["R_EAR_DB_4000"];
                $data1["DET_REMARK3"][TEXT] = $data["L_EAR_DB_4000"];
            }
            $data1["REGISTERCD"][TEXT]  = STAFFCD;
            $data1["UPDATED"][NUMBER]   = "sysdate()";
            $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
            $db->query($query);

            $data1 = array();
            $data1["YEAR"][TEXT]        = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
            $data1["DET_SEQ"][TEXT]     = "001";
            $data1["DET_REMARK2"][TEXT] = $data["OTHER_REMARK2"];
            $data1["DET_REMARK3"][TEXT] = $data["OTHER_REMARK3"];
            $data1["REGISTERCD"][TEXT]  = STAFFCD;
            $data1["UPDATED"][NUMBER]   = "sysdate()";
            $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
            $db->query($query);

            $data1 = array();
            $data1["YEAR"][TEXT]        = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
            $data1["DET_SEQ"][TEXT]     = "005";
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                $data1["DET_REMARK1"][TEXT] = $data["TB_OTHERTEST_REMARK1"];
                $data1["DET_REMARK3"][TEXT] = $data["TB_ADVISE_REMARK1"];
            }
            $data1["DET_REMARK2"][TEXT] = $data["TB_NAME_REMARK1"];
            $data1["REGISTERCD"][TEXT]  = STAFFCD;
            $data1["UPDATED"][NUMBER]   = "sysdate()";
            $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
            $db->query($query);

            $data1 = array();
            $data1["YEAR"][TEXT]        = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
            $data1["DET_SEQ"][TEXT]     = "004";
            $data1["DET_REMARK1"][TEXT] = $data["TREAT_REMARK1"];
            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                $data1["DET_REMARK2"][TEXT] = $data["TREAT_REMARK2"];
                $data1["DET_REMARK3"][TEXT] = $data["TREAT_REMARK3"];
            }
            $data1["REGISTERCD"][TEXT]  = STAFFCD;
            $data1["UPDATED"][NUMBER]   = "sysdate()";
            $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
            $db->query($query);

            $data1 = array();
            $data1["YEAR"][TEXT]        = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
            $data1["DET_SEQ"][TEXT]     = "011";
            $data1["DET_REMARK1"][TEXT] = $data["TREATCD2"];
            $data1["REGISTERCD"][TEXT]  = STAFFCD;
            $data1["UPDATED"][NUMBER]   = "sysdate()";
            $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
            $db->query($query);

            //MEDEXAM_DET_DETAIL_DAT(012)
            $s012Cnt = $db->getOne(knjf140aQuery::getDetailCnt($data["YEAR"], $data["SCHREGNO"], '012'));
            $data1 = array();
            $data1["YEAR"][TEXT]         = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]     = $data["SCHREGNO"];
            $data1["DET_SEQ"][TEXT]      = '012';
            $data1["DET_REMARK1"][TEXT]  = $data["R_VISION_CANTMEASURE"];    // 視力・測定困難（右）
            $data1["DET_REMARK2"][TEXT]  = $data["L_VISION_CANTMEASURE"];    // 視力・測定困難（左）
            $data1["DET_REMARK3"][TEXT]  = $data["R_EAR_CANTMEASURE"];       // 聴力・測定困難（右）
            $data1["DET_REMARK4"][TEXT]  = $data["L_EAR_CANTMEASURE"];       // 聴力・測定困難（左）
            $data1["DET_REMARK5"][TEXT]  = $data["NO_PRINT_OBESITY_INDEX"];  // 肥満度印字無し
            $data1["REGISTERCD"][TEXT]   = STAFFCD;
            $data1["UPDATED"][FUNC]      = "sysdate()";

            if ($s012Cnt == 0) {
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
            } else {
                $where  = " WHERE YEAR     = '".$data["YEAR"]."' ";
                $where .= "   AND SCHREGNO = '".$data["SCHREGNO"]."' ";
                $where .= "   AND DET_SEQ  = '012'";
                $query = Query::updateSQL($data1, "MEDEXAM_DET_DETAIL_DAT", $where);
            }
            $db->query($query);

            if ($model->Properties["printKenkouSindanIppan"] == "1" && $model->Properties["KenkouSindan_Ippan_Pattern"] == "1") {
                //MEDEXAM_DET_DETAIL_DAT(017)
                $s017Cnt = $db->getOne(knjf140aQuery::getDetailCnt($data["YEAR"], $data["SCHREGNO"], '017'));
                $data1 = array();
                $data1["YEAR"][TEXT]         = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]     = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]      = '017';
                $data1["DET_REMARK1"][TEXT]  = $data["URICPH1"];    // 1次尿PH
                $data1["DET_REMARK2"][TEXT]  = $data["URICPH2"];    // 2次尿PH
                $data1["REGISTERCD"][TEXT]   = STAFFCD;
                $data1["UPDATED"][FUNC]      = "sysdate()";

                if ($s017Cnt == 0) {
                    $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                } else {
                    $where  = " WHERE YEAR     = '".$data["YEAR"]."' ";
                    $where .= "   AND SCHREGNO = '".$data["SCHREGNO"]."' ";
                    $where .= "   AND DET_SEQ  = '017'";
                    $query = Query::updateSQL($data1, "MEDEXAM_DET_DETAIL_DAT", $where);
                }
                $db->query($query);
            }

            //特別支援学校
            if ($model->Properties["useSpecial_Support_School"] == "1") {
                //MEDEXAM_DET_DETAIL_DAT(018)
                $s018Cnt = $db->getOne(knjf140aQuery::getDetailCnt($data["YEAR"], $data["SCHREGNO"], '018'));
                $data1 = array();
                $data1["YEAR"][TEXT]         = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]     = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]      = '018';
                $data1["DET_REMARK1"][TEXT]  = $data["R_EAR_IN"];           // 聴力・右状態  （装用時）
                $data1["DET_REMARK2"][TEXT]  = $data["R_EAR_DB_IN"];        // 聴力・右DB    （装用時）
                $data1["DET_REMARK3"][TEXT]  = $data["R_EAR_DB_4000_IN"];   // 聴力・右4000Hz（装用時）
                $data1["DET_REMARK4"][TEXT]  = $data["L_EAR_IN"];           // 聴力・左状態  （装用時）
                $data1["DET_REMARK5"][TEXT]  = $data["L_EAR_DB_IN"];        // 聴力・左DB    （装用時）
                $data1["DET_REMARK6"][TEXT]  = $data["L_EAR_DB_4000_IN"];   // 聴力・左4000Hz（装用時）
                $data1["REGISTERCD"][TEXT]   = STAFFCD;
                $data1["UPDATED"][FUNC]      = "sysdate()";

                if ($s018Cnt == 0) {
                    $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                } else {
                    $where  = " WHERE YEAR     = '".$data["YEAR"]."' ";
                    $where .= "   AND SCHREGNO = '".$data["SCHREGNO"]."' ";
                    $where .= "   AND DET_SEQ  = '018'";
                    $query = Query::updateSQL($data1, "MEDEXAM_DET_DETAIL_DAT", $where);
                }
                $db->query($query);
            }

            if ($model->Properties["printKenkouSindanIppan"] == "1") {
                $data1 = array();
                $data1["YEAR"][TEXT]        = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]     = "003";
                if (!((int)$data["NOSEDISEASECD5"] < 2)) {
                    $data1["DET_REMARK1"][TEXT] = $data["NOSEDISEASECD_REMARK1"];
                }
                if (!((int)$data["NOSEDISEASECD6"] < 2)) {
                    $data1["DET_REMARK2"][TEXT] = $data["NOSEDISEASECD_REMARK2"];
                }
                if (!((int)$data["NOSEDISEASECD7"] < 2)) {
                    $data1["DET_REMARK3"][TEXT] = $data["NOSEDISEASECD_REMARK3"];
                }
                $data1["DET_REMARK7"][TEXT] = $data["NOSEDISEASECD5"];
                $data1["DET_REMARK8"][TEXT] = $data["NOSEDISEASECD6"];
                $data1["DET_REMARK9"][TEXT] = $data["NOSEDISEASECD7"];
                $data1["REGISTERCD"][TEXT]  = STAFFCD;
                $data1["UPDATED"][NUMBER]   = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);

                if ($model->z010name1 != "miyagiken") { // 宮城は色覚異常を使用しない
                    $data1 = array();
                    $data1["YEAR"][TEXT]        = $data["YEAR"];
                    $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                    $data1["DET_SEQ"][TEXT]     = "002";
                    $data1["DET_REMARK7"][TEXT] = $data["EYEDISEASECD5"];
                    if (array_key_exists("VISION_CANTMEASURE", $data)) {
                        $data1["DET_REMARK8"][TEXT] = $data["VISION_CANTMEASURE"];
                    }
                    $data1["REGISTERCD"][TEXT]  = STAFFCD;
                    $data1["UPDATED"][NUMBER]   = "sysdate()";
                    $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                    $db->query($query);
                } else {
                    //データがあれば実施。旧データフォーマットの互換性。
                    if (array_key_exists("VISION_CANTMEASURE", $data)) {
                        $data1 = array();
                        $data1["YEAR"][TEXT]        = $data["YEAR"];
                        $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                        $data1["DET_SEQ"][TEXT]     = "002";
                        $data1["DET_REMARK8"][TEXT] = $data["VISION_CANTMEASURE"];
                        $data1["REGISTERCD"][TEXT]  = STAFFCD;
                        $data1["UPDATED"][NUMBER]   = "sysdate()";
                        $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                        $db->query($query);
                    }
                }

                if (!((int)$data["SKINDISEASECD"] < 2)) {
                    $data1 = array();
                    $data1["YEAR"][TEXT]        = $data["YEAR"];
                    $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                    $data1["DET_SEQ"][TEXT]     = "006";
                    $data1["DET_REMARK1"][TEXT] = $data["SKINDISEASECD_REMARK"];
                    $data1["REGISTERCD"][TEXT]  = STAFFCD;
                    $data1["UPDATED"][NUMBER]   = "sysdate()";
                    $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                    $db->query($query);
                }
            }

            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                $data1 = array();
                $data1["YEAR"][TEXT]        = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]     = "002";
                $data1["DET_REMARK1"][TEXT] = $data["EYE_TEST_RESULT2"];
                $data1["DET_REMARK2"][TEXT] = $data["EYE_TEST_RESULT3"];
                if ($model->Properties["printKenkouSindanIppan"] == "2") {
                    $data1["DET_REMARK4"][TEXT] = $data["EYEDISEASECD2"];
                    $data1["DET_REMARK5"][TEXT] = $data["EYEDISEASECD3"];
                    $data1["DET_REMARK6"][TEXT] = $data["EYEDISEASECD4"];
                }
                $data1["REGISTERCD"][TEXT]  = STAFFCD;
                $data1["UPDATED"][NUMBER]   = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);

                $data1 = array();
                $data1["YEAR"][TEXT]        = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]     = "003";
                $data1["DET_REMARK1"][TEXT] = $data["NOSEDISEASECD_REMARK2"];
                $data1["DET_REMARK2"][TEXT] = $data["NOSEDISEASECD_REMARK3"];
                if ($model->Properties["printKenkouSindanIppan"] == "2") {
                    $data1["DET_REMARK4"][TEXT] = $data["NOSEDISEASECD2"];
                    $data1["DET_REMARK5"][TEXT] = $data["NOSEDISEASECD3"];
                    $data1["DET_REMARK6"][TEXT] = $data["NOSEDISEASECD4"];
                }
                $data1["REGISTERCD"][TEXT]  = STAFFCD;
                $data1["UPDATED"][NUMBER]   = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);

                $data1 = array();
                $data1["YEAR"][TEXT]        = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]     = "006";
                $data1["DET_REMARK1"][TEXT] = $data["SKINDISEASECD_REMARK"];
                $data1["REGISTERCD"][TEXT]  = STAFFCD;
                $data1["UPDATED"][NUMBER]   = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);

                $data1 = array();
                $data1["YEAR"][TEXT]        = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]     = "007";
                $data1["DET_REMARK1"][TEXT] = $data["URICOTHERTESTCD"];
                $data1["REGISTERCD"][TEXT]  = STAFFCD;
                $data1["UPDATED"][NUMBER]   = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);

                $data1 = array();
                $data1["YEAR"][TEXT]        = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]     = "008";
                $data1["DET_REMARK1"][TEXT] = $data["MESSAGE"];
                $data1["REGISTERCD"][TEXT]  = STAFFCD;
                $data1["UPDATED"][NUMBER]   = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }
        }

        if ($csvcd == "2") {
            $query  = " DELETE FROM ";
            $query .= "     MEDEXAM_TOOTH_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$data["YEAR"]}' ";
            $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
            $db->query($query);

            if ($model->Properties["printKenkouSindanIppan"] == "2") {
                $data1 = array();
                $data1["YEAR"][TEXT]            = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]        = $data["SCHREGNO"];
                $data1["TOOTH_SEQ"][TEXT]       = "001";
                $data1["TOOTH_REMARK2"][TEXT]   = $data["DENTISTTREAT2"];
                $data1["TOOTH_REMARK3"][TEXT]   = $data["DENTISTTREAT3"];
                $data1["REGISTERCD"][TEXT]      = STAFFCD;
                $data1["UPDATED"][NUMBER]       = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_TOOTH_DETAIL_DAT");
                $db->query($query);
            }

            if ($model->Properties["printKenkouSindanIppan"] != "2") {
                $data1 = array();
                $data1["YEAR"][TEXT]            = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]        = $data["SCHREGNO"];
                $data1["TOOTH_SEQ"][TEXT]       = "002";
                $data1["TOOTH_REMARK1"][TEXT]   = $data["OTHERDISEASECD2"];
                $data1["TOOTH_REMARK2"][TEXT]   = $data["OTHERDISEASE2"];
                if ($model->is_f020_otherdisese_hyouji) {
                    $data1["TOOTH_REMARK3"][TEXT]   = $data["OTHERDISEASE_REMARK1"];
                    $data1["TOOTH_REMARK4"][TEXT]   = $data["OTHERDISEASE_REMARK2"];
                    $data1["TOOTH_REMARK5"][TEXT]   = $data["OTHERDISEASE_REMARK3"];
                    $data1["TOOTH_REMARK6"][TEXT]   = $data["OTHERDISEASE_REMARK4"];
                }
                $data1["REGISTERCD"][TEXT]      = STAFFCD;
                $data1["UPDATED"][NUMBER]       = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_TOOTH_DETAIL_DAT");
                $db->query($query);
            }

            if ($model->isMigagi || $model->is_f020_otherdisese_hyouji2) {
                $data1 = array();
                $data1["YEAR"][TEXT]            = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]        = $data["SCHREGNO"];
                $data1["TOOTH_SEQ"][TEXT]       = "003";
                $data1["TOOTH_REMARK3"][TEXT]   = $data["OTHERDISEASECD3"];
                $data1["TOOTH_REMARK4"][TEXT]   = $data["OTHERDISEASECD4"];
                if ($model->is_f020_otherdisese_hyouji2) {
                    $data1["TOOTH_REMARK5"][TEXT]   = $data["OTHERDISEASE3"];
                    $data1["TOOTH_REMARK6"][TEXT]   = $data["OTHERDISEASE4"];
                }
                $data1["REGISTERCD"][TEXT]      = STAFFCD;
                $data1["UPDATED"][NUMBER]       = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_TOOTH_DETAIL_DAT");
                $db->query($query);
            }

            if ($model->is_f020_dentistremark_hyouji) {
                $data1 = array();
                $data1["YEAR"][TEXT]            = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]        = $data["SCHREGNO"];
                $data1["TOOTH_SEQ"][TEXT]       = "005";
                $data1["TOOTH_REMARK1"][TEXT]   = $data["DENTISTREMARK_REMARK1"];
                $data1["TOOTH_REMARK2"][TEXT]   = $data["DENTISTREMARK_REMARK2"];
                $data1["TOOTH_REMARK3"][TEXT]   = $data["DENTISTREMARK_REMARK3"];
                $data1["TOOTH_REMARK4"][TEXT]   = $data["DENTISTREMARK_REMARK4"];
                $data1["REGISTERCD"][TEXT]      = STAFFCD;
                $data1["UPDATED"][NUMBER]       = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_TOOTH_DETAIL_DAT");
                $db->query($query);
            }
        }

        //MEDEXAM_HDATの更新(あればアップデートなければインサート)
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$data["YEAR"]}' ";
        $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
        $countHdat = $db->getOne($query);

        if ($csvcd == "1") { //一般
            $data2["YEAR"][TEXT]       = $data["YEAR"];
            $data2["SCHREGNO"][TEXT]   = $data["SCHREGNO"];
            $data2["DATE"][TEXT]       = $data["DATE"];
            $data2["REGISTERCD"][TEXT] = STAFFCD;
            $data2["UPDATED"][NUMBER]  = "sysdate()";
        } else { //歯・口腔
            $data2["YEAR"][TEXT]       = $data["YEAR"];
            $data2["SCHREGNO"][TEXT]   = $data["SCHREGNO"];
            $data2["TOOTH_DATE"][TEXT] = $data["TOOTH_DATE"];
            $data2["REGISTERCD"][TEXT] = STAFFCD;
            $data2["UPDATED"][NUMBER]  = "sysdate()";
        }

        if ($countHdat > 0) { //レコードがあればアップデート
            $where  = " WHERE ";
            $where .= "     YEAR     = '{$data["YEAR"]}' ";
            $where .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";

            $query = Query::updateSQL($data2, "MEDEXAM_HDAT", $where);
        } else {
            $query = Query::insertSQL($data2, "MEDEXAM_HDAT");
        }

        $db->query($query);
    }

    //カウント取得(MEDEXAM_DET_DETAIL_DAT)
    public function getDetailCnt($year, $schregno, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";
        $query .= "     AND DET_SEQ  = '{$seq}' ";

        return $query;
    }

    //初期値
    public function getSyokiti($model, $nameCd1)
    {
        if ($model->Properties["printKenkouSindanIppan"] == "2") {
            $query = "SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = '{$nameCd1}'";
        } else {
            $query = "SELECT CAST(NULL AS VARCHAR(1)) FROM NAME_MST WHERE NAMECD1 = '{$nameCd1}'";
        }
        $query.= " AND NAMESPARE3 = '1' ORDER BY NAMECD2 ";
        return $query;
    }

    //初期値98
    public function getSyokiti98($model, $nameCd1)
    {
        $query = "SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = '{$nameCd1}' AND NAMECD2 = '98' ";
        return $query;
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = "DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJF140A'";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //CVS作成用のQUERY
    public function selectCsvQuery()
    {
        $query  = "SELECT MSGROW, MSGREMARK FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJF140A'";
    
        return $query;
    }
}
