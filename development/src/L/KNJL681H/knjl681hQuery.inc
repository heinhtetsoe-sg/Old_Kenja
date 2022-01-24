<?php
class knjl681hQuery extends Query
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
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
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
        $query .= "     R1.RECEPTNO AS VALUE, ";
        $query .= "     R1.RECEPTNO || '：' || B1.NAME AS LABEL, ";
        $query .= "     R1.TESTDIV, ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.ENTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B1.EXAMNO       = R1.EXAMNO ";
        $query .= "     INNER JOIN GOKAKU_SEQ G1 ";
        $query .= "          ON G1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND G1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND G1.SEQ          = B1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "     R1.ENTEXAMYEAR      = '".$model->examYear."' ";
        $query .= "     AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND R1.EXAM_TYPE    = '1' ";
        $query .= "     AND B1.PROCEDUREDIV = '1' "; // 手続区分 1:済み
        $query .= " ORDER BY ";
        $query .= "     R1.RECEPTNO ";

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

        $queryBase = knjl681hQuery::getBaseDatData($model);
        $result = $db->query($queryBase);
        while ($base = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($base["RECEPTNO"], $passExamNo)) {
                $setEntDiv = "2"; // 辞退
            } else {
                $setEntDiv = "1"; // 入学
            }

            //ENTEXAM_APPLICANTBASE_DAT
            $query = knjl681hQuery::updateBaseDatSql($model, $base["EXAMNO"], $setEntDiv);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //データの追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $queryBase = knjl681hQuery::getBaseDatData($model);
        $result = $db->query($queryBase);
        while ($base = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = array_search($base["RECEPTNO"], array_column($data_arr, "RECEPTNO"));
            if (($key === 0) || ($key > 0)) {
                if ($data_arr[$key]["ERROR"] == "1") {
                    // エラーとなったレコードは処理しない
                    continue;
                }

                $setEntDiv = "2"; // 辞退
            } else {
                $setEntDiv = "1"; // 入学
            }

            //ENTEXAM_APPLICANTBASE_DAT
            $query = knjl681hQuery::updateBaseDatSql($model, $base["EXAMNO"], $setEntDiv);
            $db->query($query);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //ENTEXAM_APPLICANTBASE_DAT更新用SQL
    public function updateBaseDatSql($model, $examno, $setEntDiv)
    {
        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTDIV"][TEXT]           = $setEntDiv;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  = " WHERE ENTEXAMYEAR   = '{$model->examYear}' ";
        $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
        $where .= "   AND EXAMNO        = '{$examno}' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //受験番号　getBaseDatDataと同じ条件
    public function checkReceptNo($model, $receptNo = "")
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
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR  = R1.ENTEXAMYEAR ";
        $query .= "         AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B1.EXAMNO       = R1.EXAMNO ";
        $query .= "     INNER JOIN GOKAKU_SEQ G1 ";
        $query .= "          ON G1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND G1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND G1.SEQ          = B1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "     R1.ENTEXAMYEAR      = '".$model->examYear."' ";
        $query .= "     AND R1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND R1.EXAM_TYPE    = '1' ";
        $query .= "     AND B1.PROCEDUREDIV = '1' "; // 手続区分 1:済み

        return $query;
    }
}
