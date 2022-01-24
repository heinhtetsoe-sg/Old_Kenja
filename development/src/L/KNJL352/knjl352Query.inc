<?php

require_once('for_php7.php');

class knjl352Query extends Query {

    //名称マスタよりデータを取得
    function GetName($ObjYear, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        //名称マスタL900(NAMECD='00') ---DELETE
        $query = " DELETE FROM NAME_MST ";
        $query.= " WHERE NAMECD1 = 'L900' AND";
        $query.= "       NAMECD2 = '00' ";
        $db->query($query);

        //名称マスタL900(NAMECD='00') ---INSERT
        $data = array();
        $data["NAMECD1"][TEXT]      = "L900";
        $data["NAMECD2"][TEXT]      = "00";
        $data["NAME1"][TEXT]        = $model->field["JYUKU_NAME"];
        $data["NAME2"][TEXT]        = $model->field["CLASS_ROOM"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "NAME_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    function getTestdivMst($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV, ";
        $query .= "     NAME, ";
        $query .= "     ABBV, ";
        $query .= "     TESTDAY ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDAY, ";
        $query .= "     TESTDIV ";
        return $query;
    }

    function getStudentSql($model) {
        $query  = " WITH BASE_DETAIL AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         EXAMNO, ";
        $query .= "         REMARK1, ";
        $query .= "         REMARK2 ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "         AND SEQ = '008' ";
        $query .= "         AND REMARK1 IS NOT NULL ";
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         EXAMNO, ";
        $query .= "         REMARK3 AS REMARK1, ";
        $query .= "         REMARK4 AS REMARK2 ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "         AND SEQ = '008' ";
        $query .= "         AND REMARK3 IS NOT NULL ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     BASE_D.EXAMNO, ";
        $query .= "     BASE.PROCEDUREDIV, ";
        $query .= "     BASE.ENTDIV, ";
        $query .= "     BASE.NAME, ";
        $query .= "     Z002.NAME2 AS SEX, ";
        $query .= "     PRI.PRISCHOOLCD, ";
        $query .= "     PRI.PRISCHOOL_NAME, ";
        $query .= "     BASE_D.REMARK2 ";
        $query .= " FROM ";
        $query .= "     BASE_DETAIL BASE_D ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE_D.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "           AND BASE_D.EXAMNO = BASE.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "          AND BASE.SEX = Z002.NAMECD2 ";
        $query .= "     INNER JOIN PRISCHOOL_MST PRI ON BASE_D.REMARK1 = PRI.PRISCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     BASE_D.ENTEXAMYEAR = '{$model->ObjYear}' ";
        if ($model->field["JYUKU_NAME"]) {
            $query .= "     AND PRI.PRISCHOOL_NAME LIKE '%{$model->field["JYUKU_NAME"]}%' ";
        }
        if ($model->field["CLASS_ROOM"]) {
            $query .= "     AND BASE_D.REMARK2 LIKE '%{$model->field["CLASS_ROOM"]}%' ";
        }
        $query .= " ORDER BY ";
        $query .= "     PRI.PRISCHOOLCD, ";
        $query .= "     BASE_D.REMARK2, ";
        $query .= "     BASE_D.EXAMNO ";
        return $query;
    }

    function getReceptSql($model, $examno) {
        $query  = " SELECT ";
        $query .= "     RECEPTNO, ";
        $query .= "     TESTDIV, ";
        $query .= "     TOTAL4, ";
        $query .= "     JUDGEDIV, ";
        $query .= "     HONORDIV, ";
        $query .= "     JUDGECLASS ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAM_TYPE = '2' ";
        $query .= "     AND EXAMNO = '{$examno}' ";
        return $query;
    }
}
?>
