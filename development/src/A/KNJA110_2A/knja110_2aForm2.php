<?php

require_once('for_php7.php');

class knja110_2aForm2
{
    public function main(&$model)
    {
        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knja110_2aindex.php", "", "edit");
        $arg["reload"] = "";
        $db = Query::dbCheckOut();

        //更新等使用不可
        $sendUpdDisabled = "";
        if ($model->sendUnUpdate) {
            $sendUpdDisabled = " disabled ";
        }

        //保護者履歴入力ボタン
        $link = REQUESTROOT."/A/KNJA110_2A/knja110_2aindex.php?cmd=rireki&SCHREGNO=".$model->schregno;
        $extra = "onclick=\"Page_jumper('$link', 1);\"";
        $arg["button"]["btn_rireki"] = knjCreateBtn($objForm, "btn_rireki", "保護者履歴修正", $extra.$sendUpdDisabled);

        //保証人履歴入力ボタン
        $link = REQUESTROOT."/A/KNJA110_2A/knja110_2aindex.php?cmd=rireki2&SCHREGNO=".$model->schregno;
        $extra = "onclick=\"Page_jumper('$link', 2);\"";
        $arg["button"]["btn_rireki2"] = knjCreateBtn($objForm, "btn_rireki2", "保証人履歴修正", $extra.$sendUpdDisabled);

        //生徒情報の取得        (SCHREG_ADDRESS_DAT)
        $row1 = $db->getRow(knja110_2aQuery::getRowAddress($model->schregno, $model->issuedate), DB_FETCHMODE_ASSOC);
        //保護者情報の取得      (GUARDIAN_DAT、GUARDIAN_ADDRESS_DAT)
        $row2 = $db->getRow(knja110_2aQuery::getGuardianAddr($model, $model->schregno, $model->guard_issuedate), DB_FETCHMODE_ASSOC);
        //緊急連絡先情報の取得  (SCHREG_BASE_MST)
        $row3 = $db->getRow(knja110_2aQuery::getEmergencyInfo($model->schregno), DB_FETCHMODE_ASSOC);
        //保証人情報の取得      (GUARDIAN_DAT、GUARANTOR_ADDRESS_DAT)
        $row4 = $db->getRow(knja110_2aQuery::getGuarantorAddr($model, $model->schregno, $model->guarantor_issuedate), DB_FETCHMODE_ASSOC);
        //その他情報の取得      (SCHREG_SEND_ADDRESS_DAT)
        $row5 = $db->getRow(knja110_2aQuery::getSendAddr($model, $model->schregno, $model->send_div), DB_FETCHMODE_ASSOC);
        //家族情報の取得      (SCHREG_RELA_DAT)
        $row6 = $db->getRow(knja110_2aQuery::getRelaDat($model, $model->schregno, $model->rela_no), DB_FETCHMODE_ASSOC);
        //家族情報の取得      (SCHREG_BASE_MST)
        if ($model->cmd == "kakutei") {
            $row6 = $db->getRow(knja110_2aQuery::getSchDataKakutei($model), DB_FETCHMODE_ASSOC);
        }
        //備忘録
        $rowDetail009 = $db->getRow(knja110_2aQuery::getDetail009R2($model->schregno), DB_FETCHMODE_ASSOC);

        $copyRow = array(); //コピーボタン:Hiddenで保持用
        //保護者情報の取得      (GUARDIAN_DAT、GUARDIAN_ADDRESS_DAT)
        $copyRow = $db->getRow(knja110_2aQuery::getGuardianAddr($model, $model->schregno, "", "copy"), DB_FETCHMODE_ASSOC);

        knjCreateHidden($objForm, "COPY_GUARD_ISSUEDATE", str_replace("-", "/", $copyRow["GUARD_ISSUEDATE"]));
        knjCreateHidden($objForm, "COPY_GUARD_EXPIREDATE", str_replace("-", "/", $copyRow["GUARD_EXPIREDATE"]));
        knjCreateHidden($objForm, "COPY_GUARD_ZIPCD", $copyRow["GUARD_ZIPCD"]);
        knjCreateHidden($objForm, "COPY_GUARD_ADDR1", $copyRow["GUARD_ADDR1"]);
        knjCreateHidden($objForm, "COPY_GUARD_ADDR2", $copyRow["GUARD_ADDR2"]);
        knjCreateHidden($objForm, "COPY_GUARD_TELNO", $copyRow["GUARD_TELNO"]);
        knjCreateHidden($objForm, "COPY_GUARD_TELNO2", $copyRow["GUARD_TELNO2"]);
        knjCreateHidden($objForm, "COPY_GUARD_FAXNO", $copyRow["GUARD_FAXNO"]);

        $copyRow2 = array(); //コピーボタン:Hiddenで保持用
        //保護者情報２の取得      (GUARDIAN2_DAT、GUARDIAN2_ADDRESS_DAT)
        $copyRow2 = $db->getRow(knja110_2aQuery::getGuardianAddr($model, $model->schregno, "", "copy2"), DB_FETCHMODE_ASSOC);

        knjCreateHidden($objForm, "COPY2_GUARD_ISSUEDATE", str_replace("-", "/", $copyRow2["GUARD_ISSUEDATE"]));
        knjCreateHidden($objForm, "COPY2_GUARD_EXPIREDATE", str_replace("-", "/", $copyRow2["GUARD_EXPIREDATE"]));
        knjCreateHidden($objForm, "COPY2_GUARD_ZIPCD", $copyRow2["GUARD_ZIPCD"]);
        knjCreateHidden($objForm, "COPY2_GUARD_ADDR1", $copyRow2["GUARD_ADDR1"]);
        knjCreateHidden($objForm, "COPY2_GUARD_ADDR2", $copyRow2["GUARD_ADDR2"]);
        knjCreateHidden($objForm, "COPY2_GUARD_TELNO", $copyRow2["GUARD_TELNO"]);
        knjCreateHidden($objForm, "COPY2_GUARD_TELNO2", $copyRow2["GUARD_TELNO2"]);
        knjCreateHidden($objForm, "COPY2_GUARD_FAXNO", $copyRow2["GUARD_FAXNO"]);

        if ($model->Properties["useGuardian2"] == '1') {
            $arg["useGuardian2"] = "ON";
        } else {
            $arg["useGuardian2"] = "";
        }

        $model->form2 = array_merge((array)$row1, (array)$row2, (array)$row3, (array)$row4, (array)$row5, (array)$row6, (array)$rowDetail009);

        if (!$model->isWarning()) {
            $row  = array_merge((array)$row1, (array)$row2, (array)$row3, (array)$row4, (array)$row5, (array)$row6, (array)$rowDetail009);
        } else {
            $row =& $model->field;
        }

        knjCreateHidden($objForm, "CHECK_GUARD_ISSUEDATE", $model->form2["GUARD_ISSUEDATE"]);
        knjCreateHidden($objForm, "CHECK_GUARD_EXPIREDATE", $model->form2["GUARD_EXPIREDATE"]);
        knjCreateHidden($objForm, "CHECK_RELATIONSHIP", $model->form2["RELATIONSHIP"]);
        knjCreateHidden($objForm, "CHECK_GUARD_NAME", $model->form2["GUARD_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARD_KANA", $model->form2["GUARD_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARD_REAL_NAME", $model->form2["GUARD_REAL_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARD_REAL_KANA", $model->form2["GUARD_REAL_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARD_SEX", $model->form2["GUARD_SEX"]);
        knjCreateHidden($objForm, "CHECK_GUARD_BIRTHDAY", $model->form2["GUARD_BIRTHDAY"]);

        knjCreateHidden($objForm, "E_APPDATE");
        knjCreateHidden($objForm, "RELATIONSHIP_FLG");
        knjCreateHidden($objForm, "GUARD_NAME_FLG");
        knjCreateHidden($objForm, "GUARD_KANA_FLG");
        knjCreateHidden($objForm, "GUARD_REAL_NAME_FLG");
        knjCreateHidden($objForm, "GUARD_REAL_KANA_FLG");
        knjCreateHidden($objForm, "GUARD_SEX_FLG");
        knjCreateHidden($objForm, "GUARD_BIRTHDAY_FLG");

        //保証人情報格納（履歴用）
        knjCreateHidden($objForm, "CHECK_GUARANTOR_ISSUEDATE", $model->form2["GUARANTOR_ISSUEDATE"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_EXPIREDATE", $model->form2["GUARANTOR_EXPIREDATE"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_RELATIONSHIP", $model->form2["GUARANTOR_RELATIONSHIP"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_NAME", $model->form2["GUARANTOR_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_KANA", $model->form2["GUARANTOR_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_REAL_NAME", $model->form2["GUARANTOR_REAL_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_REAL_KANA", $model->form2["GUARANTOR_REAL_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_SEX", $model->form2["GUARANTOR_SEX"]);

        knjCreateHidden($objForm, "GUARANTOR_RELATIONSHIP_FLG");
        knjCreateHidden($objForm, "GUARANTOR_NAME_FLG");
        knjCreateHidden($objForm, "GUARANTOR_KANA_FLG");
        knjCreateHidden($objForm, "GUARANTOR_REAL_NAME_FLG");
        knjCreateHidden($objForm, "GUARANTOR_REAL_KANA_FLG");
        knjCreateHidden($objForm, "GUARANTOR_SEX_FLG");

        if (!$model->issuedate) {
            $model->issuedate = str_replace("-", "/", $row["ISSUEDATE"]);
        }

        if (!$model->guard_issuedate) {
            $model->guard_issuedate = str_replace("-", "/", $row["GUARD_ISSUEDATE"]);
        }

        if (!$model->guarantor_issuedate) {
            $model->guarantor_issuedate = str_replace("-", "/", $row["GUARANTOR_ISSUEDATE"]);
        }

        if (!$model->send_div) {
            $model->send_div = $row["SEND_DIV"];
        }

        if (!$model->rela_no) {
            $model->rela_no = $row["RELA_NO"];
        }

        $schreg_disabled = $guard_disabled = "";
        $arg["INFO_DIV2_TITLE"] = "保護者情報";
        if ($model->infoDiv == "2" || $model->infoDiv == "3") {
            $arg["infoDiv2"]    = "ON";
            $arg["show_btn_R"]  = "ON";
            $schreg_disabled    = "disabled";
            $arg["INFO_DIV2_COLOR"] = "style=\"color:yellow;\"";
            if ($model->infoDiv == "3") {
                $arg["INFO_DIV2_TITLE"]  = "保護者情報２";
            }
        } elseif ($model->infoDiv == "4") {
            $arg["infoDiv4"]    = "ON";
            $arg["show_btn_R"]  = "ON";
            $schreg_disabled    = "disabled";
            $arg["INFO_DIV4_COLOR"] = "style=\"color:yellow;\"";
        } elseif ($model->infoDiv == "5") {
            $arg["infoDiv5"]    = "ON";
            $arg["show_btn_R"]  = "ON";
            $schreg_disabled    = "disabled";
            $arg["INFO_DIV5_COLOR"] = "style=\"color:yellow;\"";
        } elseif ($model->infoDiv == "6") {
            $arg["infoDiv6"]    = "ON";
            $arg["show_btn_R"]  = "ON";
            $schreg_disabled    = "disabled";
            $arg["INFO_DIV6_COLOR"] = "style=\"color:yellow;\"";
        } else {
            $arg["infoDiv2"]    = "ON";
            $arg["show_btn_L"]  = "ON";
            $guard_disabled     = "disabled";
            $arg["INFO_DIV1_COLOR"] = "style=\"color:yellow;\"";
        }

        /******************************************************************************/
        /******************************************************************************/
        /*******      *****************************************************************/
        /******* 生徒 *****************************************************************/
        /*******      *****************************************************************/
        /******************************************************************************/
        /******************************************************************************/
        //有効期間開始日付
        $arg["data"]["ISSUEDATE"] = str_replace("input ", "input {$schreg_disabled} ", View::popUpCalendar($objForm, "ISSUEDATE", str_replace("-", "/", $row["ISSUEDATE"]), ""));
        //有効期間開始日付
        $arg["data"]["EXPIREDATE"] = str_replace("input ", "input {$schreg_disabled} ", View::popUpCalendar($objForm, "EXPIREDATE", str_replace("-", "/", $row["EXPIREDATE"]), ""));
        //郵便番号
        if ($model->Properties["search_zipcd_set_town_to_addr2"] == "1") {
            $arg["data"]["ZIPCD"] = str_replace("input ", "input {$schreg_disabled} ", View::popUpZipCode($objForm, "ZIPCD", $row["ZIPCD"], "ADDR1", 10, "ADDR2"));
        } else {
            $arg["data"]["ZIPCD"] = str_replace("input ", "input {$schreg_disabled} ", View::popUpZipCode($objForm, "ZIPCD", $row["ZIPCD"], "ADDR1"));
        }
        //地区コード
        $opt = array();
        $opt["A020"][] = array("label" => "","value" => "00");
        $result = $db->query(knja110_2aQuery::getNNMst());
        while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[$row3["NAMECD1"]][] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                                             "value" => $row3["NAMECD2"]);
        }
        $extra = "" . $schreg_disabled;
        $arg["data"]["AREACD"] = knjCreateCombo($objForm, "AREACD", $row["AREACD"], $opt["A020"], $extra, 1);

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
        //電話番号メモ
        $extra = "" . $schreg_disabled;
        $arg["data"]["TELNO_MEMO"] = knjCreateTextBox($objForm, $row["TELNO_MEMO"], "TELNO_MEMO", 16, 24, $extra);
        //優先順位
        $extra = "onblur=\"this.value=toInteger(this.value)\" " . $schreg_disabled;
        $arg["data"]["PRIORITY1"] = knjCreateTextBox($objForm, $row["PRIORITY1"], "PRIORITY1", 3, 3, $extra);
        //電話番号２
        $extra = "" . $schreg_disabled;
        $arg["data"]["TELNO2"] = knjCreateTextBox($objForm, $row["TELNO2"], "TELNO2", 16, 14, $extra);
        //電話番号メモ
        $extra = "" . $schreg_disabled;
        $arg["data"]["TELNO2_MEMO"] = knjCreateTextBox($objForm, $row["TELNO2_MEMO"], "TELNO2_MEMO", 16, 24, $extra);
        //優先順位
        $extra = "onblur=\"this.value=toInteger(this.value)\" " . $schreg_disabled;
        $arg["data"]["PRIORITY2"] = knjCreateTextBox($objForm, $row["PRIORITY2"], "PRIORITY2", 3, 3, $extra);
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
        //急用電話番号1_1
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYTELNO"] = knjCreateTextBox($objForm, $row["EMERGENCYTELNO"], "EMERGENCYTELNO", 16, 14, $extra);
        //急用電話番号メモ1_1
        $extra = "" . $schreg_disabled;
        $arg["data"]["E_TELNO_MEMO"] = knjCreateTextBox($objForm, $row["E_TELNO_MEMO"], "E_TELNO_MEMO", 16, 24, $extra);
        //優先順位
        $extra = "onblur=\"this.value=toInteger(this.value)\" " . $schreg_disabled;
        $arg["data"]["PRIORITY3"] = knjCreateTextBox($objForm, $row["PRIORITY3"], "PRIORITY3", 3, 3, $extra);
        //急用電話番号1_2
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYTELNO_2"] = knjCreateTextBox($objForm, $row["EMERGENCYTELNO_2"], "EMERGENCYTELNO_2", 16, 14, $extra);
        //急用電話番号メモ1_2
        $extra = "" . $schreg_disabled;
        $arg["data"]["E_TELNO_MEMO_2"] = knjCreateTextBox($objForm, $row["E_TELNO_MEMO_2"], "E_TELNO_MEMO_2", 16, 24, $extra);
        //優先順位
        $extra = "onblur=\"this.value=toInteger(this.value)\" " . $schreg_disabled;
        $arg["data"]["PRIORITY4"] = knjCreateTextBox($objForm, $row["PRIORITY4"], "PRIORITY4", 3, 3, $extra);

        //急用連絡先2
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYCALL2"] = knjCreateTextBox($objForm, $row["EMERGENCYCALL2"], "EMERGENCYCALL2", 40, 60, $extra);
        //急用連絡氏名2
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYNAME2"] = knjCreateTextBox($objForm, $row["EMERGENCYNAME2"], "EMERGENCYNAME2", 40, 60, $extra);
        //急用連絡続柄名2
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYRELA_NAME2"] = knjCreateTextBox($objForm, $row["EMERGENCYRELA_NAME2"], "EMERGENCYRELA_NAME2", 22, 30, $extra);
        //急用電話番号2_1
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYTELNO2"] = knjCreateTextBox($objForm, $row["EMERGENCYTELNO2"], "EMERGENCYTELNO2", 16, 14, $extra);
        //急用電話番号メモ2_1
        $extra = "" . $schreg_disabled;
        $arg["data"]["E_TELNO_MEMO2"] = knjCreateTextBox($objForm, $row["E_TELNO_MEMO2"], "E_TELNO_MEMO2", 16, 24, $extra);
        //優先順位
        $extra = "onblur=\"this.value=toInteger(this.value)\" " . $schreg_disabled;
        $arg["data"]["PRIORITY5"] = knjCreateTextBox($objForm, $row["PRIORITY5"], "PRIORITY5", 3, 3, $extra);
        //急用電話番号2_2
        $extra = "" . $schreg_disabled;
        $arg["data"]["EMERGENCYTELNO2_2"] = knjCreateTextBox($objForm, $row["EMERGENCYTELNO2_2"], "EMERGENCYTELNO2_2", 16, 14, $extra);
        //急用電話番号メモ2_2
        $extra = "" . $schreg_disabled;
        $arg["data"]["E_TELNO_MEMO2_2"] = knjCreateTextBox($objForm, $row["E_TELNO_MEMO2_2"], "E_TELNO_MEMO2_2", 16, 24, $extra);
        //優先順位
        $extra = "onblur=\"this.value=toInteger(this.value)\" " . $schreg_disabled;
        $arg["data"]["PRIORITY6"] = knjCreateTextBox($objForm, $row["PRIORITY6"], "PRIORITY6", 3, 3, $extra);

        /**********/
        /* ボタン */
        /**********/
        //コピーボタン
        $extra = "style=\"width:130px\"onclick=\"copy(1);\"" . $schreg_disabled;
        $arg["button"]["btn_copy1"] = knjCreateBtn($objForm, "btn_copy1", "保護者よりコピー", $extra.$sendUpdDisabled);
        //コピーボタン
        if ($model->Properties["useGuardian2"] == '1') {
            $extra = "style=\"width:130px\"onclick=\"copyHidden(1);\"" . $schreg_disabled;
            $arg["button"]["btn_copy3"] = knjCreateBtn($objForm, "btn_copy3", "保護者２よりコピー", $extra.$sendUpdDisabled);
        }
        //MAX開始日付取得
        $maxAddr = $db->getRow(knja110_2aQuery::getRowAddress($model->schregno, ""), DB_FETCHMODE_ASSOC);
        //コピーボタン
        $extra = "style=\"width:130px\" onclick=\"return btn_submit('copyBros');\"" . $schreg_disabled;
        if ($maxAddr["ISSUEDATE"] != str_replace('/', '-', $model->issuedate)) {
            $extra = " disabled";
        }
        $arg["button"]["btn_copybros"] = knjCreateBtn($objForm, "btn_copybros", "兄弟姉妹へコピー", $extra.$sendUpdDisabled);
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"" . $schreg_disabled;
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追加", $extra.$sendUpdDisabled);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"" . $schreg_disabled;
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更新", $extra.$sendUpdDisabled);
        //更新後前の生徒へボタン
        if ($model->infoDiv != "2" && $model->infoDiv != "3") {
            $extra = "" . $schreg_disabled;
            $arg["button"]["btn_up_next"]    = str_replace("name", $schreg_disabled.$sendUpdDisabled." name", View::updateNext($model, $objForm, 'btn_update'));
        }
        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"" . $schreg_disabled;
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削除", $extra.$sendUpdDisabled);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"" . $schreg_disabled;
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取消", $extra.$sendUpdDisabled);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"" . $schreg_disabled;
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $row["UPDATED"]);
        knjCreateHidden($objForm, "GUARD_UPDATED", $row["GUARD_UPDATED"]);
        knjCreateHidden($objForm, "GUARANTOR_UPDATED", $row["GUARANTOR_UPDATED"]);
        knjCreateHidden($objForm, "SEND_UPDATED", $row["SEND_UPDATED"]);
        knjCreateHidden($objForm, "RELA_UPDATED", $row["RELA_UPDATED"]);
        knjCreateHidden($objForm, "REQUESTROOT", REQUESTROOT);
        knjCreateHidden($objForm, "SEND_UN_UPDATE", $model->sendUnUpdate);

        //コピー対象の兄弟姉妹取得
        $checkBros = $db->getCol(knja110_2aQuery::getRegdBrother($model));
        knjCreateHidden($objForm, "CHECK_BROS", get_count($checkBros));

        /********************************************************************************/
        /********************************************************************************/
        /*******        *****************************************************************/
        /******* 保護者 *****************************************************************/
        /*******        *****************************************************************/
        /********************************************************************************/
        /********************************************************************************/
        //有効期間開始日付
        $arg["data"]["GUARD_ISSUEDATE"] = str_replace("input ", "input {$guard_disabled} ", View::popUpCalendar($objForm, "GUARD_ISSUEDATE", str_replace("-", "/", $row["GUARD_ISSUEDATE"]), ""));
        //有効期間開始日付
        $arg["data"]["GUARD_EXPIREDATE"] = str_replace("input ", "input {$guard_disabled} ", View::popUpCalendar($objForm, "GUARD_EXPIREDATE", str_replace("-", "/", $row["GUARD_EXPIREDATE"]), ""));
        //誕生日
        $arg["data"]["GUARD_BIRTHDAY"] = str_replace("input ", "input {$guard_disabled} ", View::popUpCalendar($objForm, "GUARD_BIRTHDAY", str_replace("-", "/", $row["GUARD_BIRTHDAY"]), ""));
        //郵便番号
        if ($model->Properties["search_zipcd_set_town_to_addr2"] == "1") {
            $arg["data"]["GUARD_ZIPCD"] = str_replace("input ", "input {$guard_disabled} ", View::popUpZipCode($objForm, "GUARD_ZIPCD", $row["GUARD_ZIPCD"], "GUARD_ADDR1", 10, "GUARD_ADDR2"));
        } else {
            $arg["data"]["GUARD_ZIPCD"] = str_replace("input ", "input {$guard_disabled} ", View::popUpZipCode($objForm, "GUARD_ZIPCD", $row["GUARD_ZIPCD"], "GUARD_ADDR1"));
        }

        /********************/
        /* テキストボックス */
        /********************/
        //保護者氏名
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_NAME"]       = knjCreateTextBox($objForm, $row["GUARD_NAME"], "GUARD_NAME", 40, 60, $extra);
        //保護者かな
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_KANA"]       = knjCreateTextBox($objForm, $row["GUARD_KANA"], "GUARD_KANA", 40, 120, $extra);
        //保護者氏名
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_REAL_NAME"]  = knjCreateTextBox($objForm, $row["GUARD_REAL_NAME"], "GUARD_REAL_NAME", 40, 60, $extra);
        //保護者かな
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_REAL_KANA"]  = knjCreateTextBox($objForm, $row["GUARD_REAL_KANA"], "GUARD_REAL_KANA", 40, 120, $extra);
        //住所
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_ADDR1"]      = knjCreateTextBox($objForm, $row["GUARD_ADDR1"], "GUARD_ADDR1", 50, 90, $extra);
        //方書き(アパート名等)
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_ADDR2"]      = knjCreateTextBox($objForm, $row["GUARD_ADDR2"], "GUARD_ADDR2", 50, 90, $extra);
        //方書きを住所1とする
        $extra = $row["GUARD_ADDR_FLG"] == "1" ? " checked " : "";
        $arg["data"]["GUARD_ADDR_FLG"] = knjCreateCheckBox($objForm, "GUARD_ADDR_FLG", "1", $extra.$guard_disabled);
        //電話番号
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_TELNO"]      = knjCreateTextBox($objForm, $row["GUARD_TELNO"], "GUARD_TELNO", 16, 14, $extra);
        //電話番号２
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_TELNO2"]     = knjCreateTextBox($objForm, $row["GUARD_TELNO2"], "GUARD_TELNO2", 16, 14, $extra);
        //Fax番号
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_FAXNO"]      = knjCreateTextBox($objForm, $row["GUARD_FAXNO"], "GUARD_FAXNO", 16, 14, $extra);
        //E-mail
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_E_MAIL"]     = knjCreateTextBox($objForm, $row["GUARD_E_MAIL"], "GUARD_E_MAIL", 25, 20, $extra);
        //勤務先
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_WORK_NAME"]  = knjCreateTextBox($objForm, $row["GUARD_WORK_NAME"], "GUARD_WORK_NAME", 40, 60, $extra);
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
        $query = knja110_2aQuery::getNNMst();
        $result = $db->query($query);
        while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[$row3["NAMECD1"]][] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                                             "value" => $row3["NAMECD2"]);
        }
        //性別
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_SEX"] = knjCreateCombo($objForm, "GUARD_SEX", $row["GUARD_SEX"], $opt["Z002"], $extra, 1);
        //続柄
        $extra = "" . $guard_disabled;
        $arg["data"]["RELATIONSHIP"] = knjCreateCombo($objForm, "RELATIONSHIP", $row["RELATIONSHIP"], $opt["H201"], $extra, 1);
        //職種コード
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARD_JOBCD"] = knjCreateCombo($objForm, "GUARD_JOBCD", $row["GUARD_JOBCD"], $opt["H202"], $extra, 1);

        /********************************************************************************/
        /********************************************************************************/
        /*******        *****************************************************************/
        /******* 保証人 *****************************************************************/
        /*******        *****************************************************************/
        /********************************************************************************/
        /********************************************************************************/
        //有効期間開始日付
        $arg["data"]["GUARANTOR_ISSUEDATE"] = str_replace("input ", "input {$guard_disabled} ", View::popUpCalendar($objForm, "GUARANTOR_ISSUEDATE", str_replace("-", "/", $row["GUARANTOR_ISSUEDATE"]), ""));
        //有効期間開始日付
        $arg["data"]["GUARANTOR_EXPIREDATE"] = str_replace("input ", "input {$guard_disabled} ", View::popUpCalendar($objForm, "GUARANTOR_EXPIREDATE", str_replace("-", "/", $row["GUARANTOR_EXPIREDATE"]), ""));
        //郵便番号
        if ($model->Properties["search_zipcd_set_town_to_addr2"] == "1") {
            $arg["data"]["GUARANTOR_ZIPCD"] = str_replace("input ", "input {$guard_disabled} ", View::popUpZipCode($objForm, "GUARANTOR_ZIPCD", $row["GUARANTOR_ZIPCD"], "GUARANTOR_ADDR1", 10, "GUARANTOR_ADDR2"));
        } else {
            $arg["data"]["GUARANTOR_ZIPCD"] = str_replace("input ", "input {$guard_disabled} ", View::popUpZipCode($objForm, "GUARANTOR_ZIPCD", $row["GUARANTOR_ZIPCD"], "GUARANTOR_ADDR1"));
        }

        /********************/
        /* テキストボックス */
        /********************/
        //氏名
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARANTOR_NAME"]      = knjCreateTextBox($objForm, $row["GUARANTOR_NAME"], "GUARANTOR_NAME", 40, 60, $extra);
        //氏名かな
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARANTOR_KANA"]      = knjCreateTextBox($objForm, $row["GUARANTOR_KANA"], "GUARANTOR_KANA", 40, 120, $extra);
        //戸籍氏名
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARANTOR_REAL_NAME"] = knjCreateTextBox($objForm, $row["GUARANTOR_REAL_NAME"], "GUARANTOR_REAL_NAME", 40, 60, $extra);
        //戸籍氏名かな
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARANTOR_REAL_KANA"] = knjCreateTextBox($objForm, $row["GUARANTOR_REAL_KANA"], "GUARANTOR_REAL_KANA", 40, 120, $extra);
        //住所
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARANTOR_ADDR1"]     = knjCreateTextBox($objForm, $row["GUARANTOR_ADDR1"], "GUARANTOR_ADDR1", 50, 90, $extra);
        //方書き(アパート名等)
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARANTOR_ADDR2"]     = knjCreateTextBox($objForm, $row["GUARANTOR_ADDR2"], "GUARANTOR_ADDR2", 50, 90, $extra);
        //方書きを住所1とする
        $extra  = $row["GUARANTOR_ADDR_FLG"] == "1" ? " checked " : "";
        $arg["data"]["GUARANTOR_ADDR_FLG"]  = knjCreateCheckBox($objForm, "GUARANTOR_ADDR_FLG", "1", $extra.$guard_disabled);
        //電話番号
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARANTOR_TELNO"]     = knjCreateTextBox($objForm, $row["GUARANTOR_TELNO"], "GUARANTOR_TELNO", 16, 14, $extra);
        //保証人等の勤務先タイトル名称
        $arg["data"]["PUBLIC_OFFICE_TITLE"] = $model->publicOffice_TitleName;
        $extra = "" . $guard_disabled;
        $arg["data"]["PUBLIC_OFFICE"]       = knjCreateTextBox($objForm, $row["PUBLIC_OFFICE"], "PUBLIC_OFFICE", 20, 20, $extra);

        /******************/
        /* コンボボックス */
        /******************/
        //名称マスタよりコンボボックスのデータを取得
        $opt = array();
        $opt["Z002"][] = array("label" => "","value" => "0");
        $opt["H201"][] = array("label" => "","value" => "00");
        $opt["H202"][] = array("label" => "","value" => "00");
        $query = knja110_2aQuery::getNNMst();
        $result = $db->query($query);
        while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[$row3["NAMECD1"]][] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                                             "value" => $row3["NAMECD2"]);
        }
        //性別
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARANTOR_SEX"] = knjCreateCombo($objForm, "GUARANTOR_SEX", $row["GUARANTOR_SEX"], $opt["Z002"], $extra, 1);
        //生徒との関係
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARANTOR_RELATIONSHIP"] = knjCreateCombo($objForm, "GUARANTOR_RELATIONSHIP", $row["GUARANTOR_RELATIONSHIP"], $opt["H201"], $extra, 1);
        //職種コード
        $extra = "" . $guard_disabled;
        $arg["data"]["GUARANTOR_JOBCD"] = knjCreateCombo($objForm, "GUARANTOR_JOBCD", $row["GUARANTOR_JOBCD"], $opt["H202"], $extra, 1);

        /********************************************************************************/
        /********************************************************************************/
        /*******        *****************************************************************/
        /******* その他 *****************************************************************/
        /*******        *****************************************************************/
        /********************************************************************************/
        /********************************************************************************/
        //郵便番号
        if ($model->Properties["search_zipcd_set_town_to_addr2"] == "1") {
            $arg["data"]["SEND_ZIPCD"] = str_replace("input ", "input {$guard_disabled} ", View::popUpZipCode($objForm, "SEND_ZIPCD", $row["SEND_ZIPCD"], "SEND_ADDR1", 10, "SEND_ADDR2"));
        } else {
            $arg["data"]["SEND_ZIPCD"] = str_replace("input ", "input {$guard_disabled} ", View::popUpZipCode($objForm, "SEND_ZIPCD", $row["SEND_ZIPCD"], "SEND_ADDR1"));
        }

        /********************/
        /* テキストボックス */
        /********************/
        //氏名
        $extra = "" . $guard_disabled;
        $arg["data"]["SEND_NAME"]           = knjCreateTextBox($objForm, $row["SEND_NAME"], "SEND_NAME", 40, 60, $extra);
        //氏名かな
        $extra = "" . $guard_disabled;
        $arg["data"]["SEND_KANA"]           = knjCreateTextBox($objForm, $row["SEND_KANA"], "SEND_KANA", 40, 120, $extra);
        //住所
        $extra = "" . $guard_disabled;
        $arg["data"]["SEND_ADDR1"]          = knjCreateTextBox($objForm, $row["SEND_ADDR1"], "SEND_ADDR1", 50, 90, $extra);
        //方書き(アパート名等)
        $extra = "" . $guard_disabled;
        $arg["data"]["SEND_ADDR2"]          = knjCreateTextBox($objForm, $row["SEND_ADDR2"], "SEND_ADDR2", 50, 90, $extra);
        //方書きを住所1とする
        $extra  = $row["SEND_ADDR_FLG"] == "1" ? " checked " : "";
        $arg["data"]["SEND_ADDR_FLG"]       = knjCreateCheckBox($objForm, "SEND_ADDR_FLG", "1", $extra.$guard_disabled);
        //電話番号１
        $extra = "" . $guard_disabled;
        $arg["data"]["SEND_TELNO"]          = knjCreateTextBox($objForm, $row["SEND_TELNO"], "SEND_TELNO", 16, 14, $extra);
        //電話番号２
        $extra = "" . $guard_disabled;
        $arg["data"]["SEND_TELNO2"]         = knjCreateTextBox($objForm, $row["SEND_TELNO2"], "SEND_TELNO2", 16, 14, $extra);
        //保証人等の勤務先タイトル名称
        $arg["data"]["SEND_PUBLIC_OFFICE_TITLE"] = $model->publicOffice_TitleName;
        $extra = "" . $guard_disabled;
        $arg["data"]["SEND_PUBLIC_OFFICE"]  = knjCreateTextBox($objForm, $row["SEND_PUBLIC_OFFICE"], "SEND_PUBLIC_OFFICE", 20, 20, $extra);

        /******************/
        /* コンボボックス */
        /******************/
        //名称マスタよりコンボボックスのデータを取得
        $opt = array();
        $opt["Z002"][] = array("label" => "","value" => "0");
        $opt["H201"][] = array("label" => "","value" => "00");
        $opt["H202"][] = array("label" => "","value" => "00");
        $opt["A020"][] = array("label" => "","value" => "00");
        $query = knja110_2aQuery::getNNMst();
        $result = $db->query($query);
        while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[$row3["NAMECD1"]][] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                                             "value" => $row3["NAMECD2"]);
        }
        //性別
        $extra = "" . $guard_disabled;
        $arg["data"]["SEND_SEX"] = knjCreateCombo($objForm, "SEND_SEX", $row["SEND_SEX"], $opt["Z002"], $extra, 1);
        //生徒との関係
        $extra = "" . $guard_disabled;
        $arg["data"]["SEND_RELATIONSHIP"] = knjCreateCombo($objForm, "SEND_RELATIONSHIP", $row["SEND_RELATIONSHIP"], $opt["H201"], $extra, 1);
        //職種コード
        $extra = "" . $guard_disabled;
        $arg["data"]["SEND_JOBCD"] = knjCreateCombo($objForm, "SEND_JOBCD", $row["SEND_JOBCD"], $opt["H202"], $extra, 1);
        //地区コード
        $extra = "" . $guard_disabled;
        $arg["data"]["SEND_AREACD"] = knjCreateCombo($objForm, "SEND_AREACD", $row["SEND_AREACD"], $opt["A020"], $extra, 1);

        //区分
        $optDiv = array();
        $optDiv[] = array("label" => "","value" => "");
        $optDiv[] = array("label" => "その他",      "value" => "1");
        $optDiv[] = array("label" => "その他２",    "value" => "2");
        $optDiv[] = array("label" => "下宿保証人",  "value" => "3");
        $extra = "" . $guard_disabled;
        $arg["data"]["SEND_DIV"] = knjCreateCombo($objForm, "SEND_DIV", $row["SEND_DIV"], $optDiv, $extra, 1);

        /********************************************************************************/
        /********************************************************************************/
        /*******        *****************************************************************/
        /*******  家族  *****************************************************************/
        /*******        *****************************************************************/
        /********************************************************************************/
        /********************************************************************************/
        global $sess;

        //連番
        $arg["data"]["RELA_NO"]         = $row["RELA_NO"];
        knjCreateHidden($objForm, "RELA_NO", $row["RELA_NO"]);
        //生年月日
        $arg["data"]["RELA_BIRTHDAY"]   = str_replace("input ", "input {$guard_disabled} ", View::popUpCalendar($objForm, "RELA_BIRTHDAY", str_replace("-", "/", $row["RELA_BIRTHDAY"]), ""));

        /********************/
        /* テキストボックス */
        /********************/
        //氏名
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_NAME"]       = knjCreateTextBox($objForm, $row["RELA_NAME"], "RELA_NAME", 40, 40, $extra);
        //氏名かな
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_KANA"]       = knjCreateTextBox($objForm, $row["RELA_KANA"], "RELA_KANA", 40, 80, $extra);
        //職業又は学校
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_OCCUPATION"] = knjCreateTextBox($objForm, $row["RELA_OCCUPATION"], "RELA_OCCUPATION", 40, 40, $extra);
        //備考
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_REMARK"]     = knjCreateTextBox($objForm, $row["RELA_REMARK"], "RELA_REMARK", 40, 30, $extra);
        //兄弟姉妹学籍番号
        $extra = "onblur=\"this.value=toInteger(this.value)\" " . $guard_disabled;
        $arg["data"]["RELA_SCHREGNO"]   = knjCreateTextBox($objForm, $row["RELA_SCHREGNO"], "RELA_SCHREGNO", 10, 10, $extra);
        //兄弟姉妹検索ボタン
        $extra = "style=\"width:140px\"onclick=\"loadwindow('./knja110_2aSearchBrother.php?cmd=search&MODE=set&useGuardian2={$model->Properties["useGuardian2"]}&CD=$model->schregno&SCHREGNO='+document.forms[0]['RELA_SCHREGNO'].value+'&STUCD_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 600, 350)\"";
        $arg["data"]["BTN_STUCD2"] = knjCreateBtn($objForm, "BTN_STUCD2", "兄弟姉妹検索", $extra.$sendUpdDisabled);
        //確定ボタン
        $extra = "onclick=\"return btn_submit('kakutei');\"";
        $arg["button"]["kakutei"] = knjCreateBtn($objForm, "kakutei", "確定", $extra.$sendUpdDisabled);

        /******************/
        /* コンボボックス */
        /******************/
        //名称マスタよりコンボボックスのデータを取得
        $opt = array();
        $opt["Z002"][] = array("label" => "","value" => "0");
        $opt["H201"][] = array("label" => "","value" => "00");
        $opt["H200"][] = array("label" => "","value" => "00");
        $query = knja110_2aQuery::getNNMst();
        $result = $db->query($query);
        while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[$row3["NAMECD1"]][] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                                             "value" => $row3["NAMECD2"]);
        }
        //性別
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_SEX"] = knjCreateCombo($objForm, "RELA_SEX", $row["RELA_SEX"], $opt["Z002"], $extra, 1);
        //続柄
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_RELATIONSHIP"] = knjCreateCombo($objForm, "RELA_RELATIONSHIP", $row["RELA_RELATIONSHIP"], $opt["H201"], $extra, 1);
        //同居区分
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_REGIDENTIALCD"] = knjCreateCombo($objForm, "RELA_REGIDENTIALCD", $row["RELA_REGIDENTIALCD"], $opt["H200"], $extra, 1);
        //在卒区分
        $opt["FLG"][] = array('label' => "",     'value' => "");
        $opt["FLG"][] = array('label' => "在学", 'value' => "1");
        $opt["FLG"][] = array('label' => "卒業", 'value' => "2");
        $extra = "" . $guard_disabled;
        $arg["data"]["REGD_GRD_FLG"] = knjCreateCombo($objForm, "REGD_GRD_FLG", $row["REGD_GRD_FLG"], $opt["FLG"], $extra, 1);
        //学年
        $opt["GRADE"][] = array("label" => "", "value" => "");
        $query = knja110_2aQuery::getSchregRegdGdat($model);
        $result = $db->query($query);
        while ($rowG = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt["GRADE"][] = array("label" => $rowG["LABEL"],
                                    "value" => $rowG["VALUE"]);
        }
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_GRADE"] = knjCreateCombo($objForm, "RELA_GRADE", $row["RELA_GRADE"], $opt["GRADE"], $extra, 1);

        //備忘録
        $extra = "id=\"DETAIL_009_R2\" ";
        $arg["data"]["DETAIL_009_R2"] = knjCreateTextArea($objForm, "DETAIL_009_R2", "5", "40", "soft", $extra, $row["DETAIL_009_R2"]);
        knjCreateHidden($objForm, "DETAIL_009_R2_KETA", 40);
        knjCreateHidden($objForm, "DETAIL_009_R2_GYO", 5);
        KnjCreateHidden($objForm, "DETAIL_009_R2_STAT", "statusarea1");

        /**********/
        /* ボタン */
        /**********/
        //コピーボタン
        $extra = "style=\"width:120px\"onclick=\"copy('".$model->infoDiv."');\"" . $guard_disabled;
        $arg["button"]["btn_copy2"]    = knjCreateBtn($objForm, "btn_copy2", "生徒よりコピー", $extra.$sendUpdDisabled);
        //コピーボタン
        $btn_copy4_name = ($model->infoDiv == "2") ? "保護者２よりコピー" : "保護者よりコピー";
        $extra = "style=\"width:130px\"onclick=\"copyHidden('".$model->infoDiv."');\"" . $guard_disabled;
        $arg["button"]["btn_copy4"]    = knjCreateBtn($objForm, "btn_copy4", $btn_copy4_name, $extra.$sendUpdDisabled);
        //保護者基礎データの更新
