<?php

require_once('for_php7.php');

class knjz215Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz215index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != "changeCmb" && $model->cmd != "edit2") {
            if ($model->cmd == "reset" || $model->sendFlg) {
                $paraField = $model->sendField;
            } else {
                $paraField = $model->field;
            }
            $query = knjz215Query::getSelectData($paraField);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //学年
        $query = knjz215Query::getGrade($model);
        $extra = "onChange=\"return btn_submit('changeCmb');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["GRADE"], "GRADE", $extra, "BLANK");

        //科目数
        $query = knjz215Query::getGroupDiv();
        $extra = "onChange=\"return btn_submit('changeCmb');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["GROUP_DIV"], "GROUP_DIV", $extra);

        //コースリスト
        $selectData = array();
        foreach ($model->selectdata as $key => $val) {
            $selectData[$val] = "1";
        }
        if ($model->sendFlg) {
            $set = $model->sendField["GRADE"].":".$model->sendField["COURSECD"].":".$model->sendField["MAJORCD"].":".$model->sendField["COURSECODE"];
            $selectData[$set] = "1";
        }
        makeListToList($objForm, $arg, $db, $Row, "COURSE", $selectData, $model);

        //教科
        $query = knjz215Query::getClass($model);
        $extra = "onChange=\"return btn_submit('changeCmb');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["CLASSCD"], "CLASSCD", $extra, "BLANK");

        //科目リスト
        $selectData = array();
        foreach ($model->selectSublass as $key => $val) {
            $selectData[$val] = "1";
        }
        if ($model->sendFlg) {
            $result = $db->query(knjz215Query::getSubclassCnt($model->sendField, "SELECT", $model));
            while ($setSub = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $selectData[$setSub["SUBCLASSCD"]] = "1";
            }
            $result->free();
        }
        makeListToList($objForm, $arg, $db, $Row, "SUBCLASS", $selectData, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz215index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz215Form2.html", $arg); 
    }
}

//コース一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $Row, $name, $selectData, &$model)
{
    //コース一覧
    $optData = array();
    $optSelect = array();
    if ($name == "COURSE") {
        $query = knjz215Query::getCourse($model, $Row["GRADE"], $Row["GROUP_DIV"], "");
    } else {
        $query = knjz215Query::getSubclass($Row["CLASSCD"], "", $model);
    }
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
            if ($name == "COURSE") {
                $query = knjz215Query::getCourse($model, $Row["GRADE"], $Row["GROUP_DIV"], $key);
            } else {
                $query = knjz215Query::getSubclass($Row["CLASSCD"], $key,  $model);
            }
            $otpSet = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($otpSet)) {
                $optSelect[]= array('label' => $otpSet["LABEL"],
                                    'value' => $otpSet["VALUE"]);
            }
        }
    }

    //コース一覧作成
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('left', '".$name."')\"";
    $arg[$name][$name."_NAME"] = knjCreateCombo($objForm, $name."_NAME", "", $optData, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('right', '".$name."')\"";
    $arg[$name][$name."_SELECTED"] = knjCreateCombo($objForm, $name."_SELECTED", "", $optSelect, $extra, 15);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', '".$name."');\"";
    $arg[$name][$name."_LEFTS"] = knjCreateBtn($objForm, $name."_LEFTS", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', '".$name."');\"";
    $arg[$name][$name."_LEFT1"] = knjCreateBtn($objForm, $name."_LEFT1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', '".$name."');\"";
    $arg[$name][$name."_RIGHT1"] = knjCreateBtn($objForm, $name."_RIGHT1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', '".$name."');\"";
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
function makeHidden(&$objForm, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectSublass");
}

?>
