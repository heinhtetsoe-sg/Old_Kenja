<?php

require_once('for_php7.php');

class knjz030eForm2
{
    public function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz030eindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //出願コース
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjz030eQuery::getNameMst($model, "L058");
        makeCombo($objForm, $arg, $db, $query, $model->desirediv, "DESIREDIV", $extra, 1, 'BLANK');

        //受験区分コンボ
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjz030eQuery::getNameMst($model, "L045");
        makeCombo($objForm, $arg, $db, $query, $model->testdiv1, "TESTDIV1", $extra, 1, 'BLANK');

        //合格コース
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjz030eQuery::getNameMst($model, "L058");
        makeCombo($objForm, $arg, $db, $query, $model->sucDesirediv, "SUC_DESIREDIV", $extra, 1, 'BLANK');

        /** リストtoリスト */
        //選択コース一覧
        $query = knjz030eQuery::getListCourse($model, 'Llist');
        $result = $db->query($query);
        $opt_left = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array("label" => htmlspecialchars($row["SUC_TESTDIV1_NAME"]),
                                "value" => $row["SUC_TESTDIV1"]);
        }
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "L_COURSE", "left", $opt_left, $extra, 20);

        //コース一覧
        $query = knjz030eQuery::getListCourse($model, 'Rlist');
        $result = $db->query($query);
        $opt_right = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array("label" => htmlspecialchars($row["SUC_TESTDIV1_NAME"]),
                                 "value" => $row["SUC_TESTDIV1"]);
        }
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "R_COURSE", "right", $opt_right, $extra, 20);

        //追加削除ボタン
        $extra = "onclick=\"return move('sel_add_all');\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
        $extra = "onclick=\"return move('left');\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
        $extra = "onclick=\"return move('right');\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
        $extra = "onclick=\"return move('sel_del_all');\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        $result->free();
        Query::dbCheckIn($db);

        //更 新
        $extra = "onclick=\"return doSubmit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取 消
        $extra = "onclick=\"return doSubmit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終 了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjz030eindex.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz030eForm2.html", $arg);
    }
}
//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="")
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
        $i++;
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
