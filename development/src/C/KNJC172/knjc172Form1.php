<?php

require_once('for_php7.php');

class knjc172Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjc172Form1", "POST", "knjc172index.php", "", "knjc172Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //年組コンボ
        $query = knjc172Query::getHrClass();
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "");

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        /********/
        /*ボタン*/
        /********/
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CATEGORY_SELECTED_DATA");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc172Form1.html", $arg); 
    }
}

function makeListToList(&$objForm, &$arg, $db, $model) {
    //更新対象生徒（左）を取得
    $query = knjc172Query::getSchregSchoolRefusal($model);
    $result = $db->query($query);
    $leftSchregnoList =array();
    $optLeft = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $leftSchregnoList[] = $row["SCHREGNO"];
        $optLeft[] = array('label' => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番"."　".$row["NAME_SHOW"],
                           'value' => $row["SCHREGNO"]);
    }

    //生徒一覧（右）を作成する
    $query = knjc172Query::getStudent($model, $leftSchregnoList);
    $result = $db->query($query);
    $optRight = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $optRight[] = array('label' => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番"."　".$row["NAME_SHOW"],
                            'value' => $row["SCHREGNO"]);
    }
    $result->free();

    //更新対象生徒（左）を作成する
    $extra = "multiple style=\"width:250px;\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $optRight, $extra, 20);

    //生徒一覧（右）を作成する
    $extra = "multiple style=\"width:250px;\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $optLeft, $extra, 20);

    //extra
    $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";

    //対象選択">>"ボタン
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra_rights);
    //対象取消"<<"ボタン
    $arg["button"]["btn_lefts"]  = knjCreateBtn($objForm, "btn_lefts", "<<", $extra_lefts);
    //対象選択"＞"ボタン
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra_right1);
    //対象取消"＜"ボタン
    $arg["button"]["btn_left1"]  = knjCreateBtn($objForm, "btn_left1", "＜", $extra_left1);
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
            'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
