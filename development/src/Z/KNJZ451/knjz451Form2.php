<?php

require_once('for_php7.php');

class knjz451Form2 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz451index.php", "", "edit");

        //DB接続
        $db  = Query::dbCheckOut();

        if ($model->year == "") $model->year = $db->getOne(knjz451Query::getExeYear());
        if (VARS::get("SEND_FLG") == "1" || $model->cmd == "qualiChange" || $model->cmd == "reset") {
            $query = knjz451Query::getQualifiedHdat($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["QUALIFIED_CD"] = $Row["QUALIFIED_CD"] ? $Row["QUALIFIED_CD"] : $model->field["QUALIFIED_CD"];
        } else {
            $Row =& $model->field;
        }

        /******************/
        /* コンボボックス */
        /******************/
        //取得可能検定
        $query = knjz451Query::getQualifiedMst($model);
        $extra = "onChange=\"return btn_submit('qualiChange');\"".$disabled;
        makeCmb($objForm, $arg, $db, $query, $Row["QUALIFIED_CD"], "QUALIFIED_CD", $extra, 1, "BLANK");

        /********************/
        /* テキストボックス */
        /********************/
        //取得期限
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["LIMIT_MONTH"] = knjCreateTextBox($objForm, $Row["LIMIT_MONTH"], "LIMIT_MONTH", 2, 2, $extra);
        //取得個数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SETUP_CNT"] = knjCreateTextBox($objForm, $Row["SETUP_CNT"], "SETUP_CNT", 2, 2, $extra);

        /******************/
        /* リストtoリスト */
        /******************/
        makeListToList($objForm, $arg, $db, $model, $disabled);

        /**********/
        /* ボタン */
        /**********/
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

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && VARS::post("cmd") != "edit" && VARS::post("cmd") != "reset") {
            $arg["reload"]  = "window.open('knjz451index.php?cmd=list_from_right&ed=1','left_frame');";
        }
        View::toHTML($model, "knjz451Form2.html", $arg);
    }
}
/********************************************************** 以下関数 **************************************************************/
//科目一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $disabled) {
    //クラス一覧
    $selectedArray = array();
    $leftList = $rightList = array();

    $query = knjz451Query::getLeftList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $selectedArray[] = $row["VALUE"];
        $leftList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
    }

    $query = knjz451Query::getRightList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $selectedArray)) { //リストの左側に表示する科目は除く
            $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
    }

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
