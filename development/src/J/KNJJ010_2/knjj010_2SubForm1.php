<?php

require_once('for_php7.php');

class knjj010_2SubForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjj010_2index.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //校種表示
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $arg["dispschkind"] = "1";
            $query = knjj010_2Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('subform1');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND_CI", $model->schKind_CI, $extra, 1);
        }

        //部クラブ情報
        $info = $db->getRow(knjj010_2Query::getRow($model, $model->clubcd), DB_FETCHMODE_ASSOC);
        $arg["CLUBINFO"] = $info["CLUBCD"].'　'.$info["CLUBNAME"];

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "subform1"){
            $arg["reload"]  = "parent.left_frame.location.href='knjj010_2index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjj010_2SubForm1.html", $arg);
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //対象種目一覧を作成する(※useClubMultiSchoolKind=1の時、SQLにて、既存選択データを継続保持するよう、指定。)
    $query = knjj010_2Query::getClubItemDat($model, $model->clubcd);
    $result = $db->query($query);
    $opt_left = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:180px\" width=\"180px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

    //種目一覧を作成する
    $query = knjj010_2Query::getClubItemMst($model, $model->clubcd);
    $result = $db->query($query);
    $opt_right = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:180px\" width=\"180px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

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

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //更新ボタン
    $extra = "onclick=\"return btn_submit('subform1_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('subform1_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('edit');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectItem");
    knjCreateHidden($objForm, "useClubMultiSchoolKind", $model->Properties["useClubMultiSchoolKind"]);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
