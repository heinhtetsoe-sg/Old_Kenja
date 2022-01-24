<?php

require_once('for_php7.php');

class knjl434mForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl434mForm1", "POST", "knjl434mindex.php", "", "knjl434mForm1");

        $opt=array();

        $arg["data"]["YEAR"] = $model->examYear;

        //DB接続
        $db = Query::dbCheckOut();

        //校種コンボボックス作成
        $extra = "onchange=\"return btn_submit('select_kind')\"";
        $query = knjl434mQuery::getSchoolKindName($model->examYear);
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND_DIV", $model->field["SCHOOL_KIND_DIV"], $extra, 1, 0, 0);

        //志望区分リストを作成する
        $extra = "multiple ondblclick=\"move1('left')\" style=\"width:250px; height:170px;\" ";
        $query = knjl434mQuery::getExamList($model->examYear, $model->field["SCHOOL_KIND_DIV"]);
        makeCmb($objForm, $arg, $db, $query, "EXAM_NAME", $model->field["EXAM_NAME"], $extra, 10, "");

        //出力対象志望区分リストを作成する
        $extra = "multiple ondblclick=\"move1('right')\" style=\"width:250px; height:170px;\" ";
        $arg["data"]["EXAM_SELECTED"] = knjCreateCombo($objForm, "EXAM_SELECTED", $model->field["EXAM_SELECTED"], array(), $extra, 10);

        //DB切断
        Query::dbCheckIn($db);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL434M");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "EXAM_YEAR", $model->examYear);
        knjCreateHidden($objForm, "EXAM_SELECTED_KEY");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl434mForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $multiple, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row1["LABEL"], "value" => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size, $multiple);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //CSV出力ボタン
    $extra = "onclick=\"btn_submit('csv');\"";
    $arg["button"]["btn_csvout"] = knjCreateBtn($objForm, "btn_csvout", "CSV出力", $extra);
}
