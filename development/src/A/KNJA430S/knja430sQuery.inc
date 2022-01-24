<?php

require_once('for_php7.php');

class knja430sQuery extends Query {

    function getStaffYear() {
        $query  = " SELECT ";
        $query .= "     YEAR as LABEL, ";
        $query .= "     YEAR as VALUE ";
        $query .= " FROM ";
        $query .= "     STAFF_YDAT ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";

        return $query;
    }

    //職員リスト取得
    function getStaffList($year, $code, $name, $kana)
    {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFFNAME, ";
        $query .= "     T1.STAFFNAME_KANA ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST T1 ";
        $query .= " WHERE ";
        if (strlen($code)) {
            $query .= " T1.STAFFCD LIKE '%{$code}%' AND ";
        }
        if (strlen($name)) {
            $query .= " T1.STAFFNAME LIKE '%{$name}%' AND ";
        }
        if (strlen($kana)) {
            $query .= " T1.STAFFNAME_KANA LIKE '%{$kana}%' AND ";
        }
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.STAFFCD";
        return $query;
    }

    //印鑑番号リスト取得
    function getStampList($code)
    {
        $query  = " SELECT ";
        $query .= "     L1.STAMP_NO, ";
        $query .= "     L1.DIST, ";
        $query .= "     L1.START_DATE, ";
        $query .= "     L1.STOP_DATE, ";
        $query .= "     L1.START_REASON, ";
        $query .= "     L1.STOP_REASON, ";
        $query .= "     L1.DATE ";
        $query .= " FROM ";
        $query .= "     ATTEST_INKAN_DAT L1 ";
        $query .= " WHERE ";
        $query .= "     L1.STAFFCD = '{$code}' ";
        $query .= " ORDER BY ";
        $query .= "     L1.STAMP_NO";
        return $query;
    }

    function getStaffName($code)
    {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '{$code}' ";
        return $query;
    }

    //印鑑番号取得
    function getStampRow($code)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     L1.STAMP_NO, ";
        $query .= "     L1.DIST, ";
        $query .= "     L1.START_DATE, ";
        $query .= "     L1.STOP_DATE, ";
        $query .= "     L1.START_REASON, ";
        $query .= "     L1.STOP_REASON, ";
        $query .= "     L1.DATE ";
        $query .= " FROM ";
        $query .= "     ATTEST_INKAN_DAT L1 ";
        $query .= " WHERE ";
        $query .= "     L1.STAMP_NO = '{$code}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //新規印鑑番号取得
    function getMaxStampNo()
    {
        $db = Query::dbCheckOut();

        $seireki2keta = substr(CTRL_DATE,2,2); // TODO：仕様未確定。とりあえずログイン日付を使用。

        $query  = " SELECT ";
        $query .= "     COUNT(*) as CNT, ";
        $query .= "     int(max(STAMP_NO)) + 1 as MAX_STAMP_NO ";
        $query .= " FROM ";
        $query .= "     ATTEST_INKAN_DAT ";
        $query .= " where ";
        $query .= "     STAMP_NO like '{$seireki2keta}%' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if ($row["CNT"]) {
            $rtnVal = $row["MAX_STAMP_NO"];
        } else {
            $rtnVal = $seireki2keta ."0001";
        }

        return sprintf("%06d", $rtnVal);
    }

    function getIneiFlg($code) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEST_INKAN_DAT ";
        $query .= " WHERE ";
        $query .= "     STAMP_NO = '{$code}' ";

        return $query;
    }

    function &getInsertQueryInkan($model)
    {
        $db = Query::dbCheckOut();

        $data["STAMP_NO"][TEXT]     = $model->maxStampNo;
        $data["STAFFCD"][TEXT]      = $model->staffcd;
        $data["START_DATE"][DATE]   = $model->field["START_DATE"];
        $data["START_REASON"][TEXT] = $model->field["START_REASON"];

        $data["DIST"][TEXT]         = ($model->field["DIST"]) ? 1 : 0;
        $data["STOP_DATE"][DATE]    = $model->field["STOP_DATE"];
        $data["STOP_REASON"][TEXT]  = $model->field["STOP_REASON"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ATTEST_INKAN_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    function &getUpdateQueryInkan($model)
    {
        $db = Query::dbCheckOut();

        $data["START_DATE"][DATE]   = $model->field["START_DATE"];
        $data["START_REASON"][TEXT] = $model->field["START_REASON"];
        $data["DIST"][TEXT]         = ($model->field["DIST"]) ? 1 : 0;
        $data["STOP_DATE"][DATE]    = $model->field["STOP_DATE"];
        $data["STOP_REASON"][TEXT]  = $model->field["STOP_REASON"];

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = "WHERE STAMP_NO = '".$model->stampNo."' ";
//      $where .= "  AND STAFFCD = '" .$fields["STAFFCD"] ."'";
        $query = Query::updateSQL($data, "ATTEST_INKAN_DAT", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    function &getDeleteQueryInkan($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ATTEST_INKAN_DAT ";
        $query .= "  WHERE STAMP_NO = '".$model->stampNo."' ";
//      $query .= "    AND STAFFCD = '" .$model->staffcd ."' ";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
}
?>
