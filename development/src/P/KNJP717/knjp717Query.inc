<?php

require_once('for_php7.php');

class knjp717Query extends Query {
    //１レコード取得
    function getRow($model, $seq) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     REGISTBANK_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".$model->schoolcd."' ";
        $query .= "     AND SCHREGNO    = '".$model->schregno."' ";
        $query .= "     AND SEQ         = '".$seq."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    function getStudentName($model) {
        $add1y = $model->search_div == "1" ? 1 : 0;
        $setYear = ($model->select_year) ? $model->select_year : (CTRL_YEAR + $add1y);

        if ($model->search_div == "1") {
            $query = "SELECT SCHREGNO,NAME,NAME_KANA FROM FRESHMAN_DAT WHERE ENTERYEAR = '".$setYear."' AND SCHREGNO ='".$model->schregno."'";
        } else {
            $query = "SELECT SCHREGNO,NAME_SHOW,NAME_KANA FROM SCHREG_BASE_MST WHERE SCHREGNO ='".$model->schregno."'";
        }
        return $query;
    }

    //保護者情報
    function getGuardianData($model) {
        $query .= " SELECT ";
        $query .= "     RELATIONSHIP, ";
        $query .= "     TRANSLATE_H_HK(GUARD_KANA) AS GUARD_KANA ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        return $query;
    }

    //銀行の取得
    function getBankcd() {
        $db = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "     BANKNAME, ";
        $query .= "     BANKCD AS VALUE, ";
        $query .= "     BANKCD || ':' || value(BANKNAME,'') || '(' || BANKNAME_KANA || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     BANK_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        $opt = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = $row;
        }
        $result->free();

        Query::dbCheckIn($db);

        return $opt;
    }

    //銀行の検索
    function getBankSearch($bankSearch) {
        $db = Query::dbCheckOut();

        $query  = " SELECT DISTINCT ";
        $query .= "     BANKCD AS VALUE, ";
        $query .= "     BANKCD || ':' || value(BANKNAME,'') || '(' || BANKNAME_KANA || ')' AS LABEL ";
        $query .= " FROM ";
        $query .= "     BANK_MST ";
        $query .= " WHERE ";
        $query .= "     BANKNAME LIKE '{$bankSearch}%' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //銀行の取得(支店名)
    function getBranchcd($bankcd) {
        $query  = " SELECT DISTINCT ";
        $query .= "     BRANCHCD AS VALUE, ";
        $query .= "     BRANCHCD || ':' || value(BRANCHNAME,'') || '(' || BRANCHNAME_KANA || ')' AS LABEL, ";
        $query .= "     BRANCHNAME_KANA ";
        $query .= " FROM ";
        $query .= "     BANK_MST ";
        $query .= " WHERE ";
        $query .= "     BANKCD = '".$bankcd."' ";
        $query .= " ORDER BY ";
        $query .= "     BRANCHNAME_KANA ";

        return $query;
    }

    //名称取得
    function getNameMst($namecd1) {
        $query  = "";
        if ($namecd1 == "H201") {
            $query .= " WITH T_HONNIN (VALUE, LABEL) AS ( ";
            $query .= "      VALUES('00', '00:本人') ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "      * ";
            $query .= " FROM  ";
            $query .= "      T_HONNIN ";
            $query .= " UNION ALL ";
        }
        $query .= " SELECT ";
        $query .= "      NAMECD2 AS VALUE, ";
        $query .= "      NAMECD2 || ':' || NAME1 AS LABEL";
        $query .= " FROM  ";
        $query .= "      V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM REGISTBANK_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".$model->schoolcd."' ";
        $query .= "     AND SCHREGNO    = '".$model->schregno."' ";
        $db->query($query);

        $seqList = array("1", "2");
        $fieldList = array("BANKCD", "BRANCHCD", "DEPOSIT_ITEM", "ACCOUNTNO", "ACCOUNTNAME", "RELATIONSHIP");

        foreach ($seqList as $seq) {
            if (!strlen($model->field["BANKCD".$seq])) continue;

            $data = array();
            $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["SEQ"][TEXT]          = $seq;
            foreach ($fieldList as $fieldName) {
                $data[$fieldName][TEXT] = $model->field[$fieldName.$seq];
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "REGISTBANK_DAT");
            $db->query($query);

            //口座名義を小文字等を変換する
            $query  = " update REGISTBANK_DAT ";
            $query .= " set ";
            $query .= "     ACCOUNTNAME = TRIM(REPLACE(REPLACE(REPLACE(REPLACE(translate(REPLACE(ACCOUNTNAME, '　', ' '),'ｱｲｳｴｵﾔﾕﾖﾂ','ｧｨｩｪｫｬｭｮｯ'), 'ー', '-'), '－', '-'), '―', '-'), '‐', '-')) ";
            $query .= " where ";
            $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     and SCHREGNO    = '{$model->schregno}' ";
            $query .= "     and SEQ         = '{$seq}' ";
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return ;
    }

    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM REGISTBANK_DAT ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".$model->schoolcd."' ";
        $query .= "     AND SCHREGNO    = '".$model->schregno."' ";
        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }

}
?>
