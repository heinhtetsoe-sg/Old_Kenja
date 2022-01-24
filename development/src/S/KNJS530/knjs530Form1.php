<?php

require_once('for_php7.php');


class knjs530form1 {
    function main(&$model) {
        
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjs530index.php", "", "main");

        $db = Query::dbCheckOut();

        //年度コンボボックス
        $optNendo = array();
        $query = knjs530Query::selectYearQuery();
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
        
        //textbox
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $setYearAdd = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);

        //button
        $extra = "onclick=\"return add('year_add');\"";
        $setYearBtn = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

        $arg["YEAR"] = array( "VAL" => $setYear."&nbsp;&nbsp;".
                                       $setYearAdd."&nbsp;".$setYearBtn);
        
        //行事マスタ区分コンボ
        $opt1 = array();
        $value_flg1 = false;
        $opt1[] = array('label' => "1:学校", 'value' => "1");
        $opt1[] = array('label' => "2:学年", 'value' => "2");
        if ($model->field["GRADE"] == ($opt1[0]["value"] || $opt1[1]["value"])) $value_flg1 = true;
        
        $model->field["DATA_DIV"] = ($model->field["DATA_DIV"] && $value_flg1) ? $model->field["DATA_DIV"] : $opt1[0]["value"];
        $extra = "onchange=\"return btn_submit('main');\"";
        $arg["info"]["DATA_DIV"] = knjCreateCombo($objForm, "DATA_DIV", $model->field["DATA_DIV"], $opt1, $extra, 1);
        
        //学校区分コンボ
        $query = knjs530Query::getSchoolkind($model);
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
        
