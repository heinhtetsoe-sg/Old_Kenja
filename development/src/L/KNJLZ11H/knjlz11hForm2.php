<?php
class knjlz11hForm2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjlz11hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && !VARS::get("chFlg") && $model->qualifiedJudgeCd) {
            $Row = $db->getRow(knjlz11hQuery::getRow($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //資格
        $query = knjlz11hQuery::getEntexamSettingMst($model, "L026", $model->qualifiedCd);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["SHIKAKU_NAME"] = $row['NAME1'];
        //入試資格判定ＣＤ
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["QUALIFIED_JUDGE_CD"] = knjCreateTextBox($objForm, $Row["QUALIFIED_JUDGE_CD"], "QUALIFIED_JUDGE_CD", 2, 2, $extra);
        //入試資格名称
        $extra = "";
        $arg["data"]["QUALIFIED_NAME"] = knjCreateTextArea($objForm, "QUALIFIED_NAME", 4, 70, "soft", $extra, $Row["QUALIFIED_NAME"]);
        //入試資格略称
        $extra = "";
        $arg["data"]["QUALIFIED_ABBV"] = knjCreateTextBox($objForm, $Row["QUALIFIED_ABBV"], "QUALIFIED_ABBV", 22, 20, $extra);
        //加点
        $extra = "";
        $arg["data"]["PLUS_POINT"] = knjCreateTextBox($objForm, $Row["PLUS_POINT"], "PLUS_POINT", 3, 3, $extra);

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
        knjCreateHidden($objForm, "QUALIFIED_CD", $model->qualifiedCd);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjlz11hindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjlz11hForm2.html", $arg); 
    }
}
?>
