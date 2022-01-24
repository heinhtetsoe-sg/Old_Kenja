<?php

require_once('for_php7.php');

class knja110_2bQuery extends Query {

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
            $query .= "     GUARD_TELNO2, ";
            $query .= "     GUARD_FAXNO, ";
            $query .= "     GUARD_E_MAIL ";
            $query .= " FROM ";
            $query .= "     GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$model->schregno."' ";
            $query .= " ORDER BY ";
            $query .= "     ISSUEDATE ";
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
            $query  = " SELECT ";
            $query .= "     ISSUEDATE ";
            $query .= " FROM ";
            $query .= "     GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '{$model->schregno}' ";
            $query .= " ORDER BY ";
            $query .= "     ISSUEDATE DESC ";
            $query .= " FETCH FIRST ROW ONLY ";
        } else {
            $query  = " SELECT ";
            $query .= "     ISSUEDATE ";
            $query .= " FROM ";
            $query .= "     SCHREG_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '{$model->schregno}' ";
            $query .= " ORDER BY ";
            $query .= "     ISSUEDATE DESC ";
            $query .= " FETCH FIRST ROW ONLY ";
        }

        return $query;
    }


    //listの地区コード表示
    function List_AreaCd($cd)
    {
        return "SELECT name1 FROM v_name_mst WHERE namecd1 = 'A020' AND namecd2 = '".$cd."' AND year = '".CTRL_YEAR."'";
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
            $guardian2 = ($model->infoDiv == "3") ? "" : "2";
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
        $query .= "     L1.GUARD_TELNO2, ";
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

    //コンボボックスの地区コードを表示
    function getV_name_mst()
    {
        return "SELECT namecd2,name1 FROM v_name_mst WHERE namecd1 ='A020' AND year = '".CTRL_YEAR."'";
    }

    //コンボボックスのデータを名称マスタから取得
    function get_name_mst(){

        $query  = "SELECT namecd1, ";
        $query .= "       namecd2, ";
        $query .= "       name1 ";
        $query .= "  FROM v_name_mst";
        $query .= " WHERE namecd1 IN ('Z002','H201','H202') ";
        $query .= "   AND year = '".CTRL_YEAR."'";
        $query .= "ORDER BY namecd1,namecd2";
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
        $data["TELNO2"][TEXT]         = $model->field["TELNO2"];
        $data["FAXNO"][TEXT]          = $model->field["FAXNO"];
        $data["EMAIL"][TEXT]          = $model->field["EMAIL"];

        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data,"schreg_address_dat");
        $db->query($query);

        //急用連絡先を更新
        knja110_2bQuery::updateEmergencyInfo($db, $model, "update");

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
        $data["TELNO2"][TEXT]               = $model->field["TELNO2"];
        $data["FAXNO"][TEXT]                = $model->field["FAXNO"];
        $data["EMAIL"][TEXT]                = $model->field["EMAIL"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = "WHERE SCHREGNO  = '".$model->schregno."'";
        $where .= "  AND ISSUEDATE = date('".str_replace("/", "-", $model->issuedate)."')";

        $query = Query::updateSQL($data,"SCHREG_ADDRESS_DAT",$where);
        $db->query($query);

        //急用連絡先を更新(2005/10/20 追加)
        knja110_2bQuery::updateEmergencyInfo($db, $model, "update");


        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //保護者データをインサート
    function insert_Guardian_dat($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //GUARDIAN_HIST_DAT の更新
        knja110_2bQuery::make_guardian_hist_dat($model, $db);

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
        $data["GUARD_TELNO2"][TEXT]   = $model->field["GUARD_TELNO2"];
        $data["GUARD_FAXNO"][TEXT]    = $model->field["GUARD_FAXNO"];
        $data["GUARD_E_MAIL"][TEXT]   = $model->field["GUARD_E_MAIL"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][FUNC]        = "SYSDATE()";

        $query = Query::insertSQL($data,"GUARDIAN{$model->guardian2}_ADDRESS_DAT");
        $db->query($query);

        // 保護者情報
        $query = knja110_2bQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARD_ZIPCD"][TEXT]      = $setAddr["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $setAddr["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $setAddr["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]      = $setAddr["GUARD_TELNO"];
        $data["GUARD_TELNO2"][TEXT]     = $setAddr["GUARD_TELNO2"];
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
        knja110_2bQuery::make_guardian_hist_dat($model, $db);

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

        $query = Query::updateSQL($data,"GUARDIAN{$model->guardian2}_ADDRESS_DAT", $where);
        $db->query($query);

        // 保護者情報
        $query = knja110_2bQuery::getMaxAddress($model);
        $setAddr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $data = array();
        $data["GUARD_ZIPCD"][TEXT]      = $setAddr["GUARD_ZIPCD"];
        $data["GUARD_ADDR1"][TEXT]      = $setAddr["GUARD_ADDR1"];
        $data["GUARD_ADDR2"][TEXT]      = $setAddr["GUARD_ADDR2"];
        $data["GUARD_TELNO"][TEXT]      = $setAddr["GUARD_TELNO"];
        $data["GUARD_TELNO2"][TEXT]     = $setAddr["GUARD_TELNO2"];
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

    //住所履歴の最新取得
    function getMaxAddress($model) {
        $query  = " WITH MAX_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     MAX_T, ";
        $query .= "     GUARDIAN{$model->guardian2}_ADDRESS_DAT T1 ";
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
            $query = knja110_2bQuery::get_last_expiredate($model, $model->schregno);
            $last_expiredate = $db->getOne($query);

            $query = knja110_2bQuery::get_ent_date($model->schregno);
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

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $query  ="DELETE FROM schreg_address_dat WHERE schregno = '".$model->schregno."' AND issuedate = date('".str_replace("/", "-", $model->issuedate)."')";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    function &getDelete_GUARDIAN_ADDRESS_DAT($model) {
        $db = Query::dbCheckOut();
        $query  ="DELETE FROM GUARDIAN{$model->guardian2}_ADDRESS_DAT WHERE schregno = '{$model->schregno}' AND issuedate = date('".str_replace("/", "-", $model->guard_issuedate)."')";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }


    //生徒の緊急連絡先情報を取得するSQLを発行
    function getEmergencyInfo($schregno)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EMERGENCYCALL, ";
        $query .= "     T1.EMERGENCYNAME, ";
        $query .= "     T1.EMERGENCYRELA_NAME, ";
        $query .= "     T1.EMERGENCYTELNO, ";
        $query .= "     T1.EMERGENCYCALL2, ";
        $query .= "     T1.EMERGENCYNAME2, ";
        $query .= "     T1.EMERGENCYRELA_NAME2, ";
        $query .= "     T1.EMERGENCYTELNO2, ";
        $query .= "     L5.BASE_REMARK1 AS EMERGENCYCALL3, ";
        $query .= "     L5.BASE_REMARK2 AS EMERGENCYNAME3, ";
        $query .= "     L5.BASE_REMARK3 AS EMERGENCYRELA_NAME3, ";
        $query .= "     L5.BASE_REMARK4 AS EMERGENCYTELNO3 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST L5 ON T1.SCHREGNO = L5.SCHREGNO AND L5.BASE_SEQ = '005' ";
        $query .= " WHERE T1.SCHREGNO  = '".$schregno."' ";

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

        //急用連絡先3を更新(DelIns)
        $data = array();
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["BASE_SEQ"][TEXT]         = '005';
        $data["BASE_REMARK1"][TEXT]     = ($flg == "update")? $model->field["EMERGENCYCALL3"] : "";
        $data["BASE_REMARK2"][TEXT]     = ($flg == "update")? $model->field["EMERGENCYNAME3"] : "";
        $data["BASE_REMARK3"][TEXT]     = ($flg == "update")? $model->field["EMERGENCYRELA_NAME3"] : "";
        $data["BASE_REMARK4"][TEXT]     = ($flg == "update")? $model->field["EMERGENCYTELNO3"] : "";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "SYSDATE()";

        $query  = "DELETE FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '".$model->schregno."' AND BASE_SEQ = '005'";
        $db->query($query);

        $query = Query::insertSQL($data,"SCHREG_BASE_DETAIL_MST");
        $db->query($query);
    }


    //最終履歴の終了日取得
    function get_last_expiredate($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     EXPIREDATE ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_HIST_DAT ";
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

    function getGuardianHistDateCheck($model) {
        $issueData = str_replace("/", "-", $model->histField["ISSUEDATE"]);
        $expireDate = str_replace("/", "-", $model->histField["EXPIREDATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_HIST_DAT T1 ";
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

    function getGuardHistUpd($model, $schregno) {
        $query .= " WITH SYSD(SYSD) AS ( ";
        $query .= "     VALUES(CAST(sysdate() AS DATE)) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     GUARDIAN{$model->guardian2}_HIST_DAT T1, ";
        $query .= "     SYSD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregno}' ";
        $query .= "     AND SYSD.SYSD BETWEEN T1.ISSUEDATE AND T1.EXPIREDATE ";

        return $query;
    }

    function getGuardUpdFromHist($model, $data, $schregno) {
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $where  = "WHERE SCHREGNO = '{$schregno}' ";
        $query = Query::updateSQL($data, "GUARDIAN{$model->guardian2}_DAT", $where);

        return $query;
    }
}
?>
