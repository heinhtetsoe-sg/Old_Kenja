<?php

require_once('for_php7.php');


class knjs531form1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjs531index.php", "", "main");

        $db = Query::dbCheckOut();

        //画面切替（明細／一括）
        if ($model->batch) {
            $arg["batch"] = 1;
        } else {
            $arg["meisai"] = 1;
        }

        //画面サイズ
        $arg["WIDTH"] = "920";

        //年度コンボボックス
        $optNendo = array();
        $query = knjs531Query::selectYearQuery($model);
        $result = $db->query($query);

        //年度コンボ対象配列
        $optNendoKouho = array();
        if ($model->field["YEAR"])  {
            $optNendoKouho[$model->field["YEAR"]] = "";
        }
        while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optNendoKouho[$row1["VALUE"]] = "";
        }
        $result->free();

        //降順に並び替えし、コンボへ追加して表示する
        krsort($optNendoKouho);

        foreach ($optNendoKouho as $key => $val) {
            $optNendo[] = array('label' => $key,
                                'value' => $key);
        }

        if (get_count($optNendo) > 0) {
            foreach ($optNendo as $key => $val) {
                $ctrlFlg = ($val["value"] == CTRL_YEAR) ? true : $ctrlFlg;
            }
            if (strlen($model->field["YEAR"]) == 0 && $ctrlFlg) {
                $Data = CTRL_YEAR;
            } else if (strlen($model->field["YEAR"]) == 0 && !$ctrlFlg) {
                $Data = $optNendo[0]["value"];
            }
        }

        $model->field["YEAR"] = (strlen($model->field["YEAR"]) == 0) ? $Data : $model->field["YEAR"];
        $extra = "onchange=\"return btn_submit('main');\"";
        $setYear = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $optNendo, $extra, 1);

        //年度追加テキスト
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $setYearAdd = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);

        //年度追加ボタン
        $extra = "onclick=\"return add('year_add');\"";
        $setYearBtn = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

        $arg["YEAR"] = array("VAL" => $setYear."&nbsp;&nbsp;".
                                      $setYearAdd."&nbsp;".$setYearBtn);

        //学校区分コンボ
        $query = knjs531Query::getSchoolkind($model);
        $opt2 = array();
        $value_flg2 = false;
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt2[] = array('label' => $row2["LABEL"],
                            'value' => $row2["VALUE"]);
            if ($model->field["SCHOOL_KIND"] == $row2["VALUE"]) $value_flg2 = true;
        }
        $model->field["SCHOOL_KIND"] = ($model->field["SCHOOL_KIND"] && $value_flg2) ? $model->field["SCHOOL_KIND"] : $opt2[0]["value"];
        $extra = "onchange=\"return btn_submit('main');\"";
        $arg["info"]["SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $opt2, $extra, 1);

        if (!$model->batch && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
            //処理区分ラジオボタン 1:年組 2:個人（訪問生）
            $opt = array(1, 2);
            $model->field["SHORI_DIV"] = ($model->field["SHORI_DIV"] == "") ? "1" : $model->field["SHORI_DIV"];
            $click = " onclick=\"return btn_submit('main');\"";
            $extra = array("id=\"SHORI_DIV1\"".$click, "id=\"SHORI_DIV2\"".$click);
            $radioArray = knjCreateRadio($objForm, "SHORI_DIV", $model->field["SHORI_DIV"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["info"][$key] = $val;
            $arg["shori_div"] = 1;
        }

        //指定月を取得
        $opt = array();
        $opt[] = array('label' => "04月", 'value' => "04");
        $opt[] = array('label' => "05月", 'value' => "05");
        $opt[] = array('label' => "06月", 'value' => "06");
        $opt[] = array('label' => "07月", 'value' => "07");
        $opt[] = array('label' => "08月", 'value' => "08");
        $opt[] = array('label' => "09月", 'value' => "09");
        $opt[] = array('label' => "10月", 'value' => "10");
        $opt[] = array('label' => "11月", 'value' => "11");
        $opt[] = array('label' => "12月", 'value' => "12");
        $opt[] = array('label' => "01月", 'value' => "01");
        $opt[] = array('label' => "02月", 'value' => "02");
        $opt[] = array('label' => "03月", 'value' => "03");

        //月初期値
        list($year, $month, $day) = preg_split("/-/", CTRL_DATE);
        $model->field["MONTH"] = $model->field["MONTH"] ? $model->field["MONTH"] : $month;
        $extra = "onchange=\"return btn_submit('main');\"";
        $arg["info"]["MONTH"] = knjCreateCombo($objForm, "MONTH", $model->field["MONTH"], $opt, $extra, 1);

        if ($model->Properties["useSpecial_Support_Hrclass"] != '1' && $model->Properties["useFi_Hrclass"] == '1') {
            //クラス区分ラジオボタン 1:法定 2:複式
            $opt = array(1, 2);
            $model->field["HR_CLASS_DIV"] = ($model->field["HR_CLASS_DIV"] == "") ? "1" : $model->field["HR_CLASS_DIV"];
            $click = " onclick=\"return btn_submit('main');\"";
            $extra = array("id=\"HR_CLASS_DIV1\"".$click, "id=\"HR_CLASS_DIV2\"".$click);
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_DIV", $model->field["HR_CLASS_DIV"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["info"][$key] = $val;
            $arg["hr_class_div"] = 1;
        } else {
            $model->field["HR_CLASS_DIV"] = ($model->field["HR_CLASS_DIV"] == "") ? "1" : $model->field["HR_CLASS_DIV"];
            knjCreateHidden($objForm, "HR_CLASS_DIV", 1);
        }

        //年組コンボ
        $opt3 = array();
        $value_flg3 = false;

        //対象年組取得
        if ($model->batch) {
            $query = knjs531Query::getGrade($model);
            $arg["info"]["GRADE_HR_NAME"] = "学年";
            $extra = "";
            $model->field["GRADE_HR"] = substr($model->field["GRADE_HR"],0,2);
        } else {
            //複式クラスを使うプロパティ
            if ($model->field["HR_CLASS_DIV"] == '2') {
                $query = knjs531Query::getGradeFiClass($model);
            } else {
                $query = knjs531Query::getGradeHrclass($model);
            }
            $arg["info"]["GRADE_HR_NAME"] = "年組";
            $extra = "onchange=\"return btn_submit('main');\"";
        }
        $result = $db->query($query);

        $hr_class = "";
        while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt3[] = array('label' => $row3["LABEL"],
                            'value' => $row3["VALUE"]);

            //一括画面からメイン画面に移動のときの初期値設定
            if ((strlen($model->field["GRADE_HR"]) == 2) && ($model->field["GRADE_HR"] == substr($row3["VALUE"],0,2)) && !$hr_class) $hr_class = substr($row3["VALUE"],3);
            if ($hr_class && $value_flg3 == false) $model->field["GRADE_HR"] = $model->field["GRADE_HR"].':'.$hr_class;

            if ($model->field["GRADE_HR"] == $row3["VALUE"]) $value_flg3 = true;
        }
        $model->field["GRADE_HR"] = ($model->field["GRADE_HR"] && $value_flg3) ? $model->field["GRADE_HR"] : $opt3[0]["value"];
        $arg["info"]["GRADE_HR"] = knjCreateCombo($objForm, "GRADE_HR", $model->field["GRADE_HR"], $opt3, $extra, 1);

        $use_visitor = false;
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->field["SHORI_DIV"] == "2") {
            //訪問生コンボ
            $opt4 = array();
            $value_flg4 = false;
            $query = knjs531Query::getVisitor($model);
            $extra = "onchange=\"return btn_submit('main');\"";
            $result = $db->query($query);
            while ($row4 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt4[] = array('label' => $row4["LABEL"],
                                'value' => $row4["VALUE"]);

                if ($model->field["VISITOR"] == $row4["VALUE"]) $value_flg4 = true;
            }

            $model->field["VISITOR"] = ($model->field["VISITOR"] && $value_flg4) ? $model->field["VISITOR"] : $opt4[0]["value"];
            $arg["info"]["VISITOR"] = knjCreateCombo($objForm, "VISITOR", $model->field["VISITOR"], $opt4, $extra, 1);
            $arg["use_visitor"] = 1;
            $use_visitor = true;
        }
        knjCreateHidden($objForm, "use_visitor", $use_visitor);

        //行事予定登録処理、EVENT_DATデータ件数確認
        $query = knjs531Query::getEventcount($model);
        $getEvent = $db->getOne($query);
        knjCreateHidden($objForm, "GETEVENT", $getEvent);

        //指定月のEVENT_DATデータ件数確認
        $query = knjs531Query::getEventcountMonth($model);
        $getEventMonth = $db->getOne($query);
        knjCreateHidden($objForm, "GETEVENTMONTH", $getEventMonth);

        if ($use_visitor == true) {
            //指定月のEVENT_SCHREG_DATデータ件数確認
            $query = knjs531Query::getEventSchregCountMonth($model);
            $getEventSchregMonth = $db->getOne($query);
            knjCreateHidden($objForm, "GETEVENTSCHREGMONTH", $getEventSchregMonth);
        }

        if (!$model->batch) {
            //表示データ
            setDispData($objForm, $arg, $db, $model);

            if ($model->field["SHORI_DIV"] != "2") {
                //学年別一括反映ボタン
                $extra = "onclick=\"return btn_submit('batch');\"";
                $arg["button"]["btn_batch"] = KnjCreateBtn($objForm, "btn_batch", "学年別一括反映", $extra);
                $arg["use_btn_batch"] = 1;
            }

            //更新ボタン
            $extra = "onclick=\"return btn_submit('update');\"";
            $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

            //取消ボタン
            $extra = "onclick=\"return btn_submit('reset');\"";
            $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        }

        //個人（訪問生）の初期値・更新
        $extra = ($use_visitor == true && $model->field["VISITOR"]) ? "onclick=\"return btn_submit('sch_shokiti');\"" : "disabled";
        $arg["button"]["btn_sch_shokiti"] = knjCreateBtn($objForm, 'btn_sch_shokiti', '年組行事データ反映(指定月分)', $extra);

        //使用不可
        $disabled = "";
        if ($model->field["SHORI_DIV"] == "2") $disabled = " disabled";

        //初期値・更新
        $extra = "onclick=\"return btn_submit('shokitiyear');\"".$disabled;
        $arg["button"]["btn_shokitiyear"] = knjCreateBtn($objForm, 'shokitiyear', '行事マスタ反映(年度分)', $extra);

        //初期値・更新
        $extra = "onclick=\"return btn_submit('shokitimonth');\"".$disabled;
        $arg["button"]["btn_shokitimonth"] = knjCreateBtn($objForm, 'shokitimonth', '行事マスタ反映(指定月分)', $extra);

        //終了ボタン
        if ($model->batch) {
            $extra = "onclick=\"return btn_submit('back');\"";
            $label = '戻 る';
        } else {
            $extra = "onclick=\"return closeWin();\"";
            $label = '終 了';
        }
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', $label, $extra);

        //Hidden作成
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $row["UPDATED"]);
        knjCreateHidden($objForm, "batch");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjs531Form1.html", $arg);
    }
}

