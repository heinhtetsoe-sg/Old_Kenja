<?php

require_once('for_php7.php');

class knjz350_nenkan_testitemForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz350_nenkan_testitemindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != "changeCmb") {
            if ($model->cmd == "reset" || $model->sendFlg) {
                $paraField = $model->sendField;
            } else {
                $paraField = $model->field;
            }
            $query = knjz350_nenkan_testitemQuery::getSelectData($paraField);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
            $model->sendField["GRADE"] = $Row["GRADE"];
            $model->sendField["COURSE_MAJOR_COURSECODE"] = $Row["COURSE_MAJOR_COURSECODE"];
            $model->sendField["SUBCLASSCD"] = $Row["SUBCLASSCD"];
        }

        //学年
        $query = knjz350_nenkan_testitemQuery::getGrade($model);
        $extra = "onChange=\"return btn_submit('changeCmb');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["GRADE"], "GRADE", $extra, "BLANK");

        //校種取得
        $query = knjz350_nenkan_testitemQuery::getSchoolKind($model, $Row["GRADE"]);
        $model->selectSchoolKind = $db->getOne($query);

        //コース
        $query = knjz350_nenkan_testitemQuery::getCourse($Row["GRADE"]);
        $extra = "onChange=\"return btn_submit('changeCmb');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["COURSE_MAJOR_COURSECODE"], "COURSE_MAJOR_COURSECODE", $extra, "BLANK");

        //科目
        $query = knjz350_nenkan_testitemQuery::getSubclass($model);
        $extra = "onChange=\"return btn_submit('changeCmb');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SUBCLASSCD"], "SUBCLASSCD", $extra, "BLANK");

        //List to List
        if ($model->Properties["useTestCountflg"]) {
            makeListToList($objForm, $arg, $db, $Row, $model, $model->Properties["useTestCountflg"]);
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "changeCmb" ){
            $arg["reload"]  = "window.open('knjz350_nenkan_testitemindex.php?cmd=list&shori=add','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz350_nenkan_testitemForm2.html", $arg); 
    }
}

//コース一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $Row, $model, $tableName)
{
    //コース一覧
    $optData = array();
    $optSelect = array();
    $result = $db->query(knjz350_nenkan_testitemQuery::getTestItem($model, $tableName, ""));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $optData[]= array('label' => $row["LABEL"],
                          'value' => $row["VALUE"]);
    }
    $result->free();
    $result = $db->query(knjz350_nenkan_testitemQuery::getTestItem($model, $tableName, "set"));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $optSelect[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
    }
    $result->free();

    //コース一覧作成
    $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left')\"";
    $arg["data"]["COURSE_NAME"] = knjCreateCombo($objForm, "COURSE_NAME", "", $optData, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right')\"";
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

    $result = $db->query($query);
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

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}

?>
