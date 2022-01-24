<?php

require_once('for_php7.php');


class knjf342fixedForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjf342fixedForm1", "POST", "knjf342index.php", "", "knjf342fixedForm1");

        //変更開始日付
        $arg["data"]["FIXED_DATE"] = View::popUpCalendar($objForm, "FIXED_DATE", str_replace("-", "/", CTRL_DATE), "");

        //DB接続
        $db = Query::dbCheckOut();

        //更新ボタン
        $extra = "onclick=\"return btn_submit('fixedUpd');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "確 定", $extra);

        //終了ボタン
        $extra = "onclick=\"return btn_submit();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "キャンセル", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf342fixedForm1.html", $arg);
    }
}
