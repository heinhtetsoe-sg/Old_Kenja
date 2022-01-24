<?php

require_once('for_php7.php');

class knjl580hQuery extends Query
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

    //入試判別取得
    public function getDistinctId($model)
    {
        $query  = " SELECT ";
        $query .= "     DISTINCT_ID AS VALUE, ";
        $query .= "     DISTINCT_ID || ':' || DISTINCT_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_DISTINCTION_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リストtoリスト取得
    public function getReceptDatData($model, $csv = "")
    {
        $query  = " SELECT ";
        if ($csv) {
            $query .= "     RCPT.DISTINCT_ID , ";
            $query .= "     D007.REMARK1 AS SHIGANSYA_SEQ,";
            $query .= "     D007.REMARK2 AS MIRAI_TESTDIV,";
            $query .= "     BASE.NAME, ";
            $query .= "     Z002.NAME1 AS SEX,";
            $query .= "     D007.REMARK8 AS TEST_NAME_ABBV, ";
        }
        $query .= "     RCPT.EXAMNO AS VALUE, ";
        $query .= "     RCPT.EXAMNO || '：' || BASE.NAME || '(' || value(D007.REMARK8, '') || ')'AS LABEL, ";
        $query .= "     RCPT.ENTEXAMYEAR, ";
        $query .= "     RCPT.APPLICANTDIV, ";
        $query .= "     RCPT.TESTDIV, ";
        $query .= "     RCPT.EXAM_TYPE, ";
        $query .= "     RCPT.RECEPTNO, ";
        $query .= "     RCPT.EXAMNO, ";
        $query .= "     BASE.ENTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                             AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                             AND RCPT.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT D007 ON D007.ENTEXAMYEAR  = RCPT.ENTEXAMYEAR ";
        $query .= "                                             AND D007.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "                                             AND D007.TESTDIV      = RCPT.TESTDIV ";
        $query .= "                                             AND D007.EXAM_TYPE    = RCPT.EXAM_TYPE ";
        $query .= "                                             AND D007.RECEPTNO     = RCPT.RECEPTNO ";
        $query .= "                                             AND D007.SEQ          = '007' ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ON Z002.YEAR    = RCPT.ENTEXAMYEAR ";
        $query .= "                              AND Z002.NAMECD1 = 'Z002' ";
        $query .= "                              AND Z002.NAMECD2 = BASE.SEX ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND RCPT.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND RCPT.DISTINCT_ID  = '".$model->distinctId."' ";
        $query .= "     AND RCPT.JUDGEDIV     = '1' "; // 合格者
        $query .= " ORDER BY ";
        $query .= "     RCPT.EXAMNO ";

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

        //辞退者一覧を配列に格納
        $passExamNo = explode(",", $model->selectdata);

        $queryRecept = knjl580hQuery::getReceptDatData($model);
        $result = $db->query($queryRecept);
        while ($recept = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            /** ENTEXAM_APPLICANTBASE_DAT **/
            $data = array();
            if (in_array($recept["EXAMNO"], $passExamNo)) {
                $setEntDiv = "2"; // 辞退
            } else {
                $setEntDiv = "1"; // 入学
            }
            $data["ENTDIV"][TEXT]           = $setEntDiv;

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR    = '{$recept["ENTEXAMYEAR"]}' ";
            $where .= "   AND APPLICANTDIV   = '{$recept["APPLICANTDIV"]}' ";
            $where .= "   AND EXAMNO         = '{$recept["EXAMNO"]}' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
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
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $data = array();
            $data["ENTDIV"][TEXT]           = $data_arr[$i]["ENTDIV"];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->year}' ";
            $where .= "   AND APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' ";
            $where .= "   AND EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);

            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //名称マスタ登録コードチェック
    public function nameCntSql($model, $namecd, $setcd, $where = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
        $query .= "     AND NAMECD1 = '{$namecd}' ";
        $query .= "     AND NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

        return $query;
    }

    //受験番号
    public function checkExamNo($model, $examNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND DISTINCT_ID  = '{$model->distinctId}' ";
        $query .= "     AND EXAMNO       = '{$examNo}' ";

        return $query;
    }
}
