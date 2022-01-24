<?php

require_once('for_php7.php');

class knjz093Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz093index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //校種コンボ
        $extra = "onChange=\"return btn_submit('changeType')\"";
        $query = knjz093Query::getNameMst('L019');
        makeCmb($objForm, $arg, $db, $query, "SELECT_FINSCHOOL_TYPE", $model->selectFinschoolType, $extra, 1, "ALL");

        //出身学校一覧取得
        $query  = knjz093Query::selectQuery($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            if ($row["FINSCHOOLCD"] == $model->finschoolcd) {
                $row["FINSCHOOL_TYPE"] = ($row["FINSCHOOL_TYPE"]) ? $row["FINSCHOOL_TYPE"] : "　";
                $row["FINSCHOOL_TYPE"] = "<a name=\"target\">{$row["FINSCHOOL_TYPE"]}</a><script>location.href='#target';</script>";
            }
            $arg["data"][] = $row; 
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "list"){
            $arg["reload"] = "parent.right_frame.location.href='knjz093index.php?cmd=new';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz093Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $all) {
    $opt = array();
    $opt[] = array('label' => '', 'value' => '');
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($all == "ALL") {
        $opt[] = array('label' => '-- 全て --', 'value' => '99');
        if ($value == "99") $value_flg = true;
    }

    $value = (($value === '0' || $value) && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
