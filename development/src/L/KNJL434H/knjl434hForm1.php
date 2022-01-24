<?php

require_once('for_php7.php');

class knjl434hForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl434hForm1", "POST", "knjl434hindex.php", "", "knjl434hForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボ
        $extra = " onchange=\"return btn_submit('knjl434h');\"";
        $query = knjl434hQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //受験型コンボ
        $extra = " onchange=\"return btn_submit('knjl434h');\"";
        $query = knjl434hQuery::getExamtypeMst($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["EXAMTYPE"], "EXAMTYPE", $extra, 1);

        //回数コンボ
        $query = knjl434hQuery::getExamtypeMst($model, $model->field["EXAMTYPE"]);
        $result = $db->query($query);
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $extra = "";
        $query = knjl434hQuery::getSettingMst($model, "L004", $row["TESTDIV"]);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);
    
        //専併区分コンボ
        $extra = "";
        $query = knjl434hQuery::getSettingMst($model, "L006");
        makeCmb($objForm, $arg, $db, $query, $model->field["SHDIV"], "SHDIV", $extra, 1, "ALL");

        //入試科目
        $extra = "";
        $query = knjl434hQuery::getTestsubclasscd($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTSUBCLASSCD"], "TESTSUBCLASSCD", $extra, 1, "ALL");

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "TIME", date("H:i"));
        knjCreateHidden($objForm, "PRGID", "KNJL434H");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl434hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
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

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
