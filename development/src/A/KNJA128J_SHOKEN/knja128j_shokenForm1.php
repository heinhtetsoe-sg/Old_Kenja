<?php

require_once('for_php7.php');

class knja128j_shokenForm1 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("knja128j_shoken", "POST", "knja128j_shokenindex.php", "", "knja128j_shoken");

        //DB接続
        $db = Query::dbCheckOut();

        //年度・学年コンボ
        $opt = array();
        $opt[] = array('label' => "",'value' => "");
        $value_flg = false;
        $query = knja128j_shokenQuery::getTrainRow($model, "year_anuual");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["YEAR_ANNUAL"] == $row["VALUE"]) $value_flg = true;
        }
        $model->field["YEAR_ANNUAL"] = ($model->field["YEAR_ANNUAL"] && $value_flg) ? $model->field["YEAR_ANNUAL"] : $opt[0]["value"];
        $extra = "id = \"YEAR_ANNUAL\" aria-label = \"年度と学年\" onchange=\" current_cursor('YEAR_ANNUAL');return btn_submit('edit')\"";
        $arg["YEAR_ANUUAL"] = knjCreateCombo($objForm, "YEAR_ANNUAL", $model->field["YEAR_ANNUAL"], $opt, $extra, 1);

        //生徒名
        $getName = $db->getOne(knja128j_shokenQuery::getName($model));

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$getName;

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knja128j_shokenQuery::getBehavior($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $scd = $row["DIV"] .$row["CODE"];
            $Row["RECORD"][$scd] = $row["RECORD"];
        }
        $result->free();

        //特別活動の記録の観点取得
        $row = $db->getRow(knja128j_shokenQuery::getTrainRow($model, "setyear"), DB_FETCHMODE_ASSOC);
        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $Row =& $model->record;
            $row =& $model->field;
        }

        //行動の記録チェックボックス
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $checkboxName1 = array("基本的な生活習慣", "健康・体力の向上", "自主・自律", "責任感", "創意工夫", "思いやり・協力", "生命尊重・自然愛護", "勤労・奉仕", "公正・公平", "公共心・公徳心");
        for ($i=1; $i<11; $i++) {
            $ival = "1" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\" aria-label = \"".$checkboxName1[$i-1]."\"";
            $extra .= "disabled";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */

        //特別活動の記録チェックボックス
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $checkboxName2 = array("学級活動", "生徒会活動", "学校行事");
        for ($i=1; $i<5; $i++) {
            $ival = "2" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\" aria-label = \"".$checkboxName2[$i-1]."\"";
            $extra .= "disabled";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録の観点
        $arg["SPECIALACTREMARK"] = getTextOrArea($objForm, "SPECIALACTREMARK", $model->specialactremark_moji, $model->specialactremark_gyou, $row["SPECIALACTREMARK"], $model);
        $arg["SPECIALACTREMARK_COMMENT"] = "(全角".$model->specialactremark_moji."文字X".$model->specialactremark_gyou."行まで)";

        //戻るボタン
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = " id = \"btn_back\" onclick=\"parent.current_cursor_focus();return parent.closeit()\" aria-label =\"戻る\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja128j_shokenForm1.html", $arg);
    }
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = (int)$gyou % 5;
            $minus = ((int)$gyou / 5) > 1 ? ((int)$gyou / 5) * 6 : 5;
        }
        $height = $gyou * 13.5 + ((int)$gyou -1) * 3 + (5 - ($minus + $minusHasu));
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = "aria-label = \"特別活動の記録の観点 全角14文字X7行まで\" id=\"".$name."\" style=\"height:".$height."px;\" onkeyup=\"charCount(this.value, $gyou, ($moji * 2), true);\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ((int)$moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ((int)$moji * 2), $moji, $extra);
    }
    return $retArg;
}
?>
