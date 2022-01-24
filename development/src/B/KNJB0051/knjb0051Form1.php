<?php

require_once('for_php7.php');

class knjb0051Form1 {
    function main(&$model){
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb0051Form1", "POST", "knjb0051index.php", "", "knjb0051Form1");
        $db = Query::dbCheckOut();

        $arg["data"]["YEAR"] = CTRL_YEAR;

        /**********/
        /* ラジオ */
        /**********/
        //時間割種別・指定日付
        $opt = array(1, 2); //1:基本時間割 2:通常時間割
        $model->field["JIKANWARI_SYUBETU"] = ($model->field["JIKANWARI_SYUBETU"] == "") ? "1" : $model->field["JIKANWARI_SYUBETU"];
        $click = "onclick=\"jikanwari(this);\"";
        $extra = array($click." id=\"JIKANWARI_SYUBETU1\"", $click." id=\"JIKANWARI_SYUBETU2\"");
        $radioArray = knjCreateRadio($objForm, "JIKANWARI_SYUBETU", $model->field["JIKANWARI_SYUBETU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if( $model->field["JIKANWARI_SYUBETU"] == 2 ) {             //通常時間割選択時
            $dis_jikan = "disabled";                    //時間割選択コンボ使用不可
            $dis_date  = "";                            //指定日付テキスト使用可
            $arg["Dis_Date"] = " dis_date(false); " ;
        } else {                                        //基本時間割選択時
            $dis_jikan = "";                            //時間割選択コンボ使用可
            $dis_date  = "disabled";                    //指定日付テキスト使用不可
            $arg["Dis_Date"] = " dis_date(true); " ;
        }

        //時間割選択コンボボックスを作成
        $opt = knjb0051Query::getBscHdQuery($model, $db);
        $opt = isset($opt) ? $opt : array();
        $extra = $dis_jikan;
        $arg["data"]["TITLE"] = knjCreateCombo($objForm, "TITLE", $model->field["TITLE"], $opt, $extra, 1);

        //対象範囲(週数)
        $opt = array(1, 2, 3, 4);
        $model->field["DATE_WEEK"] = ($model->field["DATE_WEEK"] == "") ? "1" : $model->field["DATE_WEEK"];
        $click = "onclick=\"btn_submit('knjb0051');\"";
        $extra = array($click." id=\"DATE_WEEK1\"", $click." id=\"DATE_WEEK2\"", $click." id=\"DATE_WEEK3\"", $click." id=\"DATE_WEEK4\"");
        $radioArray = knjCreateRadio($objForm, "DATE_WEEK", $model->field["DATE_WEEK"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //指定日付(開始)
        if ($model->field["JIKANWARI_SYUBETU"] == 2){
            if (!isset($model->field["SDATE"])) {
                $model->field["SDATE"] = str_replace("-","/",CTRL_DATE);
            }
            //指定日を含む指定週の開始日(月曜日)と終了日(日曜日)を取得
            list($y, $m, $d) = preg_split("/\//", $model->field["SDATE"]);
            $dayAdd = 7 * ((int)$model->field["DATE_WEEK"]);
            $setSdate = mktime(0, 0, 0, $m, $d + (int)$dayAdd - 1, $y);
            $setSdate = date('Y/m/d', $setSdate);
            $model->field["EDATE"] = $setSdate;
        } else {
            $model->field["SDATE"] = "";
            $model->field["EDATE"] = "";
        }
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"], "reload=true");

        //指定日付(終了)
        $extra = "disabled";
        $arg["data"]["EDATE"] = knjCreateTextBox($objForm, $model->field["EDATE"], "EDATE", 12, "", $extra);

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
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", str_replace("-","/",CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJB0051");
        //JavaScriptで参照するため
        knjCreateHidden($objForm, "T_YEAR");
        knjCreateHidden($objForm, "T_BSCSEQ");
        knjCreateHidden($objForm, "T_SEMESTER");
        //チェック用
        knjCreateHidden($objForm, "CHK_SDATE", $model->control['学期開始日付'][9]);
        knjCreateHidden($objForm, "CHK_EDATE", $model->control['学期終了日付'][9]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();
        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb0051Form1.html", $arg);
    }
}
?>
