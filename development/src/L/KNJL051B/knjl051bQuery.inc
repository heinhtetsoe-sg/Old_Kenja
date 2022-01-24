<?php

require_once('for_php7.php');

class knjl051bQuery extends Query {

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

    //一覧表示
    function SelectQuery($model, $flg)
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
        $query .= " ), MAIN_T AS ( ";
        $query .= " SELECT ";
        if ($flg == "COUNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     RECE.RECEPTNO, ";
            $query .= "     RECE.EXAMNO, ";
            $query .= "     BASE.NAME, ";
            $query .= "     BASE.NAME_KANA, ";
            $query .= "     N1.NAME2 AS SEX, ";
            $query .= "     INTER.INTERVIEW_A AS A_CHECK, ";
            $query .= "     INTER.INTERVIEW_B AS B_CHECK, ";
            $query .= "     INTER.INTERVIEW_C AS C_CHECK ";
        }
        $query .= "  FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RECE ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "           ON RECE.ENTEXAMYEAR   = BASE.ENTEXAMYEAR ";
        $query .= "          AND RECE.EXAMNO        = BASE.EXAMNO ";
        if (strlen($model->special_reason_div)) {
            $query .= "      AND BASE.SPECIAL_REASON_DIV IS NOT NULL ";
        }
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTER ";
        $query .= "           ON RECE.ENTEXAMYEAR   = INTER.ENTEXAMYEAR ";
        $query .= "          AND RECE.APPLICANTDIV  = INTER.APPLICANTDIV ";
        $query .= "          AND RECE.TESTDIV       = INTER.TESTDIV ";
        $query .= "          AND RECE.EXAMNO        = INTER.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "           ON N1.NAMECD1 = 'Z002' ";
        $query .= "          AND N1.NAMECD2 = BASE.SEX ";
        $query .= " WHERE RECE.ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND RECE.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND RECE.TESTDIV        = '".$model->testdiv."' ";
        $query .= "   AND RECE.EXAM_TYPE      = '".$model->exam_type."' ";
        if ($model->cmd == "read") {
            $query .= "   AND RECE.RECEPTNO >= '".$model->field["S_EXAMNO"]."' ";
            if ($flg != "COUNT") {
                $query .= " ORDER BY RECE.RECEPTNO ";
            }
        } else if ($model->cmd == "back") {
            $query .= "   AND RECE.RECEPTNO < '".$model->field["S_EXAMNO"]."' ";
            if ($flg != "COUNT") {
                $query .= " ORDER BY RECE.RECEPTNO DESC ";
            }
        } else if ($model->cmd == "next") {
            $query .= "   AND RECE.RECEPTNO > '".$model->field["E_EXAMNO"]."' ";
            if ($flg != "COUNT") {
                $query .= " ORDER BY RECE.RECEPTNO ";
            }
        }
        $query .= " FETCH FIRST 50 ROWS ONLY ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= "  FROM ";
        $query .= "     MAIN_T ";
        if ($flg != "COUNT") {
            $query .= " ORDER BY RECEPTNO ";
        }
        return $query;
    }

    //更新
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $tmp = explode(",", $model->hid_receptno);
        $receptno_array = array();
        foreach ($tmp as $key => $receptno_count) {
            $receptno_array = explode("-", $receptno_count);
            $query  = " DELETE FROM ENTEXAM_INTERVIEW_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR        = '".$model->ObjYear."' ";
            $query .= "     AND APPLICANTDIV   = '".$model->applicantdiv."' ";
            $query .= "     AND TESTDIV        = '".$model->testdiv."' ";
            $query .= "     AND EXAMNO         = '".$receptno_array[0]."' ";
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["TESTDIV"][TEXT]              = $model->testdiv;
            $data["EXAMNO"][TEXT]               = $receptno_array[0];
            $data["INTERVIEW_A"][TEXT]          = $model->interView_A[$receptno_array[1]];
            $data["INTERVIEW_B"][TEXT]          = $model->interView_B[$receptno_array[1]];
            $data["INTERVIEW_C"][TEXT]          = $model->interView_C[$receptno_array[1]];
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