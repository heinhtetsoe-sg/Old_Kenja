<?php

require_once('for_php7.php');

class knjc034dForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjc034dindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query = knjc034dQuery::getNameMstA023($model);
            /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
            $extra = "id = \"SCHOOL_KIND\" aria-label = \"校種\" onChange=\"current_cursor('SCHOOL_KIND');btn_submit('edit')\";";
            /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */
            makeCmb($objForm, $arg, $db, $query, $model->setSchoolKind, "SCHOOL_KIND", $extra, 1, "");
        } else {
            $model->setSchoolKind = SCHOOLKIND;
        }

        //対象月コンボ
        makeMonthSemeCmb($objForm, $arg, $db, $model);

        if ($model->Properties["useFi_Hrclass"] == "1" || $model->Properties["useSpecial_Support_Hrclass"] == '1') {
            //クラス方式選択 (1:法定クラス 2:複式又は実クラス)
            $opt = array(1, 2);
            if ($model->hr_class_type == "") {
                $model->hr_class_type = "1";
            }
            /* Edit by HPA for current_cursor start 2020/02/03 */
            $click1 = " onClick=\"current_cursor('HR_CLASS_TYPE1');return btn_submit('edit');\"";
            $click2 = " onClick=\"current_cursor('HR_CLASS_TYPE2');return btn_submit('edit');\"";
            $extra = array("id=\"HR_CLASS_TYPE1\"".$click1, "id=\"HR_CLASS_TYPE2\"".$click2);
            /* Edit by HPA for current_cursor end 2020/02/20 */
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->hr_class_type, $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg[$key] = $val;
            }

            if ($model->Properties["useFi_Hrclass"] != "1") {
                //学年混合チェックボックス
                $extra  = ($model->grade_mix == "1") ? "checked" : "";
                $extra .= ($model->hr_class_type != "1") ? " disabled" : "";
                /* Edit by HPA for current_cursor start 2020/02/03 */
                $extra .= " onClick=\"current_cursor('GRADE_MIX');return btn_submit('edit');\"";
                /* Edit by HPA for current_cursor end 2020/02/20 */
                $extra .= " id=\"GRADE_MIX\"";
                $arg["GRADE_MIX"] = knjCreateCheckBox($objForm, "GRADE_MIX", "1", $extra, "");
                $arg["HR_CLASS_TYPE_1"] = "1";
            }
            //複式クラスの名称表示
            $arg["HR_CLASS_TYPE_LABEL"] = ($model->Properties["useFi_Hrclass"] == "1") ?  '複式クラス' : '実クラス';

            $arg["useFukushiki"] = 1;
        } else {
            if ($model->hr_class_type == "") {
                $model->hr_class_type = "1";
            }
            knjCreateHidden($objForm, "HR_CLASS_TYPE", "1");
        }

        $month = $sem = $year = $day_cnt = $lastMonth = "";
        if ($model->month_sem) {
            //月、学期
            list($month, $sem) = explode('-', $model->month_sem);

            //年取得
            $year = ((int)$month < 4) ? CTRL_YEAR + 1 : CTRL_YEAR;

            //月から日数取得（月末取得）
            $day_cnt = date('t', mktime(0, 0, 0, $month, 1, $year));

            //学期開始日・終了日取得
            $semeM = array();
            if ($model->hr_class_type == "1" && $model->grade_hr_class && $model->grade_mix != "1") {
                list($grade, $hr_class) = explode('-', $model->grade_hr_class);
                $query = knjc034dQuery::selectSemesGradeAll($grade, $sem);
            } else {
                $query = knjc034dQuery::selectSemesAll($sem);
            }
            $semeM = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //開始日
            $day_from = ($semeM["S_MONTH"] == $month) ? $semeM["S_DAY"] : "1";

            //終了日
            $day_to = ($semeM["E_MONTH"] == $month) ? $semeM["E_DAY"] : $day_cnt;

            //学期の最終月取得
            $lastMonth = sprintf('%02d', $semeM["E_MONTH"]);
        } else {
            //開始日
            $day_from = 1;
            //終了日
            $day_to = 31;
        }

        knjCreateHidden($objForm, "DAY_FROM", $day_from);
        knjCreateHidden($objForm, "DAY_TO", $day_to);
        knjCreateHidden($objForm, "LAST_MONTH", $lastMonth);

        //開始日付
        $date_from = ($month) ? $year."-".$month."-".sprintf('%02d', $day_from) : "";
        //終了日付
        $date_to   = ($month) ? $year."-".$month."-".sprintf('%02d', $day_to) : "";

        //表幅
        $arg["WIDTH"] = 70 * ((int)$day_to - (int)$day_from + 1) + 7 * ((int)$day_to - (int)$day_from + 1) + 250;

        //年組コンボ
        $query = knjc034dQuery::getHrClass($model, $sem);
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
        $extra = "id= \"GRADE_HR_CLASS\" aria-label = \"学級\" onchange=\"current_cursor('GRADE_HR_CLASS');return btn_submit('edit');\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */
        makeCmb($objForm, $arg, $db, $query, $model->grade_hr_class, "GRADE_HR_CLASS", $extra, 1, "BLANK");

        //出席番号の項目名切替
        $arg["ATTENDNO_LABEL"] = ($model->hr_class_type == "1" && $model->grade_mix == "1") ?  '年組番' : 'No.';

        //データ取得
        $schList = $sch_array = array();
        if (!isset($model->warning) && $model->grade_hr_class != "") {
            //生徒一覧
            $query = knjc034dQuery::getSchInfo($model->hr_class_type, $model->grade_mix, $sem, $model->grade_hr_class);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schList[] = $row;
                $sch_array[] = $row["SCHREGNO"];
            }
            $result->free();
        }

        //休日（訪問生）
        $schHoliday = $schEvent = $holiday = array();
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query = knjc034dQuery::getSchHoliday($model, $month, $sch_array);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schEvent[] = $row["SCHREGNO"];

                if ($row["HOLIDAY_FLG"] == "1") {
                    $schHoliday[$row["SCHREGNO"]][$row["EXECUTEDATE"]] = 1;
                }
            }
            $result->free();
        }

        if ($model->grade_hr_class) {
            if ($model->hr_class_type == "1" && $model->grade_mix == "1") {
                //休日取得 -- EVENT_MST
                $holiday = $db->getCol(knjc034dQuery::getHoliday2($model, $model->hr_class_type, $model->grade_hr_class, $date_from, $date_to));
            } elseif ($model->hr_class_type == "2" && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
                //休日取得 -- PUBLIC_HOLIDAY_MST
                $query = knjc034dQuery::getPublicHoliday($month);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //月日を2桁変換、1月から3月の時は年を+1する
                    $row["HOLIDAY_MONTH"] = sprintf("%02d", $row["HOLIDAY_MONTH"]);
                    if (in_array($row["HOLIDAY_MONTH"], array("01", "02", "03"))) {
                        $row["YEAR"] = (int)$row["YEAR"] + 1;
                    }
                    $row["HOLIDAY_DAY"] = sprintf("%02d", $row["HOLIDAY_DAY"]);

                    //日付指定をする場合
                    if ($row["HOLIDAY_DIV"] == "1") {
                        $holiday[] = $row["YEAR"]."-".$row["HOLIDAY_MONTH"]."-".$row["HOLIDAY_DAY"];
                    //曜日指定をする場合
                    } elseif ($row["HOLIDAY_DIV"] == "2") {
                        //曜日コードをPHP用のコードに変換
                        $row["HOLIDAY_WEEKDAY"] = (int)$row["HOLIDAY_WEEKDAY"] - 1;

                        $holiday[] = getWhatDayOfWeek($row["YEAR"], $row["HOLIDAY_MONTH"], $row["HOLIDAY_WEEK_PERIOD"], $row["HOLIDAY_WEEKDAY"]);
                    }
                }
                $result->free();
            } else {
                //休日取得 -- EVENT_DAT
                $holiday = $db->getCol(knjc034dQuery::getHoliday($model, $model->hr_class_type, $model->grade_hr_class, $date_from, $date_to));
            }
        }

        //曜日一覧
        $weekday = array( "日", "月", "火", "水", "木", "金", "土" );

        //出欠状況取得
        $executed_array = array();
        if ($month) {
            $query = knjc034dQuery::checkExecuted($model, $date_from, $date_to);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $executed_array[$row["ATTENDDATE"]] = $row["EXECUTED"];
            }
            $result->free();
        }

        //項目名表示（一括変更対象項目）
        $dateFlg = false;
        for ($i = $day_from; $i <= $day_to; $i++) {
            //曜日
            $week = ($model->month_sem) ? '('.$weekday[date("w", mktime(0, 0, 0, $month, $i, $year))].')' : "";

            $label = array();
            if ($model->grade_hr_class && $model->month_sem && ((!isset($model->warning) && get_count($schList) > 0) || (isset($model->warning) && $model->data_cnt > 0))) {
                //日付
                $date = $year."-".$month."-".sprintf('%02d', $i);

                //出欠対象日フラグ
                $dateFlg = true;

                if ((!isset($model->warning) && get_count($schList) > 1) || (isset($model->warning) && $model->data_cnt > 1)) {
                    //リンク有
                    $mix = ($model->hr_class_type == "1" && $model->grade_mix == "1") ? "1" : "0";
                    $linkData = "loadwindow('knjc034dindex.php?cmd=replace&REP_ITEM=".$date."&TYPE=".$model->hr_class_type."&MIX=".$mix."&GHR=".$model->grade_hr_class."&SEM=".$sem."',0,0,450,430)";
                    /* Edit by HPA for current_cursor start 2020/02/03 */
                    $label["NAME"] = View::alink("#", $i.$week, "id = \"$i\" onclick=\"current_cursor('$i');$linkData\"");
                /* Edit by HPA for current_cursor end 2020/02/20 */
                } else {
                    //表示のみ
                    $label["NAME"] = $i.$week;
                }

                if ($model->hr_class_type == "1" && $model->grade_mix != "1") {
                    if (isset($model->warning)) {
                        //エラーの場合
                        $executed = $model->fields["EXECUTED"][$date];
                    } else {
                        //出欠状況取得
                        $executed = $executed_array[$date];
                    }

                    //出欠状況チェックボックス
                    $id = 'zumi_'.$date;
                    $label_id = "EXECUTED_".$date;
                    /* Edit by HPA for PC-talker 読み start 2020/02/03 */
                    $day = preg_replace("/[^A-Za-z0-9 ]/", '', $i.$week);
                    if ($executed == 1) {
                        $extra = "id=\"{$label_id}\" aria-label = \"".$day."日 の 済\" onclick=\"current_cursor('{$label_id}');checkExecutedLabel(this, '$id','$label_id', '$day');\" checked='checked'";
                        $zumi = "<LABEL for={$label_id}><span id='$id' style='color:white;'>済</span></LABEL>";
                    } else {
                        $extra = "id=\"{$label_id}\" aria-label = \"".$day."日 の 未\" onclick=\"current_cursor('{$label_id}');checkExecutedLabel(this, '$id','$label_id', '$day');\"";
                        $zumi = "<LABEL for={$label_id}><span id='$id' style='color:#ff0099;'>未</span></LABEL>";
                        /* Edit by HPA for PC-talker 読み end 2020/02/20 */
                    }
                    $label["EXECUTED"] = knjCreateCheckBox($objForm, "EXECUTED"."_".$date, '1', $extra).$zumi;
                }
            } else {
                //表示のみ
                $label["NAME"] = $i.$week;
            }

            //サイズ（幅）
            $label["WIDTH"] = "70";

            $arg["label"][] = $label;
        }

        //更新時のチェックでエラーの場合、画面情報をセット
        if (isset($model->warning)) {
            //生徒一覧
            for ($counter = 0; $counter < $model->data_cnt; $counter++) {
                $Row = array();
                foreach ($model->fields as $key => $val) {
                    $Row[$key] = $val[$counter];
                }
                $schList[] = $Row;
            }
        }

        //生徒一覧の表示件数
        knjCreateHidden($objForm, "DATA_CNT", get_count($schList));

        //出欠コードコンボに表示しないコード＆子・孫が設定されている親の出欠コード取得
        $dicd["omitcd"] = $dicd["parents"] = array();
        if ($model->Properties["attend_Shosai"] == 1) {
            $query = knjc034dQuery::getAttendCode($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                list($di_cd, $subl_cd, $subm_cd) = preg_split("/-/", $row["VALUE"]);

                //出欠コードコンボに表示しないコード取得
                if ($subm_cd != '0000' && !in_array($di_cd.'-'.$subl_cd.'-0000', $dicd["omitcd"])) {
                    $dicd["omitcd"][] = $di_cd.'-'.$subl_cd.'-0000';
                }
                //子・孫が設定されている親の出欠コード取得
                if ($subl_cd != '0000' && !in_array($di_cd.'-0000-0000', $dicd["parents"])) {
                    $dicd["parents"][] = $di_cd.'-0000-0000';
                }
            }
        }

        $attend_array = $idou_array = $idou_disable = $remark_array = array();
        if ($month) {
            //出欠データ取得
            $query = knjc034dQuery::getAttendDayDat($model, $sch_array, $date_from, $date_to);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $attend_array[$row["SCHREGNO"]][$row["ATTENDDATE"]][] = $row["DI_CD"];
            }
            $result->free();

            //異動チェック
            for ($i = $day_from; $i <= $day_to; $i++) {
                $date = $year."-".$month."-".sprintf('%02d', $i);
                $query = knjc034dQuery::checkIdou($sch_array, $sem, $date);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $idou_array[$row["SCHREGNO"]][$date] = $row["IDOU_COLOR"];
                    $idou_disable[$row["SCHREGNO"]][$date] = $row["IDOU_DISABLE"];
                }
                $result->free();
            }

            //備考の参照月取得
            $remarkMonth = ($model->Properties["useAttendRemarkLastMonth"] == 1) ? $lastMonth : $month;
            //出欠の備考取得
            $query = knjc034dQuery::getAttendSemesRemarkDat($remarkMonth, $sem, $sch_array);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $remark_array[$row["SCHREGNO"]] = $row["REMARK1"];
            }
            $result->free();
        }

        //コンボボックスの情報を一回だけ保持
        $combo_array = array();
        $query = knjc034dQuery::getAttendCode($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $combo_array[] = $row;
        }
        $result->free();
        //訪問生用（出席）
        $visit_array = array();
        $query = knjc034dQuery::getNameMst('C001', '0');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->Properties["attend_Shosai"] == 1) {
                $row["VALUE"] = $row["VALUE"]."-0000-0000";
            }
            $visit_array[] = $row;
        }
        $result->free();

        //生徒一覧を表示
        $setData = array();
        $colorFlg = false;
        foreach ($schList as $counter => $Row) {
            //出席番号
            $setData["ATTENDNO"] = $Row["ATTENDNO"];
            knjCreateHidden($objForm, "ATTENDNO"."_".$counter, $Row["ATTENDNO"]);
            //氏名
            $setData["NAME_SHOW"] = $Row["NAME_SHOW"];
            knjCreateHidden($objForm, "NAME_SHOW"."_".$counter, $Row["NAME_SHOW"]);
            //訪問生フラグ
            $setData["VISIT"] = $Row["VISIT_FLG"] == "1" ? "1" : "";
            if ($setData["VISIT"]) {
                $arg["VISIT"] = "1";
            }

            //背景色切替フラグ
            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            $lessonCnt = $idouDayCnt = 0;
            $di_cd = "";
            /* Edit by HPA for PC-talker 読み start 2020/02/03 */
            $day = 0 ;
            for ($i = $day_from; $i <= $day_to; $i++) {
                $day++ ;

                if ($model->grade_hr_class && $model->month_sem) {
                    //対象日付
                    $date = $year."-".$month."-".sprintf('%02d', $i);

                    //出欠データ取得
                    $di_cd_array = $attend_array[$Row["SCHREGNO"]][$date];
                    $di_cd_1st = $attend_array[$Row["SCHREGNO"]][$date][0];

                    $value = "";
                    if (isset($model->warning)) {
                        //エラーの場合
                        $value = $Row[$date];
                    } elseif (get_count($di_cd_array) > 1) {
                        $sub = ($model->Properties["attend_Shosai"] == 1) ? "-0000-0000" : "";
                        if (get_count($di_cd_array) == 2 && in_array("15".$sub, $di_cd_array) && in_array("16".$sub, $di_cd_array)) {
                            //遅刻・早退は'99'をセット
                            $value = "99".$sub;
                        } else {
                            //複数件のとき、小さいコードをセット
                            $value = $di_cd_1st;
                        }
                    } elseif (get_count($di_cd_array) == 1) {
                        $value = $di_cd_array[0];
                    }

                    //異動チェック
                    $idou = $idou_array[$Row["SCHREGNO"]][$date];

                    //背景色
                    if ($date == CTRL_DATE) {
                        $bgcolor = "#00ff00";   //ログイン日付
                    } elseif ($idou > 0) {
                        $bgcolor = "yellow";    //異動
                    } elseif (date("w", mktime(0, 0, 0, $month, $i, $year)) == 6) {
                        $bgcolor = "lightblue"; //土曜日
                    } elseif (date("w", mktime(0, 0, 0, $month, $i, $year)) == 0) {
                        $bgcolor = "pink";      //日曜日
                    } elseif ($colorFlg) {
                        $bgcolor = "#ffffff";
                    } else {
                        $bgcolor = "#cccccc";
                    }

                    //サイズ（幅）
                    $width = "70";

                    //出欠コンボ
                    $cmb_width = ($model->Properties["attend_Shosai"] == 1) ? "width:70px;" : "";

                    //異動（在籍外）・休日なら使用不可
                    if ($idou_disable[$Row["SCHREGNO"]][$date] > 0) {
                        $extra = "disabled style=\"background-color:darkgray;{$cmb_width}\"";
                    } elseif (in_array($Row["SCHREGNO"], $schEvent)) {
                        if ($schHoliday[$Row["SCHREGNO"]][$date] == 1) {
                            $extra = "disabled style=\"background-color:darkgray;{$cmb_width}\"";
                        } else {
                            $lessonCnt++;
                            $extra = "style=\"font-weight:bold;{$cmb_width}\" ";
                        }
                    } elseif (in_array($date, $holiday)) {
                        $extra = "disabled style=\"background-color:darkgray;{$cmb_width}\"";
                    } else {
                        $extra = "style=\"font-weight:bold;{$cmb_width}\" ";
                        $lessonCnt++;
                    }
                    $extra .= "aria-label = \"".$setData["NAME_SHOW"]." の ".$day."日\"";
                    /* Edit by HPA for PC-talker 読み end 2020/02/20 */

                    $di_cd .= "<td width=".$width." align=\"center\" bgcolor=\"{$bgcolor}\">";
                    $comboTmpArray = array();
                    if ($setData["VISIT"]) {
                        //訪問生の場合は「出席を追加」
                        $comboTmpArray = $visit_array;
                        for ($idx=0; $idx < get_count($combo_array); $idx++) {
                            $item = $combo_array[$idx];
                            $comboTmpArray[] = $item;
                        }
                    } else {
                        $comboTmpArray = $combo_array;
                    }
                    $di_cd .= makeCmbReturn($objForm, $arg, $db, $comboTmpArray, $value, $date."_".$counter, $extra, 1, $model, $dicd, "BLANK");
                    $di_cd .= "</td>";

                    if ($idou_disable[$Row["SCHREGNO"]][$date] > 0) {
                        $idouDayCnt++;
                    }
                } else {
                    //背景色
                    $bgcolor = "#ffffff";
                    //サイズ（幅）
                    $width = "70";

                    $di_cd .= "<td width=".$width." align=\"center\" bgcolor=".$bgcolor."></td>";
                }
            }

            //異動区分
            if ($idouDayCnt == ((int)$day_to - (int)$day_from + 1)) {
                $idouType = 'all_day';      //全日異動
            } elseif ($idouDayCnt > 0) {
                $idouType = 'some_day';     //一部異動
            } else {
                $idouType = '';             //異動なし
            }

            //出欠の備考取得
            if (isset($model->warning)) {
                //エラーの場合
                $remark = $Row["REMARK"];
            } else {
                $remark = $remark_array[$Row["SCHREGNO"]];
            }
            //出欠の備考の背景色
            if ($idou > 0) {
                $bgcolor = "yellow";    //異動
            } elseif ($colorFlg) {
                $bgcolor = "#ffffff";
            } else {
                $bgcolor = "#cccccc";
            }
            //出欠の備考テキスト
            $width = "*";
            $di_cd .= "<td width=".$width." align=\"center\" bgcolor=\"{$bgcolor}\">";
            /* Edit by HPA for PC-talker 読み start 2020/02/03 */
            $di_cd .= knjCreateTextBox($objForm, $remark, "REMARK_".$counter, 30, 30, "aria-label =\"".$setData["NAME_SHOW"]." の 出欠の備考\"");
            /* Edit by HPA for PC-talker 読み end 2020/02/20 */
            $di_cd .= "</td>";

            $setData["DI_CD"] = $di_cd;

            $arg["data"][] = $setData;

            knjCreateHidden($objForm, "SCHREGNO"."_".$counter, $Row["SCHREGNO"]);
            knjCreateHidden($objForm, "LESSONCNT"."_".$counter, $lessonCnt);
            knjCreateHidden($objForm, "IDOU_TYPE"."_".$counter, $idouType);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $schList, $dateFlg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useFi_Hrclass", $model->Properties["useFi_Hrclass"]);
        knjCreateHidden($objForm, "useSpecial_Support_Hrclass", $model->Properties["useSpecial_Support_Hrclass"]);
        //データ保持用
        knjCreateHidden($objForm, "HIDDEN_SCHOOL_KIND");
        knjCreateHidden($objForm, "HIDDEN_MONTH_SEM");
        knjCreateHidden($objForm, "HIDDEN_HR_CLASS_TYPE");
        knjCreateHidden($objForm, "HIDDEN_GRADE_MIX");
        knjCreateHidden($objForm, "HIDDEN_GRADE_HR_CLASS");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjc034dForm1.html", $arg);
    }
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model)
{
    $query = knjc034dQuery::selectSemesAll();
    $result = $db->query($query);
    $data = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    $opt_month  = array();
    $opt_month[] = array("label" => "", "value" => "");

    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            //対象月一覧取得（管理者コントロール）
            $getdata = $db->getRow(knjc034dQuery::selectMonthQuery($month, $model), DB_FETCHMODE_ASSOC);
            if (is_array($getdata)) {
                $opt_month[] = array("label" => $getdata["NAME1"]." (".$data[$dcnt]["SEMESTERNAME"].") ",
                                     "value" => $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]);
            }
        }
    }
    if ($model->month_sem == "" || $model->month_sem == null) {
        $model->month_sem = "";
        if ($model->Properties["setDefaultMonthCtrlDate"] == '1' && $model->cmd == '') {
            $dateSemester = $db->getOne(knjc034dQuery::getDateSemester(CTRL_DATE));
            $dateMonth = date("m", strtotime(CTRL_DATE));
            $ctrlMonth = $dateMonth."-".$dateSemester;
            $ctrlMonth2 = sprintf("%02d", $dateMonth)."-".$dateSemester;
            foreach ($opt_month as $opt) {
                if ($opt["value"] == $ctrlMonth || $opt["value"] == $ctrlMonth2) {
                    $model->month_sem = $opt["value"];
                    break;
                }
            }
        }
    }
    /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
    $extra = "id = \"MONTH_SEM\" aria-label = \"月\" onChange=\"current_cursor('MONTH_SEM');btn_submit('edit')\";";
    $arg["MONTH_SEM"] = knjCreateCombo($objForm, "MONTH_SEM", $model->month_sem, $opt_month, $extra, 1);
    /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成（表内）
