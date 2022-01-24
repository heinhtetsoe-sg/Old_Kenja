<?php

require_once('for_php7.php');

class knjl223rForm1
{
    function main(&$model){

        $objForm = new form;
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl223rForm1", "POST", "knjl223rindex.php", "", "knjl223rForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //事前入試制度
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl223rQuery::getNameMst($model, $model->ObjYear, "L003"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["APPLICANTDIV"] = $row["VALUE"];
            if ($model->field["APPLICANTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] && $value_flg) ? $model->field["APPLICANTDIV"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        //事前入試区分
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl223rQuery::getNameMst($model, $model->ObjYear, "L004"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["TESTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["TESTDIV"] = ($model->field["TESTDIV"] && $value_flg) ? $model->field["TESTDIV"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);

        //欠席日数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $model->field["S_KESSEKI"] = $model->field["S_KESSEKI"] == "" ? "21" : $model->field["S_KESSEKI"];
        $arg["data"]["S_KESSEKI"] = knjCreateTextBox($objForm, $model->field["S_KESSEKI"], "S_KESSEKI", 2, 2, $extra);
        $extra = "onChange=\"return btn_submit('knjl223r')\"";
        $model->field["E_KESSEKI"] = $model->field["E_KESSEKI"] == "" ? "51" : $model->field["E_KESSEKI"];
        $arg["data"]["E_KESSEKI"] = knjCreateTextBox($objForm, $model->field["E_KESSEKI"], "E_KESSEKI", 2, 2, $extra);
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $model->field["S2_KESSEKI"] = $model->field["S2_KESSEKI"] == "" ? $model->field["E_KESSEKI"] : $model->field["S2_KESSEKI"];
        $arg["data"]["S2_KESSEKI"] = knjCreateTextBox($objForm, $model->field["S2_KESSEKI"], "S2_KESSEKI", 2, 2, $extra);
        
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
        knjCreateHidden($objForm, "PRGID", "KNJL223R");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl223rForm1.html", $arg); 
        
    }
}
?>
