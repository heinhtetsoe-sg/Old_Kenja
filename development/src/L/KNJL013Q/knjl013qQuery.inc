<?php

require_once('for_php7.php');

class knjl013qQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        if ($namecd1 == "L004") {
            $query .= " AND NAMECD2 != '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //チェック　受付データ（欠席入力～入力済み）有り
    function getReceptCnt($model) {
        $query  = " SELECT ";
        $query .= "     SUM(CASE WHEN JUDGEDIV IS NOT NULL OR TOTAL4 IS NOT NULL THEN 1 ELSE 0 END) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";

        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        $query  = " DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $db->query($query);

        //INSERT
        $query  = " INSERT INTO ENTEXAM_RECEPT_DAT ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     EXAM_TYPE, ";
        $query .= "     RECEPTNO, ";
        $query .= "     EXAMNO, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     '1' AS EXAM_TYPE, ";
        $query .= "     EXAMNO, ";
        $query .= "     EXAMNO, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $db->query($query);

        //「5:一般入試」を実行する時、基準テスト対象者(GENERAL_FLG = '1')も生成する。
        if ($model->field["TESTDIV"] == "5" && SCHOOLKIND != "J" && SCHOOLKIND != "P") {
            //推薦入試合格者について、基準テスト対象者(GENERAL_FLG = '1')をセット
            //一旦クリア
            $query  = " UPDATE ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "    SET GENERAL_FLG  = NULL ";
            $query .= "  WHERE ENTEXAMYEAR  = '{$model->year}' ";
            $query .= "    AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "    AND TESTDIV      in ('3','4') ";
            $query .= "    AND GENERAL_FLG  = '1' ";
            $db->query($query);
            //セット
            $query  = " UPDATE ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "    SET GENERAL_FLG  = '1' ";
            $query .= "  WHERE ENTEXAMYEAR  = '{$model->year}' ";
            $query .= "    AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "    AND TESTDIV      in ('3','4') ";
            $query .= "    AND EXAMNO       in ( ";
            $query .= "         SELECT R1.EXAMNO ";
            $query .= "           FROM ENTEXAM_RECEPT_DAT R1 ";
            $query .= "                LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'L013' AND N1.NAMECD2 = R1.JUDGEDIV ";
            $query .= "          WHERE R1.ENTEXAMYEAR  = '{$model->year}' ";
            $query .= "            AND R1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "            AND R1.TESTDIV      in ('3','4') ";
            $query .= "            AND N1.NAMESPARE1   = '1' ";
            $query .= "    ) ";
            $db->query($query);

            //受付データ生成
            $query  = " INSERT INTO ENTEXAM_RECEPT_DAT ( ";
            $query .= "     ENTEXAMYEAR, ";
            $query .= "     APPLICANTDIV, ";
            $query .= "     TESTDIV, ";
            $query .= "     EXAM_TYPE, ";
            $query .= "     RECEPTNO, ";
            $query .= "     EXAMNO, ";
            $query .= "     REGISTERCD, ";
            $query .= "     UPDATED ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     ENTEXAMYEAR, ";
            $query .= "     APPLICANTDIV, ";
            $query .= "     '5' AS TESTDIV, ";
            $query .= "     '1' AS EXAM_TYPE, ";
            $query .= "     EXAMNO, ";
            $query .= "     EXAMNO, ";
            $query .= "     '".STAFFCD."', ";
            $query .= "     sysdate() ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$model->year}' ";
            $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND ( ";
            $query .= "         TESTDIV = '9' "; //「9:駿中生」
            $query .= "      OR TESTDIV in ('3','4') AND GENERAL_FLG = '1' "; //「3:校長推薦」「4:自己推薦」「1:基準テスト対象者」
            $query .= "     ) ";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
