<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：委員会コード表示を変更                   山城 2004/11/26 */
/********************************************************************/

class knjj080_2Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjj080_2index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning))
        {
            $query = knjj080_2Query::getRow($model, $model->committeecd, $model->committee_flg);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if (!$model->schKind) {
                //校種コンボ
                $query = knjj080_2Query::getSchkind($model);
                $extra = "";
                makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schKind, $extra, 1, "");
            }
        }

        //委員会区分コンボ
        $opt = array();
        $value_flg = false;
        $query = knjj080_2Query::getCommitteeFlg();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["COMMITTEE_FLG"] = knjCreateCombo($objForm, "COMMITTEE_FLG", $Row["COMMITTEE_FLG"], $opt, $extra, 1);

        //委員会コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["COMMITTEECD"] = knjCreateTextBox($objForm, $Row["COMMITTEECD"], "COMMITTEECD", 4, 4, $extra);

        //委員会名称
        $extra = "";
        $arg["data"]["COMMITTEENAME"] = knjCreateTextBox($objForm, $Row["COMMITTEENAME"], "COMMITTEENAME", 30, 45, $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $link = REQUESTROOT."/J/KNJJ080/knjj080index.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjj080_2index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj080_2Form2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
