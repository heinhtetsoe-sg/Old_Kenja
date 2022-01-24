<?php

require_once('for_php7.php');

class knjx_guardian_addressQuery extends Query
{

    //起動チェック
    public function checkToStart($db)
    {
        foreach (array('Z002','H201','A020') as $val) { //性別、続柄、地区がマスタに存在するかチェック！
            $query = "SELECT COUNT(*) AS a FROM v_name_mst WHERE namecd1 = '".$val."' AND year = '".CTRL_YEAR."'";
            if ($db->getOne($query) == "0") {
                return false;
            }
        }
        return true;
    }

    //学籍基礎マスタより学籍番号と名前を取得
    public function getSchregnoName($model)
    {
        $query  ="SELECT schregno,name_show as name FROM schreg_base_mst";
        $query .=" WHERE schregno = '".$model->schregno."'";
        return $query;
    }

    //学籍住所データよりデータを取得
    public function getAddressAll($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     ISSUEDATE  AS GUARD_ISSUEDATE, ";
        $query .= "     EXPIREDATE AS GUARD_EXPIREDATE, ";
        $query .= "     GUARD_ZIPCD, ";
        $query .= "     GUARD_ADDR1, ";
        $query .= "     GUARD_ADDR2, ";
        $query .= "     GUARD_ADDR_FLG, ";
        $query .= "     GUARD_TELNO, ";
        $query .= "     GUARD_TELNO2, ";
        $query .= "     GUARD_FAXNO, ";
        $query .= "     GUARD_E_MAIL ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE ";

        return $query;
    }

    //最新の有効開始日付を取得
    public function getMaxIssueDate($model)
    {
        $query  = " SELECT ";
        $query .= "     ISSUEDATE ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }


    //listの地区コード表示
    public function listAreaCd($cd)
    {
        return "SELECT name1 FROM v_name_mst WHERE namecd1 = 'A020' AND namecd2 = '".$cd."' AND year = '".CTRL_YEAR."'";
    }

    //期間開始日付のリンクをクリックした学籍住所データを単票形式で表示
    public function getRowAddress($schregno, $issuedate)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        if (strlen($issuedate)) {
            $query .= "     AND ISSUEDATE = '" . str_replace("/", "-", $issuedate) . "' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    public function getCntGAddress($schregno, $guard_issuedate)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' AND ";
        $query .= "     CHAR(ISSUEDATE) = '" . str_replace("/", "-", $guard_issuedate) . "' ";

        return $query;
    }

    public function getGuardianAddressDat($schregno, $guard_issuedate)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' AND ";
        $query .= "     ISSUEDATE = '" . str_replace("/", "-", $guard_issuedate) . "' ";

        return $query;
    }