//        $link = REQUESTROOT."/A/KNJA110_2A/knja110_2aindex.php?cmd=guardian_hist_dat_kousin&SCHREGNO=".$model->schregno;
//        $extra = "onclick=\"Page_jumper('{$link}');\"" . $guard_disabled;
//        $arg["button"]["GUARDIAN_HIST_DAT_KOUSIN"] = knjCreateBtn($objForm, "GUARDIAN_HIST_DAT_KOUSIN", "保護者基礎データの更新", $extra);
        //追加ボタン
        $add_cmd = ($model->infoDiv >= "4") ? "add".$model->infoDiv : "add2";
        $extra = "onclick=\"return btn_submit('".$add_cmd."');\"" . $guard_disabled;
        $arg["button"]["btn_add2"]     = knjCreateBtn($objForm, "btn_add2", "追加", $extra.$sendUpdDisabled);
        //更新ボタン
        $upd_cmd = ($model->infoDiv >= "4") ? "update".$model->infoDiv : "update2";
        $extra = "onclick=\"return btn_submit('".$upd_cmd."');\"" . $guard_disabled;
        $arg["button"]["btn_update2"]  = knjCreateBtn($objForm, "btn_update2", "更新", $extra.$sendUpdDisabled);
        //更新後前の生徒へボタン
        if ($model->infoDiv == "2" || $model->infoDiv == "3") {
            $extra = "" . $guard_disabled;
            $arg["button"]["btn_up_next2"] = str_replace("name", $guard_disabled.$sendUpdDisabled." name", View::updateNext($model, $objForm, 'btn_update2'));
        }
        //削除ボタン
        $del_cmd = ($model->infoDiv >= "4") ? "delete".$model->infoDiv : "delete2";
        $extra = "onclick=\"return btn_submit('".$del_cmd."');\"" . $guard_disabled;
        $arg["button"]["btn_del2"]     = knjCreateBtn($objForm, "btn_del2", "削除", $extra.$sendUpdDisabled);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"" . $guard_disabled;
        $arg["button"]["btn_reset2"]   = knjCreateBtn($objForm, "btn_reset2", "取消", $extra.$sendUpdDisabled);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"" . $guard_disabled;
        $arg["button"]["btn_end2"]     = knjCreateBtn($objForm, "btn_end2", "終了", $extra);

        $result->free();
        Query::dbCheckIn($db);
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.top_frame.location.href='knja110_2aindex.php?cmd=list';";
        }

        View::toHTML5($model, "knja110_2aForm2.html", $arg);
    }
}
