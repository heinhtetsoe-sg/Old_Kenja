<?php

require_once('for_php7.php');

class knjz040pForm1 {
    function main(&$model) {
        $arg["jscript"] = "";
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz040pindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == 'list2') {
            $arg["reload"][] = "parent.right_frame.location.href='knjz040pindex.php?cmd=edit2';";
        }

        $arg["TOP"]["YEAR"] = $model->year ."年度";

        //入試制度コンボ
        $query = knjz040pQuery::getNameMst($model->year, "L003", $model);
        $extra = "onChange=\"return btn_submit('list2')\"";
        makeCmb($objForm, $arg, $db, $query, $model->fields["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "TOP", "");

        //データ
        $result = $db->query(knjz040pQuery::hallList($model));
        $arg["data"] = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"][] = $row;
        }

        Query::dbCheckIn($db);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjz040pForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $argName = "", $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$argName][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
