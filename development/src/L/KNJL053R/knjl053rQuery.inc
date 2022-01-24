<?php

require_once('for_php7.php');

class knjl053rQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //会場
    function getHallName($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXAMHALLCD AS VALUE, ";
        $query .= "     T1.EXAMHALL_NAME || ' (' || T1.S_RECEPTNO || '～' || T1.E_RECEPTNO || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND T1.EXAM_TYPE    = '{$model->exam_type}' ";

        $query .= " ORDER BY ";
        $query .= "     T1.EXAMHALLCD ";
        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {
        $query  = " WITH WISH_T AS ( ";
        $query .= " SELECT ";
        $query .= "     DESIREDIV, ";
        $query .= "     MIN(WISHNO) AS WISHNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_WISHDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR   = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV   = '".$model->testdiv."' ";
        $query .= " GROUP BY ";
        $query .= "     DESIREDIV ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     RECE.RECEPTNO, ";
        $query .= "     RECE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     N1.NAME2 AS SEX, ";
        $query .= "     INTER.INTERVIEW_VALUE AS C_CHECK ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECE ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "           ON RECE.ENTEXAMYEAR   = BASE.ENTEXAMYEAR ";
        $query .= "          AND RECE.APPLICANTDIV  = BASE.APPLICANTDIV ";
        $query .= "          AND RECE.EXAMNO        = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTER ";
        $query .= "           ON RECE.ENTEXAMYEAR   = INTER.ENTEXAMYEAR ";
        $query .= "          AND RECE.APPLICANTDIV  = INTER.APPLICANTDIV ";
        $query .= "          AND RECE.TESTDIV       = INTER.TESTDIV ";
        $query .= "          AND RECE.EXAMNO        = INTER.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_HALL_YDAT HALL ";
        $query .= "           ON RECE.ENTEXAMYEAR   = HALL.ENTEXAMYEAR ";
        $query .= "          AND RECE.APPLICANTDIV  = HALL.APPLICANTDIV ";
        $query .= "          AND RECE.TESTDIV       = HALL.TESTDIV ";
        $query .= "          AND RECE.EXAM_TYPE     = HALL.EXAM_TYPE ";
        $query .= "          AND HALL.EXAMHALLCD    = '".$model->examhallcd."' ";
        $query .= "          AND RECE.RECEPTNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "           ON N1.NAMECD1 = 'Z002' ";
        $query .= "          AND N1.NAMECD2 = BASE.SEX ";
        $query .= " WHERE RECE.ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND RECE.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND RECE.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND RECE.EXAM_TYPE      = '".$model->exam_type."' ";
        $query .= " ORDER BY RECE.RECEPTNO ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        foreach ($model->cCheck as $receptno => $cCheck) {
            $setExamno = $model->examnoArray[$receptno];
            $query  = " DELETE FROM ENTEXAM_INTERVIEW_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR        = '".$model->ObjYear."' ";
            $query .= "     AND APPLICANTDIV   = '".$model->applicantdiv."' ";
            $query .= "     AND TESTDIV        = '".$model->testdiv."' ";
            $query .= "     AND EXAMNO         = '".$setExamno."' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["TESTDIV"][TEXT]              = $model->testdiv;
            $data["EXAMNO"][TEXT]               = $setExamno;
            $data["INTERVIEW_VALUE"][TEXT]      = $cCheck;
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>