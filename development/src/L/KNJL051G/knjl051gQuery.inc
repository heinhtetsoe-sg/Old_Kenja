<?php

require_once('for_php7.php');

class knjl051gQuery extends Query {

    //学校コードを取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

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

    //志望コース取得
    function getEntexamCourseMst($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COURSECD || MAJORCD || EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV      = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場取得
    function getEntexamHallYdat($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     EXAMHALLCD AS VALUE, ";
        $query .= "     EXAMHALL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "     TESTDIV      = '{$model->testdiv}' AND ";
        if ($model->isKeiai == "1") {
            $query .= "     EXAM_TYPE    = '{$model->shdiv}' ";
        } else {
            $query .= "     EXAM_TYPE    = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.RECEPTNO, ";
        $query .= "         T1.EXAMNO, ";
        $query .= "         S1.NAME, ";
        $query .= "         L1.INTERVIEW_VALUE, ";
        $query .= "         L1.INTERVIEW_REMARK, ";
        $query .= "         S1.REMARK1 ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT T1 ";
        $query .= "         INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT S1 ";
        $query .= "              ON S1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND S1.EXAMNO           = T1.EXAMNO ";
        $query .= "             AND S1.SHDIV            = '{$model->shdiv}' ";
        $query .= "             AND S1.DAI1_COURSECD || S1.DAI1_MAJORCD || S1.DAI1_COURSECODE = '{$model->examcoursecd}' ";
        $query .= "         INNER JOIN ENTEXAM_HALL_YDAT H1 ";
        $query .= "              ON H1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND H1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND H1.TESTDIV          = T1.TESTDIV ";
        if ($model->isKeiai == "1") {
            $query .= "             AND H1.EXAM_TYPE        = '{$model->shdiv}' ";
        } else {
            $query .= "             AND H1.EXAM_TYPE        = '1' ";
        }
        $query .= "             AND H1.EXAMHALLCD       = '{$model->examhallcd}' ";
        $query .= "             AND T1.RECEPTNO BETWEEN H1.S_RECEPTNO AND H1.E_RECEPTNO ";
        $query .= "         LEFT JOIN ENTEXAM_INTERVIEW_DAT L1 ";
        $query .= "              ON L1.ENTEXAMYEAR      = T1.ENTEXAMYEAR ";
        $query .= "             AND L1.APPLICANTDIV     = T1.APPLICANTDIV ";
        $query .= "             AND L1.TESTDIV          = T1.TESTDIV ";
        $query .= "             AND L1.EXAMNO           = T1.EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "         T1.APPLICANTDIV = '{$model->applicantdiv}' AND ";
        $query .= "         T1.TESTDIV      = '{$model->testdiv}' ";
        if (strlen($model->special_reason_div)) {
            $query .= "         AND T1.RECEPTNO IN (";
            $query .= "               SELECT ";
            $query .= "                   W1.RECEPTNO ";
            $query .= "               FROM ";
            $query .= "                   ENTEXAM_RECEPT_DAT W1 ";
            $query .= "                   INNER JOIN ENTEXAM_APPLICANTBASE_DAT W2 ";
            $query .= "                         ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR ";
            $query .= "                        AND W2.EXAMNO = W1.EXAMNO ";
            $query .= "                        AND W2.SPECIAL_REASON_DIV IS NOT NULL ";
            $query .= "               WHERE ";
            $query .= "                       W1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $query .= "                   AND W1.APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "                   AND W1.TESTDIV      = '".$model->testdiv."' ";
            $query .= "             )";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " ORDER BY ";
        $query .= "     RECEPTNO ";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_INTERVIEW_DAT ";
        $query .= " WHERE ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND EXAMNO IN ('".implode("','",$model->examno)."')";
        $db->query($query);

        //追加
        foreach ($model->interview_value as $receptno => $value) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAMNO"][TEXT]           = $model->examno[$receptno];
            $data["INTERVIEW_REMARK"][TEXT] = $model->interview_remark[$receptno];
            $data["INTERVIEW_VALUE"][TEXT]  = $value;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
            $db->query($query);

            //監督者情報
            $data = array();
            $data["REMARK1"][TEXT]          = $model->remark1[$receptno];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $where .= "   AND EXAMNO        = '".$model->examno[$receptno]."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>