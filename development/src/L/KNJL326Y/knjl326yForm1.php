<?php

require_once('for_php7.php');

class knjl326yForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl326yForm1", "POST", "knjl326yindex.php", "", "knjl326yForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $model->field["APPLICANTDIV"] = "1";
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl326yQuery::getNameMst($model->ObjYear, "L003", $model->field["APPLICANTDIV"]));

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('knjl326y');\"";
        $query = knjl326yQuery::getNameMst($model->ObjYear, "L024");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //志望区分コンボボックス
        $query = knjl326yQuery::getDesireDiv($model);
        makeCmb($objForm, $arg, $db, $query, "DESIREDIV", $model->field["DESIREDIV"], "", 1);

        //通知日付
        $model->field["NDATE"] = ($model->field["NDATE"] == "") ? str_replace("-", "/", CTRL_DATE) : $model->field["NDATE"];
        $arg["data"]["NDATE"] = View::popUpCalendarAlp($objForm, "NDATE", $model->field["NDATE"], "");

        //出力範囲１ラジオボタン 1:合格者 2:不合格者
        $opt_output1 = array(1, 2);
        $model->field["OUTPUT1"] = ($model->field["OUTPUT1"] == "") ? "1" : $model->field["OUTPUT1"];
        $extra = array("id=\"OUTPUT11\" onclick =\" return btn_submit('knjl326y');\"", "id=\"OUTPUT12\" onclick =\" return btn_submit('knjl326y');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT1", $model->field["OUTPUT1"], $extra, $opt_output1, get_count($opt_output1));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //締め切り日付
        $model->field["LDATE"] = ($model->field["LDATE"] == "") ? str_replace("-", "/", CTRL_DATE) : $model->field["LDATE"];
        $disable = ($model->field["OUTPUT1"] == "2") ? "" : " disabled";
        $arg["data"]["LDATE"] = View::popUpCalendarAlp($objForm, "LDATE", $model->field["LDATE"], $disable);

        //出願日
        $model->field["APP_DATE"] = ($model->field["APP_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : $model->field["APP_DATE"];
        $disable = ($model->field["OUTPUT1"] == "2") ? "" : " disabled";
        $arg["data"]["APP_DATE"] = View::popUpCalendarAlp($objForm, "APP_DATE", $model->field["APP_DATE"], $disable);

        //出力範囲２ラジオボタン 1:全員 2:指定
        $opt_output2 = array(1, 2);
        $model->field["OUTPUT2"] = ($model->field["OUTPUT2"] == "") ? "1" : $model->field["OUTPUT2"];
        $extra = array("id=\"OUTPUT21\" onclick =\" return btn_submit('knjl326y');\"", "id=\"OUTPUT22\" onclick =\" return btn_submit('knjl326y');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT2", $model->field["OUTPUT2"], $extra, $opt_output2, get_count($opt_output2));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //受験番号指定
        $value = ($model->field["EXAMNO"]) ? $model->field["EXAMNO"] : "";
        if($model->field["OUTPUT2"] == "2"){
            $extra = " STYLE=\"text-align: left\"; onblur=\"this.value=toInteger(this.value)\"";
        } else {
            $extra = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $value, "EXAMNO", 5, 5, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl326yForm1.html", $arg); 
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
    knjCreateHidden($objForm, "PRGID", "KNJL326Y");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

    knjCreateHidden($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"]);
}
?>
