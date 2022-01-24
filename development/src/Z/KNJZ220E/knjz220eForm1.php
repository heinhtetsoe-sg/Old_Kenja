<?php

require_once('for_php7.php');

class knjz220eForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz220eindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = CTRL_YEAR;

        //前年度からコピー
        $extra = "onClick=\"return btn_submit('copy');\"";
        $arg["button"]["pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度からコピー", $extra);

        //校種コンボ
        $extra = "onChange=\"return btn_submit('changeSchoolKind')\"";
        $query = knjz220eQuery::getVNameMstA023($model);
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1);

        //リスト表示
        $result = $db->query(knjz220eQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["GRADE_NAME1"] = View::alink("knjz220eindex.php", $row["GRADE_NAME1"], "target=\"right_frame\"",
                array("cmd"            => "edit",
                    "GRADE"          => $row["GRADE"]));

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        if ((!isset($model->warning) && $model->cmd == "copy") || $model->cmd == "changeSchoolKind") {
            $arg["reload"]  = "window.open('knjz220eindex.php?cmd=edit&GRADE=','right_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz220eForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
