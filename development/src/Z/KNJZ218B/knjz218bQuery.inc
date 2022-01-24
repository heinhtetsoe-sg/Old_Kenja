<?php

require_once('for_php7.php');

class knjz218bQuery extends Query {

    //リスト
    function getList($model)
    {
        $query  = " SELECT ";
        $query .= "   T1.GROUPCD, ";
        $query .= "   T2.GROUPNAME, ";
        $query .= "   T2.GROUPABBV, ";
        $query .= "   T1.MOCKCD, ";
        $query .= "   T3.MOCKNAME1 ";
        $query .= " FROM ";
        $query .= "   MOCK_DISP_GROUP_DAT T1 ";
        $query .= "   LEFT JOIN MOCK_DISP_GROUP_MST T2 ";
        $query .= "     ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.GROUPCD = T1.GROUPCD ";
        $query .= "   LEFT JOIN MOCK_MST T3 ";
        $query .= "     ON T1.MOCKCD = T3.MOCKCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "   T1.GROUPCD, ";
        $query .= "   T3.MOCKCD ";

        return $query;
    }

    //科目数取得
    function getSubclassCnt($field, $div = "")
    {
        $query  = " SELECT ";
        if ($div == "SELECT") {
            $query .= "     MOCKCD ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     MOCK_DISP_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GROUPCD = '".$field["GROUPCD"]."' ";

        return $query;
    }

    //１レコード取得
    function getSelectData($field)
    {
        $query  = " SELECT ";
        $query .= "     GROUPCD, ";
        $query .= "     GROUPNAME, ";
        $query .= "     GROUPABBV ";
        $query .= " FROM ";
        $query .= "     MOCK_DISP_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GROUPCD = '".$field["GROUPCD"]."' ";

        return $query;
    }

    //科目
    function getSubclass($model, $key="")
    {
        $query  = " SELECT ";
        $query .= "   MOCKCD  || ':' || MOCKNAME1 AS LABEL, ";
        $query .= "   MOCKCD AS VALUE ";
        $query .= " FROM ";
        $query .= "   MOCK_MST ";
        $query .= " WHERE ";
        if ($key) {
            $query .= "   MOCKCD = '".$key."' AND ";
        }
        $query .= "   SUBSTR(MOCKCD, 2, 4) = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "   VALUE ";

        return $query;
    }

    //教科
    function getCopyCheck($model, $yearDiv = "NOW")
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "   COUNT(*) ";
        $query .= " FROM ";
        $query .= "   MOCK_DISP_GROUP_DAT MDGD ";
        $query .= "   INNER JOIN MOCK_DISP_GROUP_MST MDGM ";
        $query .= "     ON MDGM.YEAR = MDGD.YEAR ";
        $query .= "     AND MDGM.GROUPCD = MDGD.GROUPCD ";
        $query .= "  WHERE ";
        if ($yearDiv == "NOW") {
            $query .= "     MDGD.YEAR = '".CTRL_YEAR."' ";
        } else {
            $query .= "     MDGD.YEAR = '".(CTRL_YEAR - 1)."' ";
        }

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);

        return $cnt;
    }

    function getLastYearList($model) {
            $query  = " SELECT ";
            $query .= "   MDGM.YEAR, ";
            $query .= "   MDGM.GROUPCD, ";
            $query .= "   MDGM.GROUPNAME, ";
            $query .= "   MDGM.GROUPABBV, ";
            $query .= "   MDGD.MOCKCD ";
            $query .= " FROM ";
            $query .= "   MOCK_DISP_GROUP_DAT MDGD ";
            $query .= "   INNER JOIN MOCK_DISP_GROUP_MST MDGM ";
            $query .= "     ON MDGM.YEAR = MDGD.YEAR ";
            $query .= "     AND MDGM.GROUPCD = MDGD.GROUPCD ";
            $query .= " WHERE ";
            $query .= "   MDGD.YEAR = '".(CTRL_YEAR - 1)."' ";

        return $query;
    }

    //--- COPY
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $query = knjz218bQuery::getLastYearList($model);
        $result = $db->query($query);
        $befGroupCd = "";

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($befGroupCd = "" || $row["GROUPCD"] != $befGroupCd) {
                //MST作成
                $mstData = array();
                $mstData["YEAR"][TEXT]              = CTRL_YEAR;
                $mstData["GROUPCD"][TEXT]           = $row["GROUPCD"];
                $mstData["GROUPNAME"][TEXT]         = $row["GROUPNAME"];
                $mstData["GROUPABBV"][TEXT]         = $row["GROUPABBV"];
                $mstData["REGISTERCD"][TEXT]        = STAFFCD;
                $mstData["UPDATED"][NUMBER]         = "sysdate()";

                $query = Query::insertSQL($mstData, "MOCK_DISP_GROUP_MST");
                $db->query($query);
            }

            $datData = array();
            $datData["YEAR"][TEXT]                  = CTRL_YEAR;
            $datData["GROUPCD"][TEXT]               = $row["GROUPCD"];
            $datData["MOCKCD"][TEXT]                = $row["MOCKCD"];
            $datData["REGISTERCD"][TEXT]            = STAFFCD;
            $datData["UPDATED"][NUMBER]             = "sysdate()";

            $query = Query::insertSQL($datData, "MOCK_DISP_GROUP_DAT");
            $db->query($query);

            $befGroupCd = $row["GROUPCD"];
        }
        $db->commit();
        $result->free();
        Query::dbCheckIn($db);
        return;
    }

    //--- 存在チェック
    function getMockExistsCnt($fields)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "   COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "   MOCK_DISP_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND GROUPCD = '".$fields["GROUPCD"]."' ";

        $cnt = $db->getOne($query);
        return $cnt;
    }

    //--- INSERT
    function &getInsertQuery($fields, $subclass)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjz218bQuery::getDeleteQuery($db, $fields, $subclass);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["GROUPCD"][TEXT]          = $fields["GROUPCD"];
        $data["GROUPNAME"][TEXT]        = $fields["GROUPNAME"];
        $data["GROUPABBV"][TEXT]        = $fields["GROUPABBV"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $query = Query::insertSQL($data, "MOCK_DISP_GROUP_MST");

        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["GROUPCD"][TEXT]          = $fields["GROUPCD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        foreach ($subclass as $subKey => $subVal) {
            $data["MOCKCD"][TEXT]   = $subVal;

            $query = Query::insertSQL($data, "MOCK_DISP_GROUP_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($db, $fields, $selectMock)
    {
        $query  = " DELETE FROM ";
        $query .= "     MOCK_DISP_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GROUPCD = '".$fields["GROUPCD"]."' ";
        $db->query($query);
        $query  = " DELETE FROM ";
        $query .= "     MOCK_DISP_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GROUPCD = '".$fields["GROUPCD"]."' ";
        $db->query($query);
        return $result;
    }
}
?>
