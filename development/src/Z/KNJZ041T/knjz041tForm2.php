<?php

require_once('for_php7.php');

class knjz041tForm2 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $db = Query::dbCheckOut();
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz041tindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $query = knjz041tQuery::getRow($model->majorlcd,$model->majorscd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

/********************************************************************************************/
/********************************************************************************************/
/*******                    *****************************************************************/
/******* ENTEXAM_MAJORL_MST *****************************************************************/
/*******                    *****************************************************************/
/********************************************************************************************/
/********************************************************************************************/
        //大学科コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MAJORLCD"] = knjCreateTextBox($objForm, $model->majorlcd, "MAJORLCD", 2, 2, $extra);
        //大学科名称
        $extra = "";
        $arg["data"]["MAJORLNAME"] = knjCreateTextBox($objForm, $Row["MAJORLNAME"], "MAJORLNAME", 40, 20, $extra);
        //大学科略称
        $extra = "";
        $arg["data"]["MAJORLABBV"] = knjCreateTextBox($objForm, $Row["MAJORLABBV"], "MAJORLABBV", 4, 2, $extra);

/********************************************************************************************/
/********************************************************************************************/
/*******                    *****************************************************************/
/******* ENTEXAM_MAJORS_MST *****************************************************************/
/*******                    *****************************************************************/
/********************************************************************************************/
/********************************************************************************************/
        //小学科コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MAJORSCD"] = knjCreateTextBox($objForm, $model->majorscd, "MAJORSCD", 1, 1, $extra);
        //小学科名称
        $extra = "";
        $arg["data"]["MAJORSNAME"] = knjCreateTextBox($objForm, $Row["MAJORSNAME"], "MAJORSNAME", 40, 20, $extra);
        //小学科略称
        $extra = "";
        $arg["data"]["MAJORSABBV"] = knjCreateTextBox($objForm, $Row["MAJORSABBV"], "MAJORSABBV", 4, 2, $extra);
        //(賢者)学科コード
        $opt = array();
        $opt[] = array('label' => '',
                       'value' => '');
        $value_flg = false;
        $query = knjz041tQuery::getMajorcd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["MAIN_MAJORCD"] == $row["VALUE"]) $value_flg = true;
        }
        $Row["MAIN_MAJORCD"] = ($Row["MAIN_MAJORCD"] && $value_flg) ? $Row["MAIN_MAJORCD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["MAIN_MAJORCD"] = knjCreateCombo($objForm, "MAIN_MAJORCD", $Row["MAIN_MAJORCD"], $opt, $extra, 1);

        /**********/
        /* ボタン */
        /**********/
        //更新
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリア
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz041tindex.php?cmd=list"
                            . "&year=" .$model->year."';";
        }

        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz041tForm2.html", $arg);
    }
}
?>
