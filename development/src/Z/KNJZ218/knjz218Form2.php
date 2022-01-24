<?php

require_once('for_php7.php');

class knjz218Form2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz218index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル設定
        makeTitle($arg, $db);

        //学年コンボ
        $query = knjz218Query::getGrade();
        $extra = "onChange=\"btn_submit('sel')\";";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra);

        //類型グループコードテキスト
        $extra = "onChange=\"btn_submit('cdchange')\";";
        $arg["data"]["TYPE_GROUP_CD"] = knjCreateTextBox($objForm, $model->type_group_cd, "TYPE_GROUP_CD", 6, 6, $extra);

        //類型グループ名称テキスト
        $model->type_group_name = ($model->type_group_name) ? $model->type_group_name : $db->getOne(knjz218Query::getGroupName($model->type_group_cd, $model->grade));
        $arg["data"]["TYPE_GROUP_NAME"] = knjCreateTextBox($objForm, $model->type_group_name, "TYPE_GROUP_NAME", 40, 20, "");

        //類型グループコースリストToリスト作成
        makeListToList($objForm, $arg, $db, $model, "COURSE", "0", 15);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (isset($model->message)) {   //更新できたら左のリストを再読込
            $arg["reload"] = "window.open('knjz218index.php?cmd=list&init=1', 'left_frame')";
        }

        View::toHTML($model, "knjz218Form2.html", $arg); 
    }
}

//タイトル設定
function makeTitle(&$arg, $db)
{
    $arg["info"]    = array("LEFT_LIST"     => "グループコース",
                            "RIGHT_LIST"    => "コース一覧" );
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra)
{
    $result = $db->query($query);
    $opt = array();

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $value = ($value) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}

//コースリストToリスト作成
function makeListToList(&$objForm, &$arg, $db, &$model, $name, $target, $size)
{
    $opt_left = $opt_right = array();

    $result = $db->query(knjz218Query::selectQuery($model->grade, $model->type_group_cd));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($row["JOINCD"]) {
            $opt_left[]  = array("label" => $row["LABEL"], 
                                 "value" => $row["VALUE"]);
        } else {
            $opt_right[] = array("label" => $row["LABEL"],
                                 "value" => $row["VALUE"]);
        }
    }
    $result->free();

    //グループコース
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right', '".$name."')\"";
    $arg["main_part"][$name."LEFT_PART"]   = knjCreateCombo($objForm, "L".$name, "right", $opt_left, $extra, $size);

    //コース一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left', '".$name."')\"";
    $arg["main_part"][$name."RIGHT_PART"]  = knjCreateCombo($objForm, "R".$name, "left", $opt_right, $extra, $size);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"return moves('sel_add_all', '".$name."');\"";
    $arg["main_part"][$name."SEL_ADD_ALL"] = knjCreateBtn($objForm, $name."sel_add_all", "≪", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move('left', '".$name."');\"";
    $arg["main_part"][$name."SEL_ADD"]     = knjCreateBtn($objForm, $name."sel_add", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move('right', '".$name."');\"";
    $arg["main_part"][$name."SEL_DEL"]     = knjCreateBtn($objForm, $name."sel_del", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"return moves('sel_del_all', '".$name."');\"";
    $arg["main_part"][$name."SEL_DEL_ALL"] = knjCreateBtn($objForm, $name."sel_del_all", "≫", $extra);

}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", "onclick=\"return doSubmit('insert');\"");
    //更新ボタン
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return doSubmit('update');\"");
    //削除ボタン
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", "onclick=\"return btn_submit('delete');\"");
    //取消ボタン
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "courseselect");
}

?>
