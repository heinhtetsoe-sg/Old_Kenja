<?php

require_once('for_php7.php');

class knjl320gForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl320gForm1", "POST", "knjl320gindex.php", "", "knjl320gForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $extra = "";
        $query = knjl320gQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボボックス
        $extra = "";
        $query = knjl320gQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //最低点
        $extra = "STYLE=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PASS_SCORE"] = knjCreateTextBox($objForm, $model->field["PASS_SCORE"], "PASS_SCORE", 3, 3, $extra);

        //点以下
        $extra = "STYLE=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PASS_SCORE_TO"] = knjCreateTextBox($objForm, $model->field["PASS_SCORE_TO"], "PASS_SCORE_TO", 3, 3, $extra);

        //帳票区分ラジオボタン
        $opt = array(1, 2, 3);
        $model->field["FORM_TYPE"] = ($model->field["FORM_TYPE"] == "") ? "1" : $model->field["FORM_TYPE"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"FORM_TYPE{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "FORM_TYPE", $model->field["FORM_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //合否区分ラジオボタン
        $opt = array(1, 2);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "1" : $model->field["SORT"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SORT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //特別措置者(インフルエンザ)
        $extra  = "onChange=\"change_flg()\" id=\"SPECIAL_REASON_DIV\" ";
        $extra .= strlen($Row["SPECIAL_REASON_DIV"]) ? "checked" : "";
        $arg["data"]["SPECIAL_REASON_DIV"] = knjCreateCheckBox($objForm, "SPECIAL_REASON_DIV", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl320gForm1.html", $arg); 
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
    knjCreateHidden($objForm, "PRGID", "KNJL320G");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
}
?>
