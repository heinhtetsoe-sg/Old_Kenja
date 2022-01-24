<?php

require_once('for_php7.php');

class knjh410_knja110_2aQuery extends Query {

    //起動チェック
    function ChecktoStart($db)
    {
        foreach (array('Z002','H201','A020') as $val) //性別、続柄、地区がマスタに存在するかチェック！
        {
            $query = "SELECT COUNT(*) AS a FROM v_name_mst WHERE namecd1 = '".$val."' AND year = '".CTRL_YEAR."'";
            if ($db->getOne($query) == "0") {
                return false;
            }
        }
        return true; 
    }

    //学籍基礎マスタより学籍番号と名前を取得
    function getSchregno_name($model){
        $query  ="SELECT schregno,name_show as name FROM schreg_base_mst";
        $query .=" WHERE schregno = '".$model->schregno."'";
        return $query;
    }

    //学籍住所データよりデータを取得
    function getAddress_all($model)
    {
        if ($model->infoDiv == "2" || $model->infoDiv == "3") {
            $query  = " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     ISSUEDATE  AS GUARD_ISSUEDATE, ";
            $query .= "     EXPIREDATE AS GUARD_EXPIREDATE, ";
            $query .= "     GUARD_ZIPCD, ";
            $query .= "     GUARD_ADDR1, ";
            $query .= "     GUARD_ADDR2, ";
            $query .= "     GUARD_ADDR_FLG, ";
            $query .= "     GUARD_TELNO, ";
            $query .= "     GUARD_FAXNO, ";
            $query .= "     GUARD_E_MAIL ";
            $query .= " FROM ";
            $query .= "     GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$model->schregno."' ";
            $query .= " ORDER BY ";
            $query .= "     ISSUEDATE ";
        } else if ($model->infoDiv == "4") {
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
        } else if ($model->infoDiv == "5") {
            $query  = " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     DIV AS SEND_DIV, ";
            $query .= "     SEND_RELATIONSHIP, ";
            $query .= "     SEND_NAME, ";
            $query .= "     SEND_KANA, ";
            $query .= "     SEND_SEX, ";
            $query .= "     SEND_ZIPCD, ";
            $query .= "     SEND_AREACD, ";
            $query .= "     SEND_ADDR1, ";
            $query .= "     SEND_ADDR2, ";
            $query .= "     SEND_ADDR_FLG, ";
            $query .= "     SEND_TELNO, ";
            $query .= "     SEND_TELNO2, ";
            $query .= "     SEND_JOBCD, ";
            $query .= "     PUBLIC_OFFICE AS SEND_PUBLIC_OFFICE ";
            $query .= " FROM ";
            $query .= "     SCHREG_SEND_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$model->schregno."' ";
            $query .= " ORDER BY ";
            $query .= "     DIV ";
        } else if ($model->infoDiv == "6") {
            $query  = " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     RELANO          AS RELA_NO, ";
            $query .= "     RELANAME        AS RELA_NAME, ";
            $query .= "     RELAKANA        AS RELA_KANA, ";
            $query .= "     RELASEX         AS RELA_SEX, ";
            $query .= "     RELABIRTHDAY    AS RELA_BIRTHDAY, ";
            $query .= "     RELATIONSHIP    AS RELA_RELATIONSHIP, ";
            $query .= "     OCCUPATION      AS RELA_OCCUPATION, ";
            $query .= "     RELA_SCHREGNO, ";
            $query .= "     REGIDENTIALCD   AS RELA_REGIDENTIALCD, ";
            $query .= "     REMARK          AS RELA_REMARK ";
            $query .= " FROM ";
            $query .= "     SCHREG_RELA_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$model->schregno."' ";
            $query .= " ORDER BY ";
            $query .= "     RELANO ";
        } else {
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     SCHREG_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$model->schregno."' ";
            $query .= " ORDER BY ";
            $query .= "     ISSUEDATE ";
        }

        return $query;
    }

    //最新の有効開始日付を取得
    function get_max_issuedate($model) {
        if ($model->infoDiv == "2" || $model->infoDiv == "3") {
            $table = " GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
        } else if ($model->infoDiv == "4") {
            $table = " GUARANTOR_ADDRESS_DAT ";
        } else {
            $table = " SCHREG_ADDRESS_DAT ";
        }

        $query  = " SELECT ";
        $query .= "     ISSUEDATE ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }


    //listの地区コード表示
    function List_AreaCd()
    {
        return "SELECT * FROM v_name_mst WHERE namecd1 = 'A020' AND year = '".CTRL_YEAR."'";
    }

    //期間開始日付のリンクをクリックした学籍住所データを単票形式で表示
    function getRow_Address($schregno,$issuedate) {
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

    function get_cnt_Address($schregno,$issuedate) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' AND ";
        $query .= "     CHAR(ISSUEDATE) = '" . str_replace("/", "-", $issuedate) . "' ";

        return $query;
    }

    function get_cnt_G_Address($model, $schregno,$guard_issuedate) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' AND ";
        $query .= "     CHAR(ISSUEDATE) = '" . str_replace("/", "-", $guard_issuedate) . "' ";

        return $query;
    }

    //保証人住所データ取得
    function getGuarantorAddressDat($schregno, $issuedate, $flg="") {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     GUARANTOR_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     ISSUEDATE   = '".str_replace("/", "-", $issuedate)."' ";

        return $query;
    }

    //その他住所データ取得
    function getSchregSendAddressDat($schregno, $send_div, $flg="") {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_SEND_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     DIV         = '".$send_div."' ";

        return $query;
    }

    function getGuardianAddressDat($model, $schregno, $guard_issuedate) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' AND ";
        $query .= "     ISSUEDATE = '" . str_replace("/", "-", $guard_issuedate) . "' ";

