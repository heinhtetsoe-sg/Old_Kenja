<?php

require_once('for_php7.php');

class knjh020aForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh020aindex.php", "", "edit");

        //保護者履歴入力ボタン
        $link = REQUESTROOT."/H/KNJH020A/knjh020aindex.php?cmd=rireki&SCHREGNO=".$model->schregno;
        $extra = "onclick=\"Rireki_jumper('$link');\"";
        $arg["button"]["btn_rireki"] = knjCreateBtn($objForm, "btn_rireki", "保護者履歴修正", $extra);

        //保証人履歴入力ボタン
        $link = REQUESTROOT."/H/KNJH020A/knjh020aindex.php?cmd=rireki2&SCHREGNO=".$model->schregno;
        $extra = "onclick=\"Rireki_jumper('$link');\"";
        $arg["button"]["btn_rireki2"] = knjCreateBtn($objForm, "btn_rireki2", "保証人履歴修正", $extra);

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning)) {
            $Row_parents = knjh020aQuery::getRowParents($model);

            $Row_others =  array();
            $db = Query::dbCheckOut();
            $query  = knjh020aQuery::getRowOthers($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                foreach ($row as $key => $val) {
                    if (strpos($key, "SEND") !== false) {
                        $Row_others[$key."_".$row["DIV"]] = $row[$key];
                    }
                }
            }
            Query::dbCheckIn($db);
        } else {
            $Row_parents =& $model->field;
            $Row_others  =& $model->field;
        }
        //ヘッダー部作成
        $Row_himself = knjh020aQuery::getRowHimself($model);
        if ($model->schregno) {
            //学籍番号
            $arg["header"]["SCHREGNO"] = $model->schregno;
            //生徒氏名
            $arg["header"]["NAME_SHOW"] = $Row_himself["NAME_SHOW"];
            //生年月日
            $birth_day = array();
            $birth_day = explode("-", $Row_himself["BIRTHDAY"]);
            $arg["header"]["BIRTHDAY"] = $birth_day[0]."年".$birth_day[1]."月".$birth_day[2]."日";
        } else {
            //学籍番号
            $arg["header"]["SCHREGNO"] = "　　　　";
            //生徒氏名
            $arg["header"]["NAME_SHOW"] = "　　";
            //生年月日
            $arg["header"]["BIRTHDAY"] = "　年　月　日";
        }

        global $sess;
        //テキストエリア
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["J_STUCD"] = knjCreateTextBox($objForm, "", "J_STUCD", 10, 10, $extra);
        //検索ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('./knjh020aSubForm1.php?cmd=search&useGuardian2={$model->Properties["useGuardian2"]}&CD=$model->schregno&SCHREGNO='+document.forms[0]['J_STUCD'].value+'&STUCD_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 600, 350)\"";
        $arg["data"]["BTN_STUCD"] = knjCreateBtn($objForm, "btn_stucd", "兄弟姉妹検索", $extra);
        //反映ボタンを作成する
        $extra = "onclick=\"hiddenWin('./knjh020aSubForm1.php?cmd=apply&useGuardian2={$model->Properties["useGuardian2"]}&CD=$model->schregno&stucd='+document.forms[0]['J_STUCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["BTN_APPLY"] = knjCreateBtn($objForm, "btn_apply", "反映", $extra);

        if ($model->auth) {
            $link1 = REQUESTROOT."/H/KNJH020_2A/knjh020_2aindex.php?AUTH=".$model->auth."&SCHREGNO=".$model->schregno."&DAMMY=DAMMY";
        } else {
            $link1 = REQUESTROOT."/H/KNJH020_2A/knjh020_2aindex.php?AUTH=".AUTHORITY."&SCHREGNO=".$model->schregno."&DAMMY=DAMMY";
        }

        if ($model->Properties["useFamilyDat"] != '1') {
            //親族情報へボタンを作成する
            $extra = "onclick=\" Page_jumper('$link1');\"";
            $arg["button2"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "親族情報へ", $extra);
        }


        /** *************************保護者情報************************************ */
        //氏名(漢字)
        $extra ="onChange=\"\"";
        $arg["data"]["GUARD_NAME"] = knjCreateTextBox($objForm, $Row_parents["GUARD_NAME"], "GUARD_NAME", 40, 40, $extra);
        $arg["data"]["NAMESLEN"] = $model->nameSLen;

        //氏名(カナ)
        $extra ="onChange=\"\"";
        $arg["data"]["GUARD_KANA"] = knjCreateTextBox($objForm, $Row_parents["GUARD_KANA"], "GUARD_KANA", 80, 80, $extra);

        //氏名(戸籍氏名)
        $extra ="onChange=\"\"";
        $arg["data"]["GUARD_REAL_NAME"] = knjCreateTextBox($objForm, $Row_parents["GUARD_REAL_NAME"], "GUARD_REAL_NAME", 40, 40, $extra);

        //氏名(戸籍氏名かな)
        $extra ="onChange=\"\"";
        $arg["data"]["GUARD_REAL_KANA"] = knjCreateTextBox($objForm, $Row_parents["GUARD_REAL_KANA"], "GUARD_REAL_KANA", 80, 80, $extra);

        //性別
        $db     = Query::dbCheckOut();
        $query  = knjh020aQuery::getNameMstData("Z002");
        $result = $db->query($query);

        //性別コンボボックスの中身を作成------------------------------
        $opt_sex  = array();
        $info_sex = array();
        $opt_sex[]  = array("label"=>"","value"=>"0");
        $info_sex[] = array("label"=>"","value"=>"0");

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd2 = substr($row["NAMECD2"], 0, 1);
            $opt_sex[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME2"]),
                                "value" => $row["NAMECD2"]);
            $info_sex[$row["NAMECD2"]] = $row["NAME2"];
        }

        $extra = "onChange=\"\"";
        $arg["data"]["GUARD_SEX"] = knjCreateCombo($objForm, "GUARD_SEX", $Row_parents["GUARD_SEX"], $opt_sex, $extra, 1);

        //生年月日カレンダーコントロール
        $arg["data"]["GUARD_BIRTHDAY"] = View::popUpCalendar($objForm, "GUARD_BIRTHDAY", str_replace("-", "/", $Row_parents["GUARD_BIRTHDAY"]));

        //続柄
        $query  = knjh020aQuery::getNameMstData("H201");
        $result = $db->query($query);

        //続柄コンボボックスの中身を作成------------------------------
        $opt_relat = array();
        $info_relat = array();
        $opt_relat[]  = array("label"=>"","value"=>"0");
        $info_relat[] = array("label"=>"","value"=>"0");

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd2 = substr($row["NAMECD2"], 0, 2);
            $opt_relat[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME1"]),
                                  "value" => $row["NAMECD2"]);
            $info_relat[$row["NAMECD2"]] = $row["NAME1"];
        }

        $extra = "onChange=\"\"";
        $arg["data"]["RELATIONSHIP"] = knjCreateCombo($objForm, "RELATIONSHIP", $Row_parents["RELATIONSHIP"], $opt_relat, $extra, 1);

        //保護者情報郵便番号
        $arg["data"]["GUARD_ZIPCD"] = $Row_parents["GUARD_ZIPCD"];

        //保護者住所履歴入力ボタン
        $link = REQUESTROOT."/X/KNJX_GUARDIAN_ADDRESS/knjx_guardian_addressindex.php?cmd=jyuushorireki&SCHREGNO=".$model->schregno."&SEND_PRGID="."KNJH020A"."&SEND_AUTH=".$model->auth;
        $extra = "onclick=\"Rireki_jumper('$link');\"";
        $arg["button"]["btn_jyuushorireki"] = knjCreateBtn($objForm, "btn_jyuushorireki", "保護者住所履歴修正", $extra);

        //住所１
        $arg["data"]["GUARD_ADDR1"] = $Row_parents["GUARD_ADDR1"];

        //住所２
        $arg["data"]["GUARD_ADDR2"] = $Row_parents["GUARD_ADDR2"];

        //電話番号
        $arg["data"]["GUARD_TELNO"] = $Row_parents["GUARD_TELNO"];

        //電話番号２
        $arg["data"]["GUARD_TELNO2"] = $Row_parents["GUARD_TELNO2"];

        //ＦＡＸ番号
        $arg["data"]["GUARD_FAXNO"] = $Row_parents["GUARD_FAXNO"];

        //Ｅ－ＭＡＩＬ
        $arg["data"]["GUARD_E_MAIL"] = $Row_parents["GUARD_E_MAIL"];

        //勤務先名称
        $extra ="";
        $arg["data"]["GUARD_WORK_NAME"] = knjCreateTextBox($objForm, $Row_parents["GUARD_WORK_NAME"], "GUARD_WORK_NAME", 40, 40, $extra);

        //職種コード
        $query  = knjh020aQuery::getNameMstData("H202");
        $result = $db->query($query);

        //職種コンボボックスの中身を作成------------------------------
        $opt_jobcd   = array();
        $opt_jobcd[] = array("label" => "","value" => "00");

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd2 = substr($row["NAMECD2"], 0, 2);
            $opt_jobcd[] = array("label" => $namecd2.":".htmlspecialchars($row["NAME1"]),
                                 "value" => $row["NAMECD2"]
                                );
        }

        $extra = "onChange=\"\"";
        $arg["data"]["GUARD_JOBCD"] = knjCreateCombo($objForm, "GUARD_JOBCD", $Row_parents["GUARD_JOBCD"], $opt_jobcd, $extra, 1);

        //勤務先電話番号
        $extra = "onblur=\"this.value=toTelNo(this.value)\"";
        $arg["data"]["GUARD_WORK_TELNO"] = knjCreateTextBox($objForm, $Row_parents["GUARD_WORK_TELNO"], "GUARD_WORK_TELNO", 14, 14, $extra);

        /* ********************************************保証人情報********************************************** */
        //保証人氏名(漢字)
        $extra = "onChange=\"\"";
        $arg["data"]["GUARANTOR_NAME"] = knjCreateTextBox($objForm, $Row_parents["GUARANTOR_NAME"], "GUARANTOR_NAME", 40, 40, $extra);

        //保証人氏名(カナ)
        $extra = "onChange=\"\"";
        $arg["data"]["GUARANTOR_KANA"] = knjCreateTextBox($objForm, $Row_parents["GUARANTOR_KANA"], "GUARANTOR_KANA", 80, 80, $extra);

        //保証人氏名(戸籍氏名)
        $extra = "onChange=\"\"";
        $arg["data"]["GUARANTOR_REAL_NAME"] = knjCreateTextBox($objForm, $Row_parents["GUARANTOR_REAL_NAME"], "GUARANTOR_REAL_NAME", 40, 40, $extra);

        //保証人氏名(戸籍氏名かな)
        $extra = "onChange=\"\"";
        $arg["data"]["GUARANTOR_REAL_KANA"] = knjCreateTextBox($objForm, $Row_parents["GUARANTOR_REAL_KANA"], "GUARANTOR_REAL_KANA", 80, 80, $extra);

        //保証人性別
        $query  = knjh020aQuery::getNameMstData("Z002");
        $result = $db->query($query);

        //性別コンボボックスの中身を作成------------------------------
        $opt_sex  = array();
        $info_sex = array();
        $opt_sex[]  = array("label" => "","value" => "0");
        $info_sex[] = array("label" => "","value" => "0");

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd2 = substr($row["NAMECD2"], 0, 1);
            $opt_sex[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME2"]),
                                "value" => $row["NAMECD2"]);
            $info_sex[$row["NAMECD2"]] = $row["NAME2"];
        }

        $extra = "onChange=\"\"";
        $arg["data"]["GUARANTOR_SEX"] = knjCreateCombo($objForm, "GUARANTOR_SEX", $Row_parents["GUARANTOR_SEX"], $opt_sex, $extra, 1);

        //保証人生年月日カレンダーコントロール
        $arg["data"]["GUARANTOR_BIRTHDAY"] = View::popUpCalendar(
            $objForm,
            "GUARANTOR_BIRTHDAY",
            str_replace("-", "/", $Row_parents["GUARANTOR_BIRTHDAY"]),
            ""
        );

        //保証人続柄
        $query  = knjh020aQuery::getNameMstData("H201");
        $result = $db->query($query);

        //続柄コンボボックスの中身を作成------------------------------
        $opt_relat  = array();
        $info_relat = array();
        $opt_relat[]  = array("label" => "","value" => "00");
        $info_relat[] = array("label" => "","value" => "00");

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd2 = substr($row["NAMECD2"], 0, 2);
            $opt_relat[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME1"]),
                                  "value" => $row["NAMECD2"]);
            $info_relat[$row["NAMECD2"]] = $row["NAME1"];
        }

        $extra = "onChange=\"\"";
        $arg["data"]["GUARANTOR_RELATIONSHIP"] = knjCreateCombo($objForm, "GUARANTOR_RELATIONSHIP", $Row_parents["GUARANTOR_RELATIONSHIP"], $opt_relat, $extra, 1);

        //保証人情報郵便番号
        $arg["data"]["J_GUARANTOR_ZIPCD"] = $Row_parents["GUARANTOR_ZIPCD"];

        //保証人住所履歴入力ボタン
        $link = REQUESTROOT."/X/KNJX_GUARANTOR_ADDRESS/knjx_guarantor_addressindex.php?cmd=jyuushorireki&SCHREGNO=".$model->schregno."&SEND_PRGID="."KNJH020A"."&SEND_AUTH=".$model->auth;
        $extra = "onclick=\"Rireki_jumper('$link');\"";
        $arg["button"]["btn_jyuushorireki_hosyou"] = knjCreateBtn($objForm, "btn_jyuushorireki_hosyou", "保証人住所履歴修正", $extra);

        //保証人住所１
        $arg["data"]["GUARANTOR_ADDR1"] = $Row_parents["GUARANTOR_ADDR1"];

        //保証人住所２
        $arg["data"]["GUARANTOR_ADDR2"] = $Row_parents["GUARANTOR_ADDR2"];

        //保証人電話番号
        $arg["data"]["GUARANTOR_TELNO"] = $Row_parents["GUARANTOR_TELNO"];

        //職種コード
        $query  = knjh020aQuery::getNameMstData("H202");
        $result = $db->query($query);

        //職種コンボボックスの中身を作成------------------------------
        $opt_jobcd   = array();
        $opt_jobcd[] = array("label" => "","value" => "00");

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd2 = substr($row["NAMECD2"], 0, 2);
            $opt_jobcd[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME1"]),
                                  "value" => $row["NAMECD2"]);
        }

        $extra = "onChange=\"\"";
        $arg["data"]["GUARANTOR_JOBCD"] = knjCreateCombo($objForm, "GUARANTOR_JOBCD", $Row_parents["GUARANTOR_JOBCD"], $opt_jobcd, $extra, 1);

        //保証人等の勤務先タイトル名称設定
        $arg["data"]["PUBLIC_OFFICE_TITLE"] = $model->publicOffice_TitleName;
        $extra = "onChange=\"\"";
        $arg["data"]["PUBLIC_OFFICE"] = knjCreateTextBox($objForm, $Row_parents["PUBLIC_OFFICE"], "PUBLIC_OFFICE", 20, 20, $extra);


        /* ********************************************その他情報********************************************** */

        $div_array = array("1", "2", "3");
        foreach ($div_array as $div) {
            $div_name = "_".$div;

            //氏名テキストボックス
            $name = "SEND_NAME".$div_name;
            $extra = "";
            $arg["data"][$name] = knjCreateTextBox($objForm, $Row_others[$name], $name, 40, 40, $extra);

            //かなテキストボックス
            $name = "SEND_KANA".$div_name;
            $extra = "";
            $arg["data"][$name] = knjCreateTextBox($objForm, $Row_others[$name], $name, 80, 80, $extra);

            //性別コンボボックス
            $opt_sex  = array();
            $info_sex = array();
            $opt_sex[]  = array("label" => "","value" => "0");
            $info_sex[] = array("label" => "","value" => "0");

            $query  = knjh020aQuery::getNameMstData("Z002");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd2 = substr($row["NAMECD2"], 0, 1);
                $opt_sex[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME2"]),
                                    "value" => $row["NAMECD2"]);
                $info_sex[$row["NAMECD2"]] = $row["NAME2"];
            }

            $name = "SEND_SEX".$div_name;
            $extra = "";
            $arg["data"][$name] = knjCreateCombo($objForm, $name, $Row_others[$name], $opt_sex, $extra, 1);

            //続柄コンボボックス
            $opt_relat      = array();
            $info_relat     = array();
            $opt_relat[]    = array("label" => "","value" => "00");
            $info_relat[]   = array("label" => "","value" => "00");

            $query  = knjh020aQuery::getNameMstData("H201");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd2 = substr($row["NAMECD2"], 0, 2);
                $opt_relat[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME1"]),
                                      "value" => $row["NAMECD2"]);
                $info_relat[$row["NAMECD2"]] = $row["NAME1"];
            }

            $name = "SEND_RELATIONSHIP".$div_name;
            $extra = "";
            $arg["data"][$name] = knjCreateCombo($objForm, $name, $Row_others[$name], $opt_relat, $extra, 1);

            //郵便番号
            $name = "SEND_ZIPCD".$div_name;
            $arg["data"][$name] = View::popUpZipCode($objForm, $name, $Row_others[$name], "SEND_ADDR1".$div_name);

            //地区コンボボックス
            $name = "SEND_AREACD".$div_name;
            $result = $db->query(knjh020aQuery::getNameMstA020());
            $opt = array();
            $opt[] = array("label" => "","value" => "00");
            while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                               "value" => $row3["NAMECD2"]);
            }
            $arg["data"][$name] = knjCreateCombo($objForm, $name, $Row_others[$name], $opt, "", 1);

            //住所１テキストボックス
            $name = "SEND_ADDR1".$div_name;
            $extra = "";
            $arg["data"][$name] = knjCreateTextBox($objForm, $Row_others[$name], $name, 50, 90, $extra);

            //住所２テキストボックス
            $name = "SEND_ADDR2".$div_name;
            $extra = "";
            $arg["data"][$name] = knjCreateTextBox($objForm, $Row_others[$name], $name, 50, 90, $extra);

            //方書き印刷可チェックボックス
            $name = "SEND_ADDR_FLG".$div_name;
            $extra = $Row_others[$name] == "1" ? " checked " : "";
            $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "1", $extra);

            //電話番号１テキストボックス
            $name = "SEND_TELNO".$div_name;
            $extra = "onblur=\"this.value=toTelNo(this.value)\"";
            $arg["data"][$name] = knjCreateTextBox($objForm, $Row_others[$name], $name, 14, 14, $extra);

            //電話番号２テキストボックス
            $name = "SEND_TELNO2".$div_name;
            $extra = "onblur=\"this.value=toTelNo(this.value)\"";
            $arg["data"][$name] = knjCreateTextBox($objForm, $Row_others[$name], $name, 14, 14, $extra);

            //職種コンボボックス
            $opt_jobcd   = array();
            $opt_jobcd[] = array("label" => "","value" => "00");

            $query  = knjh020aQuery::getNameMstData("H202");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $namecd2 = substr($row["NAMECD2"], 0, 2);
                $opt_jobcd[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME1"]),
                                      "value" => $row["NAMECD2"]);
            }

            $name = "SEND_JOBCD".$div_name;
            $extra = "";
            $arg["data"][$name] = knjCreateCombo($objForm, $name, $Row_others[$name], $opt_jobcd, $extra, 1);

            //保証人等の勤務先タイトル名称テキストボックス
            $arg["data"]["SEND_PUBLIC_OFFICE_TITLE".$div_name] = $model->publicOffice_TitleName;
            $name = "SEND_PUBLIC_OFFICE".$div_name;
            $extra = "";
            $arg["data"][$name] = knjCreateTextBox($objForm, $Row_others[$name], $name, 20, 20, $extra);
        }
        $arg["data"]["ADDR_SLEN"] = $model->addrSLen;

        Query::dbCheckIn($db);


        //ここまで---------------------

        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');

        //削除ボタンを作成する
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row_parents["UPDATED"]);
        knjCreateHidden($objForm, "STAFFNAME", $Row["STAFFNAME"]);
        knjCreateHidden($objForm, "STAFFKANA", $Row["STAFFKANA"]);

        knjCreateHidden($objForm, "CHECK_GUARD_ISSUEDATE", $Row_parents["GUARD_ISSUEDATE"]);
        knjCreateHidden($objForm, "CHECK_GUARD_EXPIREDATE", $Row_parents["GUARD_EXPIREDATE"]);
        knjCreateHidden($objForm, "CHECK_RELATIONSHIP", $Row_parents["RELATIONSHIP"]);
        knjCreateHidden($objForm, "CHECK_GUARD_NAME", $Row_parents["GUARD_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARD_KANA", $Row_parents["GUARD_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARD_REAL_NAME", $Row_parents["GUARD_REAL_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARD_REAL_KANA", $Row_parents["GUARD_REAL_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARD_SEX", $Row_parents["GUARD_SEX"]);
        knjCreateHidden($objForm, "CHECK_GUARD_BIRTHDAY", $Row_parents["GUARD_BIRTHDAY"]);

        knjCreateHidden($objForm, "REQUESTROOT", REQUESTROOT);
        knjCreateHidden($objForm, "E_APPDATE");
        knjCreateHidden($objForm, "RELATIONSHIP_FLG");
        knjCreateHidden($objForm, "GUARD_NAME_FLG");
        knjCreateHidden($objForm, "GUARD_KANA_FLG");
        knjCreateHidden($objForm, "GUARD_REAL_NAME_FLG");
        knjCreateHidden($objForm, "GUARD_REAL_KANA_FLG");
        knjCreateHidden($objForm, "GUARD_SEX_FLG");
        knjCreateHidden($objForm, "GUARD_BIRTHDAY_FLG");

        knjCreateHidden($objForm, "CHECK_GUARANTOR_ISSUEDATE", $Row_parents["GUARANTOR_ISSUEDATE"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_EXPIREDATE", $Row_parents["GUARANTOR_EXPIREDATE"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_RELATIONSHIP", $Row_parents["GUARANTOR_RELATIONSHIP"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_NAME", $Row_parents["GUARANTOR_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_KANA", $Row_parents["GUARANTOR_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_REAL_NAME", $Row_parents["GUARANTOR_REAL_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_REAL_KANA", $Row_parents["GUARANTOR_REAL_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_SEX", $Row_parents["GUARANTOR_SEX"]);

        knjCreateHidden($objForm, "E_APPDATE2");
        knjCreateHidden($objForm, "GUARANTOR_RELATIONSHIP_FLG");
        knjCreateHidden($objForm, "GUARANTOR_NAME_FLG");
        knjCreateHidden($objForm, "GUARANTOR_KANA_FLG");
        knjCreateHidden($objForm, "GUARANTOR_REAL_NAME_FLG");
        knjCreateHidden($objForm, "GUARANTOR_REAL_KANA_FLG");
        knjCreateHidden($objForm, "GUARANTOR_SEX_FLG");

        //学籍番号
        knjCreateHidden($objForm, "tmpSCHREGNO", $model->schregno);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh020aForm1.html", $arg);
    }
}
