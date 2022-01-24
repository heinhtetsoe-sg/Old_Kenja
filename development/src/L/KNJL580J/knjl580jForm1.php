<?php

require_once('for_php7.php');

class knjl580jForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["TOP"]["YEAR"] = CTRL_YEAR + 1;

        //学校種別
        $query = knjl580jQuery::getNameMst($model->year, "L003");
        $extra = "onchange=\"return btn_submit('read');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //入試種別
        $query = knjl580jQuery::getTestdiv($model);
        $extra = "onchange=\"return btn_submit('read');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        /******************/
        /**リストToリスト**/
        /******************/
        //リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**************/
        /* ボタン作成 */
        /**************/
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "EXAM_TYPE", $model->exam_type);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL580J");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl580jindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl580jForm1.html", $arg);
    }
}
//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    $rightList = $leftList = $leftArr = array();
    $leftCnt = $rightCnt = 0;
    $query = knjl580jQuery::getReceptDatData($model);
    $result = $db->query($query);
    
    //入学辞退者一覧
    while ($rowL = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($rowL["ENTDIV"] == "2") {
            $leftList[] = array('label' => $rowL["LABEL"],
                                'value' => $rowL["VALUE"]);
            $leftArr[] = $row["VALUE"];
            $leftCnt++;
        }
    }
    $result = $db->query($query);

    //手続終了者一覧
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($row["ENTDIV"] != "2" && !in_array($row["VALUE"], $leftArr)) {
            $rightList[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
            $rightCnt++;
        }
    }
    $result->free();

    $arg["data"]["leftCount"]  = $leftCnt;
    $arg["data"]["rgihtCount"] = $rightCnt;

    //手続終了者一覧（受験番号順）
    $extra = "multiple style=\"width:500px\" width=\"500px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 40);

    //入学辞退者一覧
    $extra = "multiple style=\"width:500px\" width=\"500px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $leftList, $extra, 40);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
