<?php

require_once('for_php7.php');

class knjl319rForm1
{
    function main(&$model){

        $objForm = new form;
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl319rForm1", "POST", "knjl319rindex.php", "", "knjl319rForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl319rQuery::getNameMst($model, $model->ObjYear, "L003"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //2:中学固定とする
            if ($row["VALUE"] != "2") {
                continue;
            }

            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["APPLICANTDIV"] = $row["VALUE"];
            if ($model->field["APPLICANTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] && $value_flg) ? $model->field["APPLICANTDIV"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('knjl319r');\"";
        $arg["data"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        //入試区分
        $opt = array();
        $value_flg = false;
        $namecd1 = $model->field["APPLICANTDIV"] == "2" ? "L024" : "L004";
        $result = $db->query(knjl319rQuery::getNameMst($model, $model->ObjYear, $namecd1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["TESTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["TESTDIV"] = $row["VALUE"];
            if ($model->field["TESTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["TESTDIV"] = ($model->field["TESTDIV"] && $value_flg) ? $model->field["TESTDIV"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('knjl319r');\"";
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);

        //専併
        $opt = array();
        $value_flg = false;
        $namecd1 = "L006";
        $result = $db->query(knjl319rQuery::getNameMst($model, $model->ObjYear, $namecd1));
        $opt[] = array('label' => "すべて",
                       'value' => "ALL");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["SHDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["SHDIV"] = ($model->field["SHDIV"] && $value_flg) ? $model->field["SHDIV"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('knjl319r');\"";
        $arg["data"]["SHDIV"] = knjCreateCombo($objForm, "SHDIV", $model->field["SHDIV"], $opt, $extra, 1);

        //特待
        $opt = array();
        $value_flg = false;
        $namecd1 = "L013";
        $result = $db->query(knjl319rQuery::getNameMst($model, $model->ObjYear, $namecd1));
        $opt[] = array('label' => "すべて",
                       'value' => "ALL");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["ABBV2"] == '') {
                continue;
            }
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["JUDGEDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["JUDGEDIV"] = ($model->field["JUDGEDIV"] && $value_flg) ? $model->field["JUDGEDIV"] : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('knjl319r');\"";
        $arg["data"]["JUDGEDIV"] = knjCreateCombo($objForm, "JUDGEDIV", $model->field["JUDGEDIV"], $opt, $extra, 1);

        //印刷ボタン
        $extra = " onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        
        //終了ボタン
        $extra = " onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL319R");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl319rForm1.html", $arg); 
        
    }
}
?>
