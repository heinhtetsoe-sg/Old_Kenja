<?php

require_once('for_php7.php');

class knjl081aQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験コンボ
    public function getTestdivMst($model)
    {
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

    //出身学校コンボ
    public function getFinschoolMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     FS_CD AS VALUE, ";
        $query .= "     FINSCHOOL_NAME_ABBV AS LABEL, ";
        $query .= "     NULL AS NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST FINS ";
        $query .= "     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "         AND BASE.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "         AND BASE.TESTDIV        = '1' ";
        $query .= "         AND BASE.FS_CD          = FINS.FINSCHOOLCD ";
        $query .= "         AND BASE.ENTDIV IS NOT NULL ";
        $query .= " WHERE ";
        $query .= "     EXISTS ( ";
        $query .= "           SELECT ";
        $query .= "               'X' ";
        $query .= "           FROM ";
        $query .= "               ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "           INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT AD030 ";
        $query .= "                ON AD030.ENTEXAMYEAR   = RECEPT.ENTEXAMYEAR ";
        $query .= "               AND AD030.APPLICANTDIV  = RECEPT.APPLICANTDIV ";
        $query .= "               AND AD030.EXAMNO        = RECEPT.EXAMNO ";
        $query .= "               AND AD030.SEQ           = '030' ";
        $query .= "               AND AD030.REMARK1       = RECEPT.TESTDIV ";
        $query .= "               AND AD030.REMARK2       = RECEPT.RECEPTNO ";
        $query .= "           WHERE ";
        $query .= "               RECEPT.ENTEXAMYEAR        = BASE.ENTEXAMYEAR ";
        $query .= "               AND RECEPT.APPLICANTDIV   = BASE.APPLICANTDIV ";
        $query .= "               AND RECEPT.TESTDIV        = '{$model->testdiv}' ";
        $query .= "               AND RECEPT.EXAMNO         = BASE.EXAMNO ";
        $query .= "               AND RECEPT.EXAM_TYPE      = '".$model->examType."' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     Z002.ABBV1 AS SEX, ";
        $query .= "     L012.NAME1 AS COURSENAME, ";
        $query .= "     FINS.FINSCHOOL_NAME_ABBV, ";
        $query .= "     BASE.PROCEDUREDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO         = RCPT.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT AD030 ";
        $query .= "          ON AD030.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ";
        $query .= "         AND AD030.APPLICANTDIV  = RCPT.APPLICANTDIV ";
        $query .= "         AND AD030.EXAMNO        = RCPT.EXAMNO ";
        $query .= "         AND AD030.SEQ           = '030' ";
        $query .= "         AND AD030.REMARK1       = RCPT.TESTDIV ";
        $query .= "         AND AD030.REMARK2       = RCPT.RECEPTNO ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ";
        $query .= "          ON Z002.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND Z002.NAMECD1    = 'Z002' ";
        $query .= "         AND Z002.NAMECD2    = BASE.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON BASE.FS_CD = FINS.FINSCHOOLCD ";
        $query .= "     LEFT JOIN V_NAME_MST L003 ";
        $query .= "          ON L003.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L003.NAMECD1    = 'L003' ";
        $query .= "         AND L003.NAMECD2    = BASE.APPLICANTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L012 ";
        $query .= "          ON L012.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L012.NAMECD1    = 'L' || VALUE(L003.NAMESPARE3,'H') || '12' ";
        $query .= "         AND L012.NAMECD2    = BASE.ENTDIV ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "     AND RCPT.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "     AND RCPT.TESTDIV        = '".$model->testdiv."' ";
        if ($model->fs_cd != "ALL") {
            $query .= "     AND BASE.FS_CD      = '".$model->fs_cd."' ";
        }
        $query .= "     AND RCPT.EXAM_TYPE      = '".$model->examType."' ";
        $query .= "     AND BASE.ENTDIV IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     RCPT.RECEPTNO ";

        return $query;
    }

    //更新処理
    public function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新
        foreach ($model->setReceptNo as $key => $ReceptNo) {
            $examno = $model->arrExamNo[$ReceptNo];

            $data = array();
            $data["PROCEDUREDIV"][TEXT] = $model->arrProcedureDiv[$ReceptNo];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO        = '".$examno."' ";

            $db->query(Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where));
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
