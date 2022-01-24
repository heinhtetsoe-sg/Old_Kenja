<?php

require_once('for_php7.php');

class knjz239Form2 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz239index.php", "", "edit");
        $db  = Query::dbCheckOut();
        if ($model->year == "") $model->year = $db->getOne(knjz239Query::getExeYear());
        if (VARS::get("SEND_FLG") == "1" || $model->cmd == "reset") {
            $query = knjz239Query::getSubclassCompSelectMst($model->year, $model->grade, $model->groupcd, $model->courseMajor, $model->courseCode);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        $disabled = ($Row["GROUPCD"] < 600) ? "" : " disabled";
        //履修選択科目登録データチェック(SUBCLASS_STD_SELECT_DAT)
        $getCount = $db->getOne(knjz239Query::countSubclassStdSelectDat($model));
        if ($getCount > 0) {
            $deleteDisabled = " disabled";
        } else {
            $deleteDisabled = "";
        }
        //クリア処理
        if ($model->cmd === 'clear') {
            $model->selectdata = "";
            $Row = array();
            $disabled = "";
            $deleteDisabled = "";
        }
        /******************/
        /* コンボボックス */
        /******************/
        //学年
        $query = knjz239Query::getSchregRegdGdat($model);
        $extra = "onChange=\"return btn_submit('set');\"".$disabled;
        makeCmb($objForm, $arg, $db, $query, $Row["GRADE"], "GRADE", $extra, 1, "BLANK");

        //校種
        $query = knjz239Query::getSchoolKind($model);
        $model->schoolKind = $db->getOne($query);

        //課程学科
        $query = knjz239Query::getCourseMajor($model);
        $extra = "onChange=\"return btn_submit('set');\"".$disabled;
        makeCmb($objForm, $arg, $db, $query, $Row["COURSEMAJOR"], "COURSEMAJOR", $extra, 1, "BLANK");

        //コース
        $query = knjz239Query::getCourseCode($model);
        $extra = "onChange=\"return btn_submit('set');\"".$disabled;
        makeCmb($objForm, $arg, $db, $query, $Row["COURSECODE"], "COURSECODE", $extra, 1, "BLANK");

        //教科
        $query = knjz239Query::getClassCd($model);
        $opt = array();
        $opt[] = array('label' => '', 'value' => 'all');
        $value = $Row["CLASSCD"];
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('edit');\"".$disabled;
        $arg["data"]["CLASSCD"] = knjCreateCombo($objForm, "CLASSCD", $value, $opt, $extra, 1);

        /********************/
        /* テキストボックス */
        /********************/
        //選択グループコード
        $extra = "onblur=\"checkGroupcd(this)\";";
        $arg["data"]["GROUPCD"] = knjCreateTextBox($objForm, $Row["GROUPCD"], "GROUPCD", 3, 3, $extra);
        //特活グループ名称
        $extra = "";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 20, $extra);
        //特活グループ略称
        $extra = "";
        $arg["data"]["ABBV"] = knjCreateTextBox($objForm, $Row["ABBV"], "ABBV", 6, 3, $extra);
        //科目数上限
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["JOUGEN"] = knjCreateTextBox($objForm, $Row["JOUGEN"], "JOUGEN", 2, 2, $extra);
        //科目数下限
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["KAGEN"] = knjCreateTextBox($objForm, $Row["KAGEN"], "KAGEN", 2, 2, $extra);

        /******************/
        /* リストtoリスト */
        /******************/
        makeListToList($objForm, $arg, $db, $model, $disabled);

        /**********/
        /* ボタン */
        /**********/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"".$disabled;
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"".$deleteDisabled;
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
        //クリアボタン
        $extra = "onclick=\"return btn_submit('clear')\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "クリア", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && VARS::post("cmd") != "edit" && VARS::post("cmd") != "reset" && VARS::post("cmd") != "set" ) {
            $arg["reload"]  = "window.open('knjz239index.php?cmd=list_from_right&ed=1','left_frame');";
        }
        View::toHTML($model, "knjz239Form2.html", $arg);
    }
}
/********************************************************** 以下関数 **************************************************************/
//科目一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $disabled) {
    //クラス一覧
    $selectedArray = array();
    $leftList = $rightList = array();
    if ($model->field["SEND_FLG"] == '1' || $model->cmd == "reset") { //左フレームからの呼出の時 or リセットの時
        $query = knjz239Query::getSubclass2($model); //リストの左側に表示すべき科目を取得
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $selectedArray[] = $row["VALUE"];        //科目コードだけを配列に入れていく
        }
    } else {                                         //はじめに表示する時、更新後に表示する時など左フレームから呼ばれていない時
        if (is_array($model->selectdata)) {
            $selectedArray = $model->selectdata;     //右フレームのリストにあった科目コードを配列に入れていく
        }
    }
    $result = $db->query(knjz239Query::getSubclass($model)); //リストの右側を作る
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $selectedArray)) { //リストの左側に表示する科目は除く
            $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
    }
    //学年、課程学科、コースを選択した時、初期化する
    if ($model->cmd === 'set')  $selectedArray = array();
    foreach ($selectedArray as $val) {                      //リストの左側を作る
        $query = knjz239Query::getSubclass3($model, $val);  //配列の科目コードからラベルと値を取得
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $leftList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"".$disabled;
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"".$disabled;
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $leftList, $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"".$disabled;
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"".$disabled;
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"".$disabled;
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"".$disabled;
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
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
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
