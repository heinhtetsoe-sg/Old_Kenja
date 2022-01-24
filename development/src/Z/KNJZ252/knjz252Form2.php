<?php

require_once('for_php7.php');

class knjz252Form2 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz252index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->Properties["unUsePrgStampInei"] == "1") {
            $arg["unUseInei"] = "1";
        } else {
            $arg["useInei"] = "1";
        }

        //右側の読込が早いと校種の値がセットされていない場合がある為、初期値設定しておく
        if (!$model->schoolKind) {
            //校種コンボ
            $query = knjz252Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('list');\"";
            makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");
        }

        //警告メッセージを表示しない場合
        $setData = array();
        $setData["PROGRAMID"] = $model->field["R_PROGRAMID"];
        for ($seqCnt = 1; $seqCnt <= $model->maxSeq; $seqCnt++) {
            $setField["SEQ"]       = $seqCnt;
            $setField["TITLE"]     = ($model->cmd == "reset") ? "" : $model->field["R_TITLE".$seqCnt];
            $setField["FILE_NAME"] = ($model->cmd == "reset") ? "" : $model->field["R_FILE_NAME".$seqCnt];
            $setData["DATA"][]     = $setField;
        }
        if ($model->prgId && (!isset($model->warning) || $model->cmd == "reset")) {
            //データを取得
            $query = knjz252Query::ReadQuery($model, $model->prgId);
            $result = $db->query($query);
            $dataCntArray = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setData["PROGRAMID"] = $row["PROGRAMID"];
                $setData["DATA"][$row["SEQ"] - 1] = $row;
            }
            $result->free();
        }

        $optStaff   = array();
        $optStaff[] = array('label' => "", 'value' => "");
        $query      = knjz252Query::getIneiStaff($model);
        $result     = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optStaff[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        }
        //プログラムID
        $extra = " onblur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["R_PROGRAMID"] = knjCreateTextBox($objForm, $setData["PROGRAMID"], "R_PROGRAMID", 20, 20, $extra);

        foreach ($setData["DATA"] as $key => $val) {

            $setMeiseai["R_SEQ"] = $val["SEQ"];

            //印影欄表示名称
            $extra = "";
            $setMeiseai["R_TITLE"] = knjCreateTextBox($objForm, $val["TITLE"], "R_TITLE".$val["SEQ"], 20, 20, $extra);

            //印影職員番号
            $extra = "";
            $value = $val["FILE_NAME"] ? $val["FILE_NAME"] : $opt[0]["value"];
            $setMeiseai["R_FILE_NAME"] = knjCreateCombo($objForm, "R_FILE_NAME".$val["SEQ"], $value, $optStaff, $extra, 1);


            $arg["meisai"][] = $setMeiseai;
        }

        //追加ボタンを作成する
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjz252index.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz252Form2.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
