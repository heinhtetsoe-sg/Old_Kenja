<?php

require_once('for_php7.php');

class knjl622fForm1
{
    function main(&$model){

        $objForm = new form;
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl622fForm1", "POST", "knjl622findex.php", "", "knjl622fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl622fQuery::getNameMst($model->ObjYear, "L003");
        $extra = " onchange=\"return btn_submit('knjl622f');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分
        $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $query = knjl622fQuery::getNameMst($model->ObjYear, $namecd1);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        ////判定資料
        //第一志望コース
        $query = knjl622fQuery::getExamcourse($model->ObjYear, $model->field["APPLICANTDIV"], $model->field["TESTDIV"]);
        $extra = " disabled ";
        makeCmb($objForm, $arg, $db, $query, $model->field["EXAMCOURSE"], "EXAMCOURSE", $extra, 1);

        //専併区分
        $query = knjl622fQuery::getNameMst($model->ObjYear, "L006");
        $extra = " disabled ";
        makeCmb($objForm, $arg, $db, $query, $model->field["SHDIV"], "SHDIV", $extra, 1);

        //受験型
        $query = knjl622fQuery::getExamType($model->ObjYear, $model->field["APPLICANTDIV"], $model->field["TESTDIV"]);
        $extra = " disabled ";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV1"], "TESTDIV1", $extra, 1);

        ////内部認定判定資料
        //中学３年時のコース
        $query = knjl622fQuery::getFSCourse($model->ObjYear);
        $extra = " disabled ";
        makeCmb($objForm, $arg, $db, $query, $model->field["FS_COURSE"], "FS_COURSE", $extra, 1);

        //出力順
        if ($model->field["SORT"] == "") $model->field["SORT"] = "1";
        $opt = array();
        $opt[] = array("label" => "1:受験番号順", "value" => "1");
        $opt[] = array("label" => "2:成績順", "value" => "2");
        $extra = " disabled ";
        $arg["data"]["SORT"] = knjCreateCombo($objForm, "SORT", $model->field["SORT"], $opt, $extra, 1);

        //印刷ボタン
        $extra = " onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        
        //終了ボタン
        $extra = " onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL622F");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl622fForm1.html", $arg); 
        
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        //この画面では、SHDIVは空白がデフォなので、SHDIVだけはNAMESPARE2はチェックしない。
        if ($value == "" && ($name != "SHDIV" && $row["NAMESPARE2"] == '1')) $value = $row["VALUE"];
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