    //保護者情報の取得
    public function getGuardianAddr($schregno, $guard_issuedate)
    {
        $query  = " SELECT ";
        $query .= "     L1.RELATIONSHIP, ";
        $query .= "     L1.GUARD_NAME, ";
        $query .= "     L1.GUARD_KANA, ";
        $query .= "     L1.GUARD_REAL_NAME, ";
        $query .= "     L1.GUARD_REAL_KANA, ";
        $query .= "     L1.GUARD_SEX, ";
        $query .= "     L1.GUARD_BIRTHDAY, ";
        $query .= "     T1.GUARD_ZIPCD, ";
        $query .= "     T1.GUARD_ADDR1, ";
        $query .= "     T1.GUARD_ADDR2, ";
        $query .= "     T1.GUARD_ADDR_FLG, ";
        $query .= "     T1.GUARD_TELNO, ";
        $query .= "     T1.GUARD_TELNO2, ";
        $query .= "     T1.GUARD_FAXNO, ";
        $query .= "     T1.GUARD_E_MAIL, ";
        $query .= "     L1.GUARD_JOBCD, ";
        $query .= "     L1.GUARD_WORK_NAME, ";
        $query .= "     L1.GUARD_WORK_TELNO, ";
        $query .= "     T1.ISSUEDATE AS GUARD_ISSUEDATE, ";
        $query .= "     T1.EXPIREDATE AS GUARD_EXPIREDATE, ";
        $query .= "     T1.UPDATED AS GUARD_UPDATED ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_ADDRESS_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     GUARDIAN_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO  = '{$schregno}' ";
        if (strlen($guard_issuedate)) {
            $query .= "     AND T1.ISSUEDATE = '" . str_replace("/", "-", $guard_issuedate) . "' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //コンボボックスのデータを名称マスタから取得
    public function getNameMsts()
    {
        $query  = "SELECT namecd1, ";
        $query .= "       namecd2, ";
        $query .= "       name1 ";
        $query .= "  FROM v_name_mst";
        $query .= " WHERE namecd1 IN ('Z002','H201','H202') ";
        $query .= "   AND year = '".CTRL_YEAR."'";
        $query .= "ORDER BY namecd1,namecd2";
        return $query;
    }

    //保護者データをインサート
    public function insertGuardianDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //GUARDIAN_ADDRESS_DAT の追加
        $query  = " SELECT ";
        $query .= "     ISSUEDATE ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        $max_issuedate = $db->getOne($query);
        if ($max_issuedate) {
            //現在ある最後の終了有効期間を入力のあった開始日付の前日とする
            $to_day = str_replace('-', '', $model->field["GUARD_ISSUEDATE"]);
            $last_day = date("Y-m-d", strtotime("-1 day", strtotime($to_day)));

            $data = array();
            $data["EXPIREDATE"][TEXT] = $last_day;

            $where = "WHERE SCHREGNO = '{$model->schregno}' AND ISSUEDATE = '{$max_issuedate}'";

            $query = Query::updateSQL($data, "GUARDIAN_ADDRESS_DAT", $where);
            $db->query($query);
        }

        $data = array();
        $data["SCHREGNO"][TEXT]       = $model->schregno;
        $data["ISSUEDATE"][FUNC]      = "date('".$model->field["GUARD_ISSUEDATE"]."')";
        $data["EXPIREDATE"][FUNC]     = "date('".$model->field["GUARD_EXPIREDATE"]."')";
        $data["GUARD_ZIPCD"][TEXT]    = $model->field["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]    = $model->field["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]    = $model->field["GUARD_ADDR2"];
        $data["GUARD_ADDR_FLG"][TEXT] = $model->field["GUARD_ADDR_FLG"];
        $data["GUARD_TELNO"][TEXT]    = $model->field["GUARD_TELNO"];
        $data["GUARD_TELNO2"][TEXT]   = $model->field["GUARD_TELNO2"];
        $data["GUARD_FAXNO"][TEXT]    = $model->field["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]   = $model->field["GUARD_E_MAIL"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "SYSDATE()";

        $query = Query::insertSQL($data, "GUARDIAN_ADDRESS_DAT");
        $db->query($query);

        // 保護者情報
        $query = knjx_guardian_addressQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARD_ZIPCD"][TEXT]      = $setAddr["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $setAddr["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $setAddr["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]      = $setAddr["GUARD_TELNO"];
        $data["GUARD_TELNO2"][TEXT]     = $setAddr["GUARD_TELNO2"];
        $data["GUARD_FAXNO"][TEXT]      = $setAddr["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]     = $setAddr["GUARD_E_MAIL"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  ="WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //保護者データをアップデート
    public function updateGuardianDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["ISSUEDATE"][FUNC]        = "date('".$model->field["GUARD_ISSUEDATE"]."')";
        $data["EXPIREDATE"][FUNC]       = "date('".$model->field["GUARD_EXPIREDATE"]."')";
        $data["GUARD_ZIPCD"][TEXT]      = $model->field["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $model->field["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $model->field["GUARD_ADDR2"];
        $data["GUARD_ADDR_FLG"][TEXT]   = $model->field["GUARD_ADDR_FLG"];
        $data["GUARD_TELNO"][TEXT]      = $model->field["GUARD_TELNO"];
        $data["GUARD_TELNO2"][TEXT]     = $model->field["GUARD_TELNO2"];
        $data["GUARD_FAXNO"][TEXT]      = $model->field["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]     = $model->field["GUARD_E_MAIL"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where = "WHERE SCHREGNO = '{$model->schregno}' AND ISSUEDATE = date('".str_replace("/", "-", $model->guard_issuedate)."')";

        $query = Query::updateSQL($data, "GUARDIAN_ADDRESS_DAT", $where);
        $db->query($query);

        // 保護者情報
        $query = knjx_guardian_addressQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARD_ZIPCD"][TEXT]      = $setAddr["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $setAddr["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $setAddr["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]      = $setAddr["GUARD_TELNO"];
        $data["GUARD_TELNO2"][TEXT]     = $setAddr["GUARD_TELNO2"];
        $data["GUARD_FAXNO"][TEXT]      = $setAddr["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]     = $setAddr["GUARD_E_MAIL"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  ="WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //住所履歴の最新取得
    public function getMaxAddress($model)
    {
        $query  = " WITH MAX_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     MAX_T, ";
        $query .= "     GUARDIAN_ADDRESS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     MAX_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND MAX_T.ISSUEDATE = T1.ISSUEDATE ";

        return $query;
    }

    public function deleteGurdianAddressDat($model)
    {
        $db = Query::dbCheckOut();
        $query  ="DELETE FROM GUARDIAN_ADDRESS_DAT WHERE schregno = '{$model->schregno}' AND issuedate = date('".str_replace("/", "-", $model->guard_issuedate)."')";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //生徒の緊急連絡先情報を取得するSQLを発行
    public function getEmergencyInfo($schregno)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     EMERGENCYCALL,  EMERGENCYNAME,  EMERGENCYRELA_NAME,  EMERGENCYTELNO,    ";
        $query .= "     EMERGENCYCALL2, EMERGENCYNAME2, EMERGENCYRELA_NAME2, EMERGENCYTELNO2    ";
        $query .= " FROM SCHREG_BASE_MST ";
        $query .= " WHERE SCHREGNO  = '".$schregno."' ";

        return $query;
    }

    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 || ' ' || T1.NAME1 AS LABEL, ";
        $query .= "     T1.NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
