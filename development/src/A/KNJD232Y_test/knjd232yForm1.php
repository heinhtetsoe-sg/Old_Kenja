<?php

require_once('for_php7.php');

class knjd232yForm1 {
    function main(&$model) {
        //権限チェックtesteteste
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd232yForm1", "POST", "knjd232yindex.php", "", "knjd232yForm1");
        $db = Query::dbCheckOut();

        //年度yersdteyeye
        $arg["data"]["YEAR"] = CTRL_YEAR;
        //学期
        $arg["data"]["GAKKI"] = $model->control["学期名"][CTRL_SEMESTER];
        //上限値
        $query = knjd232yQuery::getSyuutokuJougenti();
        $row_jougen = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //修得上限値
        $arg["data"]["SYUTOKU_JOUGENTI"] = $row_jougen["SYUTOKU_JOUGENTI"];
        //履修上限値
        $arg["data"]["RISYU_JOUGENTI"]   = $row_jougen["RISYU_JOUGENTI"];
        //特活上限値
        $arg["data"]["RISYU_JOUGENTI_SPECIAL"] = $row_jougen["RISYU_JOUGENTI_SPECIAL"];

        /******************/
        /* コンボボックス */
        /******************/
        //学期コンボボックスを作成する
        $opt_seme = array();
        $query = knjd232yQuery::getSelectSeme();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_seme[] = array('label' => $row["SEMESTERNAME"],
                                'value' => $row["SEMESTER"]);
        }
        if($model->field["GAKKI2"]=="") $model->field["GAKKI2"] = '9';
        $result->free();

        $extra = "onchange=\"return btn_submit('gakki_change');\"";
        $arg["data"]["GAKKI2"] = knjCreateCombo($objForm, "GAKKI2", $model->field["GAKKI2"], $opt_seme, $extra, 1);

        //学年コンボボックスを作成する
        $opt_grade=array();
        $query = knjd232yQuery::getSelectGrade();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_grade[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $opt_grade[] = array('label' => "全て",
                             'value' => "99");
        if($model->field["GRADE"]=="") $model->field["GRADE"] = $opt_grade[0]["value"];
        $result->free();

        $extra = "";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $opt_grade, $extra, 1);

        //テスト種別コンボボックスを作成する
        $opt_test = array();
        if ($model->school_div_name == 'tottori' && $model->field["GAKKI2"] == "9") {
            $opt_test[] = array('label' => '9900 学年評価', 'value' => '9900-2');
            $opt_test[] = array('label' => '9900 学年評定', 'value' => '9900-1');
        } else {
            $query = knjd232yQuery::getTestKind($model);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row["VALUE"] = ($row["VALUE"] == '9900') ? $row["VALUE"].'-1' : $row["VALUE"].'-2';
                $opt_test[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
            }
            $result->free();
        }
        $model->field["TESTKIND"] = ($model->field["TESTKIND"] == "") ? $opt_test[0]["value"] : $model->field["TESTKIND"];

        if($model->useTestKind == "1"){
            $arg["data"]["TESTKIND"] = knjCreateCombo($objForm, "TESTKIND", $model->field["TESTKIND"], $opt_test, "", 1);
            $arg["data"]["TESTKIND_KOUMOKU"] = 'テスト種別';
        } else {
            knjCreateHidden($objForm, "TESTKIND", "9900-1");
        }