function makeCmbReturn(&$objForm, &$arg, $db, $combo_array, &$value, $name, $extra, $size, $model, $dicd, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    for ($i = 0; $i < get_count($combo_array); $i++) {
        if ($model->Properties["attend_Shosai"] == 1) {
            //孫コードがあるときは子コードは出力しない
            if (in_array($combo_array[$i]["VALUE"], $dicd["omitcd"])) {
                continue;
            }
        }
        $opt[] = array("label" => $combo_array[$i]["LABEL"],
                        "value" => $combo_array[$i]["VALUE"]);
        if ($value == $combo_array[$i]["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    //出欠（詳細）表示
    if ($model->Properties["attend_Shosai"] == 1) {
        $setCombo  = "";
        $setCombo .= "<select name='".$name."' size='1' ".$extra.">";

        $tmpP = "";
        for ($i = 0; $i < get_count($opt); $i++) {
            //選択されている値
            $selected = ($value == $opt[$i]["value"]) ? "selected" : "";

            list($di_cd, $subl_cd, $subm_cd) = preg_split("/-/", $opt[$i]["value"]);

            if ($tmpP != $di_cd && in_array($tmpP.'-0000-0000', $dicd["parents"])) {
                $setCombo .= "</optgroup>";
            }

            if (in_array($opt[$i]["value"], $dicd["parents"])) {
                //子・孫コードがあればグループ化
                $setCombo .= "<optgroup label='".$opt[$i]["label"]."'>";
                /* Edit by HPA for PC-talker 読み start 2020/02/03 */
                $optGroup = $opt[$i]["label"];
            } else {
                if ($opt[$i]["label"] == "忌引" || $opt[$i]["label"] == "事欠" || $opt[$i]["label"] == "遅刻" || $opt[$i]["label"] == "早退" || $opt[$i]["label"] == "遅刻+早退") {
                    $label ="";
                } else {
                    $label =" aria-label = \"".$optGroup."の".$opt[$i]["label"]."\"";
                }
                $setCombo .= "<option value='".$opt[$i]["value"]."' $label ".$selected.">".$opt[$i]["label"]."</option>";
                /* Edit by HPA for PC-talker 読み end 2020/02/20 */
            }
            $tmpP = $di_cd;
        }
        $setCombo .= "</select>";

        return $setCombo;
    } else {
        return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $schList, $dateFlg)
{
    //更新
    /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
    $extra = ($model->grade_hr_class && $model->month_sem && get_count($schList) > 0 && $dateFlg) ? "id = \"update\" aria-label = \"更新\" onclick=\"current_cursor('update');return btn_submit('update');\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = ($model->grade_hr_class && $model->month_sem && get_count($schList) > 0 && $dateFlg) ? "id = \"reset\" aria-label = \"取消\" onclick=\"current_cursor('reset');return btn_submit('reset');\"" : "disabled";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "aria-label = \"終了\" onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */
}

//任意の年月の第n曜日の日付を求める関数
//  $year 年
//  $month 月
//  $number 何番目の曜日か、第1曜日なら1。第3曜日なら3
//  $dayOfWeek 求めたい曜日。0～6までの数字で曜日の日～土を指定する
function getWhatDayOfWeek($year, $month, $number, $dayOfWeek)
{
    //指定した年月の1日の曜日を取得
    $firstDayOfWeek = date("w", mktime(0, 0, 0, $month, 1, $year));
    $day = (int)$dayOfWeek - (int)$firstDayOfWeek + 1;
    //1週間を足す
    if ($day <= 0) {
        $day += 7;
    }
    $weekselect = mktime(0, 0, 0, $month, $day, $year);
    //n曜日まで1週間を足し込み
    $weekselect += (86400 * 7 * ((int)$number - 1));
    return date("Y-m-d", $weekselect);
}
