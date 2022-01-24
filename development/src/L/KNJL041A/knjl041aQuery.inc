<?php

require_once('for_php7.php');

class knjl041aQuery extends Query {
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

    //受験種別コンボ
    function getTestdivMst($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL, ";
        $query .= "     DEFAULT_FLG AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     Z002.ABBV1 AS SEX, ";
        $query .= "     FINS.FINSCHOOL_NAME_ABBV, ";
        $query .= "     RCPT.JUDGEDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     LEFT JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "                                         AND BASE.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "                                         AND BASE.TESTDIV      = RCPT.TESTDIV ";
        $query .= "                                         AND BASE.EXAMNO       = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ON Z002.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND Z002.NAMECD1 = 'Z002' ";
        $query .= "                              AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON BASE.FS_CD = FINS.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR  = '".$model->ObjYear."'  ";
        $query .= "     AND RCPT.APPLICANTDIV = '".$model->applicantdiv."'  ";
        $query .= "     AND RCPT.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND RCPT.EXAM_TYPE    = '".$model->examType."' ";
        $query .= " ORDER BY ";
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }

/***** 更新処理 *****/
    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->setReceptNo as $key => $ReceptNo) {
            //更新(ENTEXAM_APPLICANTBASE_DAT)
            $data = array();
            if ($model->arrTesAb[$ReceptNo]) {
                $data["JUDGEDIV"][TEXT]   = $model->arrTesAb[$ReceptNo];
            } else {
                $data["JUDGEDIV"][TEXT]   = "";
            }
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
            $where .= "   AND TESTDIV      = '".$model->testdiv."' ";
            $where .= "   AND RECEPTNO     = '".$ReceptNo."' ";
            $where .= "   AND EXAM_TYPE    = '".$model->examType."' ";
            $where .= "   AND VALUE(JUDGEDIV, '4') = '4' "; //欠席またはNULL

            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>