<?php

require_once('for_php7.php');

class knjz450Form2 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz450index.php", "", "edit");
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->qualified_cd)) {
            $query = knjz450Query::getQualifiedMst($model->qualified_cd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
            $Row["QUALIFIED_CD"]   = $model->field["QUALIFIED_CD"];
            $Row["QUALIFIED_NAME"] = $model->field["QUALIFIED_NAME"];
            $Row["QUALIFIED_ABBV"] = $model->field["QUALIFIED_ABBV"];
            $Row["PROMOTER"]       = $model->field["PROMOTER"];
        }

        /********************/
        /* テキストボックス */
        /********************/
        //資格コード
        $extra = "onblur=\"return Num_Check(this);\"";
        $arg["data"]["QUALIFIED_CD"] = knjCreateTextBox($objForm, $Row["QUALIFIED_CD"], "QUALIFIED_CD", 4, 4, $extra);
        //資格名称
        $extra = "";
        $arg["data"]["QUALIFIED_NAME"] = knjCreateTextBox($objForm, $Row["QUALIFIED_NAME"], "QUALIFIED_NAME", 80, 50, $extra);
        //資格略称
        $extra = "";
        $arg["data"]["QUALIFIED_ABBV"] = knjCreateTextBox($objForm, $Row["QUALIFIED_ABBV"], "QUALIFIED_ABBV", 50, 25, $extra);
        //先フラグ
        $extra = $Row["SAKI_FLG"] == "1" ? " checked " : "";
        $arg["data"]["SAKI_FLG"] = knjCreateCheckBox($objForm, "SAKI_FLG", "1", $extra);
        //主催
        $extra = "";
        $arg["data"]["PROMOTER"] = knjCreateTextBox($objForm, $Row["PROMOTER"], "PROMOTER", 80, 50, $extra);

        /********************/
        /* チェックボックス */
        /********************/
        //学校管理資格フラグ使用するか
        if ($model->Properties["useQualifiedManagementFlg"] == "1") {
            $arg["useQualifiedManagementFlg"] = "1";
        } else {
            $arg["useQualifiedManagementFlg"] = "";
        }
        //学校管理資格
        $checkd = ($Row["MANAGEMENT_FLG"] == "1") ? " checked": "";
        $extra = "id=\"MANAGEMENT_FLG\".$checkd";
        $arg["data"]["MANAGEMENT_FLG"] = knjCreateCheckBox($objForm, "MANAGEMENT_FLG", "1", $extra);


        /******************/
        /* コンボボックス */
        /******************/
        //設定区分
        $query = knjz450Query::getConditionDiv();
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["CONDITION_DIV"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["CONDITION_DIV"] = ($Row["CONDITION_DIV"] && $value_flg) ? $Row["CONDITION_DIV"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["CONDITION_DIV"] = knjCreateCombo($objForm, "CONDITION_DIV", $Row["CONDITION_DIV"], $opt, $extra, 1);

        /**********/
        /* ボタン */
        /**********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_upate"] = knjCreateBtn($objForm, "btn_upate", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリア
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz450index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz450Form2.html", $arg);
    }
}
?>
