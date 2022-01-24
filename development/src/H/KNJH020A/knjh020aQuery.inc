<?php

require_once('for_php7.php');

class knjh020aQuery extends Query
{
    public function getRowHimself($model)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT name_show, birthday FROM schreg_base_mst ";
        $query .= "  WHERE schregno ='".$model->schregno."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //保護者情報を取得
    public function getRowParents($model)
    {
        if ($model->schregno != "") {
            $db = Query::dbCheckOut();

            $query  = " SELECT * ";
            $query .= "   FROM guardian_dat ";
            $query .= "  WHERE schregno = '".$model->schregno."' ";

            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
        }
        return $row;
    }

    //その他情報を取得
    public function getRowOthers($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     PUBLIC_OFFICE AS SEND_PUBLIC_OFFICE ";
        $query .= " FROM ";
        $query .= "     SCHREG_SEND_ADDRESS_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //コンボボックスの地区コードを表示
    public function getNameMstA020()
    {
        return "SELECT namecd2, name1 FROM v_name_mst WHERE namecd1 ='A020' AND year = '".CTRL_YEAR."'";
    }

    public function getGuardian($schregno)
    {
        $query  = " SELECT COUNT(*) ";
        $query .= "   FROM guardian_dat ";
        $query .= "  WHERE schregno = '".$schregno."' ";

        return $query;
    }

    //NAME_MSTから情報を取得
    public function getNameMstData($cd)
    {
        $query  = " SELECT * ";
        $query .= "   FROM v_name_mst ";
        $query .= "  WHERE year ='".CTRL_YEAR."'";
        $query .= "    AND namecd1 ='".$cd."'";

        return $query;
    }

    public function getGrdCls($year)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "       GRADE || ',' || HR_CLASS AS GC, GRADE, HR_CLASS, ";
        $query .= "       HR_NAME AS GC_J ";
        $query .= " FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE YEAR = '".$year."' ";
        $query .= " ORDER BY GC";

        return $query;
    }

    /**
     * INSERT
     */
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "SELECT COUNT(*) FROM guardian_dat WHERE schregno = '" .$model->schregno ."'";

        $kekka = $db->getOne($query);

        if ($kekka == 0) {
            $data["SCHREGNO"][TEXT]                 = $model->schregno;
        }
        $data["RELATIONSHIP"][TEXT]             = $model->field["RELATIONSHIP"];
        $data["GUARD_NAME"][TEXT]               = $model->field["GUARD_NAME"];
        $data["GUARD_KANA"][TEXT]               = $model->field["GUARD_KANA"];
        $data["GUARD_REAL_NAME"][TEXT]          = $model->field["GUARD_REAL_NAME"];
        $data["GUARD_REAL_KANA"][TEXT]          = $model->field["GUARD_REAL_KANA"];
        $data["GUARD_SEX"][TEXT]                = $model->field["GUARD_SEX"];
        if (preg_match("/[0-9]{4}\/[0-9]{2}\/[0-9]{2}/", $model->field["GUARD_BIRTHDAY"])) {
            $data["GUARD_BIRTHDAY"][TEXT]       = str_replace("/", "-", $model->field["GUARD_BIRTHDAY"]);
        }
        $data["GUARD_JOBCD"][TEXT]              = $model->field["GUARD_JOBCD"];
        $data["GUARD_WORK_NAME"][TEXT]          = $model->field["GUARD_WORK_NAME"];
        $data["GUARD_WORK_TELNO"][TEXT]         = $model->field["GUARD_WORK_TELNO"];

