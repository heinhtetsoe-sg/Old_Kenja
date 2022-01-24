<?php

require_once('for_php7.php');


class knjl300cForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl300cForm1", "POST", "knjl300cindex.php", "", "knjl300cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試区分コンボの設定
        $query = knjl300cQuery::getTestDiv("L004", $model->ObjYear);
        $extra = " onChange=\"return btn_submit('knjl300c');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //受験型コンボの設定
        $query = knjl300cQuery::getExamType("L005", $model->ObjYear);
        $extra = " onChange=\"return btn_submit('knjl300c');\"";
        makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $model->field["EXAM_TYPE"], $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //受付開始番号（開始）
        $value = ($model->field["RECEPTNO_FROM"]) ? $model->field["RECEPTNO_FROM"] : "";
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RECEPTNO_FROM"] = knjCreateTextBox($objForm, $value, "RECEPTNO_FROM", 5, 5, $extra);

        //受付開始番号（終了）
        $value = ($model->field["RECEPTNO_TO"]) ? $model->field["RECEPTNO_TO"] : "";
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RECEPTNO_TO"] = knjCreateTextBox($objForm, $value, "RECEPTNO_TO", 5, 5, $extra);

        //開始位置（行）コンボの設定
        $line[0] = array('label' => "１行", 'value' => 1);
        $line[1] = array('label' => "２行", 'value' => 2);
        $line[2] = array('label' => "３行", 'value' => 3);
        $line[3] = array('label' => "４行", 'value' => 4);
        $line[4] = array('label' => "５行", 'value' => 5);
        $model->field["LINE"] = ($model->field["LINE"]) ? $model->field["LINE"] : $line[0]["value"];
        $arg["data"]["LINE"] = knjCreateCombo($objForm, "LINE", $model->field["LINE"], $line, "", 1);

        //開始位置（列）コンボの設定
        $row[0] = array('label' => "１列", 'value' => 1);
        $row[1] = array('label' => "２列", 'value' => 2);
        $row[2] = array('label' => "３列", 'value' => 3);
        $row[3] = array('label' => "４列", 'value' => 4);
        $model->field["ROW"] = ($model->field["ROW"]) ? $model->field["ROW"] : $row[0]["value"];
        $arg["data"]["ROW"] = knjCreateCombo($objForm, "ROW", $model->field["ROW"], $row, "", 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl300cForm1.html", $arg); 
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    $query = knjl300cQuery::getExamHall($model);
    $result = $db->query($query);
    $opt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //教育委員会一覧を作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 20);

    //出力対象一覧を作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
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

        if($name == "APPLICANTDIV" || $name == "TESTDIV"){
            if ($row["NAMESPARE2"] && $default_flg){
                $default = $i;
                $default_flg = false;
            } else {
                $i++;
            }
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
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJL300C");
    knjCreateHidden($objForm, "cmd");
}

?>
