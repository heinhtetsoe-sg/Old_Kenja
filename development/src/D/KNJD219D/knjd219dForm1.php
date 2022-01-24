<?php

require_once('for_php7.php');


class knjd219dForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd219dForm1", "POST", "knjd219dindex.php", "", "knjd219dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        if ($model->Properties["useJviewstatLevel"] == "JVIEWSTAT_LEVEL_SEMES_MST") {
            $arg["useJviewstatLevel"] = 1;
            $query = knjd219dQuery::getSemester();
            $extra = "onchange=\"return btn_submit('knjd219d');\"";
            makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        } else {
            $model->field["SEMESTER"] = "9";
            knjCreateHidden($objForm, "SEMESTER", $model->field["SEMESTER"]);
        }

        //コメント切替
        if ($model->field["SEMESTER"] == "9" && $model->Properties["unCreateJviewstatRecordDatSeme9"] == '1') {
            $arg["COMENT1"] = "学年末の成績データの評定";
            $arg["COMENT2"] = "学年末の評定";
        } else if ($model->field["SEMESTER"] == "9") {
            $arg["COMENT1"] = "学年末の観点データと成績データの評定";
            $arg["COMENT2"] = "学年末の観点と評定";
        } else {
            $arg["COMENT1"] = "学期末の成績データの評価";
            $arg["COMENT2"] = "学期末の評価";
        }

        //選択区分ラジオボタン 1:クラス選択 2:学年選択
        $opt_div = array(1, 2);
        $model->field["SELECT_DIV"] = ($model->field["SELECT_DIV"] == "") ? "1" : $model->field["SELECT_DIV"];
        $extra  = array("id=\"SELECT_DIV1\" onclick=\"return btn_submit('knjd219d')\"", "id=\"SELECT_DIV2\" onclick=\"return btn_submit('knjd219d')\"");
        $radioArray = knjCreateRadio($objForm, "SELECT_DIV", $model->field["SELECT_DIV"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["SELECT_DIV"] == 1) $arg["class_course"] = '1';

        //学年コンボ作成
        $query = knjd219dQuery::getGradeHrClass($model);
        if ($model->field["SELECT_DIV"] == "1" || $model->field["SELECT_DIV"] == "2" ) {
            $extra = "onchange=\"return btn_submit('knjd219d'), AllClearList();\"";
        } else {
            $extra = "onchange=\"return btn_submit('knjd219d');\"";
        }
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd219dForm1.html", $arg); 
    }
}

function makeListToList(&$objForm, &$arg, $db, $model) {

    //対象一覧リストを作成する
    $query = knjd219dQuery::getHrClass($model);
    $result = $db->query($query);
    $opt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 15);

    //対象一覧リスト名
    $arg["data"]["NAME_LIST"] = ($model->field["SELECT_DIV"] == "1") ? 'クラス一覧' : 'コース一覧';

    //出力対象一覧リストを作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 15);

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
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg) {
    //実行ボタンを作成する
    $extra = "onclick=\"return btn_submit('execute');\"";
    $arg["button"]["btn_exe"] = knjCreateBtn($objForm, "btn_exe", "実 行", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "cmd");
}

?>
