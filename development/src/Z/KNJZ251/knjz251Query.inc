<?php

require_once('for_php7.php');

class knjz251Query extends Query {

    //登録データ一覧
    function getDataAll($year)
    {

        $query  = "WITH KIND_TBL AS (";
        $query .= "SELECT t1.certif_kindcd,";
        $query .= "       CASE WHEN t2.kindname is null OR t2.kindname = ''";
        $query .= "            THEN t1.kindname";
        $query .= "            ELSE t2.kindname END AS KINDNAME,";
        $query .= "       CASE WHEN t2.certif_kindcd is null OR t2.certif_kindcd = ''";
        $query .= "            THEN 'yellow'";
        $query .= "            ELSE 'white' END AS KINDCOLOR";
        $query .= " FROM v_certif_kind_mst t1";
        $query .= "      LEFT JOIN certif_school_dat t2 ON t2.year = t1.year";
        $query .= "                AND t2.certif_kindcd = t1.certif_kindcd";
        $query .= " WHERE t1.year = '{$year}'";
        $query .= ")";
        $query .= "SELECT * FROM KIND_TBL";
        $query .= " UNION ALL";
        $query .= " SELECT certif_kindcd,";
        $query .= "       kindname,";
        $query .= "       'white' AS KINDCOLOR";
        $query .= " FROM certif_school_dat";
        $query .= " WHERE year = '{$year}'";
        $query .= "       AND certif_kindcd NOT IN (SELECT certif_kindcd FROM KIND_TBL)";
        $query .= " ORDER BY";
        $query .= "     certif_kindcd";

        return $query;
    }

    function getLastYear() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CERTIF_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . (CTRL_YEAR - 1) . "' AND ";
        $query .= "     CERTIF_KINDCD >= '100' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     CERTIF_SCHOOL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '" . (CTRL_YEAR - 1) . "' ";
        $query .= "     AND T1.CERTIF_KINDCD < '100' ";
        $query .= "     AND EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     CERTIF_KIND_YDAT T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR = T2.YEAR ";
        $query .= "                     AND T1.CERTIF_KINDCD = T2.CERTIF_KINDCD ";
        $query .= "                ) ";

