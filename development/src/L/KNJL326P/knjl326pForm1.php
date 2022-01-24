<?php

require_once('for_php7.php');

class knjl326pForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl326pForm1", "POST", "knjl326pindex.php", "", "knjl326pForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $query = knjl326pQuery::getNameMst($model->ObjYear, "L003");
        $extra = " onchange=\"return btn_submit('knjl326p');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $query = knjl326pQuery::getNameMst($model->ObjYear, $namecd1);
        $extra = " onChange=\"return btn_submit('knjl326ptestdiv');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //受験結果
        $disabled = " disabled ";
        $chcked = "";
        if ($model->field["APPLICANTDIV"] == "1" && $model->field["TESTDIV"] == "1") {
            $disabled =  "";
            $chcked = " checked ";
        }
        $extra = " id=\"PRINT_EXAM_RESULT\" ";
        $arg["data"]["PRINT_EXAM_RESULT"] = knjCreateCheckBox($objForm, "PRINT_EXAM_RESULT", "1", $extra.$chcked.$disabled);

        //通知日付
        $model->field["TSUCHI"] = ($model->field["TSUCHI"]) ? $model->field["TSUCHI"] : str_replace('-', '/', CTRL_DATE);
        $arg["data"]["TSUCHI"] = View::popUpCalendarAlp($objForm, "TSUCHI", $model->field["TSUCHI"], "", "");

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //radio（1:合格通知書, 2:不合格通知書）
        $opt = array(1, 2);
        $model->field["RESALT"] = ($model->field["RESALT"] == "") ? "1" : $model->field["RESALT"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"RESALT{$val}\" onClick=\"disAbled(this);\"");
        }
        $radioArray = knjCreateRadio($objForm, "RESALT", $model->field["RESALT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["RESALT"] == "1") {
            $reDisabled = "";
            $reDisabled2 = "disabled";
        } else {
            $reDisabled = "disabled";
            $reDisabled2 = "";
        }

        //事前印刷checkbox
        $extra  = ($model->field["JIZEN"] || $model->cmd == "") ? " checked " : "";
        $extra .= "id=\"JIZEN\"" .$reDisabled;
        $arg["data"]["JIZEN"] = knjCreateCheckBox($objForm, "JIZEN", "1", $extra);

        $oriDisabled = "";
        if ($model->cmd == '' || $model->cmd == 'knjl326ptestdiv') {
            //オリエンテーション日付、時間のデフォルト値
            $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L044" : "L045";
            $oriDisabled = ($model->field["APPLICANTDIV"] == "1" && $model->field["TESTDIV"] == "1") ? " disabled " : "";
            $query = knjl326pQuery::getNameMst($model->ObjYear, $namecd1);
            $result = $db->query($query);
            $abbv2 = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->field["TESTDIV"] == $row["VALUE"]) {
                    $abbv2 = $row["ABBV3"];
                }
            }
            $result->free();
            list($model->field["ORIDATE"], $time) = explode(" ", $abbv2);
            list($model->field["ORIHOUR"], $model->field["ORIMINUTE"]) = explode(":", $time);
        }

        //オリエンテーション日付
        $model->field["ORIDATE"] = ($model->field["ORIDATE"]) ? $model->field["ORIDATE"] : str_replace('-', '/', CTRL_DATE);
        $extra = $reDisabled." ".$oriDisabled;
        $arg["data"]["ORIDATE"] = View::popUpCalendarAlp($objForm, "ORIDATE", $model->field["ORIDATE"], $extra, "");

        //オリエンテーション時間
        $extra = "style=\"text-align:right\" onblur=\"return to_Integer(this);\"" .$reDisabled." ".$oriDisabled;
        $arg["data"]["ORIHOUR"] = knjCreateTextBox($objForm, $model->field["ORIHOUR"], "ORIHOUR", 2, 2, $extra);
        $extra = "style=\"text-align:right\" onblur=\"return to_Integer(this);\"" .$reDisabled." ".$oriDisabled;
        $arg["data"]["ORIMINUTE"] = knjCreateTextBox($objForm, $model->field["ORIMINUTE"], "ORIMINUTE", 2, 2, $extra);

        //事前印刷checkbox不合格通知書
        $extra  = ($model->field["JIZEN_UNPASS"] || $model->cmd == "") ? " checked " : "";
        $extra .= "id=\"JIZEN_UNPASS\"" .$reDisabled2;
        $arg["data"]["JIZEN_UNPASS"] = knjCreateCheckBox($objForm, "JIZEN_UNPASS", "1", $extra);

        //出力対象（1:全員, 2:受験番号指定）
        $opt = array(1, 2);
        $model->field["ALLFLG"] = ($model->field["ALLFLG"] == "") ? "1" : $model->field["ALLFLG"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"ALLFLG{$val}\" onClick=\"disAbled(this);\"");
        }
        $radioArray = knjCreateRadio($objForm, "ALLFLG", $model->field["ALLFLG"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["ALLFLG"] == "1") {
            $exDisabled = "disabled";
        } else {
            $exDisabled = "";
        }

        // 出力順ラジオボタンを作成
        $opt = array(1, 2);
        if (!$model->field["ORDER"]) $model->field["ORDER"] = 1;
        $onclick = "onclick =\" return btn_submit('knj');\"";
        $extra = array("id=\"ORDER1\" " , "id=\"ORDER2\" ");
        $radioArray = knjCreateRadio($objForm, "ORDER", $model->field["ORDER"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //受験番号指定textbox
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"" .$exDisabled;
        $arg["data"]["TEXTEXAMNO"] = knjCreateTextBox($objForm, $model->field["TEXTEXAMNO"], "TEXTEXAMNO", 6, 6, $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL326P");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl326pForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
