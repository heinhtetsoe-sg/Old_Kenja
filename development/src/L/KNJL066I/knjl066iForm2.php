<?php
class knjl066iForm2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl066iindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && !VARS::get("chFlg")) {
            $Row = $db->getRow(knjl066iQuery::getRow($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        if ($model->groupdiv == "1") {
            $arg["jyuken"] = 1;
            $arg["mensetu"]  = "";
        } else {
            $arg["jyuken"] = "";
            $arg["mensetu"]  = 1;
        }

        //入試制度コンボ
        if (isset($model->warning) || $model->cmd == "change") {
            $applicantdiv = $model->field["APPLICANTDIV"];
        } else if ($model->cmd == "edit" || $model->cmd == "reset") {
            $applicantdiv = $model->applicantdiv;
        } else {
            $applicantdiv = "1";
        } 
        $query = knjl066iQuery::getNameMst($model->leftYear, "L003");
        $extra = " onchange=\"return btn_submit('change');\" ";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $applicantdiv, $extra, "1");

        //入試区分コンボ
        if (isset($model->warning) || $model->cmd == "change") {
            $testdiv = $model->field["TESTDIV"];
        } else if ($model->cmd == "edit" || $model->cmd == "reset") {
            $testdiv = $model->testdiv;
        } else {
            $testdiv = "1";
        } 
        $query = knjl066iQuery::getTestDiv($model, $applicantdiv);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $testdiv, $extra, "1");

        //班コード
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["GROUPCD"] = knjCreateTextBox($objForm, $Row["GROUPCD"], "GROUPCD", 15, 2, $extra);
        //班名称
        $extra = "";
        $arg["data"]["GROUPNAME"] = knjCreateTextBox($objForm, $Row["GROUPNAME"], "GROUPNAME", 40, 20, $extra);
        //班略称
        $extra = "";
        $arg["data"]["GROUPNAME_ABBV"] = knjCreateTextBox($objForm, $Row["GROUPNAME_ABBV"], "GROUPNAME_ABBV", 20, 10, $extra);
        //人数
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["GROUPPEOPLE"] = knjCreateTextBox($objForm, $Row["GROUPPEOPLE"], "GROUPPEOPLE", 5, 2, $extra);

        /********/
        /*ボタン*/
        /********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->left_frame);
        knjCreateHidden($objForm, "GROUPDIV", $model->groupdiv);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl066iindex.php?cmd=list&DIV={$model->div}';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl066iForm2.html", $arg); 
    }
}

/********************************************* 以下関数 *******************************************************/
//コンボ作成
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
        if ($name == 'APPLICANTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') {
                $value = $row["VALUE"];
            }
        }
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
