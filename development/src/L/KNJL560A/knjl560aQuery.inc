<?php

require_once('for_php7.php');

class knjl560aQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //対象人数チェック
    function checkRecCnt($model) {
        $query  = "SELECT COUNT(*) FROM ENTEXAM_RECEPT_DAT T1 ";
        $query .= " WHERE T1.ENTEXAMYEAR  = '".$model->examyear."'";
        $query .= "   AND T1.APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "   AND T1.TESTDIV      = '".$model->testdiv."'";
        return $query;
    }

    //得点データチェック
    function checkScoCnt($model) {
        $query  = "SELECT COUNT(*) FROM ENTEXAM_SCORE_DAT T1 ";
        $query .= " WHERE T1.ENTEXAMYEAR  = '".$model->examyear."'";
        $query .= "   AND T1.APPLICANTDIV = '".$model->applicantdiv."'"; 
        $query .= "   AND T1.TESTDIV      = '".$model->testdiv."'";
        return $query;
    }

    //算出
    function ExecuteQuery($model) {
        //参照データ（得点）

        //DB接続
        $db = Query::dbCheckOut();

        //受験科目
        $testsubArray = array();
        $query = knjl560aQuery::getNameMst($model->examyear, "L009");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testsubArray[] = $row["VALUE"];
        }
        $result->free();

        $cnt = get_count($testsubArray);

        //エラー無視
        $db->expectError();

        /* 全科目分受験フラグ、合計、平均 */ 
        $query  = "UPDATE ENTEXAM_RECEPT_DAT ";
        $query .= "   SET (EXAMNO, ";
        $query .= "        TOTAL4, ";
        $query .= "        AVARAGE4, ";
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";
        $query .= " SELECT ";
        $query .= "     TT0.EXAMNO, ";
        $query .= "     TT4.TOTAL4, ";
        $query .= "     TT4.AVG4, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT TT0 ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             S1.RECEPTNO, ";
        $query .= "             SUM(S1.SCORE) AS TOTAL4, ";
        $query .= "             DECIMAL(ROUND(AVG(FLOAT(S1.SCORE))*10,0)/10,5,1) AS AVG4 ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_SCORE_DAT S1 ";
        $query .= "             INNER JOIN V_NAME_MST E1 ON E1.YEAR = S1.ENTEXAMYEAR AND E1.NAMECD1 = 'L009' AND E1.NAMECD2 = S1.TESTSUBCLASSCD ";
        $query .= "         WHERE ";
        $query .= "                 S1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "             AND S1.APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "             AND S1.TESTDIV      = '".$model->testdiv."'";
        $query .= "             AND S1.EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= "         GROUP BY ";
        $query .= "             S1.RECEPTNO ";
        $query .= "     ) TT4 ON TT4.RECEPTNO  = TT0.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "         TT0.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND TT0.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TT0.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND TT0.EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= "     AND TT0.RECEPTNO     = ENTEXAM_RECEPT_DAT.RECEPTNO ";
        $query .= " ) ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$model->exam_type."' ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }

        //--------------------【合計での順位】--------------------

        /* 席次(合計) */
        $query  = "UPDATE ENTEXAM_RECEPT_DAT ";
        $query .= "   SET (EXAMNO, ";
        $query .= "        TOTAL_RANK4, ";
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";
        $query .= " SELECT ";
        $query .= "     TT0.EXAMNO, ";
        $query .= "     TT4.TOTAL_RANK4, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT TT0 ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             R1.RECEPTNO, ";
        $query .= "             RANK() OVER(PARTITION BY R1.TESTDIV ORDER BY R1.TOTAL4 DESC) AS TOTAL_RANK4 ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_RECEPT_DAT R1 ";
        $query .= "         WHERE ";
        $query .= "                 R1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "             AND R1.APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "             AND R1.TESTDIV      = '".$model->testdiv."'";
        $query .= "             AND R1.EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= "             AND R1.TOTAL4 IS NOT NULL ";
        $query .= "     ) TT4 ON TT4.RECEPTNO  = TT0.RECEPTNO ";
        $query .= " WHERE ";
        $query .= "         TT0.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND TT0.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TT0.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND TT0.EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= "     AND TT0.RECEPTNO     = ENTEXAM_RECEPT_DAT.RECEPTNO ";
        $query .= " )";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$model->exam_type."' ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        //--------------------【各教科の偏差値】--------------------

        $query  = "UPDATE ENTEXAM_SCORE_DAT ";
        $query .= "   SET (STD_SCORE, ";
        $query .= "        REGISTERCD, ";
        $query .= "        UPDATED) ";
        $query .= "     = ( ";

        $query .= " SELECT ";
//        $query .= "     DECIMAL(ROUND((10*(S1.SCORE-A1.AVG)/A1.STDDEV+50)*10,0)/10,5,1) AS DEVIATION, "; //丸める
        $query .= "     10*(S1.SCORE-A1.AVG)/A1.STDDEV+50 AS DEVIATION, "; //丸めない
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT S1 ";
        $query .= "     INNER JOIN V_NAME_MST E1 ON E1.YEAR = S1.ENTEXAMYEAR AND E1.NAMECD1 = 'L009' AND E1.NAMECD2 = S1.TESTSUBCLASSCD ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             S1.TESTSUBCLASSCD, ";
        $query .= "             AVG(FLOAT(S1.SCORE)) AS AVG, "; //丸めない
        $query .= "             STDDEV(FLOAT(S1.SCORE)) AS STDDEV "; //丸めない
//        $query .= "             DECIMAL(ROUND(AVG(FLOAT(S1.SCORE))*10,0)/10,5,1) AS AVG, "; //丸める
//        $query .= "             DECIMAL(ROUND(STDDEV(FLOAT(S1.SCORE))*10,0)/10,5,1) AS STDDEV "; //丸める
        $query .= "         FROM ";
        $query .= "             ENTEXAM_SCORE_DAT S1 ";
        $query .= "             INNER JOIN V_NAME_MST E1 ON E1.YEAR = S1.ENTEXAMYEAR AND E1.NAMECD1 = 'L009' AND E1.NAMECD2 = S1.TESTSUBCLASSCD ";
        $query .= "         WHERE ";
        $query .= "                 S1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "             AND S1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "             AND S1.TESTDIV      = '".$model->testdiv."' ";
        $query .= "             AND S1.EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= "         GROUP BY ";
        $query .= "             S1.TESTSUBCLASSCD ";
        $query .= "     ) A1 ON A1.TESTSUBCLASSCD = S1.TESTSUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         S1.ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND S1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND S1.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND S1.EXAM_TYPE    = '".$model->exam_type."' ";
        $query .= "     AND S1.RECEPTNO       = ENTEXAM_SCORE_DAT.RECEPTNO ";
        $query .= "     AND S1.TESTSUBCLASSCD = ENTEXAM_SCORE_DAT.TESTSUBCLASSCD ";

        $query .= " )";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$model->exam_type."' ";

        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }


        //正常
        $db->popExpect();
        Query::dbCheckIn($db);
        return DB_OK;
    } 
}
?>

