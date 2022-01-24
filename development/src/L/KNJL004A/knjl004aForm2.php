<?php

require_once('for_php7.php');

class knjl004aForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl004aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2" || $model->cmd == "reset") && $model->year && $model->applicantdiv && $model->patternCd && $model->tokutaiSelect) {
            $query = knjl004aQuery::getRow($model->year, $model->applicantdiv, $model->patternCd, $model->tokutaiSelect);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //初期値セット
        $model->year = ($model->year == "") ? CTRL_YEAR + 1: $model->year;
        if ($model->applicantdiv == "") {
            $extra = "onchange=\"return btn_submit('list');\"";
            $query = knjl004aQuery::getNameMst($model, "L003");
            makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");
        }
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl004aQuery::getNameMst($model, "L003", $model->applicantdiv));

        /*****************/
        /** textbox作成 **/
        /*****************/
        //取扱票種類
        $extra = " onblur=\"this.value=toInteger(this.value)\" ";
        $arg["data"]["PATTERN_CD"] = knjCreateTextBox($objForm, $Row["PATTERN_CD"], "PATTERN_CD", 3, 3, $extra);

        //取扱票種類名
        $extra = "";
        $arg["data"]["PATTERN_NAME"] = knjCreateTextBox($objForm, $Row["PATTERN_NAME"], "PATTERN_NAME", 20, 20, $extra);

        // 特待生、特待生以外
        $opt = array(1, 2);
        if (!$Row["TOKUTAI_SELECT"]) {
            $Row["TOKUTAI_SELECT"] = "2";
        }
        $disabled = ($Row["PATTERN_CD"] == "001") ? "" : " disabled ";
        $extra = array("id=\"TOKUTAI_SELECT1\" ".$disabled , "id=\"TOKUTAI_SELECT2\" ".$disabled);
        $radioArray = knjCreateRadio($objForm, "TOKUTAI_SELECT", $Row["TOKUTAI_SELECT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }
        if ($Row["PATTERN_CD"] == "002") {
            knjCreateHidden($objForm, "TOKUTAI_SELECT", "2");
        }

        //都道府県
        $extra = "";
        $arg["data"]["PREF_NAME"] = knjCreateTextBox($objForm, $Row["PREF_NAME"], "PREF_NAME", 8, 8, $extra);

        //口座番号
        $extra = " onblur=\"this.value=toInteger(this.value)\" ";
        $arg["data"]["ACCOUNT_NUMBER1"] = knjCreateTextBox($objForm, $Row["ACCOUNT_NUMBER1"], "ACCOUNT_NUMBER1", 5, 5, $extra);
        $arg["data"]["ACCOUNT_NUMBER2"] = knjCreateTextBox($objForm, $Row["ACCOUNT_NUMBER2"], "ACCOUNT_NUMBER2", 1, 1, $extra);
        $arg["data"]["ACCOUNT_NUMBER3"] = knjCreateTextBox($objForm, $Row["ACCOUNT_NUMBER3"], "ACCOUNT_NUMBER3", 7, 7, $extra);

        //加入者名
        $extra = "";
        $arg["data"]["MEMBER"] = knjCreateTextBox($objForm, $Row["MEMBER"], "MEMBER", 20, 20, $extra);

        //件名
        $extra = "";
        $arg["data"]["SUBJECT"] = knjCreateTextBox($objForm, $Row["SUBJECT"], "SUBJECT", 16, 16, $extra);

        //通信欄
        $extra = "";
        $arg["data"]["COMMUNICATION"] = knjCreateTextArea($objForm, $name, 4, 60, "", $extra, $Row["COMMUNICATION"]);

        //金額
        $extra = " onblur=\"this.value=toInteger(this.value)\" ";
        $arg["data"]["TRANSFER_MONEY"] = knjCreateTextBox($objForm, $Row["TRANSFER_MONEY"], "TRANSFER_MONEY", 8, 8, $extra);

        /**************/
        /**ボタン作成**/
        /**************/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '終 了', $extra);

        /**************/
        /**hidden作成**/
        /**************/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl004aindex.php?cmd=list2"
                            . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl004aForm2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
