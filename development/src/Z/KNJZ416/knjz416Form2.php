<?php

require_once('for_php7.php');

class knjz416Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz416index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->m_groupcd) && ($model->cmd == "sel")) {
            $query = knjz416Query::getMgroupMst($model->m_groupcd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //中分類グループコードテキスト作成
        $extra = "onBlur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["M_GROUPCD"] = knjCreateTextBox($objForm, $Row["M_GROUPCD"], "M_GROUPCD", 2, 2, $extra);

        //中分類グループ名称テキスト作成
        $arg["data"]["M_GROUPNAME"] = knjCreateTextBox($objForm, $Row["M_GROUPNAME"], "M_GROUPNAME", 60, 60, "");

        //大分類コンボ作成
        $query = knjz416Query::IndustryLcd();
        $extra = "onchange=\"return btn_submit('sel'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "INDUSTRY_LCD", $model->field["INDUSTRY_LCD"], $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $Row);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (isset($model->message)) { //更新できたら左のリストを再読込
            $arg["reload"] = "window.open('knjz416index.php?cmd=list&init=1', 'left_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz416Form2.html", $arg); 
    }
}

function makeListToList(&$objForm, &$arg, $db, $model, $Row) {
    //更新対象リストを作成する
    $query = knjz416Query::IndustryMcdList($model->field["INDUSTRY_LCD"], $Row["M_GROUPCD"]);
    $result = $db->query($query);
    $opt_left = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:220px\" width=\"220px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_left, $extra, 20);

    //中分類一覧リストを作成する//
    $query = knjz416Query::selectIndustryMcd($Row["M_GROUPCD"], $model->field["INDUSTRY_LCD"]);
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

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
