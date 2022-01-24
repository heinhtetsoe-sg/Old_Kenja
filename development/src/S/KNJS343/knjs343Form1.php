<?php

require_once('for_php7.php');

class knjs343Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjs343Form1", "POST", "knjs343index.php", "", "knjs343Form1");
        // Add by PP for Title 2020-02-03 start
        $arg["TITLE"] = "７日以上連・断続欠席者調査入力画面";
        echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        // Add by PP for Title 2020-02-20 end

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        if ($model->Properties["useFi_Hrclass"] == "1") {
            $arg["useFi_HrclassSelect"] = "1";
            //クラス方式選択 (1:法定クラス 2:複式クラス)
            $opt = array(1, 2);
            $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
            // Add by PP for PC-Talker 2020-02-03 start
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"current_cursor('HR_CLASS_TYPE1');return btn_submit('main');\"", "id=\"HR_CLASS_TYPE2\" onclick=\"current_cursor('HR_CLASS_TYPE2');return btn_submit('main');\"");
            // Add by PP for PC-Talker 2020-02-20 end
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        } elseif ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $arg["useSpecial_Support_HrclassSelect"] = "1";
            //クラス方式選択 (1:法定クラス 2:実クラス 3:統計学級)
            $opt = array(1, 2, 3);
            $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
            // Add by PP for PC-Talker 2020-02-03 start
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"current_cursor('HR_CLASS_TYPE1');return btn_submit('main');\"", "id=\"HR_CLASS_TYPE2\" onclick=\"current_cursor('HR_CLASS_TYPE2');return btn_submit('main');\"", "id=\"HR_CLASS_TYPE3\" onclick=\"current_cursor('HR_CLASS_TYPE3');return btn_submit('main');\"");
            // Add by PP for PC-Talker 2020-02-20 end
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }

            //学年混合チェックボックス
            $extra = $model->field["GAKUNEN_KONGOU"] == "1" ? "checked" : "";
            // Add by PP for PC-Talker 2020-02-03 start
            $extra .= " onclick=\"current_cursor('GAKUNEN_KONGOU');return btn_submit('main');\" id=\"GAKUNEN_KONGOU\"";
            // Add by PP for PC-Talker 2020-02-20 end
            $arg["data"]["GAKUNEN_KONGOU"] = knjCreateCheckBox($objForm, "GAKUNEN_KONGOU", "1", $extra, "");
        } else {
            $model->field["HR_CLASS_TYPE"] = "1";
        }

        //年組コンボボックス
        $query = knjs343Query::getGradeHrClass($model);
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"GRADE_HR_CLASS\" onChange=\"current_cursor('GRADE_HR_CLASS');return btn_submit('main');\" aria-label=\"対象クラス\"";
        // Add by PP for PC-Talker 2020-02-20 end
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //対象月コンボボックス
        makeMonthSemeCmb($objForm, $arg, $db, $model);

        //対象月の月と学期を取得
        $target_month_array = explode("-", $model->field["TARGET_MONTH"]); //月、学期がハイフン区切りだからそれを配列にする
        $model->MONTH    = $target_month_array[0];
        $model->SEMESTER = $target_month_array[1];

        //全選択
        $extra = " id=\"ALL_CHECK\" onclick=\"allCheck(this, '{$model->rowCnt}')\" aria-label=\"全削除\"";
        $arg["ALL_CHECK"] = knjCreateCheckBox($objForm, "ALL_CHECK", "1", $extra);

        //氏名セット
        $optSchName = $schNameArr = array();
        $sqlInSchregNo = $sep = "";
        $query = knjs343Query::getSchInfo($model->field["HR_CLASS_TYPE"], $model->field["GAKUNEN_KONGOU"], CTRL_SEMESTER, $model->field["GRADE_HR_CLASS"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optSchName[] = array('label' => $row["LABEL"],
                                  'value' => $row["VALUE"]);

            $schNameArr[$row["SCHREGNO"]] = array('SCHREGNO' => $row["SCHREGNO"],
                                                  'NAME'     => $row["VALUE"],
                                                  'GNAME'    => $row["GNAME"]);

            $sqlInSchregNo .= $sep.$row["SCHREGNO"];
            $sep = "', '";
        }
        //データ取得
        $mainData = $schAttendAbsenseArr = array();
        $count = 1;
        $query = knjs343Query::getAttendAbsenseMonthRemarkDat($model->field["TARGET_MONTH"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //指定年組にいない生徒はセットしない
            if (!array_key_exists($row["SCHREGNO"], $schNameArr)) {
                continue;
            }

            $mainData[$count] = $row;
            $schAttendAbsenseArr[$row["SCHREGNO"]."-".$row["SEQ"]] = $row;
            $count++;
        }

        /********************/
        /* デフォルトセット */
        /********************/
//        $colorFlg = false;
//        if ($mainData[1] == null) {
        if ($model->field["TARGET_MONTH"]) { // 月コンボある時
            //月、学期
            list($month, $sem) = explode('-', $model->field["TARGET_MONTH"]);

            //学期開始日・終了日取得
            $semeM = array();
            if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GRADE_HR_CLASS"] && $model->field["GAKUNEN_KONGOU"] != "1") {
                list($grade, $hr_class) = explode('-', $model->field["GRADE_HR_CLASS"]);
                $query = knjs343Query::selectSemesGradeAll($grade, $sem);
            } else {
                $query = knjs343Query::selectSemesAll($sem);
            }
            $semeM = $db->getRow($query, DB_FETCHMODE_ASSOC);

            /****** 前月処理 ******/
            //前月(mm)
            $lastM = ((int)$month == 1) ? 12: sprintf("%02d", $month - 1);
            //前月の年度(yyyy)
            $lastMYear = ((int)$lastM < 3) ? CTRL_YEAR + 1 : CTRL_YEAR;
            //前月の日数取得（月末取得(mm)
            $lastMDay_cnt = date('t', mktime(0, 0, 0, $lastM, 1, $lastMYear));
            //前月開始日(yyyy-mm-dd)
            $lastMDate_from = $lastMYear.'-'.$lastM.'-01';
            //前月終了日(yyyy-mm-dd)
            $lastMday_to    = $lastMYear.'-'.$lastM.'-'.$lastMDay_cnt;
            //開始日(mm)
            $lastMday_from = ($semeM["S_MONTH"] == $lastM) ? $semeM["S_DAY"]: "1";
            //前月の休日データ取得
            $lastMEventArr = array();
            $query = knjs343Query::getEventDat($model->field["GRADE_HR_CLASS"], $model->field["HR_CLASS_TYPE"], $lastMDate_from, $lastMday_to);
            $lastMEventArr = $db->getCol($query);
            //前月の休日除いた日をセット
            $lastMArr = array();
            for ($i = $lastMday_from; $i <= $lastMDay_cnt; $i++) {
                //日付(yyyy-mm-dd)
                $date = $lastMYear."-".$lastM."-".sprintf('%02d', $i);

                //休日は除く
                if (in_array($date, $lastMEventArr)) {
                    continue;
                }

                $lastMArr[] = $date;
            }
            //配列を前月末からの６日分にする
            foreach ($lastMArr as $date) {
                //６日分になったらbreak;
                if (get_count($lastMArr) <= 6) {
                    break;
                }

                array_splice($lastMArr, 0, 1);//先頭要素を削除していく
            }

            /****** 指定月処理 ******/
            //年取得(yyyy)
            $year = ((int)$month < 4) ? CTRL_YEAR + 1 : CTRL_YEAR;
            //月から日数取得（月末取得）(mm)
            $day_cnt = date('t', mktime(0, 0, 0, $month, 1, $year));
            //開始日(mm)
            $day_from = ($semeM["S_MONTH"] == $month) ? $semeM["S_DAY"]: "1";
            //終了日(mm)
            $day_to   = ($semeM["E_MONTH"] == $month) ? $semeM["E_DAY"]: $day_cnt;

            $date_from = $year.'-'.sprintf("%02d", $month - 1).'-01';
            $date_to   = $year.'-'.$month.'-'.$day_to;

            //休日データ取得
            $holyDayArr = array();
            $query = knjs343Query::getEventDat($model->field["GRADE_HR_CLASS"], $model->field["HR_CLASS_TYPE"], $date_from, $date_to);
            $holyDayArr = $db->getCol($query);
            //指定月の休日除いた日をセット
            $monthArr = array();
            for ($i = $day_from; $i <= $day_to; $i++) {
                //日付
                $date = $year."-".$month."-".sprintf('%02d', $i);

                //休日は除く
                if (in_array($date, $holyDayArr)) {
                    continue;
                }

                $monthArr[] = $date;
            }

            //前月データと今月データを合体
            if ($semeM["S_MONTH"] == $month) { // 指定月が学期開始日の時は、前月は足さない
                $margeDateArr = $monthArr;
            } else {
                $margeDateArr = array_merge($lastMArr, $monthArr);
            }

            //生徒出欠データ取得
            $schAttendArr = array();
            $query = knjs343Query::getAttendDayDat($model, $sqlInSchregNo, $date_from, $date_to);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schAttendArr[$row["SCHREGNO"]][$row["ATTENDDATE"]] = array("ATTENDDATE" => $row["ATTENDDATE"],
                                                                                "DI_CD"      => $row["DI_CD"]);
            }

            //休日を除いた連続欠席が７日以上の人をセット
            foreach ($schAttendArr as $schregNo => $attInfoArr) {
                //連続欠席カウント
                $continueCnt = 0;

                //欠席DI_CDカウント用
                $diCdArr = array();

                // カウント開始日
                $startDate = "";
                $startDateBefore = "";
                $schregData = array();
                $seq = 0;

                //【前月[月末から休日を除いた６日分] ＋ 今月[休日を除いた]】の配列
                foreach ($margeDateArr as $date) {
                    //欠席が無ければ連続カウントを初期化
                    if ($attInfoArr[$date] == '') {
                        $continueCnt = 0;
                        $diCdArr = array();
                        $startDate = "";
                        $startDateBefore = "";
                        continue;
                    } else {
                        if ($continueCnt == 0) {
                            $startDate = $date;
                        }
                        $continueCnt++;
                        $diCdArr[$attInfoArr[$date]["DI_CD"]] += 1;
                    }

                    //連続欠席が７日以上の人をセット
                    if (7 <= $continueCnt) {
                        if ($startDate != $startDateBefore) {
                            $seq += 1;
                            $startDateBefore = $startDate;
                        }

                        //登録されている生徒はセットしない
                        if (array_key_exists($schregNo."-".$seq, $schAttendAbsenseArr)) {
                            continue;
                        }
                        //echo $schregNo." ".$seq." ".$startDate." ".$date." ".$continueCnt."<br>";

                        //DI_CDが混在なら固定で、F1:病・事故欠
                        $continueDiCd = (get_count($diCdArr) > 1) ? $model->koteiDiCd : $attInfoArr[$date]["DI_CD"];

                        $schregData[$startDate] = array("COLOR_FLG" => "1",
                                                            "DI_CD"     => $continueDiCd,
                                                            "TOTAL_DAY" => $continueCnt,
                                                            "NAME"      => $schNameArr[$schregNo]["NAME"],
                                                            "GNAME"     => $schNameArr[$schregNo]["GNAME"],
                                                            "START_DATE" => $startDate);


//                          $colorFlg = true;
                    }
                }

                foreach ($schregData as $datekey => $data) {
                    $mainData[$count] = $data;
                    $count++;
                }
            }
        }

