<?php

require_once('for_php7.php');

class knjkncpyForm1 {
    function main(&$model) {

        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjkncpyindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //ボタン作成
        makeBtn($objForm, $arg, $ineiFlg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjkncpyForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $ineiFlg, $model)
{
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"deleteCookie(); closeWin();\"");

    //署名
    $extra = "onclick=\"return btn_submit('shomei');\"";
    $arg["button"]["btn_exe"] = knjCreateBtn($objForm, "btn_exe", "署名データ再生成", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
}
?>
