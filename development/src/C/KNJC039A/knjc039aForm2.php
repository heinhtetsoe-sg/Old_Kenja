<?php

require_once('for_php7.php');

class knjc039aForm2 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
           $arg["jscript"] = "OnAuthError();";
        }
        $db = Query::dbCheckOut();
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjc039aindex.php", "", "edit");
        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjc039aQuery::getRow($model, $db);
        }else{
            $Row =& $model->field;
        }

        //校種コンボ設定
        $opt = array();
        $value_flg = false;
        $value = $Row["SCHOOL_KIND"];
        $query = knjc039aQuery::getSchoolKind($model);
        $result = $db->query($query);
        $opt[0]["value"] = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["VALUE"].":".$row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";

        $arg["data"]["SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $value, $opt, $extra, 1);

        //集計コード設定
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["COLLECTION_CD"] = knjCreateTextBox($objForm, $Row["COLLECTION_CD"], "COLLECTION_CD", 3, 2, $extra);

        //集計単位名
        $extra = "";
        $arg["data"]["COLLECTION_NAME"] = knjCreateTextBox($objForm, $Row["COLLECTION_NAME"], "COLLECTION_NAME", 10, 15 , $extra);

        //集計単位
        $arg["data"]["FROM_DATE"]     = View::popUpCalendar($objForm, "FROM_DATE", str_replace("-","/",$Row["FROM_DATE"]),"");
        $arg["data"]["TO_DATE"]       = View::popUpCalendar($objForm, "TO_DATE", str_replace("-","/",$Row["TO_DATE"]),"");

        //通知表出力の学期コンボ
        $opt = array();
        $value_flg = false;
        $value = $Row["SEMESTER"];
        $query = knjc039aQuery::getSemester();
        $result = $db->query($query);
        $opt[] = array('label' => "",
                       'value' => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";

        $arg["data"]["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $value, $opt, $extra, 1);

        /**********/
        /* ボタン */
        /**********/
        //追加ボタンを作成する
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"]    = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタンを作成する
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"]    = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリアボタンを作成する
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"]  = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"]   = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjc039aindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc039aForm2.html", $arg);
    }
}

?>