        return $query;
    }

    //保護者情報の取得
    function getGuardianAddr($model, $schregno, $guard_issuedate, $flg = "") {
        $guardian2 = "";
        if ($flg == "copy") {
            //コピーボタン:Hiddenで保持用
            $guardian2 = ($model->infoDiv == "2") ? "2" : "";
        } else {
            //データ表示:通常
            $guardian2 = $model->guardian2;
        }

        $query  = " SELECT ";
        $query .= "     T1.RELATIONSHIP, ";
        $query .= "     T1.GUARD_NAME, ";
        $query .= "     T1.GUARD_KANA, ";
        $query .= "     T1.GUARD_REAL_NAME, ";
        $query .= "     T1.GUARD_REAL_KANA, ";
        $query .= "     T1.GUARD_SEX, ";
        $query .= "     T1.GUARD_BIRTHDAY, ";
        $query .= "     L1.GUARD_ZIPCD, ";
        $query .= "     L1.GUARD_ADDR1, ";
        $query .= "     L1.GUARD_ADDR2, ";
        $query .= "     L1.GUARD_ADDR_FLG, ";
        $query .= "     L1.GUARD_TELNO, ";
        $query .= "     L1.GUARD_FAXNO, ";
        $query .= "     L1.GUARD_E_MAIL, ";
        $query .= "     T1.GUARD_JOBCD, ";
        $query .= "     T1.GUARD_WORK_NAME, ";
        $query .= "     T1.GUARD_WORK_TELNO, ";
        $query .= "     L1.ISSUEDATE AS GUARD_ISSUEDATE, ";
        $query .= "     L1.EXPIREDATE AS GUARD_EXPIREDATE, ";
        $query .= "     L1.UPDATED AS GUARD_UPDATED ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$guardian2}_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     GUARDIAN{$guardian2}_ADDRESS_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO  = '{$schregno}' ";
        if (strlen($guard_issuedate)) {
            $query .= "     AND L1.ISSUEDATE = '" . str_replace("/", "-", $guard_issuedate) . "' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //保証人情報の取得
    function getGuarantorAddr($model, $schregno, $issuedate) {
        $query  = " SELECT ";
        $query .= "     T1.GUARANTOR_RELATIONSHIP, ";
        $query .= "     T1.GUARANTOR_NAME, ";
        $query .= "     T1.GUARANTOR_KANA, ";
        $query .= "     T1.GUARANTOR_REAL_NAME, ";
        $query .= "     T1.GUARANTOR_REAL_KANA, ";
        $query .= "     T1.GUARANTOR_SEX, ";
        $query .= "     L1.GUARANTOR_ZIPCD, ";
        $query .= "     L1.GUARANTOR_ADDR1, ";
        $query .= "     L1.GUARANTOR_ADDR2, ";
        $query .= "     L1.GUARANTOR_ADDR_FLG, ";
        $query .= "     L1.GUARANTOR_TELNO, ";
        $query .= "     T1.GUARANTOR_JOBCD, ";
        $query .= "     T1.PUBLIC_OFFICE, ";
        $query .= "     L1.ISSUEDATE AS GUARANTOR_ISSUEDATE, ";
        $query .= "     L1.EXPIREDATE AS GUARANTOR_EXPIREDATE, ";
        $query .= "     L1.UPDATED AS GUARANTOR_UPDATED ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     GUARANTOR_ADDRESS_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO  = '{$schregno}' ";
        if (strlen($issuedate)) {
            $query .= "     AND L1.ISSUEDATE = '".str_replace("/", "-", $issuedate)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //その他情報の取得
    function getSendAddr($model, $schregno, $send_div) {
        $query  = " SELECT ";
        $query .= "     DIV AS SEND_DIV, ";
        $query .= "     SEND_RELATIONSHIP, ";
        $query .= "     SEND_NAME, ";
        $query .= "     SEND_KANA, ";
        $query .= "     SEND_SEX, ";
        $query .= "     SEND_ZIPCD, ";
        $query .= "     SEND_AREACD, ";
        $query .= "     SEND_ADDR1, ";
        $query .= "     SEND_ADDR2, ";
        $query .= "     SEND_ADDR_FLG, ";
        $query .= "     SEND_TELNO, ";
        $query .= "     SEND_TELNO2, ";
        $query .= "     SEND_JOBCD, ";
        $query .= "     PUBLIC_OFFICE AS SEND_PUBLIC_OFFICE, ";
        $query .= "     UPDATED AS SEND_UPDATED ";
        $query .= " FROM ";
        $query .= "     SCHREG_SEND_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO  = '{$schregno}' ";
        if (strlen($send_div)) {
            $query .= "     AND DIV = '".$send_div."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     DIV ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //家族情報の取得
    function getRelaDat($model, $schregno, $rela_no) {
        $query  = " SELECT ";
        $query .= "     RELANO          AS RELA_NO, ";
        $query .= "     RELANAME        AS RELA_NAME, ";
        $query .= "     RELAKANA        AS RELA_KANA, ";
        $query .= "     RELASEX         AS RELA_SEX, ";
        $query .= "     RELABIRTHDAY    AS RELA_BIRTHDAY, ";
        $query .= "     RELATIONSHIP    AS RELA_RELATIONSHIP, ";
        $query .= "     OCCUPATION      AS RELA_OCCUPATION, ";
        $query .= "     RELA_SCHREGNO, ";
        $query .= "     REGIDENTIALCD   AS RELA_REGIDENTIALCD, ";
        $query .= "     REMARK          AS RELA_REMARK, ";
        $query .= "     UPDATED         AS RELA_UPDATED ";
        $query .= " FROM ";
        $query .= "     SCHREG_RELA_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO  = '{$schregno}' ";
        if (strlen($rela_no)) {
            $query .= "     AND RELANO = '".$rela_no."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     RELANO ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //名称マスタから取得
    function get_name_mst() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 IN ('Z002','H201','H202','A020','H200') AND ";
        $query .= "     YEAR = '".CTRL_YEAR."'";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2 ";

        return $query;
    }
    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " SELECT ";
        $query .= "     ISSUEDATE, ";
        $query .= "     EXPIREDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        list($last_issuedate, $last_expiredate) = $db->getRow($query);
        if ($last_issuedate) {
            $issuedate = str_replace('-', '', $model->field["ISSUEDATE"]);
            $last_day = date("Y-m-d",strtotime("-1 day" ,strtotime($issuedate)));

            $data = array();
            $data["EXPIREDATE"][TEXT]     = $last_day;

            $where  = "WHERE SCHREGNO  = '{$model->schregno}'";
            $where .= "  AND ISSUEDATE = '{$last_issuedate}'";

            $query = Query::updateSQL($data,"SCHREG_ADDRESS_DAT",$where);
            $db->query($query);
        }

        $data = array();
        $data["SCHREGNO"][TEXT]       = $model->schregno;
        $data["ISSUEDATE"][TEXT]      = $model->field["ISSUEDATE"];
        $data["EXPIREDATE"][TEXT]     = $model->field["EXPIREDATE"];
        $data["ZIPCD"][TEXT]          = $model->field["ZIPCD"];
        $data["AREACD"][TEXT]         = $model->field["AREACD"];
        $data["ADDR1"][TEXT]          = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]          = $model->field["ADDR2"];
        $data["ADDR_FLG"][TEXT]       = $model->field["ADDR_FLG"];
        $data["ADDR1_ENG"][TEXT]      = $model->field["ADDR1_ENG"];
        $data["ADDR2_ENG"][TEXT]      = $model->field["ADDR2_ENG"];
        $data["TELNO"][TEXT]          = $model->field["TELNO"];
        $data["FAXNO"][TEXT]          = $model->field["FAXNO"];
        $data["EMAIL"][TEXT]          = $model->field["EMAIL"];

        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data,"schreg_address_dat");
        $db->query($query);

        //急用連絡先を更新
        knjh410_knja110_2aQuery::updateEmergencyInfo($db, $model, "update");

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateModel($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        $data["ISSUEDATE"][FUNC]            = "date('".$model->field["ISSUEDATE"]."')";
        $data["EXPIREDATE"][FUNC]           = "date('".$model->field["EXPIREDATE"]."')";
        $data["ZIPCD"][TEXT]                = $model->field["ZIPCD"];
        $data["AREACD"][TEXT]               = $model->field["AREACD"];
        $data["ADDR1"][TEXT]                = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]                = $model->field["ADDR2"];
        $data["ADDR_FLG"][TEXT]             = $model->field["ADDR_FLG"];
        $data["ADDR1_ENG"][TEXT]            = $model->field["ADDR1_ENG"];
        $data["ADDR2_ENG"][TEXT]            = $model->field["ADDR2_ENG"];
        $data["TELNO"][TEXT]                = $model->field["TELNO"];
        $data["FAXNO"][TEXT]                = $model->field["FAXNO"];
        $data["EMAIL"][TEXT]                = $model->field["EMAIL"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = "WHERE SCHREGNO  = '".$model->schregno."'";
        $where .= "  AND ISSUEDATE = date('".str_replace("/", "-", $model->issuedate)."')";

        $query = Query::updateSQL($data,"SCHREG_ADDRESS_DAT",$where);
        $db->query($query);

        //急用連絡先を更新(2005/10/20 追加)
        knjh410_knja110_2aQuery::updateEmergencyInfo($db, $model, "update");


        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //保護者データをインサート
    function insert_Guardian_dat($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //GUARDIAN_HIST_DAT の更新
        knjh410_knja110_2aQuery::make_guardian_hist_dat($model, $db);

        //GUARDIAN_ADDRESS_DAT の追加
        $query  = " SELECT ";
        $query .= "     ISSUEDATE ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        $max_issuedate = $db->getOne($query);
        if ($max_issuedate) {
            //現在ある最後の終了有効期間を入力のあった開始日付の前日とする
            $to_day = str_replace('-', '', $model->field["GUARD_ISSUEDATE"]);
            $last_day = date("Y-m-d",strtotime("-1 day" ,strtotime($to_day)));

            $data = array();
            $data["EXPIREDATE"][TEXT] = $last_day;

            $where = "WHERE SCHREGNO = '{$model->schregno}' AND ISSUEDATE = '{$max_issuedate}'";

            $query = Query::updateSQL($data,"GUARDIAN{$model->guardian2}_ADDRESS_DAT", $where);
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
        $data["GUARD_FAXNO"][TEXT]    = $model->field["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]   = $model->field["GUARD_E_MAIL"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "SYSDATE()";

        $query = Query::insertSQL($data,"GUARDIAN{$model->guardian2}_ADDRESS_DAT");
        $db->query($query);

        // 保護者情報
        $query = knjh410_knja110_2aQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARD_ZIPCD"][TEXT]      = $setAddr["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $setAddr["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $setAddr["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]      = $setAddr["GUARD_TELNO"];
        $data["GUARD_FAXNO"][TEXT]      = $setAddr["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]     = $setAddr["GUARD_E_MAIL"];
        $data["GUARD_JOBCD"][TEXT]      = $model->field["GUARD_JOBCD"];
        $data["GUARD_WORK_NAME"][TEXT]  = $model->field["GUARD_WORK_NAME"];
        $data["GUARD_WORK_TELNO"][TEXT] = $model->field["GUARD_WORK_TELNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  ="WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data,"GUARDIAN{$model->guardian2}_DAT",$where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //保護者データをアップデート
    function update_Guardian_dat($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //GUARDIAN_HIST_DAT の更新
        knjh410_knja110_2aQuery::make_guardian_hist_dat($model, $db);

        $data = array();
        $data["ISSUEDATE"][FUNC]        = "date('".$model->field["GUARD_ISSUEDATE"]."')";
        $data["EXPIREDATE"][FUNC]       = "date('".$model->field["GUARD_EXPIREDATE"]."')";
        $data["GUARD_ZIPCD"][TEXT]      = $model->field["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $model->field["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $model->field["GUARD_ADDR2"];
        $data["GUARD_ADDR_FLG"][TEXT]   = $model->field["GUARD_ADDR_FLG"];
        $data["GUARD_TELNO"][TEXT]      = $model->field["GUARD_TELNO"];
        $data["GUARD_FAXNO"][TEXT]      = $model->field["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]     = $model->field["GUARD_E_MAIL"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where = "WHERE SCHREGNO = '{$model->schregno}' AND ISSUEDATE = date('".str_replace("/", "-", $model->guard_issuedate)."')";

        $query = Query::updateSQL($data,"GUARDIAN{$model->guardian2}_ADDRESS_DAT", $where);
        $db->query($query);

        // 保護者情報
        $query = knjh410_knja110_2aQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARD_ZIPCD"][TEXT]      = $setAddr["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $setAddr["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $setAddr["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]      = $setAddr["GUARD_TELNO"];
        $data["GUARD_FAXNO"][TEXT]      = $setAddr["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]     = $setAddr["GUARD_E_MAIL"];
        $data["GUARD_JOBCD"][TEXT]      = $model->field["GUARD_JOBCD"];
        $data["GUARD_WORK_NAME"][TEXT]  = $model->field["GUARD_WORK_NAME"];
        $data["GUARD_WORK_TELNO"][TEXT] = $model->field["GUARD_WORK_TELNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  ="WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data,"GUARDIAN{$model->guardian2}_DAT",$where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //保証人住所データ追加
    function insertGuarantorData($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //GUARANTOR_HIST_DAT更新
        knjh410_knja110_2aQuery::makeGuarantorHistDat($model, $db);

        //GUARANTOR_ADDRESS_DATのMAX開始日付取得
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
            $last_day = date("Y-m-d",strtotime("-1 day" ,strtotime($to_day)));

            $data = array();
            $data["EXPIREDATE"][TEXT] = $last_day;

            $where = "WHERE SCHREGNO = '{$model->schregno}' AND ISSUEDATE = '{$max_issuedate}'";

            $query = Query::updateSQL($data,"GUARANTOR_ADDRESS_DAT", $where);
            $db->query($query);
        }

        //GUARANTOR_ADDRESS_DAT追加
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

        $query = Query::insertSQL($data,"GUARANTOR_ADDRESS_DAT");
        $db->query($query);

        //GUARDIAN_DAT更新（保証人住所）
        $query = knjh410_knja110_2aQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARANTOR_ZIPCD"][TEXT]  = $setAddr["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]  = $setAddr["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]  = $setAddr["GUARANTOR_ADDR2"];
        $data["GUARANTOR_TELNO"][TEXT]  = $setAddr["GUARANTOR_TELNO"];
        $data["GUARANTOR_JOBCD"][TEXT]  = $model->field["GUARANTOR_JOBCD"];
        $data["PUBLIC_OFFICE"][TEXT]    = $model->field["PUBLIC_OFFICE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where = "WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data,"GUARDIAN_DAT",$where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //保証人住所データ更新
    function updateGuarantorData($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //GUARANTOR_HIST_DAT更新
        knjh410_knja110_2aQuery::makeGuarantorHistDat($model, $db);

        //GUARANTOR_ADDRESS_DAT更新
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
        $query = Query::updateSQL($data,"GUARANTOR_ADDRESS_DAT", $where);
        $db->query($query);

        //GUARDIAN_DAT更新（保証人住所）
        $query = knjh410_knja110_2aQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARANTOR_ZIPCD"][TEXT]  = $setAddr["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]  = $setAddr["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]  = $setAddr["GUARANTOR_ADDR2"];
        $data["GUARANTOR_TELNO"][TEXT]  = $setAddr["GUARANTOR_TELNO"];
        $data["GUARANTOR_JOBCD"][TEXT]  = $model->field["GUARANTOR_JOBCD"];
        $data["PUBLIC_OFFICE"][TEXT]    = $model->field["PUBLIC_OFFICE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where = "WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data,"GUARDIAN_DAT",$where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //その他住所データ追加
    function insertSendAddressData($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["DIV"][TEXT]                  = $model->field["SEND_DIV"];
        $data["SEND_RELATIONSHIP"][TEXT]    = $model->field["SEND_RELATIONSHIP"];
        $data["SEND_NAME"][TEXT]            = $model->field["SEND_NAME"];
        $data["SEND_KANA"][TEXT]            = $model->field["SEND_KANA"];
        $data["SEND_SEX"][TEXT]             = $model->field["SEND_SEX"];
        $data["SEND_ZIPCD"][TEXT]           = $model->field["SEND_ZIPCD"];
        $data["SEND_AREACD"][TEXT]          = $model->field["SEND_AREACD"];
        $data["SEND_ADDR1"][TEXT]           = $model->field["SEND_ADDR1"];
        $data["SEND_ADDR2"][TEXT]           = $model->field["SEND_ADDR2"];
        $data["SEND_ADDR_FLG"][TEXT]        = $model->field["SEND_ADDR_FLG"];
        $data["SEND_TELNO"][TEXT]           = $model->field["SEND_TELNO"];
        $data["SEND_TELNO2"][TEXT]          = $model->field["SEND_TELNO2"];
        $data["SEND_JOBCD"][TEXT]           = $model->field["SEND_JOBCD"];
        $data["PUBLIC_OFFICE"][TEXT]        = $model->field["SEND_PUBLIC_OFFICE"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data,"SCHREG_SEND_ADDRESS_DAT");
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //その他住所データ更新
    function updateSendAddressData($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["SEND_RELATIONSHIP"][TEXT]    = $model->field["SEND_RELATIONSHIP"];
        $data["SEND_NAME"][TEXT]            = $model->field["SEND_NAME"];
        $data["SEND_KANA"][TEXT]            = $model->field["SEND_KANA"];
        $data["SEND_SEX"][TEXT]             = $model->field["SEND_SEX"];
        $data["SEND_ZIPCD"][TEXT]           = $model->field["SEND_ZIPCD"];
        $data["SEND_AREACD"][TEXT]          = $model->field["SEND_AREACD"];
        $data["SEND_ADDR1"][TEXT]           = $model->field["SEND_ADDR1"];
        $data["SEND_ADDR2"][TEXT]           = $model->field["SEND_ADDR2"];
        $data["SEND_ADDR_FLG"][TEXT]        = $model->field["SEND_ADDR_FLG"];
        $data["SEND_TELNO"][TEXT]           = $model->field["SEND_TELNO"];
        $data["SEND_TELNO2"][TEXT]          = $model->field["SEND_TELNO2"];
        $data["SEND_JOBCD"][TEXT]           = $model->field["SEND_JOBCD"];
        $data["PUBLIC_OFFICE"][TEXT]        = $model->field["SEND_PUBLIC_OFFICE"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where = "WHERE SCHREGNO = '{$model->schregno}' AND DIV = '".$model->send_div."' ";
        $query = Query::updateSQL($data,"SCHREG_SEND_ADDRESS_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //家族情報データ取得
    function getSchregRelaDat($schregno, $rela_no, $flg="") {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_RELA_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        $query .= "     RELANO      = '".$rela_no."' ";

        return $query;
    }

    //家族情報データ・連番の最大値を取得
    function getRela_Maxno($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     MAX(INT(RELANO)) AS MAX ";
        $query .= " FROM ";
        $query .= "     SCHREG_RELA_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        $rela_max = $db->getOne($query);
        Query::dbCheckIn($db);
        return $rela_max;
    }

    //家族情報データ追加
    function insertRelaData($model, $rela_no) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["RELANO"][TEXT]               = $rela_no;
        $data["RELANAME"][TEXT]             = $model->field["RELA_NAME"];
        $data["RELAKANA"][TEXT]             = $model->field["RELA_KANA"];
        $data["RELASEX"][TEXT]              = $model->field["RELA_SEX"];
        $data["RELABIRTHDAY"][TEXT]         = str_replace("/","-",$model->field["RELA_BIRTHDAY"]);
        $data["RELATIONSHIP"][TEXT]         = $model->field["RELA_RELATIONSHIP"];
        $data["OCCUPATION"][TEXT]           = $model->field["RELA_OCCUPATION"];
        $data["RELA_SCHREGNO"][TEXT]        = $model->field["RELA_SCHREGNO"];
        $data["REGIDENTIALCD"][TEXT]        = $model->field["RELA_REGIDENTIALCD"];
        $data["REMARK"][TEXT]               = $model->field["RELA_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $query = Query::insertSQL($data,"SCHREG_RELA_DAT");
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //家族情報データ更新
    function updateRelaData($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = array();
        $data["RELANAME"][TEXT]             = $model->field["RELA_NAME"];
        $data["RELAKANA"][TEXT]             = $model->field["RELA_KANA"];
        $data["RELASEX"][TEXT]              = $model->field["RELA_SEX"];
        $data["RELABIRTHDAY"][TEXT]         = str_replace("/","-",$model->field["RELA_BIRTHDAY"]);
        $data["RELATIONSHIP"][TEXT]         = $model->field["RELA_RELATIONSHIP"];
        $data["OCCUPATION"][TEXT]           = $model->field["RELA_OCCUPATION"];
        $data["RELA_SCHREGNO"][TEXT]        = $model->field["RELA_SCHREGNO"];
        $data["REGIDENTIALCD"][TEXT]        = $model->field["RELA_REGIDENTIALCD"];
        $data["REMARK"][TEXT]               = $model->field["RELA_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "SYSDATE()";

        $where = "WHERE SCHREGNO = '{$model->schregno}' AND RELANO = '".$model->rela_no."' ";
        $query = Query::updateSQL($data,"SCHREG_RELA_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //家族情報データ削除
    function deleteRelaData($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM SCHREG_RELA_DAT WHERE SCHREGNO = '{$model->schregno}' AND RELANO = '".$model->rela_no."' ";
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //GUARANTOR_HIST_DAT更新
    function makeGuarantorHistDat($model, $db) {
        $make_hist_flg = false;

        if ($model->field["GUARANTOR_RELATIONSHIP_FLG"] == '1') { $make_hist_flg = true; }
        if ($model->field["GUARANTOR_NAME_FLG"]         == '1') { $make_hist_flg = true; }
        if ($model->field["GUARANTOR_KANA_FLG"]         == '1') { $make_hist_flg = true; }
        if ($model->field["GUARANTOR_REAL_NAME_FLG"]    == '1') { $make_hist_flg = true; }
        if ($model->field["GUARANTOR_REAL_KANA_FLG"]    == '1') { $make_hist_flg = true; }
        if ($model->field["GUARANTOR_SEX_FLG"]          == '1') { $make_hist_flg = true; }

        if ($make_hist_flg) {
            $query = knjh410_knja110_2aQuery::get_last_expiredate($model, $model->schregno);
            $last_expiredate = $db->getOne($query);

            $query = knjh410_knja110_2aQuery::get_ent_date($model->schregno);
            $ent_date = $db->getOne($query);

            if (strlen($last_expiredate)) {
                $start_day = str_replace("-", "/", $last_expiredate);
                $start_day = date("Y-m-d",strtotime("1 day" ,strtotime($start_day)));
            } elseif (strlen($ent_date)) {
                $start_day = str_replace("-", "/", $ent_date);
                $start_day = date("Y-m-d",strtotime($start_day));
            } else {
                $start_day = CTRL_YEAR.'/04/01';
                $start_day = date("Y-m-d",strtotime($start_day));
            }

            $e_appdate = str_replace('/', '-', $model->field["E_APPDATE"]);

            $query  = " INSERT INTO GUARANTOR_HIST_DAT ";
            $query .= " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     '{$start_day}' AS ISSUEDATE, ";
            $query .= "     '{$e_appdate}' AS EXPIREDATE, ";
            $query .= "     GUARANTOR_RELATIONSHIP, ";
            $query .= "     GUARANTOR_NAME, ";
            $query .= "     GUARANTOR_KANA, ";
            $query .= "     GUARANTOR_REAL_NAME, ";
            $query .= "     GUARANTOR_REAL_KANA, ";
            $query .= "     GUARANTOR_SEX, ";
            $query .= "     '{$model->field["GUARANTOR_RELATIONSHIP_FLG"]}' AS GUARANTOR_RELATIONSHIP_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_NAME_FLG"]}'         AS GUARANTOR_NAME_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_KANA_FLG"]}'         AS GUARANTOR_KANA_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_REAL_NAME_FLG"]}'    AS GUARANTOR_REAL_NAME_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_REAL_KANA_FLG"]}'    AS GUARANTOR_REAL_KANA_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_SEX_FLG"]}'          AS GUARANTOR_SEX_FLG, ";
            $query .= "     REGISTERCD, ";
            $query .= "     UPDATED ";
            $query .= " FROM ";
            $query .= "     GUARDIAN_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '{$model->schregno}' ";

            $db->query($query);

            $query = "VALUES(DATE(sysdate()))";
            $sysDate = $db->getOne($query);

            if ($sysDate < $e_appdate) {
                $saisyuArray = explode('-', $e_appdate);
                $saisyuYear  = $saisyuArray[0];
                $saisyuMonth = $saisyuArray[1];
                $saisyuDay   = $saisyuArray[2];

                $time = mktime(0, 0, 0, $saisyuMonth, $saisyuDay + 1, $saisyuYear); //一日後を取得
                $sdate = date("Y-m-d", $time); //それを今回の(履歴の)開始日とする
                $edate = "9999-12-31";

                $data = array();
                $data["SCHREGNO"][TEXT]                     = $model->schregno;
                $data["ISSUEDATE"][DATE]                    = $sdate;
                $data["EXPIREDATE"][DATE]                   = $edate;
                $data["GUARANTOR_RELATIONSHIP"][TEXT]       = $model->field["GUARANTOR_RELATIONSHIP"];
                $data["GUARANTOR_NAME"][TEXT]               = $model->field["GUARANTOR_NAME"];
                $data["GUARANTOR_KANA"][TEXT]               = $model->field["GUARANTOR_KANA"];
                $data["GUARANTOR_REAL_NAME"][TEXT]          = $model->field["GUARANTOR_REAL_NAME"];
                $data["GUARANTOR_REAL_KANA"][TEXT]          = $model->field["GUARANTOR_REAL_KANA"];
                $data["GUARANTOR_SEX"][TEXT]                = $model->field["GUARANTOR_SEX"];
                $data["GUARANTOR_RELATIONSHIP_FLG"][TEXT]   = $model->field["GUARANTOR_RELATIONSHIP_FLG"];
                $data["GUARANTOR_NAME_FLG"][TEXT]           = $model->field["GUARANTOR_NAME_FLG"];
                $data["GUARANTOR_KANA_FLG"][TEXT]           = $model->field["GUARANTOR_KANA_FLG"];
                $data["GUARANTOR_REAL_NAME_FLG"][TEXT]      = $model->field["GUARANTOR_REAL_NAME_FLG"];
                $data["GUARANTOR_REAL_KANA_FLG"][TEXT]      = $model->field["GUARANTOR_REAL_KANA_FLG"];
                $data["GUARANTOR_SEX_FLG"][TEXT]            = $model->field["GUARANTOR_SEX_FLG"];
                $data["REGISTERCD"][TEXT]                   = STAFFCD;
                $data["UPDATED"][FUNC]                      = "SYSDATE()";

                $yoyakuQuery = Query::insertSQL($data, "GUARANTOR_HIST_DAT");
                $db->query($yoyakuQuery);
            }
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $guardian_dat_count = $db->getOne($query);

        $data = array();
        if (!$guardian_dat_count) $data["SCHREGNO"][TEXT] = $model->schregno;
        $data["GUARANTOR_RELATIONSHIP"][TEXT]   = $model->field["GUARANTOR_RELATIONSHIP"];
        $data["GUARANTOR_NAME"][TEXT]           = $model->field["GUARANTOR_NAME"];
        $data["GUARANTOR_KANA"][TEXT]           = $model->field["GUARANTOR_KANA"];
        $data["GUARANTOR_REAL_NAME"][TEXT]      = $model->field["GUARANTOR_REAL_NAME"];
        $data["GUARANTOR_REAL_KANA"][TEXT]      = $model->field["GUARANTOR_REAL_KANA"];
        $data["GUARANTOR_SEX"][TEXT]            = $model->field["GUARANTOR_SEX"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "SYSDATE()";

        if ($guardian_dat_count > 0) {
            $where = "WHERE SCHREGNO = '{$model->schregno}'";
            $query = Query::updateSQL($data,"GUARDIAN_DAT", $where);
        } else {
            $query = Query::insertSQL($data,"GUARDIAN_DAT");
        }
        $db->query($query);
    }

    //住所履歴の最新取得
    function getMaxAddress($model) {
        if ($model->infoDiv == "4") {
            $table = " GUARANTOR_ADDRESS_DAT ";
        } else {
            $table = " GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
        }

        $query  = " WITH MAX_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     MAX_T, ";
        $query .=       $table." T1 ";
        $query .= " WHERE ";
        $query .= "     MAX_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND MAX_T.ISSUEDATE = T1.ISSUEDATE ";

        return $query;
    }

    //GUARDIAN_HIST_DAT の更新
    function make_guardian_hist_dat($model, $db) {
        $make_hist_flg = false;

        if ($model->field["RELATIONSHIP_FLG"]    == '1') { $make_hist_flg = true; }
        if ($model->field["GUARD_NAME_FLG"]      == '1') { $make_hist_flg = true; }
        if ($model->field["GUARD_KANA_FLG"]      == '1') { $make_hist_flg = true; }
        if ($model->field["GUARD_REAL_NAME_FLG"] == '1') { $make_hist_flg = true; }
        if ($model->field["GUARD_REAL_KANA_FLG"] == '1') { $make_hist_flg = true; }
        if ($model->field["GUARD_SEX_FLG"]       == '1') { $make_hist_flg = true; }
        if ($model->field["GUARD_BIRTHDAY_FLG"]  == '1') { $make_hist_flg = true; }

        if ($make_hist_flg) {
            $query = knjh410_knja110_2aQuery::get_last_expiredate($model, $model->schregno);
            $last_expiredate = $db->getOne($query);

            $query = knjh410_knja110_2aQuery::get_ent_date($model->schregno);
            $ent_date = $db->getOne($query);

            if (strlen($last_expiredate)) {
                $start_day = str_replace("-", "/", $last_expiredate);
                $start_day = date("Y-m-d",strtotime("1 day" ,strtotime($start_day)));
            } elseif (strlen($ent_date)) {
                $start_day = str_replace("-", "/", $ent_date);
                $start_day = date("Y-m-d",strtotime($start_day));
            } else {
                $start_day = CTRL_YEAR.'/04/01';
                $start_day = date("Y-m-d",strtotime($start_day));
            }

            $e_appdate = str_replace('/', '-', $model->field["E_APPDATE"]);

            $query  = " INSERT INTO GUARDIAN{$model->guardian2}_HIST_DAT ";
            $query .= " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     '{$start_day}' AS ISSUEDATE, ";
            $query .= "     '{$e_appdate}' AS EXPIREDATE, ";
            $query .= "     RELATIONSHIP, ";
            $query .= "     GUARD_NAME, ";
            $query .= "     GUARD_KANA, ";
            $query .= "     GUARD_REAL_NAME, ";
            $query .= "     GUARD_REAL_KANA, ";
            $query .= "     GUARD_SEX, ";
            $query .= "     GUARD_BIRTHDAY, ";
            $query .= "     '{$model->field["RELATIONSHIP_FLG"]}'    AS RELATIONSHIP_FLG, ";
            $query .= "     '{$model->field["GUARD_NAME_FLG"]}'      AS GUARD_NAME_FLG, ";
            $query .= "     '{$model->field["GUARD_KANA_FLG"]}'      AS GUARD_KANA_FLG, ";
            $query .= "     '{$model->field["GUARD_REAL_NAME_FLG"]}' AS GUARD_REAL_NAME_FLG, ";
            $query .= "     '{$model->field["GUARD_REAL_KANA_FLG"]}' AS GUARD_REAL_KANA_FLG, ";
            $query .= "     '{$model->field["GUARD_SEX_FLG"]}'       AS GUARD_SEX_FLG, ";
            $query .= "     '{$model->field["GUARD_BIRTHDAY_FLG"]}'  AS GUARD_BIRTHDAY_FLG, ";
            $query .= "     REGISTERCD, ";
            $query .= "     UPDATED ";
            $query .= " FROM ";
            $query .= "     GUARDIAN{$model->guardian2}_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '{$model->schregno}' ";

            $db->query($query);

            $query = "VALUES(DATE(sysdate()))";
            $sysDate = $db->getOne($query);

            if ($sysDate < $e_appdate) {
                $saisyuArray = explode('-', $e_appdate);
                $saisyuYear  = $saisyuArray[0];
                $saisyuMonth = $saisyuArray[1];
                $saisyuDay   = $saisyuArray[2];

                $time = mktime(0, 0, 0, $saisyuMonth, $saisyuDay + 1, $saisyuYear); //一日後を取得
                $sdate = date("Y-m-d", $time); //それを今回の(履歴の)開始日とする
                $edate = "9999-12-31";

                $data = array();
                $data["SCHREGNO"][TEXT]            = $model->schregno;
                $data["RELATIONSHIP"][TEXT]        = $model->field["RELATIONSHIP"];
                $data["GUARD_NAME"][TEXT]          = $model->field["GUARD_NAME"];
                $data["GUARD_KANA"][TEXT]          = $model->field["GUARD_KANA"];
                $data["GUARD_REAL_NAME"][TEXT]     = $model->field["GUARD_REAL_NAME"];
                $data["GUARD_REAL_KANA"][TEXT]     = $model->field["GUARD_REAL_KANA"];
                $data["GUARD_SEX"][TEXT]           = $model->field["GUARD_SEX"];
                $data["GUARD_BIRTHDAY"][TEXT]      = $model->field["GUARD_BIRTHDAY"];
                $data["RELATIONSHIP_FLG"][TEXT]    = $model->field["RELATIONSHIP_FLG"];
                $data["GUARD_NAME_FLG"][TEXT]      = $model->field["GUARD_NAME_FLG"];
                $data["GUARD_KANA_FLG"][TEXT]      = $model->field["GUARD_KANA_FLG"];
                $data["GUARD_REAL_NAME_FLG"][TEXT] = $model->field["GUARD_REAL_NAME_FLG"];
                $data["GUARD_REAL_KANA_FLG"][TEXT] = $model->field["GUARD_REAL_KANA_FLG"];
                $data["GUARD_SEX_FLG"][TEXT]       = $model->field["GUARD_SEX_FLG"];
                $data["GUARD_BIRTHDAY_FLG"][TEXT]  = $model->field["GUARD_BIRTHDAY_FLG"];

                $data["ISSUEDATE"][DATE]  = $sdate;
                $data["EXPIREDATE"][DATE] = $edate;

                $yoyakuQuery = Query::insertSQL($data, "GUARDIAN{$model->guardian2}_HIST_DAT");
                $db->query($yoyakuQuery);
            }
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $guardian_dat_count = $db->getOne($query);
        if ($guardian_dat_count > 0) {
            $data = array();
            $data["RELATIONSHIP"][TEXT]    = $model->field["RELATIONSHIP"];
            $data["GUARD_NAME"][TEXT]      = $model->field["GUARD_NAME"];
            $data["GUARD_KANA"][TEXT]      = $model->field["GUARD_KANA"];
            $data["GUARD_REAL_NAME"][TEXT] = $model->field["GUARD_REAL_NAME"];
            $data["GUARD_REAL_KANA"][TEXT] = $model->field["GUARD_REAL_KANA"];
            $data["GUARD_SEX"][TEXT]       = $model->field["GUARD_SEX"];
            $data["GUARD_BIRTHDAY"][TEXT]  = $model->field["GUARD_BIRTHDAY"];

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $where = "WHERE SCHREGNO = '{$model->schregno}'";

            $query = Query::updateSQL($data,"GUARDIAN{$model->guardian2}_DAT", $where);
        } else {
            $data = array();
            $data["SCHREGNO"][TEXT]        = $model->schregno;
            $data["RELATIONSHIP"][TEXT]    = $model->field["RELATIONSHIP"];
            $data["GUARD_NAME"][TEXT]      = $model->field["GUARD_NAME"];
            $data["GUARD_KANA"][TEXT]      = $model->field["GUARD_KANA"];
            $data["GUARD_REAL_NAME"][TEXT] = $model->field["GUARD_REAL_NAME"];
            $data["GUARD_REAL_KANA"][TEXT] = $model->field["GUARD_REAL_KANA"];
            $data["GUARD_SEX"][TEXT]       = $model->field["GUARD_SEX"];
            $data["GUARD_BIRTHDAY"][TEXT]  = $model->field["GUARD_BIRTHDAY"];

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $query = Query::insertSQL($data,"GUARDIAN{$model->guardian2}_DAT");
        }
        $db->query($query);
    }

    //DELETE -- SCHREG_ADDRESS_DAT
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $query  ="DELETE FROM schreg_address_dat WHERE schregno = '".$model->schregno."' AND issuedate = date('".str_replace("/", "-", $model->issuedate)."')";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE -- GUARDIAN_ADDRESS_DAT
    function &getDelete_GUARDIAN_ADDRESS_DAT($model) {
        $db = Query::dbCheckOut();
        $query  ="DELETE FROM GUARDIAN{$model->guardian2}_ADDRESS_DAT WHERE schregno = '{$model->schregno}' AND issuedate = date('".str_replace("/", "-", $model->guard_issuedate)."')";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE -- GUARANTOR_ADDRESS_DAT
    function &deleteGuarantorAddressDat($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除 -- 保証人住所データ
        $query  = " DELETE FROM GUARANTOR_ADDRESS_DAT WHERE SCHREGNO = '{$model->schregno}' AND ISSUEDATE = date('".str_replace("/", "-", $model->guarantor_issuedate)."') ";
        $db->query($query);

        //更新 -- 保護者データ
        $query = knjh410_knja110_2aQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARANTOR_ZIPCD"][TEXT]  = $setAddr["GUARANTOR_ZIPCD"];
        $data["GUARANTOR_ADDR1"][TEXT]  = $setAddr["GUARANTOR_ADDR1"];
        $data["GUARANTOR_ADDR2"][TEXT]  = $setAddr["GUARANTOR_ADDR2"];
        $data["GUARANTOR_TELNO"][TEXT]  = $setAddr["GUARANTOR_TELNO"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where = "WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data,"GUARDIAN_DAT",$where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //DELETE -- SCHREG_SEND_ADDRESS_DAT
    function &deleteSchregSendAddressDat($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除 -- その他住所データ
        $query  = " DELETE FROM SCHREG_SEND_ADDRESS_DAT WHERE SCHREGNO = '{$model->schregno}' AND DIV = '".$model->send_div."' ";
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //生徒の緊急連絡先情報を取得するSQLを発行
    function getEmergencyInfo($schregno)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     EMERGENCYCALL,  EMERGENCYNAME,  EMERGENCYRELA_NAME,  EMERGENCYTELNO,    ";
        $query .= "     EMERGENCYCALL2, EMERGENCYNAME2, EMERGENCYRELA_NAME2, EMERGENCYTELNO2    ";
        $query .= " FROM SCHREG_BASE_MST ";
        $query .= " WHERE SCHREGNO  = '".$schregno."' ";

        return $query;
    }

    //生徒の緊急連絡先情報を更新
    function updateEmergencyInfo(&$db, &$model, $flg)
    {
        $data = array();

        $data["EMERGENCYCALL"][TEXT]       = ($flg == "update")? $model->field["EMERGENCYCALL"] : "";
        $data["EMERGENCYNAME"][TEXT]       = ($flg == "update")? $model->field["EMERGENCYNAME"] : "";
        $data["EMERGENCYRELA_NAME"][TEXT]  = ($flg == "update")? $model->field["EMERGENCYRELA_NAME"] : "";
        $data["EMERGENCYTELNO"][TEXT]      = ($flg == "update")? $model->field["EMERGENCYTELNO"] : "";

        $data["EMERGENCYCALL2"][TEXT]      = ($flg == "update")? $model->field["EMERGENCYCALL2"] : "";
        $data["EMERGENCYNAME2"][TEXT]      = ($flg == "update")? $model->field["EMERGENCYNAME2"] : "";
        $data["EMERGENCYRELA_NAME2"][TEXT] = ($flg == "update")? $model->field["EMERGENCYRELA_NAME2"] : "";
        $data["EMERGENCYTELNO2"][TEXT]     = ($flg == "update")? $model->field["EMERGENCYTELNO2"] : "";

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $where  ="WHERE SCHREGNO = '".$model->schregno."'";

        $query = Query::updateSQL($data,"SCHREG_BASE_MST",$where);
        $db->query($query);
    }


    //最終履歴の終了日取得
    function get_last_expiredate($model, $schregno) {
        if ($model->infoDiv == "4") {
            $table = " GUARANTOR_HIST_DAT ";
        } else {
            $table = " GUARDIAN{$model->guardian2}_HIST_DAT ";
        }

        $query  = " SELECT ";
        $query .= "     EXPIREDATE ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //最終履歴がなかったら入学日付を取得する
    function get_ent_date($schregno) {
        $query  = " SELECT ";
        $query .= "     ENT_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    function getNameMst($namecd1) {
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

    function getGuardianHist($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ISSUEDATE, ";
        $query .= "     T1.EXPIREDATE, ";
        $query .= "     T1.RELATIONSHIP, ";
        $query .= "     T1.GUARD_NAME, ";
        $query .= "     T1.GUARD_KANA, ";
        $query .= "     T1.GUARD_REAL_NAME, ";
        $query .= "     T1.GUARD_REAL_KANA, ";
        $query .= "     T1.GUARD_SEX, ";
        $query .= "     T1.GUARD_BIRTHDAY, ";
        $query .= "     L1.NAME1 AS RELATION_NAME, ";
        $query .= "     L2.NAME1 AS GUARD_SEX_NAME, ";
        $query .= "     T1.RELATIONSHIP_FLG, ";
        $query .= "     T1.GUARD_NAME_FLG, ";
        $query .= "     T1.GUARD_KANA_FLG, ";
        $query .= "     T1.GUARD_REAL_NAME_FLG, ";
        $query .= "     T1.GUARD_REAL_KANA_FLG, ";
        $query .= "     T1.GUARD_SEX_FLG, ";
        $query .= "     T1.GUARD_BIRTHDAY_FLG ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_HIST_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'H201' ";
        $query .= "          AND T1.RELATIONSHIP = L1.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'Z002' ";
        $query .= "          AND T1.GUARD_SEX = L2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ISSUEDATE ";

        return $query;
    }

    function getGuardianHistData($model, $schregNo, $issueData) {
        $issueData = str_replace("/", "-", $issueData);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ISSUEDATE, ";
        $query .= "     T1.EXPIREDATE, ";
        $query .= "     T1.RELATIONSHIP, ";
        $query .= "     T1.GUARD_NAME, ";
        $query .= "     T1.GUARD_KANA, ";
        $query .= "     T1.GUARD_REAL_NAME, ";
        $query .= "     T1.GUARD_REAL_KANA, ";
        $query .= "     T1.GUARD_SEX, ";
        $query .= "     T1.GUARD_BIRTHDAY, ";
        $query .= "     T1.RELATIONSHIP_FLG, ";
        $query .= "     T1.GUARD_NAME_FLG, ";
        $query .= "     T1.GUARD_KANA_FLG, ";
        $query .= "     T1.GUARD_REAL_NAME_FLG, ";
        $query .= "     T1.GUARD_REAL_KANA_FLG, ";
        $query .= "     T1.GUARD_SEX_FLG, ";
        $query .= "     T1.GUARD_BIRTHDAY_FLG ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregNo."' ";
        $query .= "     AND T1.ISSUEDATE = '".$issueData."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ISSUEDATE ";

        return $query;
    }

    function getGuardianHistDateCheck($model, $table="") {
        $issueData = str_replace("/", "-", $model->histField["ISSUEDATE"]);
        $expireDate = str_replace("/", "-", $model->histField["EXPIREDATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        if ($table) {
            $query .=       $table." T1 ";
        } else {
            $query .= "     GUARDIAN{$model->guardian2}_HIST_DAT T1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND T1.ISSUEDATE <> '".$issueData."' ";
        $query .= "     AND (T1.ISSUEDATE BETWEEN '".$issueData."' AND '".$expireDate."' ";
        $query .= "          OR ";
        $query .= "          T1.EXPIREDATE BETWEEN '".$issueData."' AND '".$expireDate."') ";

        return $query;
    }

    function getDelHistQuery($model) {
        $query  = " DELETE FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND ISSUEDATE = '".str_replace("/", "-", $model->histField["ISSUEDATE"])."' ";

        return $query;
    }

    function getInsHistQuery($model) {
        $data["SCHREGNO"][TEXT]            = $model->schregno;
        $data["ISSUEDATE"][DATE]           = $model->histField["ISSUEDATE"];
        $data["EXPIREDATE"][DATE]          = $model->histField["EXPIREDATE"];
        $data["RELATIONSHIP"][TEXT]        = $model->histField["RELATIONSHIP"];
        $data["GUARD_NAME"][TEXT]          = $model->histField["GUARD_NAME"];
        $data["GUARD_KANA"][TEXT]          = $model->histField["GUARD_KANA"];
        $data["GUARD_REAL_NAME"][TEXT]     = $model->histField["GUARD_REAL_NAME"];
        $data["GUARD_REAL_KANA"][TEXT]     = $model->histField["GUARD_REAL_KANA"];
        $data["GUARD_SEX"][TEXT]           = $model->histField["GUARD_SEX"];
        $data["GUARD_BIRTHDAY"][DATE]      = $model->histField["GUARD_BIRTHDAY"];

        $data["RELATIONSHIP_FLG"][TEXT]    = $model->histField["RELATIONSHIP_FLG"]    ? $model->histField["RELATIONSHIP_FLG"]    : "0";
        $data["GUARD_NAME_FLG"][TEXT]      = $model->histField["GUARD_NAME_FLG"]      ? $model->histField["GUARD_NAME_FLG"]      : "0";
        $data["GUARD_KANA_FLG"][TEXT]      = $model->histField["GUARD_KANA_FLG"]      ? $model->histField["GUARD_KANA_FLG"]      : "0";
        $data["GUARD_REAL_NAME_FLG"][TEXT] = $model->histField["GUARD_REAL_NAME_FLG"] ? $model->histField["GUARD_REAL_NAME_FLG"] : "0";
        $data["GUARD_REAL_KANA_FLG"][TEXT] = $model->histField["GUARD_REAL_KANA_FLG"] ? $model->histField["GUARD_REAL_KANA_FLG"] : "0";
        $data["GUARD_SEX_FLG"][TEXT]       = $model->histField["GUARD_SEX_FLG"]       ? $model->histField["GUARD_SEX_FLG"]       : "0";
        $data["GUARD_BIRTHDAY_FLG"][TEXT]  = $model->histField["GUARD_BIRTHDAY_FLG"]  ? $model->histField["GUARD_BIRTHDAY_FLG"]  : "0";

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $query = Query::insertSQL($data, "GUARDIAN{$model->guardian2}_HIST_DAT");

        return $query;
    }

    function getGuardHistUpd($model, $schregno, $table) {
        $query  = " WITH SYSD(SYSD) AS ( ";
        $query .= "     VALUES(CAST(sysdate() AS DATE)) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .=       $table." T1, ";
        $query .= "     SYSD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregno}' ";
        $query .= "     AND SYSD.SYSD BETWEEN T1.ISSUEDATE AND T1.EXPIREDATE ";

        return $query;
    }

    function getGuardUpdFromHist($model, $data, $schregno, $table) {
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $where  = "WHERE SCHREGNO = '{$schregno}' ";
        $query = Query::updateSQL($data, $table, $where);

        return $query;
    }

    //保証人履歴データ取得
    function getGuarantorHistDat($model, $issuedate="") {
        $query  = " SELECT ";
        $query .= "     *";
        $query .= " FROM ";
        $query .= "     GUARANTOR_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        if ($issuedate) {
            $query .= " AND ISSUEDATE   = '".str_replace("/", "-", $issuedate)."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     ISSUEDATE ";
        }

        return $query;
    }

    //保護者データ取得
    function getGuardianDat($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO  = '{$schregno}' ";

        return $query;
    }

    //保証人履歴データ削除
    function deleteGuarantorHistDat($model) {
        $query  = " DELETE FROM ";
        $query .= "     GUARANTOR_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     ISSUEDATE   = '".str_replace("/", "-", $model->histField["ISSUEDATE"])."' ";

        return $query;
    }

    //保証人履歴データ追加
    function insertGuarantorHistDat($model) {
        $data = array();
        $data["SCHREGNO"][TEXT]                 = $model->schregno;
        $data["ISSUEDATE"][DATE]                = str_replace("/", "-", $model->histField["ISSUEDATE"]);
        $data["EXPIREDATE"][DATE]               = str_replace("/", "-", $model->histField["EXPIREDATE"]);
        $data["GUARANTOR_RELATIONSHIP"][TEXT]   = $model->histField["GUARANTOR_RELATIONSHIP"];
        $data["GUARANTOR_NAME"][TEXT]           = $model->histField["GUARANTOR_NAME"];
        $data["GUARANTOR_KANA"][TEXT]           = $model->histField["GUARANTOR_KANA"];
        $data["GUARANTOR_REAL_NAME"][TEXT]      = $model->histField["GUARANTOR_REAL_NAME"];
        $data["GUARANTOR_REAL_KANA"][TEXT]      = $model->histField["GUARANTOR_REAL_KANA"];
        $data["GUARANTOR_SEX"][TEXT]            = $model->histField["GUARANTOR_SEX"];

        $data["GUARANTOR_RELATIONSHIP_FLG"][TEXT] = $model->histField["GUARANTOR_RELATIONSHIP_FLG"] ? $model->histField["GUARANTOR_RELATIONSHIP_FLG"] : "0";
        $data["GUARANTOR_NAME_FLG"][TEXT]       = $model->histField["GUARANTOR_NAME_FLG"]      ? $model->histField["GUARANTOR_NAME_FLG"]      : "0";
        $data["GUARANTOR_KANA_FLG"][TEXT]       = $model->histField["GUARANTOR_KANA_FLG"]      ? $model->histField["GUARANTOR_KANA_FLG"]      : "0";
        $data["GUARANTOR_REAL_NAME_FLG"][TEXT]  = $model->histField["GUARANTOR_REAL_NAME_FLG"] ? $model->histField["GUARANTOR_REAL_NAME_FLG"] : "0";
        $data["GUARANTOR_REAL_KANA_FLG"][TEXT]  = $model->histField["GUARANTOR_REAL_KANA_FLG"] ? $model->histField["GUARANTOR_REAL_KANA_FLG"] : "0";
        $data["GUARANTOR_SEX_FLG"][TEXT]        = $model->histField["GUARANTOR_SEX_FLG"]       ? $model->histField["GUARANTOR_SEX_FLG"]       : "0";

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $query = Query::insertSQL($data, "GUARANTOR_HIST_DAT");

        return $query;
    }
}
?>
