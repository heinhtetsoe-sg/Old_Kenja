<?php

require_once('for_php7.php');

class knjl507gForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl507gForm1", "POST", "knjl507gindex.php", "", "knjl507gForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $extra = "";
        $query = knjl507gQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボボックス
        $extra = "";
        $query = knjl507gQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //popUpCalendar
        $model->field["PRINT_DATE"] = $model->field["PRINT_DATE"] ? $model->field["PRINT_DATE"] : CTRL_DATE;
        $model->field["PRINT_DATE"] = str_replace("-", "/", $model->field["PRINT_DATE"]);
        $arg["data"]["PRINT_DATE"] = View::popUpCalendar($objForm, "PRINT_DATE", $model->field["PRINT_DATE"]);

        //帳票種類
        $opt = array(1, 2);
        $model->field["PRINT_DIV"] = ($model->field["PRINT_DIV"] == "") ? "1" : $model->field["PRINT_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PRINT_DIV{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "PRINT_DIV", $model->field["PRINT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //合格対象
        $opt = array(1, 2);
        $model->field["PASS_DIV"] = ($model->field["PASS_DIV"] == "") ? "1" : $model->field["PASS_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PASS_DIV{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "PASS_DIV", $model->field["PASS_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //不合格対象
        $opt = array(1, 2);
        $model->field["UNPASS_DIV"] = ($model->field["UNPASS_DIV"] == "") ? "1" : $model->field["UNPASS_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"UNPASS_DIV{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "UNPASS_DIV", $model->field["UNPASS_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //textbox
        $extra = "";
        $arg["data"]["PASS_EXAMNO"] = knjCreateTextBox($objForm, $model->field["PASS_EXAMNO"], "PASS_EXAMNO", 8, 8, $extra);
        $arg["data"]["PASS_EXAMNO_TO"] = knjCreateTextBox($objForm, $model->field["PASS_EXAMNO_TO"], "PASS_EXAMNO_TO", 8, 8, $extra);

        //textbox
        $extra = "";
        $arg["data"]["UNPASS_EXAMNO"] = knjCreateTextBox($objForm, $model->field["UNPASS_EXAMNO"], "UNPASS_EXAMNO", 8, 8, $extra);
        $arg["data"]["UNPASS_EXAMNO_TO"] = knjCreateTextBox($objForm, $model->field["UNPASS_EXAMNO_TO"], "UNPASS_EXAMNO_TO", 8, 8, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl507gForm1.html", $arg); 
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
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL507G");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
}
?>
