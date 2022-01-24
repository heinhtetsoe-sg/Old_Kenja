<?php

require_once('for_php7.php');

class knjz218bForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz218bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != "edit2") {
            if ($model->cmd == "reset" || $model->sendFlg) {
                $paraField = $model->sendField;
            } else {
                $paraField = $model->field;
            }
            $query = knjz218bQuery::getSelectData($paraField);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //グループCD
        $extra = " onblur=\"this.value=toInteger(this.value)\" ";
        $arg["data"]["GROUPCD"] = knjCreateTextBox($objForm, $Row["GROUPCD"], "GROUPCD", 4, 4, $extra);
        //グループ名
        $extra = "";
        $arg["data"]["GROUPNAME"] = knjCreateTextBox($objForm, $Row["GROUPNAME"], "GROUPNAME", 40, 40, $extra);
        //グループ略
        $extra = "";
        $arg["data"]["GROUPABBV"] = knjCreateTextBox($objForm, $Row["GROUPABBV"], "GROUPABBV", 20, 20, $extra);

        //模試リスト
        $selectData = array();
        if ($model->cmd != "reset") {
            foreach ($model->selectMock as $key => $val) {
                $selectData[$val] = "1";
            }
        }
        if ($model->sendFlg || $model->cmd == "reset") {
            $result = $db->query(knjz218bQuery::getSubclassCnt($model->sendField, "SELECT"));
            while ($setSub = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $selectData[$setSub["MOCKCD"]] = "1";
            }
            $result->free();
        }
        makeListToList($objForm, $arg, $db, $Row, "SUBCLASS", $selectData, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $Row, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz218bindex.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz218bForm2.html", $arg); 
    }
}

//模試一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $Row, $name, $selectData, $model)
{
    //コース一覧
    $optData = array();
    $optSelect = array();
    $query = knjz218bQuery::getSubclass($model);

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($selectData[$row["VALUE"]] != "1" ) {
            $optData[]= array('label' => $row["LABEL"],
                              'value' => $row["VALUE"]);
        }
    }
    $result->free();

    foreach ($selectData as $key => $val) {
        if ($key) {
                $query = knjz218bQuery::getSubclass($model, $key);
            $otpSet = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($otpSet)) {
                $optSelect[]= array('label' => $otpSet["LABEL"],
                                    'value' => $otpSet["VALUE"]);
            }
        }
    }

    //コース一覧作成
    $extra = "multiple style=\"width:200px\" width=\"200px\" ondblclick=\"move1('left')\"";
    $arg[$name][$name."_NAME"] = knjCreateCombo($objForm, $name."_NAME", "", $optData, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:200px\" width=\"200px\" ondblclick=\"move1('right')\"";
    $arg[$name][$name."_SELECTED"] = knjCreateCombo($objForm, $name."_SELECTED", "", $optSelect, $extra, 15);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg[$name][$name."_LEFTS"] = knjCreateBtn($objForm, $name."_LEFTS", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg[$name][$name."_LEFT1"] = knjCreateBtn($objForm, $name."_LEFT1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg[$name][$name."_RIGHT1"] = knjCreateBtn($objForm, $name."_RIGHT1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg[$name][$name."_RIGHTS"] = knjCreateBtn($objForm, $name."_RIGHTS", ">>", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $result = $db->query($query);
    $opt = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $Row, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectMock");
    knjCreateHidden($objForm, "SEMESTER", $model->leftSemester);
}

?>
