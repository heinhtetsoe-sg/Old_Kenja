<?php

require_once('for_php7.php');

class knjl304rForm1
{
    function main(&$model){

        $objForm = new form;
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl304rForm1", "POST", "knjl304rindex.php", "", "knjl304rForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl304rQuery::getNameMst($model, $model->ObjYear, "L003"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["APPLICANTDIV"] = $row["VALUE"];
            if ($model->field["APPLICANTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] && $value_flg) ? $model->field["APPLICANTDIV"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('knjl304r');\"";
        $arg["data"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        //入試区分
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl304rQuery::getNameMst($model, $model->ObjYear, "L004"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["TESTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["TESTDIV"] = $row["VALUE"];
            if ($model->field["TESTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["TESTDIV"] = ($model->field["TESTDIV"] && $value_flg) ? $model->field["TESTDIV"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('knjl304r');\"";
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);
        
        //課程学科
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl304rQuery::getCourseMajorcd($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $opt[] = array('label' => '--全て--','value' => '9999');
        $result->free();
        $model->field["COURSE_MAJORCD"] = ($model->field["COURSE_MAJORCD"]) ? $model->field["COURSE_MAJORCD"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('knjl304r');\"";
        $arg["data"]["COURSE_MAJORCD"] = knjCreateCombo($objForm, "COURSE_MAJORCD", $model->field["COURSE_MAJORCD"], $opt, $extra, 1);
        
        //志望コース
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl304rQuery::getEntexamCourseMst($model->ObjYear, $model->field["APPLICANTDIV"], $model->field["TESTDIV"], $model->field["COURSE_MAJORCD"]));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["EXAMCOURSECD"] == $row["VALUE"]) $value_flg = true;
        }
        if ($model->field["COURSE_MAJORCD"] === '9999') {
            $opt[] = array('label' => '--全て--','value' => '9999');
        }
        $result->free();
        $model->field["EXAMCOURSECD"] = ($model->field["EXAMCOURSECD"] && $value_flg) ? $model->field["EXAMCOURSECD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["EXAMCOURSECD"] = knjCreateCombo($objForm, "EXAMCOURSECD", $model->field["EXAMCOURSECD"], $opt, $extra, 1);

        //選抜Ⅰ合格除くチェックボックス
        $extra  = ($model->field["EXCL_SENBATSU1"] == "1") ? "checked" : "";
        $extra .= ($model->field["TESTDIV"] == "2") ? "" : " disabled";
        $extra .= " id=\"EXCL_SENBATSU1\"";
        $arg["data"]["EXCL_SENBATSU1"] = knjCreateCheckBox($objForm, "EXCL_SENBATSU1", "1", $extra, "");

        //印刷ボタン
        $extra = " onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        
        //終了ボタン
        $extra = " onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL304R");
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl304rForm1.html", $arg); 
        
    }
}
?>