        //SHR
        $opt = array();
        $value_flg = false;
        $query = knjd232yQuery::getShr($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        if($model->field["SHR"]=="") $model->field["SHR"] = $opt[0]["value"];
        $extra = "";
        $arg["data"]["SHR"] = knjCreateCombo($objForm, "SHR", $model->field["SHR"], $opt, $extra, 1);

        /**********/
        /* その他 */
        /**********/
        //評定 or 評価
        if ($model->field["GAKKI2"] == 9) {
            $arg["data"]["HYOUTEI_OR_HYOUKA"] = '評定';
        } else {
            $arg["data"]["HYOUTEI_OR_HYOUKA"] = '評価';
        }

        /********************/
        /* チェックボックス */
        /********************/
        //元科目表示
        $extra  = ($model->field["ATTEND_SUBCLASSCD"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"ATTEND_SUBCLASSCD\"";
        $arg["data"]["ATTEND_SUBCLASSCD"] = knjCreateCheckBox($objForm, "ATTEND_SUBCLASSCD", "1", $extra, "");
        //先科目表示
        $extra  = ($model->field["COMBINED_SUBCLASSCD"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"COMBINED_SUBCLASSCD\"";
        $arg["data"]["COMBINED_SUBCLASSCD"] = knjCreateCheckBox($objForm, "COMBINED_SUBCLASSCD", "1", $extra, "");

        //教科・科目/総合的な時間
        if (!$model->field["KYOUKA_SOUGOU1"] && !$model->field["KYOUKA_SOUGOU2"]) {
            $model->field["KYOUKA_SOUGOU1"] = '1';
        }
        $extra = ($model->field["KYOUKA_SOUGOU1"] == "1") ? $extra = "checked='checked' id=\"KYOUKA_SOUGOU1\"" : "id=\"KYOUKA_SOUGOU1\"";
        $arg["data"]["KYOUKA_SOUGOU1"] = knjCreateCheckBox($objForm, "KYOUKA_SOUGOU1", "1", $extra);
        $extra = ($model->field["KYOUKA_SOUGOU2"] == "1") ? $extra = "checked='checked' id=\"KYOUKA_SOUGOU2\"" : "id=\"KYOUKA_SOUGOU2\"";
        $arg["data"]["KYOUKA_SOUGOU2"] = knjCreateCheckBox($objForm, "KYOUKA_SOUGOU2", "1", $extra);

        //成績不振者
        $extra = ($model->field["SEISEKI_HUSIN1"] == "1") ? $extra = "checked='checked' id=\"SEISEKI_HUSIN1\"" : "id=\"SEISEKI_HUSIN1\"";
        $arg["data"]["SEISEKI_HUSIN1"] = knjCreateCheckBox($objForm, "SEISEKI_HUSIN1", "1", $extra);

        $extra = ($model->field["SEISEKI_HUSIN2"] == "1") ? "checked='checked' id=\"SEISEKI_HUSIN2\"" : "id=\"SEISEKI_HUSIN2\"";
        $disabled = ($model->field["SYUKKETU_SYUKEI"] == "1") ? " disabled" : "";
        $arg["data"]["SEISEKI_HUSIN2"] = knjCreateCheckBox($objForm, "SEISEKI_HUSIN2", "1", $extra.$disabled);

        $extra = ($model->field["SEISEKI_HUSIN3"] == "1") ? "checked='checked' id=\"SEISEKI_HUSIN3\"" : "id=\"SEISEKI_HUSIN3\"";
        $disabled = ($model->field["SYUKKETU_SYUKEI"] == "1") ? " disabled" : "";
        $arg["data"]["SEISEKI_HUSIN3"] = knjCreateCheckBox($objForm, "SEISEKI_HUSIN3", "1", $extra.$disabled);

        //皆勤者/在籍期間全て
        $extra  = ($model->field["ZAISEKI_ALL"] == "1") ? "checked" : "";
        $extra .= " id=\"ZAISEKI_ALL\"";
        $disabled = ($model->field["SYUKKETU_SYUKEI"] == "1") ? " disabled" : "";
        $arg["data"]["ZAISEKI_ALL"] = knjCreateCheckBox($objForm, "ZAISEKI_ALL", "1", $extra.$disabled, "");

        /****************/
        /* ラジオボタン */
        /****************/
        //出欠集計範囲 (1:学期 2:累計)
        $opt = array(1, 2);
        $model->field["SYUKKETU_SYUKEI"] = ($model->field["SYUKKETU_SYUKEI"] == "") ? "2" : $model->field["SYUKKETU_SYUKEI"];
        $extra = array("id=\"SYUKKETU_SYUKEI1\" onclick=\" useOption();\"", "id=\"SYUKKETU_SYUKEI2\" onclick=\" useOption();\"");
        $radioArray = knjCreateRadio($objForm, "SYUKKETU_SYUKEI", $model->field["SYUKKETU_SYUKEI"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //CSV出力方法 (1:1行/1人 2:複数行/1人)
        $opt = array(1, 2);
        $model->field["SYUTURYOKU_HOU"] = ($model->field["SYUTURYOKU_HOU"] == "") ? "1" : $model->field["SYUTURYOKU_HOU"];
        $extra = array("id=\"SYUTURYOKU_HOU1\"", "id=\"SYUTURYOKU_HOU2\"");
        $radioArray = knjCreateRadio($objForm, "SYUTURYOKU_HOU", $model->field["SYUTURYOKU_HOU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出欠状況
        $opt = array(1, 2, 3);
        $model->field["SYUKKETU_JOUKYOU"] = ($model->field["SYUKKETU_JOUKYOU"] == "") ? "1" : $model->field["SYUKKETU_JOUKYOU"];
        $extra = array("id=\"SYUKKETU_JOUKYOU1\"", "id=\"SYUKKETU_JOUKYOU2\"", "id=\"SYUKKETU_JOUKYOU3\"");
        $radioArray = knjCreateRadio($objForm, "SYUKKETU_JOUKYOU", $model->field["SYUKKETU_JOUKYOU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //皆勤者
        $opt = array(1, 2);
        $model->field["KAIKINSYA"] = ($model->field["KAIKINSYA"] == "") ? "1" : $model->field["KAIKINSYA"];
        $extra = array("id=\"KAIKINSYA1\"", "id=\"KAIKINSYA2\"");
        $radioArray = knjCreateRadio($objForm, "KAIKINSYA", $model->field["KAIKINSYA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********************/
        /* テキストボックス */
        /********************/
        /************************** 成績優良者 **************************/
        //評定平均（以上）テキストボックスを作成する
        $extra = "style=\"text-align: right\" onblur=\"this.value=toFloat(this.value)\"";
        if (VARS::post("cmd") == 'knjd232y' || VARS::post("cmd") == '') {
            if ($model->field["GAKKI2"] == 9) {
                $value = "4.3";
            } else {
                $value = $model->field["SEISEKI_YURYOU_HYOUTEI"];
            }
        } else {
            $value = $model->field["SEISEKI_YURYOU_HYOUTEI"];
        }
        $arg["data"]["SEISEKI_YURYOU_HYOUTEI"] = knjCreateTextBox($objForm, $value, "SEISEKI_YURYOU_HYOUTEI", 3, 3, $extra);
        /************************** 成績不振者 **************************/
        //成績不振者 教科・科目1
        $extra = "style=\"text-align: right\" onblur=\"this.value=toFloat(this.value)\"";
        $value = ($model->cmd == '' ||$model->cmd  == 'gakki_change') ? '1' : $model->field["SEISEKI_HUSIN_HYOUTEI_FROM"];
        $arg["data"]["SEISEKI_HUSIN_HYOUTEI_FROM"] = knjCreateTextBox($objForm, $value, "SEISEKI_HUSIN_HYOUTEI_FROM", 3, 3, $extra);
        $value = ($model->cmd == '' ||$model->cmd  == 'gakki_change') ? '1' : $model->field["SEISEKI_HUSIN_HYOUTEI_TO"];
        $arg["data"]["SEISEKI_HUSIN_HYOUTEI_TO"] = knjCreateTextBox($objForm, $value, "SEISEKI_HUSIN_HYOUTEI_TO", 3, 3, $extra);

        /************************** 出欠状況 **************************/
        //出欠状況/遅刻
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["SYUKKETU_SYUKKETU_TIKOKU"];
        $arg["data"]["SYUKKETU_SYUKKETU_TIKOKU"] = knjCreateTextBox($objForm, $value, "SYUKKETU_SYUKKETU_TIKOKU", 3, 3, $extra);
        //出欠状況/早退
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["SYUKKETU_SYUKKETU_SOUTAI"];
        $arg["data"]["SYUKKETU_SYUKKETU_SOUTAI"] = knjCreateTextBox($objForm, $value, "SYUKKETU_SYUKKETU_SOUTAI", 3, 3, $extra);
        //出欠状況/欠席
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["SYUKKETU_SYUKKETU_KESSEKI"];
        $arg["data"]["SYUKKETU_SYUKKETU_KESSEKI"] = knjCreateTextBox($objForm, $value, "SYUKKETU_SYUKKETU_KESSEKI", 3, 3, $extra);

        //教科・科目/遅刻
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["SYUKKETU_KYOUKA_TIKOKU"];
        $arg["data"]["SYUKKETU_KYOUKA_TIKOKU"] = knjCreateTextBox($objForm, $value, "SYUKKETU_KYOUKA_TIKOKU", 3, 3, $extra);
        //教科・科目/早退
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["SYUKKETU_KYOUKA_SOUTAI"];
        $arg["data"]["SYUKKETU_KYOUKA_SOUTAI"] = knjCreateTextBox($objForm, $value, "SYUKKETU_KYOUKA_SOUTAI", 3, 3, $extra);
        //教科・科目/欠課
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["SYUKKETU_KYOUKA_KEKKA"];
        $arg["data"]["SYUKKETU_KYOUKA_KEKKA"] = knjCreateTextBox($objForm, $value, "SYUKKETU_KYOUKA_KEKKA", 3, 3, $extra);

        //教科以外/遅刻
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["SYUKKETU_IGAI_TIKOKU"];
        $arg["data"]["SYUKKETU_IGAI_TIKOKU"] = knjCreateTextBox($objForm, $value, "SYUKKETU_IGAI_TIKOKU", 3, 3, $extra);
        //教科以外/早退
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["SYUKKETU_IGAI_SOUTAI"];
        $arg["data"]["SYUKKETU_IGAI_SOUTAI"] = knjCreateTextBox($objForm, $value, "SYUKKETU_IGAI_SOUTAI", 3, 3, $extra);
        //教科以外/欠課
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["SYUKKETU_IGAI_KEKKA"];
        $arg["data"]["SYUKKETU_IGAI_KEKKA"] = knjCreateTextBox($objForm, $value, "SYUKKETU_IGAI_KEKKA", 3, 3, $extra);
        /************************** 皆勤者 **************************/
        //皆勤者/遅刻
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["KAIKIN_KAIKIN_TIKOKU"];
        $arg["data"]["KAIKIN_KAIKIN_TIKOKU"] = knjCreateTextBox($objForm, $value, "KAIKIN_KAIKIN_TIKOKU", 3, 3, $extra);
        //皆勤者/早退
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["KAIKIN_KAIKIN_SOUTAI"];
        $arg["data"]["KAIKIN_KAIKIN_SOUTAI"] = knjCreateTextBox($objForm, $value, "KAIKIN_KAIKIN_SOUTAI", 3, 3, $extra);
        //皆勤者/欠席
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["KAIKIN_KAIKIN_KESSEKI"];
        $arg["data"]["KAIKIN_KAIKIN_KESSEKI"] = knjCreateTextBox($objForm, $value, "KAIKIN_KAIKIN_KESSEKI", 3, 3, $extra);
        //皆勤者/欠課
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["KAIKIN_KAIKIN_KEKKA"];
        $arg["data"]["KAIKIN_KAIKIN_KEKKA"] = knjCreateTextBox($objForm, $value, "KAIKIN_KAIKIN_KEKKA", 3, 3, $extra);
        //皆勤者/授業遅刻
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["KAIKIN_KAIKIN_JUGYO_TIKOKU"];
        $arg["data"]["KAIKIN_KAIKIN_JUGYO_TIKOKU"] = knjCreateTextBox($objForm, $value, "KAIKIN_KAIKIN_JUGYO_TIKOKU", 3, 3, $extra);

        //精勤者/遅刻
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["KAIKIN_SEIKIN_TIKOKU"];
        $arg["data"]["KAIKIN_SEIKIN_TIKOKU"] = knjCreateTextBox($objForm, $value, "KAIKIN_SEIKIN_TIKOKU", 3, 3, $extra);
        //精勤者/早退
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["KAIKIN_SEIKIN_SOUTAI"];
        $arg["data"]["KAIKIN_SEIKIN_SOUTAI"] = knjCreateTextBox($objForm, $value, "KAIKIN_SEIKIN_SOUTAI", 3, 3, $extra);
        //精勤者/欠席
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["KAIKIN_SEIKIN_KESSEKI"];
        $arg["data"]["KAIKIN_SEIKIN_KESSEKI"] = knjCreateTextBox($objForm, $value, "KAIKIN_SEIKIN_KESSEKI", 3, 3, $extra);
        //精勤者/欠課
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["KAIKIN_SEIKIN_KEKKA"];
        $arg["data"]["KAIKIN_SEIKIN_KEKKA"] = knjCreateTextBox($objForm, $value, "KAIKIN_SEIKIN_KEKKA", 3, 3, $extra);
        //精勤者/授業遅刻
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["KAIKIN_SEIKIN_JUGYO_TIKOKU"];
        $arg["data"]["KAIKIN_SEIKIN_JUGYO_TIKOKU"] = knjCreateTextBox($objForm, $value, "KAIKIN_SEIKIN_JUGYO_TIKOKU", 3, 3, $extra);
        //精勤者/SHR・遅刻
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["KAIKIN_SEIKIN_SHR_TIKOKU"];
        $arg["data"]["KAIKIN_SEIKIN_SHR_TIKOKU"] = knjCreateTextBox($objForm, $value, "KAIKIN_SEIKIN_SHR_TIKOKU", 3, 3, $extra);
        //精勤者/SHR・早退
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $value = $model->field["KAIKIN_SEIKIN_SHR_SOUTAI"];
        $arg["data"]["KAIKIN_SEIKIN_SHR_SOUTAI"] = knjCreateTextBox($objForm, $value, "KAIKIN_SEIKIN_SHR_SOUTAI", 3, 3, $extra);

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
        //CSV出力(成績優良者)
        $extra = "onclick=\"return btn_submit('csv1');\"";
        $arg["button"]["btn_csv1"] = knjCreateBtn($objForm, "btn_csv1", "CSV出力", $extra);
        //CSV出力(成績不振者)
        $extra = "onclick=\"return btn_submit('csv2');\"";
        $arg["button"]["btn_csv2"] = knjCreateBtn($objForm, "btn_csv2", "CSV出力", $extra);
        //CSV出力(成績不振者、特別活動)
        $extra = "onclick=\"return btn_submit('csv2_1');\"";
        $disabled = ($model->field["SYUKKETU_SYUKEI"] == "1") ? " disabled" : "";
        $arg["button"]["btn_csv2_1"] = knjCreateBtn($objForm, "btn_csv2_1", "CSV出力", $extra.$disabled);
        //CSV出力(出欠状況)
        $extra = "onclick=\"return btn_submit('csv3');\"";
        $arg["button"]["btn_csv3"] = knjCreateBtn($objForm, "btn_csv3", "CSV出力", $extra);
        //CSV出力(出欠状況)
        $extra = "onclick=\"return btn_submit('csv4');\"";
        $arg["button"]["btn_csv4"] = knjCreateBtn($objForm, "btn_csv4", "CSV出力", $extra);
        //CSV出力(皆勤者)
        $extra = "onclick=\"return btn_submit('csv5');\"";
        $arg["button"]["btn_csv5"] = knjCreateBtn($objForm, "btn_csv5", "CSV出力", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "YEAR",  CTRL_YEAR);
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD232Y");
        knjCreateHidden($objForm, "SDATE", $model->control["学期開始日付"][$model->field["GAKKI2"]]); //学期開始日
        knjCreateHidden($objForm, "EDATE", $model->control["学期終了日付"][$model->field["GAKKI2"]]); //学期終了日
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd232yForm1.html", $arg); 
    }
}
?>
