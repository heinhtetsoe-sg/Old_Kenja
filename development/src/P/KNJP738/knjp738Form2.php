<?php

require_once('for_php7.php');

class knjp738Form2 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knjp738index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージが表示される場合
        if (isset($model->warning)) {
            $Row =& $model->field;
        } else {
            $query = knjp738Query::getRow($model, 1);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        $model->year       = ($model->year)       ? $model->year      : CTRL_YEAR;
        $model->schoolkind = ($model->schoolkind) ? $model->schoolkind: SCHOOLKIND;

        //グループCD
        $readOnly = ($model->grp_cd) ? " readonly": "";
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["GRP_CD"] = knjCreateTextBox($objForm, $Row["GRP_CD"], "GRP_CD", 3, 3, $extra.$readOnly);

        //グループ名称
        $extra = "";
        $arg["data"]["GRP_NAME"] = knjCreateTextBox($objForm, $Row["GRP_NAME"], "GRP_NAME", 20, 20, $extra);

        //新規ボタン
        $extra = "onclick=\"return btn_submit('editNew');\"";
        $arg["button"]["btn_new"] = knjCreateBtn($objForm, "btn_new", "新 規", $extra);

        //リストTOリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //追加
        $extra = ($model->grp_cd) ? " disabled": "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = ($model->grp_cd) ? "onclick=\"return btn_submit('update');\"" : "disabled";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除
        $extra = ($model->grp_cd) ? "onclick=\"return btn_submit('delete');\"": "disabled";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        if ($model->cmd == "edit2" && !isset($model->warning)) {
            $arg["reload"] = "window.open('knjp738index.php?cmd=list','left_frame');";
        }

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjp738Form2.html", $arg);
    }
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, &$model) {
    $selectdata = (strlen($model->selectdata)) ? explode(",", $model->selectdata) : array();
    $opt_right = $opt_left = array();

    //項目一覧取得
    if ($model->year && $model->schoolkind) {
        $query = knjp738Query::getCollectM($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ((in_array($row["VALUE"], $selectdata) && isset($model->warning)) || (strlen($row["GRP_CD"] && !isset($model->warning)))) {
                $opt_left[]  = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            } else {
                $opt_right[] = array('label' => $row["LABEL"],
                                     'value' => $row["VALUE"]);
            }
        }
        $result->free();
    }

    //項目一覧（右リスト）
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["RIGHT_LIST"] = knjCreateCombo($objForm, "RIGHT_LIST", "", $opt_right, $extra, 20);

    //対象項目一覧（左リスト）
    $extra = "multiple style=\"width:100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["LEFT_LIST"] = knjCreateCombo($objForm, "LEFT_LIST", "", $opt_left, $extra, 20);

    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}
?>
