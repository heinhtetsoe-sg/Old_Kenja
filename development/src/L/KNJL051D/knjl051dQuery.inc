<?php

require_once('for_php7.php');
class knjl051dQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
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

    //一覧表示
    public function selectQuery($model, $div)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        if ($div == "csv") {
            $query .= "         B1.ENTEXAMYEAR, ";
            $query .= "         B1.TESTDIV, ";
            $query .= "         H1.EXAMHALLCD, ";
            $query .= "         '".$model->lastColumn."' AS ".$model->lastColumn.", ";
        }
        $query .= "         B1.EXAMNO, ";
        $query .= "         B1.NAME, ";
        $query .= "         B1.NAME_KANA, ";
        $query .= "         N1.NAME1 AS SEX, ";
        $query .= "         CASE WHEN B1.JUDGEMENT = '4' THEN '欠席' ELSE NULL END AS ATTEND, ";
        $query .= "         I1.INTERVIEW_VALUE ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "         INNER JOIN ENTEXAM_HALL_YDAT H1 ";
        $query .= "              ON H1.ENTEXAMYEAR      = B1.ENTEXAMYEAR ";
        $query .= "             AND H1.APPLICANTDIV     = B1.APPLICANTDIV ";
        $query .= "             AND H1.TESTDIV          = B1.TESTDIV ";
        $query .= "             AND H1.EXAM_TYPE        = '{$model->exam_type}' ";
        $query .= "             AND H1.EXAMHALLCD       = '{$model->examhallcd}' ";
        $query .= "             AND B1.EXAMNO BETWEEN H1.S_RECEPTNO AND H1.E_RECEPTNO ";
        $query .= "         LEFT JOIN NAME_MST N1 ";
        $query .= "              ON N1.NAMECD1          = 'Z002' ";
        $query .= "             AND N1.NAMECD2          = B1.SEX ";
        $query .= "         LEFT JOIN ENTEXAM_INTERVIEW_DAT I1 ";
        $query .= "              ON I1.ENTEXAMYEAR      = B1.ENTEXAMYEAR ";
        $query .= "             AND I1.APPLICANTDIV     = B1.APPLICANTDIV ";
        $query .= "             AND I1.TESTDIV          = B1.TESTDIV ";
        $query .= "             AND I1.EXAMNO           = B1.EXAMNO ";
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

        //追加
        foreach ($model->interview as $examno => $value) {
            $where  = " WHERE ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
            $where .= "   AND TESTDIV      = '".$model->testdiv."' ";
            $where .= "   AND EXAMNO       = '".$examno."' ";
            $query = " SELECT COUNT(*) FROM ENTEXAM_INTERVIEW_DAT ".$where;

            $cnt = $db->getOne($query);

            $data = array();
            $data["INTERVIEW_VALUE"][TEXT]  = !strlen($value) ? "" : $value;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($cnt == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAMNO"][TEXT]           = $examno;
                $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_DAT", $where);
            }

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
