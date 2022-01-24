<?php

class knjl065iquery extends Query {

    //名称マスタ取得
    function getNameMst($nameCd1, $nameCd2="") {

        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 VALUE ";
        $query .= "   , NAMECD2 || ':' || NAME1 LABEL ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '".(CTRL_YEAR + 1)."' ";
        if ($nameCd1 != "") {
            $query .= "   AND NAMECD1 = '{$nameCd1}' ";
        }
        if ($nameCd2 != "") {
            $query .= "   AND NAMECD2 = '{$nameCd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    function getEntexamTestDivMst($applicantDiv) {

        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "   AND TESTDIV IN ('1', '2') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試科目を取得
    function getTestSubclasscd($model) {
        $query  = "    SELECT ";
        $query .= "        TESTSUBCLASSCD ";
        $query .= "    FROM ";
        $query .= "        ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= "    WHERE ";
        $query .= "        ENTEXAMYEAR       = '{$model->examyear}' ";
        $query .= "        AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "        AND TESTDIV       = '{$model->testdiv}' ";
        $query .= "        AND EXAM_TYPE     = '1' ";

        return $query;
    }

    function selectMainQuery($model) {

        $query .= " SELECT ";
        $query .= "     BASE.EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= " INNER JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE2 ";
        $query .= "      ON BASE2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "     AND BASE2.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "     AND BASE2.EXAMNO = BASE.RECOM_EXAMNO ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR        = '{$model->examyear}' ";
        $query .= "     AND BASE.APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND BASE.TESTDIV        = '{$model->testdiv}' ";
        $query .= "     AND BASE.RECOM_EXAMNO   IS NOT NULL  ";
        $query .= "     AND BASE2.JUDGEMENT     = '1' "; //他試験区分で合格

        return $query;
    }

    //更新 -- ENTEXAM_APPLICANTBASE_DAT
    function &getUpdateBaseDatQuery($model, $examno) {
        //併願受験したA日程または帰国生入試)で既に合格していた場合B日程は除外(未受験)とする

        $data = array();
        $data["JUDGEMENT"][TEXT]    = '5'; //未受験
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where .= " WHERE ";
        $where .= "   ENTEXAMYEAR        = '{$model->examyear}' ";
        $where .= "   AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $where .= "   AND EXAMNO         = '{$examno}' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);

        return $query;
    }
}
?>
