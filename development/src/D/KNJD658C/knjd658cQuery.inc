<?php

require_once('for_php7.php');
class knjd658cquery extends Query
{
    //模試名コンボ
    public function getMock2Dat()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.MOCKCD AS VALUE, ";
        $query .= "     T1.MOCKCD || ' ' || L1.MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK2_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     MOCK_MST L1 ON L1.MOCKCD = T1.MOCKCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //CSVデータより読込
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            /************/
            /* まず削除 */
            /************/
            $query  = " DELETE FROM ";
            $query .= "     MOCK2_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR             = '{$data_arr[$i]["YEAR"]}' AND ";
            $query .= "     MOCKCD           = '{$data_arr[$i]["MOCKCD"]}' AND ";
            $query .= "     SCHREGNO         = '{$data_arr[$i]["SCHREGNO"]}' AND ";
            $query .= "     MOCK_SUBCLASS_CD = '{$data_arr[$i]["MOCK_SUBCLASS_CD"]}' AND ";
            $query .= "     MOCKDIV IN ('0', '9') ";

            $db->query($query);

            /**************/
            /* インサート */
            /**************/
            $data = array();
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["MOCKCD"][TEXT]           = $data_arr[$i]["MOCKCD"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["MOCK_SUBCLASS_CD"][TEXT] = $data_arr[$i]["MOCK_SUBCLASS_CD"];
            $data["MOCKDIV"][TEXT]          = '0';
            $data["SCORE"][NUMBER]          = $data_arr[$i]["SCORE"];
            $data["AVG"][NUMBER]            = $data_arr[$i]["AVG"];
            $data["DEVIATION"][NUMBER]      = $data_arr[$i]["DEVIATION"];
            $data["RANK"][NUMBER]           = $data_arr[$i]["RANK"];
            $data["COUNT"][NUMBER]          = $data_arr[$i]["COUNT"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][NUMBER]   = "SYSDATE()";

            $query = Query::insertSQL($data, "MOCK2_DAT");
            $db->query($query);


            $data = array();
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["MOCKCD"][TEXT]           = $data_arr[$i]["MOCKCD"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["MOCK_SUBCLASS_CD"][TEXT] = $data_arr[$i]["MOCK_SUBCLASS_CD"];
            $data["MOCKDIV"][TEXT]          = '9';
            $data["SCORE"][NUMBER]          = $data_arr[$i]["SCORE"];
            $data["DEVIATION"][NUMBER]      = $data_arr[$i]["ZENKOKU_HENSATI"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][NUMBER]   = "SYSDATE()";

            $query = Query::insertSQL($data, "MOCK2_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除(CSVデータより読込)
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM ";
            $query .= "     MOCK2_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR             = '{$data_arr[$i]["YEAR"]}' AND ";
            $query .= "     MOCKCD           = '{$data_arr[$i]["MOCKCD"]}' AND ";
            $query .= "     SCHREGNO         = '{$data_arr[$i]["SCHREGNO"]}' AND ";
            $query .= "     MOCK_SUBCLASS_CD = '{$data_arr[$i]["MOCK_SUBCLASS_CD"]}' AND ";
            $query .= "     MOCKDIV IN ('0', '9') ";

            $db->query($query);
            $cnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $year = CTRL_YEAR;

        $query  = " WITH SUB_MAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         YEAR, ";
        $query .= "         MOCKCD, ";
        $query .= "         SCHREGNO, ";
        $query .= "         MOCK_SUBCLASS_CD ";
        $query .= "     FROM ";
        $query .= "         MOCK2_DAT ";
        $query .= "     WHERE ";
        $query .= "         MOCKDIV IN ('0', '9') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     CASE WHEN L1.SCORE IS NOT NULL ";
        $query .= "          THEN L1.SCORE ";
        $query .= "          ELSE L2.SCORE ";
        $query .= "     END AS SCORE, ";
        $query .= "     L1.AVG, ";
        $query .= "     L1.DEVIATION, ";
        $query .= "     L1.RANK, ";
        $query .= "     L1.COUNT, ";
        $query .= "     L2.DEVIATION AS ZENKOKU_HENSATI, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SUB_MAIN T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     MOCK2_DAT L1 ON  L1.YEAR             = T1.YEAR ";
        $query .= "                  AND L1.MOCKCD           = T1.MOCKCD ";
        $query .= "                  AND L1.SCHREGNO         = T1.SCHREGNO ";
        $query .= "                  AND L1.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
        $query .= "                  AND L1.MOCKDIV          = '0' ";
        $query .= " LEFT JOIN ";
        $query .= "     MOCK2_DAT L2 ON  L2.YEAR             = T1.YEAR ";
        $query .= "                  AND L2.MOCKCD           = T1.MOCKCD ";
        $query .= "                  AND L2.SCHREGNO         = T1.SCHREGNO ";
        $query .= "                  AND L2.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
        $query .= "                  AND L2.MOCKDIV          = '9' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$year}' AND ";
        $query .= "     T1.MOCKCD   = '{$model->field["MOCKCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     MOCKCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MOCK_SUBCLASS_CD ";

        return $query;
    }
}
