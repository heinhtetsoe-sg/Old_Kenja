<?php

require_once('for_php7.php');

class knjl030vForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //試験年度
        $arg["YEAR"] = $model->examyear;

        //校種コンボ
        $extra = "onChange=\"return btn_submit('main')\"";
        $query = knjl030vQuery::getSchoolKind($model);
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolKind, $extra, 1);

        //試験コンボ
        $extra = "";
        $query = knjl030vQuery::getExamId($model);
        makeCmb($objForm, $arg, $db, $query, "EXAM_ID", $model->examId, $extra, 1);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //CSV出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //非表示項目
        knjCreateHidden($objForm, "cmd", $model->cmd);

        //履歴表示
        makeListRireki($objForm, $arg, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        //HTML出力終了
        $arg["start"]  = $objForm->get_start("main", "POST", "knjl030vindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjl030vForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($name == "SCHOOL_KIND") {
            if ($value == "" && $row["NAMESPARE2"] == '1') {
                $value = $row["VALUE"];
            }
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//履歴表示
function makeListRireki(&$objForm, &$arg, $db, $model)
{
    $query = knjl030vQuery::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["EXEC_DATE"] = str_replace("-", "/", $row["EXEC_DATE"]);

        $arg['data2'][] = $row;
    }
    $result->free();
}
