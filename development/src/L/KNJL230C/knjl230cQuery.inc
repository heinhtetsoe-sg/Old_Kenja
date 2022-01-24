<?php

require_once('for_php7.php');

class knjl230cQuery extends Query {

    //学校コードを取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     CASE WHEN NAME1 = 'CHIBEN' THEN NAME2 ELSE NULL END AS SCHOOLCODE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtn = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtn;
    }

    //入試制度を取得
    function getApplicantdiv($model) {
        $query  = " WITH DATA(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1','1:中学') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     DATA ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //プレテスト区分を取得
    function getPreTestdiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->examyear}' ";
        $query .= "     AND NAMECD1 = 'L104' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function ExecuteQuery($model) {

        //DB接続
        $db = Query::dbCheckOut();

        //エラー無視
        $db->expectError();

        /**
         ** 受付データの削除
         **/
        $query  = "DELETE FROM ENTEXAM_RECEPT_PRE_DAT T1 ";
        $query .= " WHERE T1.entexamyear  = '{$model->examyear}'";
        $query .= "   AND T1.applicantdiv = '{$model->applicantdiv}'";
        $query .= "   AND T1.pre_receptno IN ( ";
        $query .= "        SELECT W1.pre_receptno FROM ENTEXAM_APPLICANTBASE_PRE_DAT W1 ";
        $query .= "         WHERE W1.entexamyear  = '{$model->examyear}'";
        $query .= "           AND W1.applicantdiv = '{$model->applicantdiv}'";
        $query .= "           AND W1.pre_testdiv  = '{$model->pre_testdiv}'";
        $query .= "   ) ";
        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }

        /**
         ** 受付データの追加
         **/
        $query = knjl230cQuery::getInsReceptPreDat($model);
        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
           $db->popExpect();
           Query::dbCheckIn($db);
           return $result->getCode();
        }

        /**
         ** 得点データの更新
         **/
        $query = knjl230cQuery::getUpdScorePreDat($model);
        //エラー時後処理
        $result = $db->query($query);
        if (DB::isError($result)) {
            $db->popExpect();
            Query::dbCheckIn($db);
            return $result->getCode();
        }

        $db->popExpect();
        Query::dbCheckIn($db);
        return DB_OK;
    } 

    /**
     ** 受付データの追加
     **     全科目受験フラグ・・・「1:全科目受験 0:それ以外」
     **     ２科目合計・平均・・・「1:国語、2:算数」の合計・平均
     **     全科目合計・平均・・・「1:国語、2:算数、3:理科」の合計・平均
     **     ２科目全体・席次・・・２科目合計の席次。母集団はⅠ型＋Ⅱ型。
     **     ２科目志望・席次・・・２科目合計の席次。母集団はⅠ型またはⅡ型。
     **     全科目全体・席次・・・全科目合計の席次。母集団はⅠ型＋Ⅱ型。
     **     全科目志望・席次・・・全科目合計の席次。母集団はⅠ型またはⅡ型。
     **     ※得点データのゴミデータは参照しない。
     **/
    function getInsReceptPreDat($model) {
        $query  = " INSERT INTO ENTEXAM_RECEPT_PRE_DAT ";
        $query .= " WITH T_TOTAL AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.APPLICANTDIV, ";
        $query .= "         T1.PRE_RECEPTNO, ";
        $query .= "         T1.PRE_EXAM_TYPE, ";
        $query .= "         CASE WHEN COUNT(T3.SCORE) = COUNT(T2.TESTSUBCLASSCD) THEN '1' ELSE '0' END AS ATTEND_ALL_FLG, ";
        if ($model->isGojo) {
            $query .= "         SUM(CASE WHEN T3.TESTSUBCLASSCD = '4' THEN NULL ELSE T3.SCORE END) AS TOTAL2, ";
            $query .= "         ROUND(SUM(FLOAT(CASE WHEN T3.TESTSUBCLASSCD = '4' THEN NULL ELSE T3.SCORE END))/(SUM(FLOAT(VALUE(CASE WHEN T3.TESTSUBCLASSCD = '4' THEN 0 ELSE T2.PERFECT END, 100)))/100)*10,0)/10 AS AVARAGE2, ";
            $query .= "         SUM(CASE WHEN T3.TESTSUBCLASSCD = '3' THEN NULL ELSE T3.SCORE END) AS TOTAL3, ";
            $query .= "         ROUND(SUM(FLOAT(CASE WHEN T3.TESTSUBCLASSCD = '3' THEN NULL ELSE T3.SCORE END))/(SUM(FLOAT(VALUE(CASE WHEN T3.TESTSUBCLASSCD = '3' THEN 0 ELSE T2.PERFECT END, 100)))/100)*10,0)/10 AS AVARAGE3, ";
            $query .= "         SUM(T3.SCORE) AS TOTAL4, ";
            $query .= "         ROUND(SUM(FLOAT(T3.SCORE))/(SUM(FLOAT(VALUE(T2.PERFECT, 100)))/100)*10,0)/10 AS AVARAGE4 ";
        } else {
            $query .= "         SUM(CASE WHEN T3.TESTSUBCLASSCD = '3' THEN NULL ELSE T3.SCORE END) AS TOTAL2, ";
            $query .= "         ROUND(SUM(FLOAT(CASE WHEN T3.TESTSUBCLASSCD = '3' THEN NULL ELSE T3.SCORE END))/(SUM(FLOAT(VALUE(CASE WHEN T3.TESTSUBCLASSCD = '3' THEN 0 ELSE T2.PERFECT END, 100)))/100)*10,0)/10 AS AVARAGE2, ";
            $query .= "         SUM(T3.SCORE) AS TOTAL4, ";
            $query .= "         ROUND(SUM(FLOAT(T3.SCORE))/(SUM(FLOAT(VALUE(T2.PERFECT, 100)))/100)*10,0)/10 AS AVARAGE4 ";
        }
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_PRE_DAT T1 ";
        $query .= "         INNER JOIN ENTEXAM_PERFECT_PRE_MST T2 ";
        $query .= "             ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T2.PRE_EXAM_TYPE = T1.PRE_EXAM_TYPE ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_PRE_DAT T3 ";
        $query .= "             ON  T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "             AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "             AND T3.PRE_RECEPTNO = T1.PRE_RECEPTNO ";
        $query .= "             AND T3.TESTSUBCLASSCD = T2.TESTSUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "             T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "         AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND T1.PRE_TESTDIV  = '{$model->pre_testdiv}' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.APPLICANTDIV, ";
        $query .= "         T1.PRE_RECEPTNO, ";
        $query .= "         T1.PRE_EXAM_TYPE ";
        $query .= "     ) ";
        //メイン
        $query .= " SELECT ";
        $query .= "     '{$model->examyear}' AS ENTEXAMYEAR, ";
        $query .= "     '{$model->applicantdiv}' AS APPLICANTDIV, ";
        $query .= "     T1.PRE_RECEPTNO, ";
        $query .= "     T1.ATTEND_ALL_FLG, ";
        $query .= "     T1.TOTAL2, ";
        $query .= "     T1.AVARAGE2, ";
        $query .= "     RANK() OVER(PARTITION BY T1.APPLICANTDIV                   ORDER BY T1.TOTAL2 DESC) AS TOTAL_RANK2, ";
        $query .= "     RANK() OVER(PARTITION BY T1.APPLICANTDIV, T1.PRE_EXAM_TYPE ORDER BY T1.TOTAL2 DESC) AS DIV_RANK2, ";
        $query .= "     T1.TOTAL4, ";
        $query .= "     T1.AVARAGE4, ";
        $query .= "     RANK() OVER(PARTITION BY T1.APPLICANTDIV                   ORDER BY T1.TOTAL4 DESC) AS TOTAL_RANK4, ";
        $query .= "     RANK() OVER(PARTITION BY T1.APPLICANTDIV, T1.PRE_EXAM_TYPE ORDER BY T1.TOTAL4 DESC) AS DIV_RANK4, ";
        if ($model->isGojo) {
            $query .= "     T1.TOTAL3, ";
            $query .= "     T1.AVARAGE3, ";
            $query .= "     RANK() OVER(PARTITION BY T1.APPLICANTDIV                   ORDER BY T1.TOTAL3 DESC) AS TOTAL_RANK3, ";
            $query .= "     RANK() OVER(PARTITION BY T1.APPLICANTDIV, T1.PRE_EXAM_TYPE ORDER BY T1.TOTAL3 DESC) AS DIV_RANK3, ";
        } else {
            $query .= "     CAST(NULL AS SMALLINT) AS TOTAL3, ";
            $query .= "     CAST(NULL AS DECIMAL(4,1)) AS AVARAGE3, ";
            $query .= "     CAST(NULL AS SMALLINT) AS TOTAL_RANK3, ";
            $query .= "     CAST(NULL AS SMALLINT) AS DIV_RANK3, ";
        }
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     SYSDATE() AS UPDATED ";
        $query .= " FROM ";
        $query .= "     T_TOTAL T1 ";
        $query .= " WHERE ";
        $query .= "     T1.TOTAL2 IS NOT NULL ";

        return $query;
    }

    /**
     ** 得点データの更新
     **     受験科目ごと
     **     全体席次・・・母集団はⅠ型＋Ⅱ型。
     **     志望席次・・・母集団はⅠ型またはⅡ型。
     **     ※得点データのゴミデータは更新しない。
     **/
    function getUpdScorePreDat($model) {
        $query  = " UPDATE ENTEXAM_SCORE_PRE_DAT ";
        $query .= "    SET (RANK,DIV_RANK,REGISTERCD,UPDATED) ";
        $query .= "     = ( ";
        $query .= "     SELECT ";
        $query .= "         T2.RANK, ";
        $query .= "         T2.DIV_RANK, ";
        $query .= "         '".STAFFCD."' AS REGISTERCD, ";
        $query .= "         SYSDATE() AS UPDATED ";
        $query .= "     FROM ";
        $query .= "         ( ";
        $query .= "         SELECT ";
        $query .= "             T1.PRE_RECEPTNO, ";
        $query .= "             T1.PRE_EXAM_TYPE, ";
        $query .= "             T3.TESTSUBCLASSCD, ";
        $query .= "             T3.SCORE ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_APPLICANTBASE_PRE_DAT T1 ";
        $query .= "             INNER JOIN ENTEXAM_PERFECT_PRE_MST T2 ";
        $query .= "                 ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                 AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                 AND T2.PRE_EXAM_TYPE = T1.PRE_EXAM_TYPE ";
        $query .= "             INNER JOIN ENTEXAM_SCORE_PRE_DAT T3 ";
        $query .= "                 ON  T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                 AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                 AND T3.PRE_RECEPTNO = T1.PRE_RECEPTNO ";
        $query .= "                 AND T3.TESTSUBCLASSCD = T2.TESTSUBCLASSCD ";
        $query .= "         WHERE ";
        $query .= "                 T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "             AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "             AND T1.PRE_TESTDIV  = '{$model->pre_testdiv}' ";
        $query .= "         ) T1 ";
        $query .= "         LEFT JOIN ( ";
        $query .= "             SELECT ";
        $query .= "                 T1.PRE_RECEPTNO, ";
        $query .= "                 T1.TESTSUBCLASSCD, ";
        $query .= "                 RANK() OVER(PARTITION BY T1.TESTSUBCLASSCD                   ORDER BY T1.SCORE DESC) AS RANK, ";
        $query .= "                 RANK() OVER(PARTITION BY T1.TESTSUBCLASSCD, T1.PRE_EXAM_TYPE ORDER BY T1.SCORE DESC) AS DIV_RANK ";
        $query .= "             FROM ";
        $query .= "                 ( ";
        $query .= "                 SELECT ";
        $query .= "                     T1.PRE_RECEPTNO, ";
        $query .= "                     T1.PRE_EXAM_TYPE, ";
        $query .= "                     T3.TESTSUBCLASSCD, ";
        $query .= "                     T3.SCORE ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_PRE_DAT T1 ";
        $query .= "                     INNER JOIN ENTEXAM_PERFECT_PRE_MST T2 ";
        $query .= "                         ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                         AND T2.PRE_EXAM_TYPE = T1.PRE_EXAM_TYPE ";
        $query .= "                     INNER JOIN ENTEXAM_SCORE_PRE_DAT T3 ";
        $query .= "                         ON  T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                         AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                         AND T3.PRE_RECEPTNO = T1.PRE_RECEPTNO ";
        $query .= "                         AND T3.TESTSUBCLASSCD = T2.TESTSUBCLASSCD ";
        $query .= "                 WHERE ";
        $query .= "                         T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "                     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "                     AND T1.PRE_TESTDIV  = '{$model->pre_testdiv}' ";
        $query .= "                 ) T1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.SCORE IS NOT NULL ";
        $query .= "             ) T2 ON T2.PRE_RECEPTNO = T1.PRE_RECEPTNO AND T2.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "             T1.PRE_RECEPTNO   = ENTEXAM_SCORE_PRE_DAT.PRE_RECEPTNO ";
        $query .= "         AND T1.TESTSUBCLASSCD = ENTEXAM_SCORE_PRE_DAT.TESTSUBCLASSCD ";
        $query .= "         ) ";

        $query .= " WHERE ";
        $query .= "         ENTEXAM_SCORE_PRE_DAT.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "     AND ENTEXAM_SCORE_PRE_DAT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             ( ";
        $query .= "             SELECT ";
        $query .= "                 T1.PRE_RECEPTNO, ";
        $query .= "                 T3.TESTSUBCLASSCD ";
        $query .= "             FROM ";
        $query .= "                 ENTEXAM_APPLICANTBASE_PRE_DAT T1 ";
        $query .= "                 INNER JOIN ENTEXAM_PERFECT_PRE_MST T2 ";
        $query .= "                     ON  T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                     AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                     AND T2.PRE_EXAM_TYPE = T1.PRE_EXAM_TYPE ";
        $query .= "                 INNER JOIN ENTEXAM_SCORE_PRE_DAT T3 ";
        $query .= "                     ON  T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                     AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                     AND T3.PRE_RECEPTNO = T1.PRE_RECEPTNO ";
        $query .= "                     AND T3.TESTSUBCLASSCD = T2.TESTSUBCLASSCD ";
        $query .= "             WHERE ";
        $query .= "                     T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "                 AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "                 AND T1.PRE_TESTDIV  = '{$model->pre_testdiv}' ";
        $query .= "             ) T1 ";
        $query .= "         WHERE ";
        $query .= "                 T1.PRE_RECEPTNO   = ENTEXAM_SCORE_PRE_DAT.PRE_RECEPTNO ";
        $query .= "             AND T1.TESTSUBCLASSCD = ENTEXAM_SCORE_PRE_DAT.TESTSUBCLASSCD ";
        $query .= "         ) ";

        return $query;
    }
}
?>

