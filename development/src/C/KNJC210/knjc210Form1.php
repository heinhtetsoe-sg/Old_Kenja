<?php

require_once('for_php7.php');
class knjc210Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjc210Form1", "POST", "knjc210index.php", "", "knjc210Form1");
        //DB接続
        $db = Query::dbCheckOut();
        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        //学期
        $arg["data"]["SEMESTER"] = $model->control["学期名"][CTRL_SEMESTER];

        /********/
        /* 日付 */
        /********/
        //印刷範囲(開始)
        $model->field["SDATE"] = $model->field["SDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["SDATE"];
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"]);
        //印刷範囲(終了)
        $model->field["EDATE"] = $model->field["EDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["EDATE"];
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $model->field["EDATE"]);

        /****************/
        /* ラジオボタン */
        /****************/
        $opt = array(1, 2);
        $model->field["HIDUKE"] = ($model->field["HIDUKE"] == "") ? "1" : $model->field["HIDUKE"];
        $extra = array("id=\"HIDUKE1\"", "id=\"HIDUKE2\"");
        $radioArray = knjCreateRadio($objForm, "HIDUKE", $model->field["HIDUKE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

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
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJC210");
        //日付チェック用
        knjCreateHidden($objForm, "CHK_LDATE", str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "CHK_SDATE", $model->control['学期開始日付'][$model->control['学期']]);
        knjCreateHidden($objForm, "CHK_EDATE", $model->control['学期終了日付'][$model->control['学期']]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc210Form1.html", $arg);
    }
}
?>
