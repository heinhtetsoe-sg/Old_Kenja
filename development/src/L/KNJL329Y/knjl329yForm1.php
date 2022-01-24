<?php

require_once('for_php7.php');

class knjl329yForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl329yForm1", "POST", "knjl329yindex.php", "", "knjl329yForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('knjl329y');\"";
        $query = knjl329yQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボボックス
        $model->field["TESTDIV"] = ($model->cmd == "change") ? $model->field["TESTDIV"] : "";
        $namecd = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $query = knjl329yQuery::getNameMst($model->ObjYear, $namecd);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], "", 1);

        //通知日付
        $model->field["NDATE"] = ($model->field["NDATE"] == "") ? str_replace("-", "/", CTRL_DATE) : $model->field["NDATE"];
        $arg["data"]["NDATE"] = View::popUpCalendarAlp($objForm, "NDATE", $model->field["NDATE"], "");

        //出力帳票ラジオボタン 1:学校宛結果一覧表 2:合格通知書
        $opt_outputd = array(1, 2);
        $model->field["OUTPUTDIV"] = ($model->field["OUTPUTDIV"] == "") ? "2" : $model->field["OUTPUTDIV"];
        $extra = array("id=\"OUTPUTDIV1\" onclick=\"return btn_submit('change');\"", "id=\"OUTPUTDIV2\" onclick=\"return btn_submit('change');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUTDIV", $model->field["OUTPUTDIV"], $extra, $opt_outputd, get_count($opt_outputd));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力範囲ラジオボタン 1:全て 2:指定
        $opt_output = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\" onclick=\"return btn_submit('change');\"", "id=\"OUTPUT2\" onclick=\"return btn_submit('change');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_output, get_count($opt_output));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学校番号指定
        $value = ($model->field["FS_CD"]) ? $model->field["FS_CD"] : "";
        if($model->field["OUTPUT"] == "2"){
            $extra = " STYLE=\"text-align: left\"; onblur=\"this.value=toInteger(this.value)\"";
        } else {
            $extra = " disabled STYLE=\"background-color:#cccccc\"";
        }
        $arg["data"]["FS_CD"] = knjCreateTextBox($objForm, $value, "FS_CD", 7, 7, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl329yForm1.html", $arg); 
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
    knjCreateHidden($objForm, "PRGID", "KNJL329Y");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
}
?>
