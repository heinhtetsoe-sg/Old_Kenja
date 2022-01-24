<?php

require_once('for_php7.php');

class knjz290_2SubForm2
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knjz290_2index.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();

        //職員リストToリスト作成
        $name = "STAFF";
        makeListToList($objForm, $arg, $db, $model, $name);

        //科目リストToリスト作成
        $name = "SUBCLASS";
        makeListToList($objForm, $arg, $db, $model, $name);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz290_2SubForm2.html", $arg);
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $name) {

    //対象リストを作成する//
    if($name == "SUBCLASS"){
        $query = knjz290_2Query::getStfSubclass($model->staffcd);
    } else {
        $query = knjz290_2Query::getStaffList($model, "select");
    }
    $result = $db->query($query);
    $opt_left = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right', '".$name."')\"";
    $arg["data"][$name."_SELECTED"] = knjCreateCombo($objForm, $name."_SELECTED", "", $opt_left, $extra, 15);

    //一覧リストを作成する
    if($name == "SUBCLASS"){
        $query = knjz290_2Query::getSubclass($model->staffcd);
    } else {
        $query = knjz290_2Query::getStaffList($model, "list");
    }
    $result = $db->query($query);
    $opt_right = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left', '".$name."')\"";
    $arg["data"][$name."_NAME"] = knjCreateCombo($objForm, $name."_NAME", "", $opt_right, $extra, 15);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', '".$name."');\"";
    $arg["button"][$name."_rights"] = knjCreateBtn($objForm, $name."_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', '".$name."');\"";
    $arg["button"][$name."_lefts"] = knjCreateBtn($objForm, $name."_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', '".$name."');\"";
    $arg["button"][$name."_right1"] = knjCreateBtn($objForm, $name."_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', '".$name."');\"";
    $arg["button"][$name."_left1"] = knjCreateBtn($objForm, $name."_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform2_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform2_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('edit');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectStaff");
    knjCreateHidden($objForm, "selectSubclass");
}
?>
