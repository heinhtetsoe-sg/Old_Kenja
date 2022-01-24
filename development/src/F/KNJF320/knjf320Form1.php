<?php

require_once('for_php7.php');

class knjf320Form1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjf320index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ作成
        $query = knjf320Query::getYear();
        $extra = "onchange=\"return btn_submit('year');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);
        
        //日付一覧取得
        $result = $db->query(knjf320Query::getList($model)); 
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["WORK_DATE"] = str_replace("-", "/", $row["WORK_DATE"]);
            $arg["data"][] = $row;
        }
        $result->free();

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "year"){
            $arg["reload"] = "window.open('knjf320index.php?cmd=edit&YEAR={$model->year}','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf320Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
