<?php

require_once('for_php7.php');

class knjz216aForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz216aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != "changeCmb") {
            if ($model->cmd == "reset" || $model->sendFlg) {
                $paraField = $model->sendField;
            } else {
                $paraField = $model->field;
            }
            $query = knjz216aQuery::getSelectData($paraField);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //実力テスト区分
        $query = knjz216aQuery::getProficiencyDiv();
        $extra = "onChange=\"btn_submit('changeCmb');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["PROFICIENCYDIV"], "PROFICIENCYDIV", $extra, 1, "BLANK");

        //模試
        $query = knjz216aQuery::getProficiencyName1($Row["PROFICIENCYDIV"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["PROFICIENCYCD"], "PROFICIENCYCD", $extra, "BLANK");

        //学年
        $query = knjz216aQuery::getGrade($model, "SCHREG_REGD_HDAT");
        $extra = "onChange=\"return btn_submit('changeCmb');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["GRADE"], "GRADE", $extra, "BLANK");

        //List to List
        $selectData = array();
        foreach ($model->selectdata as $key => $val) {
            $selectData[$val] = "1";
        }
        if ($model->sendFlg) {
            $set = $model->sendField["GRADE"].":".$model->sendField["COURSECD"].":".$model->sendField["MAJORCD"].":".$model->sendField["COURSECODE"];
            $selectData[$set] = "1";
        }
        makeListToList($objForm, $arg, $db, $Row, $selectData, $model);

        //科目数
        makeCmb($objForm, $arg, $db, "", $Row["GROUP_DIV"], "GROUP_DIV", "", "BLANK");

        //グループ名
        $arg["data"]["GROUP_NAME"] = knjCreateTextBox($objForm, $Row["GROUP_NAME"], "GROUP_NAME", 20, 20, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $Row, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz216aindex.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz216aForm2.html", $arg); 
    }
}

//コース一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $Row, $selectData, $model)
{
    //コース一覧
    $optData = array();
    $optSelect = array();
    $result = $db->query(knjz216aQuery::getCourse($model, $Row["GRADE"], ""));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($selectData[$row["VALUE"]] != "1" ) {
            $optData[]= array('label' => $row["LABEL"],
                              'value' => $row["VALUE"]);
        }
    }
    $result->free();

    foreach ($selectData as $key => $val) {
        if ($key) {
            $query = knjz216aQuery::getCourse($model, $Row["GRADE"], $key);
            $otpSet = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($otpSet)) {
                $optSelect[]= array('label' => $otpSet["LABEL"],
                                    'value' => $otpSet["VALUE"]);
            }
        }
    }

    //コース一覧作成
    $extra = "multiple style=\"width:180px\" width=\"180px\" ondblclick=\"move1('left')\"";
    $arg["data"]["COURSE_NAME"] = knjCreateCombo($objForm, "COURSE_NAME", "", $optData, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:180px\" width=\"180px\" ondblclick=\"move1('right')\"";
    $arg["data"]["COURSE_SELECTED"] = knjCreateCombo($objForm, "COURSE_SELECTED", "", $optSelect, $extra, 15);

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
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $opt = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    if ($name == "GROUP_DIV") {
        $opt[] = array("label" => "3", "value" => "3");
        $opt[] = array("label" => "5", "value" => "5");
        $opt[] = array("label" => "9", "value" => "9");
        if ($value != "3" && $value != "5" && $value != "9" && $value != null) {
            $opt[] = array("lavel" => $value, "value" => $value);
        }
    } else {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
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
function makeHidden(&$objForm, $Row, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "SEMESTER", $model->leftSemester);
}

?>
