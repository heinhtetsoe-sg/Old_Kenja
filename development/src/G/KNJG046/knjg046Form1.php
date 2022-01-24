<?php

require_once('for_php7.php');
class knjg046Form1
{
    function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjg046Form1", "POST", "knjg046index.php", "", "knjg046Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //開始日付作成
        $model->field["DATE_FROM"] = $model->field["DATE_FROM"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE_FROM"];
        $arg["data"]["DATE_FROM"] = View::popUpCalendar($objForm, "DATE_FROM", $model->field["DATE_FROM"]);

        //終了日付作成
        $model->field["DATE_TO"] = $model->field["DATE_TO"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE_TO"];
        $arg["data"]["DATE_TO"] = View::popUpCalendar($objForm, "DATE_TO", $model->field["DATE_TO"]);

        //チェックボックス
        $extra = ($model->field["PRINT_HOLIDAY"] == "1") ? "checked" : "";
        $extra .= " id=\"PRINT_HOLIDAY\"";
        $arg["data"]["PRINT_HOLIDAY"] = knjCreateCheckBox($objForm, "PRINT_HOLIDAY", "1", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg046Form1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJG046");
    knjCreateHidden($objForm, "hibiNyuuryokuNasi", $model->Properties["hibiNyuuryokuNasi"]);
    knjCreateHidden($objForm, "useNurseoffAttend", $model->Properties["useNurseoffAttend"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}
