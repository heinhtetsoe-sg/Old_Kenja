<?php
class knjlz01hForm2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjlz01hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && !VARS::get("chFlg") && $model->testdiv) {
            $Row = $db->getRow(knjlz01hQuery::getRow($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //入試区分コード
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["TESTDIV"] = knjCreateTextBox($objForm, $Row["TESTDIV"], "TESTDIV", 2, 2, $extra);

        //入試区分名称
        $extra = "";
        $arg["data"]["TESTDIV_NAME"] = knjCreateTextBox($objForm, $Row["TESTDIV_NAME"], "TESTDIV_NAME", 22, 20, $extra);

        //入試区分略称
        $extra = "";
        $arg["data"]["TESTDIV_ABBV"] = knjCreateTextBox($objForm, $Row["TESTDIV_ABBV"], "TESTDIV_ABBV", 8, 6, $extra);

        //出願条件　評定合計
        $extra = "";
        $arg["data"]["CONDITION_HYOTEI"] = knjCreateTextBox($objForm, $Row["CONDITION_HYOTEI"], "CONDITION_HYOTEI", 3, 3, $extra);

        //出願条件５教科合計
        $extra = "";
        $arg["data"]["CONDITION_GOKYOKA"] = knjCreateTextBox($objForm, $Row["CONDITION_GOKYOKA"], "CONDITION_GOKYOKA", 3, 3, $extra);

        //募集人数 男子
        $extra = "";
        $arg["data"]["CAPACITY_MALE"] = knjCreateTextBox($objForm, $Row["CAPACITY_MALE"], "CAPACITY_MALE", 3, 3, $extra);

        //募集人数 女子
        $extra = "";
        $arg["data"]["CAPACITY_FEMALE"] = knjCreateTextBox($objForm, $Row["CAPACITY_FEMALE"], "CAPACITY_FEMALE", 3, 3, $extra);

        //試験日
        $arg["data"]["TEST_DATE"] = View::popUpCalendar2($objForm, "TEST_DATE", str_replace("-","/",$Row["TEST_DATE"]), "", "", "");

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
        knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjlz01hindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjlz01hForm2.html", $arg); 
    }
}
?>
