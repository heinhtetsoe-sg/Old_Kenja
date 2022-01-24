<?php

require_once('for_php7.php');
class knjl151dQuery extends Query
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

    //MAX_TESTDIV取得
    public function getMaxTestDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     max(TESTDIV) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " GROUP BY ";
        $query .= "     ENTEXAMYEAR ";

        return $query;
    }

    //志望区分リスト取得
    public function getTestDivList($year)
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1  AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = 'L004' ";
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
            $query .= "         '".$model->lastColumn."' AS ".$model->lastColumn.", ";
        }
        $query .= "         B1.EXAMNO, ";
        $query .= "         B1.NAME, ";
        $query .= "         I1.INTERVIEW_VALUE, ";
        $query .= "         I1.INTERVIEW_REMARK, ";
        $query .= "         I1.INTERVIEW_VALUE2, ";
        $query .= "         I1.INTERVIEW_REMARK2 ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DAT B1 ";
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
            $query .= "     FETCH FIRST 40 ROWS ONLY ";
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

        //登録
        foreach ($model->interview as $examno => $value) {
            //登録済みチェック
            //未登録なら追加、登録済みなら更新
            $query  = " SELECT EXAMNO FROM ";
            $query .= "     ENTEXAM_INTERVIEW_DAT ";
            $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
            $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
            $query .= "   AND TESTDIV        = '".$model->testdiv."' ";
            $query .= "   AND EXAMNO = '".$examno."' ";
            $result = $db->query($query);
            $chkflg = false;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["EXAMNO"] != null) {
                    $chkflg = true;
                }
            }
            $result->free();

            $data = array();
            $where = array();
            
            if ($chkflg) {
                $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
                $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
                $where .= "   AND EXAMNO        = '".$examno."' ";
            } else {
                $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["TESTDIV"][TEXT]          = $model->testdiv;
                $data["EXAMNO"][TEXT]           = $examno;
            }
            $interviewval = $value["INTERVIEW_VAL"];
            $interviewrmk = $value["INTERVIEW_RMK"];

            if ($model->evaltype == "1") {
                $data["INTERVIEW_VALUE"][TEXT]  = !strlen($interviewval) ? "" : $interviewval;
                $data["INTERVIEW_REMARK"][TEXT] = !strlen($interviewrmk) ? "" : $interviewrmk;
            } else {
                $data["INTERVIEW_VALUE2"][TEXT]  = !strlen($interviewval) ? "" : $interviewval;
                $data["INTERVIEW_REMARK2"][TEXT] = !strlen($interviewrmk) ? "" : $interviewrmk;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            if ($chkflg) {
                $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
