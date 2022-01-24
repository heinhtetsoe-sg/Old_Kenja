<?php

require_once('for_php7.php');

class knjf161Form1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjf161Form1", "POST", "knjf161index.php", "", "knjf161Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->test_year;

        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            //学校名称2表示
            $schoolname2 = $db->getOne(knjf161Query::getSchoolName2($model, $model->test_year));
            $arg["SCH_NAME2"] = (strlen($schoolname2) > 0) ? "<<".$schoolname2.">>" : "";
        }

        /****************/
        /* ラジオボタン */
        /****************/
        //帳票種類
        $opt = array(1, 2); //(1:新入生調査票 2:保健調査票)
        $model->field["PRINT_DIV"] = ($model->field["PRINT_DIV"] == "") ? "1" : $model->field["PRINT_DIV"];
        $extra = array("id=\"PRINT_DIV1\"", "id=\"PRINT_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "PRINT_DIV", $model->field["PRINT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /**************/
        /* ボタン作成 */
        /**************/
        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "YEAR", $model->test_year);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJF161");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
        knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf161Form1.html", $arg);
    }
}
?>
