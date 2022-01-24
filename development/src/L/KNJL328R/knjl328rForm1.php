<?php

require_once('for_php7.php');

class knjl328rForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl328rForm1", "POST", "knjl328rindex.php", "", "knjl328rForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl328rQuery::getNameMst($model, $model->ObjYear, "L003"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["APPLICANTDIV"] = $row["VALUE"];
            if ($model->field["APPLICANTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] && $value_flg) ? $model->field["APPLICANTDIV"] : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('knjl328r');\"";
        $arg["data"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        //入試区分
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl328rQuery::getNameMst($model, $model->ObjYear, "L024"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["TESTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["TESTDIV"] = $row["VALUE"];
            if ($model->field["TESTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["TESTDIV"] = ($model->field["TESTDIV"] && $value_flg) ? $model->field["TESTDIV"] : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('knjl328r');\"";
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);

        //通知日付
        $disabled = "";
        $model->field["NOTICEDATE"] = $model->field["NOTICEDATE"] == "" ? CTRL_DATE : $model->field["NOTICEDATE"];
        $value = str_replace("-","/",$model->field["NOTICEDATE"]);
        $arg["data"]["NOTICEDATE"] = View::popUpCalendarAlp($objForm, "NOTICEDATE", $value, $disabled);

        //帳票種類ラジオボタン 1:通知 2:特別奨学生通知書
        $opt_form = array(1, 2);
        $model->field["FORM"] = ($model->field["FORM"] == "") ? "1" : $model->field["FORM"];
        $extra = array("id=\"FORM1\" onclick =\" return btn_submit('knjl328r');\"", "id=\"FORM2\" onclick =\" return btn_submit('knjl328r');\"");
        $radioArray = knjCreateRadio($objForm, "FORM", $model->field["FORM"], $extra, $opt_form, get_count($opt_form));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //（通知）出力範囲１ラジオボタン 1:全て 2:合格通知書 3:不合格通知書
        $opt_output1 = array(1, 2, 3);
        $model->field["OUTPUT1"] = ($model->field["OUTPUT1"] == "") ? "1" : $model->field["OUTPUT1"];
        $click  = ($model->field["FORM"] == "1") ? "" : " disabled";
        $click .= " onclick =\" return btn_submit('knjl328r');\"";
        $extra  = array("id=\"OUTPUT11\"".$click, "id=\"OUTPUT12\"".$click, "id=\"OUTPUT13\"".$click);
        $radioArray = knjCreateRadio($objForm, "OUTPUT1", $model->field["OUTPUT1"], $extra, $opt_output1, get_count($opt_output1));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //（通知）出力範囲２ラジオボタン 1:全員 2:指定
        $opt_output2 = array(1, 2);
        $model->field["OUTPUT2"] = ($model->field["OUTPUT2"] == "") ? "1" : $model->field["OUTPUT2"];
        $extra = array("id=\"OUTPUT21\"".$click, "id=\"OUTPUT22\"".$click);
        $radioArray = knjCreateRadio($objForm, "OUTPUT2", $model->field["OUTPUT2"], $extra, $opt_output2, get_count($opt_output2));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //（通知）受験番号指定
        $value = ($model->field["RECEPTNO1"]) ? $model->field["RECEPTNO1"] : "";
        if ($model->field["FORM"] == "1" && $model->field["OUTPUT2"] == "2") {
            $extra = " STYLE=\"text-align: left\"; onblur=\"this.value=toAlphaNumber(this.value)\"";
        } else {
            $extra = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["RECEPTNO1"] = knjCreateTextBox($objForm, $value, "RECEPTNO1", 5, 5, $extra);

        //（奨学生）出力範囲ラジオボタン 1:全員 2:指定
        $opt_output3 = array(1, 2);
        $model->field["OUTPUT3"] = ($model->field["OUTPUT3"] == "") ? "1" : $model->field["OUTPUT3"];
        $click  = ($model->field["FORM"] == "2") ? "" : " disabled";
        $click .= " onclick =\" return btn_submit('knjl328r');\"";
        $extra = array("id=\"OUTPUT31\"".$click, "id=\"OUTPUT32\"".$click);
        $radioArray = knjCreateRadio($objForm, "OUTPUT3", $model->field["OUTPUT3"], $extra, $opt_output3, get_count($opt_output3));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //（奨学生）受験番号指定
        $value = ($model->field["RECEPTNO2"]) ? $model->field["RECEPTNO2"] : "";
        if ($model->field["FORM"] == "2" && $model->field["OUTPUT3"] == "2") {
            $extra = " STYLE=\"text-align: left\"; onblur=\"this.value=toAlphaNumber(this.value)\"";
        } else {
            $extra = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["RECEPTNO2"] = knjCreateTextBox($objForm, $value, "RECEPTNO2", 5, 5, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl328rForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
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

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL328R");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

    knjCreateHidden($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"]);
}
?>
