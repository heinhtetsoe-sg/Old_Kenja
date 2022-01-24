<?php

require_once('for_php7.php');

class knjz203Form2 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
           $arg["jscript"] = "OnAuthError();";
        }
        $db = Query::dbCheckOut();
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz203index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz203Query::getRow($model, $db);
        }else{
            $Row =& $model->field;
        }

       //デフォルト値
        if($model->coursecode =="") {
            $query = knjz203Query::getFirst_CouseKey($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $model->coursecode = $row["COURSECODE"];
            $model->coursecd   = $row["COURSECD"];
            $model->majorcd    = $row["MAJORCD"];
            $model->grade      = $row["GRADE"];
            $model->coursename="";
        }

        //科目コンボ設定
        $opt       = array();
        $result    = $db->query(knjz203Query::getAttendSubclassSpecialMst($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["SPECIAL_GROUP_CD"]." ".$row["SPECIAL_GROUP_NAME"],
                           "value" => $row["SPECIAL_GROUP_CD"]);
        }
        $result->free();

        //科目コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "SPECIAL_GROUP_CD",
                            "size"        => "1",
                            "value"       => $Row["SPECIAL_GROUP_CD"],
                            "options"     => $opt ));
        $arg["data"]["SPECIAL_GROUP_CD"] = $objForm->ge("SPECIAL_GROUP_CD");

        /********************/
        /* テキストボックス */
        /********************/
        //特活上限値
        $extra = "onblur=\"this.value=toNumber(this.value)\"";
        $arg["data"]["ABSENCE_HIGH"] = knjCreateTextBox($objForm, $Row["ABSENCE_HIGH"], "ABSENCE_HIGH", 5, 4, $extra);
        //欠課数オーバー
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ABSENCE_WARN"] = knjCreateTextBox($objForm, $Row["ABSENCE_WARN"], "ABSENCE_WARN", 2, 2, $extra);
        //欠課数オーバー
        $arg["data"]["ABSENCE_WARN2"] = knjCreateTextBox($objForm, $Row["ABSENCE_WARN2"], "ABSENCE_WARN2", 2, 2, $extra);
        //欠課数オーバー
        $arg["data"]["ABSENCE_WARN3"] = knjCreateTextBox($objForm, $Row["ABSENCE_WARN3"], "ABSENCE_WARN3", 2, 2, $extra);

        //欠課数オーバーのタイトル
        if (in_array("1", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN"]  = $model->control["学期名"]["1"];
        }
        if (in_array("2", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN2"] = $model->control["学期名"]["2"];
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN3"] = $model->control["学期名"]["3"];
        }

        //授業時数のフラグ  特活上限値の出力の判定に使う
        $jugyou_jisu_flg = $db->getOne(knjz203Query::getJugyouJisuFlg());   //1:法定授業 2:実授業
        if ($jugyou_jisu_flg != "2") {
            $arg["title"]["ABSENCE_HIGH"] = "1";
        }

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
        if ($jugyou_jisu_flg == "2") {
            knjCreateHidden($objForm, "ABSENCE_HIGH", $Row["ABSENCE_HIGH"]);
        }

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjz203index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz203Form2.html", $arg);
    }
}
?>
