<?php

require_once('for_php7.php');

class knjz218aForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("sel", "POST", "knjz218aindex.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != "change") {
            if ($model->cmd == "clear" || $model->sendFlg) {
                $paraField = $model->sendField;
            } else {
                $paraField = $model->field;
            }
            $query = knjz218aQuery::getCourseGroupHdata($paraField["GRADE"], $paraField["GROUP_CD"]);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
            if ($model->cmd === "change") {
                unset($Row["GROUP_CD"]);
                unset($Row["GROUP_NAME"]);
                unset($Row["GROUP_ABBV"]);
                unset($model->selectdata);
            }
        }

        //学年コンボ
        $query = knjz218aQuery::getGrade($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, $Row["GRADE"], "GRADE", $extra);

        //コースグループコードテキスト
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["GROUP_CD"] = knjCreateTextBox($objForm, $Row["GROUP_CD"], "GROUP_CD", 3, 3, $extra);

        //コースグループ名称テキスト
        $arg["data"]["GROUP_NAME"] = knjCreateTextBox($objForm, $Row["GROUP_NAME"], "GROUP_NAME", 20, 20, "");

        //コースグループ略称テキスト
        $arg["data"]["GROUP_ABBV"] = knjCreateTextBox($objForm, $Row["GROUP_ABBV"], "GROUP_ABBV", 10, 10, "");

        //タイトル設定
        makeTitle($arg, $db);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $Row, "COURSE", "0", 15, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (isset($model->message)) {   //更新できたら左のリストを再読込
            $arg["reload"] = "window.open('knjz218aindex.php?cmd=list&init=1', 'left_frame')";
        }

        View::toHTML($model, "knjz218aForm2.html", $arg); 
    }
}

//タイトル設定
function makeTitle(&$arg, $db) {
    $arg["info"]    = array("LEFT_LIST"     => "対象コース",
                            "RIGHT_LIST"    => "コース一覧" );
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra) {
    $result = $db->query($query);
    $opt = array();

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $value = ($value) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, &$Row, $name, $target, $size, $model) {
    $opt_left = $opt_right = array();
    $result = $db->query(knjz218aQuery::selectQuery($Row["GRADE"], $Row["GROUP_CD"]));
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

    //対象コース
    $extra = "multiple STYLE=\"width:400px\" width:\"400px\" ondblclick=\"move('right', '".$name."')\"";
    $arg["main_part"][$name."LEFT_PART"]   = knjCreateCombo($objForm, "L".$name, "right", $opt_left, $extra, $size);

    //コース一覧
    $extra = "multiple STYLE=\"width:400px\" width:\"400px\" ondblclick=\"move('left', '".$name."')\"";
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
function makeBtn(&$objForm, &$arg) {
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
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}
?>
