<?php

require_once('for_php7.php');

class knjz415Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz415index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->l_groupcd)) {
            $query = knjz415Query::getLgroupMst($model->l_groupcd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //大分類グループコードテキスト作成
        $extra = "onBlur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["L_GROUPCD"] = knjCreateTextBox($objForm, $Row["L_GROUPCD"], "L_GROUPCD", 2, 2, $extra);

        //大分類グループ名称テキスト作成
        $arg["data"]["L_GROUPNAME"] = knjCreateTextBox($objForm, $Row["L_GROUPNAME"], "L_GROUPNAME", 60, 60, "");

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $Row);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (isset($model->message)) { //更新できたら左のリストを再読込
            $arg["reload"] = "window.open('knjz415index.php?cmd=list&init=1', 'left_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz415Form2.html", $arg); 
    }
}

function makeListToList(&$objForm, &$arg, $db, $Row) {

    //更新対象リストを作成する
    $query = knjz415Query::IndustryLcdList($Row["L_GROUPCD"]);
    $result = $db->query($query);
    $opt_left = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:220px\" width=\"220px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_left, $extra, 20);

    //大分類一覧リストを作成する//
    $query = knjz415Query::selectIndustryLcd($Row["L_GROUPCD"]);
    $result = $db->query($query);
    $opt_right = array();
    while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[] = array('label' => $row2["LABEL"],
                             'value' => $row2["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:220px\" width=\"220px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_right, $extra, 20);

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
function makeButton(&$objForm, &$arg) {
    //更新ボタン
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return doSubmit();\"");
    //削除ボタン
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", "onclick=\"return btn_submit('delete');\"");
    //取消ボタン
    $arg["button"]["btn_clear"]  = knjCreateBtn($objForm, "btn_clear", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"]    = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}
?>
