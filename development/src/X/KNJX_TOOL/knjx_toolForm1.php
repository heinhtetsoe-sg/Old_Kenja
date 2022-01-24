<?php

require_once('for_php7.php');

class knjx_toolForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjx_toolindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();
        //ボタン作成
        makeButton($objForm, $arg, $model, $db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]    = $objForm->get_finish();

        View::toHTML($model, "knjx_toolForm1.html", $arg);
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model, $db)
{
    //学籍
    $extra = "onclick=\"return btn_submit('regd');\"";
    $arg["btn_regd"] = knjCreateBtn($objForm, "btn_regd", "更 新", $extra);

    //試験（定期考査）
    $extra = "onclick=\"return btn_submit('score1');\"";
    $arg["btn_score1"] = knjCreateBtn($objForm, "btn_score1", "更 新", $extra);

    //試験（実力試験）
    $extra = "onclick=\"return btn_submit('score2');\"";
    $arg["btn_score2"] = knjCreateBtn($objForm, "btn_score2", "更 新", $extra);

    //試験（外部模試）
    $extra = "onclick=\"return btn_submit('score4');\"";
    $arg["btn_score4"] = knjCreateBtn($objForm, "btn_score4", "更 新", $extra);

    //試験（センター自己採点）
    $extra = "onclick=\"return btn_submit('score5');\"";
    $arg["btn_score5"] = knjCreateBtn($objForm, "btn_score5", "更 新", $extra);

    //出欠
    $extra = "onclick=\"return btn_submit('attend');\"";
    $arg["btn_attend"] = knjCreateBtn($objForm, "btn_attend", "更 新", $extra);

    //評定
    $extra = "onclick=\"return btn_submit('record');\"";
    $arg["btn_record"] = knjCreateBtn($objForm, "btn_record", "更 新", $extra);

    //大学合格
    $extra = "onclick=\"return btn_submit('grad');\"";
    $arg["btn_grad"] = knjCreateBtn($objForm, "btn_grad", "更 新", $extra);

    //ユーザー
    $extra = "onclick=\"return btn_submit('user');\"";
    $arg["btn_user"] = knjCreateBtn($objForm, "btn_user", "更 新", $extra);


    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}



?>
