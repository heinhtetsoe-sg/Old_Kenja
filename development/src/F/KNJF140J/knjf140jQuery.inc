<?php

require_once('for_php7.php');
class knjf140jquery extends Query
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

    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID       = 'KNJF140J' ";
        $query .= "     AND INVALID_FLG = '0' ";

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
            $query .= "     R_VISION_CANTMEASURE, ";
            $query .= "     L_VISION_CANTMEASURE, ";
            $query .= "     R_BAREVISION_MARK, ";
            $query .= "     L_BAREVISION_MARK, ";
            $query .= "     R_VISION_MARK, ";
            $query .= "     L_VISION_MARK, ";
            $query .= "     R_BAREVISION, ";
            $query .= "     L_BAREVISION, ";
            $query .= "     R_VISION, ";
            $query .= "     L_VISION, ";
            $query .= "     R_EAR_CANTMEASURE, ";
            $query .= "     R_EAR_DB, ";
            $query .= "     R_EAR, ";
            $query .= "     L_EAR_CANTMEASURE, ";
            $query .= "     L_EAR_DB, ";
            $query .= "     L_EAR, ";
            $query .= "     NUTRITIONCD, ";
            $query .= "     NUTRITIONCD_REMARK, ";
            $query .= "     SPINERIBCD, ";
            $query .= "     SPINERIBCD_REMARK, ";
            $query .= "     SPINERIBCD1, ";
            $query .= "     SPINERIBCD_REMARK1, ";
            $query .= "     SPINERIBCD2, ";
            $query .= "     SPINERIBCD_REMARK2, ";
            $query .= "     SPINERIBCD3, ";
            $query .= "     SPINERIBCD_REMARK3, ";
            $query .= "     EYEDISEASECD, ";
            $query .= "     EYE_TEST_RESULT, ";
            $query .= "     EYEDISEASECD5, ";
            $query .= "     VISION_CANTMEASURE, ";
            $query .= "     NOSEDISEASECD, ";
            $query .= "     NOSEDISEASECD_REMARK, ";
            $query .= "     NOSEDISEASECD5, ";
            $query .= "     NOSEDISEASECD_REMARK1, ";
            $query .= "     NOSEDISEASECD6, ";
            $query .= "     NOSEDISEASECD_REMARK2, ";
            $query .= "     NOSEDISEASECD7, ";
            $query .= "     NOSEDISEASECD_REMARK3, ";
            $query .= "     SKINDISEASECD, ";
            $query .= "     SKINDISEASECD_REMARK, ";
            $query .= "     TB_FILMDATE, ";
            $query .= "     TB_FILMNO, ";
            $query .= "     TB_REMARKCD, ";
            $query .= "     TB_X_RAY, ";
            $query .= "     TB_RE_EXAMINATION_DATE, ";
            $query .= "     TB_RE_EXAMINATION_RESULT, ";
            $query .= "     TB_RE_EXAMINATION_FILMNO, ";
            $query .= "     TB_OTHERTESTCD, ";
            $query .= "     TB_NAMECD, ";
            $query .= "     TB_NAME_REMARK1, ";
            $query .= "     TB_ADVISECD, ";
            $query .= "     HEART_MEDEXAM, ";
            $query .= "     HEART_MEDEXAM_REMARK, ";
            $query .= "     MANAGEMENT_DIV, ";
            $query .= "     MANAGEMENT_REMARK, ";
            $query .= "     HEARTDISEASECD, ";
            $query .= "     HEARTDISEASECD_REMARK, ";
            $query .= "     ALBUMINURIA1CD, ";
            $query .= "     URICSUGAR1CD, ";
            $query .= "     URICBLEED1CD, ";
            $query .= "     ALBUMINURIA2CD, ";
            $query .= "     URICSUGAR2CD, ";
            $query .= "     URICBLEED2CD, ";
            $query .= "     DETAILED_EXAMINATION, ";
            $query .= "     URI_ADVISECD, ";
            $query .= "     URICOTHERTEST, ";
            $query .= "     OTHER_REMARK, ";
            $query .= "     DOC_DATE, ";
            $query .= "     DOC_REMARK, ";
            $query .= "     TREATCD, ";
            $query .= "     TREAT_REMARK1, ";
            $query .= "     TREATCD2, ";
            $query .= "     TREATCD2_REMARK1, ";
            $query .= "     REMARK, ";
            $query .= "     MEDICAL_HISTORY1, ";
            $query .= "     MEDICAL_HISTORY2, ";
            $query .= "     MEDICAL_HISTORY3, ";
            $query .= "     DIAGNOSIS_NAME, ";
            $query .= "     'LASTCOLUMN' AS LASTCOLUMN ";
            $query .= " FROM SCHREG_REGD_DAT T3 ";
            $query .= " LEFT JOIN MEDEXAM_HDAT T2 ";
            $query .= "        ON T3.YEAR     = T2.YEAR ";
            $query .= "       AND T3.SCHREGNO = T2.SCHREGNO ";
            $query .= " LEFT JOIN V_MEDEXAM_DET_DAT T1 ";
            $query .= "        ON T1.YEAR     = T2.YEAR ";
            $query .= "       AND T1.SCHREGNO = T2.SCHREGNO ";
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
            $query .= "     OTHERDISEASECD2, ";
            $query .= "     OTHERDISEASE2, ";
            $query .= "     DENTISTREMARKCD, ";
            $query .= "     DENTISTREMARK, ";
            $query .= "     DT005.TOOTH_REMARK1 AS DENTISTREMARKCD2, ";
            $query .= "     DT005.TOOTH_REMARK2 AS DENTISTREMARK2, ";
            $query .= "     DT005.TOOTH_REMARK3 AS DENTISTREMARKCD3, ";
            $query .= "     DT005.TOOTH_REMARK4 AS DENTISTREMARK3, ";
            $query .= "     DENTISTREMARKDATE, ";
            $query .= "     DENTISTTREATCD, ";
            $query .= "     DENTISTTREAT, ";
            $query .= "     DT005.TOOTH_REMARK5 AS DENTISTTREATCD2, ";
            $query .= "     DT005.TOOTH_REMARK6 AS DENTISTTREAT2, ";
            $query .= "     'LASTCOLUMN' AS LASTCOLUMN ";
            $query .= " FROM SCHREG_REGD_DAT T3 ";
            $query .= " LEFT JOIN MEDEXAM_HDAT T2 ";
            $query .= "        ON T3.YEAR         = T2.YEAR ";
            $query .= "        AND T3.SCHREGNO    = T2.SCHREGNO ";
            $query .= " LEFT JOIN V_MEDEXAM_TOOTH_DAT T1 ";
            $query .= "        ON T1.YEAR         = T2.YEAR ";
            $query .= "       AND T1.SCHREGNO     = T2.SCHREGNO ";
            $query .= " LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT005 ";
            $query .= "        ON DT005.YEAR      = T1.YEAR ";
            $query .= "       AND DT005.SCHREGNO  = T1.SCHREGNO ";
            $query .= "       AND DT005.TOOTH_SEQ = '005' ";
        }
        $query .= " LEFT JOIN SCHREG_BASE_MST B1 ON T3.SCHREGNO = B1.SCHREGNO ";
        $query .= " WHERE T3.YEAR || T3.SEMESTER = '{$model->field["YEAR"]}' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
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
        $query .= "ORDER BY T3.GRADE,";
        $query .= "         T3.HR_CLASS,";
        $query .= "         T3.ATTENDNO";

        return $query;
    }

    //コード一覧出力
    public function getCodeListSQL($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS CD";
        $query .= "    ,NAME1   AS NAME";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = {$namecd1} ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //コード一覧最大行数取得
    public function getMaxRowCount($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(NAMECD2) AS ROW_COUNT";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 IN ({$namecd1})";
        $query .= " GROUP BY ";
        $query .= "     NAMECD1 ";
        $query .= " ORDER BY ";
        $query .= "     COUNT(NAMECD2) ";

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
        $query .= "     YEAR           = '{$data["YEAR"]}' ";
        $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
        if ($db->getOne($query) == 0) {
            $query  = " DELETE FROM ";
            $query .= "     MEDEXAM_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR       = '{$data["YEAR"]}' ";
            $query .= " AND SCHREGNO   = '{$data["SCHREGNO"]}' ";
            $db->query($query);
        } else {
            $data2["YEAR"][TEXT]       = $data["YEAR"];
            $data2["SCHREGNO"][TEXT]   = $data["SCHREGNO"];
            $data2[$fieldName][TEXT]   = "";
            $data2["REGISTERCD"][TEXT] = STAFFCD;
            $data2["UPDATED"][NUMBER]  = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR       = '{$data["YEAR"]}' ";
            $where .= " AND SCHREGNO   = '{$data["SCHREGNO"]}' ";

            $query = Query::updateSQL($data2, "MEDEXAM_HDAT", $where);
            $db->query($query);
        }
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function deleteinsertQueryCsv(&$db, $data, $csvcd, $model, $disableCodes)
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
            $data1["YEAR"][TEXT]                      = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]                  = $data["SCHREGNO"];
            $data1["HEIGHT"][NUMBER]                  = $data["HEIGHT"];
            $data1["WEIGHT"][NUMBER]                  = $data["WEIGHT"];
            if ($data["R_VISION_CANTMEASURE"] != "1") {
                $data1["R_BAREVISION_MARK"][TEXT]     = $data["R_BAREVISION_MARK"];
                $data1["R_VISION_MARK"][TEXT]         = $data["R_VISION_MARK"];
                $data1["R_BAREVISION"][TEXT]          = $data["R_BAREVISION"];
                $data1["R_VISION"][TEXT]              = $data["R_VISION"];
            }
            if ($data["L_VISION_CANTMEASURE"] != "1") {
                $data1["L_BAREVISION_MARK"][TEXT]     = $data["L_BAREVISION_MARK"];
                $data1["L_VISION_MARK"][TEXT]         = $data["L_VISION_MARK"];
                $data1["L_BAREVISION"][TEXT]          = $data["L_BAREVISION"];
                $data1["L_VISION"][TEXT]              = $data["L_VISION"];
            }
            if ($data["R_EAR_CANTMEASURE"] != "1") {
                $data1["R_EAR_DB"][NUMBER]            = $data["R_EAR_DB"];
                $data1["R_EAR"][TEXT]                 = $data["R_EAR"];
            }
            if ($data["L_EAR_CANTMEASURE"] != "1") {
                $data1["L_EAR_DB"][NUMBER]            = $data["L_EAR_DB"];
                $data1["L_EAR"][TEXT]                 = $data["L_EAR"];
            }
            $data1["NUTRITIONCD"][TEXT]               = $data["NUTRITIONCD"];
            $data1["SPINERIBCD"][TEXT]                = $data["SPINERIBCD"];
            if (in_array($data["SPINERIBCD"], $disableCodes["F040"]) == false && $data["SPINERIBCD"] != '') {
                $data1["SPINERIBCD_REMARK"][TEXT]     = $data["SPINERIBCD_REMARK"];      //脊柱・胸郭・四肢（全般）所見
            }
            $data1["EYEDISEASECD"][TEXT]              = $data["EYEDISEASECD"];
            if (in_array($data["EYEDISEASECD"], $disableCodes["F050"]) == false && $data["EYEDISEASECD"] != '') {
                $data1["EYE_TEST_RESULT"][TEXT]       = $data["EYE_TEST_RESULT"];        //目の疾病及び異常所見
            }
            $data1["NOSEDISEASECD"][TEXT]             = $data["NOSEDISEASECD"];
            if (in_array($data["NOSEDISEASECD"], $disableCodes["F060"]) == false && $data["NOSEDISEASECD"] != '') {
                $data1["NOSEDISEASECD_REMARK"][TEXT]  = $data["NOSEDISEASECD_REMARK"];   //耳鼻咽頭疾患（全般）所見
            }
            $data1["SKINDISEASECD"][TEXT]             = $data["SKINDISEASECD"];
            $data1["HEART_MEDEXAM"][TEXT]             = $data["HEART_MEDEXAM"];
            if (in_array($data["HEART_MEDEXAM"], $disableCodes["F080"]) == false && $data["HEART_MEDEXAM"] != '') {
                $data1["HEART_MEDEXAM_REMARK"][TEXT]  = $data["HEART_MEDEXAM_REMARK"];   //心臓　臨床医学的検査（心電図）所見
            }
            $data1["MANAGEMENT_DIV"][TEXT]            = $data["MANAGEMENT_DIV"];
            if (in_array($data["MANAGEMENT_DIV"], $disableCodes["F091"]) == false && $data["MANAGEMENT_DIV"] != '') {
                $data1["MANAGEMENT_REMARK"][TEXT]     = $data["MANAGEMENT_REMARK"];     //心臓（精密検査）所見
            }
            $data1["HEARTDISEASECD"][TEXT]            = $data["HEARTDISEASECD"];
            if (in_array($data["HEARTDISEASECD"], $disableCodes["F090"]) == false && $data["HEARTDISEASECD"] != '') {
                $data1["HEARTDISEASECD_REMARK"][TEXT] = $data["HEARTDISEASECD_REMARK"];  //心臓（疾病及び異常）所見
            }
            $data1["ALBUMINURIA1CD"][TEXT]            = $data["ALBUMINURIA1CD"];
            $data1["URICSUGAR1CD"][TEXT]              = $data["URICSUGAR1CD"];
            $data1["URICBLEED1CD"][TEXT]              = $data["URICBLEED1CD"];
            $data1["ALBUMINURIA2CD"][TEXT]            = $data["ALBUMINURIA2CD"];
            $data1["URICSUGAR2CD"][TEXT]              = $data["URICSUGAR2CD"];
            $data1["URICBLEED2CD"][TEXT]              = $data["URICBLEED2CD"];
            $data1["URI_ADVISECD"][TEXT]              = $data["URI_ADVISECD"];
            $data1["TB_FILMDATE"][TEXT]               = $data["TB_FILMDATE"];
            $data1["TB_FILMNO"][TEXT]                 = $data["TB_FILMNO"];
            $data1["TB_REMARKCD"][TEXT]               = $data["TB_REMARKCD"];
            $data1["TB_OTHERTESTCD"][TEXT]            = $data["TB_OTHERTESTCD"];
            $data1["TB_NAMECD"][TEXT]                 = $data["TB_NAMECD"];
            $data1["TB_ADVISECD"][TEXT]               = $data["TB_ADVISECD"];
            if (in_array($data["TB_REMARKCD"], $disableCodes["F100"]) == false && $data["TB_REMARKCD"] != '') {
                $data1["TB_X_RAY"][TEXT]              = $data["TB_X_RAY"];       //結核（所見）所見
            }
            if (in_array($data["DETAILED_EXAMINATION"], $disableCodes["F146"]) == false && $data["DETAILED_EXAMINATION"] != '') {
                $data1["URICOTHERTEST"][TEXT]         = $data["URICOTHERTEST"];  //尿（精密検査）所見
            }
            $data1["OTHER_REMARK"][TEXT]              = $data["OTHER_REMARK"];
            $data1["DOC_REMARK"][TEXT]                = $data["DOC_REMARK"];
            $data1["DOC_DATE"][TEXT]                  = $data["DOC_DATE"];
            $data1["TREATCD"][TEXT]                   = $data["TREATCD"];
            $data1["REMARK"][TEXT]                    = $data["REMARK"];
            $data1["MEDICAL_HISTORY1"][TEXT]          = $data["MEDICAL_HISTORY1"];
            $data1["MEDICAL_HISTORY2"][TEXT]          = $data["MEDICAL_HISTORY2"];
            $data1["MEDICAL_HISTORY3"][TEXT]          = $data["MEDICAL_HISTORY3"];
            $data1["DIAGNOSIS_NAME"][TEXT]            = $data["DIAGNOSIS_NAME"];
            $data1["REGISTERCD"][TEXT]                = STAFFCD;
            $data1["UPDATED"][NUMBER]                 = "sysdate()";
        } else { //歯・口腔
            //「学校歯科医・日付」が設定されていないとき、「健康診断実施日付」をセットする
            if (!$data["DENTISTREMARKDATE"]) {
                $data["DENTISTREMARKDATE"]         = $data["TOOTH_DATE"];
            }

            $data1["YEAR"][TEXT]                   = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]               = $data["SCHREGNO"];
            $data1["JAWS_JOINTCD"][TEXT]           = $data["JAWS_JOINTCD"];
            $data1["JAWS_JOINTCD2"][TEXT]          = $data["JAWS_JOINTCD2"];
            $data1["PLAQUECD"][TEXT]               = $data["PLAQUECD"];
            $data1["GUMCD"][TEXT]                  = $data["GUMCD"];
            $data1["CALCULUS"][TEXT]               = $data["CALCULUS"];
            $data1["ORTHODONTICS"][TEXT]           = $data["ORTHODONTICS"];
            $data1["UP_R_BABY5"][TEXT]             = $data["UP_R_BABY5"];
            $data1["UP_R_BABY4"][TEXT]             = $data["UP_R_BABY4"];
            $data1["UP_R_BABY3"][TEXT]             = $data["UP_R_BABY3"];
            $data1["UP_R_BABY2"][TEXT]             = $data["UP_R_BABY2"];
            $data1["UP_R_BABY1"][TEXT]             = $data["UP_R_BABY1"];
            $data1["UP_L_BABY1"][TEXT]             = $data["UP_L_BABY1"];
            $data1["UP_L_BABY2"][TEXT]             = $data["UP_L_BABY2"];
            $data1["UP_L_BABY3"][TEXT]             = $data["UP_L_BABY3"];
            $data1["UP_L_BABY4"][TEXT]             = $data["UP_L_BABY4"];
            $data1["UP_L_BABY5"][TEXT]             = $data["UP_L_BABY5"];
            $data1["LW_R_BABY5"][TEXT]             = $data["LW_R_BABY5"];
            $data1["LW_R_BABY4"][TEXT]             = $data["LW_R_BABY4"];
            $data1["LW_R_BABY3"][TEXT]             = $data["LW_R_BABY3"];
            $data1["LW_R_BABY2"][TEXT]             = $data["LW_R_BABY2"];
            $data1["LW_R_BABY1"][TEXT]             = $data["LW_R_BABY1"];
            $data1["LW_L_BABY1"][TEXT]             = $data["LW_L_BABY1"];
            $data1["LW_L_BABY2"][TEXT]             = $data["LW_L_BABY2"];
            $data1["LW_L_BABY3"][TEXT]             = $data["LW_L_BABY3"];
            $data1["LW_L_BABY4"][TEXT]             = $data["LW_L_BABY4"];
            $data1["LW_L_BABY5"][TEXT]             = $data["LW_L_BABY5"];
            $data1["BABYTOOTH"][NUMBER]            = $data["BABYTOOTH"];
            $data1["REMAINBABYTOOTH"][NUMBER]      = $data["REMAINBABYTOOTH"];
            $data1["TREATEDBABYTOOTH"][NUMBER]     = $data["TREATEDBABYTOOTH"];
            if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
                $data1["BRACK_BABYTOOTH"][NUMBER]  = $toothData["BRACK_BABYTOOTH"];
            } else {
                $data1["BRACK_BABYTOOTH"][NUMBER]  = $data["BRACK_BABYTOOTH"];
            }
            $data1["UP_R_ADULT8"][TEXT]            = $data["UP_R_ADULT8"];
            $data1["UP_R_ADULT7"][TEXT]            = $data["UP_R_ADULT7"];
            $data1["UP_R_ADULT6"][TEXT]            = $data["UP_R_ADULT6"];
            $data1["UP_R_ADULT5"][TEXT]            = $data["UP_R_ADULT5"];
            $data1["UP_R_ADULT4"][TEXT]            = $data["UP_R_ADULT4"];
            $data1["UP_R_ADULT3"][TEXT]            = $data["UP_R_ADULT3"];
            $data1["UP_R_ADULT2"][TEXT]            = $data["UP_R_ADULT2"];
            $data1["UP_R_ADULT1"][TEXT]            = $data["UP_R_ADULT1"];
            $data1["UP_L_ADULT1"][TEXT]            = $data["UP_L_ADULT1"];
            $data1["UP_L_ADULT2"][TEXT]            = $data["UP_L_ADULT2"];
            $data1["UP_L_ADULT3"][TEXT]            = $data["UP_L_ADULT3"];
            $data1["UP_L_ADULT4"][TEXT]            = $data["UP_L_ADULT4"];
            $data1["UP_L_ADULT5"][TEXT]            = $data["UP_L_ADULT5"];
            $data1["UP_L_ADULT6"][TEXT]            = $data["UP_L_ADULT6"];
            $data1["UP_L_ADULT7"][TEXT]            = $data["UP_L_ADULT7"];
            $data1["UP_L_ADULT8"][TEXT]            = $data["UP_L_ADULT8"];
            $data1["LW_R_ADULT8"][TEXT]            = $data["LW_R_ADULT8"];
            $data1["LW_R_ADULT7"][TEXT]            = $data["LW_R_ADULT7"];
            $data1["LW_R_ADULT6"][TEXT]            = $data["LW_R_ADULT6"];
            $data1["LW_R_ADULT5"][TEXT]            = $data["LW_R_ADULT5"];
            $data1["LW_R_ADULT4"][TEXT]            = $data["LW_R_ADULT4"];
            $data1["LW_R_ADULT3"][TEXT]            = $data["LW_R_ADULT3"];
            $data1["LW_R_ADULT2"][TEXT]            = $data["LW_R_ADULT2"];
            $data1["LW_R_ADULT1"][TEXT]            = $data["LW_R_ADULT1"];
            $data1["LW_L_ADULT1"][TEXT]            = $data["LW_L_ADULT1"];
            $data1["LW_L_ADULT2"][TEXT]            = $data["LW_L_ADULT2"];
            $data1["LW_L_ADULT3"][TEXT]            = $data["LW_L_ADULT3"];
            $data1["LW_L_ADULT4"][TEXT]            = $data["LW_L_ADULT4"];
            $data1["LW_L_ADULT5"][TEXT]            = $data["LW_L_ADULT5"];
            $data1["LW_L_ADULT6"][TEXT]            = $data["LW_L_ADULT6"];
            $data1["LW_L_ADULT7"][TEXT]            = $data["LW_L_ADULT7"];
            $data1["LW_L_ADULT8"][TEXT]            = $data["LW_L_ADULT8"];
            $data1["ADULTTOOTH"][NUMBER]           = $data["ADULTTOOTH"];
            $data1["REMAINADULTTOOTH"][NUMBER]     = $data["REMAINADULTTOOTH"];
            $data1["TREATEDADULTTOOTH"][NUMBER]    = $data["TREATEDADULTTOOTH"];
            $data1["LOSTADULTTOOTH"][NUMBER]       = $data["LOSTADULTTOOTH"];
            if ($model->Properties["printKenkouSindanIppan"] == "2" || $model->Properties["printKenkouSindanIppan"] == "3") {
                $data1["BRACK_ADULTTOOTH"][NUMBER] = $toothData["BRACK_ADULTTOOTH"];
            } else {
                $data1["BRACK_ADULTTOOTH"][NUMBER] = $data["BRACK_ADULTTOOTH"];
            }
            $data1["OTHERDISEASECD"][TEXT]         = $data["OTHERDISEASECD"];
            if (in_array($data["OTHERDISEASECD"], $disableCodes["F530"]) == false && $data["OTHERDISEASECD"] != '') {
                $data1["OTHERDISEASE"][TEXT]       = $data["OTHERDISEASE"];    //その他疾病及び異常
            }
            $data1["DENTISTREMARKCD"][TEXT]        = $data["DENTISTREMARKCD"];
            if (in_array($data["DENTISTREMARKCD"], $disableCodes["F540"]) == false && $data["DENTISTREMARKCD"] != '') {
                $data1["DENTISTREMARK"][TEXT]      = $data["DENTISTREMARK"];  //学校歯科医所見1
            }
            $data1["DENTISTREMARKDATE"][TEXT]      = $data["DENTISTREMARKDATE"];
            $data1["DENTISTTREATCD"][TEXT]         = $data["DENTISTTREATCD"];
            if (in_array($data["DENTISTTREATCD"], $disableCodes["F541"]) == false && $data["DENTISTTREATCD"] != '') {
                $data1["DENTISTTREAT"][TEXT]       = $data["DENTISTTREAT"];   //学校歯科医事後処置1
            }
            $data1["REGISTERCD"][TEXT]             = STAFFCD;
            $data1["UPDATED"][NUMBER]              = "sysdate()";
        }
        $query = Query::insertSQL($data1, $table);

        $db->query($query);

        if ($csvcd == "1") { //一般
            $query  = " DELETE FROM ";
            $query .= "     MEDEXAM_DET_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$data["YEAR"]}' ";
            $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
            $query .= " AND DET_SEQ <> '010' ";
            $db->query($query);

            //DET_SEQ = 002
            $data1 = array();
            $data1["YEAR"][TEXT]        = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
            $data1["DET_SEQ"][TEXT]     = "002";
            $data1["DET_REMARK7"][TEXT] = $data["EYEDISEASECD5"];
            if (in_array($data["EYEDISEASECD5"], $disableCodes["F051"]) == false && $data["EYEDISEASECD5"] != '') {
                $data1["DET_REMARK8"][TEXT] = $data["VISION_CANTMEASURE"];  //色覚異常所見
            }
            $data1["REGISTERCD"][TEXT]  = STAFFCD;
            $data1["UPDATED"][NUMBER]   = "sysdate()";
            $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
            $db->query($query);

            //DET_SEQ = 003
            //耳鼻咽頭疾患（全般）が「00:未受検」「01:異常なし」「未選択」以外
            if ($data["NOSEDISEASECD"] != "00" && $data["NOSEDISEASECD"] != "01" && $data["NOSEDISEASECD"] != '') {
                $data1 = array();
                $data1["YEAR"][TEXT]            = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]        = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]         = "003";
                $data1["DET_REMARK7"][TEXT]     = $data["NOSEDISEASECD5"];  //耳鼻咽頭疾患（疾患1）
                //耳鼻咽頭疾患（疾患1）の所見欄が有効時
                if (in_array($data["NOSEDISEASECD5"], $disableCodes["F061"]) == false && $data["NOSEDISEASECD5"] != '') {
                    $data1["DET_REMARK1"][TEXT] = $data["NOSEDISEASECD_REMARK1"];  //耳鼻咽頭疾患（疾患1）所見
                }
                $data1["DET_REMARK8"][TEXT] = $data["NOSEDISEASECD6"];  //耳鼻咽頭疾患（疾患2）
                //耳鼻咽頭疾患（疾患2）の所見欄が有効時
                if (in_array($data["NOSEDISEASECD6"], $disableCodes["F061"]) == false && $data["NOSEDISEASECD6"] != '') {
                    $data1["DET_REMARK2"][TEXT]  = $data["NOSEDISEASECD_REMARK2"];  //耳鼻咽頭疾患（疾患2）所見
                }
                $data1["DET_REMARK9"][TEXT] = $data["NOSEDISEASECD7"];  //耳鼻咽頭疾患（疾患3）
                //耳鼻咽頭疾患（疾患3）の所見欄が有効時
                if (in_array($data["NOSEDISEASECD7"], $disableCodes["F061"]) == false && $data["NOSEDISEASECD7"] != '') {
                    $data1["DET_REMARK3"][TEXT] = $data["NOSEDISEASECD_REMARK3"];  //耳鼻咽頭疾患（疾患3）所見
                }
                $data1["REGISTERCD"][TEXT]      = STAFFCD;
                $data1["UPDATED"][NUMBER]       = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            //DET_SEQ = 004
            $data1 = array();
            $data1["YEAR"][TEXT]        = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
            $data1["DET_SEQ"][TEXT]     = "004";
            if (in_array($data["TREATCD"], $disableCodes["F150"]) == false && $data["TREATCD"] != '') {
                $data1["DET_REMARK1"][TEXT] = $data["TREAT_REMARK1"];  //事後措置1所見
            }
            $data1["DET_REMARK4"][TEXT] = $data["TB_RE_EXAMINATION_DATE"];
            $data1["DET_REMARK5"][TEXT] = $data["TB_RE_EXAMINATION_RESULT"];
            $data1["DET_REMARK6"][TEXT] = $data["TB_RE_EXAMINATION_FILMNO"];
            $data1["REGISTERCD"][TEXT]  = STAFFCD;
            $data1["UPDATED"][NUMBER]   = "sysdate()";
            $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
            $db->query($query);

            //DET_SEQ = 005
            if (in_array($data["TB_NAMECD"], $disableCodes["F120"]) == false && $data["TB_NAMECD"] != '') {
                $data1 = array();
                $data1["YEAR"][TEXT]        = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]     = "005";
                $data1["DET_REMARK2"][TEXT] = $data["TB_NAME_REMARK1"];  //結核（その他の検査）疾病及び異常所見
                $data1["REGISTERCD"][TEXT]  = STAFFCD;
                $data1["UPDATED"][NUMBER]   = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            //DET_SEQ = 006
            if (in_array($data["SKINDISEASECD"], $disableCodes["F070"]) == false && $data["SKINDISEASECD"] != '') {
                $data1 = array();
                $data1["YEAR"][TEXT]        = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]     = "006";
                $data1["DET_REMARK1"][TEXT] = $data["SKINDISEASECD_REMARK"];  //皮膚疾患所見
                $data1["REGISTERCD"][TEXT]  = STAFFCD;
                $data1["UPDATED"][NUMBER]   = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            //DET_SEQ = 007
            $data1 = array();
            $data1["YEAR"][TEXT]        = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
            $data1["DET_SEQ"][TEXT]     = "007";
            $data1["DET_REMARK2"][TEXT] = $data["DETAILED_EXAMINATION"];
            $data1["REGISTERCD"][TEXT]  = STAFFCD;
            $data1["UPDATED"][NUMBER]   = "sysdate()";
            $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
            $db->query($query);

            //DET_SEQ = 009
            if (in_array($data["NUTRITIONCD"], $disableCodes["F030"]) == false && $data["NUTRITIONCD"] != '') {
                $data1 = array();
                $data1["YEAR"][TEXT]        = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]     = "009";
                $data1["DET_REMARK1"][TEXT] = $data["NUTRITIONCD_REMARK"];  //栄養状態所見所見
                $data1["REGISTERCD"][TEXT]  = STAFFCD;
                $data1["UPDATED"][NUMBER]   = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            //DET_SEQ = 011
            //事後措置1が「01:なし」「未選択」以外
            if ($data["TREATCD"] != "01" && $data["TREATCD"] != '') {
                $data1 = array();
                $data1["YEAR"][TEXT]        = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]     = "011";
                $data1["DET_REMARK1"][TEXT] = $data["TREATCD2"];
                $data1["REGISTERCD"][TEXT]  = STAFFCD;
                $data1["UPDATED"][NUMBER]   = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            //DET_SEQ = 012
            $s012Cnt = $db->getOne(knjf140jQuery::getDetailCnt($data["YEAR"], $data["SCHREGNO"], '012'));
            $data1 = array();
            $data1["YEAR"][TEXT]         = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]     = $data["SCHREGNO"];
            $data1["DET_SEQ"][TEXT]      = '012';
            $data1["DET_REMARK1"][TEXT]  = $data["R_VISION_CANTMEASURE"];    // 視力・測定困難（右）
            $data1["DET_REMARK2"][TEXT]  = $data["L_VISION_CANTMEASURE"];    // 視力・測定困難（左）
            $data1["DET_REMARK3"][TEXT]  = $data["R_EAR_CANTMEASURE"];       // 聴力・測定困難（右）
            $data1["DET_REMARK4"][TEXT]  = $data["L_EAR_CANTMEASURE"];       // 聴力・測定困難（左）
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

            //DET_SEQ = 013
            //事後措置1が「01:なし」「未選択」以外　かつ　事後措置2の所見欄が有効時
            if (($data["TREATCD"] != "01" && $data["TREATCD"] != '')
               && (in_array($data["TREATCD2"], $disableCodes["F151"]) == false && $data["TREATCD2"] != '')) {
                $data1 = array();
                $data1["YEAR"][TEXT]        = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]    = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]     = "013";
                $data1["DET_REMARK2"][TEXT] = $data["TREATCD2_REMARK1"];  //事後措置2所見
                $data1["REGISTERCD"][TEXT]  = STAFFCD;
                $data1["UPDATED"][NUMBER]   = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }

            //DET_SEQ = 014
            //脊柱胸郭四肢（全般）が「00:未受検」「01:異常なし」「未選択」以外
            if ($data["SPINERIBCD"] != "00" && $data["SPINERIBCD"] != "01" && $data["SPINERIBCD"] != '') {
                $data1 = array();
                $data1["YEAR"][TEXT]            = $data["YEAR"];
                $data1["SCHREGNO"][TEXT]        = $data["SCHREGNO"];
                $data1["DET_SEQ"][TEXT]         = "014";
                $data1["DET_REMARK1"][TEXT]     = $data["SPINERIBCD1"];  //脊柱・胸郭・四肢（疾患1）
                if (in_array($data["SPINERIBCD1"], $disableCodes["F041"]) == false && $data["SPINERIBCD1"] != '') {
                    $data1["DET_REMARK2"][TEXT] = $data["SPINERIBCD_REMARK1"];  //脊柱・胸郭・四肢（疾患1）所見
                }
                $data1["DET_REMARK3"][TEXT] = $data["SPINERIBCD2"];  //脊柱・胸郭・四肢（疾患2）
                if (in_array($data["SPINERIBCD2"], $disableCodes["F041"]) == false && $data["SPINERIBCD2"] != '') {
                    $data1["DET_REMARK4"][TEXT] = $data["SPINERIBCD_REMARK2"];  //脊柱・胸郭・四肢（疾患2）所見
                }
                $data1["DET_REMARK5"][TEXT] = $data["SPINERIBCD3"];  //脊柱・胸郭・四肢（疾患3）
                if (in_array($data["SPINERIBCD3"], $disableCodes["F041"]) == false && $data["SPINERIBCD3"] != '') {
                    $data1["DET_REMARK6"][TEXT] = $data["SPINERIBCD_REMARK3"];  //脊柱・胸郭・四肢（疾患3）所見
                }
                $data1["REGISTERCD"][TEXT]      = STAFFCD;
                $data1["UPDATED"][NUMBER]       = "sysdate()";
                $query = Query::insertSQL($data1, "MEDEXAM_DET_DETAIL_DAT");
                $db->query($query);
            }
        }

        if ($csvcd == "2") { //歯・口腔
            $query  = " DELETE FROM ";
            $query .= "     MEDEXAM_TOOTH_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '{$data["YEAR"]}' ";
            $query .= " AND SCHREGNO = '{$data["SCHREGNO"]}' ";
            $db->query($query);

            //TOOTH_SEQ = 002
            $data1 = array();
            $data1["YEAR"][TEXT]                = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]            = $data["SCHREGNO"];
            $data1["TOOTH_SEQ"][TEXT]           = "002";
            $data1["TOOTH_REMARK1"][TEXT]       = $data["OTHERDISEASECD2"];
            if (in_array($data["OTHERDISEASECD2"], $disableCodes["F531"]) == false && $data["OTHERDISEASECD2"] != '') {
                $data1["TOOTH_REMARK2"][TEXT]   = $data["OTHERDISEASE2"];  //口腔の疾病及び異常所見
            }
            $data1["REGISTERCD"][TEXT]          = STAFFCD;
            $data1["UPDATED"][NUMBER]           = "sysdate()";
            $query = Query::insertSQL($data1, "MEDEXAM_TOOTH_DETAIL_DAT");
            $db->query($query);

            //TOOTH_SEQ = 005
            $data1 = array();
            $data1["YEAR"][TEXT]                      = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]                  = $data["SCHREGNO"];
            $data1["TOOTH_SEQ"][TEXT]                 = "005";
            //所見1が「00:未受検」「未選択」以外
            if ($data["DENTISTREMARKCD"] != "00" && $data["DENTISTREMARKCD"] != '') {
                $data1["TOOTH_REMARK1"][TEXT]         = $data["DENTISTREMARKCD2"];
                if (in_array($data["DENTISTREMARKCD2"], $disableCodes["F540"]) == false && $data["DENTISTREMARKCD2"] != '') {
                    $data1["TOOTH_REMARK2"][TEXT]     = $data["DENTISTREMARK2"];  //所見2
                }
                //所見2が「00:未受検」「未選択」以外
                if ($data["DENTISTREMARKCD2"] != "00" && $data["DENTISTREMARKCD2"] != '') {
                    $data1["TOOTH_REMARK3"][TEXT]     = $data["DENTISTREMARKCD3"];
                    if (in_array($data["DENTISTREMARKCD3"], $disableCodes["F540"]) == false && $data["DENTISTREMARKCD3"] != '') {
                        $data1["TOOTH_REMARK4"][TEXT] = $data["DENTISTREMARK3"];  //所見3
                    }
                }
            }
            //事後措置1が「01:なし」「未選択」以外
            if ($data["DENTISTTREATCD"] != "01" && $data["DENTISTTREATCD"] != '') {
                $data1["TOOTH_REMARK5"][TEXT]         = $data["DENTISTTREATCD2"];
                if (in_array($data["DENTISTTREATCD2"], $disableCodes["F541"]) == false && $data["DENTISTTREATCD2"] != '') {
                    $data1["TOOTH_REMARK6"][TEXT]     = $data["DENTISTTREAT2"];  //事後措置2所見
                }
            }
            $data1["REGISTERCD"][TEXT]                = STAFFCD;
            $data1["UPDATED"][NUMBER]                 = "sysdate()";
            $query = Query::insertSQL($data1, "MEDEXAM_TOOTH_DETAIL_DAT");
            $db->query($query);
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

    //名称マスタ(所見欄を無効にするNAMECD2を取得)
    public function getNameMstDisableCodes($model, $setInNamecd1, $year)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "        YEAR       = '{$year}' ";
        $query .= "    AND NAMECD1    = '{$setInNamecd1}' ";
        $query .= "    AND NAMESPARE2 = '1' ";

        return $query;
    }
}
