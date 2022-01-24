<?php

require_once('for_php7.php');

class knjl050mQuery extends Query {
    //確定済みかチェック
    function getEntexamControlDat($model) {
        list($subclassCd, $paperCd, $perfect) = preg_split("/-/", $model->field["TESTPAPERCD"]);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' AND ";
        $query .= "     TESTSUBCLASSCD = '{$subclassCd}' AND ";
        $query .= "     TESTPAPERCD = '{$paperCd}' AND ";
        $query .= "     EXAMHALLCD = '{$model->field["EXAMHALLCD"]}' AND ";
        $query .= "     CONTROL_FLG = '1' ";

        return $query;
    }

    //確定テーブルの更新
    function insertEntexamControlDat($model, $db) {
        list($subclassCd, $paperCd, $perfect) = preg_split("/-/", $model->field["TESTPAPERCD"]);

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR    = '{$model->year}' AND ";
        $query .= "     TESTSUBCLASSCD = '{$subclassCd}' AND ";
        $query .= "     TESTPAPERCD    = '{$paperCd}' AND ";
        $query .= "     EXAMHALLCD     = '{$model->field["EXAMHALLCD"]}' ";

        $db->query($query);
        $query = '';

        //確定
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["TESTSUBCLASSCD"][TEXT]   = $subclassCd;
        $data["TESTPAPERCD"][TEXT]      = $paperCd;
        $data["EXAMHALLCD"][TEXT]       = $model->field["EXAMHALLCD"];
        $data["CONTROL_FLG"][TEXT]      = '1';
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_CONTROL_DAT");
        $db->query($query);
    }

    function cntHallDat($model) {
        $query  = " WITH BASE AS ( ";
        $query .= " SELECT ";
        $query .= "     EXAMHALLCD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '1' ";
        $query .= "     AND EXAM_TYPE = '1' ";
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_RECEPT_DAT ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR = '{$model->year}' ";
        $query .= "             AND APPLICANTDIV = '1' ";
        $query .= "             AND TESTDIV = '1' ";
        $query .= "             AND EXAM_TYPE = '1' ";
        $query .= "             AND RECEPTNO BETWEEN S_RECEPTNO AND E_RECEPTNO ";
        $query .= "     ) ";
        $query .= " GROUP BY ";
        $query .= "     EXAMHALLCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     BASE ";

        return $query;
    }

