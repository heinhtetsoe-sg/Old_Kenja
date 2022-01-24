<?php

require_once('for_php7.php');

class knjl328aForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl328aForm1", "POST", "knjl328aindex.php", "", "knjl328aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["TOP"]["YEAR"] = $model->examyear."年度";

        //受験校種コンボ
        $result = $db->query(knjl328aQuery::getNameMst($model->examyear, "L003"));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == "1") {
                $model->field["APPLICANTDIV"] = $row["VALUE"];
            }
        }
        $extra = "onChange=\"return btn_submit('chgApplicantdiv')\"";
        $arg["TOP"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        //受験校種コンボ
        $result = $db->query(knjl328aQuery::getPatternCombo($model));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($model->field["PATTERN_CD"] == "") {
                $model->field["PATTERN_CD"] = $row["VALUE"];
            }
        }
        $extra = "onChange=\"return btn_submit('chgPatternCd')\"";
        $arg["TOP"]["PATTERN_CD"] = knjCreateCombo($objForm, "PATTERN_CD", $model->field["PATTERN_CD"], $opt, $extra, 1);

        //専併区分
        $extra = "";
        $query = knjl328aQuery::getNameMst($model->examyear, "L006");
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $model->field["SHDIV"], $extra, 1, "ALL");

        //入学コース
        $extra = "";
        $query = knjl328aQuery::getTestdivMst($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, "ALL");

        //志望コース
        $extra = "";
        $query = knjl328aQuery::getCourseCmb($model, $model->field["APPLICANTDIV"]);
        makeCmb($objForm, $arg, $db, $query, "DESIREDIV", $model->field["DESIREDIV"], $extra, 1, "ALL");

        //受験番号範囲
        $extra = "";
        $arg["TOP"]["RECEPTNO_FROM"] = knjCreateTextBox($objForm, $model->field["RECEPTNO_FROM"], "RECEPTNO_FROM", 7, 7, $extra);
        $arg["TOP"]["RECEPTNO_TO"]   = knjCreateTextBox($objForm, $model->field["RECEPTNO_TO"], "RECEPTNO_TO", 7, 7, $extra);

        // 特待生、特待生以外
        $opt = array(1, 2);
        $onclick = "onclick =\" return btn_submit('chgTokutaiSelect');\"";
        if ($model->field["PATTERN_CD"] == "001") {
            $disabled = "";
            if (!$model->field["TOKUTAI_SELECT"]) {
                $model->field["TOKUTAI_SELECT"] = "2";
            }
        } else {
            $disabled = " disabled ";
            $model->field["TOKUTAI_SELECT"] = "";
        }
        $extra = array("id=\"TOKUTAI_SELECT1\" ".$onclick.$disabled , "id=\"TOKUTAI_SELECT2\" ".$onclick.$disabled);
        $radioArray = knjCreateRadio($objForm, "TOKUTAI_SELECT", $model->field["TOKUTAI_SELECT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "" || in_array($model->cmd, array("knjl328a", "reset", "chgApplicantdiv", "chgTokutaiSelect", "chgPatternCd"))) && $model->examyear && $model->field["APPLICANTDIV"]) {
            $query = knjl328aQuery::getRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //都道府県
        $extra = "";
        $arg["TOP"]["PREF_NAME"] = knjCreateTextBox($objForm, $Row["PREF_NAME"], "PREF_NAME", 8, 8, $extra);

        //口座番号
        $extra = " onblur=\"this.value=toNumber(this.value);\" ";
        $arg["TOP"]["ACCOUNT_NUMBER1"]  = knjCreateTextBox($objForm, $Row["ACCOUNT_NUMBER1"], "ACCOUNT_NUMBER1", 5, 5, $extra);
        $arg["TOP"]["ACCOUNT_NUMBER2"]  = knjCreateTextBox($objForm, $Row["ACCOUNT_NUMBER2"], "ACCOUNT_NUMBER2", 1, 1, $extra);
        $arg["TOP"]["ACCOUNT_NUMBER3"]  = knjCreateTextBox($objForm, $Row["ACCOUNT_NUMBER3"], "ACCOUNT_NUMBER3", 7, 7, $extra);

        //件名
        $extra = "";
        $arg["TOP"]["SUBJECT"] = knjCreateTextBox($objForm, $Row["SUBJECT"], "SUBJECT", 16, 16, $extra);

        //通信欄
        $extra = "";
        if ($model->field["PATTERN_CD"] == "001" && ($model->cmd == '' || in_array($model->cmd, array("chgApplicantdiv", "chgTokutaiSelect", "chgPatternCd")))) {
            // 名称マスタから内訳を取得しセット
            $query = knjl328aQuery::getNameMstL056($model->examyear, $model->field["APPLICANTDIV"]);
            $result = $db->query($query);
            $maxlen = 10;
            while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->field["TOKUTAI_SELECT"] == "1" && $row1["NAMECD2"] == "1") {
                    continue; // 入学金は対象外
                }
                $len = mb_strlen($row1["NAME1"]);
                $maxlen = $maxlen < $len ? $len : $maxlen;
            }
            $maxlen = $maxlen * 2;  // 半角桁 = 全角文字数 * 2
            $result->free();
            $comm = "";
            $moneySum = 0;
            $result = $db->query($query);
            while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->field["TOKUTAI_SELECT"] == "1" && $row1["NAMECD2"] == "1") {
                    continue; // 入学金は対象外
                }
                $len = mb_strlen($row1["NAME1"]) * 2;
                $space = "";
                for ($i = 0; $i < $maxlen - $len; $i++) {
                    $space .= " ";
                }
                $comm .= "\n";
                $comm .= sprintf("　　%s{$space}　　%10s", $row1["NAME1"], $row1["NAMESPARE1"]);
                $moneySum += $row1["NAMESPARE2"];
            }
            $result->free();
            if ($comm) {
                $Row["COMMUNICATION"] .= $comm;
                $Row["TRANSFER_MONEY"] = $moneySum;
            }
        }
        $arg["TOP"]["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION", 4, 60, "", $extra, $Row["COMMUNICATION"]);

        //加入者名
        $extra = "";
        $arg["TOP"]["MEMBER"] = knjCreateTextBox($objForm, $Row["MEMBER"], "MEMBER", 20, 20, $extra);

        //金額
        $extra = " onblur=\"this.value=toNumber(this.value);\" ";
        $arg["TOP"]["TRANSFER_MONEY"] = knjCreateTextBox($objForm, $Row["TRANSFER_MONEY"], "TRANSFER_MONEY", 8, 8, $extra);

        //印刷ボタン
        $extra = "onclick=\"return btn_submit('updateAndPrint');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examyear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL328A");
        if (!isset($model->warning) && $model->cmd == "print") {
            $arg["jscript"] = " newwin('" . SERVLET_URL . "');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl328aForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    }
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg && $value != "ALL") {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
