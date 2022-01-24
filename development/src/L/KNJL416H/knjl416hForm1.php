<?php
class knjl416hForm1
{
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]  = $objForm->get_start("csv", "POST", "knjl416hindex.php", "", "csv");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR + 1;
        
        //入試制度
        $query = knjl416hQuery::getNameMst($model, "L003");
        $extra = "onChange=\"btn_submit('chgAppDiv')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //試験回数
        $model->testdiv = ($model->cmd == "chgAppDiv") ? "" : $model->testdiv;
        $query = knjl416hQuery::getSettingMst($model, "L004");
        $extra = "onChange=\"btn_submit('chgTestDiv')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //受験型
        $model->examtype = ($model->cmd == "chgAppDiv" || $model->cmd == "chgTestDiv") ? "" : $model->examtype;
        $query = knjl416hQuery::getEntexamExamTypeMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "EXAM_TYPE", $model->examtype, $extra, 1, "");

        //コース
        $model->course = ($model->cmd == "chgAppDiv") ? "" : $model->course;
        $query = knjl416hQuery::getEntexamCourseMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "EXAMCOURSECD", $model->course, $extra, 1, "");

        //出願区分
        $model->shdiv = ($model->cmd == "chgAppDiv") ? "" : $model->shdiv;
        $query = knjl416hQuery::getSettingMst($model, "L006");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $model->shdiv, $extra, 1, "");
        
        /**********/
        /* ボタン */
        /**********/
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR + 1);
        knjCreateHidden($objForm, "DATE", CTRL_DATE);

        $arg["IFRAME"] = VIEW::setIframeJs();
        //DB切断
        Query::dbCheckIn($db);
        
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl416hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
