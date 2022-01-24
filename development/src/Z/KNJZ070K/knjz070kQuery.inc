<?php
class knjz070kQuery extends Query {

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T3.abbv1 ";
        $query .= " FROM ";
        $query .= "     expense_s_mst T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     v_name_mst T3 ";
        $query .= " ON ";
        $query .= "     T1.year = T3.year AND ";
        $query .= "     T3.namecd1 = 'Z002' AND ";
        $query .= "     T3.namecd2 = T1.sex ";
        $query .= " WHERE ";
        $query .= "     T1.year = '".$model->year."'";
        $query .= " ORDER BY ";
        $query .= "     T1.expense_s_cd ";

        return $query;
    }

    //１レコード取得
    function getRow($model,$flg)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     EXPENSE_S_CD, ";
        $query .= "     EXPENSE_S_NAME, ";
        $query .= "     EXPENSE_S_MONEY, ";
        $query .= "     SEX ";
        $query .= " FROM ";
        $query .= "     EXPENSE_S_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' AND ";

        if($flg == 1){
            $query .= "  EXPENSE_S_CD = '".$model->exp_scd."'";
        }elseif($flg == 2){
            $query .= "  EXPENSE_S_CD = '".sprintf("%02d",$model->field["EXPENSE_S_CD"])."'";
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

    //MS_MSTデータ判定
    function getMsmst($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) AS CNT FROM EXPENSE_MS_MST ";
        $query .= " WHERE year          = '".$fields["YEAR"]."'";
        $query .= "   AND expense_s_cd  = '".sprintf("%02d",$fields["EXPENSE_S_CD"])."'";

        $db->query($query);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row["CNT"];
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]                 = $fields["YEAR"];
        $data["EXPENSE_S_CD"][TEXT]         = sprintf("%02d",$fields["EXPENSE_S_CD"]);
        $data["EXPENSE_S_NAME"][TEXT]       = $fields["EXPENSE_S_NAME"];
        $data["EXPENSE_S_MONEY"][NUMBER]    = $fields["EXPENSE_S_MONEY"];
        $data["SEX"][TEXT]                  = $fields["SEX"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "EXPENSE_S_MST");
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

        $data["EXPENSE_S_NAME"][TEXT]       = $fields["EXPENSE_S_NAME"];
        $data["EXPENSE_S_MONEY"][NUMBER]    = $fields["EXPENSE_S_MONEY"];
        $data["SEX"][TEXT]                  = $fields["SEX"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE year          = '".$fields["YEAR"]."'";
        $where .= "   AND expense_s_cd  = '".sprintf("%02d",$fields["EXPENSE_S_CD"])."'";

        $query = Query::updateSQL($data, "EXPENSE_S_MST", $where);
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

        $query  = " DELETE FROM expense_s_mst ";
        $query .= " WHERE year          = '".$fields["YEAR"]."'";
        $query .= "   AND expense_s_cd  = '".sprintf("%02d",$fields["EXPENSE_S_CD"])."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

    //今年度のデータが存在するかチェック
    function CheckData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM expense_s_mst WHERE year = '".$year."'";
        $row = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO EXPENSE_S_MST ";
        $query .= "           ( YEAR, ";
        $query .= "             EXPENSE_S_CD, ";
        $query .= "             EXPENSE_S_NAME, ";
        $query .= "             EXPENSE_S_MONEY, ";
        $query .= "             SEX, ";
        $query .= "             registercd, ";
        $query .= "             updated ) ";
        $query .= "   (SELECT ";
        $query .= "         '".$model->year."', ";
        $query .= "         T1.EXPENSE_S_CD, ";
        $query .= "         T1.EXPENSE_S_NAME, ";
        $query .= "         T1.EXPENSE_S_MONEY, ";
        $query .= "         T1.SEX, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         EXPENSE_S_MST T1 ";
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
