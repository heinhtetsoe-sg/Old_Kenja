<?php

require_once('for_php7.php');

class knjz015Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz015index.php", "", "edit");

        $db     = Query::dbCheckOut();

        if (!$model->isWarning()) {
            $row  = $db->getRow(knjz015Query::getRow($model),DB_FETCHMODE_ASSOC);
        } else {
            $row =& $model->field;
        }

        //一括更新ボタン
        $link = REQUESTROOT."/Z/KNJZ015_IKKATSU/knjz015_ikkatsuindex.php?cmd=sel";
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["btn_jump"] = knjCreateBtn($objForm, "btn_jump", "一括更新", $extra);

        //校種コンボ
        $query = knjz015Query::getSchKind();
        $model->field["SCHKIND"] = ($model->schkind == "") ? $model->field["SCHKIND"] : $row["SCHKIND"];
        $extra = "onchange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1, "BLANK");

        //プログラムIDコンボ
        $query = knjz015Query::getPrgId();
        $model->field["PRGIDLIST"] = ($model->prgId == "") ? $model->field["PRGIDLIST"] : $row["PROGRAMID"];
        $extra = "onchange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, "PRGIDLIST", $model->field["PRGIDLIST"], $extra, 1, "BLANK");

        /****************/
        /*リストtoリスト*/
        /****************/
        //左リスト
        $query       = knjz015Query::selectLeftList($model);
        $result      = $db->query($query);
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
            $opt_left_id[] = $row["VALUE"];
        }
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"";
        $arg["main_part"]["LEFT_LIST"] = knjCreateCombo($objForm, "LEFT_LIST", "left", $opt_left, $extra, 7);

        //右リスト
        $opt_right = array();
        $query  = knjz015Query::selectRightList($model, $opt_left_id);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"";
        $arg["main_part"]["RIGHT_LIST"] = knjCreateCombo($objForm, "RIGHT_LIST", "right", $opt_right, $extra, 7);

        /************/
        /*ボタン作成*/
        /************/
        //追加ボタン
        $extra = "onclick=\"return move('sel_add_all');\"";
        $arg["button"]["sel_add_all"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
        //追加ボタン
        $extra = "onclick=\"return move('left');\"";
        $arg["button"]["sel_add"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
        //削除ボタン
        $extra = "onclick=\"return move('right');\"";
        $arg["button"]["sel_del"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
        //削除ボタン
        $extra = "onclick=\"return move('sel_del_all');\"";
        $arg["button"]["sel_del_all"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        $result->free();
        Query::dbCheckIn($db);

        //更新ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete')\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjz015index.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz015Form2.html", $arg);
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
