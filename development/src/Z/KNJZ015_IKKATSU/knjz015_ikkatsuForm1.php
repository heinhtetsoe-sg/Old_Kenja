<?php

require_once('for_php7.php');

class knjz015_ikkatsuForm1 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz015_ikkatsuindex.php", "", "sel");
        //DB接続
        $db = Query::dbCheckOut();

        //校種コンボ
        $query = knjz015_ikkatsuQuery::getSchKind();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, "BLANK");

        /****************/
        /*リストtoリスト*/
        /****************/
        //プログラムID
        //左リスト(更新対象)
        $opt_left1 = array();
        $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move('right','prgId_input','prgId_delete',1)\"";
        $arg["main_part1"]["LEFT_PART"] = knjCreateCombo($objForm, "prgId_input", "left", $opt_left1, $extra, 15);
        //右リスト
        $opt_right1 = array();
        $query  = knjz015_ikkatsuQuery::selectRightList1();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right1[] = array('label' => $row["LABEL"],
                                  'value' => $row["VALUE"]);
        }
        $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move('left','prgId_input','prgId_delete',1)\"";
        $arg["main_part1"]["RIGHT_PART"] = knjCreateCombo($objForm, "prgId_delete", "right", $opt_right1, $extra, 15);

        //対応校種
        //左リスト(更新対象)
        $opt_left2 = array();
        $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move2('right')\"";
        $arg["main_part2"]["LEFT_PART"] = knjCreateCombo($objForm, "selKind_input", "left", $opt_left2, $extra, 7);
        //右リスト
        $opt_right2 = array();
        $query  = knjz015_ikkatsuQuery::selectRightList2();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right2[] = array('label' => $row["LABEL"],
                                  'value' => $row["VALUE"]);
        }
        $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move2('left')\"";
        $arg["main_part2"]["RIGHT_PART"] = knjCreateCombo($objForm, "selKind_delete", "right", $opt_right2, $extra, 7);
        $result->free();

        //プログラムID側の移動ボタン
        //全追加ボタンを作成する
        $extra = "onclick=\"return move('sel_add_all','prgId_input','prgId_delete',1);\"";
        $arg["main_part1"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all1", "≪", $extra);
        //追加ボタンを作成する
        $extra = "onclick=\"return move('left','prgId_input','prgId_delete',1);\"";
        $arg["main_part1"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add1", "＜", $extra);
        //削除ボタンを作成する
        $extra = "onclick=\"return move('right','prgId_input','prgId_delete',1);\"";
        $arg["main_part1"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del1", "＞", $extra);
        //全削除ボタンを作成する
        $extra = "onclick=\"return move('sel_del_all','prgId_input','prgId_delete',1);\"";
        $arg["main_part1"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all1", "≫", $extra);

        //対応校種側の移動ボタン
        //全追加ボタンを作成する
        $extra = "onclick=\"return move1('sel_add_all');\"";
        $arg["main_part2"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all2", "≪", $extra);
        //追加ボタンを作成する
        $extra = "onclick=\"return move1('left');\"";
        $arg["main_part2"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add2", "＜", $extra);
        //削除ボタンを作成する
        $extra = "onclick=\"return move1('right');\"";
        $arg["main_part2"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del2", "＞", $extra);
        //全削除ボタンを作成する
        $extra = "onclick=\"return move1('sel_del_all');\"";
        $arg["main_part2"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all2", "≫", $extra);

        //更新ボタンを作成する
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["btn_keep"] = knjCreateBtn($objForm, "btn_keep", "更 新", $extra);
        //戻るボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ015/knjz015index.php?cmd=edit";
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdata2");

        $arg["info"]    = array("LEFT_LIST"  => "更新対象",
                                "RIGHT_LIST" => "リスト"
                                );

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "sel") {
            $arg["reload"]  = "parent.left_frame.location.href=parent.left_frame.location.href;";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz015_ikkatsuForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
