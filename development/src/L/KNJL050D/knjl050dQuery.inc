<?php

require_once('for_php7.php');
class knjl050dQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場取得
    public function getHallData($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     EXAMHALLCD || ':' || EXAMHALL_NAME AS LABEL, ";
        $query .= "     EXAMHALLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '".$model->exam_type."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目取得
    public function getTestSubclasscd($model, $testsubclasscd = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1     = 'L009' AND ";
        $query .= "     NAME1 IS NOT NULL ";
        if ($testsubclasscd) {
            $query .= " AND NAMECD2     = '".$testsubclasscd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model, $div)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        if ($div == "csv") {
            $query .= "         B1.ENTEXAMYEAR, ";
            $query .= "         B1.TESTDIV, ";
            $query .= "         H1.EXAMHALLCD, ";
            $query .= "         S1.TESTSUBCLASSCD, ";
            $query .= "         '".$model->lastColumn."' AS ".$model->lastColumn.", ";
        }
        $query .= "         B1.EXAMNO, ";
        $query .= "         CASE WHEN B1.JUDGEMENT = '4' THEN '欠席' ELSE NULL END AS ATTEND, ";
        $query .= "         S1.SCORE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "         INNER JOIN ENTEXAM_HALL_YDAT H1 ";
        $query .= "              ON H1.ENTEXAMYEAR      = B1.ENTEXAMYEAR ";
        $query .= "             AND H1.APPLICANTDIV     = B1.APPLICANTDIV ";
        $query .= "             AND H1.TESTDIV          = B1.TESTDIV ";
        $query .= "             AND H1.EXAM_TYPE        = '{$model->exam_type}' ";
        if ($model->examhallcd != "ALL") {
            $query .= "             AND H1.EXAMHALLCD       = '{$model->examhallcd}' ";
        }
        $query .= "             AND B1.EXAMNO BETWEEN H1.S_RECEPTNO AND H1.E_RECEPTNO ";
        $query .= "         LEFT JOIN ENTEXAM_SCORE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = B1.ENTEXAMYEAR ";
        $query .= "             AND S1.APPLICANTDIV     = B1.APPLICANTDIV ";
        $query .= "             AND S1.TESTDIV          = B1.TESTDIV ";
        $query .= "             AND S1.EXAM_TYPE        = '{$model->exam_type}' ";
        $query .= "             AND S1.RECEPTNO         = B1.EXAMNO ";
        $query .= "             AND S1.TESTSUBCLASSCD   = '{$model->testsubclasscd}' ";
        $query .= "     WHERE ";
        $query .= "             B1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "         AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "         AND B1.TESTDIV      = '{$model->testdiv}' ";
        if ($div == "list") {
            if ($model->cmd == "next") {
                $query .= "         AND B1.EXAMNO > '{$model->e_examno}' ";
            } elseif ($model->cmd == "back") {
                $query .= "         AND B1.EXAMNO < '{$model->s_examno}' ";
            } else {
                if (strlen($model->s_examno)) {
                    $query .= "         AND B1.EXAMNO >= '{$model->s_examno}' ";
                }
            }

            $sort = ($model->cmd == "back") ? "DESC" : "";
            $query .= "     ORDER BY ";
            $query .= "         B1.EXAMNO ".$sort;
            $query .= "     FETCH FIRST 50 ROWS ONLY ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     EXAMNO ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND EXAM_TYPE      = '".$model->exam_type."' ";
        $query .= "   AND TESTSUBCLASSCD = '".$model->testsubclasscd."' ";
        $query .= "   AND RECEPTNO IN ('".implode("','", array_keys($model->score))."')";
        $db->query($query);

        //追加
        foreach ($model->score as $examno => $score) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]        = $model->exam_type;
            $data["RECEPTNO"][TEXT]         = $examno;
            $data["TESTSUBCLASSCD"][TEXT]   = $model->testsubclasscd;
            $data["ATTEND_FLG"][TEXT]       = !strlen($score) ? "" : "1";
            $data["SCORE"][NUMBER]          = !strlen($score) ? "" : $score;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_SCORE_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
