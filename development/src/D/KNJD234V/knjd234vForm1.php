<?php

require_once('for_php7.php');

class knjd234vForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd234vForm1", "POST", "knjd234vindex.php", "", "knjd234vForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $z010 = $db->getOne(knjd234vQuery::getZ010($model));
        $model->isReitaku = ($z010 == "reitaku" ? true : false);
        if ($model->isReitaku) {
            $arg["isReitaku"] = "1";
        } else {
            $arg["isReitaku"] = "";
        }

        /**********/
        /* コンボ */
        /**********/
        //学年
        $query = knjd234vQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");
        
        //学科
        $query = knjd234vQuery::getMajor($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "MAJOR", $model->field["MAJOR"], $extra, 1, "");

        //学期
        $query = knjd234vQuery::getSemester($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);
        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }
        
        //学校校種を取得
        $model->schoolKind = $db->getOne(knjd234vQuery::getSchoolkindQuery($model->field["GRADE"]));
        knjCreateHidden($objForm, "setSchoolKind", $model->schoolKind);

        //テスト名
        $query = knjd234vQuery::getTest($model, 1);
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        $add = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            $add = true;
            if ($model->test_cd == $row["VALUE"]) $value_flg = true;
        }
        if ($add == false) {
            $query = knjd234vQuery::getTest($model, 2);
            $opt = array();
            $value_flg = false;
            $result = $db->query($query);
            $add = false;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
                $add = true;
                if ($model->test_cd == $row["VALUE"]) $value_flg = true;
            }
        }

        $model->test_cd = ($model->test_cd && $value_flg) ? $model->test_cd : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('main');\"";
        $arg["data"]["TEST_CD"] = knjCreateCombo($objForm, "TEST_CD", $model->test_cd, $opt, $extra, 1);

        /****************/
        /* ラジオボタン */
        /****************/
        //出欠集計範囲(累計･学期)ラジオボタン 1:累計 2:学期
        $model->field["DATE_DIV"] = $model->field["DATE_DIV"] ? $model->field["DATE_DIV"] : '1';
        $opt_datediv = array(1, 2);
        $extra2 = " onclick=\"return btn_submit('main');\"";
        $extra = array("id=\"DATE_DIV1\"".$extra2, "id=\"DATE_DIV2\"".$extra2);
        $radioArray = knjCreateRadio($objForm, "DATE_DIV", $model->field["DATE_DIV"], $extra, $opt_datediv, get_count($opt_datediv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //仮評定を出力する
        if  ($model->cmd == "") {
            $model->field["KARI_HYOTEI"] = "1";
        }
        if ($model->semester == '9' && $model->test_cd == '990009') { // 学年評定
            $arg["setKariHyotei"] = "1";
            $extra = "id=\"KARI_HYOTEI\" ".($model->field["KARI_HYOTEI"] == "1" ? " checked " : "");
            $arg["data"]["KARI_HYOTEI"] = knjCreateCheckBox($objForm, "KARI_HYOTEI", "1", $extra);
        }

        //前定期考査順位出力 1:学級 2:学年 3:コース 4:学科
        $model->field["OUTPUT_RANK_BEFORE"] = $model->field["OUTPUT_RANK_BEFORE"] ? $model->field["OUTPUT_RANK_BEFORE"] : '2';
        $scoreDiv = substr($model->test_cd, 4);
        //if ("01" == $scoreDiv) { // テスト種別小区分が'01'(素点)のみ
            $arg["setZenTeikiKousa"] = "1";

            $query = knjd234vQuery::getTestBefore($model, 1);
            $opt = array();
            $value_flg = false;
            $result = $db->query($query);
            $add = false;
            $opt[] = array('label' => '',
                           'value' => '');
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
                $add = true;
                if ($model->cmd != '' && $row["VALUE"] == $model->test_cd_before) {
                    $value_flg = true;
                }
            }
            if ($add == false) {
                $query = knjd234vQuery::getTestBefore($model, 2);
                $value_flg = false;
                $result = $db->query($query);
                $add = false;
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
                    $add = true;
                    if ($model->cmd != '' && $row["VALUE"] == $model->test_cd_before) {
                        $value_flg = true;
                    }
                }
            }
            $model->test_cd_before = $value_flg ? $model->test_cd_before : $opt[0]["value"];
            $extra = " id=\"TEST_CD_BEFORE\" ";
            $arg["data"]["TEST_CD_BEFORE"] = knjCreateCombo($objForm, "TEST_CD_BEFORE", $model->test_cd_before, $opt, $extra, 1);

            $opt_rank = array(1, 2, 3, 4);
            $extra = array("id=\"OUTPUT_RANK_BEFORE1\"", "id=\"OUTPUT_RANK_BEFORE2\"", "id=\"OUTPUT_RANK_BEFORE3\"", "id=\"OUTPUT_RANK_BEFORE4\"");
            $radioArray = knjCreateRadio($objForm, "OUTPUT_RANK_BEFORE", $model->field["OUTPUT_RANK_BEFORE"], $extra, $opt_rank, get_count($opt_rank));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        //} else {
        //    knjCreateHidden($objForm, "OUTPUT_RANK_BEFORE", $model->field["OUTPUT_RANK_BEFORE"]);
        //}

        //順位の基準点 1:総計 2:平均点
        $model->field["OUTPUT_KIJUN"] = $model->field["OUTPUT_KIJUN"] ? $model->field["OUTPUT_KIJUN"] : '1';
        $opt_kijun = array(1, 2);
        $extra = array("id=\"OUTPUT_KIJUN1\"", "id=\"OUTPUT_KIJUN2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_KIJUN", $model->field["OUTPUT_KIJUN"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //備考欄 1:出身学校 2:部活動 3:進路希望 4:空白
        $model->field["OUTPUT_REMARK"] = $model->field["OUTPUT_REMARK"] ? $model->field["OUTPUT_REMARK"] : '1';
        $opt_remark = array(1, 2, 3, 4);
        $extra = array("id=\"OUTPUT_REMARK1\"", "id=\"OUTPUT_REMARK2\"", "id=\"OUTPUT_REMARK3\"", "id=\"OUTPUT_REMARK4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_REMARK", $model->field["OUTPUT_REMARK"], $extra, $opt_remark, get_count($opt_remark));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出欠集計開始日付
        $sDate = $model->control["学期開始日付"][$model->field["SEMESTER"]];//日付がない場合、学期開始日付を使用する。
        $sDate = str_replace("-", "/", $sDate);
        //累計の場合、出欠集計範囲の開始日は、学期詳細マスタの詳細コード＝１の開始日とする。
        if ($model->field["DATE_DIV"] == "1") {
            $query = knjd234vQuery::getSemesterDetailMst("1");
            $result = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (strlen($result["SDATE"])) {
                $sDate = $result["SDATE"];
            }
            $sDate = str_replace("-", "/", $sDate);
        }
        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //出欠集計終了日付
        $query = knjd234vQuery::get_semester_detail($model);
        $semester_detail = $db->getOne($query);
        $query = knjd234vQuery::getSemesterDetailMst($semester_detail);
        $result = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (strlen($result["EDATE"])) {
            $eDate = $result["EDATE"];
        } else {
            $eDate = CTRL_DATE;//日付がない場合、学籍処理日を使用する。
        }
        $eDate = str_replace("-", "/", $eDate);
        //日付が学期の範囲外の場合、学期終了日付を使用する。
        if ($eDate < $model->control["学期開始日付"][$model->field["SEMESTER"]] || 
            $eDate > $model->control["学期終了日付"][$model->field["SEMESTER"]]) {
            $eDate = $model->control["学期終了日付"][$model->field["SEMESTER"]];
        }
        knjCreateHidden($objForm, "DATE", $eDate);
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $eDate);

        // 表示順位指定チェックボックス
        foreach ($model->rankForArray as $rf) {
            foreach ($model->rankArray as $r) {
                $name = $rf."_".$r."_RANK";
                $extra = $model->field[$name] == "1" || $model->cmd == "" ? "checked" : "";
                $extra .= " id=\"".$name."\" ";
                $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "1", $extra, "");
            }
        }

        /********************/
        /* テキストボックス */
        /********************/
        if ($model->isReitaku) {
            //成績不振者
            //成績不振者出力
            $extra = $model->cmd == '' || $model->field["OUTPUT_SLUMP"] == "1" ? "checked" : "";
            $extra .= " id=\"OUTPUT_SLUMP\"";
            $arg["data"]["OUTPUT_SLUMP"] = knjCreateCheckBox($objForm, "OUTPUT_SLUMP", "1", $extra, "");
            $extra = " onBlur=\"return this.value=toInteger(this.value);\" style=\"text-align: right;\"";
            //項目表示
            if (!$model->field["SLUMP_TENSU_INF2"]) {
                $model->field["SLUMP_TENSU_INF2"] = "3";
            }
            $extraid = " id=\"SLUMP_TENSU_INF2\"";
            $arg["data"]["SLUMP_TENSU_INF2"] = knjCreateTextBox($objForm, $model->field["SLUMP_TENSU_INF2"], "SLUMP_TENSU_INF2", 2, 2, $extra.$extraid);
        }

        //要追指導
        //要追指導出力
        $extra = $model->cmd == '' || $model->field["OUTPUT_SHIDOU"] == "1" ? "checked" : "";
        $extra .= " id=\"OUTPUT_SHIDOU\"";
        $arg["data"]["OUTPUT_SHIDOU"] = knjCreateCheckBox($objForm, "OUTPUT_SHIDOU", "1", $extra, "");
        $extra = " onBlur=\"return this.value=toInteger(this.value);\" style=\"text-align: right;\"";
        //項目表示
        //初期値セット
        if (!$model->field["SHIDOU_TENSU_INF2"]) {
            $query = knjd234vQuery::getAssessHigh();
            $result = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->field["SHIDOU_TENSU_INF2"] = $result["ASSESSHIGH"] ? $result["ASSESSHIGH"] : "39";
        }
        $model->field["SHIDOU_KAMOKUSU_INF2"] = $model->field["SHIDOU_KAMOKUSU_INF2"] ? $model->field["SHIDOU_KAMOKUSU_INF2"] : "1";
        
        $extraid = " id=\"SHIDOU_TENSU_INF2\"";
        $arg["data"]["SHIDOU_TENSU_INF2"] = knjCreateTextBox($objForm, $model->field["SHIDOU_TENSU_INF2"], "SHIDOU_TENSU_INF2", 2, 2, $extra.$extraid);
        $extraid = " id=\"SHIDOU_KAMOKUSU_INF2\"";
        $arg["data"]["SHIDOU_KAMOKUSU_INF2"] = knjCreateTextBox($objForm, $model->field["SHIDOU_KAMOKUSU_INF2"], "SHIDOU_KAMOKUSU_INF2", 2, 2, $extra.$extraid);
        
        //出欠状況を出力する
        $extra = $model->cmd == '' || $model->field["OUTPUT_SHUKKETSU"] == "1" ? "checked" : "";
        $extra .= " id=\"OUTPUT_SHUKKETSU\"";
        $arg["data"]["OUTPUT_SHUKKETSU"] = knjCreateCheckBox($objForm, "OUTPUT_SHUKKETSU", "1", $extra, "");
        //出欠状況
        $extra = " onBlur=\"return this.value=toInteger(this.value);\" style=\"text-align: right;\"";
        $extraid = " id=\"KESSEKI\"";
        $arg["data"]["KESSEKI"] = knjCreateTextBox($objForm, $model->field["KESSEKI"], "KESSEKI", 2, 2, $extra.$extraid);
        $extraid = " id=\"CHIKOKU\"";
        $arg["data"]["CHIKOKU"] = knjCreateTextBox($objForm, $model->field["CHIKOKU"], "CHIKOKU", 2, 2, $extra.$extraid);
        $extraid = " id=\"SOUTAI\"";
        $arg["data"]["SOUTAI"] = knjCreateTextBox($objForm, $model->field["SOUTAI"], "SOUTAI", 2, 2, $extra.$extraid);
        
        //教科・科目を出力する
        $extra = $model->cmd == '' || $model->field["OUTPUT_KYOKAKAMOKU"] == "1" ? "checked" : "";
        $extra .= " id=\"OUTPUT_KYOKAKAMOKU\"";
        $arg["data"]["OUTPUT_KYOKAKAMOKU"] = knjCreateCheckBox($objForm, "OUTPUT_KYOKAKAMOKU", "1", $extra, "");
        //教科・科目の欠席日数、欠課時数
        $extra = " id=\"NISSUU_BUNBO\"";
        $arg["data"]["NISSUU_BUNBO"] = knjCreateTextBox($objForm, $model->field["NISSUU_BUNBO"], "NISSUU_BUNBO", 2, 2, $extra);
        $extra = " id=\"NISSUU_BUNSHI\"";
        $arg["data"]["NISSUU_BUNSHI"] = knjCreateTextBox($objForm, $model->field["NISSUU_BUNSHI"], "NISSUU_BUNSHI", 2, 2, $extra);
        $extra = " id=\"JISUU_BUNBO\"";
        $arg["data"]["JISUU_BUNBO"] = knjCreateTextBox($objForm, $model->field["JISUU_BUNBO"], "JISUU_BUNBO", 2, 2, $extra);
        $extra = " id=\"JISUU_BUNSHI\"";
        $arg["data"]["JISUU_BUNSHI"] = knjCreateTextBox($objForm, $model->field["JISUU_BUNSHI"], "JISUU_BUNSHI", 2, 2, $extra);

        //度数分布を出力する
        $extra = $model->cmd == '' || $model->field["OUTPUT_DOSUBUPU"] == "1" ? "checked" : "";
        $extra .= " id=\"OUTPUT_DOSUBUPU\" ";
        $arg["data"]["OUTPUT_DOSUBUPU"] = knjCreateCheckBox($objForm, "OUTPUT_DOSUBUPU", "1", $extra, "");
        //度数分布 コース別に出力
        $model->field["DOSUBUPU_COURSE"] = $model->cmd == "" ? "1" : $model->field["DOSUBUPU_COURSE"];
        $extra = "id=\"DOSUBUPU_COURSE\" ".($model->field["DOSUBUPU_COURSE"] == "1" ? " checked " : "");
        $arg["data"]["DOSUBUPU_COURSE"] = knjCreateCheckBox($objForm, "DOSUBUPU_COURSE", "1", $extra);

        //優良者を出力する
        $extra = $model->cmd == '' || $model->field["OUTPUT_YURYO"] == "1" ? "checked" : "";
        $extra .= " id=\"OUTPUT_YURYO\" ";
        $arg["data"]["OUTPUT_YURYO"] = knjCreateCheckBox($objForm, "OUTPUT_YURYO", "1", $extra, "");
        //優良者
        $extra = " id=\"YURYO\" onBlur=\"return this.value=toInteger(this.value);\" style=\"text-align: right;\"";
        $model->field["YURYO"] = $model->field["YURYO"] ? $model->field["YURYO"] : "40";
        $arg["data"]["YURYO"] = knjCreateTextBox($objForm, $model->field["YURYO"], "YURYO", 3, 3, $extra);
        //コース別に出力
        $extra = "id=\"JOUI_COURSE\" ".($model->field["JOUI_COURSE"] == "1" ? " checked " : "");
        $arg["data"]["JOUI_COURSE"] = knjCreateCheckBox($objForm, "JOUI_COURSE", "1", $extra);

        //不振を出力する
        $extra = $model->cmd == '' || $model->field["OUTPUT_FUSHIN"] == "1" ? "checked" : "";
        $extra .= " id=\"OUTPUT_FUSHIN\" ";
        $arg["data"]["OUTPUT_FUSHIN"] = knjCreateCheckBox($objForm, "OUTPUT_FUSHIN", "1", $extra, "");
        //成績不振者 or 欠点科目数
        $opt = array(1, 2);
        if (!$model->field["FUSHIN_DIV"]) $model->field["FUSHIN_DIV"] = "1";
        $onclick = "";
        $extra = array("id=\"FUSHIN_DIV1\" ".$onclick
                     , "id=\"FUSHIN_DIV2\" ".$onclick
                      );
        $radioArray = knjCreateRadio($objForm, "FUSHIN_DIV", $model->field["FUSHIN_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        //不振者
        $extra = " id=\"FUSHIN\" onBlur=\"return this.value=toInteger(this.value);\" style=\"text-align: right;\"";
        $model->field["FUSHIN"] = $model->field["FUSHIN"] ? $model->field["FUSHIN"] : "40";
        $arg["data"]["FUSHIN"] = knjCreateTextBox($objForm, $model->field["FUSHIN"], "FUSHIN", 3, 3, $extra);
        //欠点科目数テキストボックス
        $model->field["KETTEN_COUNT"] = $model->field["KETTEN_COUNT"] ? $model->field["KETTEN_COUNT"] : "1";
        $extra  = " id=\"KETTEN_COUNT\" onblur=\"this.value=toInteger(this.value)\" style=\"text-align: right;\" ";
        $arg["data"]["KETTEN_COUNT"] = knjCreateTextBox($objForm, $model->field["KETTEN_COUNT"], "KETTEN_COUNT", 2, 2, $extra);
        //欠試者は除く
        $extra = "id=\"KESSHI_NOZOKU\" ".($model->field["KESSHI_NOZOKU"] == "1" ? " checked " : "");
        $arg["data"]["KESSHI_NOZOKU"] = knjCreateCheckBox($objForm, "KESSHI_NOZOKU", "1", $extra);
        //コース別に出力
        $extra = "id=\"KAI_COURSE\" ".($model->field["KAI_COURSE"] == "1" ? " checked " : "");
        $arg["data"]["KAI_COURSE"] = knjCreateCheckBox($objForm, "KAI_COURSE", "1", $extra);
        //欠点科目数順に表示
        $extra = $model->field["FUSHIN_ORDER_KETTEN_COUNT"] == "1" ? "checked" : "";
        $extra .= " id=\"FUSHIN_ORDER_KETTEN_COUNT\" ";
        $arg["data"]["FUSHIN_ORDER_KETTEN_COUNT"] = knjCreateCheckBox($objForm, "FUSHIN_ORDER_KETTEN_COUNT", "1", $extra, "");

        //休学者を対象外とする
        $extra = "id=\"KYUGAKU\" ".($model->field["KYUGAKU"] == "1" ? " checked " : "");
        $arg["data"]["KYUGAKU"] = knjCreateCheckBox($objForm, "KYUGAKU", "1", $extra);

        //D056
        $query = knjd234vQuery::getNameMst($model, 'D056');
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "D056", $model->field["D056"], $extra, 1, "BLANK");

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, $extra, 1);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //file
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        /**********/
        /* ボタン */
        /**********/
        //CSV出力
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
        //CSV取込
        $extra = "onclick=\"return btn_submit('csvExe');\"";
        $arg["button"]["btn_csvExe"] = knjCreateBtn($objForm, "btn_csvExe", "CSV取込", $extra);
        //エラー出力
        $extra = "onclick=\"return btn_submit('csvErr');\"";
        $arg["button"]["btn_errPut"] = knjCreateBtn($objForm, "btn_errPut", "エラー出力", $extra);
        //プレビュー/印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'print');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //CSV出力
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv_main');\"";
        $arg["button"]["btn_csv_main"] = knjCreateBtn($objForm, "btn_csv_main", "CSV出力", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
        knjCreateHidden($objForm, "PRGID",         "KNJD234V");
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "knjd234vPattern", $model->Properties["knjd234vPattern"]);
        knjCreateHidden($objForm, "knjd234vUseMajorcdKeta1", $model->Properties["knjd234vUseMajorcdKeta1"]);
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "knjd234vPrintTsusanKettenKamokusu", $model->Properties["knjd234vPrintTsusanKettenKamokusu"]);
        knjCreateHidden($objForm, "checkOutputEachItem", "1"); // 指示画面の出力チェックボックス判別用
        knjCreateHidden($objForm, "OUTPUT_RANK_SET", "1"); // 指示画面の順位表示チェックボックス判別用

        //学期
        knjCreateHidden($objForm, "SEME_DATE",  $seme);
        //学期開始日付
        knjCreateHidden($objForm, "SEME_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
        //学期終了日付
        knjCreateHidden($objForm, "SEME_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //仮評定出力保持
        if (!($model->semester == '9' && $model->test_cd == '990009')) { // 学年評定以外
            knjCreateHidden($objForm, "KARI_HYOTEI", $model->field["KARI_HYOTEI"]);
        }

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd234vForm1.html", $arg);
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
