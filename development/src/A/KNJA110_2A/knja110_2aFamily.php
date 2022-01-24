<?php

require_once('for_php7.php');

class knja110_2aFamily
{
    public function main(&$model)
    {
        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knja110_2aindex.php", "", "edit");

        $arg["fep"] = $model->Properties["FEP"];

        $arg["reload"] = "";
        $db = Query::dbCheckOut();

        //更新等使用不可
        $sendUpdDisabled = "";
        if ($model->sendUnUpdate) {
            $sendUpdDisabled = " disabled ";
        }

        //生徒情報の取得        (SCHREG_ADDRESS_DAT)
        $row1 = $db->getRow(knja110_2aQuery::getRowAddress($model->schregno, $model->issuedate), DB_FETCHMODE_ASSOC);
        //緊急連絡先情報の取得  (SCHREG_BASE_MST)
        $row3 = $db->getRow(knja110_2aQuery::getEmergencyInfo($model->schregno), DB_FETCHMODE_ASSOC);
        //家族情報の取得      (SCHREG_RELA_DAT)
        $row6 = $db->getRow(knja110_2aQuery::getFamilyDat($model, $model->rela_no), DB_FETCHMODE_ASSOC);
        //備忘録
        $rowDetail009 = $db->getRow(knja110_2aQuery::getDetail009R2($model->schregno), DB_FETCHMODE_ASSOC);

        if ($model->Properties["useGuardian2"] == '1') {
            $arg["useGuardian2"] = "ON";
        } else {
            $arg["useGuardian2"] = "";
        }

        $model->form2 = array_merge((array)$row1, (array)$row3, (array)$row6, (array)$rowDetail009);

        if (!$model->isWarning()) {
            $row  = array_merge((array)$row1, (array)$row3, (array)$row6, (array)$rowDetail009);
        } else {
            $row =& $model->field;
        }

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
        $schreg_disabled    = "disabled";
        $arg["INFO_DIV6_COLOR"] = "style=\"color:yellow;\"";

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
        $arg["data"]["ZIPCD"] = str_replace("input ", "input {$schreg_disabled} ", View::popUpZipCode($objForm, "ZIPCD", $row["ZIPCD"], "ADDR1"));
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
        /*******  家族  *****************************************************************/
        /*******        *****************************************************************/
        /********************************************************************************/
        /********************************************************************************/
        global $sess;

        $query = knja110_2aQuery::getFamilyNo($model);
        $model->familyNo = $db->getOne($query);

        //コピー元学籍番号
        $model->stucd = $model->familyNo;
        $extra = "style=\"text-align:right; background-color:#999999\" readOnly ";
        $arg["data"]["STUCD"] = knjCreateTextBox($objForm, $model->stucd, "STUCD", 10, 10, $extra);

        //検索ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('./knja110_2aFamilySearch.php?cmd=search&MODE=reflect&useGuardian2={$model->Properties["useGuardian2"]}&CD=$model->schregno&FAMILYNO='+document.forms[0]['STUCD'].value+'&STUCD_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 600, 350)\"";
        $arg["data"]["BTN_STUCD"] = knjCreateBtn($objForm, "btn_stucd", "家族番号検索", $extra.$sendUpdDisabled);

        //連番
        $arg["data"]["RELA_NO"] = $row["RELA_NO"];
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
        $arg["data"]["RELA_SCHREGNO"]   = $row["RELA_SCHREGNO"];
        knjCreateHidden($objForm, "RELA_SCHREGNO", $row["RELA_SCHREGNO"]);

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
        //長子
        $extra = " id=\"TYOUSHI_FLG\" ";
        $tyoushi = $row["TYOUSHI_FLG"] == "1" ? " checked " : "";
        $arg["data"]["TYOUSHI_FLG"] = knjCreateCheckBox($objForm, "TYOUSHI_FLG", "1", $extra.$tyoushi);
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

        //追加ボタン
        $add_cmd = "addFamily";
        $extra = "onclick=\"return btn_submit('".$add_cmd."');\"" . $guard_disabled;
        $arg["button"]["btn_add2"]     = knjCreateBtn($objForm, "btn_add2", "追加", $extra.$sendUpdDisabled);
        //更新ボタン
        $upd_cmd = "updFamily";
        $extra = "onclick=\"return btn_submit('".$upd_cmd."');\"" . $guard_disabled;
        $arg["button"]["btn_update2"]  = knjCreateBtn($objForm, "btn_update2", "更新", $extra.$sendUpdDisabled);
        //削除ボタン
        $del_cmd = "delFamily";
        $extra = "onclick=\"return btn_submit('".$del_cmd."');\"" . $guard_disabled;
        $arg["button"]["btn_del2"]     = knjCreateBtn($objForm, "btn_del2", "削除", $extra.$sendUpdDisabled);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"" . $guard_disabled;
        $arg["button"]["btn_reset2"]   = knjCreateBtn($objForm, "btn_reset2", "取消", $extra.$sendUpdDisabled);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"" . $guard_disabled;
        $arg["button"]["btn_end2"]     = knjCreateBtn($objForm, "btn_end2", "終了", $extra);

        //hidden
        knjCreateHidden($objForm, "searchVal");

        $result->free();
        Query::dbCheckIn($db);
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.top_frame.location.href='knja110_2aindex.php?cmd=list';";
        }

        View::toHTML5($model, "knja110_2aFamily.html", $arg);
    }
}
