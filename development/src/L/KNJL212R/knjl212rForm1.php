<?php

require_once('for_php7.php');

class knjl212rForm1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl212rForm1", "POST", "knjl212rindex.php", "", "knjl212rForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //事前入試制度区分
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl212rQuery::getNameMst($model->ObjYear, "L004"));
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

        //重複チェック項目チェックボックス
        //かな
        if ($model->field["OUTKANA"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["OUTKANA"] = knjCreateCheckBox($objForm, "OUTKANA", "1", $extra);

        //学校
        if ($model->field["OUTSCHL"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["OUTSCHL"] = knjCreateCheckBox($objForm, "OUTSCHL", "1", $extra);

        //性別
        if ($model->field["OUTSCHL"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["OUTSEX"] = knjCreateCheckBox($objForm, "OUTSEX", "1", $extra);

        //印刷ボタン
        $extra = " onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        
        //終了ボタン
        $extra = " onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);


        //hidden
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL212R");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl212rForm1.html", $arg); 
    }
}
?>
