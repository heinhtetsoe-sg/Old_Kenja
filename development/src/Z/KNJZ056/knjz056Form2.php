<?php

require_once('for_php7.php');

class knjz056Form2 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz056index.php", "", "edit");
        $db = Query::dbCheckOut();
        
        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->coursecd) && isset($model->majorcd) && $model->cmd != "course") {
            $query = knjz056Query::getMajorCategoryDat($model->coursecd,$model->majorcd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }
        /******************/
        /* コンボボックス */
        /******************/
        
        //課程コード
        $query = knjz056Query::getCourse();
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["COURSECD"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["COURSECD"] = ($Row["COURSECD"] && $value_flg) ? $Row["COURSECD"] : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('course');\"";
        $arg["data"]["COURSECD"] = knjCreateCombo($objForm, "COURSECD", $Row["COURSECD"], $opt, $extra, 1);
        
        //学科コード
        $query = knjz056Query::getMajor($Row["COURSECD"]);
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["MAJORCD"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["MAJORCD"] = ($Row["MAJORCD"] && $value_flg) ? $Row["MAJORCD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["MAJORCD"] = knjCreateCombo($objForm, "MAJORCD", $Row["MAJORCD"], $opt, $extra, 1);
        
        //学科分類
        $query = knjz056Query::getMajorName();
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["CATEGORYCD"] == $row["VALUE"]) $value_flg = true;
        }
        $value = ($Row["CATEGORYCD"] && $value_flg) ? $Row["CATEGORYCD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["CATEGORYCD"] = knjCreateCombo($objForm, "CATEGORYCD", $Row["CATEGORYCD"], $opt, $extra, 1);
        
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
            $arg["reload"]  = "parent.left_frame.location.href='knjz056index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz056Form2.html", $arg);
    }
}
?>