//データ表示用
function setDispData(&$objForm, &$arg, $db, $model) {

   //月初期値
   list($year, $month, $day) = preg_split("/-/", CTRL_DATE);
   $year = $model->field["YEAR"];
   $month = $model->field["MONTH"];
   $day = 1;
   //カレンダー配列 [曜日コード] : "WEEK" = 曜日
   //                              "DAY"[第〇週] = 日付
   $model->weekArray = array("1" => array("WEEK" => "月"),
                             "2" => array("WEEK" => "火"),
                             "3" => array("WEEK" => "水"),
                             "4" => array("WEEK" => "木"),
                             "5" => array("WEEK" => "金"),
                             "6" => array("WEEK" => "土"),
                             "0" => array("WEEK" => "日"));

   $year = $model->field["MONTH"] > 3 ? $model->field["YEAR"] : $model->field["YEAR"] + 1;
   //対象月の1日の曜日
   $daiItiYoubi = date("w", mktime( 0, 0, 0, $model->field["MONTH"], 1, $year));

   //対象月の最終日
   $last_day = date("t", mktime(0, 0, 0, $model->field["MONTH"], 1, $year));
   $last_date = date("Y-m-d", mktime(0, 0, 0, $model->field["MONTH"], $last_day, $year));

    //エラーメッセージが出たとき、保持したデータ取得
    $dataTmp = array();
    if (isset($model->warning)) {
        foreach ($model->updfield as $executeDate => $value) {
            foreach ($value as $key => $val) {
                $dataTmp[$executeDate][$key] = $val;
            }
        }
    }

   //日付のカウントアップ（対象月の最終日まで）
   $addCnt = 0;
   //曜日のカウントアップ
   $youbiCnt = 0;
   for ($i = 0; $i < $last_day; $i++) {
        //一ヶ月
        $setDay = date("Y-m-d", mktime( 0, 0, 0, $month, $day + $addCnt, $year));
        $setDayWa = date("Y年m月d日", mktime( 0, 0, 0, $month, $day + $addCnt, $year));
        //曜日のカウントアップ
        $daiItiYoubi = $daiItiYoubi + $youbiCnt;
        if ($daiItiYoubi > 6) {
            $daiItiYoubi = 0;
        }
        $addCnt++;
        //常に１ずつプラス
        $youbiCnt = 1;

        //対象データを取得
        $query = knjs531Query::countEventMst($model,$setDay);
        $eventCnt = $db->getOne($query);

        if ($model->Properties["useEventAbbv"] == "1") {
            $arg["disp_holiday_event"] = "1";
        }
        //エラーメッセージが出たとき、保持したデータをセット
        if (isset($model->warning)) {
            $row = $dataTmp[$setDay];

            //日時
            $opt["EXECUTEDATE"] = $setDayWa."(".$model->weekArray[$daiItiYoubi]["WEEK"].")";

            //休日のチェックボックス
            if ($row["HOLIDAY_FLG"] == "1") {
                $extra = "checked='checked' ";
            } else {
                $extra = "";
            }
            $opt["HOLIDAY_FLG"] = knjCreateCheckBox($objForm, "HOLIDAY_FLG_".$setDay, "1", $extra);

            //プロパティが立っている時は行事略称を表示
            if ($model->Properties["useEventAbbv"] == "1") {
                $extra = "";
                $opt["EVENT_ABBV"] = knjCreateTextBox($objForm, $row["EVENT_ABBV"], "EVENT_ABBV_".$setDay, 6, 3, $extra);
            }

            //行事名
            $extra = "";
            $opt["REMARK1"] = knjCreateTextBox($objForm, $row["REMARK1"], "REMARK1_".$setDay, 20, 20, $extra);

            //備考
            $extra = "";
            $opt["REMARK2"] = knjCreateTextBox($objForm, $row["REMARK2"], "REMARK2_".$setDay, 50, 50, $extra);

            $arg["data"][] = $opt;
        } else {
            if ($eventCnt > 0) {
                $query = knjs531Query::setEventMst($model,$setDay);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //日時
                    $opt["EXECUTEDATE"] = $setDayWa."(".$model->weekArray[$daiItiYoubi]["WEEK"].")";

                    //休日のチェックボックス
                    if ($row["HOLIDAY_FLG"] == "1") {
                        $extra = "checked='checked' ";
                    } else {
                        $extra = "";
                    }
                    $opt["HOLIDAY_FLG"] = knjCreateCheckBox($objForm, "HOLIDAY_FLG_".$setDay, "1", $extra);

                    //プロパティが立っている時は行事略称を表示
                    if ($model->Properties["useEventAbbv"] == "1") {
                        $extra = "";
                        $opt["EVENT_ABBV"] = knjCreateTextBox($objForm, $row["EVENT_ABBV"], "EVENT_ABBV_".$setDay, 6, 3, $extra);
                    }

                    //行事名
                    $extra = "";
                    $opt["REMARK1"] = knjCreateTextBox($objForm, $row["REMARK1"], "REMARK1_".$setDay, 20, 20, $extra);

                    //備考
                    $extra = "";
                    $opt["REMARK2"] = knjCreateTextBox($objForm, $row["REMARK2"], "REMARK2_".$setDay, 50, 50, $extra);

                    $arg["data"][] = $opt;
                }
                $result->free();
            } else {
                //日時
                $opt["EXECUTEDATE"] = $setDayWa."(".$model->weekArray[$daiItiYoubi]["WEEK"].")";

                //休日のチェックボックス
                if ($model->field["HOLIDAY_FLG"] == "1") {
                    $extra = "checked='checked' ";
                } else {
                    $extra = "";
                }
                $opt["HOLIDAY_FLG"] = knjCreateCheckBox($objForm, "HOLIDAY_FLG_".$setDay, "1", $extra);

                //プロパティが立っている時は行事略称を表示
                if ($model->Properties["useEventAbbv"] == "1") {
                    $extra = "";
                    $opt["EVENT_ABBV"] = knjCreateTextBox($objForm, $model->field["EVENT_ABBV"], "EVENT_ABBV_".$setDay, 6, 3, $extra);
                }

                //行事名
                $extra = "";
                $opt["REMARK1"] = knjCreateTextBox($objForm, $model->field["REMARK1"], "REMARK1_".$setDay, 20, 20, $extra);

                //備考
                $extra = "";
                $opt["REMARK2"] = knjCreateTextBox($objForm, $model->field["REMARK2"], "REMARK2_".$setDay, 50, 50, $extra);

                $arg["data"][] = $opt;
            }
        }
    }
}

?>
