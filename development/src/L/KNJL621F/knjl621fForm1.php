<?php

require_once('for_php7.php');

class knjl621fForm1
{
    function main(&$model){

        $objForm = new form;
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl621fForm1", "POST", "knjl621findex.php", "", "knjl621fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl621fQuery::getNameMst($model->ObjYear, "L003"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["APPLICANTDIV"] = $row["VALUE"];
            if ($model->field["APPLICANTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] && $value_flg) ? $model->field["APPLICANTDIV"] : $opt[0]["value"];
        $extra = "";
        $extra = " onchange=\"return btn_submit('knjl621f');\"";
        $arg["data"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

        //入試区分
        $opt = array();
        $value_flg = false;
        $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $result = $db->query(knjl621fQuery::getNameMst($model->ObjYear, $namecd1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["TESTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["TESTDIV"] = $row["VALUE"];
            if ($model->field["TESTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["TESTDIV"] = ($model->field["TESTDIV"] && $value_flg) ? $model->field["TESTDIV"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);

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
        knjCreateHidden($objForm, "PRGID", "KNJL621F");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl621fForm1.html", $arg); 
        
    }
}
?>
