<?php

require_once('for_php7.php');

class knje460_seikatu_zyouhouQuery extends Query {

    //更新年度コンボ
    function getYearCmb($model)
    {
        $query  = "  SELECT DISTINCT ";
        $query .= "      YEAR AS VALUE, ";
        $query .= "      YEAR AS LABEL ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_DAT ";
        $query .= "  WHERE ";
        $query .= "      SCHREGNO = '".$model->schregno."' ";
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //メインの取得
    function getMainQuery($model)
    {
        $query .= " WITH MXSCHREG AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     MAX(SEMESTER) AS SEMESTER, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field1["YEAR"]."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ), MXSCHADDR AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MAX(T1.ISSUEDATE) AS ISSUEDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT T1 ";
        $query .= "     LEFT JOIN (SELECT SCHREGNO, MAX(GRADE) AS GRADE FROM SCHREG_REGD_DAT WHERE YEAR = '".$model->field1["YEAR"]."' GROUP BY SCHREGNO) T3 ";
        $query .= "       ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = '".$model->field1["YEAR"]."' ";
        $query .= "      AND GDAT.GRADE = T3.GRADE ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "      AND T2.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND (T1.EXPIREDATE IS NULL OR T2.ENT_DATE < T1.EXPIREDATE) AND T1.ISSUEDATE <= '".CTRL_DATE."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T2WK.YEAR, ";
        $query .= "   A023.ABBV1 AS FACULTY_NAME, ";
        $query .= "   A023_2_G.GRADE_CD AS SCHOOL_KIND_MIN_GRADE_CD, ";
        $query .= "   A023_3_G.GRADE_CD AS SCHOOL_KIND_MAX_GRADE_CD, ";
        $query .= "   T5.SCHOOL_KIND, ";
        $query .= "   T5.GRADE_CD, ";
        $query .= "   T5.GRADE_NAME1 AS GRADE, ";
        $query .= "   T3.NAME_KANA AS KANA, ";
        $query .= "   T3.NAME, ";
        $query .= "   Z002.ABBV1 AS SEX, ";
        $query .= "   T3.BIRTHDAY, ";
        $query .= "   VALUE(T6.ADDR1, '') || VALUE(T6.ADDR2, '') AS ADDR, ";
        $query .= "   T6.TELNO, ";
        $query .= "   VSM.SCHOOLNAME1 AS SCHOOL_NAME, ";
        $query .= "   PRF.CHALLENGED_CARD_NAME, ";
        $query .= "   PRF.CHALLENGED_CARD_CLASS, ";
        $query .= "   PRF.CHALLENGED_CARD_RANK, ";
        $query .= "   PRF.CHALLENGED_CARD_AREA_NAME, ";
        $query .= "   PRF.CHALLENGED_CARD_AREA_NAME2, ";
        $query .= "   PRF.CHALLENGED_CARD_AREA_NAME3, ";
        $query .= "   PRF.CHALLENGED_CARD_REMARK, ";
        $query .= "   PRF.CHALLENGED_CARD_CHECK_YM, ";
        $query .= "   PRF.CHALLENGED_CARD_GRANT_FLG, ";
        $query .= "   PRF.CHALLENGED_CARD_GRANT_YM, ";
        $query .= "   PRF.CHALLENGED_CARD_BAST_YM ";
        $query .= " FROM ";
        $query .= "   MXSCHREG T2WK ";
        $query .= "   LEFT JOIN SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT T1 ";
        $query .= "     ON T1.YEAR = T2WK.YEAR ";
        $query .= "    AND T1.SCHREGNO = T2WK.SCHREGNO ";
        $query .= "   INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "     ON T2.YEAR = T2WK.YEAR ";
        $query .= "    AND T2.SEMESTER = T2WK.SEMESTER ";
        $query .= "    AND T2.SCHREGNO = T2WK.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST T3 ";
        $query .= "     ON T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T5 ";
        $query .= "     ON T5.YEAR = T2.YEAR ";
        $query .= "    AND T5.GRADE = T2.GRADE ";
        $query .= "   LEFT JOIN SCHREG_ADDRESS_DAT T6 ";
        $query .= "     ON T6.SCHREGNO = T2WK.SCHREGNO ";
        $query .= "    AND T6.ISSUEDATE = (SELECT TW.ISSUEDATE FROM MXSCHADDR TW WHERE TW.SCHREGNO = T6.SCHREGNO) ";
        $query .= "   LEFT JOIN NAME_MST A023 ";
        $query .= "     ON A023.NAMECD1 = 'A023' ";
        $query .= "    AND A023.NAME1 = T5.SCHOOL_KIND ";
        $query .= "   LEFT JOIN NAME_MST Z002 ";
        $query .= "     ON Z002.NAMECD1 = 'Z002' ";
        $query .= "    AND Z002.NAMECD2 = T3.SEX ";
        $query .= "   LEFT JOIN V_SCHOOL_MST VSM ";
        $query .= "     ON VSM.YEAR        = T2.YEAR ";
        $query .= "    AND VSM.SCHOOL_KIND = T5.SCHOOL_KIND ";
        $query .= "   LEFT JOIN SCHREG_CHALLENGED_PROFILE_MAIN_DAT PRF ";
        $query .= "     ON PRF.SCHREGNO    = T2WK.SCHREGNO ";
        $query .= "    AND PRF.RECORD_DATE = '".$model->record_date."' ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT A023_2_G ";
        $query .= "     ON A023_2_G.YEAR = T2.YEAR ";
        $query .= "    AND A023_2_G.GRADE = A023.NAME2 ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT A023_3_G ";
        $query .= "     ON A023_3_G.YEAR = T2.YEAR ";
        $query .= "    AND A023_3_G.GRADE = VALUE(A023.NAMESPARE2, A023.NAME3) ";
        $query .= " WHERE ";
        $query .= "    T2WK.YEAR = '".$model->field1["YEAR"]."' ";
        $query .= "    AND T2WK.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //SCHREG_CHALLENGED_SUPPORTPLAN_DATを取得
    function getSchregChallengedSupportplanDat($db, $model, $div, $seq, $field, $pastyear="")
    {
        $query  = " SELECT ";
        if($field == "COUNT"){
            $query .= "     COUNT(*) AS CNT ";
        } else  {
            $query .= "     ".$field." ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($pastyear == "1" ? $model->field1["PASTYEAR"] : $model->field1["YEAR"])."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND SPRT_DIV = '".$div."' ";
        $query .= "     AND SPRT_SEQ = '".$seq."' ";
        $getDate = $db->getOne($query);
        return $getDate;
    }

    //過年度データの年度を取得
    function getPastYearCmb($db, $model, $div, $seq) {
        $query  = " SELECT ";
        $query .= "   YEAR || '年度' AS LABEL, ";
        $query .= "   YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR < '".$model->field1["YEAR"]."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND SPRT_DIV = '".$div."' ";
        $query .= "     AND SPRT_SEQ = '".$seq."' ";
        $query .= " ORDER BY YEAR DESC ";
        return $query;
    }

    //スタッフマスタ
    function getStaffMst($staffCd="")
    {
        $query  = "  SELECT ";
        if ($staffCd) {
            $query .= "  STAFFNAME ";
        } else {
            $query .= "      STAFFCD AS VALUE, ";
            $query .= "      STAFFNAME AS LABEL ";
        }
        $query .= "  FROM ";
        $query .= "      STAFF_MST ";
        if ($staffCd) {
            $query .= "  WHERE ";
            $query .= "      STAFFCD = '".$staffCd."' ";
        } else {
            $query .= "  ORDER BY ";
            $query .= "      VALUE ";
        }

        return $query;
    }

    //名称マスタ
    function getNameMst($nameCd1, $nameCd2="", $dummy="")
    {
        $query  = "  SELECT ";
        if ($nameCd2 || $dummy) {
            $query .= "  NAME1 ";
        } else {
            $query .= "      NAMECD2 AS VALUE, ";
            if ($nameCd1 === 'E031' || $cd === 'E032' || $cd === 'E063') {
                $query .= "      NAME1 AS LABEL ";
            } else {
                $query .= "      NAMECD2 || ':' || NAME1 AS LABEL ";
            }
        }
        $query .= "  FROM ";
        $query .= "      NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      NAMECD1 = '".$nameCd1."' ";
        if ($nameCd2 || $dummy) {
            $query .= "  AND NAMECD2 = '".$nameCd2."' ";
        } else {
            $query .= "  ORDER BY ";
            $query .= "      VALUE ";
        }


        return $query;
    }

    //手帳の障害名を取得
    function getChallengedCardNameMst($cd="", $dummy="") {
        $query  = "  SELECT ";
        if ($cd || $dummy) {
            $query .= "      CARDNAME ";
        } else {
            $query .= "      CARDNAME_CD AS VALUE, ";
            $query .= "      CARDNAME_CD || ':' || CARDNAME AS LABEL ";
        }
        $query .= "  FROM  ";
        $query .= "      CHALLENGED_CARD_NAME_MST ";
        if ($cd || $dummy) {
            $query .= "  WHERE ";
            $query .= "      CARDNAME_CD = '".$cd."' ";
        } else {
            $query .= "  ORDER BY ";
            $query .= "      VALUE ";
        }

        return $query;
    }

    //指定年度(またはログイン年度)のRECORD_DATEのMAXを取得
    function getMaxRecordDataQuery($model)
    {
        $query  = "  WITH TMP AS ( ";
        $query .= "      SELECT ";
        $query .= "          1 AS RECDIV, MAX(RECORD_DATE) AS MAX_DATE ";
        $query .= "      FROM ";
        $query .= "          SCHREG_CHALLENGED_PROFILE_MAIN_DAT ";
        $query .= "      WHERE ";
        $query .= "          SCHREGNO    = '".$model->schregno."' ";
        $query .= "      AND RECORD_DATE <> 'NEW' ";
        $query .= "      UNION ";
        $query .= "      SELECT ";
        $query .= "          2 AS RECDIV, MAX(RECORD_DATE) AS MAX_DATE ";
        $query .= "      FROM ";
        $query .= "          SCHREG_CHALLENGED_PROFILE_MAIN_DAT ";
        $query .= "      WHERE ";
        $query .= "          SCHREGNO    = '".$model->schregno."' ";
        $query .= "      AND RECORD_DATE = 'NEW' ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      MAX_DATE ";
        $query .= "  FROM ";
        $query .= "      TMP ";
        $query .= "  WHERE ";
        $query .= "      RECDIV IN (SELECT MIN(RECDIV) FROM TMP WHERE MAX_DATE IS NOT NULL) ";

        return $query;
    }
    
    //SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DATを取得
    function getSchregChallengedSupportplanMainDat($db, $model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field1["YEAR"]."' ";
        $query .= "     AND SCHREGNO    = '".$model->schregno."' ";
        $query .= "     AND RECORD_DATE = '".$model->field1["YEAR"]."' ";
        $getDate = $db->getOne($query);
        return $getDate;
    }

/*************** 更新処理 ***************/

    //UPDATE (SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT)
    function getUpdateMainDat($db, $model)
    {
        //データ有無チェック
        $getCount = knje460_seikatu_zyouhouQuery::getSchregChallengedSupportplanMainDat($db, $model);

        $data = array();
        $data["YEAR"][TEXT]         = $model->field1["YEAR"];
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["RECORD_DATE"][TEXT]  = $model->field1["YEAR"];
        $data["WRITING_DATE"][DATE] = CTRL_DATE;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        

        //更新処理
        if ($getCount > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->field1["YEAR"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";
            $where .= "     AND RECORD_DATE = '".$model->field1["YEAR"]."' ";
            $db->query(Query::updateSQL($data, "SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT", $where));
        } else {
            $db->query(Query::insertSQL($data, "SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT"));
        }

        return;
    }

    //UPDATE (SCHREG_CHALLENGED_SUPPORTPLAN_DAT)
    function getUpdateMainQuery($db, $model, $div, $seq, $remakr)
    {
        //データ有無チェック
        $getCount = knje460_seikatu_zyouhouQuery::getSchregChallengedSupportplanDat($db, $model, $div, $seq, "COUNT");

        $data = array();
        $data["YEAR"][TEXT]         = $model->field1["YEAR"];
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["SPRT_DIV"][TEXT]     = $div;
        $data["SPRT_SEQ"][TEXT]     = $seq;
        $data["REMARK"][TEXT]       = $remakr;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        //更新処理
        if ($getCount > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->field1["YEAR"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";
            $where .= "     AND SPRT_DIV = '".$div."' ";
            $where .= "     AND SPRT_SEQ = '".$seq."' ";
            $db->query(Query::updateSQL($data, "SCHREG_CHALLENGED_SUPPORTPLAN_DAT", $where));
        } else {
            $db->query(Query::insertSQL($data, "SCHREG_CHALLENGED_SUPPORTPLAN_DAT"));
        }

        return;
    }

}
?>
