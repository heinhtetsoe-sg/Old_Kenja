<?php

require_once('for_php7.php');

class knja611vForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja611vForm1", "POST", "knja611vindex.php", "", "knja611vForm1");

        //権限チェック
        $arg["jscript"] = "";
        if ($model->tableName != 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $arg["jscript"] = "preCheck();";
        }

        //データベース接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knja611vQuery::getSchkind($model);
            $extra = "onchange=\"return btn_submit('init');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SCHKIND"], "SCHKIND", $extra, 1);
        }

        //学期コンボ
        $query = knja611vQuery::getSemester();
        $extra = "onChange=\"btn_submit('init');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GAKKI"], "GAKKI", $extra, 1);

        //テストコンボ
        $query = knja611vQuery::getTestItem($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTKINDCD"], "TESTKINDCD", $extra, 1);

        //リストtoリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //欠席者は「*」を印字する
        $extra  = ($model->field["KESSEKI_FLG"] == '1') ? "checked " : "";
        $extra .= " id=\"KESSEKI_FLG\"";
        $arg["data"]["KESSEKI_FLG"] = knjCreateCheckBox($objForm, "KESSEKI_FLG", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //データベース切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja611vForm1.html", $arg); 
    }
}

//リストtoリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧取得
    $opt_left = $opt_right = array();
    $query = knja611vQuery::getGradeHrClass($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $model->selectdata)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        } else {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //クラス一覧リスト
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $opt_right, $extra, 15);

    //出力対象クラスリスト
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", $opt_left, $extra, 15);

    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }

    //初期値
    $default = $opt[0]["value"];
    if ($name == "GAKKI") $default = CTRL_SEMESTER;
    if ($name == "SCHKIND") $default = SCHOOLKIND;

    $value = ($value && $value_flg) ? $value : $default;
    if ($name == "TESTKINDCD" && !get_count($opt)) {
        $arg["data"][$name] = "該当データなし";
    } else {
        $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //CSVボタン
    $extra = ($model->field["TESTKINDCD"]) ? "onclick=\"return btn_submit('csv');\"" : "disabled";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "ＣＳＶ出力", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}
?>
