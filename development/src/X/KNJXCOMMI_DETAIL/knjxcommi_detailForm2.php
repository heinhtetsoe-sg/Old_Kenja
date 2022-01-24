<?php

require_once('for_php7.php');

class knjxcommi_detailForm2
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjxcommi_detailindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報表示
        $arg["SCHINFO"] = $db->getOne(knjxcommi_detailQuery::getSchinfo($model));

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->seq && $model->date && $model->detail_seq) {
            $query = knjxcommi_detailQuery::getRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //委員会コンボ作成
        $query = knjxcommi_detailQuery::getCommitteeName($model);
        makeCmb($objForm, $arg, $db, $query, "SEQ", $Row["SEQ"], "", 1, $model);

        //日付作成
        $Row["DETAIL_DATE"] = ($Row["DETAIL_DATE"]) ? $Row["DETAIL_DATE"] : CTRL_DATE;
        $arg["data"]["DETAIL_DATE"] = View::popUpCalendar($objForm, "DETAIL_DATE", str_replace("-", "/", $Row["DETAIL_DATE"]));

        //記録備考テキストボックス
        $arg["data"]["DETAIL_REMARK"] = knjCreateTextBox($objForm, $Row["DETAIL_REMARK"], "DETAIL_REMARK", 30, 15, "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjxcommi_detailindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjxcommi_detailForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "SEQ") {
            $row["LABEL"] = (!$row["COMMITTEENAME"] && !$row["CHARGENAME"]) ? '' : $row["COMMITTEENAME"].$row["CHARGENAME"];
        }

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    if (common::SecurityCheck(STAFFCD, $model->programid) < DEF_UPDATE_RESTRICT) {
        //追加ボタン
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", "disabled");
        //修正ボタン
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "disabled");
        //削除ボタン
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", "disabled");
    } else {
        //追加ボタン
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", " onclick=\"return btn_submit('add');\"");
        //修正ボタン
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");
        //削除ボタン
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", "onclick=\"return btn_submit('delete');\"");
    }
    //取消ボタン
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DETAIL_SEQ", $model->detail_seq);
}
