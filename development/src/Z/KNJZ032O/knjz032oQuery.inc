<?php

require_once('for_php7.php');

class knjz032oQuery extends Query {

    //入試年度一覧取得
    function selectYearQuery()
    {
        $query  = "SELECT DISTINCT ENTEXAMYEAR ";
        $query .= "  FROM ENTEXAM_TESTDIV_MST ";
        $query .= " ORDER BY ENTEXAMYEAR DESC";
        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
        $query  = " SELECT MIN(ENTEXAMYEAR) ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR >= '" .$model->year. "'";
        return $query;
    }

    //クラス区分
    function getClassdiv($year)
    {
        //入試種別
        $query  = " WITH T_CLASSDIV (VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1', '1:特別進学') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2', '2:英数特科') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('3', '3:特待生選抜') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_CLASSDIV ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
/***
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L017' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
***/
        return $query;
    }

    //加点対象区分
    function getKatendiv($year)
    {
        $query  = " WITH T_KATENDIV (VALUE, LABEL) AS ( ";
        $query .= "     VALUES('1', '1:加点算出対象入試') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2', '2:加点ポイント対象入試') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     T_KATENDIV ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //データ取得
    function selectQuery($year)
    {
        $query  = "";
        $query .= "  SELECT ";
        $query .= "      T1.* ";
        $query .= "  FROM";
        $query .= "      ENTEXAM_TESTDIV_MST T1 ";
        $query .= "  WHERE";
        $query .= "      T1.ENTEXAMYEAR = '".$year."' ";
        $query .= "  ORDER BY";
        $query .= "      T1.TESTDIV ";
        return $query;
    }

    //１レコード取得
    function getRow($year,$testdiv)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$year."' AND";
        $query .= "     TESTDIV      = '".$testdiv."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["NAME"][TEXT]             = $fields["NAME"];
        $data["ABBV"][TEXT]             = $fields["ABBV"];
        $data["CLASSDIV"][TEXT]         = $fields["CLASSDIV"];
        $data["SHOWDIV"][TEXT]          = $fields["SHOWDIV"];
        $data["CAPACITY"][NUMBER]       = $fields["CAPACITY"];
        $data["TESTDAY"][TEXT]          = str_replace("/", "-", $fields["TESTDAY"]);
        $data["SHOWORDER"][NUMBER]      = $fields["SHOWORDER"];
        $data["KATENDIV"][TEXT]         = $fields["KATENDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_TESTDIV_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["NAME"][TEXT]             = $fields["NAME"];
        $data["ABBV"][TEXT]             = $fields["ABBV"];
        $data["CLASSDIV"][TEXT]         = $fields["CLASSDIV"];
        $data["SHOWDIV"][TEXT]          = $fields["SHOWDIV"];
        $data["CAPACITY"][NUMBER]       = $fields["CAPACITY"];
        $data["TESTDAY"][TEXT]          = str_replace("/", "-", $fields["TESTDAY"]);
        $data["SHOWORDER"][NUMBER]      = $fields["SHOWORDER"];
        $data["KATENDIV"][TEXT]         = $fields["KATENDIV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$fields["ENTEXAMYEAR"]."'";
        $where .= "   AND TESTDIV       = '".$fields["TESTDIV"]."'";

        $query = Query::updateSQL($data, "ENTEXAM_TESTDIV_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_TESTDIV_MST ";
        $query .= "       WHERE ENTEXAMYEAR  = '".$fields["ENTEXAMYEAR"] ."'";
        $query .= "         AND TESTDIV      = '".$fields["TESTDIV"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }


    //次年度のデータを作成
    function &getCopyYearQuery($model)
    {
        $flg    = false;
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT";
        $query .= "     *";
        $query .= " FROM";
        $query .= "     ENTEXAM_TESTDIV_MST T1";
        $query .= " WHERE";
        $query .= "     ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "     NOT EXISTS (SELECT 'X'";
        $query .= "                   FROM ENTEXAM_TESTDIV_MST W1";
        $query .= "                  WHERE W1.ENTEXAMYEAR  = '".$model->NextYear."' AND";
        $query .= "                        W1.TESTDIV      = T1.TESTDIV";
        $query .= "                )";

        //次年度作成できるデータが存在すれば追加
        if (is_array($db->getRow($query, DB_FETCHMODE_ASSOC))) {
            $flg = true;
            $db->autoCommit(false);

            $query  = "";
            $query .= " INSERT INTO ENTEXAM_TESTDIV_MST";
            $query .= " (";
            $query .= "     ENTEXAMYEAR,";
            $query .= "     TESTDIV,";
            $query .= "     NAME,";
            $query .= "     ABBV,";
            $query .= "     CLASSDIV,";
            $query .= "     SHOWDIV,";
            $query .= "     CAPACITY,";
            $query .= "     TESTDAY,";
            $query .= "     SHOWORDER,";
            $query .= "     KATENDIV,";
            $query .= "     REGISTERCD,UPDATED";
            $query .= " )";
            $query .= " SELECT";
            $query .= "     '$model->NextYear',";
            $query .= "     TESTDIV,";
            $query .= "     NAME,";
            $query .= "     ABBV,";
            $query .= "     CLASSDIV,";
            $query .= "     SHOWDIV,";
            $query .= "     CAPACITY,";
            $query .= "     TESTDAY,";
            $query .= "     SHOWORDER,";
            $query .= "     KATENDIV,";
            $query .= "     '".STAFFCD."', sysdate()";
            $query .= " FROM";
            $query .= "     ENTEXAM_TESTDIV_MST T1";
            $query .= " WHERE";
            $query .= "     ENTEXAMYEAR = '".$model->year."' AND";
            $query .= "     NOT EXISTS (SELECT 'X'";
            $query .= "                   FROM ENTEXAM_TESTDIV_MST W1";
            $query .= "                  WHERE W1.ENTEXAMYEAR  = '".$model->NextYear."' AND";
            $query .= "                        W1.TESTDIV      = T1.TESTDIV";
            $query .= "                )";

            $db->query($query);
            $db->commit();
        }

        Query::dbCheckIn($db);

        return $flg;
     }

}
?>
