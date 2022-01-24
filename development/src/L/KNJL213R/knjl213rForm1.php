<?php

require_once('for_php7.php');

class knjl213rForm1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl213rForm1", "POST", "knjl213rindex.php", "", "knjl213rForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl213rQuery::getNameMst($model, $model->ObjYear, "L003"));
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

        //入試制度区分
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjl213rQuery::getNameMst($model, $model->ObjYear, "L004"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["TESTDIV"] == $row["VALUE"]) $value_flg = true;
        }
        $model->field["TESTDIV"] = $model->field["TESTDIV"] && $value_flg ? $model->field["TESTDIV"] : $opt[0]["value"];
        $result->free();
        $model->field["TESTDIV"] = ($model->field["TESTDIV"] && $value_flg) ? $model->field["TESTDIV"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);

        //重複チェック項目
        $opt = array(1, 2, 3, 4, 5);
        $model->field["CENTER_TITLE"] = ($model->field["CENTER_TITLE"] == "") ? "1" : $model->field["CENTER_TITLE"];
        $extra = array("id=\"CENTER_TITLE1\"", "id=\"CENTER_TITLE2\"", "id=\"CENTER_TITLE3\"", "id=\"CENTER_TITLE4\"", "id=\"CENTER_TITLE5\"");
        $radioArray = knjCreateRadio($objForm, "CENTER_TITLE", $model->field["CENTER_TITLE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷ボタン
        $extra = " onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = " onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL213R");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl213rForm1.html", $arg);
    }
}
?>
