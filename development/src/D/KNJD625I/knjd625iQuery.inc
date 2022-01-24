<?php

require_once('for_php7.php');

class knjd625iQuery extends Query {

    //模試種別
    function getMockDiv()
    {
        $query  = " SELECT ";
        $query .= "     MOCKCD AS VALUE, ";
        $query .= "     MOCKCD || ':' || VALUE(MOCKNAME1,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科
    function getCourse() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE || ':' || VALUE(L4.COURSECODENAME,'') AS LABEL, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L4 ";
        $query .= "         ON  L4.YEAR = T1.YEAR ";
        $query .= "         AND L4.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試科目
    function getMockSubclass()
    {
        $query  = " SELECT ";
        $query .= "     MOCK_SUBCLASS_CD AS VALUE, ";
        $query .= "     MOCK_SUBCLASS_CD || ':' || VALUE(SUBCLASS_NAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト
    function getList()
    {
        $query  = " SELECT ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.MOCKCD || ':' || VALUE(T2.MOCKNAME1,'') AS MOCKNAME, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSECODE, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE || ':' || VALUE(T3.COURSECODENAME,'') AS COURSECODENAME, ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     T1.MOCK_SUBCLASS_CD || ':' || VALUE(T4.SUBCLASS_NAME,'') AS SUBCLASS_NAME ";
        $query .= " FROM ";
        $query .= "     MOCK_TOTAL_SUBCLASS_DAT T1 ";
        $query .= "     LEFT JOIN MOCK_MST T2 ";
        $query .= "         ON  T2.MOCKCD = T1.MOCKCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T3 ";
        $query .= "         ON  T3.YEAR = T1.YEAR ";
        $query .= "         AND T3.COURSECODE = T1.COURSECODE ";
        $query .= "     LEFT JOIN MOCK_SUBCLASS_MST T4 ";
        $query .= "         ON  T1.MOCK_SUBCLASS_CD = T4.MOCK_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";

        return $query;

    }

    //１レコード取得
    function getMockTotalSubclassDat($mockcd, $cocediv)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT MOCKCD, COURSECD || MAJORCD || COURSECODE AS COURSECODE, MOCK_SUBCLASS_CD ";
        $query .= "  FROM MOCK_TOTAL_SUBCLASS_DAT ";
        $query .= " WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND MOCKCD = '".$mockcd."' ";
        $query .= "   AND COURSECD || MAJORCD || COURSECODE = '".$cocediv."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //--- INSERT
    function &getInsertQuery($field)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["MOCKCD"][TEXT]           = $field["MOCKCD"];
        $data["COURSECD"][TEXT]         = SUBSTR($field["COURSECODE"],0,1);
        $data["MAJORCD"][TEXT]          = SUBSTR($field["COURSECODE"],1,3);
        $data["COURSECODE"][TEXT]       = SUBSTR($field["COURSECODE"],4,4);
        $data["MOCK_SUBCLASS_CD"][TEXT] = $field["MOCK_SUBCLASS_CD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]	    = "sysdate()";

        $query = Query::insertSQL($data, "MOCK_TOTAL_SUBCLASS_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($field)
    {
        $db = Query::dbCheckOut();

        $data["MOCK_SUBCLASS_CD"][TEXT] = $field["MOCK_SUBCLASS_CD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE YEAR = '".CTRL_YEAR."' ";
        $where .= "   AND MOCKCD  = '".$field["MOCKCD"]."' ";
        $where .= "   AND COURSECD || MAJORCD || COURSECODE  = '".$field["COURSECODE"]."' ";

        $query = Query::updateSQL($data, "MOCK_TOTAL_SUBCLASS_DAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($field)
    {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM MOCK_TOTAL_SUBCLASS_DAT ";
        $query .= " WHERE YEAR  = '".CTRL_YEAR."' ";
        $query .= "   AND MOCKCD  = '".$field["MOCKCD"]."' ";
        $query .= "   AND COURSECD || MAJORCD || COURSECODE  = '".$field["COURSECODE"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //データ取得
    function getCopyData($year) {
        $query  =  " SELECT ";
        $query .=  "    COUNT(*) ";
        $query .=  " FROM ";
        $query .=  "    MOCK_TOTAL_SUBCLASS_DAT ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".$year."' ";

        return $query;
    }

    //前年度データ取得
    function getCopyDataOld($year, $flg) {
        $query  =  " SELECT ";
        if ($flg == "cnt") {
            $query .=  "    COUNT(*) ";
        } else {
            $query .=  "    T1.* ";
        }
        $query .=  " FROM ";
        $query .=  "    MOCK_TOTAL_SUBCLASS_DAT T1";
        $query .= "     INNER JOIN COURSE_YDAT T2 ";
        $query .= "         ON  T2.COURSECD = T1.COURSECD ";
        $query .= "         AND T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     INNER JOIN MAJOR_YDAT T3 ";
        $query .= "         ON  T3.COURSECD = T1.COURSECD ";
        $query .= "         AND T3.MAJORCD = T1.MAJORCD ";
        $query .= "         AND T3.YEAR = '".CTRL_YEAR."' ";
        $query .= "     INNER JOIN COURSECODE_YDAT T4 ";
        $query .= "         ON  T4.COURSECODE = T1.COURSECODE ";
        $query .= "         AND T4.YEAR = '".CTRL_YEAR."' ";
        $query .=  " WHERE ";
        $query .=  "    T1.YEAR = '".$year."' ";

        return $query;
    }

    //今年度チェック
    function getyeardata($row) {
        $query  =  " SELECT ";
        $query .=  "    COUNT(*) ";
        $query .=  " FROM ";
        $query .=  "    MOCK_TOTAL_SUBCLASS_DAT ";
        $query .= " WHERE YEAR  = '".CTRL_YEAR."' ";
        $query .= "   AND MOCKCD  = '".$row["MOCKCD"]."' ";
        $query .= "   AND COURSECD = '".$row["COURSECD"]."' ";
        $query .= "   AND MAJORCD  = '".$row["MAJORCD"]."' ";
        $query .= "   AND COURSECODE  = '".$row["COURSECODE"]."' ";

        return $query;
    }

    //前年度コピー処理
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //前年度データ取得
        $query = knjd625iQuery::getCopyDataOld($model->pre_year, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["MOCKCD"][TEXT]           = $row["MOCKCD"];
            $data["COURSECD"][TEXT]         = $row["COURSECD"];
            $data["MAJORCD"][TEXT]          = $row["MAJORCD"];
            $data["COURSECODE"][TEXT]       = $row["COURSECODE"];
            $data["MOCK_SUBCLASS_CD"][TEXT] = $row["MOCK_SUBCLASS_CD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $count = $db->getOne(knjd625iQuery::getyeardata($row));
            if($count == 0){
                //追加
                $query = Query::insertSQL($data, "MOCK_TOTAL_SUBCLASS_DAT");
            } else {
                //更新
                $where  = " WHERE YEAR = '".CTRL_YEAR."' ";
                $where .= "   AND MOCKCD   = '".$row["MOCKCD"]."' ";
                $where .= "   AND COURSECD = '".$row["COURSECD"]."' ";
                $where .= "   AND MAJORCD  = '".$row["MAJORCD"]."' ";
                $where .= "   AND COURSECODE  = '".$row["COURSECODE"]."' ";

                $query = Query::updateSQL($data, "MOCK_TOTAL_SUBCLASS_DAT", $where);
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

}
?>