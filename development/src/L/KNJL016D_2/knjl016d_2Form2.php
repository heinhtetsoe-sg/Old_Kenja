<?php

require_once('for_php7.php');

class knjl016d_2Form2 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl016d_2index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->decisionCd)){
            $Row = $db->getRow(knjl016d_2Query::getDecisionData($model->decisionCd), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //内部判定コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["DECISION_CD"] = knjCreateTextBox($objForm, $Row["DECISION_CD"], "DECISION_CD", 2, 1, $extra);

        //内部判定名称
        $extra = "";
        $arg["data"]["DECISION_NAME"] = knjCreateTextBox($objForm, $Row["DECISION_NAME"], "DECISION_NAME", 81, 80, $extra);

        //ボタン作成
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
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //戻るボタンを作成する
        $link = REQUESTROOT."/L/KNJL016D/knjl016dindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjl016d_2index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl016d_2Form2.html", $arg);
    }
}
?>
