<?php

require_once('for_php7.php');

class knjl013yForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl013yForm1", "POST", "knjl013yindex.php", "", "knjl013yForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('knjl013y');\"";
        $query = knjl013yQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボボックス
        $model->field["TESTDIV"] = ($model->field["APPLICANTDIV"] == $model->field["APP_HOLD"]) ? $model->field["TESTDIV"] : "";
        $namecd = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $query = knjl013yQuery::getNameMst($namecd, $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //座席番号生成済みリスト
        makeReceptList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl013yForm1.html", $arg); 
    }
}

//座席番号生成済みリスト
function makeReceptList(&$objForm, &$arg, $db, $model)
{
    $query = knjl013yQuery::getReceptCnt($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //高校(1:学特、3:一般)の場合、共愛中学出身者のうちわけも表示する
        if ($model->field["APPLICANTDIV"] == "2" && ($model->field["TESTDIV"] == "1" || $model->field["TESTDIV"] == "3")) {
            $naibu_label  = ($row["NAIBU"] == "6") ? "(共愛中)" : "";
            $row["LABEL"] = $row["LABEL"] .$naibu_label;
        }
        //帰国生対応(高校のみ)の場合、帰国生のうちわけも表示する
        $kikoku_label = ($row["KIKOKU"] == "1") ? "(帰国生)" : "";
        $row["LABEL"] = $row["LABEL"] .$kikoku_label;
        //受験番号取得
        $row["S_EXAMNO"] = $db->getOne(knjl013yQuery::getReceptExamno($model, $row["S_RECEPTNO"]));
        $row["E_EXAMNO"] = $db->getOne(knjl013yQuery::getReceptExamno($model, $row["E_RECEPTNO"]));
        $arg["data2"][] = $row;
    }
    $result->free();
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    $query = knjl013yQuery::getDesirediv($model);
    $result = $db->query($query);
    $opt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //高校(1:学特、3:一般)の場合、共愛中学出身者のうちわけも表示する
        if ($model->field["APPLICANTDIV"] == "2" && ($model->field["TESTDIV"] == "1" || $model->field["TESTDIV"] == "3")) {
            $naibu_label  = ($row["NAIBU"] == "6") ? "(共愛中)" : "";
            $row["LABEL"] = $row["LABEL"] .$naibu_label;
            $row["VALUE"] = $row["VALUE"] ."-" .$row["NAIBU"];
        }
        //帰国生対応(高校のみ)の場合、帰国生のうちわけも表示する
        $kikoku_label = ($row["KIKOKU"] == "1") ? "(帰国生)" : "";
        $row["LABEL"] = $row["LABEL"] .$kikoku_label;
        $row["VALUE"] = $row["KIKOKU"] ."-" .$row["VALUE"];
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //一覧を作成する
    $extra = "multiple style=\"width:300px\" width:\"300px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 10);

    //出力対象一覧を作成する
    $extra = "multiple style=\"width:300px\" width:\"300px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 10);

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
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "cmd");

    knjCreateHidden($objForm, "APP_HOLD", $model->field["APPLICANTDIV"]);
}
?>
