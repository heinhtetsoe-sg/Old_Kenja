<?php

require_once('for_php7.php');

class knji030Query extends Query
{

    //地区一覧
    public function getAreaData()
    {
        $query  = " SELECT * FROM V_NAME_MST WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'A020' ";
        return $query;
    }

    //選択された生徒情報(一行)
    public function getGradData($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     GRD_BASE.SCHREGNO, ";
        $query .= "     GRD_BASE.NAME, ";
        $query .= "     GRD_BASE.NAME_SHOW, ";
        $query .= "     GRD_BASE.NAME_KANA, ";
        $query .= "     GRD_BASE.NAME_ENG, ";
        $query .= "     GRD_BASE.OLD_NAME, ";
        $query .= "     GRD_BASE.OLD_NAME_SHOW, ";
        $query .= "     GRD_BASE.OLD_NAME_KANA, ";
        $query .= "     GRD_BASE.OLD_NAME_ENG, ";
        $query .= "     GRD_BASE.CUR_ZIPCD, ";
        $query .= "     GRD_BASE.CUR_AREACD, ";
        $query .= "     GRD_BASE.CUR_ADDR1, ";
        $query .= "     GRD_BASE.CUR_ADDR2, ";
        $query .= "     GRD_BASE.CUR_ADDR_FLG, ";
        $query .= "     GRD_DETAIL.REMARK1 AS UNKNOWN_ADDR_FLG, ";
        $query .= "     GRD_BASE.CUR_ADDR1_ENG, ";
        $query .= "     GRD_BASE.CUR_ADDR2_ENG, ";
        $query .= "     GRD_BASE.CUR_TELNO, ";
        $query .= "     GRD_BASE.CUR_FAXNO, ";
        $query .= "     GRD_BASE.CUR_EMAIL, ";
        $query .= "     GRD_BASE.CUR_EMERGENCYCALL, ";
        $query .= "     GRD_BASE.CUR_EMERGENCYTELNO, ";
        $query .= "     GRD_BASE.ZIPCD, ";
        $query .= "     GRD_BASE.AREACD, ";
        $query .= "     GRD_BASE.ADDR1, ";
        $query .= "     GRD_BASE.ADDR2, ";
        $query .= "     GRD_BASE.TELNO, ";
        $query .= "     GRD_BASE.FAXNO, ";
        $query .= "     GRD_DETAIL.REMARK2 AS REMARK ";
        $query .= " FROM ";
        $query .= "     GRD_BASE_MST GRD_BASE ";
        $query .= "     LEFT JOIN GRD_BASE_DETAIL_MST GRD_DETAIL ON GRD_BASE.SCHREGNO = GRD_DETAIL.SCHREGNO ";
        $query .= "          AND GRD_DETAIL.SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     GRD_BASE.SCHREGNO = '{$schregno}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    /**
     * 更新
     */
    public function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHREGNO"][TEXT]               = $fields["SCHREGNO"];
        $data["NAME"][TEXT]                   = $fields["NAME"];
        $data["NAME_SHOW"][TEXT]              = $fields["NAME_SHOW"];
        $data["NAME_KANA"][TEXT]              = $fields["NAME_KANA"];
        $data["NAME_ENG"][TEXT]               = $fields["NAME_ENG"];
        $data["CUR_ZIPCD"][TEXT]              = $fields["CUR_ZIPCD"];
        $data["CUR_AREACD"][TEXT]             = ($fields["CUR_AREACD"]=="00") ? null : $fields["CUR_AREACD"];
        $data["CUR_ADDR1"][TEXT]              = $fields["CUR_ADDR1"];
        $data["CUR_ADDR2"][TEXT]              = $fields["CUR_ADDR2"];
        $data["CUR_ADDR_FLG"][TEXT]           = $fields["CUR_ADDR_FLG"];
        $data["CUR_ADDR1_ENG"][TEXT]          = $fields["CUR_ADDR1_ENG"];
        $data["CUR_ADDR2_ENG"][TEXT]          = $fields["CUR_ADDR2_ENG"];
        $data["CUR_TELNO"][TEXT]              = $fields["CUR_TELNO"];
        $data["CUR_FAXNO"][TEXT]              = $fields["CUR_FAXNO"];
        $data["CUR_EMAIL"][TEXT]              = $fields["CUR_EMAIL"];
        $data["CUR_EMERGENCYCALL"][TEXT]      = $fields["CUR_EMERGENCYCALL"];
        $data["CUR_EMERGENCYTELNO"][TEXT]     = $fields["CUR_EMERGENCYTELNO"];
        $data["ZIPCD"][TEXT]                  = $fields["ZIPCD"];
        $data["AREACD"][TEXT]                 = ($fields["AREACD"]=="00") ? null : $fields["AREACD"];
        $data["ADDR1"][TEXT]                  = $fields["ADDR1"];
        $data["ADDR2"][TEXT]                  = $fields["ADDR2"];
        $data["TELNO"][TEXT]                  = $fields["TELNO"];
        $data["FAXNO"][TEXT]                  = $fields["FAXNO"];
        $data["REGISTERCD"][TEXT]             = STAFFCD;
        $data["UPDATED"][FUNC]                = "sysdate()";
        $where  = " WHERE SCHREGNO = '".$fields["SCHREGNO"]."'";

        $query = Query::updateSQL($data, "GRD_BASE_MST", $where);
        $db->query($query);

        $query  = " DELETE FROM GRD_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$fields["SCHREGNO"]}' ";
        $query .= "     AND SEQ = '001' ";
        $db->query($query);

        $data = array();
        $data["SCHREGNO"][TEXT]     = $fields["SCHREGNO"];
        $data["SEQ"][TEXT]          = "001";
        $data["REMARK1"][TEXT]      = $fields["UNKNOWN_ADDR_FLG"];
        $data["REMARK2"][TEXT]      = $fields["REMARK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "GRD_BASE_DETAIL_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
