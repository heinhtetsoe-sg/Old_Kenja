<?php

require_once('for_php7.php');

class knjl780hQuery extends Query
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
        $query .= "     YEAR    = '" . $year . "' AND ";
        $query .= "     NAMECD1 = '" . $namecd1 . "' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '" . $namecd2 . "' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試種別取得
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
    public function getBaseDatData($model, $examNo = "")
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     B1.EXAMNO AS VALUE, ";
        $query .= "     B1.EXAMNO || '：' || B1.NAME AS LABEL, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.PROCEDUREDIV, ";
        $query .= "     B1.PROCEDUREDATE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     INNER JOIN ENTEXAM_SETTING_MST S1 ";
        $query .= "          ON S1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND S1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND S1.SETTING_CD   = 'L013' ";
        $query .= "         AND S1.NAMESPARE1   = '1' ";
        $query .= "         AND S1.SEQ          = B1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR          = '{$model->examYear}' ";
        $query .= "     AND B1.APPLICANTDIV     = '{$model->applicantdiv}' ";
        $query .= "     AND B1.TESTDIV          = '{$model->testdiv}' ";
        if (strlen($examNo)) {
            $query .= "     AND B1.EXAMNO           = '{$examNo}' ";
        }
        $query .= "     AND VALUE(B1.ENTDIV,'') != '2' "; // 辞退者は除く
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

        //手続終了者一覧を配列に格納
        $passExamNo = explode(",", $model->selectdata);

        $queryBase = knjl780hQuery::getBaseDatData($model);
        $result = $db->query($queryBase);
        while ($base = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($base["EXAMNO"], $passExamNo)) {
                //手続した人の手続日は上書きしない
                $setProDiv = "1"; // 手続済
                $setProDate = ($base["PROCEDUREDATE"]) ? $base["PROCEDUREDATE"] : str_replace('/', '-', $model->field["PROCEDUREDATE"]); // 手続日
            } else {
                $setProDiv = "2"; // 未手続
                $setProDate = "";
            }

            //ENTEXAM_APPLICANTBASE_DAT
            $query = knjl780hQuery::updateBaseDatSql($model, $base["EXAMNO"], $setProDiv, $setProDate);
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
        for ($i = 0; $i < count($data_arr); $i++) {
            if (trim($data_arr[$i]["PAY_INFO_DIV"]) == "完了") {
                //手続した人の手続日は上書きしない
                $query = knjl780hQuery::getBaseDatData($model, $data_arr[$i]["EXAMNO"]);
                $base = $db->getRow($query, DB_FETCHMODE_ASSOC);

                $setProDiv = "1"; // 手続済
                $setProDate = ($base["PROCEDUREDATE"]) ? $base["PROCEDUREDATE"] : str_replace('/', '-', $model->field["PROCEDUREDATE"]); // 手続日
            } else {
                $setProDiv = "2"; // 未手続
                $setProDate = "";
            }

            //ENTEXAM_APPLICANTBASE_DAT
            $query = knjl780hQuery::updateBaseDatSql($model, $data_arr[$i]["EXAMNO"], $setProDiv, $setProDate);
            $db->query($query);

            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //ENTEXAM_APPLICANTBASE_DAT更新用SQL
    public function updateBaseDatSql($model, $examno, $setProDiv, $setProDate)
    {
        //PROCEDUREDIV=1(手続済)の時、ENTDIV=1で更新
        if ($setProDiv == "1") {
            $setEntDiv = "1";
        //PROCEDUREDIV=1(手続済)以外の場合、ENTDIV=NULLで更新
        } else {
            $setEntDiv = "";
        }
        
        $data = array();
        $data["PROCEDUREDIV"][TEXT]     = $setProDiv;
        $data["PROCEDUREDATE"][TEXT]    = $setProDate;
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
    public function checkExamNo($model, $examNo)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     INNER JOIN ENTEXAM_SETTING_MST S1 ";
        $query .= "          ON S1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "         AND S1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "         AND S1.SETTING_CD   = 'L013' ";
        $query .= "         AND S1.NAMESPARE1   = '1' ";
        $query .= "         AND S1.SEQ          = B1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR      = '{$model->examYear}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND B1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND B1.EXAMNO       = '{$examNo}' ";
        $query .= "     AND VALUE(B1.ENTDIV,'') != '2' "; // 辞退者は除く

        return $query;
    }
}
