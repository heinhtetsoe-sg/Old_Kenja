<?php

require_once('for_php7.php');

class knjl414Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl414index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //初期値セット
        if ($model->leftYear == "") {
            $model->leftYear = CTRL_YEAR + 1;
            $model->leftEventClassCd = $db->getOne(knjl414Query::getRecruitClass());
        }

        //警告メッセージを表示しない場合
        if ($model->cmd == "reset" || $model->cmd == "linkClick") {
            $query = knjl414Query::getRecruitSendYmst($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //イベントコンボ作成
        $query = knjl414Query::getEvent($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["EVENT_CD"], "EVENT_CD", $extra, 1);

        //案内コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SEND_CD"] = knjCreateTextBox($objForm, $Row["SEND_CD"], "SEND_CD", 3, 3, $extra);

        //案内名称
        $extra = "";
        $arg["data"]["SEND_NAME"] = knjCreateTextBox($objForm, $Row["SEND_NAME"], "SEND_NAME", 50, 80, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "linkClick"){
            $arg["reload"]  = "parent.left_frame.location.href='knjl414index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl414Form2.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //修正ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