        //学年コンボ
        $opt3 = array();
        $value_flg3 = false;
        if ($model->field["DATA_DIV"] == '2') {
            //対象年度取得
            $query = knjs530Query::getGrade($model);
            $result = $db->query($query);
            
            while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt3[] = array('label' => $row3["LABEL"],
                               'value' => $row3["VALUE"]);
                if ($model->field["GRADE"] == $row3["VALUE"]) $value_flg3 = true;
            }
            $model->field["GRADE"] = ($model->field["GRADE"] && $value_flg3) ? $model->field["GRADE"] : $opt3[0]["value"];
            
        } else {
            $opt3[] = array('label' => "学校用", 'value' => "00");
            if ($model->field["GRADE"] == "00") $value_flg3 = true;
            $model->field["GRADE"] = ($model->field["GRADE"] && $value_flg3 ) ? $model->field["GRADE"] : $opt3[0]["value"];
        }
        $extra = "onchange=\"return btn_submit('main');\"";
        $arg["info"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $opt3, $extra, 1);
        
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
        $extra = "onchange=\"return btn_submit('monthmain');\"";
        $arg["info"]["MONTH"] = knjCreateCombo($objForm, "MONTH", $model->field["MONTH"], $opt, $extra, 1);

        //クラス区分
        if ($model->Properties["useFi_Hrclass"] == '1' && $model->field["DATA_DIV"] == "2" && $model->field["SCHOOL_KIND"] != "H") {
            //クラス区分ラジオボタン 1:法定 2:複式
            $opt = array(1, 2);
            $model->field["HR_CLASS_DIV"] = ($model->field["HR_CLASS_DIV"] == "") ? "1" : $model->field["HR_CLASS_DIV"];
            $click = " onclick=\"return btn_submit('main');\"";
            $extra = array("id=\"HR_CLASS_DIV1\"".$click, "id=\"HR_CLASS_DIV2\"".$click);
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_DIV", $model->field["HR_CLASS_DIV"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["info"][$key] = $val;
            $arg["hr_class_div"] = 1;
        } else {
            $hr_class_div = ($model->Properties["useFi_Hrclass"] == '1') ? "2" : "1";
            $model->field["HR_CLASS_DIV"] = ($model->field["HR_CLASS_DIV"] == "") ? $hr_class_div : $model->field["HR_CLASS_DIV"];
            knjCreateHidden($objForm, "HR_CLASS_DIV", $hr_class_div);
        }

        //学科コンボ(高校)と年組コンボ(小学校、中学校)
        //高校
        if ($model->field["SCHOOL_KIND"] == 'H') {
            knjCreateHidden($objForm, "HR_CLASS", '000');

            $query = knjs530Query::getCourdeMajor($model);
            $opt4 = array();
            $value_flg4 = false;
            $opt4[] = array('label' => '-- 全て --', 'value' => '0-000');
            $result = $db->query($query);
            while ($row4 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt4[] = array('label' => $row4["LABEL"],
                                'value' => $row4["VALUE"]);
                if ($model->field["COURSE_MAJOR"] == $row4["VALUE"]) $value_flg4 = true;
            }
            $model->field["COURSE_MAJOR"] = ($model->field["COURSE_MAJOR"] && $value_flg4) ? $model->field["COURSE_MAJOR"] : $opt4[0]["value"];
            $extra = ($model->field["DATA_DIV"] == "2") ? "onchange=\"return btn_submit('main');\"" : " disabled";

            $arg["info"]["COURSE_MAJOR"] = knjCreateCombo($objForm, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $opt4, $extra, 1);
            $arg["info"]["TITLE"]  = '学科';

            if ($model->field["DATA_DIV"] != "2") knjCreateHidden($objForm, "COURSE_MAJOR", '0-000');
        //小学校、中学校
        } else {
            $model->field["COURSE_MAJOR"] = '0-000';
            knjCreateHidden($objForm, "COURSE_MAJOR", '0-000');

            //複式クラスを使うプロパティ
            if ($model->field["HR_CLASS_DIV"] == '2') {
                $query = knjs530Query::getFiClass($model);
            } else {
                $query = knjs530Query::getHrClass($model);
            }
            $opt = array();
            $value_flg = false;
            $opt[] = array('label' => '-- 全て --', 'value' => '000');
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
                if ($model->field["HR_CLASS"] == $row["VALUE"]) $value_flg = true;
            }
            $model->field["HR_CLASS"] = ($model->field["HR_CLASS"] && $value_flg) ? $model->field["HR_CLASS"] : $opt[0]["value"];
            $extra = ($model->field["DATA_DIV"] == "2") ? "onchange=\"return btn_submit('main');\"" : " disabled";
            
            $arg["info"]["HR_CLASS"] = knjCreateCombo($objForm, "HR_CLASS", $model->field["HR_CLASS"], $opt, $extra, 1);
            $arg["info"]["TITLE"] = '年組';
            
            if ($model->field["DATA_DIV"] != "2") knjCreateHidden($objForm, "HR_CLASS", '000');
        }

        /****************/
        /*  コピー処理  */
        /****************/
        if ($model->field["DATA_DIV"] == "2") {
            //コピー先学年コンボ
            $query = knjs530Query::getGrade($model);
            $opt5 = array();
            $value_flg5 = false;
            $result = $db->query($query);
            while ($row5 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt5[] = array('label' => $row5["LABEL"],
                                'value' => $row5["VALUE"]);
                if ($model->field["COPY_GRADE"] == $row5["VALUE"]) $value_flg5 = true;
            }
            $model->field["COPY_GRADE"] = ($model->field["COPY_GRADE"] && $value_flg5) ? $model->field["COPY_GRADE"] : $opt5[0]["value"];
            $extra = "onchange=\"return btn_submit('main');\"";
            $arg["copy"]["COPY_GRADE"] = knjCreateCombo($objForm, "COPY_GRADE", $model->field["COPY_GRADE"], $opt5, $extra, 1);

            //コピー先学科コンボまたは年組コンボ
            //高校
            if ($model->field["SCHOOL_KIND"] == 'H') {
                knjCreateHidden($objForm, "COPY_HR_CLASS", '000');
                $query = knjs530Query::getCourdeMajor($model);
                $opt6 = array();
                $value_flg6 = false;
                $opt6[] = array('label' => '-- 全て --', 'value' => '0-000');
                $result = $db->query($query);
                while ($row6 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt6[] = array('label' => $row6["LABEL"],
                                    'value' => $row6["VALUE"]);
                    if ($model->field["COPY_MAJOR"] == $row6["VALUE"]) $value_flg6 = true;
                }
                $model->field["COPY_MAJOR"] = ($model->field["COPY_MAJOR"] && $value_flg6) ? $model->field["COPY_MAJOR"] : $opt6[0]["value"];

                $extra = "onchange=\"return btn_submit('main');\"";
                $arg["copy"]["COPY_MAJOR"] = knjCreateCombo($objForm, "COPY_MAJOR", $model->field["COPY_MAJOR"], $opt6, $extra, 1);
                $arg["copy"]["COPY_TITLE"]  = '学科：';
            //小学校、中学校
            } else {
                $model->field["COPY_MAJOR"] = '0-000';
                knjCreateHidden($objForm, "COPY_MAJOR", '0-000');
                //複式クラスを使うプロパティ
                if ($model->field["HR_CLASS_DIV"] == '2') {
                    $query = knjs530Query::getFiClass($model, "copy");
                } else {
                    $query = knjs530Query::getHrClass($model, "copy");
                }
                $opt = array();
                $value_flg = false;
                $opt[] = array('label' => '-- 全て --', 'value' => '000');
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
                    if ($model->field["COPY_HR_CLASS"] == $row["VALUE"]) $value_flg = true;
                }
                $model->field["COPY_HR_CLASS"] = ($model->field["COPY_HR_CLASS"] && $value_flg) ? $model->field["COPY_HR_CLASS"] : $opt[0]["value"];

                $extra = "onchange=\"return btn_submit('main');\"";
                $arg["copy"]["COPY_HR_CLASS"] = knjCreateCombo($objForm, "COPY_HR_CLASS", $model->field["COPY_HR_CLASS"], $opt, $extra, 1);
                $arg["copy"]["COPY_TITLE"]  = ($model->field["HR_CLASS_DIV"] == '2') ? '複式年組：' : '年組：';
            }

            //コピーボタン（指定年度分）
            $extra = "onclick=\"return btn_submit('copy_year');\"";
            $arg["button"]["btn_copy_year"] = knjCreateBtn($objForm, 'btn_copy_year', '指定年度分コピー', $extra);

            //コピーボタン（指定年月分）
            $extra = "onclick=\"return btn_submit('copy_month');\"";
            $arg["button"]["btn_copy_month"] = knjCreateBtn($objForm, 'btn_copy_month', '指定年月分コピー', $extra);

            //コピー先の学校校種取得
            $query = knjs530Query::getCopySchoolKind($model);
            $getSchoolKind = $db->getOne($query);
            knjCreateHidden($objForm, "COPY_SCHOOL_KIND", $getSchoolKind);

            //コピー先のデータ件数確認（指定年度）
            $query = knjs530Query::getCopyEventcount($model, $getSchoolKind);
            $getCopyEventCnt = $db->getOne($query);
            knjCreateHidden($objForm, "COPY_EVENT_CNT", $getCopyEventCnt);

            //コピー先のデータ件数確認（指定年月）
            $query = knjs530Query::getCopyEventcount($model, $getSchoolKind, "month");
            $getCopyEventCntMonth = $db->getOne($query);
            knjCreateHidden($objForm, "COPY_EVENT_CNT_MONTH", $getCopyEventCntMonth);
        }

        //表示データ
        setDispData($objForm, $arg, $db, $model);
        
        //行事予定登録処理、EVENT_MSTデータ件数確認
        $query = knjs530Query::getEventcount($model);
        $getEvent = $db->getOne($query);
        knjCreateHidden($objForm, "GETEVENT", $getEvent);
        
        //指定月のEVENT_MSTデータ件数確認
        $query = knjs530Query::getEventcountMonth($model);
        $getEventMonth = $db->getOne($query);
        knjCreateHidden($objForm, "GETEVENTMONTH", $getEventMonth);
        
        //対象年度の学年データ存在確認
        $query = knjs530Query::getGradeCount($model);
        $getGrade = $db->getOne($query);
        knjCreateHidden($objForm, "GETGRADE", $getGrade);
        
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);
        
        //公休・休日反映(年度分)・更新
        $extra = "onclick=\"return btn_submit('shokitiyear');\"";
        $arg["button"]["btn_shokitiyear"] = knjCreateBtn($objForm, 'shokitiyear', '公休・休日反映(年度分)', $extra);

        //公休・休日反映(指定月分)・更新
        $extra = "onclick=\"return btn_submit('shokitimonth');\"";
        $arg["button"]["btn_shokitimonth"] = knjCreateBtn($objForm, 'shokitimonth', '公休・休日反映(指定月分)', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        //Hidden作成
        //データ変更フラグ(チェックボックス、テキストボックスに変更があった場合に"true")
        knjCreateHidden($objForm, "DATA_CHANGE_FLG", "false");
        
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $row["UPDATED"]);
        Query::dbCheckIn($db);        
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjs530Form1.html", $arg);
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
        $query = knjs530Query::countEventMst($model,$setDay);
        $eventCnt = $db->getOne($query);
        
        if ($eventCnt > 0) {
            $query = knjs530Query::setEventMst($model,$setDay);
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
                $extra .= " onClick=\"dataFlgSet()\"";
                $opt["HOLIDAY_FLG"] = knjCreateCheckBox($objForm, "HOLIDAY_FLG_".$setDay, "1", $extra);

                //行事名
                $extra = "onChange=\"dataFlgSet()\"";
                $opt["REMARK1"] = knjCreateTextBox($objForm, $row["REMARK1"], "REMARK1_".$setDay, 50, 50, $extra);
                
                //備考
                $extra = "onChange=\"dataFlgSet()\"";
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
            $extra .= "onClick=\"dataFlgSet()\"";
            $opt["HOLIDAY_FLG"] = knjCreateCheckBox($objForm, "HOLIDAY_FLG_".$setDay, "1", $extra);
            
            //行事名
            $extra = "onChange=\"dataFlgSet()\"";
            $opt["REMARK1"] = knjCreateTextBox($objForm, $model->field["REMARK1"], "REMARK1_".$setDay, 50, 50, $extra);
            
            //備考
            $extra = "onChange=\"dataFlgSet()\"";
            $opt["REMARK2"] = knjCreateTextBox($objForm, $model->field["REMARK2"], "REMARK2_".$setDay, 50, 50, $extra);
            
            $arg["data"][] = $opt;
        }
   }
}

?>
