<?php

require_once('for_php7.php');

class knjd232xForm1 {
    function main(&$model) {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd232xForm1", "POST", "knjd232xindex.php", "", "knjd232xForm1");

        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjd232xQuery::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期テキストボックスの設定
        $arg["data"]["GAKKI"] = $model->control["学期名"][CTRL_SEMESTER];

        //学期コンボボックスを作成する
        $opt_seme = array();
        $query = knjd232xQuery::getSelectSeme();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_seme[] = array('label' => $row["SEMESTERNAME"],
                                'value' => $row["SEMESTER"]);
        }
        if($model->field["GAKKI2"]=="") $model->field["GAKKI2"] = CTRL_SEMESTER;
        $result->free();

        $extra = "onchange=\"return btn_submit('knjd232x');\"";
        $arg["data"]["GAKKI2"] = knjCreateCombo($objForm, "GAKKI2", $model->field["GAKKI2"], $opt_seme, $extra, 1);

        //学年コンボボックスを作成する
        $opt_schooldiv = "学年";
        $opt_grade=array();
        $query = knjd232xQuery::getSelectGrade($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_grade[] = array('label' => $row["GRADE_NAME1"],
                                 'value' => $row["GRADE"]);
        }
        if($model->field["GRADE"]=="") $model->field["GRADE"] = $opt_grade[0]["value"];
        $result->free();

        $extra = "";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $opt_grade, $extra, 1);

        /********************/
        /* テキストボックス */
        /********************/
        //成績優良者
        //評定平均（以上）テキストボックスを作成する
        $extra = "style=\"text-align: right\" onblur=\"this.value=toFloat(this.value)\"";
        $value = 4.3;
        $arg["data"]["ASSESS1"] = knjCreateTextBox($objForm, $value, "ASSESS1", 3, 3, $extra);
        //成績不振者
        //評定テキストボックスを作成する
        $extra = "style=\"text-align: right\" onblur=\"this.value=toFloat(this.value)\"";
        $value = isset($model->field["ASSESS2"]) ? $model->field["ASSESS2"] : "1";
        $arg["data"]["ASSESS2"] = knjCreateTextBox($objForm, $value, "ASSESS2", 3, 2, $extra);
        //科目数テキストボックスを作成する
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = isset($model->field["COUNT2"]) ? $model->field["COUNT2"] : "1";
        $arg["data"]["COUNT2"] = knjCreateTextBox($objForm, $value, "COUNT2", 3, 3, $extra);
        //未履修科目数テキストボックスを作成する
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = isset($model->field["UNSTUDY2"]) ? $model->field["UNSTUDY2"] : "1";
        $arg["data"]["UNSTUDY2"] = knjCreateTextBox($objForm, $value, "UNSTUDY2", 3, 3, $extra);
        //評定平均（以下）テキストボックスを作成する
        $extra = "style=\"text-align: right\" onblur=\"this.value=toFloat(this.value)\"";
        $value = 2.2;
        $arg["data"]["ASSESS_AVE2"] = knjCreateTextBox($objForm, $value, "ASSESS_AVE2", 3, 3, $extra);
        //出欠状況不振者
        //遅刻数テキストボックスを作成する
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["LATE5"];
        $arg["data"]["LATE5"] = knjCreateTextBox($objForm, $value, "LATE5", 3, 3, $extra);
        //早退数テキストボックスを作成する
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["EARLY5"];
        $arg["data"]["EARLY5"] = knjCreateTextBox($objForm, $value, "EARLY5", 3, 3, $extra);
        //欠席数テキストボックスを作成する
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["ABSENT5"];
        $arg["data"]["ABSENT5"] = knjCreateTextBox($objForm, $value, "ABSENT5", 3, 3, $extra);
        //欠課数テキストボックスを作成する
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["SUBCLASS_ABSENT5"];
        $arg["data"]["SUBCLASS_ABSENT5"] = knjCreateTextBox($objForm, $value, "SUBCLASS_ABSENT5", 3, 3, $extra);

        /********/
        /* 日付 */
        /********/
        //移動対象日付
        $arg["el"]["DATE"]=View::popUpCalendar($objForm,"DATE",isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"]);

        /**********/
        /* ボタン */
        /**********/
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //CSV出力ボタン
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knjd232xQuery::getSchoolCd());
            //CSV出力(成績優良者)
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "', 'csv1');\"";
            $arg["button"]["btn_csv1"] = knjCreateBtn($objForm, "btn_csv1", "エクセル出力", $extra);
            //CSV出力(成績不振者)
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "', 'csv2');\"";
            $arg["button"]["btn_csv2"] = knjCreateBtn($objForm, "btn_csv2", "エクセル出力", $extra);
            //CSV出力(出欠状況)
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "', 'csv3');\"";
            $arg["button"]["btn_csv3"] = knjCreateBtn($objForm, "btn_csv3", "エクセル出力", $extra);
        } else {
            //CSV出力(成績優良者)
            $extra = "onclick=\"return btn_submit('csv1');\"";
            $arg["button"]["btn_csv1"] = knjCreateBtn($objForm, "btn_csv1", "ＣＳＶ出力", $extra);
            //CSV出力(成績不振者)
            $extra = "onclick=\"return btn_submit('csv2');\"";
            $arg["button"]["btn_csv2"] = knjCreateBtn($objForm, "btn_csv2", "ＣＳＶ出力", $extra);
            //CSV出力(出欠状況)
            $extra = "onclick=\"return btn_submit('csv3');\"";
            $arg["button"]["btn_csv3"] = knjCreateBtn($objForm, "btn_csv3", "ＣＳＶ出力", $extra);
        }

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "YEAR",  CTRL_YEAR);
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD232X");
        knjCreateHidden($objForm, "SDATE", $model->control["学期開始日付"][$model->field["GAKKI2"]]); //学期開始日
        knjCreateHidden($objForm, "EDATE", $model->control["学期終了日付"][$model->field["GAKKI2"]]); //学期終了日
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "EXCEL_KIND");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd232xForm1.html", $arg); 
    }
}
?>
