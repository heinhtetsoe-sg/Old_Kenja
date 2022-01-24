<?php

require_once('for_php7.php');

class knjx_guarantor_addressQuery extends Query
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
    public function getSchregNoName($model)
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
        $query .= "     ISSUEDATE  AS GUARANTOR_ISSUEDATE, ";
        $query .= "     EXPIREDATE AS GUARANTOR_EXPIREDATE, ";
        $query .= "     GUARANTOR_ZIPCD, ";
        $query .= "     GUARANTOR_ADDR1, ";
        $query .= "     GUARANTOR_ADDR2, ";
        $query .= "     GUARANTOR_ADDR_FLG, ";
        $query .= "     GUARANTOR_TELNO ";
        $query .= " FROM ";
        $query .= "     GUARANTOR_ADDRESS_DAT ";
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
        $query .= "     GUARANTOR_ADDRESS_DAT ";
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

    public function getCntGAddress($schregno, $guarantor_issuedate)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GUARANTOR_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' AND ";
        $query .= "     CHAR(ISSUEDATE) = '" . str_replace("/", "-", $guarantor_issuedate) . "' ";

        return $query;
    }

    public function getGuarantorAddressDat($schregno, $guarantor_issuedate)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     GUARANTOR_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' AND ";
        $query .= "     ISSUEDATE = '" . str_replace("/", "-", $guarantor_issuedate) . "' ";

        return $query;
    }

    //保護者情報の取得
    public function getGuarantorAddr($schregno, $guarantor_issuedate)
    {
        $query  = " SELECT ";
        $query .= "     L1.RELATIONSHIP, ";
        $query .= "     L1.GUARANTOR_NAME, ";
        $query .= "     L1.GUARANTOR_KANA, ";
        $query .= "     L1.GUARANTOR_REAL_NAME, ";
        $query .= "     L1.GUARANTOR_REAL_KANA, ";
        $query .= "     L1.GUARANTOR_SEX, ";
        $query .= "     T1.GUARANTOR_ZIPCD, ";
        $query .= "     T1.GUARANTOR_ADDR1, ";
        $query .= "     T1.GUARANTOR_ADDR2, ";
        $query .= "     T1.GUARANTOR_ADDR_FLG, ";
        $query .= "     T1.GUARANTOR_TELNO, ";
        $query .= "     L1.GUARANTOR_JOBCD, ";
        $query .= "     L1.PUBLIC_OFFICE, ";
        $query .= "     T1.ISSUEDATE AS GUARANTOR_ISSUEDATE, ";
        $query .= "     T1.EXPIREDATE AS GUARANTOR_EXPIREDATE, ";
        $query .= "     T1.UPDATED AS GUARANTOR_UPDATED ";
        $query .= " FROM ";
        $query .= "     GUARANTOR_ADDRESS_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     GUARDIAN_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO  = '{$schregno}' ";
        if (strlen($guarantor_issuedate)) {
            $query .= "     AND T1.ISSUEDATE = '" . str_replace("/", "-", $guarantor_issuedate) . "' ";
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
    public function insertGuarantorDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //GUARANTOR_ADDRESS_DAT の追加
        $query  = " SELECT ";
        $query .= "     ISSUEDATE ";
        $query .= " FROM ";
        $query .= "     GUARANTOR_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        $max_issuedate = $db->getOne($query);
        if ($max_issuedate) {
            //現在ある最後の終了有効期間を入力のあった開始日付の前日とする
            $to_day = str_replace('-', '', $model->field["GUARANTOR_ISSUEDATE"]);
            $last_day = date("Y-m-d", strtotime("-1 day", strtotime($to_day)));

            $data = array();
            $data["EXPIREDATE"][TEXT] = $last_day;

            $where = "WHERE SCHREGNO = '{$model->schregno}' AND ISSUEDATE = '{$max_issuedate}'";

            $query = Query::updateSQL($data, "GUARANTOR_ADDRESS_DAT", $where);
            $db->query($query);
        }

        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["ISSUEDATE"][FUNC]            = "date('".$model->field["GUARANTOR_ISSUEDATE"]."')";
        $data["EXPIREDATE"][FUNC]           = "date('".$model->field["GUARANTOR_EXPIREDATE"]."')";
        $data["GUARANTOR_ZIPCD"][TEXT]      = $model->field["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]      = $model->field["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]      = $model->field["GUARANTOR_ADDR2"];
        $data["GUARANTOR_ADDR_FLG"][TEXT]   = $model->field["GUARANTOR_ADDR_FLG"];
        $data["GUARANTOR_TELNO"][TEXT]      = $model->field["GUARANTOR_TELNO"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data, "GUARANTOR_ADDRESS_DAT");
        $db->query($query);

        // 保護者情報
        $query = knjx_guarantor_addressQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARANTOR_ZIPCD"][TEXT]  = $setAddr["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]  = $setAddr["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]  = $setAddr["GUARANTOR_ADDR2"];
        $data["GUARANTOR_TELNO"][TEXT]  = $setAddr["GUARANTOR_TELNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  ="WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //保護者データをアップデート
    public function updateGuarantorDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["ISSUEDATE"][FUNC]            = "date('".$model->field["GUARANTOR_ISSUEDATE"]."')";
        $data["EXPIREDATE"][FUNC]           = "date('".$model->field["GUARANTOR_EXPIREDATE"]."')";
        $data["GUARANTOR_ZIPCD"][TEXT]      = $model->field["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]      = $model->field["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]      = $model->field["GUARANTOR_ADDR2"];
        $data["GUARANTOR_ADDR_FLG"][TEXT]   = $model->field["GUARANTOR_ADDR_FLG"];
        $data["GUARANTOR_TELNO"][TEXT]      = $model->field["GUARANTOR_TELNO"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where = "WHERE SCHREGNO = '{$model->schregno}' AND ISSUEDATE = date('".str_replace("/", "-", $model->guarantor_issuedate)."')";

        $query = Query::updateSQL($data, "GUARANTOR_ADDRESS_DAT", $where);
        $db->query($query);

        // 保護者情報
        $query = knjx_guarantor_addressQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARANTOR_ZIPCD"][TEXT]  = $setAddr["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]  = $setAddr["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]  = $setAddr["GUARANTOR_ADDR2"];
        $data["GUARANTOR_TELNO"][TEXT]  = $setAddr["GUARANTOR_TELNO"];
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
        $query .= "     GUARANTOR_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     MAX_T, ";
        $query .= "     GUARANTOR_ADDRESS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     MAX_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND MAX_T.ISSUEDATE = T1.ISSUEDATE ";

        return $query;
    }

    public function deleteGuarantorAddressDat($model)
    {
        $db = Query::dbCheckOut();
        $query  ="DELETE FROM GUARANTOR_ADDRESS_DAT WHERE schregno = '{$model->schregno}' AND issuedate = date('".str_replace("/", "-", $model->guarantor_issuedate)."')";
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
}