//        }
        /* デフォルトfin */

        $setData = array();
        // データセット
        for ($i=1; $i <= $model->rowCnt; $i++) {
            //checkbox
            $setName = "CHECK-".$i;
            // Add by PP for PC-Talker 2020-02-03 start
            $extra = "id=\"{$setName}\" class=\"changeColor\" data-name=\"{$setName}\" aria-label=\"削除\"";
            // Add by PP for PC-Talker 2020-02-20 end
            $setData["CHECK"] = knjCreateCheckBox($objForm, $setName, "1", $extra);
            $setData["CHECK_NAME"] = $setName;

            //種別
            $query = knjs343Query::getDiCd($model);
            $name  = "DI_CD"."-".$i;
            // Add by PP for PC-Talker 2020-02-03 start
            $extra = "aria-label=\"種別\"";
            // Add by PP for PC-Talker 2020-02-20 end
            $value = (isset($model->warning)) ? $model->field[$name]: $mainData[$i]["DI_CD"];
            $setData["DI_CD"] = makeCmbRet($objForm, $arg, $db, $query, $name, $value, $extra, 1, "BLANK");

            //日数
            $name  = "TOTAL_DAY"."-".$i;
            // Add by PP for PC-Talker 2020-02-03 start
            $extra = "id=\"$name\" style=\"text-align:right\" onblur=\"tmpSet(this, '$name');this.value=toInteger(this.value);\" aria-label=\"日数\"";
            // Add by PP for PC-Talker 2020-02-20 end
            $value = (isset($model->warning)) ? $model->field[$name]: $mainData[$i]["TOTAL_DAY"];
            $setData["TOTAL_DAY"] = knjCreateTextBox($objForm, $value, $name, 3, 3, $extra);

            //氏名
            $name  = "NAME"."-".$i;
            $query = knjs343Query::getSchInfo($model->field["HR_CLASS_TYPE"], $model->field["GAKUNEN_KONGOU"], CTRL_SEMESTER, $model->field["GRADE_HR_CLASS"]);
            // Add by PP for PC-Talker 2020-02-03 start
            $extra = "onChange=\"setGname(this, '{$i}')\" aria-label=\"氏名\"";
            // Add by PP for PC-Talker 2020-02-20 end
            $value = (isset($model->warning)) ? $model->field[$name]: $mainData[$i]["NAME"];
            // Add by PP for PC-Talker 2020-02-03 start
            $chk_value = $value;
            // Add by PP for PC-Talker 2020-02-20 end
            $setData["NAME"] = makeCmbRet($objForm, $arg, $db, $query, $name, $value, $extra, 1, "BLANK");

            //事由
            $name  = "REMARK"."-".$i;
            // Add by PP for PC-Talker 2020-02-03 start
            $stu_name = "";
            for ($p = 0; $p < get_count($optSchName); $p++) {
                if ($optSchName[$p]['value'] == $chk_value) {
                    $stu_name = $optSchName[$p]['label'];
                }
            }
            $label = ($stu_name == "")? "aria-label=\"事由\"": "aria-label=\"{$stu_name}の事由\"";
            $extra = "id=\"$name\" $label";
            // Add by PP for PC-Talker 2020-02-20 end
            $value = (isset($model->warning)) ? $model->field[$name]: $mainData[$i]["REMARK"];
            $setData["REMARK"] = knjCreateTextBox($objForm, $value, $name, 31, 45, $extra);

            //保護者氏名
            $name  = "GNAME"."-".$i;
            $setData["GNAME_ID"] = $name;
            $value = (isset($model->warning)) ? $model->field[$name]: $mainData[$i]["GNAME"];
            $setData["GNAME"]    = $value;
            knjCreateHidden($objForm, $name, $value);

            //担任のとった処置
            $name  = "TREATMENT"."-".$i;
            // Add by PP for PC-Talker 2020-02-03 start
            $stu_name = "";
            for ($p = 0; $p < get_count($optSchName); $p++) {
                if ($optSchName[$p]['value'] == $chk_value) {
                    $stu_name = $optSchName[$p]['label'];
                }
            }
            $label = ($stu_name == "")? "aria-label=\"担任のとった処置\"": "aria-label=\"{$stu_name}の担任のとった処置\"";
            $extra = "id=\"$name\" $label";
            $value = (isset($model->warning)) ? $model->field[$name]: $mainData[$i]["TREATMENT"];
            // Add by PP for PC-Talker 2020-02-20 end
            $setData["TREATMENT"] = knjCreateTextBox($objForm, $value, $name, 51, 75, $extra);

            //デフォルト表示の時は、背景色を変更
            if ($mainData[$i]["COLOR_FLG"] == "1" && $mainData[$i]["NAME"] != '') {
                $setData["BGCOLOR"] = "pink";
            } else {
                $setData["BGCOLOR"] = "#ffffff";
            }

            $arg["data2"][] = $setData;
        }

        /**********/
        /* ボタン */
        /**********/
        //更新ボタン
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('update');\" aria-label=\"更新\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        // Add by PP for PC-Talker 2020-02-20 end
        //取消ボタン
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"btn_reset\" onclick=\"current_cursor('btn_reset'); return btn_submit('reset');\" aria-label=\"取消\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        // Add by PP for PC-Talker 2020-02-20 end
        //終了ボタン
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "onclick=\"closeWin();\" aria-label=\"終了\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        // Add by PP for PC-Talker 2020-02-20 end

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "rowCnt", $model->rowCnt);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjs343Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
//makeCmb2
function makeCmbRet(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $result->free();
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model)
{
    if ($model->field["TARGET_MONTH"] == '') {
        // 初期値はログイン日付の月
        $ctrl_date = preg_split("/-/", CTRL_DATE);
        $query = knjs343Query::getSemesAll();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($i = $row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
                $month = sprintf("%02d", ($i > 12) ? ($i - 12) : $i);

                //対象月名称取得
                $monthname = $db->getOne(knjs343Query::getMonthName($month, $model));
                if ($monthname) {
                    if (((int) $ctrl_date[1]) == $month) {
                        $model->field["TARGET_MONTH"] = $month.'-'.$row["SEMESTER"];
                    }
                }
            }
        }
    }
    $value_flg = false;
    $query = knjs343Query::getSemesAll();
    $result = $db->query($query);
    $opt_month = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        for ($i = $row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
            $month = sprintf("%02d", ($i > 12) ? ($i - 12) : $i);

            //対象月名称取得
            $monthname = $db->getOne(knjs343Query::getMonthName($month, $model));
            if ($monthname) {
                $opt_month[] = array("label" => $monthname." (".$row["SEMESTERNAME"].") ",
                                     "value" => $month.'-'.$row["SEMESTER"]);
                if ($model->field["TARGET_MONTH"] == $month.'-'.$row["SEMESTER"]) {
                    $value_flg = true;
                }
            }
        }
    }
    $result->free();

    //初期値はログイン月
    $ctrl_date = preg_split("/-/", CTRL_DATE);
    $model->field["TARGET_MONTH"] = ($model->field["TARGET_MONTH"] && $value_flg) ? $model->field["TARGET_MONTH"] : (int)$ctrl_date[1].'-'.CTRL_SEMESTER;
    // Add by PP for PC-Talker 2020-02-03 start
    $arg["data"]["TARGET_MONTH"] = knjCreateCombo($objForm, "TARGET_MONTH", $model->field["TARGET_MONTH"], $opt_month, "id=\"TARGET_MONTH\" onChange=\"current_cursor('TARGET_MONTH');return btn_submit('main');\" aria-label=\"対象月\"", 1);
    // Add by PP for PC-Talker 2020-02-20 end
}
