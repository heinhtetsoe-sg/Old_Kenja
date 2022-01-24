<?php

require_once('for_php7.php');

class knjg084Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("sel", "POST", "knjg084index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //年組
        $query = knjg084Query::getAuth($model);
        $extra = "onChange=\"btn_submit('change2')\";";
        makeCmb($objForm, $arg, $db, $query, "SEL_GRADECLASS", $model->field["GRADE_CLASS"], $extra, 1, "", "全て");
        
        //タイトル設定
        makeTitle($arg, $db);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, "STUDENT", "0", 15, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (isset($model->message)) {   //更新できたら左のリストを再読込
            $arg["reload"] = "window.open('knjg084index.php?cmd=list&init=1', 'left_frame')";
        }

        View::toHTML($model, "knjg084Form2.html", $arg); 
    }
}

//タイトル設定
function makeTitle(&$arg, $db) {
    $arg["info"]    = array("LEFT_LIST"     => "生徒対象者一覧",
                            "RIGHT_LIST"    => "生徒一覧" );
}

function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $BLANK="", $selstr="") {
    $opt = array();
    $value_flg = false;
    if ($BLANK) $opt[] = array('label' => $selstr, 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $name, $target, $size, $model) {
    $opt_left = $opt_right = array();
    //右側
    $resultlist = $db->query(knjg084Query::getStudent($model));
    while ($row = $resultlist->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[] = array("label" => $row["LABEL"],
                             "value" => $row["VALUE"]);
    }
    $resultlist->free();
    
    //左側
    $result = $db->query(knjg084Query::getStudent_left($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[]  = array("label" => $row["LABEL"], 
                             "value" => $row["VALUE"]);
    }
    $result->free();

    //対象コース
    $extra = "multiple STYLE=\"width:500px\" width:\"500px\" ondblclick=\"move('right', '".$name."')\"";
    $arg["main_part"][$name."LEFT_PART"]   = knjCreateCombo($objForm, "L".$name, "right", $opt_left, $extra, $size);

    //コース一覧
    $extra = "multiple STYLE=\"width:500px\" width:\"500px\" ondblclick=\"move('left', '".$name."')\"";
    $arg["main_part"][$name."RIGHT_PART"]  = knjCreateCombo($objForm, "R".$name, "left", $opt_right, $extra, $size);

    // << ボタン作成
    $extra = "style=\"height:40px;width:40px\" onclick=\"return moves('sel_add_all', '".$name."');\"";
    $arg["main_part"][$name."SEL_ADD_ALL"] = knjCreateBtn($objForm, $name."sel_add_all", "≪", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:40px;width:40px\" onclick=\"return move('left', '".$name."');\"";
    $arg["main_part"][$name."SEL_ADD"]     = knjCreateBtn($objForm, $name."sel_add", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:40px;width:40px\" onclick=\"return move('right', '".$name."');\"";
    $arg["main_part"][$name."SEL_DEL"]     = knjCreateBtn($objForm, $name."sel_del", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:40px;width:40px\" onclick=\"return moves('sel_del_all', '".$name."');\"";
    $arg["main_part"][$name."SEL_DEL_ALL"] = knjCreateBtn($objForm, $name."sel_del_all", "≫", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return doSubmit('update');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdata2");
}
?>