        return $query;
    }

    //選択対象データ取得
    function getDispData($cd)
    {
        $dataflg = knjz251Query::checkCertif($cd,CTRL_YEAR);
        $db = Query::dbCheckOut();

        if ($dataflg > 0) {
            $query  = "SELECT t1.CERTIF_KINDCD, ";
            $query .= "       CASE WHEN t1.kindname is null OR t1.kindname = ''";
            $query .= "            THEN t2.kindname";
            $query .= "            ELSE t1.kindname END AS KINDNAME,";
            $query .= "       t1.CERTIF_NO,";
            $query .= "       t1.SYOSYO_NAME,";
            $query .= "       t1.SYOSYO_NAME2,";
            $query .= "       t1.SCHOOL_NAME,";
            $query .= "       t1.JOB_NAME,";
            $query .= "       t1.PRINCIPAL_NAME,";
            $query .= "       t1.REMARK1,";
            $query .= "       t1.REMARK2,";
            $query .= "       t1.REMARK3,";
            $query .= "       t1.REMARK4,";
            $query .= "       t1.REMARK5,";
            $query .= "       t1.REMARK6,";
            $query .= "       t1.REMARK7,";
            $query .= "       t1.REMARK8,";
            $query .= "       t1.REMARK9,";
            $query .= "       t1.REMARK10";
            $query .= " FROM certif_school_dat t1";
            $query .= "      LEFT JOIN v_certif_kind_mst t2 ON t2.year = t1.year";
            $query .= "                AND t2.certif_kindcd = t1.certif_kindcd";
            $query .= " WHERE t1.year = '" .CTRL_YEAR. "'";
            $query .= "       AND t1.certif_kindcd = '" .$cd ."'";
        } else {
            $query  = "SELECT CERTIF_KINDCD, ";
            $query .= "       KINDNAME,";
            $query .= "       '' AS CERTIF_NO,";
            $query .= "       '' AS SYOSYO_NAME,";
            $query .= "       '' AS SYOSYO_NAME2,";
            $query .= "       '' AS SCHOOL_NAME,";
            $query .= "       '' AS JOB_NAME,";
            $query .= "       '' AS PRINCIPAL_NAME";
            $query .= " FROM v_certif_kind_mst";
            $query .= " WHERE year = '" .CTRL_YEAR. "'";
            $query .= "       AND certif_kindcd = '" .$cd ."'";
        }
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //前年度にデータがあるかどうかチェック
    function countThisYear($year) {
        $query  = "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    CERTIF_SCHOOL_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR='{$year}' ";

        return $query;
    }

    //証明書学校データチェック
    function checkCertif($cd,$year,$type = "on")
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT COUNT(*) AS DATAFLG ";
        $query .= " FROM certif_school_dat ";
        $query .= " WHERE year = '" .$year. "'";
        if ($type == "on") {
            $query .= "       AND certif_kindcd = '" .$cd ."'";
        }
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row["DATAFLG"];
    }

    //INSERT
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["CERTIF_KINDCD"][TEXT]    = $fields["CERTIF_KINDCD"];
        $data["KINDNAME"][TEXT]         = $fields["KINDNAME"];
        $data["CERTIF_NO"][TEXT]        = $fields["CERTIF_NO"];
        $data["SYOSYO_NAME"][TEXT]      = $fields["SYOSYO_NAME"];
        $data["SYOSYO_NAME2"][TEXT]     = $fields["SYOSYO_NAME2"];
        $data["SCHOOL_NAME"][TEXT]      = $fields["SCHOOL_NAME"];
        $data["JOB_NAME"][TEXT]         = $fields["JOB_NAME"];
        $data["PRINCIPAL_NAME"][TEXT]   = $fields["PRINCIPAL_NAME"];
        for ($i = 1; $i <= 10; $i++) {
            $data["REMARK".$i][TEXT]   = $fields["REMARK".$i];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "certif_school_dat");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["CERTIF_KINDCD"][TEXT]    = $fields["CERTIF_KINDCD"];
        $data["KINDNAME"][TEXT]         = $fields["KINDNAME"];
        $data["CERTIF_NO"][TEXT]        = $fields["CERTIF_NO"];
        $data["SYOSYO_NAME"][TEXT]      = $fields["SYOSYO_NAME"];
        $data["SYOSYO_NAME2"][TEXT]     = $fields["SYOSYO_NAME2"];
        $data["SCHOOL_NAME"][TEXT]      = $fields["SCHOOL_NAME"];
        $data["JOB_NAME"][TEXT]         = $fields["JOB_NAME"];
        $data["PRINCIPAL_NAME"][TEXT]   = $fields["PRINCIPAL_NAME"];
        for ($i = 1; $i <= 10; $i++) {
            $data["REMARK".$i][TEXT]   = $fields["REMARK".$i];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE year = '" .CTRL_YEAR. "'";
        $where .= "       AND certif_kindcd = '" .$fields["CERTIF_KINDCD"]. "'";
        $query = Query::updateSQL($data, "certif_school_dat", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //COPY
    function &getCopyQuery($year)
    {
        $db = Query::dbCheckOut();

        $query  = "INSERT INTO certif_school_dat";
        $query .= " select ";
        $query .= "     '" .CTRL_YEAR. "',";
        $query .= "     CERTIF_KINDCD,";
        $query .= "     KINDNAME,";
        $query .= "     CERTIF_NO,";
        $query .= "     SYOSYO_NAME,";
        $query .= "     SYOSYO_NAME2,";
        $query .= "     SCHOOL_NAME,";
        $query .= "     JOB_NAME,";
        $query .= "     PRINCIPAL_NAME,";
        $query .= "     REMARK1,";
        $query .= "     REMARK2,";
        $query .= "     REMARK3,";
        $query .= "     REMARK4,";
        $query .= "     REMARK5,";
        $query .= "     REMARK6,";
        $query .= "     REMARK7,";
        $query .= "     REMARK8,";
        $query .= "     REMARK9,";
        $query .= "     REMARK10,";
        $query .= "     '" .STAFFCD. "',";
        $query .= "     sysdate()";
        $query .= " from certif_school_dat";
        $query .= " where year = '" .$year. "'";
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM certif_school_dat";
        $query .= " WHERE year = '" .CTRL_YEAR. "'";
        $query .= "       AND certif_kindcd = '" .$fields["CERTIF_KINDCD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

}
?>
