<?php

require_once('for_php7.php');

class knja110_2bForm2
{
    function main(&$model)
    {
        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knja110_2bindex.php", "", "edit");
        $arg["reload"] = "";
        $db = Query::dbCheckOut();

        //履歴入力ボタン
        $link = REQUESTROOT."/A/KNJA110_2B/knja110_2bindex.php?cmd=rireki&SCHREGNO=".$model->schregno;
        $extra = "onclick=\"Page_jumper('$link');\"";
        $arg["button"]["btn_rireki"] = knjCreateBtn($objForm, "btn_rireki", "保護者履歴修正", $extra);

        //生徒情報の取得        (SCHREG_ADDRESS_DAT)
        $row1 = $db->getRow(knja110_2bQuery::getRow_Address($model->schregno,$model->issuedate),DB_FETCHMODE_ASSOC);
        //保護者情報の取得      (GUARDIAN_DAT、GUARDIAN_ADDRESS_DAT)
        $row2 = $db->getRow(knja110_2bQuery::getGuardianAddr($model, $model->schregno,$model->guard_issuedate),DB_FETCHMODE_ASSOC);
        //緊急連絡先情報の取得  (SCHREG_BASE_MST)
        $row3 = $db->getRow(knja110_2bQuery::getEmergencyInfo($model->schregno),DB_FETCHMODE_ASSOC);

        $copyRow = array(); //コピーボタン:Hiddenで保持用
        if ($model->Properties["useGuardian2"] == '1') {
            $arg["useGuardian2"] = "ON";
            //保護者情報の取得      (GUARDIAN_DAT、GUARDIAN_ADDRESS_DAT)
            $copyRow = $db->getRow(knja110_2bQuery::getGuardianAddr($model, $model->schregno, "", "copy"),DB_FETCHMODE_ASSOC);

            knjCreateHidden($objForm, "COPY_GUARD_ISSUEDATE",  str_replace("-","/",$copyRow["GUARD_ISSUEDATE"]));
            knjCreateHidden($objForm, "COPY_GUARD_EXPIREDATE", str_replace("-","/",$copyRow["GUARD_EXPIREDATE"]));
            knjCreateHidden($objForm, "COPY_GUARD_ZIPCD",      $copyRow["GUARD_ZIPCD"]);
            knjCreateHidden($objForm, "COPY_GUARD_ADDR1",      $copyRow["GUARD_ADDR1"]);
            knjCreateHidden($objForm, "COPY_GUARD_ADDR2",      $copyRow["GUARD_ADDR2"]);
            knjCreateHidden($objForm, "COPY_GUARD_TELNO",      $copyRow["GUARD_TELNO"]);
            knjCreateHidden($objForm, "COPY_GUARD_TELNO2",     $copyRow["GUARD_TELNO2"]);
            knjCreateHidden($objForm, "COPY_GUARD_FAXNO",      $copyRow["GUARD_FAXNO"]);
        } else {
            $arg["useGuardian2"] = "";
        }

        $model->form2 = array_merge((array)$row1,(array)$row2,(array)$row3);

        if(!$model->isWarning()){
            $row  = array_merge((array)$row1,(array)$row2,(array)$row3);
        }else{
            $row =& $model->field;
        }

        knjCreateHidden($objForm, "CHECK_GUARD_ISSUEDATE",  $model->form2["GUARD_ISSUEDATE"]);
        knjCreateHidden($objForm, "CHECK_GUARD_EXPIREDATE", $model->form2["GUARD_EXPIREDATE"]);
        knjCreateHidden($objForm, "CHECK_RELATIONSHIP",     $model->form2["RELATIONSHIP"]);
        knjCreateHidden($objForm, "CHECK_GUARD_NAME",       $model->form2["GUARD_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARD_KANA",       $model->form2["GUARD_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARD_REAL_NAME",  $model->form2["GUARD_REAL_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARD_REAL_KANA",  $model->form2["GUARD_REAL_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARD_SEX",        $model->form2["GUARD_SEX"]);
        knjCreateHidden($objForm, "CHECK_GUARD_BIRTHDAY",   $model->form2["GUARD_BIRTHDAY"]);

        knjCreateHidden($objForm, "E_APPDATE" );
        knjCreateHidden($objForm, "RELATIONSHIP_FLG"    );
        knjCreateHidden($objForm, "GUARD_NAME_FLG"      );
        knjCreateHidden($objForm, "GUARD_KANA_FLG"      );
        knjCreateHidden($objForm, "GUARD_REAL_NAME_FLG" );
        knjCreateHidden($objForm, "GUARD_REAL_KANA_FLG" );
        knjCreateHidden($objForm, "GUARD_SEX_FLG"       );
        knjCreateHidden($objForm, "GUARD_BIRTHDAY_FLG"  );

        if (!$model->issuedate) {
            $model->issuedate = str_replace("-","/",$row["ISSUEDATE"]);
        }

        if (!$model->guard_issuedate) {
            $model->guard_issuedate = str_replace("-","/",$row["GUARD_ISSUEDATE"]);
        }

        if ($model->infoDiv == "2" || $model->infoDiv == "3") {
            $arg["infoDiv1"] = "";
            $arg["infoDiv2"] = "ON";
        } else {
            $arg["infoDiv1"] = "ON";
            $arg["infoDiv2"] = "";
        }

        if ($model->infoDiv == "2" || $model->infoDiv == "3") {
            $schreg_disabled = 'disabled';
            $guard_disabled  = '';
        } else {
            $schreg_disabled = '';
            $guard_disabled  = 'disabled';
        }

/******************************************************************************/
/******************************************************************************/
/*******      *****************************************************************/
/******* 生徒 *****************************************************************/
/*******      *****************************************************************/
/******************************************************************************/
/******************************************************************************/
        //有効期間開始日付
        $arg["data"]["ISSUEDATE"] = str_replace("input ", "input {$schreg_disabled} ", View::popUpCalendar($objForm, "ISSUEDATE", str_replace("-","/",$row["ISSUEDATE"]),""));
        //有効期間開始日付
        $arg["data"]["EXPIREDATE"] = str_replace("input ", "input {$schreg_disabled} ", View::popUpCalendar($objForm, "EXPIREDATE", str_replace("-","/",$row["EXPIREDATE"]),""));
        //郵便番号
        $arg["data"]["ZIPCD"] = str_replace("input ", "input {$schreg_disabled} ", View::popUpZipCode($objForm, "ZIPCD", $row["ZIPCD"],"ADDR1"));
        //地区コード
        $result = $db->query(knja110_2bQuery::getV_name_mst());
        $opt = array();
        $opt[] = array("label" => "","value" => "00");
        while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                           "value" => $row3["NAMECD2"]);
        }
        $extra = "" . $schreg_disabled;
        $arg["data"]["AREACD"] = knjCreateCombo($objForm, "AREACD", $row["AREACD"], $opt, $extra, 1);

        /********************/
        /* テキストボックス */
        /********************/
        //住所
        $extra = "" . $schreg_disabled;
        $arg["data"]["ADDR1"] = knjCreateTextBox($objForm, $row["ADDR1"], "ADDR1", 50, 90, $extra);
        //方書き(アパート名等)
        $extra = "" . $schreg_disabled;
        $arg["data"]["ADDR2"] = knjCreateTextBox($objForm, $row["ADDR2"], "ADDR2", 50, 90, $extra);
        //方書きを住所1とする
        $extra = $row["ADDR_FLG"] == "1" ? " checked " : "";
        $arg["data"]["ADDR_FLG"] = knjCreateCheckBox($objForm, "ADDR_FLG", "1", $extra.$schreg_disabled);
        //(英字)住所
        $extra = "" . $schreg_disabled;
        $arg["data"]["ADDR1_ENG"] = knjCreateTextBox($objForm, $row["ADDR1_ENG"], "ADDR1_ENG", 50, 70, $extra);
        //(英字)方書き(アパート名等)
        $extra = "" . $schreg_disabled;
        $arg["data"]["ADDR2_ENG"] = knjCreateTextBox($objForm, $row["ADDR2_ENG"], "ADDR2_ENG", 50, 70, $extra);
        //電話番号
        $extra = "" . $schreg_disabled;
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $row["TELNO"], "TELNO", 16, 14, $extra);
        //電話番号２
        $extra = "" . $schreg_disabled;
        $arg["data"]["TELNO2"] = knjCreateTextBox($objForm, $row["TELNO2"], "TELNO2", 16, 14, $extra);
        //Fax番号
        $extra = "" . $schreg_disabled;
        $arg["data"]["FAXNO"] = knjCreateTextBox($objForm, $row["FAXNO"], "FAXNO", 16, 14, $extra);
        //E-mail
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMAIL"] = knjCreateTextBox($objForm, $row["EMAIL"], "EMAIL", 25, 20, $extra);
        //急用連絡先
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYCALL"] = knjCreateTextBox($objForm, $row["EMERGENCYCALL"], "EMERGENCYCALL", 40, 60, $extra);
        //急用連絡氏名
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYNAME"] = knjCreateTextBox($objForm, $row["EMERGENCYNAME"], "EMERGENCYNAME", 40, 60, $extra);
        //急用連絡続柄名
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYRELA_NAME"] = knjCreateTextBox($objForm, $row["EMERGENCYRELA_NAME"], "EMERGENCYRELA_NAME", 22, 30, $extra);
        //急用電話番号
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYTELNO"] = knjCreateTextBox($objForm, $row["EMERGENCYTELNO"], "EMERGENCYTELNO", 16, 14, $extra);
        //急用連絡先２
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYCALL2"] = knjCreateTextBox($objForm, $row["EMERGENCYCALL2"], "EMERGENCYCALL2", 40, 60, $extra);
        //急用連絡氏名２
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYNAME2"] = knjCreateTextBox($objForm, $row["EMERGENCYNAME2"], "EMERGENCYNAME2", 40, 60, $extra);
        //急用連絡続柄名２
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYRELA_NAME2"] = knjCreateTextBox($objForm, $row["EMERGENCYRELA_NAME2"], "EMERGENCYRELA_NAME2", 22, 30, $extra);
        //急用電話番号２
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYTELNO2"] = knjCreateTextBox($objForm, $row["EMERGENCYTELNO2"], "EMERGENCYTELNO2", 16, 14, $extra);
        //急用連絡先３
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYCALL3"] = knjCreateTextBox($objForm, $row["EMERGENCYCALL3"], "EMERGENCYCALL3", 40, 60, $extra);
        //急用連絡氏名３
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYNAME3"] = knjCreateTextBox($objForm, $row["EMERGENCYNAME3"], "EMERGENCYNAME3", 40, 60, $extra);
        //急用連絡続柄名３
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYRELA_NAME3"] = knjCreateTextBox($objForm, $row["EMERGENCYRELA_NAME3"], "EMERGENCYRELA_NAME3", 22, 30, $extra);
        //急用電話番号３
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYTELNO3"] = knjCreateTextBox($objForm, $row["EMERGENCYTELNO3"], "EMERGENCYTELNO3", 16, 14, $extra);

        /**********/
        /* ボタン */
        /**********/
        //コピーボタン
        $extra = "style=\"width:130px\"onclick=\"copy(1);\"" . $schreg_disabled;
        $arg["button"]["btn_copy1"] = knjCreateBtn($objForm, "btn_copy1", "保護者よりコピー", $extra);
        //コピーボタン
        if ($model->Properties["useGuardian2"] == '1') {
            $extra = "style=\"width:130px\"onclick=\"copyHidden(1);\"" . $schreg_disabled;
            $arg["button"]["btn_copy3"] = knjCreateBtn($objForm, "btn_copy3", "保護者２よりコピー", $extra);
        }
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"" . $schreg_disabled;
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追加", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"" . $schreg_disabled;
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更新", $extra);
        //更新後前の生徒へボタン
        if ($model->infoDiv != "2" && $model->infoDiv != "3") {
            $extra = "" . $schreg_disabled;
            $arg["button"]["btn_up_next"]    = str_replace("name", "{$schreg_disabled} name", View::updateNext($model, $objForm, 'btn_update'));
        }
        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"" . $schreg_disabled;
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"" . $schreg_disabled;
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"" . $schreg_disabled;
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $row["UPDATED"]);
        knjCreateHidden($objForm, "GUARD_UPDATED", $row["GUARD_UPDATED"]);
        knjCreateHidden($objForm, "REQUESTROOT", REQUESTROOT);

/********************************************************************************/
/********************************************************************************/
/*******        *****************************************************************/
/******* 保護者 *****************************************************************/
/*******        *****************************************************************/
/********************************************************************************/
/********************************************************************************/
        //有効期間開始日付
        $arg["data"]["GUARD_ISSUEDATE"] = str_replace("input ", "input {$guard_disabled} ", View::popUpCalendar($objForm, "GUARD_ISSUEDATE", str_replace("-","/",$row["GUARD_ISSUEDATE"]), ""));
        //有効期間開始日付
        $arg["data"]["GUARD_EXPIREDATE"] = str_replace("input ", "input {$guard_disabled} ", View::popUpCalendar($objForm, "GUARD_EXPIREDATE", str_replace("-","/",$row["GUARD_EXPIREDATE"]), ""));
        //誕生日
        $arg["data"]["GUARD_BIRTHDAY"] = str_replace("input ", "input {$guard_disabled} ", View::popUpCalendar($objForm, "GUARD_BIRTHDAY",str_replace("-","/",$row["GUARD_BIRTHDAY"]), ""));
        //郵便番号
        $arg["data"]["GUARD_ZIPCD"] = str_replace("input ", "input {$guard_disabled} ", View::popUpZipCode($objForm, "GUARD_ZIPCD", $row["GUARD_ZIPCD"],"GUARD_ADDR1"));

        /********************/
        /* テキストボックス */
        /********************/
        //保護者氏名
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_NAME"]       = knjCreateTextBox($objForm, $row["GUARD_NAME"],       "GUARD_NAME", 40, 60, $extra);
        //保護者かな
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_KANA"]       = knjCreateTextBox($objForm, $row["GUARD_KANA"],       "GUARD_KANA", 40, 120, $extra);
        //保護者氏名
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_REAL_NAME"]  = knjCreateTextBox($objForm, $row["GUARD_REAL_NAME"], "GUARD_REAL_NAME", 40, 60, $extra);
        //保護者かな
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_REAL_KANA"]  = knjCreateTextBox($objForm, $row["GUARD_REAL_KANA"], "GUARD_REAL_KANA", 40, 120, $extra);
        //住所
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_ADDR1"]      = knjCreateTextBox($objForm, $row["GUARD_ADDR1"],      "GUARD_ADDR1", 50, 90, $extra);
        //方書き(アパート名等)
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_ADDR2"]      = knjCreateTextBox($objForm, $row["GUARD_ADDR2"],      "GUARD_ADDR2", 50, 90, $extra);
        //方書きを住所1とする
        $extra = $row["GUARD_ADDR_FLG"] == "1" ? " checked " : "";
        $arg["data"]["GUARD_ADDR_FLG"] = knjCreateCheckBox($objForm, "GUARD_ADDR_FLG", "1", $extra.$guard_disabled);
        //電話番号
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_TELNO"]      = knjCreateTextBox($objForm, $row["GUARD_TELNO"],      "GUARD_TELNO", 16, 14, $extra);
        //電話番号２
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_TELNO2"]     = knjCreateTextBox($objForm, $row["GUARD_TELNO2"],     "GUARD_TELNO2", 16, 14, $extra);
        //Fax番号
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_FAXNO"]      = knjCreateTextBox($objForm, $row["GUARD_FAXNO"],      "GUARD_FAXNO", 16, 14, $extra);
        //E-mail
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_E_MAIL"]     = knjCreateTextBox($objForm, $row["GUARD_E_MAIL"],     "GUARD_E_MAIL", 25, 20, $extra);
        //勤務先
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_WORK_NAME"]  = knjCreateTextBox($objForm, $row["GUARD_WORK_NAME"],  "GUARD_WORK_NAME", 40, 60, $extra);
        //勤務先電話番号
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_WORK_TELNO"] = knjCreateTextBox($objForm, $row["GUARD_WORK_TELNO"], "GUARD_WORK_TELNO", 16, 14, $extra);

        /******************/
        /* コンボボックス */
        /******************/
        //名称マスタよりコンボボックスのデータを取得
        $opt = array();
        $opt["Z002"][] = array("label" => "","value" => "0");
        $opt["H201"][] = array("label" => "","value" => "00");
        $opt["H202"][] = array("label" => "","value" => "00");
        $query = knja110_2bQuery::get_name_mst();
        $result = $db->query($query);
        while($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[$row3["NAMECD1"]][] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                                             "value" => $row3["NAMECD2"]);
        }
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_SEX"] = knjCreateCombo($objForm, "GUARD_SEX", $row["GUARD_SEX"], $opt["Z002"], $extra, 1);
        $extra = "" . $guard_disabled;
        $arg["data"]["RELATIONSHIP"] = knjCreateCombo($objForm, "RELATIONSHIP", $row["RELATIONSHIP"], $opt["H201"], $extra, 1);
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_JOBCD"] = knjCreateCombo($objForm, "GUARD_JOBCD", $row["GUARD_JOBCD"], $opt["H202"], $extra, 1);

