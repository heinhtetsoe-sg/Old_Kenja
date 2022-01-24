<?php

require_once('for_php7.php');

class knjp091kForm1
{
    function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp091kindex.php", "", "edit");

        //DB接続
        $db     = Query::dbCheckOut();

        $query = knjp091kQuery::getYear($model);
        $extra = "onChange=\"return btn_submit('list')\"";
        makeCmb($objForm, $arg, $db, $query, "EXE_YEAR", $model->exe_year, $extra, 1);

        //出身学校一覧取得
        $query  = knjp091kQuery::selectQuery($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             //レコードを連想配列のまま配列$arg[data]に追加していく。 
             array_walk($row, "htmlspecialchars_array");
             $row["PAID_MONEY_DATE"] = str_replace("-", "/", $row["PAID_MONEY_DATE"]);
             $row["REPLACE_MONEY_DATE"] = str_replace("-", "/", $row["REPLACE_MONEY_DATE"]);
             $arg["data"][] = $row; 
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp091kForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "EXE_YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