        $data["GUARANTOR_RELATIONSHIP"][TEXT]   = $model->field["GUARANTOR_RELATIONSHIP"];
        $data["GUARANTOR_NAME"][TEXT]           = $model->field["GUARANTOR_NAME"];
        $data["GUARANTOR_KANA"][TEXT]           = $model->field["GUARANTOR_KANA"];
        $data["GUARANTOR_SEX"][TEXT]            = $model->field["GUARANTOR_SEX"];
        $data["GUARANTOR_JOBCD"][TEXT]          = $model->field["GUARANTOR_JOBCD"];
        $data["PUBLIC_OFFICE"][TEXT]            = $model->field["PUBLIC_OFFICE"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "sysdate()";
        if ($kekka == 0) {
            $query = Query::insertSQL($data, "GUARDIAN_DAT");
        } else {
            $where = "WHERE schregno = '" .$model->schregno ."'";
            $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
        }
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * INSERT その他情報
     */
    public function &getInsert2Query($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $div_array = array("1", "2", "3");
        foreach ($div_array as $div) {
            $div_name = "_".$div;

            //削除
            $query  = " DELETE FROM ";
            $query .= "     SCHREG_SEND_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $query .= "     DIV         = '".$div."' ";
            $db->query($query);

            //追加
            $data = array();
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["DIV"][TEXT]                  = $div;
            $data["SEND_RELATIONSHIP"][TEXT]    = $model->field["SEND_RELATIONSHIP".$div_name];
            $data["SEND_NAME"][TEXT]            = $model->field["SEND_NAME".$div_name];
            $data["SEND_KANA"][TEXT]            = $model->field["SEND_KANA".$div_name];
            $data["SEND_SEX"][TEXT]             = $model->field["SEND_SEX".$div_name];
            $data["SEND_ZIPCD"][TEXT]           = $model->field["SEND_ZIPCD".$div_name];
            $data["SEND_AREACD"][TEXT]          = $model->field["SEND_AREACD".$div_name];
            $data["SEND_ADDR1"][TEXT]           = $model->field["SEND_ADDR1".$div_name];
            $data["SEND_ADDR2"][TEXT]           = $model->field["SEND_ADDR2".$div_name];
            $data["SEND_ADDR_FLG"][TEXT]        = $model->field["SEND_ADDR_FLG".$div_name];
            $data["SEND_TELNO"][TEXT]           = $model->field["SEND_TELNO".$div_name];
            $data["SEND_TELNO2"][TEXT]          = $model->field["SEND_TELNO2".$div_name];
            $data["SEND_JOBCD"][TEXT]           = $model->field["SEND_JOBCD".$div_name];
            $data["PUBLIC_OFFICE"][TEXT]        = $model->field["SEND_PUBLIC_OFFICE".$div_name];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_SEND_ADDRESS_DAT");
            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return true;
    }


    //保護者データをアップデート
    public function updateGuardianDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //GUARDIAN_HIST_DAT の更新
        knjh020aQuery::makeGuardianHistDat($model, $db);
        knjh020aQuery::makeGuarantorHistDat($model, $db);

        $data = array();
        $data["GUARD_JOBCD"][TEXT]              = $model->field["GUARD_JOBCD"];
        $data["GUARD_WORK_NAME"][TEXT]          = $model->field["GUARD_WORK_NAME"];
        $data["GUARD_WORK_TELNO"][TEXT]         = $model->field["GUARD_WORK_TELNO"];

        $data["GUARANTOR_RELATIONSHIP"][TEXT]   = $model->field["GUARANTOR_RELATIONSHIP"];
        $data["GUARANTOR_NAME"][TEXT]           = $model->field["GUARANTOR_NAME"];
        $data["GUARANTOR_KANA"][TEXT]           = $model->field["GUARANTOR_KANA"];
        $data["GUARANTOR_REAL_NAME"][TEXT]      = $model->field["GUARANTOR_REAL_NAME"];
        $data["GUARANTOR_REAL_KANA"][TEXT]      = $model->field["GUARANTOR_REAL_KANA"];
        $data["GUARANTOR_SEX"][TEXT]            = $model->field["GUARANTOR_SEX"];
        $data["GUARANTOR_JOBCD"][TEXT]          = $model->field["GUARANTOR_JOBCD"];
        $data["PUBLIC_OFFICE"][TEXT]            = $model->field["PUBLIC_OFFICE"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][FUNC]                  = "sysdate()";

        $where = "WHERE SCHREGNO = '{$model->schregno}'";

        //履歴を作った際に必ず「GUARDIAN_DAT」ができるので、UPDATE文で更新する
        $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

    //GUARDIAN_HIST_DAT の更新
    public function makeGuardianHistDat($model, $db)
    {
        $make_hist_flg = false;

        if ($model->field["RELATIONSHIP_FLG"]    == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARD_NAME_FLG"]      == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARD_KANA_FLG"]      == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARD_REAL_NAME_FLG"] == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARD_REAL_KANA_FLG"] == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARD_SEX_FLG"]       == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARD_BIRTHDAY_FLG"]  == '1') {
            $make_hist_flg = true;
        }

