<?php

require_once('for_php7.php');


class knjp184kForm1
{
    function main(&$model){

        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //1:金額別, 2:生徒別
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //授業料期
        $query = knjp184kQuery::getExpenseM($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["EXPENSE_M_CD"], "EXPENSE_M_CD", $extra, 1, "");

        //popUpCalendar
        $setVal = strtr($model->field["PAID_MONEY_DATE"], "-", "/");
        $arg["data"]["PAID_MONEY_DATE"] = View::popUpCalendar($objForm, "PAID_MONEY_DATE", $setVal);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "CSV出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["start"]   = $objForm->get_start("knjp184kForm1", "POST", "knjp184kindex.php", "", "knjp184kForm1");
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp184kForm1.html", $arg); 
    }

}
//makeCmb
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
