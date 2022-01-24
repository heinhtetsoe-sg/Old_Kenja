<?php

require_once('for_php7.php');

class knjx_guardian_addressForm1
{
    public function main(&$model)
    {
        //フォーム作成
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjx_guardian_addressindex.php", "", "edit");
        $arg["reload"] = "";
        $db = Query::dbCheckOut();

        //起動チェック
        if (!knjx_guardian_addressQuery::checkToStart($db)) {
            $link = REQUESTROOT."/A/KNJA110A/knja110aindex.php?cmd=edit&schregno=".$model->schregno;
            $arg["close"] = "closing_window('$link');";
        }

        if ($model->cmd == "list2") {
            $link = REQUESTROOT."/X/KNJX_GUARDIAN_ADDRESS/knjx_guardian_addressindex.php?cmd=edit2&schregno=".$model->schregno."&INFO_DIV=".$model->infoDiv;
            $arg["close"] = "openEdit('$link');";
        }

        //学籍基礎マスタより学籍番号と名前を取得
        $Row         = $db->getRow(knjx_guardian_addressQuery::getSchregnoName($model), DB_FETCHMODE_ASSOC);
        $arg["NO"]   = $Row["SCHREGNO"];
        $arg["NAME"] = $Row["NAME"];

        //学籍住所データよりデータを取得
        if ($model->schregno) {
            $row2 = $db->getRow(knjx_guardian_addressQuery::getEmergencyInfo($model->schregno), DB_FETCHMODE_ASSOC);
            $result = $db->query(knjx_guardian_addressQuery::getAddressAll($model));

            while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row  = array_merge((array)$row1, (array)$row2);

                $name1 = $db->getOne(knjx_guardian_addressQuery::listAreaCd($row["AREACD"]));
                $row["AREA_CD"]          = $row["AREACD"].":".$name1;
                $row["ISSUEDATE"]        = str_replace("-", "/", $row["ISSUEDATE"]);
                $row["EXPIREDATE"]       = str_replace("-", "/", $row["EXPIREDATE"]);
                $row["GUARD_ISSUEDATE"]  = str_replace("-", "/", $row["GUARD_ISSUEDATE"]);
                $row["GUARD_EXPIREDATE"] = str_replace("-", "/", $row["GUARD_EXPIREDATE"]);
                $row["GUARD_ADDR_FLG"]   = $row["GUARD_ADDR_FLG"] == "1" ? "可" : "";
                $row["ADDR_FLG"]         = $row["ADDR_FLG"] == "1" ? "可" : "";
                $arg["rereki_data"][] = $row;
            }
        }

        //生徒情報の取得        (SCHREG_ADDRESS_DAT)
        $row1 = $db->getRow(knjx_guardian_addressQuery::getRowAddress($model->schregno, $model->issuedate), DB_FETCHMODE_ASSOC);
        //保護者情報の取得      (GUARDIAN_DAT、GUARDIAN_ADDRESS_DAT)
        $row2 = $db->getRow(knjx_guardian_addressQuery::getGuardianAddr($model->schregno, $model->guard_issuedate), DB_FETCHMODE_ASSOC);
        //緊急連絡先情報の取得  (SCHREG_BASE_MST)
        $row3 = $db->getRow(knjx_guardian_addressQuery::getEmergencyInfo($model->schregno), DB_FETCHMODE_ASSOC);

        $model->form2 = array_merge((array)$row1, (array)$row2, (array)$row3);

        if (!$model->isWarning()) {
            $row  = array_merge((array)$row1, (array)$row2, (array)$row3);
        } else {
            $row =& $model->field;
        }

        knjCreateHidden($objForm, "CHECK_GUARD_ISSUEDATE", $model->form2["GUARD_ISSUEDATE"]);
        knjCreateHidden($objForm, "CHECK_GUARD_EXPIREDATE", $model->form2["GUARD_EXPIREDATE"]);

        if (!$model->issuedate) {
            $model->issuedate = str_replace("-", "/", $row["ISSUEDATE"]);
        }

        if (!$model->guard_issuedate) {
            $model->guard_issuedate = str_replace("-", "/", $row["GUARD_ISSUEDATE"]);
        }

        /********************************************************************************/
        /********************************************************************************/
        /*******        *****************************************************************/
        /******* 保護者 *****************************************************************/
        /*******        *****************************************************************/
        /********************************************************************************/
        /********************************************************************************/
        //有効期間開始日付
        $arg["data"]["GUARD_ISSUEDATE"] = str_replace("input ", "input ", View::popUpCalendar($objForm, "GUARD_ISSUEDATE", str_replace("-", "/", $row["GUARD_ISSUEDATE"]), ""));
        //有効期間開始日付
        $arg["data"]["GUARD_EXPIREDATE"] = str_replace("input ", "input ", View::popUpCalendar($objForm, "GUARD_EXPIREDATE", str_replace("-", "/", $row["GUARD_EXPIREDATE"]), ""));
        //誕生日
        $arg["data"]["GUARD_BIRTHDAY"] = str_replace("-", "/", $row["GUARD_BIRTHDAY"]);
        //郵便番号
        $arg["data"]["GUARD_ZIPCD"] = str_replace("input ", "input {$guard_disabled} ", View::popUpZipCode($objForm, "GUARD_ZIPCD", $row["GUARD_ZIPCD"], "GUARD_ADDR1"));

