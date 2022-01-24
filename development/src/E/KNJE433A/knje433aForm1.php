<?php

require_once('for_php7.php');

class knje433aForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knje433aForm1", "POST", "knje433aindex.php", "", "knje433aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //教育委員会チェック
        $getFlg = $db->getOne(knje433aQuery::getNameMst());
        if ($getFlg !== '1') {
            $arg["jscript"] = "OnEdboardError();";
        }

        //年度
        $extra = "onChange=\"return btn_submit('knje433a');\"";
        $query = knje433aQuery::getYear();
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //クラス一覧リスト
        makeClassList($objForm, $arg, $db, $model);

        //出力データ区分
        $opt = array(1, 2);
        $model->field["DATA_DIV"] = ($model->field["DATA_DIV"] == "") ? "1" : $model->field["DATA_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DATA_DIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "DATA_DIV", $model->field["DATA_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje433aForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name != "YEAR") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    } else {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//クラス一覧リスト作成
function makeClassList(&$objForm, &$arg, $db, &$model)
{
    $row1 = array();
    $query = knje433aQuery::getSchoolData($model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row1[] = array('label' => $row["FLG_NAME"]." ".$row["EDBOARD_SCHOOLCD"].":".$row["EDBOARD_SCHOOLNAME"],
                        'value' => $row["FLG"]."".$row["EDBOARD_SCHOOLCD"]);
    }
    $result->free();

    //一覧リストを作成する
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $row1, $extra, 20);

    //出力対象教科リストを作成する
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $db) {
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJE433A");
}
?>
