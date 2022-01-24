<?php

require_once('for_php7.php');

class knje460_sien_kikanQuery extends Query {

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
        $query .= "     YEAR = '".$model->field5["YEAR"]."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO ";
        $query .= " ), MXSCHADDR AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MAX(T1.ISSUEDATE) AS ISSUEDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT T1 ";
        $query .= "     LEFT JOIN (SELECT SCHREGNO, MAX(GRADE) AS GRADE FROM SCHREG_REGD_DAT WHERE YEAR = '".$model->field5["YEAR"]."' GROUP BY SCHREGNO) T3 ";
        $query .= "       ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = '".$model->field5["YEAR"]."' ";
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
        $query .= "   VSM.SCHOOLNAME1 AS SCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "   MXSCHREG T2WK ";
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
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT A023_2_G ";
        $query .= "     ON A023_2_G.YEAR = T2.YEAR ";
        $query .= "    AND A023_2_G.GRADE = A023.NAME2 ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT A023_3_G ";
        $query .= "     ON A023_3_G.YEAR = T2.YEAR ";
        $query .= "    AND A023_3_G.GRADE = VALUE(A023.NAMESPARE2, A023.NAME3) ";
        $query .= " WHERE ";
        $query .= "    T2WK.YEAR = '".$model->field5["YEAR"]."' ";
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
        $query .= "     YEAR = '".($pastyear == "1" ? $model->field5["PASTYEAR"] : $model->field5["YEAR"])."' ";
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
        $query .= "     YEAR < '".$model->field5["YEAR"]."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND SPRT_DIV = '".$div."' ";
        $query .= "     AND SPRT_SEQ = '".$seq."' ";
        $query .= " ORDER BY YEAR DESC ";
        return $query;
    }

    //記入者名を取得
    function getStaffName($db, $model)
    {
        $query  = "  SELECT ";
        $query .= "      T1.STAFFNAME ";
        $query .= "  FROM ";
        $query .= "      STAFF_MST T1 ";
        $query .= "      INNER JOIN SCHREG_CHALLENGED_SUPPORTPLAN_DAT T2 ";
        $query .= "              ON T2.YEAR     = '".$model->field5["YEAR"]."' ";
        $query .= "             AND T2.SCHREGNO = '".$model->schregno."' ";
        $query .= "             AND T2.SPRT_DIV = '01' ";
        $query .= "             AND T2.SPRT_SEQ = '03' ";
        $query .= "             AND T2.REMARK   = T1.STAFFCD ";
        $getDate = $db->getOne($query);
        return $getDate;
    }

    //関係機関コンボを取得
    function getSienKikan($model, $facilityCd, $count="", $pastyear="")
    {
        $query  = "  SELECT ";
        if($count == "COUNT"){
            $query .= "     COUNT(T1.SPRT_FACILITY_CD) AS CNT ";
        } else  {
            $query .= "     T1.SPRT_FACILITY_CD   AS VALUE, ";
            $query .= "     T1.SPRT_FACILITY_NAME AS LABEL ";
        }
        $query .= "  FROM ";
        $query .= "      CHALLENGED_SUPPORT_FACILITY_YMST T1 ";
        $query .= "      INNER JOIN SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT T2 ";
        $query .= "              ON T2.YEAR     = T1.YEAR ";
        $query .= "             AND T2.SCHREGNO = '".$model->schregno."' ";
        $query .= "      INNER JOIN CHALLENGED_SUPPORT_FACILITY_GRP_MST T3 ";
        $query .= "              ON T3.YEAR              = T2.YEAR ";
        $query .= "             AND T3.SPRT_FACILITY_GRP = T2.SPRT_FACILITY_GRP ";
        $query .= "      INNER JOIN CHALLENGED_SUPPORT_FACILITY_GRP_DAT T4 ";
        $query .= "              ON T4.YEAR              = T3.YEAR ";
        $query .= "             AND T4.SPRT_FACILITY_GRP = T3.SPRT_FACILITY_GRP ";
        $query .= "             AND T4.SPRT_FACILITY_CD  = T1.SPRT_FACILITY_CD ";
        $query .= "  WHERE ";
        $query .= "     T1.YEAR = '".($pastyear == "1" ? $model->field5["PASTYEAR"] : $model->field5["YEAR"])."' ";
        if($facilityCd != "ALL"){
            $query .= "     AND T1.SPRT_FACILITY_CD = '".$facilityCd."' ";
        }
        if(!$count){
            $query .= "  ORDER BY ";
            $query .= "      VALUE ";
        }


        return $query;
    }

    //名称マスタ
    function getNameMst($nameCd1, $nameCd2="")
    {
        $query  = "  SELECT ";
        $query .= "      NAMECD2 AS VALUE, ";
        $query .= "      NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= "  FROM ";
        $query .= "      NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      NAMECD1 = '".$nameCd1."' ";
        if ($nameCd2) {
            $query .= "  AND NAMECD2 = '".$nameCd2."' ";
        }
        $query .= "  ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }
    
    //当該生徒の全支援内容を取得
    function getSienKikanAll($model, $count="")
    {
        $query  = " SELECT ";
        if($count == "COUNT"){
            $query .= "     COUNT(T1.SPRT_FACILITY_CD) AS CNT ";
        } else  {
            $query .= "    T3.SPRT_FACILITY_GRP, ";
            $query .= "    T1.SPRT_FACILITY_CD, ";
            $query .= "    T1.SPRT_FACILITY_NAME, ";
            $query .= "    T5.SEQ, ";
            $query .= "    T5.REMARK ";
        }
        $query .= " FROM ";
        $query .= "     CHALLENGED_SUPPORT_FACILITY_YMST T1 ";
        $query .= "     INNER JOIN SCHREG_CHALLENGED_SUPPORT_FACILITY_GRP_DAT T2 ";
        $query .= "             ON T2.YEAR     = T1.YEAR ";
        $query .= "             AND T2.SCHREGNO = '".$model->schregno."' ";
        $query .= "     INNER JOIN CHALLENGED_SUPPORT_FACILITY_GRP_MST T3 ";
        $query .= "             ON T3.YEAR              = T2.YEAR ";
        $query .= "            AND T3.SPRT_FACILITY_GRP = T2.SPRT_FACILITY_GRP ";
        $query .= "     INNER JOIN CHALLENGED_SUPPORT_FACILITY_GRP_DAT T4 ";
        $query .= "             ON T4.YEAR              = T3.YEAR ";
        $query .= "            AND T4.SPRT_FACILITY_GRP = T3.SPRT_FACILITY_GRP ";
        $query .= "            AND T4.SPRT_FACILITY_CD  = T1.SPRT_FACILITY_CD ";
        $query .= "     LEFT JOIN SCHREG_CHALLENGED_SUPPORTPLAN_FACILITY_DAT T5 ";
        $query .= "            ON T5.YEAR              = T1.YEAR ";
        $query .= "           AND T5.SPRT_FACILITY_CD  = T1.SPRT_FACILITY_CD ";
        $query .= "           AND T5.SCHREGNO          = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field5["YEAR"]."' ";
        if(!$count){
            $query .= " ORDER BY ";
            $query .= "     SPRT_FACILITY_GRP, SPRT_FACILITY_CD, SEQ ";
        }



        return $query;
    }

    //SCHREG_CHALLENGED_SUPPORTPLAN_FACILITY_DATを取得
    function getSchregChallengedSupportplanFacilityDat($db, $model, $sienKikan, $seq, $field, $pastyear="")
    {
        $query  = " SELECT ";
        if($field == "COUNT"){
            $query .= "     COUNT(*) AS CNT ";
        } else  {
            $query .= "     ".$field." ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_FACILITY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                 = '".($pastyear == "1" ? $model->field5["PASTYEAR"] : $model->field5["YEAR"])."' ";
        $query .= "     AND SCHREGNO         = '".$model->schregno."' ";
        $query .= "     AND SPRT_FACILITY_CD = '".$sienKikan."' ";
        $query .= "     AND SEQ              = '".$seq."' ";
        $getDate = $db->getOne($query);
        return $getDate;
    }

    //SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DATを取得
    function getSchregChallengedSupportplanMainDat($db, $model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field5["YEAR"]."' ";
        $query .= "     AND SCHREGNO    = '".$model->schregno."' ";
        $query .= "     AND RECORD_DATE = '".$model->field5["YEAR"]."' ";
        $getDate = $db->getOne($query);
        return $getDate;
    }

/*************** 更新処理 ***************/

    //UPDATE (SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT)
    function getUpdateMainDat($db, $model)
    {
        //データ有無チェック
        $getCount = knje460_sien_kikanQuery::getSchregChallengedSupportplanMainDat($db, $model);

        $data = array();
        $data["YEAR"][TEXT]         = $model->field5["YEAR"];
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["RECORD_DATE"][TEXT]  = $model->field5["YEAR"];
        $data["WRITING_DATE"][DATE] = CTRL_DATE;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";
        

        //更新処理
        if ($getCount > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->field5["YEAR"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";
            $where .= "     AND RECORD_DATE = '".$model->field5["YEAR"]."' ";
            $db->query(Query::updateSQL($data, "SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT", $where));
        } else {
            $db->query(Query::insertSQL($data, "SCHREG_CHALLENGED_SUPPORTPLAN_MAIN_DAT"));
        }

        return;
    }

    //DELETE (SCHREG_CHALLENGED_SUPPORTPLAN_FACILITY_DAT)
    function getDeleteMainQuery($db, $model, $sienKikan)
    {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_CHALLENGED_SUPPORTPLAN_FACILITY_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR                 = '".$model->field5["YEAR"]."' ";
        $query .= "     AND SCHREGNO         = '".$model->schregno."' ";
        $query .= "     AND SPRT_FACILITY_CD = '".$sienKikan."' ";

        $db->query($query);
    }

    //INSERT (SCHREG_CHALLENGED_SUPPORTPLAN_FACILITY_DAT)
    function getUpdateMainQuery($db, $model, $sienKikan, $seq, $remakr, $status)
    {
        $data = array();
        $data["YEAR"][TEXT]             = $model->field5["YEAR"];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["SPRT_FACILITY_CD"][TEXT] = $sienKikan;
        $data["SEQ"][TEXT]              = $seq;
        $data["REMARK"][TEXT]           = $remakr;
        $data["STATUS"][TEXT]           = $status;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        //更新処理
        $db->query(Query::insertSQL($data, "SCHREG_CHALLENGED_SUPPORTPLAN_FACILITY_DAT"));

        return;
    }

    //UPDATE (SCHREG_CHALLENGED_SUPPORTPLAN_DAT)
    function getUpdateSupportPlan($db, $model, $div, $seq, $remakr)
    {
        //データ有無チェック
        $getCount = knje460_sien_kikanQuery::getSchregChallengedSupportplanDat($db, $model, $div, $seq, "COUNT");

        $data = array();
        $data["YEAR"][TEXT]         = $model->field5["YEAR"];
        $data["SCHREGNO"][TEXT]     = $model->schregno;
        $data["SPRT_DIV"][TEXT]     = $div;
        $data["SPRT_SEQ"][TEXT]     = $seq;
        $data["REMARK"][TEXT]       = $remakr;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        //更新処理
        if ($getCount > 0) {
            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->field5["YEAR"]."' ";
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
