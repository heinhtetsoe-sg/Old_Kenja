<?php

require_once('for_php7.php');

class knjh660query extends Query
{

    //データ更新
    public function UpdateKansanRankDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " WITH CNT_T AS ( ";
        $query .= " SELECT ";
        $query .= "     CENTERCD, ";
        $query .= "     KANSAN_DIV, ";
        $query .= "     BUNRIDIV, ";
        $query .= "     RANK_RANGE, ";
        $query .= "     RANK_DIV, ";
        $query .= "     CENTERDIV, ";
        $query .= "     COUNT(*) AS KANSAN_CNT ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_KANSAN_RANK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     CENTERCD, ";
        $query .= "     KANSAN_DIV, ";
        $query .= "     BUNRIDIV, ";
        $query .= "     RANK_RANGE, ";
        $query .= "     RANK_DIV, ";
        $query .= "     CENTERDIV ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     RANK_T.CENTERCD, ";
        $query .= "     RANK_T.SCHREGNO, ";
        $query .= "     RANK_T.KANSAN_DIV, ";
        $query .= "     RANK_T.BUNRIDIV, ";
        $query .= "     RANK_T.RANK_RANGE, ";
        $query .= "     RANK_T.RANK_DIV, ";
        $query .= "     RANK_T.CENTERDIV, ";
        $query .= "     CNT_T.KANSAN_CNT, ";
        $query .= "     RANK() OVER(PARTITION BY RANK_T.CENTERCD, RANK_T.KANSAN_DIV, RANK_T.BUNRIDIV, RANK_T.RANK_RANGE, RANK_T.RANK_DIV, RANK_T.CENTERDIV ORDER BY RANK_T.SCORE DESC) AS KANSAN_RANK ";
        $query .= " FROM ";
        $query .= "     MOCK_CENTER_KANSAN_RANK_DAT RANK_T ";
        $query .= "     LEFT JOIN CNT_T ON RANK_T.CENTERCD = CNT_T.CENTERCD ";
        $query .= "          AND RANK_T.KANSAN_DIV = CNT_T.KANSAN_DIV ";
        $query .= "          AND RANK_T.BUNRIDIV = CNT_T.BUNRIDIV ";
        $query .= "          AND RANK_T.RANK_RANGE = CNT_T.RANK_RANGE ";
        $query .= "          AND RANK_T.RANK_DIV = CNT_T.RANK_DIV ";
        $query .= "          AND RANK_T.CENTERDIV = CNT_T.CENTERDIV ";
        $query .= " WHERE ";
        $query .= "     RANK_T.YEAR = '".CTRL_YEAR."' ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["RANK"][NUMBER]      = $row["KANSAN_RANK"];
            $data["CNT"][NUMBER]       = $row["KANSAN_CNT"];
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' ";
            $where .= "     AND CENTERCD = '".$row["CENTERCD"]."' ";
            $where .= "     AND SCHREGNO = '".$row["SCHREGNO"]."' ";
            $where .= "     AND KANSAN_DIV = '".$row["KANSAN_DIV"]."' ";
            $where .= "     AND BUNRIDIV = '".$row["BUNRIDIV"]."' ";
            $where .= "     AND RANK_RANGE = '".$row["RANK_RANGE"]."' ";
            $where .= "     AND RANK_DIV = '".$row["RANK_DIV"]."' ";
            $where .= "     AND CENTERDIV = '".$row["CENTERDIV"]."' ";

            $query = Query::updateSQL($data, "MOCK_CENTER_KANSAN_RANK_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
