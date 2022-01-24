<?php

require_once('for_php7.php');

class knjl060dQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象人数チェック
    function checkBaseCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' ";

        return $query;
    }

    //合計・平均・順位算出
    function getCulcQuery($model) {
        $query  = " WITH BASE AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "         APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "         TESTDIV         = '".$model->testdiv."' ";
        $query .= " ), CONFRPT AS ( ";
        $query .= "     SELECT ";
        $query .= "         EXAMNO, ";
        $query .= "         SUM(TOTAL) AS TOTAL, ";
        $query .= "         CASE WHEN SUM(TOTAL) IS NOT NULL THEN 0 ELSE 1 END AS FLG, ";
        $query .= "         ROUND(DECIMAL((SUM(TOTAL) * 100) / SUM(CNT),5,2) / 100, 1) AS AVG ";
        $query .= "     FROM (  SELECT ";
        $query .= "                 T1.EXAMNO, ";
        $query .= "                 '003' AS  SEQ, ";
        $add = "";
        for ($i = 1; $i <= 9; $i++) {
            $query .= "             ".$add." (CASE WHEN L1.CONFIDENTIAL_RPT0".$i." IS NOT NULL THEN 1 ELSE 0 END) ";
            $add = "+";
        }
        $query .= "                 AS CNT, ";
        $query .= "                 TOTAL_ALL AS TOTAL ";
        $query .= "             FROM ";
        $query .= "                 BASE T1 ";
        $query .= "                 LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= "                      ON T1.ENTEXAMYEAR  = L1.ENTEXAMYEAR ";
        $query .= "                     AND T1.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "                     AND T1.EXAMNO       = L1.EXAMNO ";
        $query .= "             UNION ";
        $query .= "             SELECT ";
        $query .= "                 T1.EXAMNO, ";
        $query .= "                 L1.SEQ, ";
        $add = "";
        for ($i = 1; $i <= 9; $i++) {
            $query .= "             ".$add." (CASE WHEN L1.REMARK".$i." IS NOT NULL THEN 1 ELSE 0 END) ";
            $add = "+";
        }
        $query .= "                 AS CNT, ";
        $query .= "                 INT(L1.REMARK11) AS TOTAL ";
        $query .= "             FROM ";
        $query .= "                 BASE T1 ";
        $query .= "                 LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT L1 ";
        $query .= "                      ON T1.ENTEXAMYEAR  = L1.ENTEXAMYEAR ";
        $query .= "                     AND T1.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "                     AND T1.EXAMNO       = L1.EXAMNO ";
        $query .= "                     AND L1.SEQ IN ('001', '002') ";
        $query .= "             ) T1 ";
        $query .= "     GROUP BY ";
        $query .= "         EXAMNO ";
        $query .= " ), SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         SUM(L1.SCORE) AS TOTAL, ";
        $query .= "         CASE WHEN SUM(L1.SCORE) IS NOT NULL THEN 0 ELSE 1 END AS FLG, ";
        $query .= "         ROUND(AVG(DECIMAL(L1.SCORE)),1) AS AVG ";
        $query .= "     FROM ";
        $query .= "         BASE T1 ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT L1 ";
        $query .= "              ON T1.ENTEXAMYEAR  = L1.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "             AND T1.TESTDIV      = L1.TESTDIV ";
        $query .= "             AND L1.EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= "             AND T1.EXAMNO       = L1.RECEPTNO ";
        $query .= "     GROUP BY ";
        $query .= "         T1.EXAMNO ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         L1.TOTAL AS TOTAL1, ";
        $query .= "         L1.AVG AS AVARAGE1, ";
        $query .= "         L1.FLG AS TOTAL_FLG1, ";
        $query .= "         RANK() OVER(PARTITION BY L1.FLG ORDER BY L1.TOTAL DESC) AS TOTAL_RANK1, ";
        $query .= "         L2.TOTAL AS TOTAL4, ";
        $query .= "         L2.AVG AS AVARAGE4, ";
        $query .= "         L2.FLG AS TOTAL_FLG4, ";
        $query .= "         RANK() OVER(PARTITION BY L2.FLG ORDER BY L2.TOTAL DESC) AS TOTAL_RANK4, ";
        $query .= "         VALUE(L1.TOTAL,0) + VALUE(L2.TOTAL,0) AS TOTAL2, ";
        $query .= "         CASE WHEN (L1.FLG + L2.FLG) > 1 THEN 1 ELSE 0 END AS TOTAL_FLG2, ";
        $query .= "         RANK() OVER(PARTITION BY (CASE WHEN (L1.FLG + L2.FLG) > 1 THEN 1 ELSE 0 END) ORDER BY (VALUE(L1.TOTAL,0) + VALUE(L2.TOTAL,0)) DESC) AS TOTAL_RANK2, ";
        $query .= "         RANK() OVER(PARTITION BY (CASE WHEN (L1.FLG + L2.FLG) > 1 THEN 1 ELSE 0 END), DESIREDIV ORDER BY (VALUE(L1.TOTAL,0) + VALUE(L2.TOTAL,0)) DESC) AS DIV_RANK2 ";
        $query .= "     FROM ";
        $query .= "         BASE T1 ";
        $query .= "         LEFT JOIN CONFRPT L1 ON T1.EXAMNO = L1.EXAMNO ";
        $query .= "         LEFT JOIN SCORE L2 ON T1.EXAMNO = L2.EXAMNO ";
        $query .= " ), STANDARD_DEVIATION AS ( ";
        $query .= "     SELECT ";
        $query .= "         DECIMAL(ROUND(STDDEV(FLOAT(TOTAL2))*100000,0)/100000,8,5) AS CALC_STDDEV, ";
        $query .= "         DECIMAL(ROUND(AVG(FLOAT(TOTAL2))*100000,0)/100000,8,5) AS CALC_AVG ";
        $query .= "     FROM ";
        $query .= "         MAIN ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     CASE WHEN 0 < T2.CALC_STDDEV THEN DECIMAL(ROUND((10*(T1.TOTAL2-T2.CALC_AVG)/T2.CALC_STDDEV+50)*10,0)/10,5,1) END AS JUDGE_DEVIATION ";
        $query .= " FROM ";
        $query .= "     MAIN T1, ";
        $query .= "     STANDARD_DEVIATION T2 ";
        $query .= " ORDER BY ";
        $query .= "     EXAMNO ";

        return $query;
    }

    //算出
    function ExecuteQuery($model) {
        //DB接続
        $db = Query::dbCheckOut();

        //エラー無視
        $db->expectError();

        //削除
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '".$model->exam_type."' ";
        $db->query($query);

        //算出データ取得
        $data_flg = false;
        $query = knjl060dQuery::getCulcQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["TESTDIV"][TEXT]              = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]            = $model->exam_type;
            $data["RECEPTNO"][TEXT]             = $row["EXAMNO"];
            $data["EXAMNO"][TEXT]               = $row["EXAMNO"];
            $data["TOTAL2"][NUMBER]             = ($row["TOTAL_FLG2"] == 1) ? "" : $row["TOTAL2"];
            $data["TOTAL_RANK2"][NUMBER]        = ($row["TOTAL_FLG2"] == 1) ? "" : $row["TOTAL_RANK2"];
            $data["DIV_RANK2"][NUMBER]          = ($row["TOTAL_FLG2"] == 1) ? "" : $row["DIV_RANK2"];
            $data["TOTAL4"][NUMBER]             = $row["TOTAL4"];
            $data["AVARAGE4"][NUMBER]           = $row["AVARAGE4"];
            $data["TOTAL_RANK4"][NUMBER]        = ($row["TOTAL_FLG4"] == 1) ? "" : $row["TOTAL_RANK4"];
            $data["TOTAL1"][NUMBER]             = $row["TOTAL1"];
            $data["AVARAGE1"][NUMBER]           = $row["AVARAGE1"];
            $data["TOTAL_RANK1"][NUMBER]        = ($row["TOTAL_FLG1"] == 1) ? "" : $row["TOTAL_RANK1"];
            $data["JUDGE_DEVIATION"][NUMBER]    = ($row["TOTAL_FLG2"] == 1) ? "" : $row["JUDGE_DEVIATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $result1 = $db->query(Query::insertSQL($data, "ENTEXAM_RECEPT_DAT"));
            //エラー時後処理
            if (DB::isError($result1)) {
               $db->popExpect();
               Query::dbCheckIn($db);
               return $result1->getCode();
            }
            $data_flg = true;
        }
        $result->free();

        //対象データなし
        if (!$data_flg) {
           return "NO_DATA";
        }

        //正常
        $db->popExpect();
        Query::dbCheckIn($db);
        return DB_OK;
    } 
}
?>

