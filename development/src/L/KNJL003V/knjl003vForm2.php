<?php

require_once('for_php7.php');

class knjl003vForm2
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl003vindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && !VARS::get("chFlg") && $model->placeId) {
            $query = knjl003vQuery::getRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        /******************/
        /*  テキスト作成  */
        /******************/
        //会場ID
        $extra = "onblur=\"this.value=toAlphaNumber(this.value)\"";
        $arg["data"]["PLACE_ID"] = knjCreateTextBox($objForm, $Row["PLACE_ID"], "PLACE_ID", 4, 4, $extra);

        //会場名
        $extra = "";
        $arg["data"]["PLACE_NAME"] = knjCreateTextBox($objForm, $Row["PLACE_NAME"], "PLACE_NAME", 40, 20, $extra);

        //収容人数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SEATS"] = knjCreateTextBox($objForm, $Row["SEATS"], "SEATS", 2, 2, $extra);

        /****************/
        /*  ボタン作成  */
        /****************/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);
        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        /****************/
        /*  hidden作成  */
        /****************/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl003vindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl003vForm2.html", $arg);
    }
}
