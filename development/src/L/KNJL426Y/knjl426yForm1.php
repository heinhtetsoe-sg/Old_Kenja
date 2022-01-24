<?php

require_once('for_php7.php');

class knjl426yForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl426yForm1", "POST", "knjl426yindex.php", "", "knjl426yForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('knjl426y');\"";
        $query = knjl426yQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('knjl426y');\"";
        $model->field["TESTDIV"] = ($model->field["APPLICANTDIV"] == $model->field["APP_HOLD"]) ? $model->field["TESTDIV"] : "";
        $query = knjl426yQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //通知日付
        $model->field["NDATE"] = ($model->field["NDATE"] == "") ? str_replace("-", "/", CTRL_DATE) : $model->field["NDATE"];
        $arg["data"]["NDATE"] = View::popUpCalendarAlp($objForm, "NDATE", $model->field["NDATE"], "");

        //印影チェック
        $extra = "id=\"CHECK_INEI\"";
        if ($model->field["CHECK_INEI"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["CHECK_INEI"] = knjCreateCheckBox($objForm, "CHECK_INEI", "1", $extra);


        //帳票種類ラジオボタン 1:通知 2:ラベル
        $opt_form = array(1, 2);
        $model->field["FORM"] = ($model->field["FORM"] == "") ? "1" : $model->field["FORM"];
        $extra = array("id=\"FORM1\" onclick =\" return btn_submit('knjl426y');\"", "id=\"FORM2\" onclick =\" return btn_submit('knjl426y');\"");
        $radioArray = knjCreateRadio($objForm, "FORM", $model->field["FORM"], $extra, $opt_form, get_count($opt_form));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //（通知）出力範囲１ラジオボタン 1:全て 2:合格者 3:不合格者 4:再受験
        if ($model->field["TESTDIV"] == "1" || $model->field["TESTDIV"] == "2") {
            $disabled = "";
        } else {
            $disabled = " disabled";
        }
        $opt_output1 = array(1, 2, 3, 4);
        $model->field["OUTPUT1"] = ($model->field["OUTPUT1"] == "") ? "1" : $model->field["OUTPUT1"];
        $click  = " onclick =\" return btn_submit('knjl426y');\"";
        $extra  = array("id=\"OUTPUT11\"".$click, "id=\"OUTPUT12\"".$click, "id=\"OUTPUT13\"".$click, "id=\"OUTPUT14\"".$click.$disabled);
        $radioArray = knjCreateRadio($objForm, "OUTPUT1", $model->field["OUTPUT1"], $extra, $opt_output1, get_count($opt_output1));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        knjCreateHidden($objForm, "OUTPUT1_VALUE", $model->field["OUTPUT1"]);

        //締め切り日付
        $model->field["SIME_DATE"] = ($model->field["SIME_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : $model->field["SIME_DATE"];
        $arg["data"]["SIME_DATE"] = View::popUpCalendarAlp($objForm, "SIME_DATE", $model->field["SIME_DATE"], $disabled);

        //（通知）出力範囲２ラジオボタン 1:全員 2:指定
        $opt_output2 = array(1, 2);
        $model->field["OUTPUT2"] = ($model->field["OUTPUT2"] == "") ? "1" : $model->field["OUTPUT2"];
        $extra = array("id=\"OUTPUT21\"".$click, "id=\"OUTPUT22\"".$click);
        $radioArray = knjCreateRadio($objForm, "OUTPUT2", $model->field["OUTPUT2"], $extra, $opt_output2, get_count($opt_output2));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //（通知）受験番号指定
        $value = ($model->field["EXAMNO1"]) ? $model->field["EXAMNO1"] : "";
        if($model->field["OUTPUT2"] == "2"){
            $extra = " STYLE=\"text-align: left\"; onblur=\"this.value=toInteger(this.value)\"";
        } else {
            $extra = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["EXAMNO1"] = knjCreateTextBox($objForm, $value, "EXAMNO1", 5, 5, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl426yForm1.html", $arg); 
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
    knjCreateHidden($objForm, "PRGID", "KNJL426Y");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

    knjCreateHidden($objForm, "APP_HOLD", $model->field["APPLICANTDIV"]);
}
?>