    //科目詳細
    function getSubclassDetail($model, $hallCnt) {
        $query  = " WITH CONTROL_CNT AS ( ";
        $query .= " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     TESTPAPERCD, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     CONTROL_FLG = '1' ";
        $query .= " GROUP BY ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     TESTPAPERCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.NAME1 || '-' || L1.TESTPAPERCD AS LABEL, ";
        $query .= "     T1.NAMECD2 || '-' ||L1.TESTPAPERCD || '-' || CASE WHEN L1.PERFECT < 10 THEN CAST(L1.PERFECT AS CHAR(1)) ";
        $query .= "                                                       WHEN L1.PERFECT < 100 THEN CAST(L1.PERFECT AS CHAR(2)) ";
        $query .= "                                                       WHEN L1.PERFECT < 1000 THEN CAST(L1.PERFECT AS CHAR(3)) ";
        $query .= "                                                       ELSE '' ";
        $query .= "                                                  END ";
        $query .= "     AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_PERFECT_DETAIL_MST L1 ON  T1.NAMECD2 = L1.TESTSUBCLASSCD ";
        $query .= "                                   AND T1.YEAR    = L1.ENTEXAMYEAR ";
        $query .= " LEFT JOIN ";
        $query .= "     CONTROL_CNT L2 ON  L2.TESTSUBCLASSCD = L1.TESTSUBCLASSCD ";
        $query .= "                    AND L2.TESTPAPERCD = L1.TESTPAPERCD ";
        $query .= "                    AND L2.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.NAMECD1 = 'L009' ";
        if (!$model->kakutei) {
            $query .= "     AND VALUE(L2.CNT, 0) < {$hallCnt} ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場データ
    function getHallDat($model, $hallCd = "") {
        list($subclassCd, $paperCd, $perfect) = preg_split("/-/", $model->field["TESTPAPERCD"]);

        $query  = " SELECT ";
        $query .= "     EXAMHALL_NAME AS LABEL, ";
        $query .= "     EXAMHALLCD AS VALUE, ";
        $query .= "     S_RECEPTNO, ";
        $query .= "     E_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '1' ";
        $query .= "     AND EXAM_TYPE = '1' ";
        if ($hallCd) {
            $query .= "     AND EXAMHALLCD = '{$hallCd}' ";
        }
        $query .= "     AND EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_RECEPT_DAT ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR = '{$model->year}' ";
        $query .= "             AND APPLICANTDIV = '1' ";
        $query .= "             AND TESTDIV = '1' ";
        $query .= "             AND EXAM_TYPE = '1' ";
        $query .= "             AND RECEPTNO BETWEEN S_RECEPTNO AND E_RECEPTNO ";
        $query .= "     ) ";
        if (!$model->kakutei) {
            $query .= "     AND EXAMHALLCD NOT IN ( ";
            $query .= "             SELECT ";
            $query .= "                 EXAMHALLCD ";
            $query .= "             FROM ";
            $query .= "                 ENTEXAM_CONTROL_DAT ";
            $query .= "             WHERE ";
            $query .= "                 ENTEXAMYEAR = '{$model->year}' ";
            $query .= "                 AND TESTSUBCLASSCD = '{$subclassCd}' ";
            $query .= "                 AND TESTPAPERCD = '{$paperCd}' ";
            $query .= "                 AND CONTROL_FLG = '1' ";
            $query .= "     ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     EXAMHALL_NAME, ";
        $query .= "     EXAMHALLCD, ";
        $query .= "     S_RECEPTNO, ";
        $query .= "     E_RECEPTNO ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験番号MAXとMINデータ
    function getReceptMaxMin($model, $sRecept, $eRecept)
    {

        $query  = " SELECT ";
        $query .= "     MIN(RECEPTNO) AS S_RECEPTNO, ";
        $query .= "     MAX(RECEPTNO) AS E_RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '1' ";
        $query .= "     AND TESTDIV = '1' ";
        $query .= "     AND EXAM_TYPE = '1' ";
        $query .= "     AND RECEPTNO BETWEEN '".$sRecept."' AND '".$eRecept."' ";

        return $query;
    }

    //受験番号データ
    function getReceptNo($model, $checkReceptNo)
    {

        $query  = " SELECT ";
        $query .= "     RECEPTNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '1' ";
        $query .= "     AND TESTDIV = '1' ";
        $query .= "     AND EXAM_TYPE = '1' ";
        if ($model->cmd == "back" || $model->cmd == "upBack") {
            $query .= "     AND RECEPTNO <= '".$checkReceptNo."' ";
            $query .= " ORDER BY ";
            $query .= "     RECEPTNO DESC ";
        } else if ($model->cmd == "next" || $model->cmd == "upNext") {
            $query .= "     AND RECEPTNO >= '".$checkReceptNo."' ";
            $query .= " ORDER BY ";
            $query .= "     RECEPTNO ";
        } else {
            $query .= "     AND RECEPTNO = '".$model->receptno."' ";
            $query .= " ORDER BY ";
            $query .= "     RECEPTNO ";
        }

        return $query;
    }

    //得点データ
    function getScore($model)
    {
        list($subclassCd, $paperCd, $perfect) = preg_split("/-/", $model->field["TESTPAPERCD"]);

        $query  = " SELECT ";
        $query .= "     SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '1' ";
        $query .= "     AND TESTDIV = '1' ";
        $query .= "     AND EXAM_TYPE = '1' ";
        $query .= "     AND RECEPTNO = '".$model->receptno."' ";
        $query .= "     AND TESTSUBCLASSCD = '".$subclassCd."' ";
        $query .= "     AND TESTPAPERCD = '".$paperCd."' ";

        return $query;
    }

    //--- DELITE INSERT
    function &getDeleteInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($subclassCd, $paperCd, $perfect) = preg_split("/-/", $model->field["TESTPAPERCD"]);

        //削除
        $query  = "DELETE FROM ENTEXAM_SCORE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '1' ";
        $query .= "     AND TESTDIV = '1' ";
        $query .= "     AND EXAM_TYPE = '1' ";
        $query .= "     AND RECEPTNO = '".$model->receptno."' ";
        $query .= "     AND TESTSUBCLASSCD = '".$subclassCd."' ";
        $query .= "     AND TESTPAPERCD = '".$paperCd."' ";

        $db->query($query);

        //新規
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = "1";
        $data["TESTDIV"][TEXT]          = "1";
        $data["EXAM_TYPE"][TEXT]        = "1";
        $data["RECEPTNO"][TEXT]         = $model->receptno;
        $data["TESTSUBCLASSCD"][TEXT]   = $subclassCd;
        $data["TESTPAPERCD"][TEXT]      = $paperCd;
        $data["SCORE"][NUMBER]          = $model->field["SCORE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_SCORE_DETAIL_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($subclassCd, $paperCd, $perfect) = preg_split("/-/", $model->field["TESTPAPERCD"]);
        //ENTEXAM_DESIRE_DAT

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>