<?php

require_once('for_php7.php');


class knjf332fixedForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjf332fixedForm1", "POST", "knja110aindex.php", "", "knjf332fixedForm1");

        //変更開始日付
        $arg["data"]["FIXED_DATE"] = View::popUpCalendar($objForm, "FIXED_DATE", str_replace("-","/",CTRL_DATE),"");

        //DB接続
        $db = Query::dbCheckOut();

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf332fixedForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //更新
    $extra = "onclick=\"return btn_submit('fixedUpd');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "確 定", $extra);

    //終了
    $extra = "onclick=\"return btn_submit('subEnd');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "キャンセル", $extra);
}
?>
