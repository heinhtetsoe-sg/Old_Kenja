<?php

require_once('for_php7.php');

class knjl331aForm1
{
    public function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl331aForm1", "POST", "knjl331aindex.php", "", "knjl331aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["TOP"]["YEAR"] = $model->examyear."年度";

        //受験校種コンボ
        $result = $db->query(knjl331aQuery::getNameMst($model->examyear, "L003"));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == "1") {
                $model->field["APPLICANTDIV"] = $row["VALUE"];
            }
        }
        $extra = "onChange=\"return btn_submit('knjl331a')\"";
        $arg["TOP"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        //試験回コンボ
        $extra = " onchange=\"return btn_submit('knjl331a');\"";
        $query = knjl331aQuery::getTestdivMst($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, "ALL");

        //専併区分
        $extra = "";
        $query = knjl331aQuery::getNameMst($model->examyear, "L006");
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $model->field["SHDIV"], $extra, 1, "ALL");

        //入学コース
        $extra = "";
        $query = knjl331aQuery::getEnterCourse($model);
        makeCmb($objForm, $arg, $db, $query, "ENTER_COURSE", $model->field["ENTER_COURSE"], $extra, 1, "ALL");

        //合格コース
        $extra = "";
        $query = knjl331aQuery::getPassCourse($model);
        makeCmb($objForm, $arg, $db, $query, "PASS_COURSE", $model->field["PASS_COURSE"], $extra, 1, "ALL");

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examyear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL331A");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl331aForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    }
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg && $value != "ALL") {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
