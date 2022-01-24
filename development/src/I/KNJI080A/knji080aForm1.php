<?php

require_once('for_php7.php');


class knji080aForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knji080aForm1", "POST", "knji080aindex.php", "", "knji080aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //卒業年度
        $opt = array();
        $value_flg = false;
        $query = knji080aQuery::selectYear();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["GRADUATE_YEAR"]."年度卒",
                           'value' => $row["GRADUATE_YEAR"]);
            if ($model->field["GRADUATE_YEAR"] == $row["GRADUATE_YEAR"]) $value_flg = true;
        }
        $model->field["GRADUATE_YEAR"] = ($model->field["GRADUATE_YEAR"] && $value_flg) ? $model->field["GRADUATE_YEAR"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["GRADUATE_YEAR"] = knjCreateCombo($objForm, "GRADUATE_YEAR", $model->field["GRADUATE_YEAR"], $opt, $extra, 1);

        //DB切断
        Query::dbCheckIn($db);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJI080A");
        knjCreateHidden($objForm, "cmd");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knji080aForm1.html", $arg); 
    }
}
?>
