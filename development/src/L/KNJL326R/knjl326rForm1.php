<?php

require_once('for_php7.php');

class knjl326rForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl326rForm1", "POST", "knjl326rindex.php", "", "knjl326rForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl326rQuery::getNameMst($model, $model->ObjYear, "L003"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["APPLICANTDIV"] = $row["VALUE"];
            if ($model->field["APPLICANTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] && $value_flg) ? $model->field["APPLICANTDIV"] : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('knjl326r');\"";
        $arg["data"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        //入試区分
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl326rQuery::getNameMst($model, $model->ObjYear, "L004"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["TESTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["TESTDIV"] = $row["VALUE"];
            if ($model->field["TESTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["TESTDIV"] = ($model->field["TESTDIV"] && $value_flg) ? $model->field["TESTDIV"] : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('knjl326r');\"";
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);

        //通知日付
        $disabled = "";
        $model->field["NOTICEDATE"] = $model->field["NOTICEDATE"] == "" ? CTRL_DATE : $model->field["NOTICEDATE"];
        $value = str_replace("-","/",$model->field["NOTICEDATE"]);
        $arg["data"]["NOTICEDATE"] = View::popUpCalendarAlp($objForm, "NOTICEDATE", $value, $disabled);

        //帳票種類ラジオボタン 1:お礼状 2:合否結果通知書 3:封筒
        $opt_form = array(1, 2, 3);
        $model->field["FORM"] = ($model->field["FORM"] == "") ? "1" : $model->field["FORM"];
        $extra = array("id=\"FORM1\" onclick =\" return btn_submit('knjl326r');\"", "id=\"FORM2\" onclick =\" return btn_submit('knjl326r');\"", "id=\"FORM3\" onclick =\" return btn_submit('knjl326r');\"");
        $radioArray = knjCreateRadio($objForm, "FORM", $model->field["FORM"], $extra, $opt_form, get_count($opt_form));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力範囲ラジオボタン 1:全校 2:指定
        $opt_output = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\" onclick =\" return btn_submit('knjl326r');\"", "id=\"OUTPUT2\" onclick =\" return btn_submit('knjl326r');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_output, get_count($opt_output));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出身学校コード指定
        $value = ($model->field["SCHOOLCD"]) ? $model->field["SCHOOLCD"] : "";
        if($model->field["OUTPUT"] == "2"){
            $extra = " STYLE=\"text-align: left\"; onblur=\"this.value=toInteger(this.value); this.value=setSchoolcd();\"";
        } else {
            $extra = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["SCHOOLCD"] = knjCreateTextBox($objForm, $value, "SCHOOLCD", 7, 7, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl326rForm1.html", $arg); 
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
    knjCreateHidden($objForm, "PRGID", "KNJL326R");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

    knjCreateHidden($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"]);
}
?>