        /**********/
        /* ボタン */
        /**********/
        //コピーボタン
        $extra = "style=\"width:120px\"onclick=\"copy('');\"" . $guard_disabled;
        $arg["button"]["btn_copy2"]    = knjCreateBtn($objForm, "btn_copy2", "生徒よりコピー", $extra);
        //コピーボタン
        if ($model->Properties["useGuardian2"] == '1') {
            $btn_copy4_name = ($model->infoDiv == "3") ? "保護者よりコピー" : "保護者２よりコピー";
            $extra = "style=\"width:130px\"onclick=\"copyHidden('');\"" . $guard_disabled;
            $arg["button"]["btn_copy4"]    = knjCreateBtn($objForm, "btn_copy4", $btn_copy4_name, $extra);
        }
        //保護者基礎データの更新
//        $link = REQUESTROOT."/A/KNJA110_2B/knja110_2bindex.php?cmd=guardian_hist_dat_kousin&SCHREGNO=".$model->schregno;
//        $extra = "onclick=\"Page_jumper('{$link}');\"" . $guard_disabled;
//        $arg["button"]["GUARDIAN_HIST_DAT_KOUSIN"] = knjCreateBtn($objForm, "GUARDIAN_HIST_DAT_KOUSIN", "保護者基礎データの更新", $extra);
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add2');\"" . $guard_disabled;
        $arg["button"]["btn_add2"]     = knjCreateBtn($objForm, "btn_add2", "追加", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update2');\"" . $guard_disabled;
        $arg["button"]["btn_update2"]  = knjCreateBtn($objForm, "btn_update2", "更新", $extra);
        //更新後前の生徒へボタン
        if ($model->infoDiv == "2" || $model->infoDiv == "3") {
            $extra = "" . $guard_disabled;
            $arg["button"]["btn_up_next2"] = str_replace("name", "{$guard_disabled} name", View::updateNext($model, $objForm, 'btn_update2'));
        }
        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete2');\"" . $guard_disabled;
        $arg["button"]["btn_del2"]     = knjCreateBtn($objForm, "btn_del2", "削除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"" . $guard_disabled;
        $arg["button"]["btn_reset2"]   = knjCreateBtn($objForm, "btn_reset2", "取消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"" . $guard_disabled;
        $arg["button"]["btn_end2"]     = knjCreateBtn($objForm, "btn_end2", "終了", $extra);

        $result->free();
        Query::dbCheckIn($db);
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.top_frame.location.href='knja110_2bindex.php?cmd=list';";
        }

        View::toHTML($model, "knja110_2bForm2.html", $arg);
    }
}
?>
