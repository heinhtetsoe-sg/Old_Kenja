<?php

require_once('for_php7.php');
class knjf140query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJF140' ";
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

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SEMESTER_MST T2 ON  T1.YEAR     = T2.YEAR ";
        $query .= "                     AND T1.SEMESTER = T2.SEMESTER ";
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
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '{$model->field["YEAR"]}' ";
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
            $query .= "     T2.YEAR, ";
            $query .= "     T2.DATE, ";
            $query .= "     T2.SCHREGNO, ";
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
            $query .= "     OTHERDISEASECD, ";
            $query .= "     OTHER_ADVISECD, ";
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
            $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
            $query .= " FROM MEDEXAM_DET_DAT T1, ";
        } else { //歯・口腔
            $query  = " SELECT ";
            $query .= "     '2' AS CSVCD, ";
            $query .= "     T2.YEAR, ";
            $query .= "     T2.TOOTH_DATE, ";
            $query .= "     T2.SCHREGNO, ";
            $query .= "     JAWS_JOINTCD, ";
            $query .= "     JAWS_JOINTCD2, ";
            $query .= "     PLAQUECD, ";
            $query .= "     GUMCD, ";
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
            $query .= "     DENTISTREMARKCD, ";
            $query .= "     DENTISTREMARK, ";
            $query .= "     DENTISTREMARKDATE, ";
            $query .= "     DENTISTTREAT, ";
            $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
            $query .= " FROM MEDEXAM_TOOTH_DAT T1, ";
        }

        $query .= "     MEDEXAM_HDAT T2, ";
        $query .= "     SCHREG_REGD_DAT T3 ";
        $query .= " WHERE T3.YEAR || T3.SEMESTER = '{$model->field["YEAR"]}' ";
        if ($model->field["GRADE_HR_CLASS"]!="") {
            $query .= "AND T3.GRADE || T3.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        }
        $query .= "    AND T3.YEAR     = T2.YEAR ";
        $query .= "    AND T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "    AND T1.YEAR     = T2.YEAR ";
        $query .= "    AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "ORDER BY T2.SCHREGNO";

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

    //マスタ(存在チェック用)
    public function getMasterCheck($data, $chk_no, $namecd1 = "", $namecd2 = "")
    {
        $flg   = true;
        $query = "";
        //条件選択
        switch ($chk_no) {
            case "1":
                $where = " NAME_MST WHERE NAMECD1 = '{$namecd1}' AND NAMECD2 = '{$namecd2}' ";
                break;
            case "2":
                $where  = "     SCHREG_REGD_DAT ";
                $where .= " WHERE ";
                $where .= "      SCHREGNO = '{$data["SCHREGNO"]}' ";
                $where .= "  AND YEAR   = '{$data["YEAR"]}' ";
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
        } else {
            $table_del = "MEDEXAM_TOOTH_DAT";
            $fieldName = "TOOTH_DATE";
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
    public function deleteinsertQueryCsv(&$db, $data, $csvcd)
    {
        $table = ($csvcd == "1") ? "MEDEXAM_DET_DAT" : "MEDEXAM_TOOTH_DAT";
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
            $data1["CHEST"][NUMBER]              = $data["CHEST"];
            $data1["R_BAREVISION"][TEXT]         = $data["R_BAREVISION"];
            $data1["R_BAREVISION_MARK"][TEXT]    = $data["R_BAREVISION_MARK"];
            $data1["L_BAREVISION"][TEXT]         = $data["L_BAREVISION"];
            $data1["L_BAREVISION_MARK"][TEXT]    = $data["L_BAREVISION_MARK"];
            $data1["R_VISION"][TEXT]             = $data["R_VISION"];
            $data1["R_VISION_MARK"][TEXT]        = $data["R_VISION_MARK"];
            $data1["L_VISION"][TEXT]             = $data["L_VISION"];
            $data1["L_VISION_MARK"][TEXT]        = $data["L_VISION_MARK"];
            $data1["R_EAR"][TEXT]                = $data["R_EAR"];
            $data1["R_EAR_DB"][NUMBER]           = $data["R_EAR_DB"];
            $data1["L_EAR"][TEXT]                = $data["L_EAR"];
            $data1["L_EAR_DB"][NUMBER]           = $data["L_EAR_DB"];
            $data1["ALBUMINURIA1CD"][TEXT]       = $data["ALBUMINURIA1CD"];
            $data1["URICSUGAR1CD"][TEXT]         = $data["URICSUGAR1CD"];
            $data1["URICBLEED1CD"][TEXT]         = $data["URICBLEED1CD"];
            $data1["ALBUMINURIA2CD"][TEXT]       = $data["ALBUMINURIA2CD"];
            $data1["URICSUGAR2CD"][TEXT]         = $data["URICSUGAR2CD"];
            $data1["URICBLEED2CD"][TEXT]         = $data["URICBLEED2CD"];
            $data1["URICOTHERTEST"][TEXT]        = $data["URICOTHERTEST"];
            $data1["URI_ADVISECD"][TEXT]         = $data["URI_ADVISECD"];
            $data1["NUTRITIONCD"][TEXT]          = $data["NUTRITIONCD"];
            $data1["SPINERIBCD"][TEXT]           = $data["SPINERIBCD"];
            $data1["SPINERIBCD_REMARK"][TEXT]    = $data["SPINERIBCD_REMARK"];
            $data1["EYEDISEASECD"][TEXT]         = $data["EYEDISEASECD"];
            $data1["EYE_TEST_RESULT"][TEXT]      = $data["EYE_TEST_RESULT"];
            $data1["NOSEDISEASECD"][TEXT]        = $data["NOSEDISEASECD"];
            $data1["NOSEDISEASECD_REMARK"][TEXT] = $data["NOSEDISEASECD_REMARK"];
            $data1["SKINDISEASECD"][TEXT]        = $data["SKINDISEASECD"];
            $data1["HEART_MEDEXAM"][TEXT]        = $data["HEART_MEDEXAM"];
            $data1["HEART_MEDEXAM_REMARK"][TEXT] = $data["HEART_MEDEXAM_REMARK"];
            $data1["HEARTDISEASECD"][TEXT]       = $data["HEARTDISEASECD"];
            $data1["HEARTDISEASECD_REMARK"][TEXT]= $data["HEARTDISEASECD_REMARK"];
            $data1["MANAGEMENT_DIV"][TEXT]       = $data["MANAGEMENT_DIV"];
            $data1["TB_DATE"][TEXT]              = $data["TB_DATE"];
            $data1["TB_REACT"][NUMBER]           = $data["TB_REACT"];
            $data1["TB_RESULT"][TEXT]            = $data["TB_RESULT"];
            $data1["TB_BCGDATE"][TEXT]           = $data["TB_BCGDATE"];
            $data1["TB_FILMDATE"][TEXT]          = $data["TB_FILMDATE"];
            $data1["TB_FILMNO"][TEXT]            = $data["TB_FILMNO"];
            $data1["TB_REMARKCD"][TEXT]          = $data["TB_REMARKCD"];
            $data1["TB_OTHERTESTCD"][TEXT]       = $data["TB_OTHERTESTCD"];
            $data1["TB_NAMECD"][TEXT]            = $data["TB_NAMECD"];
            $data1["TB_ADVISECD"][TEXT]          = $data["TB_ADVISECD"];
            $data1["TB_X_RAY"][TEXT]             = $data["TB_X_RAY"];
            $data1["ANEMIA_REMARK"][TEXT]        = $data["ANEMIA_REMARK"];
            $data1["HEMOGLOBIN"][NUMBER]         = $data["HEMOGLOBIN"];
            $data1["PARASITE"][TEXT]             = $data["PARASITE"];
            $data1["OTHERDISEASECD"][TEXT]       = $data["OTHERDISEASECD"];
            $data1["OTHER_ADVISECD"][TEXT]       = $data["OTHER_ADVISECD"];
            $data1["DOC_CD"][TEXT]               = $data["DOC_CD"];
            $data1["DOC_REMARK"][TEXT]           = $data["DOC_REMARK"];
            $data1["DOC_DATE"][TEXT]             = $data["DOC_DATE"];
            $data1["TREATCD"][TEXT]              = $data["TREATCD"];
            $data1["NUTRITION_RESULT"][TEXT]     = $data["NUTRITION_RESULT"];
            $data1["EYEDISEASE_RESULT"][TEXT]    = $data["EYEDISEASE_RESULT"];
            $data1["SKINDISEASE_RESULT"][TEXT]   = $data["SKINDISEASE_RESULT"];
            $data1["SPINERIB_RESULT"][TEXT]      = $data["SPINERIB_RESULT"];
            $data1["NOSEDISEASE_RESULT"][TEXT]   = $data["NOSEDISEASE_RESULT"];
            $data1["OTHERDISEASE_RESULT"][TEXT]  = $data["OTHERDISEASE_RESULT"];
            $data1["HEARTDISEASE_RESULT"][TEXT]  = $data["HEARTDISEASE_RESULT"];
            $data1["GUIDE_DIV"][TEXT]            = $data["GUIDE_DIV"];
            $data1["JOINING_SPORTS_CLUB"][TEXT]  = $data["JOINING_SPORTS_CLUB"];
            $data1["MEDICAL_HISTORY1"][TEXT]     = $data["MEDICAL_HISTORY1"];
            $data1["MEDICAL_HISTORY2"][TEXT]     = $data["MEDICAL_HISTORY2"];
            $data1["MEDICAL_HISTORY3"][TEXT]     = $data["MEDICAL_HISTORY3"];
            $data1["DIAGNOSIS_NAME"][TEXT]       = $data["DIAGNOSIS_NAME"];
            $data1["REGISTERCD"][TEXT]           = STAFFCD;
            $data1["UPDATED"][NUMBER]            = "sysdate()";
        } else { //歯・口腔
            $data1["YEAR"][TEXT]                 = $data["YEAR"];
            $data1["SCHREGNO"][TEXT]             = $data["SCHREGNO"];
            $data1["JAWS_JOINTCD"][TEXT]         = $data["JAWS_JOINTCD"];
            $data1["JAWS_JOINTCD2"][TEXT]        = $data["JAWS_JOINTCD2"];
            $data1["PLAQUECD"][TEXT]             = $data["PLAQUECD"];
            $data1["GUMCD"][TEXT]                = $data["GUMCD"];
            $data1["ORTHODONTICS"][TEXT]         = $data["ORTHODONTICS"];
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
            $data1["BRACK_BABYTOOTH"][NUMBER]    = $data["BRACK_BABYTOOTH"];
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
            $data1["BRACK_ADULTTOOTH"][NUMBER]   = $data["BRACK_ADULTTOOTH"];
            $data1["OTHERDISEASECD"][TEXT]       = $data["OTHERDISEASECD"];
            $data1["OTHERDISEASE"][TEXT]         = ($data["OTHERDISEASECD"] == '99') ? $data["OTHERDISEASE"] : "";
            $data1["DENTISTREMARKCD"][TEXT]      = $data["DENTISTREMARKCD"];
            $data1["DENTISTREMARK"][TEXT]        = $data["DENTISTREMARK"];
            $data1["DENTISTREMARKDATE"][TEXT]    = $data["DENTISTREMARKDATE"];
            $data1["DENTISTTREAT"][TEXT]         = $data["DENTISTTREAT"];
            $data1["REGISTERCD"][TEXT]           = STAFFCD;
            $data1["UPDATED"][NUMBER]            = "sysdate()";
        }

        $query = Query::insertSQL($data1, $table);

        $db->query($query);

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
            $data2["TOOTH_DATE"][TEXT] = $data["DATE"];
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
}
