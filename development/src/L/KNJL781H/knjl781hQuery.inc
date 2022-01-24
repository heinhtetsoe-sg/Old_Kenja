<?php
class knjl781hQuery extends Query
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

    //入試区分取得
    public function getTestdiv($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->examYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リストtoリスト取得
    public function getBaseDatData($model)
    {
        $query  = "";
        $query .= " WITH GOKAKU_SEQ AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR,";
        $query .= "         APPLICANTDIV, ";
        $query .= "         SEQ ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_SETTING_MST ";
        $query .= "     WHERE ";
        $query .= "         SETTING_CD = 'L013' ";
        $query .= "         AND NAMESPARE1 = '1' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     B1.EXAMNO AS VALUE, ";
        $query .= "     B1.EXAMNO || '：' || B1.NAME AS LABEL, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.ENTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     INNER JOIN GOKAKU_SEQ G1 ";
        $query .= "          ON G1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND G1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND G1.SEQ          = B1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR      = '".$model->examYear."' ";
        $query .= "     AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND B1.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND B1.PROCEDUREDIV = '1' "; // 手続区分 1:済み
        $query .= " ORDER BY ";
        $query .= "     B1.EXAMNO ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //更新
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //入学辞退者一覧を配列に格納
        $passExamNo = explode(",", $model->selectdata);

        $queryBase = knjl781hQuery::getBaseDatData($model);
        $result = $db->query($queryBase);
        while ($base = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            if (in_array($base["EXAMNO"], $passExamNo)) {
                $setEntDiv = "2"; // 辞退
            } else {
                $setEntDiv = "1"; // 入学
            }

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTDIV"][TEXT]           = $setEntDiv;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->examYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '{$base["EXAMNO"]}' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