        if ($make_hist_flg) {
            $query = knjh020aQuery::getLastExpireDate($model->schregno);
            $last_expiredate = $db->getOne($query);

            $query = knjh020aQuery::getEntDate($model->schregno);
            $ent_date = $db->getOne($query);

            if (strlen($last_expiredate)) {
                $start_day = str_replace("-", "/", $last_expiredate);
                $start_day = date("Y-m-d", strtotime("1 day", strtotime($start_day)));
            } elseif (strlen($ent_date)) {
                $start_day = str_replace("-", "/", $ent_date);
                $start_day = date("Y-m-d", strtotime($start_day));
            } else {
                $start_day = CTRL_YEAR.'/04/01';
                $start_day = date("Y-m-d", strtotime($start_day));
            }

            $e_appdate = str_replace('/', '-', $model->field["E_APPDATE"]);

            $query  = " INSERT INTO GUARDIAN_HIST_DAT ";
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
                $data["SCHREGNO"][TEXT]            = $model->schregno;
                $data["RELATIONSHIP"][TEXT]        = $model->field["RELATIONSHIP"];
                $data["GUARD_NAME"][TEXT]          = $model->field["GUARD_NAME"];
                $data["GUARD_KANA"][TEXT]          = $model->field["GUARD_KANA"];
                $data["GUARD_REAL_NAME"][TEXT]     = $model->field["GUARD_REAL_NAME"];
                $data["GUARD_REAL_KANA"][TEXT]     = $model->field["GUARD_REAL_KANA"];
                $data["GUARD_SEX"][TEXT]           = $model->field["GUARD_SEX"];
                $guard_birthday = str_replace('/', '-', $model->field["GUARD_BIRTHDAY"]);
                $data["GUARD_BIRTHDAY"][TEXT]      = $guard_birthday;
                $data["RELATIONSHIP_FLG"][TEXT]    = $model->field["RELATIONSHIP_FLG"];
                $data["GUARD_NAME_FLG"][TEXT]      = $model->field["GUARD_NAME_FLG"];
                $data["GUARD_KANA_FLG"][TEXT]      = $model->field["GUARD_KANA_FLG"];
                $data["GUARD_REAL_NAME_FLG"][TEXT] = $model->field["GUARD_REAL_NAME_FLG"];
                $data["GUARD_REAL_KANA_FLG"][TEXT] = $model->field["GUARD_REAL_KANA_FLG"];
                $data["GUARD_SEX_FLG"][TEXT]       = $model->field["GUARD_SEX_FLG"];
                $data["GUARD_BIRTHDAY_FLG"][TEXT]  = $model->field["GUARD_BIRTHDAY_FLG"];

                $data["ISSUEDATE"][DATE]  = $sdate;
                $data["EXPIREDATE"][DATE] = $edate;

                $yoyakuQuery = Query::insertSQL($data, "GUARDIAN_HIST_DAT");
                $db->query($yoyakuQuery);
            }
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $guard_birthday = str_replace('/', '-', $model->field["GUARD_BIRTHDAY"]);

        $guardian_dat_count = $db->getOne($query);
        if ($guardian_dat_count > 0) {
            $data = array();
            $data["RELATIONSHIP"][TEXT]    = $model->field["RELATIONSHIP"];
            $data["GUARD_NAME"][TEXT]      = $model->field["GUARD_NAME"];
            $data["GUARD_KANA"][TEXT]      = $model->field["GUARD_KANA"];
            $data["GUARD_REAL_NAME"][TEXT] = $model->field["GUARD_REAL_NAME"];
            $data["GUARD_REAL_KANA"][TEXT] = $model->field["GUARD_REAL_KANA"];
            $data["GUARD_SEX"][TEXT]       = $model->field["GUARD_SEX"];
            $data["GUARD_BIRTHDAY"][TEXT]  = $guard_birthday;

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $where = "WHERE SCHREGNO = '{$model->schregno}'";

            $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
        } else {
            $data = array();
            $data["SCHREGNO"][TEXT]        = $model->schregno;
            $data["RELATIONSHIP"][TEXT]    = $model->field["RELATIONSHIP"];
            $data["GUARD_NAME"][TEXT]      = $model->field["GUARD_NAME"];
            $data["GUARD_KANA"][TEXT]      = $model->field["GUARD_KANA"];
            $data["GUARD_REAL_NAME"][TEXT] = $model->field["GUARD_REAL_NAME"];
            $data["GUARD_REAL_KANA"][TEXT] = $model->field["GUARD_REAL_KANA"];
            $data["GUARD_SEX"][TEXT]       = $model->field["GUARD_SEX"];
            $data["GUARD_BIRTHDAY"][TEXT]  = $guard_birthday;

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $query = Query::insertSQL($data, "GUARDIAN_DAT");
        }
        $db->query($query);

