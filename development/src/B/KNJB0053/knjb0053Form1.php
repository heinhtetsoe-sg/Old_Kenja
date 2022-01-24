<?php

require_once('for_php7.php');

class knjb0053Form1
{
    function main(&$model)
    {
    $objForm = new form;

    $db = Query::dbCheckOut();
    //フォーム作成
    $arg["start"]   = $objForm->get_start("list", "POST", "knjb0053index.php", "", "edit");

    //年度コンボ作成
    $query = knjb0053Query::getYear();
    $extra = "onchange=\"return btn_submit('change');\"";
    makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

    //コピーボタンを作成する
    $extra = "style=\"width:130px\" onclick=\"return btn_submit('copy');\"";
    $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

    //リスト内表示
    $query  = knjb0053Query::getList($model);
    $result = $db->query($query);

    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
         //レコードを連想配列のまま配列$arg[data]に追加していく。
         array_walk($row, "htmlspecialchars_array");
         //リンク作成
         $row["RIREKI_CODE"] = View::alink("knjb0053index.php", $row["RIREKI_CODE"], "target=\"right_frame\"",
                                          array("cmd"  =>"edit",
                                                "YEAR"   =>$row["YEAR"],
                                                "RIREKI_CODE" =>$row["RIREKI_CODE"]
                                                ));
                                                
        
         $row["SELECT_DATE"] = str_replace("-","/",$row["SELECT_DATE"]);
            
         $arg["data"][] = $row;
    }
    $result->free();

    //hidden
    knjCreateHidden($objForm, "cmd");

    if ($model->cmd == "change"){
        $arg["reload"] = "window.open('knjb0053index.php?cmd=edit','right_frame');";
    }
    $arg["finish"]  = $objForm->get_finish();
    
    Query::dbCheckIn($db);

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjb0053Form1.html", $arg);
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
    $value = ($value && $value_flg) ? $value : CTRL_YEAR+1;

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
