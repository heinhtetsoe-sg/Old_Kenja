<?php

require_once('for_php7.php');


class knjm312Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm312Form1", "POST", "knjm312index.php", "", "knjm312Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //科目コンボ
        $query = knjm312Query::getSubclass($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        //日付チェック最終日
        knjCreateHidden($objForm, "CHECK_LAST_DAY", CTRL_DATE);
        //日付チェック初日
        $query = knjm312Query::getCheckSdate();
        $checkFirstDate = $db->getOne($query);
        knjCreateHidden($objForm, "CHECK_FIRST_DAY", $checkFirstDate);
        //最終提出日
        list($year, $month, $day) = preg_split("/-/", CTRL_DATE);
        $systemDate = date("Y-m-d", mktime(0, 0, 0, $month, $day - 3, $year));
        $model->field["DEADLINE_DATE"] = str_replace("-", "/", $systemDate);
        $arg["data"]["DEADLINE_DATE"] = View::popUpCalendar($objForm, "DEADLINE_DATE", str_replace("-", "/", $model->field["DEADLINE_DATE"]));

        //登録ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '登 録', $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, 'cmd');

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm312Form1.html", $arg);
    }
}

function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
