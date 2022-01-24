<?php

require_once('for_php7.php');

class knjz060kQuery extends Query {

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.expense_s_money, ";
        $query .= "     T3.name1 ";
        $query .= " FROM ";
        $query .= "     expense_m_mst T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         year,expense_m_cd,SUM(expense_s_money) as expense_s_money ";
        $query .= "      FROM ";
        $query .= "         v_expense_s_mst ";
        $query .= "      WHERE ";
        $query .= "         year = '".$model->year."'";
        $query .= "      GROUP BY ";
        $query .= "         year,expense_m_cd ";
        $query .= "     ) T2 ";
        $query .= " ON ";
        $query .= "     T1.year = T2.year AND ";
        $query .= "     T1.expense_m_cd = T2.expense_m_cd ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     v_name_mst T3 ";
        $query .= " ON ";
        $query .= "     T1.year = T3.year AND ";
        $query .= "     T1.expense_l_cd = T3.namecd2 AND ";
        $query .= "     T3.namecd1 = 'G201' ";
        $query .= " WHERE ";
        $query .= "     T1.year = '".$model->year."'";
        $query .= " ORDER BY ";
        $query .= "     T1.bank_trans_sdate,T1.expense_m_cd ";

        return $query;
    }

    //費目小分類が有りなら小分類の合計金額を取得
    function getSumSmoney($year,$mcd)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT SUM(EXPENSE_S_MONEY) as EXPENSE_S_MONEY ";
        $query .= "   FROM V_EXPENSE_S_MST ";
        $query .= "  WHERE year = '".$year."' AND ";
        $query .= "        expense_m_cd = '".$mcd."'";
        $query .= "  GROUP BY ";
        $query .= "        year,expense_m_cd ";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    //１レコード取得
    function getRow($model,$flg)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.EXPENSE_M_CD, ";
        $query .= "     T1.EXPENSE_L_CD, ";
        $query .= "     T1.EXPENSE_M_NAME, ";
        $query .= "     T1.EXPENSE_S_EXIST_FLG, ";
        $query .= "     T1.EXPENSE_M_MONEY, ";
        $query .= "     T1.DUE_DATE, ";
        $query .= "     T1.BANK_TRANS_SDATE, ";
        $query .= "     T2.EXPENSE_S_MONEY ";
        $query .= " FROM ";
        $query .= "     EXPENSE_M_MST T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         YEAR,EXPENSE_M_CD,SUM(EXPENSE_S_MONEY) as EXPENSE_S_MONEY ";
        $query .= "      FROM ";
        $query .= "         V_EXPENSE_S_MST ";
        $query .= "      WHERE ";
        $query .= "         YEAR = '".$model->year."'";
        $query .= "      GROUP BY ";
        $query .= "         YEAR,EXPENSE_M_CD ";
        $query .= "     ) T2 ";
        $query .= " ON ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.EXPENSE_M_CD = T2.EXPENSE_M_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' AND ";

        if($flg == 1){
            $query .= "  T1.EXPENSE_M_CD = '".$model->exp_mcd."'";
        }elseif($flg == 2){
            $query .= "  T1.EXPENSE_M_CD = '".sprintf("%02d",$model->field["EXPENSE_M_CD"])."'";
        }

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //名称マスタ取得
    function getName($year,$cd)
    {
        $query  = " SELECT namecd2,name1 FROM v_name_mst WHERE year = '".$year."' AND namecd1 = '".$cd."' ";
        return $query;
    }

    //同じ大分類コード、自動振替日で登録されているデータのチェック
    function ExistData($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' ";
        $query .= "   FROM expense_m_mst ";
        $query .= "  WHERE year = '".$model->year."' AND ";
        $query .= "        expense_m_cd <> '".$model->field["EXPENSE_M_CD"]."' AND ";
        $query .= "        expense_l_cd = '".$model->field["EXPENSE_L_CD"]."' AND ";
        $query .= "        bank_trans_sdate = '".str_replace("/", "-", $model->field["BANK_TRANS_SDATE"])."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    //小分類コードが存在するかチェック
    function CheckScode($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' ";
        $query .= "   FROM v_expense_s_mst ";
        $query .= "  WHERE year = '".$model->year."' AND ";
        $query .= "        expense_m_cd = '".$model->exp_mcd."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]                 = $fields["YEAR"];
        $data["EXPENSE_M_CD"][TEXT]         = sprintf("%02d",$fields["EXPENSE_M_CD"]);
        $data["EXPENSE_L_CD"][TEXT]         = $fields["EXPENSE_L_CD"];
        $data["EXPENSE_M_NAME"][TEXT]       = $fields["EXPENSE_M_NAME"];

        if ($fields["EXPENSE_S_EXIST_FLG"] == "1") {
            $fields["EXPENSE_M_MONEY"] = "";
        } else {
            $fields["EXPENSE_S_EXIST_FLG"] = "0";
        }
        $data["EXPENSE_S_EXIST_FLG"][TEXT]  = $fields["EXPENSE_S_EXIST_FLG"];
        $data["EXPENSE_M_MONEY"][NUMBER]    = $fields["EXPENSE_M_MONEY"];
        $data["DUE_DATE"][TEXT]             = str_replace("/", "-", $fields["DUE_DATE"]);
        $data["BANK_TRANS_SDATE"][TEXT]     = str_replace("/", "-", $fields["BANK_TRANS_SDATE"]);
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "EXPENSE_M_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["EXPENSE_L_CD"][TEXT]         = $fields["EXPENSE_L_CD"];
        $data["EXPENSE_M_NAME"][TEXT]       = $fields["EXPENSE_M_NAME"];

        if ($fields["EXPENSE_S_EXIST_FLG"] == "1") {
            $fields["EXPENSE_M_MONEY"] = "";
        } else {
            $fields["EXPENSE_S_EXIST_FLG"] = "0";
        }
        $data["EXPENSE_S_EXIST_FLG"][TEXT]  = $fields["EXPENSE_S_EXIST_FLG"];
        $data["EXPENSE_M_MONEY"][NUMBER]    = $fields["EXPENSE_M_MONEY"];
        $data["DUE_DATE"][TEXT]             = str_replace("/", "-", $fields["DUE_DATE"]);
        $data["BANK_TRANS_SDATE"][TEXT]     = str_replace("/", "-", $fields["BANK_TRANS_SDATE"]);
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE year          = '".$fields["YEAR"]."'";
        $where .= "   AND expense_m_cd  = '".sprintf("%02d",$fields["EXPENSE_M_CD"])."'";

        $query = Query::updateSQL($data, "EXPENSE_M_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM expense_m_mst ";
        $query .= " WHERE year          = '".$fields["YEAR"]."'";
        $query .= "   AND expense_m_cd  = '".sprintf("%02d",$fields["EXPENSE_M_CD"])."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //今年度のデータが存在するかチェック
    function CheckData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM expense_m_mst WHERE year = '".$year."'";
        $row = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $row;
    }

    //前年度の大分類コードが今年度に設定されているかチェック
    function CheckLcode($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' ";
        $query .= "   FROM v_name_mst T1 ";
        $query .= "  INNER JOIN expense_m_mst T2 ";
        $query .= "          ON T1.year = '".$year."' AND ";
        $query .= "             T1.namecd1 = 'G201' AND ";
        $query .= "             T1.namecd2 = T2.expense_l_cd ";
        $query .= "  WHERE T2.year = '".($year - 1)."'";
        
        $row = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO EXPENSE_M_MST ";
        $query .= "           ( YEAR, ";
        $query .= "             EXPENSE_M_CD, ";
        $query .= "             EXPENSE_L_CD, ";
        $query .= "             EXPENSE_M_NAME, ";
        $query .= "             EXPENSE_S_EXIST_FLG, ";
        $query .= "             EXPENSE_M_MONEY, ";
        $query .= "             DUE_DATE, ";
        $query .= "             BANK_TRANS_SDATE, ";
        $query .= "             registercd, ";
        $query .= "             updated ) ";
        $query .= "   (SELECT ";
        $query .= "         '".$model->year."', ";
        $query .= "         T1.EXPENSE_M_CD, ";
        $query .= "         T1.EXPENSE_L_CD, ";
        $query .= "         T1.EXPENSE_M_NAME, ";
        $query .= "         T1.EXPENSE_S_EXIST_FLG, ";
        $query .= "         T1.EXPENSE_M_MONEY, ";
        $query .= "         T1.DUE_DATE, ";
        $query .= "         T1.BANK_TRANS_SDATE, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         EXPENSE_M_MST T1 ";
        $query .= "     INNER JOIN ";
        $query .= "         V_NAME_MST T2 ";
        $query .= "     ON ";
        $query .= "         T2.year = '".$model->year."' AND ";
        $query .= "         T2.namecd1 = 'G201' AND ";
        $query .= "         T1.expense_l_cd = T2.namecd2 ";
        $query .= "     WHERE ";
        $query .= "         T1.year = '".($model->year - 1)."'";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
