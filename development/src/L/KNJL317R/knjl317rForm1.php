<?php

require_once('for_php7.php');

class knjl317rForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl317rForm1", "POST", "knjl317rindex.php", "", "knjl317rForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl317rQuery::getNameMst($model, $model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], "", 1, "");

        //入試区分コンボボックス
        $query = knjl317rQuery::getNameMst($model, $model->ObjYear, "L024");
        $extra = " return onchange=\"btn_submit('changeTest');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //傾斜配点出力
        $opt = array(1, 2);
        $model->field["OUTKEISYA"] = ($model->field["OUTKEISYA"] == "") ? "2" : $model->field["OUTKEISYA"];
        $extra = array("id=\"OUTKEISYA1\"", "id=\"OUTKEISYA2\"");
        $radioArray = knjCreateRadio($objForm, "OUTKEISYA", $model->field["OUTKEISYA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //加算点含む
        $extra = ($model->field["INC_KASAN"] == "on" && $model->cmd != "") ? "checked" : "";
        $extra .= " id=\"INC_KASAN\"";
        $arg["data"]["INC_KASAN"] = knjCreateCheckBox($objForm, "INC_KASAN", "on", $extra, "");

        //最高点
        $setMax = $model->field["TESTDIV"] == "1" ? "320" : "220";
        if ($model->cmd == 'changeTest') {
            $value = $setMax;
        } else {
            $value = ($model->field["MAX_SCORE"]) ? $model->field["MAX_SCORE"] : $setMax;
        }
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MAX_SCORE"] = knjCreateTextBox($objForm, $value, "MAX_SCORE", 5, 3, $extra);

        //最低点
        $setMin = $model->field["TESTDIV"] == "1" ? "100" : "70";
        if ($model->cmd == 'changeTest') {
            $value = $setMin;
        } else {
            $value = ($model->field["MIN_SCORE"]) ? $model->field["MIN_SCORE"] : $setMin;
        }
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MIN_SCORE"] = knjCreateTextBox($objForm, $value, "MIN_SCORE", 5, 3, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl317rForm1.html", $arg); 
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
    knjCreateHidden($objForm, "PRGID", "KNJL317R");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");

    knjCreateHidden($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"]);
}
?>