        /********************/
        /* テキストボックス */
        /********************/
        //保護者氏名
        $arg["data"]["GUARD_NAME"] = $row["GUARD_NAME"];
        //保護者かな
        $arg["data"]["GUARD_KANA"] = $row["GUARD_KANA"];
        //保護者氏名
        $arg["data"]["GUARD_REAL_NAME"] = $row["GUARD_REAL_NAME"];
        //保護者かな
        $arg["data"]["GUARD_REAL_KANA"] = $row["GUARD_REAL_KANA"];

        //住所
        $arg["data"]["GUARD_ADDR1"] = knjCreateTextBox($objForm, $row["GUARD_ADDR1"], "GUARD_ADDR1", 50, 90, $extra);
        //方書き(アパート名等)
        $arg["data"]["GUARD_ADDR2"] = knjCreateTextBox($objForm, $row["GUARD_ADDR2"], "GUARD_ADDR2", 50, 90, $extra);
        //方書きを住所1とする
        $extra = $row["GUARD_ADDR_FLG"] == "1" ? " checked " : "";
        $arg["data"]["GUARD_ADDR_FLG"] = knjCreateCheckBox($objForm, "GUARD_ADDR_FLG", "1", $extra);
        //電話番号
        $arg["data"]["GUARD_TELNO"] = knjCreateTextBox($objForm, $row["GUARD_TELNO"], "GUARD_TELNO", 16, 14, $extra);
        //電話番号２
        $arg["data"]["GUARD_TELNO2"] = knjCreateTextBox($objForm, $row["GUARD_TELNO2"], "GUARD_TELNO2", 16, 14, $extra);
        //Fax番号
        $arg["data"]["GUARD_FAXNO"] = knjCreateTextBox($objForm, $row["GUARD_FAXNO"], "GUARD_FAXNO", 16, 14, $extra);
        //E-mail
        $arg["data"]["GUARD_E_MAIL"] = knjCreateTextBox($objForm, $row["GUARD_E_MAIL"], "GUARD_E_MAIL", 50, 50, $extra);

        //勤務先
        $arg["data"]["GUARD_WORK_NAME"]  = $row["GUARD_WORK_NAME"];
        //勤務先電話番号
        $arg["data"]["GUARD_WORK_TELNO"] = $row["GUARD_WORK_TELNO"];

        /******************/
        /* コンボボックス */
        /******************/
        //名称マスタよりコンボボックスのデータを取得
        $opt = array();
        $opt["Z002"][] = array("label" => "","value" => "0");
        $opt["H201"][] = array("label" => "","value" => "00");
        $opt["H202"][] = array("label" => "","value" => "00");
        $query = knjx_guardian_addressQuery::getNameMsts();
        $result = $db->query($query);
        while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[$row3["NAMECD1"]][$row3["NAMECD2"]] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                                                             "value" => $row3["NAMECD2"]);
        }
        $arg["data"]["GUARD_SEX"] = $opt["Z002"][$row["GUARD_SEX"]]["label"];
        $arg["data"]["RELATIONSHIP"] = $opt["H201"][$row["RELATIONSHIP"]]["label"];
        $arg["data"]["GUARD_JOBCD"] = $opt["H202"][$row["GUARD_JOBCD"]]["label"];

        /**********/
        /* ボタン */
        /**********/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add2');\"";
        $arg["button"]["btn_add2"]     = knjCreateBtn($objForm, "btn_add2", "追加", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update2');\"";
        $arg["button"]["btn_update2"]  = knjCreateBtn($objForm, "btn_update2", "更新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete2');\"";
        $arg["button"]["btn_del2"]     = knjCreateBtn($objForm, "btn_del2", "削除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset2"]   = knjCreateBtn($objForm, "btn_reset2", "取消", $extra);
        //戻るボタン
        $link = REQUESTROOT."/H/KNJH020A/knjh020aindex.php?cmd=edit&SCHREGNO=".$model->schregno."&SEND_PRGID="."KNJX_GUARDIAN_ADDRESS"."&SEND_AUTH=".$model->auth;
        $extra = "onclick=\"modoru('$link');\"";
        $arg["button"]["btn_end2"] = knjCreateBtn($objForm, "btn_end2", "戻る", $extra);

        $result->free();

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $row["UPDATED"]);
        knjCreateHidden($objForm, "GUARD_UPDATED", $row["GUARD_UPDATED"]);
        knjCreateHidden($objForm, "REQUESTROOT", REQUESTROOT);

        knjCreateHidden($objForm, "CLICK_GUARD_ISSUEDATE");

        Query::dbCheckIn($db);
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.top_frame.location.href='knjx_guardian_addressindex.php?cmd=list';";
        }

        View::toHTML($model, "knjx_guardian_addressForm1.html", $arg);
    }
}
