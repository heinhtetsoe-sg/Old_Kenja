<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knjm837Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjm837Form1", "POST", "knjm837index.php", "", "knjm837Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //ログイン年度
        $arg["data"]["CTRL_YEAR"] = CTRL_YEAR;

        //学期
        $opt = array();
        $query = knjm837Query::getSemeMst(CTRL_YEAR);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["GAKKI"] = ($model->field["GAKKI"]) ? $model->field["GAKKI"] : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('knjm837');\"";
        $arg["data"]["GAKKI"] = knjCreateCombo($objForm, "GAKKI", $model->field["GAKKI"], $opt, $extra, 1);

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjm837Form1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //CSVボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ", $extra);

    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJM837");
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "cmd");
}

?>
