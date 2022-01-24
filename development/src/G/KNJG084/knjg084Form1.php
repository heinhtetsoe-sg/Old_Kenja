<?php

require_once('for_php7.php');

class knjg084Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjg084index.php", "", "list");

        //DB接続
        $db = Query::dbCheckOut();

        //校種
        $query = knjg084Query::getSchkind($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1, "全て", "ALL");

        //非送付者リスト
        makeNotSenderList($arg, $db, $model);

        //ボタン
        $arg["button"]["btn_copylastyear"] = knjCreateBtn($objForm, "btn_copylastyear", "前年度コピー", "onclick=\"return btn_submit('copylastyear');\"");

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        if ($model->cmd == "change" && !$model->isWarning()){
            $arg["reload"] = "parent.right_frame.btn_submit('left');";
            $model->cmd = "change";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjg084Form1.html", $arg);
    }
}

function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $BLANK=null, $selval="") {
    $opt = array();
    $value_flg = false;
    if (!is_null($BLANK)) $opt[] = array('label' => $BLANK, 'value' => $selval);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["head"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//非送付者リスト
function makeNotSenderList(&$arg, $db, $model) {
    $g_cnt = 1;
    if (!isset($arg["data"])) {
        $arg["data"] = array();
    }
    $query = knjg084Query::getEntryList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //行数取得
        $group_cnt  = $row["CNT"];
        if ($g_cnt == 1) $row["ROWSPAN1"] = $group_cnt;     //グループの行数

        $arg["data"][] = $row;

        if ($g_cnt == $group_cnt) {
            $g_cnt = 1;
        } else {
            $g_cnt++;
        }
    }
    $result->free();

}

?>