        //その他
        $div_array = array("1", "2", "3");
        foreach ($div_array as $div) {
            $div_name = "_".$div;

            //削除
            $query  = " DELETE FROM ";
            $query .= "     SCHREG_SEND_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $query .= "     DIV         = '".$div."' ";
            $db->query($query);

            //追加
            $data = array();
            $data["SCHREGNO"][TEXT]            = $model->schregno;
            $data["DIV"][TEXT]                 = $div;
            $data["SEND_RELATIONSHIP"][TEXT]   = $model->field["SEND_RELATIONSHIP".$div_name];
            $data["SEND_NAME"][TEXT]           = $model->field["SEND_NAME".$div_name];
            $data["SEND_KANA"][TEXT]           = $model->field["SEND_KANA".$div_name];
            $data["SEND_SEX"][TEXT]            = $model->field["SEND_SEX".$div_name];
            $data["SEND_ZIPCD"][TEXT]          = $model->field["SEND_ZIPCD".$div_name];
            $data["SEND_AREACD"][TEXT]         = $model->field["SEND_AREACD".$div_name];
            $data["SEND_ADDR1"][TEXT]          = $model->field["SEND_ADDR1".$div_name];
            $data["SEND_ADDR2"][TEXT]          = $model->field["SEND_ADDR2".$div_name];
            $data["SEND_ADDR_FLG"][TEXT]       = $model->field["SEND_ADDR_FLG".$div_name];
            $data["SEND_TELNO"][TEXT]          = $model->field["SEND_TELNO".$div_name];
            $data["SEND_TELNO2"][TEXT]         = $model->field["SEND_TELNO2".$div_name];
            $data["SEND_JOBCD"][TEXT]          = $model->field["SEND_JOBCD".$div_name];
            $data["PUBLIC_OFFICE"][TEXT]       = $model->field["SEND_PUBLIC_OFFICE".$div_name];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][FUNC]             = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_SEND_ADDRESS_DAT");
            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
    }

    //GUARDIAN_HIST_DAT の更新
    public function makeGuarantorHistDat($model, $db)
    {
        $make_hist_flg = false;

        if ($model->field["GUARANTOR_RELATIONSHIP_FLG"]    == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARANTOR_NAME_FLG"]      == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARANTOR_KANA_FLG"]      == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARANTOR_REAL_NAME_FLG"] == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARANTOR_REAL_KANA_FLG"] == '1') {
            $make_hist_flg = true;
        }
        if ($model->field["GUARANTOR_SEX_FLG"]       == '1') {
            $make_hist_flg = true;
        }

        if ($make_hist_flg) {
            $query = knjh020aQuery::getLastExpireDate2($model->schregno);
            $last_expiredate = $db->getOne($query);

            $query = knjh020aQuery::getEntDate($model->schregno);
            $ent_date = $db->getOne($query);

            if (strlen($last_expiredate)) {
                $start_day = str_replace("-", "/", $last_expiredate);
                $start_day = date("Y-m-d", strtotime("1 day", strtotime($start_day)));
            } elseif (strlen($ent_date)) {
                $start_day = str_replace("-", "/", $ent_date);
                $start_day = date("Y-m-d", strtotime($start_day));
            } else {
                $start_day = CTRL_YEAR.'/04/01';
                $start_day = date("Y-m-d", strtotime($start_day));
            }

            $e_appdate = str_replace('/', '-', $model->field["E_APPDATE2"]);

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
            $query .= "     '{$model->field["GUARANTOR_RELATIONSHIP_FLG"]}'    AS GUARANTOR_RELATIONSHIP_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_NAME_FLG"]}'      AS GUARANTOR_NAME_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_KANA_FLG"]}'      AS GUARANTOR_KANA_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_REAL_NAME_FLG"]}' AS GUARANTOR_REAL_NAME_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_REAL_KANA_FLG"]}' AS GUARANTOR_REAL_KANA_FLG, ";
            $query .= "     '{$model->field["GUARANTOR_SEX_FLG"]}'       AS GUARANTOR_SEX_FLG, ";
            $query .= "     REGISTERCD, ";
            $query .= "     UPDATED ";
            $query .= " FROM ";
            $query .= "     GUARDIAN_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '{$model->schregno}' ";
            $query .= "     AND GUARANTOR_RELATIONSHIP IS NOT NULL ";

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

                $data["GUARANTOR_RELATIONSHIP"][TEXT]   = $model->field["GUARANTOR_RELATIONSHIP"];
                $data["GUARANTOR_NAME"][TEXT]           = $model->field["GUARANTOR_NAME"];
                $data["GUARANTOR_KANA"][TEXT]           = $model->field["GUARANTOR_KANA"];
                $data["GUARANTOR_REAL_NAME"][TEXT]      = $model->field["GUARANTOR_REAL_NAME"];
                $data["GUARANTOR_REAL_KANA"][TEXT]      = $model->field["GUARANTOR_REAL_KANA"];
                $data["GUARANTOR_SEX"][TEXT]            = $model->field["GUARANTOR_SEX"];

                $data["GUARANTOR_RELATIONSHIP_FLG"][TEXT]  = $model->field["GUARANTOR_RELATIONSHIP_FLG"];
                $data["GUARANTOR_NAME_FLG"][TEXT]          = $model->field["GUARANTOR_NAME_FLG"];
                $data["GUARANTOR_KANA_FLG"][TEXT]          = $model->field["GUARANTOR_KANA_FLG"];
                $data["GUARANTOR_REAL_NAME_FLG"][TEXT]     = $model->field["GUARANTOR_REAL_NAME_FLG"];
                $data["GUARANTOR_REAL_KANA_FLG"][TEXT]     = $model->field["GUARANTOR_REAL_KANA_FLG"];
                $data["GUARANTOR_SEX_FLG"][TEXT]           = $model->field["GUARANTOR_SEX_FLG"];

                $data["ISSUEDATE"][DATE]  = $sdate;
                $data["EXPIREDATE"][DATE] = $edate;

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
        if ($guardian_dat_count > 0) {
            $data = array();
            $data["GUARANTOR_RELATIONSHIP"][TEXT]   = $model->field["GUARANTOR_RELATIONSHIP"];
            $data["GUARANTOR_NAME"][TEXT]           = $model->field["GUARANTOR_NAME"];
            $data["GUARANTOR_KANA"][TEXT]           = $model->field["GUARANTOR_KANA"];
            $data["GUARANTOR_REAL_NAME"][TEXT]      = $model->field["GUARANTOR_REAL_NAME"];
            $data["GUARANTOR_REAL_KANA"][TEXT]      = $model->field["GUARANTOR_REAL_KANA"];
            $data["GUARANTOR_SEX"][TEXT]            = $model->field["GUARANTOR_SEX"];
            $data["REGISTERCD"][TEXT]               = STAFFCD;
            $data["UPDATED"][FUNC]                  = "SYSDATE()";

            $where = "WHERE SCHREGNO = '{$model->schregno}'";

            $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);
        } else {
            $data = array();
            $data["SCHREGNO"][TEXT]                 = $model->schregno;
            $data["GUARANTOR_RELATIONSHIP"][TEXT]   = $model->field["GUARANTOR_RELATIONSHIP"];
            $data["GUARANTOR_NAME"][TEXT]           = $model->field["GUARANTOR_NAME"];
            $data["GUARANTOR_KANA"][TEXT]           = $model->field["GUARANTOR_KANA"];
            $data["GUARANTOR_REAL_NAME"][TEXT]      = $model->field["GUARANTOR_REAL_NAME"];
            $data["GUARANTOR_REAL_KANA"][TEXT]      = $model->field["GUARANTOR_REAL_KANA"];
            $data["GUARANTOR_SEX"][TEXT]            = $model->field["GUARANTOR_SEX"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $query = Query::insertSQL($data, "GUARDIAN_DAT");
        }
        $db->query($query);

        //その他
        $div_array = array("1", "2", "3");
        foreach ($div_array as $div) {
            $div_name = "_".$div;

            //削除
            $query  = " DELETE FROM ";
            $query .= "     SCHREG_SEND_ADDRESS_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
            $query .= "     DIV         = '".$div."' ";
            $db->query($query);

            //追加
            $data = array();
            $data["SCHREGNO"][TEXT]            = $model->schregno;
            $data["DIV"][TEXT]                 = $div;
            $data["SEND_RELATIONSHIP"][TEXT]   = $model->field["SEND_RELATIONSHIP".$div_name];
            $data["SEND_NAME"][TEXT]           = $model->field["SEND_NAME".$div_name];
            $data["SEND_KANA"][TEXT]           = $model->field["SEND_KANA".$div_name];
            $data["SEND_SEX"][TEXT]            = $model->field["SEND_SEX".$div_name];
            $data["SEND_ZIPCD"][TEXT]          = $model->field["SEND_ZIPCD".$div_name];
            $data["SEND_AREACD"][TEXT]         = $model->field["SEND_AREACD".$div_name];
            $data["SEND_ADDR1"][TEXT]          = $model->field["SEND_ADDR1".$div_name];
            $data["SEND_ADDR2"][TEXT]          = $model->field["SEND_ADDR2".$div_name];
            $data["SEND_ADDR_FLG"][TEXT]       = $model->field["SEND_ADDR_FLG".$div_name];
            $data["SEND_TELNO"][TEXT]          = $model->field["SEND_TELNO".$div_name];
            $data["SEND_TELNO2"][TEXT]         = $model->field["SEND_TELNO2".$div_name];
            $data["SEND_JOBCD"][TEXT]          = $model->field["SEND_JOBCD".$div_name];
            $data["PUBLIC_OFFICE"][TEXT]       = $model->field["SEND_PUBLIC_OFFICE".$div_name];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][FUNC]             = "sysdate()";
            $query = Query::insertSQL($data, "SCHREG_SEND_ADDRESS_DAT");

            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
    }

    /**
     * DELETE
     */
    public function &getDeleteQuery($cd)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM guardian_dat WHERE schregno = '" .$cd ."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * DELETE　その他情報
     */
    public function &getDelete2Query($cd)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM SCHREG_SEND_ADDRESS_DAT WHERE SCHREGNO = '".$cd."' AND DIV IN ('1', '2', '3') ";
        $db->query($query);

        Query::dbCheckIn($db);

        return true;
    }


    //親族データ取得
    public function getParentsdataCnt($schregno)
    {
        $query  = " SELECT COUNT(*) FROM guardian_dat ";
        $query .= " WHERE schregno  = '" .$schregno."' ";

        return $query;
    }
    //親族データ取得
    public function getParentsData($schregno)
    {
        $query  = " SELECT * FROM schreg_rela_dat ";
        $query .= " WHERE schregno  = '" .$schregno."' ";
        $query .= " ORDER BY INT(relano) ";

        return $query;
    }

    public function getGuardianHist($model)
    {
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
        $query .= "     GUARDIAN_HIST_DAT T1 ";
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

    public function getGuarantorHist($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ISSUEDATE, ";
        $query .= "     T1.EXPIREDATE, ";
        $query .= "     T1.GUARANTOR_RELATIONSHIP, ";
        $query .= "     T1.GUARANTOR_NAME, ";
        $query .= "     T1.GUARANTOR_KANA, ";
        $query .= "     T1.GUARANTOR_REAL_NAME, ";
        $query .= "     T1.GUARANTOR_REAL_KANA, ";
        $query .= "     T1.GUARANTOR_SEX, ";
        $query .= "     L1.NAME1 AS RELATION_NAME, ";
        $query .= "     L2.NAME1 AS GUARANTOR_SEX_NAME, ";
        $query .= "     T1.GUARANTOR_RELATIONSHIP_FLG, ";
        $query .= "     T1.GUARANTOR_NAME_FLG, ";
        $query .= "     T1.GUARANTOR_KANA_FLG, ";
        $query .= "     T1.GUARANTOR_REAL_NAME_FLG, ";
        $query .= "     T1.GUARANTOR_REAL_KANA_FLG, ";
        $query .= "     T1.GUARANTOR_SEX_FLG ";
        $query .= " FROM ";
        $query .= "     GUARANTOR_HIST_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'H201' ";
        $query .= "          AND T1.GUARANTOR_RELATIONSHIP = L1.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'Z002' ";
        $query .= "          AND T1.GUARANTOR_SEX = L2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ISSUEDATE ";

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

    //最終履歴の終了日取得
    public function getLastExpireDate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     EXPIREDATE ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //最終履歴の終了日取得
    public function getLastExpireDate2($schregno)
    {
        $query  = " SELECT ";
        $query .= "     EXPIREDATE ";
        $query .= " FROM ";
        $query .= "     GUARANTOR_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //最終履歴がなかったら入学日付を取得する
    public function getEntDate($schregno)
    {
        $query  = " SELECT ";
        $query .= "     ENT_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    public function getGuardianHistData($schregno, $issuedata)
    {
        $issuedata = str_replace("/", "-", $issuedata);

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
        $query .= "     GUARDIAN_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";
        $query .= "     AND T1.ISSUEDATE = '".$issuedata."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ISSUEDATE ";

        return $query;
    }

    public function getGuarantorHistData($schregno, $issuedata)
    {
        $issuedata = str_replace("/", "-", $issuedata);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ISSUEDATE, ";
        $query .= "     T1.EXPIREDATE, ";
        $query .= "     T1.GUARANTOR_RELATIONSHIP, ";
        $query .= "     T1.GUARANTOR_NAME, ";
        $query .= "     T1.GUARANTOR_KANA, ";
        $query .= "     T1.GUARANTOR_REAL_NAME, ";
        $query .= "     T1.GUARANTOR_REAL_KANA, ";
        $query .= "     T1.GUARANTOR_SEX, ";
        $query .= "     T1.GUARANTOR_RELATIONSHIP_FLG, ";
        $query .= "     T1.GUARANTOR_NAME_FLG, ";
        $query .= "     T1.GUARANTOR_KANA_FLG, ";
        $query .= "     T1.GUARANTOR_REAL_NAME_FLG, ";
        $query .= "     T1.GUARANTOR_REAL_KANA_FLG, ";
        $query .= "     T1.GUARANTOR_SEX_FLG ";
        $query .= " FROM ";
        $query .= "     GUARANTOR_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";
        $query .= "     AND T1.ISSUEDATE = '".$issuedata."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ISSUEDATE ";

        return $query;
    }

    //保護者情報の取得
    public function getGuardianAddr($schregno, $guard_issuedate)
    {
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
        $query .= "     GUARDIAN_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     GUARDIAN_ADDRESS_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
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
    public function getGuarantorAddr($schregno, $guarantor_issuedate)
    {
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
        $query .= "     L1.GUARANTOR_TELNO, ";
        $query .= "     L1.ISSUEDATE AS GUARANTOR_ISSUEDATE, ";
        $query .= "     L1.EXPIREDATE AS GUARANTOR_EXPIREDATE, ";
        $query .= "     L1.UPDATED AS GUARANTOR_UPDATED ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     GUARANTOR_ADDRESS_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO  = '{$schregno}' ";
        if (strlen($guarantor_issuedate)) {
            $query .= "     AND L1.ISSUEDATE = '" . str_replace("/", "-", $guarantor_issuedate) . "' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    public function getGuardianHistDateCheck($model)
    {
        $issueData  = str_replace("/", "-", $model->histField["ISSUEDATE"]);
        $expireDate = str_replace("/", "-", $model->histField["EXPIREDATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.ISSUEDATE <> '{$issueData}' ";
        $query .= "     AND (T1.ISSUEDATE BETWEEN '{$issueData}' AND '{$expireDate}' ";
        $query .= "          OR ";
        $query .= "          T1.EXPIREDATE BETWEEN '{$issueData}' AND '{$expireDate}') ";

        return $query;
    }

    public function getGuarantorHistDateCheck($model)
    {
        $issueData  = str_replace("/", "-", $model->histField["ISSUEDATE"]);
        $expireDate = str_replace("/", "-", $model->histField["EXPIREDATE"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     GUARANTOR_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.ISSUEDATE <> '{$issueData}' ";
        $query .= "     AND (T1.ISSUEDATE BETWEEN '{$issueData}' AND '{$expireDate}' ";
        $query .= "          OR ";
        $query .= "          T1.EXPIREDATE BETWEEN '{$issueData}' AND '{$expireDate}') ";

        return $query;
    }

    public function getDelHistQuery($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     GUARDIAN_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND ISSUEDATE = '".str_replace("/", "-", $model->histField["ISSUEDATE"])."' ";

        return $query;
    }

    public function getInsHistQuery($model)
    {
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

        $data["RELATIONSHIP_FLG"][TEXT]    = $model->histField["RELATIONSHIP_FLG"] ? $model->histField["RELATIONSHIP_FLG"] : "0";
        $data["GUARD_NAME_FLG"][TEXT]      = $model->histField["GUARD_NAME_FLG"] ? $model->histField["GUARD_NAME_FLG"] : "0";
        $data["GUARD_KANA_FLG"][TEXT]      = $model->histField["GUARD_KANA_FLG"] ? $model->histField["GUARD_KANA_FLG"] : "0";
        $data["GUARD_REAL_NAME_FLG"][TEXT] = $model->histField["GUARD_REAL_NAME_FLG"] ? $model->histField["GUARD_REAL_NAME_FLG"] : "0";
        $data["GUARD_REAL_KANA_FLG"][TEXT] = $model->histField["GUARD_REAL_KANA_FLG"] ? $model->histField["GUARD_REAL_KANA_FLG"] : "0";
        $data["GUARD_SEX_FLG"][TEXT]       = $model->histField["GUARD_SEX_FLG"] ? $model->histField["GUARD_SEX_FLG"] : "0";
        $data["GUARD_BIRTHDAY_FLG"][TEXT]  = $model->histField["GUARD_BIRTHDAY_FLG"] ? $model->histField["GUARD_BIRTHDAY_FLG"] : "0";

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $query = Query::insertSQL($data, "GUARDIAN_HIST_DAT");
        return $query;
    }

    public function getDelHistHosyouNinQuery($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     GUARANTOR_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND ISSUEDATE = '".str_replace("/", "-", $model->histField["ISSUEDATE"])."' ";

        return $query;
    }

    public function getInsHistHosyouNinQuery($model)
    {
        $data["SCHREGNO"][TEXT]                 = $model->schregno;
        $data["ISSUEDATE"][DATE]                = $model->histField["ISSUEDATE"];
        $data["EXPIREDATE"][DATE]               = $model->histField["EXPIREDATE"];
        $data["GUARANTOR_RELATIONSHIP"][TEXT]   = $model->histField["GUARANTOR_RELATIONSHIP"];
        $data["GUARANTOR_NAME"][TEXT]           = $model->histField["GUARANTOR_NAME"];
        $data["GUARANTOR_KANA"][TEXT]           = $model->histField["GUARANTOR_KANA"];
        $data["GUARANTOR_REAL_NAME"][TEXT]      = $model->histField["GUARANTOR_REAL_NAME"];
        $data["GUARANTOR_REAL_KANA"][TEXT]      = $model->histField["GUARANTOR_REAL_KANA"];
        $data["GUARANTOR_SEX"][TEXT]            = $model->histField["GUARANTOR_SEX"];

        $data["GUARANTOR_RELATIONSHIP_FLG"][TEXT]    = $model->histField["GUARANTOR_RELATIONSHIP_FLG"] ? $model->histField["GUARANTOR_RELATIONSHIP_FLG"] : "0";
        $data["GUARANTOR_NAME_FLG"][TEXT]      = $model->histField["GUARANTOR_NAME_FLG"] ? $model->histField["GUARANTOR_NAME_FLG"] : "0";
        $data["GUARANTOR_KANA_FLG"][TEXT]      = $model->histField["GUARANTOR_KANA_FLG"] ? $model->histField["GUARANTOR_KANA_FLG"] : "0";
        $data["GUARANTOR_REAL_NAME_FLG"][TEXT] = $model->histField["GUARANTOR_REAL_NAME_FLG"] ? $model->histField["GUARANTOR_REAL_NAME_FLG"] : "0";
        $data["GUARANTOR_REAL_KANA_FLG"][TEXT] = $model->histField["GUARANTOR_REAL_KANA_FLG"] ? $model->histField["GUARANTOR_REAL_KANA_FLG"] : "0";
        $data["GUARANTOR_SEX_FLG"][TEXT]       = $model->histField["GUARANTOR_SEX_FLG"] ? $model->histField["GUARANTOR_SEX_FLG"] : "0";

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $query = Query::insertSQL($data, "GUARANTOR_HIST_DAT");
        return $query;
    }

    public function getGuardHistUpd($model, $schregno)
    {
        $query .= " WITH SYSD(SYSD) AS ( ";
        $query .= "     VALUES(CAST(sysdate() AS DATE)) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_HIST_DAT T1, ";
        $query .= "     SYSD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregno}' ";
        $query .= "     AND SYSD.SYSD BETWEEN T1.ISSUEDATE AND T1.EXPIREDATE ";

        return $query;
    }

    public function getGuarantHistUpd($model, $schregno)
    {
        $query .= " WITH SYSD(SYSD) AS ( ";
        $query .= "     VALUES(CAST(sysdate() AS DATE)) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     GUARANTOR_HIST_DAT T1, ";
        $query .= "     SYSD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregno}' ";
        $query .= "     AND SYSD.SYSD BETWEEN T1.ISSUEDATE AND T1.EXPIREDATE ";

        return $query;
    }

    public function getGuardUpdFromHist($model, $data, $schregno)
    {
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $where  = "WHERE SCHREGNO = '{$schregno}' ";
        $query = Query::updateSQL($data, "GUARDIAN_DAT", $where);

        return $query;
    }
}
