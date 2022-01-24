<?php

require_once('for_php7.php');

class knje140tForm1 {
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knje140tForm1", "POST", "knje140tindex.php", "", "knje140tForm1");

        $arg["data"]["YEAR"] = CTRL_YEAR;
        //エラーコード取得
        $opt_grade = array();
        $db = Query::dbCheckOut();
        $query = knje140tQuery::getgrade($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_grade[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $result->free();

        if (!$model->field["GRADE"]) $model->field["GRADE"] = $opt_grade[0];

        $extra = "";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $opt_grade, $extra, 1);

        /**********/
        /* ボタン */
        /**********/
        //印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        knjCreateHidden($objForm, "PRGID", "KNJE140T");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "gaihyouGakkaBetu", $model->Properties["gaihyouGakkaBetu"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje140tForm1.html", $arg); 
    }
}
?>
