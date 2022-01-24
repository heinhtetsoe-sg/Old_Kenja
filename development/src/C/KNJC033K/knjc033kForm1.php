<?php

require_once('for_php7.php');


//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjc033kForm1 {
    function main(&$model)
    {
        $objForm = new form;
        /* CSV */
        $objUp = new csvFile();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc033kindex.php", "", "main");

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //DB接続
        $db = Query::dbCheckOut();

        // 学期
        $query = knjc033kQuery::getSemesterName(CTRL_YEAR, CTRL_SEMESTER);
        $semesterName = $db->getOne($query);
        $arg["semester"] = $semesterName;

        //欠課種別コンボ
        $query = knjc033kQuery::getSickDiv();
        $extra = "onChange=\"btn_submit('kekka_syubetu')\";";
        makeCmb($objForm, $arg, $db, $query, $model, "SICK", $extra);

        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            //課程学科コンボ
            $query = knjc033kQuery::c035fgetCourseMajor($model, $model->field["SCHOOL_KIND"]);
            $extra = "onChange=\"btn_submit('course')\";";
            $model->field["SUBCLASSCD"] = ($model->cmd == "course") ? "" : $model->field["SUBCLASSCD"];
            makeCmb($objForm, $arg, $db, $query, $model, "COURSE_MAJOR", $extra, "BLANK");
        }

        //科目コンボ
        $query = knjc033kQuery::selectSubclassQuery($model);
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $model->field["CHAIRCD"] = ($model->cmd == "subclasscd") ? "" : $model->field["CHAIRCD"];
        $subclassName = makeCmb($objForm, $arg, $db, $query, $model, "SUBCLASSCD", $extra, "BLANK");

        //講座コンボ
        $query = knjc033kQuery::selectChairQuery($model);
        $extra = "onChange=\"btn_submit('chaircd')\";";
        $chairName = makeCmb($objForm, $arg, $db, $query, $model, "CHAIRCD", $extra, "BLANK");

        //月コンボ
        $semeMonth = makeMonthSemeCmb($objForm, $arg, $db, $model);

        //メインラベル作成
        $header = makeMainLabel($objForm, $arg, $db, $semeMonth, $model);

        /* CSV設定 */
        setCsv($objForm, $arg, $objUp, $subclassName, $chairName, $header);

        //メインデータ作成
        $std_num = makeMainData($objForm, $arg, $db, $model, $semeMonth, $objUp, $subclassName, $chairName, $header);

        //更新用データ
        $std_appointed_day = makeInputData($objForm, $arg, $db, $model);

        /* ボタン作成 */
        makeButton($objForm, $arg, $db, $model);

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        //hiddenを作成する
        makeHidden($objForm, $arg, $std_appointed_day, $std_num, $model);
        knjCreateHidden($objForm, "changeVal", 0);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "use_Attend_zero_hyoji", $model->Properties["use_Attend_zero_hyoji"]);
        //コンボ変更時、MSG108表示用
        knjCreateHidden($objForm, "SELECT_SUBCLASSCD", $model->field["SUBCLASSCD"]);
        knjCreateHidden($objForm, "SELECT_CHAIRCD", $model->field["CHAIRCD"]);
        knjCreateHidden($objForm, "SELECT_MONTH", $model->field["MONTHCD"]);
        knjCreateHidden($objForm, "SELECT_SICK", $model->field["SICK"]);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc033kForm1.html", $arg);
    }
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model)
{
    $query      = knjc033kQuery::selectSemesAll();
    $result     = $db->query($query);
    $data       = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    $opt_month = setMonth($db, $data, "DATA", $objForm, $model);

    $arg["MONTHCD"] = createCombo($objForm, "MONTHCD", $model->field["MONTHCD"], $opt_month, "onChange=\"btn_submit('change')\";", 1);
    return $data;
}

//学期＋月データ取得
function setMonth($db, $data, $sqldiv, &$objForm, $model)
{
    $cnt = 1;
    $opt_month = array();
    $is_this_first_flag = true;
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $query = knjc033kQuery::selectMonthQuery($month, $sqldiv, $model);
            $getdata = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($getdata)) {
                if ($sqldiv == "DATA") {
                    if ($is_this_first_flag) {
                        $opt_month[] = array("label" => '',
                                             "value" => '');
                        $is_this_first_flag = false;
                    }
                    $opt_month[] = array("label" => $getdata["NAME1"]." (".$data[$dcnt]["SEMESTERNAME"].") ",
                                         "value" => $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]);
                    knjCreateHidden($objForm, "LIST_MONTH" . $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"], $cnt);
                    $cnt++;
                } else {
                    $opt_month[] = array("MONTH"        => $getdata["NAMECD2"],
                                         "MONTHNAME"    => ($getdata["NAMECD2"] * 1)."月",
                                         "SEMESTER"     => $data[$dcnt]["SEMESTER"],
                                         "SEMESTERNAME" => $data[$dcnt]["SEMESTERNAME"]);
                }
            }
        }
    }
    return $opt_month;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$model, $name, $extra, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $cnt = 0;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
        $cnt++;
    }

    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "SICK" && $row["NAMESPARE1"] == '1') {
            $syokiti = $row["VALUE"];
        }
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($name == "SUBCLASSCD") {
            knjCreateHidden($objForm, "LIST_SUBCLASSCD" . $row["VALUE"], $cnt);
            $cnt++;
        }
        if ($name == "CHAIRCD") {
            knjCreateHidden($objForm, "LIST_CHAIRCD" . $row["VALUE"], $cnt);
            $cnt++;
        }
        if ($name == "SICK") {
            knjCreateHidden($objForm, "LIST_SICK" . $row["VALUE"], $cnt);
            $cnt++;
        }
    }

    if ($name == "SICK") {
        if (strlen($syokiti)) {
            $model->field[$name] = ($model->field[$name]) ? $model->field[$name] : $syokiti;
        } else {
            $model->field[$name] = ($model->field[$name]) ? $model->field[$name] : $opt[0]["value"];
        }
    }

    $arg[$name] = createCombo($objForm, $name, $model->field[$name], $opt, $extra, 1);

    $rtnName = "";
    for ($i = 0; $i < get_count($opt); $i++) {
        $rtnName = ($opt[$i]["value"] == $model->field[$name]) ? $opt[$i]["label"] : $rtnName;
    }
    return $rtnName;
}

//メインラベル作成
function makeMainLabel(&$objForm, &$arg, $db, $semeMonth, $model)
{
    $monthdata     = setMonth($db, $semeMonth, "LABEL", $objForm, $model);
    $tr_class      = "class=\"no_search\"";
    $attendlink    = "<th width=\"70\"  align=\"center\" nowrap>年-組-番</th>";
    $name_show     = "<th width=\"108\" align=\"center\">氏名</th>";
    $total         = "<th               align=\"center\">合<br>計</th>";
    $lesson        = "<th               align=\"center\"><font size=\"1\">出席<br>すべ<br>き<br>授業<br>時数<font></th>";
    $t_notice      = "<th               align=\"center\">欠<br>時<br>数</th>";
    $t_lateearly   = "<th               align=\"center\">遅<br>刻<br>数</th>";
    $notice_late   = "<th               align=\"center\">欠<br>課<br>数</th>";

    $setTitle = array("SUBCLASS"        => "科目コード",
                      "SUBCLASSNAME"    => "科目名称",
                      "CHAIR"           => "講座コード",
                      "CHAIRNAME"       => "講座名称",
                      "SCHREGNO"        => "学籍番号",
                      "NAME_SHOW"       => "氏名",
                      "ATTENDNO"        => "年組番",
                      "TOTAL"           => "合計",
                      "LESSON"          => "授業時数",
                      "T_NOTICE"        => "欠次数",
                      "T_LATEEARLY"     => "遅刻数",
                      "NOTICE_LATE"     => "欠課数");

    $rtnData = setData($arg, $db, $tr_class, $attendlink, $name_show, $total, $lesson, $t_notice, $t_lateearly, $notice_late, $semeMonth, $monthdata, "LABEL", $setTitle, $model);

    return $rtnData;
}

//CSV設定
function setCsv(&$objForm, &$arg, &$objUp, $subclassName, $chairName, $header)
{
    $objUp->setHeader(array_values($header));

    //CSV出力ファイル名
    $objUp->setFileName(CTRL_YEAR."年度_".$subclassName."_".$chairName."_"."欠時数情報入力.csv");

    for ($i = 0; $i < get_count($header); $i++) {
        if ($i > 6 && $i < get_count($header) - 2) {
            $type[$i] = 'S';
            $size[$i] = 2;
        }
    }

    $objUp->setType($type);
    $objUp->setSize($size);
}

//入力フィールド作成
function makeInputData(&$objForm, &$arg, $db, &$model)
{
    if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
        if (in_array($model->cmd, array("course", "subclasscd", "chaircd", "change", "reset"))) {
            $model->field["LESSON_SET"] = "";
        }
        list($month, $seme) = preg_split("/-/", $model->field["MONTHCD"]);

        //入力されているMAX授業時数取得
        $query = knjc033kQuery::c035fgetInputMaxLesson($model);
        $input_lesson = $db->getOne($query);

        //課程学科
        $cm = $model->field["COURSE_MAJOR"];

        //入力されているMAX授業時数セット
        $model->field["LESSON_SET"] = ($model->field["LESSON_SET"]) ? $model->field["LESSON_SET"] : $input_lesson;

        $extra = "style=\"text-align: right;\" onChange=\"this.style.background='#ccffcc';\" onblur=\"this.value=toInteger(this.value);\"";

        //授業時数区分取得
        $query = knjc033kQuery::c035fgetJugyouJisuFlg($model, $cm);
        $jugyou_jisu_flg = $db->getOne($query);

        //レコードなし＆日々出欠なし＆法定時数のとき
        if (!$model->field["LESSON_SET"] && $model->Properties["hibiNyuuryokuNasi"] == "1" && $model->Properties["useJugyoujisuuSanshutsu"] == "1" && $jugyou_jisu_flg == "1") {
            $syusu = $credit = 0;

            //学期の最終月判定
            $query = knjc033kQuery::c035fgetMaxSemeMonthCnt($model);
            $maxMonth = $db->getOne($query);

            //単位数取得
            $query = knjc033kQuery::c035fgetCredit($model);
            $credit = $db->getOne($query);

            //月別週数を使用するとき
            if ($model->Properties["use_Month_Syusu"] == "1") {
                //月毎の週数取得
                $query = knjc033kQuery::c035fgetMonthSyusu($model, $cm);
                $syusu = $db->getOne($query);

                if ($syusu > 0 && $credit > 0) {
                    $extra = "style=\"text-align: right;background-color: #ff0099;\" onblur=\"this.value=toInteger(this.value);\"";
                    $model->field["LESSON_SET"] = $credit * $syusu;
                }

            //学期の最終月のとき
            } else if ($maxMonth == "1") {
                //学期の週数取得
                $query = knjc033kQuery::c035fgetSyusu($model, $model->field["SEMESTER"], $cm);
                $syusu = $db->getOne($query);

                if ($syusu > 0 && $credit > 0) {
                    //学期内で合算したLESSONのMAX値を取得
                    $query = knjc033kQuery::c035fgetMaxSumLesson($model);
                    $maxLesson = $db->getOne($query);

                    $extra = "style=\"text-align: right;background-color: #ff0099;\" onblur=\"this.value=toInteger(this.value);\"";
                    $cre_syu = $credit * $syusu;
                    $model->field["LESSON_SET"] = ($cre_syu - $maxLesson > 0) ? $cre_syu - $maxLesson : "";
                }
            }
        }
        //授業時数テキスト(セット用)
        $arg["LESSON_SET"] = knjCreateTextBox($objForm, $model->field["LESSON_SET"], "LESSON_SET", 3, 3, $extra);

        $setAppday = "";
        $appSep = "";
        $model->gradeArray = is_array($model->gradeArray) ? $model->gradeArray : array();
        $hiddenGradeAppDay = "";
        $GradeAppSep = "";
        foreach ($model->gradeArray as $key => $val) {
            $query = knjc033kQuery::getSchregRegdGdatGradeCd($val);
            $gradeCd = $db->getOne($query);
            $query = knjc033kQuery::getAppointedGradeDay($month, $seme, $val);
            $gradeAppDay = $db->getOne($query);
            $setAppday .= $appSep.($gradeCd * 1)."年=".$gradeAppDay;
            $appSep = " ";
            $hiddenGradeAppDay .= $GradeAppSep.$val."_".$gradeAppDay;
            $GradeAppSep = ":";
        }
        $arg["APPOINTED_DAY"] = $setAppday;

        //hidden
        knjCreateHidden($objForm, "GradeAppDay", $hiddenGradeAppDay);

        return $model->appointed_day;
    }

    if ($model->cmd == "subclasscd" || $model->cmd == "change") {
        $model->appointed_day = "";
        $model->lesson = "";
    }

    $query = knjc033kQuery::selectInputQuery($model);
    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
    $model->lesson = ($model->lesson) ? $model->lesson : $row["LESSON"];

    list($month, $seme) = preg_split("/-/", $model->field["MONTHCD"]);
    $query = knjc033kQuery::getMaxSemeMonthCnt($month, $seme);
    $maxMonth = $db->getOne($query);
    $extra = " onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onKeyDown=\"keyChangeEntToTab(this)\";";
    if (!$model->lesson && $maxMonth == "1") {
        $query = knjc033kQuery::getSyusu($model, $seme);
        $syusu = $db->getOne($query);
        if ($syusu > 0 && $model->credit > 0 && $model->Properties["hibiNyuuryokuNasi"] == "1") {
            //学期内で合算したLESSONのMAX値を取得
            $query = knjc033kQuery::getMaxSumLesson($model);
            $maxLesson = $db->getOne($query);

            $extra = "STYLE=\"background-color : #ff0099 \" ";
            $cre_syu = $model->credit * $syusu;
            $model->lesson = ($cre_syu - $maxLesson > 0) ? $cre_syu - $maxLesson : "";
        }
    }

    $setAppday = "";
    $appSep = "";
    $model->gradeArray = is_array($model->gradeArray) ? $model->gradeArray : array();
    $hiddenGradeAppDay = "";
    $GradeAppSep = "";
    foreach ($model->gradeArray as $key => $val) {
        $query = knjc033kQuery::getSchregRegdGdatGradeCd($val);
        $gradeCd = $db->getOne($query);
        $query = knjc033kQuery::getAppointedGradeDay($month, $seme, $val);
        $gradeAppDay = $db->getOne($query);
        $setAppday .= $appSep.($gradeCd * 1)."年=".$gradeAppDay;
        $appSep = " ";
        $hiddenGradeAppDay .= $GradeAppSep.$val."_".$gradeAppDay;
        $GradeAppSep = ":";
    }
    $arg["APPOINTED_DAY"] = $setAppday;
    $arg["LESSON_SET"] = createText($objForm, "LESSON_SET", $model->lesson, $extra, 2, 3);

    //hidden
    knjCreateHidden($objForm, "GradeAppDay", $hiddenGradeAppDay);

    return $model->appointed_day;
}

//メインデータ作成
function makeMainData(&$objForm, &$arg, $db, &$model, $semeMonth, &$objUp, $subclassName, $chairName, $headerData)
{
    $model->credit = 0;
    $hiddenSchGradeArray = "";
    if(($model->field["SUBCLASSCD"] != "" && $model->field["SUBCLASSCD"] != NULL) &&
       ($model->field["CHAIRCD"] != "" && $model->field["CHAIRCD"] != NULL) &&
       ($model->sickdiv[$model->field["SICK"]] != "" && $model->sickdiv[$model->field["SICK"]] != NULL)) {

        $monthdata  = setMonth($db, $semeMonth, "LABEL", $objForm, $model);
        $target_month = preg_replace('/\-.*/', ' ', $model->field["MONTHCD"]);
        $target_month = trim($target_month);
        if ($target_month < 4) {
            $year = CTRL_YEAR + 1;
        } else {
            $year = CTRL_YEAR;
        }
        $target_month = $year . $target_month;

        $query = knjc033kQuery::selectQuery($model, $monthdata, $target_month);
        $result = $db->query($query);

        //遅刻何回で欠課とするかの指数取得
        $absent = array();
        $absent = $db->getRow(knjc033kQuery::getScAbsentCov($model),DB_FETCHMODE_ASSOC);

        //休学時の欠課をカウントするかどうかのフラグ(1 or NULL)を取得。1:欠課をカウントする
        $offdaysFlg = $db->getRow(knjc033kQuery::getOffdaysFlg($model, CTRL_YEAR),DB_FETCHMODE_ASSOC);

        //生徒数取得
        knjCreateHidden($objForm, "objCntSub", get_count($db->getCol($query)));

        $counter  = 0;
        $colorFlg = false;
        $model->gradeArray = array();
        $schGradeSep = "";
        for ($i = 1; $row = $result->fetchRow(DB_FETCHMODE_ASSOC); $i++) {
            $model->gradeArray[$row["GRADE"]] = $row["GRADE"];
            $hiddenSchGradeArray .= $schGradeSep.$row["SCHREGNO"]."_".$row["GRADE"];
            $schGradeSep = ":";

            if ($i == 1) {
                $query = knjc033kQuery::getCredit($row, $model);
                $model->credit = $db->getOne($query);
            }
            //欠課時数作成
            $name     = $model->sickdiv[$model->field["SICK"]]."-".sprintf("%02d", $i);
            $extra    = " STYLE=\"text-align: right; width: 80%;\" onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onblur=\"this.value=toInteger(this.value);\" onKeyDown=\"keyChangeEntToTab(this);checkkey(this);\" onPaste=\"return showPaste(this);\" class=\"nomsclear\"";
            $meisai   = array();    //欠課時数
            $totalcnt = 0;          //合計
            //学期単位合計初期化
            for ($scnt = 1; $scnt <= get_count($semeMonth); $scnt++) {
                $totalsem[$scnt] = array("SEMESTER" => 0);
            }

            //異動月取得
            $smonth = $emonth = array();
            $smonth = $db->getCol(knjc033kQuery::getTransferData2($row["SCHREGNO"], "s"));
            $emonth = $db->getCol(knjc033kQuery::getTransferData2($row["SCHREGNO"], "e"));

            $headerSeq = 0;

            for ($setcnt = 0; $setcnt < get_count($monthdata); $setcnt++) {
                //未入力判定
                $meisai[$setcnt]["FLG"] = strlen($row["SUB_SCHREGNO".($setcnt + 1)]) == 0 ? "NG" : "OK";

                if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                    $meisai[$setcnt]["MEISAI"] = $row["MONTH".($setcnt + 1)];
                } else {
                    $meisai[$setcnt]["MEISAI"] = ($row["MONTH".($setcnt + 1)] != 0) ? $row["MONTH".($setcnt + 1)] : "";
                }
                $meisai[$setcnt]["MEISAICSV"] = ($row["MONTH".($setcnt + 1)] != 0) ? $row["MONTH".($setcnt + 1)] : "";
                if ($model->field["MONTHCD"] == $monthdata[$setcnt]["MONTH"]."-".$monthdata[$setcnt]["SEMESTER"]) {
                    if (isset($model->warning)) {
                        $row["MONTH".($setcnt + 1)] = $model->reset[$row["SCHREGNO"]];
                    }
                    $headerSeq = $setcnt;
                    if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                        $value = $row["MONTH".($setcnt + 1)];
                    } else {
                        $value = ($row["MONTH".($setcnt + 1)] != 0) ? $row["MONTH".($setcnt + 1)] : "";
                    }
                    $meisai[$setcnt]["MEISAI"] = createText($objForm, $name, $value, $extra, 2, 3);
                }
                if ($row["SEMESTER".($setcnt + 1)]) {
                    $totalsem[$row["SEMESTER".($setcnt + 1)]]["SEMESTER"] = $totalsem[$row["SEMESTER".($setcnt + 1)]]["SEMESTER"] + $row["MONTH".($setcnt + 1)];
                    $totalcnt = $totalcnt + $row["MONTH".($setcnt + 1)];
                }

                //異動者（退学・転学・卒業）
                $ido_appointed_day = $db->getOne(knjc033kQuery::getAppointedGradeDay($monthdata[$setcnt]["MONTH"], $monthdata[$setcnt]["SEMESTER"], $row["GRADE"]));
                $idou_year = ($monthdata[$setcnt]["MONTH"] < '04') ? CTRL_YEAR + 1 : CTRL_YEAR;
                $idou_day = ($ido_appointed_day == "") ? getFinalDay($db, $monthdata[$setcnt]["MONTH"], $monthdata[$setcnt]["SEMESTER"], $row["GRADE"]) : $ido_appointed_day;
                $idou_date = $idou_year.'-'.$monthdata[$setcnt]["MONTH"].'-'.$idou_day;
                $idou = $db->getOne(knjc033kQuery::getIdouData($row["SCHREGNO"], $idou_date));

                //異動者（留学・休学）
                $idou2 = $db->getOne(knjc033kQuery::getTransferData1($row["SCHREGNO"], $idou_date));
                $idou3 = 0;
                if (in_array(sprintf("%2d", $monthdata[$setcnt]["MONTH"]), $smonth) || in_array(sprintf("%2d", $monthdata[$setcnt]["MONTH"]), $emonth)) {
                    $idou3 = 1;
                }

                //異動期間は背景色を黄色にする
                $row["BGCOLOR_IDOU".($setcnt + 1)] = ($idou > 0 || $idou2 > 0 || $idou3 > 0) ? "bgcolor=yellow" : "";
            }

            $name = "SCHREGNO".sprintf("%02d", $i);
            $objForm->ae(createHiddenAe($name, $row["SCHREGNO"]));

            //リンク作成
            $row["ATTENDNO"] = $row["HR_NAMEABBV"] . "-" . $row["ATTENDNO"];
            $link_name  = "cmd,SCHREGNO,CHAIRCD,ATTENDNO,GRADE";
            $link_value = "newpopup,".$row["SCHREGNO"].",".$model->field["CHAIRCD"].",".$row["ATTENDNO"].",".$row["GRADE"];
            $row["ATTENDLINK"] = "javascript:openWindow('".$link_name."', '".$link_value."');";

            //5行毎に色を変える
            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
            $bgcolor_row = $colorFlg ? "#ffffff" : "#cccccc";
            $counter++;

            //累積情報
            $attend = array();
            if ($absent["ABSENT_COV"] == "0" || $absent["ABSENT_COV"] == "2" || $absent["ABSENT_COV"] == "4") { 
                $row_ruikei = $db->getRow(knjc033kQuery::GetAttendData($model->field["CHAIRCD"],$model->field["SUBCLASSCD"],$absent["ABSENT_COV"],$absent["ABSENT_COV_LATE"], $offdaysFlg, $row["SCHREGNO"], $model), DB_FETCHMODE_ASSOC);
            } else {
                $row_ruikei = $db->getRow(knjc033kQuery::GetAttendData2($model->field["CHAIRCD"],$model->field["SUBCLASSCD"],$absent["ABSENT_COV"],$absent["ABSENT_COV_LATE"], $offdaysFlg, $row["SCHREGNO"], $model), DB_FETCHMODE_ASSOC);
            }

            $row_ruikei["LESSON"]      = strlen($row_ruikei["LESSON"])      ? $row_ruikei["LESSON"] : 0;
            $row_ruikei["T_NOTICE"]    = strlen($row_ruikei["T_NOTICE"])    ? $row_ruikei["T_NOTICE"] : 0;
            $row_ruikei["T_LATEEARLY"] = strlen($row_ruikei["T_LATEEARLY"]) ? $row_ruikei["T_LATEEARLY"] : 0;
            $row_ruikei["NOTICE_LATE"] = strlen($row_ruikei["NOTICE_LATE"]) ? $row_ruikei["NOTICE_LATE"] : 0;

            //欠課の上限値取得
            $warn = $db->getRow(knjc033kQuery::getJogenchi($row, $model->field["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);

            if ($row_ruikei["NOTICE_LATE"] > $warn["RISHU_JOGENCHI"] && $warn["RISHU_JOGENCHI"] > 0) {
                $bgcolor_warn = "bgcolor=\"red\"";      //累積警告
            } else if ($row_ruikei["NOTICE_LATE"] > $warn["SHUTOKU_JOGENCHI"] && $warn["SHUTOKU_JOGENCHI"] > 0) {
                $bgcolor_warn = "bgcolor=\"yellow\"";   //累積注意
            } else {
                $bgcolor_warn = "";
            }

            $tr_class    = "bgcolor=\"{$bgcolor_row}\"";
            if ($model->Properties["hibiNyuuryokuNasi"] == "1") {
                $attendlink  = "<td width=\"70\"  align=\"center\" {$bgcolor_warn} >".$row["ATTENDNO"]."</td>";
            } else {
                $attendlink  = "<td width=\"70\"  align=\"center\" {$bgcolor_warn} ><a href=\"".$row["ATTENDLINK"]."\">".$row["ATTENDNO"]."</a></td>";
            }
            $name_show   = "<td width=\"108\" align=\"center\" {$bgcolor_warn} >".$row["NAME_SHOW"]."</td>";
            $total       = "<th               align=\"center\">".$totalcnt."</th>";
            $lesson      = "<th               align=\"center\">".$row_ruikei["LESSON"]."</th>";
            $t_notice    = "<th               align=\"center\">".$row_ruikei["T_NOTICE"]."</th>";
            $t_lateearly = "<th               align=\"center\">".$row_ruikei["T_LATEEARLY"]."</th>";
            $notice_late = "<th               align=\"center\">".$row_ruikei["NOTICE_LATE"]."</th>";


            $row["TOTAL"]       = $totalcnt;
            $row["LESSON"]      = $row_ruikei["LESSON"];
            $row["T_NOTICE"]    = $row_ruikei["T_NOTICE"];
            $row["T_LATEEARLY"] = $row_ruikei["T_LATEEARLY"];
            $row["NOTICE_LATE"] = $row_ruikei["NOTICE_LATE"];

            $row["SUBCLASS"] = $model->field["SUBCLASSCD"];
            $row["SUBCLASSNAME"] = $subclassName;
            $row["CHAIR"] = $model->field["CHAIRCD"];
            $row["CHAIRNAME"] = $chairName;

            $rtnData = setData($arg, $db, $tr_class, $attendlink, $name_show, $total, $lesson, $t_notice, $t_lateearly, $notice_late, $totalsem, $meisai, "MEISAI", $row, $model);
            setCsvValue($objUp, $model, $headerData, $subclassName, $chairName, $rtnData, $i, $headerSeq);
        }
        $result->free();
        //件数
        knjCreateHidden($objForm, "COUNTER", $counter);
    } else {
        //件数
        knjCreateHidden($objForm, "COUNTER", 0);
    }
    //hidden
    knjCreateHidden($objForm, "SchGradeArray", $hiddenSchGradeArray);

    return $i;
}

//データセット
function setData(&$arg, $db, $tr_class, $attendlink, $name_show, $total, $lesson, $t_notice, $t_lateearly, $notice_late, $semeMonth, $monthdata, $putdiv, $setRow, $model)
{
    $title["TR_CLASS"]   = $tr_class;
    $title["ATTENDLINK"] = $attendlink;
    $title["NAME_SHOW"]  = $name_show;

    $rtnData[] = $setRow["SUBCLASS"];
    $rtnData[] = $setRow["SUBCLASSNAME"];
    $rtnData[] = $setRow["CHAIR"];
    $rtnData[] = $setRow["CHAIRNAME"];
    $rtnData[] = $setRow["ATTENDNO"];
    $rtnData[] = $setRow["SCHREGNO"];
    $rtnData[] = $setRow["NAME_SHOW"];

    $monthcnt = get_count($semeMonth);

    //各月
    $seme = 0;
    if ($monthcnt < 3) {
        $seme = 40 * (3 - $monthcnt);
    }
    $monthWidth = round((400 + $seme) / get_count($monthdata));
    for ($i = 0; $i < get_count($monthdata); $i++) {
        if ($putdiv == "LABEL") {
            $title["MONTH".($i + 1)]  = "<th width=\"".$monthWidth."\"  align=\"center\">".$monthdata[$i]["MONTHNAME"]."<br>";
            $title["MONTH".($i + 1)] .= $monthdata[$i]["SEMESTERNAME"]."</th>";

            $rtnData[] = $monthdata[$i]["MONTHNAME"].$monthdata[$i]["SEMESTERNAME"];

        } else {
            $color = $monthdata[$i]["FLG"] == "NG" ? "bgcolor=#ccffcc" : "";
            $color = ($setRow["BGCOLOR_IDOU".($i + 1)] == "") ? $color : $setRow["BGCOLOR_IDOU".($i + 1)];
            $title["MONTH".($i + 1)]  = "<td width=\"".$monthWidth."\"  align=\"right\" ".$color.">".$monthdata[$i]["MEISAI"]."</td>";

            $rtnData[] = $monthdata[$i]["MEISAICSV"];

        }
    }

    //学期計
    for ($i = 0; $i < $monthcnt; $i++) {
        if ($putdiv == "LABEL") {
            $query = knjc033kQuery::getSickName($model);
            $sickName = $db->getOne($query);
            $title["TOTAL_SEM".($i + 1)] = "<th width=\"35\" align=\"center\">{$semeMonth[$i]["SEMESTERNAME"]}<br>{$sickName}</th>";

            $rtnData[] = $semeMonth[$i]["SEMESTERNAME"].$sickName;

        } else {
            $title["TOTAL_SEM".($i + 1)] = "<th width=\"35\" align=\"center\">".$semeMonth[$i + 1]["SEMESTER"]."</th>";

            $rtnData[] = $semeMonth[$i + 1]["SEMESTER"];

        }
    }
    $title["TOTAL"]       = $total;
    $title["LESSON"]      = $lesson;
    $title["T_NOTICE"]    = $t_notice;
    $title["T_LATEEARLY"] = $t_lateearly;
    $title["NOTICE_LATE"] = $notice_late;

    $rtnData[] = $setRow["TOTAL"];
    $rtnData[] = $setRow["LESSON"];
    $rtnData[] = $setRow["T_NOTICE"];
    $rtnData[] = $setRow["T_LATEEARLY"];
    $rtnData[] = $setRow["NOTICE_LATE"];
    $rtnData[] = "DUMMY";

    $arg["data"][] = $title;

    return $rtnData;
}

//CSV値設定
function setCsvValue(&$objUp, $model, $headerData, $subclassName, $chairName, $row, $i, $headerSeq)
{
    //キー値をセット
    $key = array("科目コード"   => $model->field["SUBCLASSCD"],
                 "講座コード"   => $model->field["CHAIRCD"],
                 "学籍番号"     => $row[5]);

    //ゼロ埋めフラグ
    $flg = array("科目コード" => array(true,6),
                 "講座コード" => array(true,7),
                 "学籍番号"   => array(true,8));

    $objUp->setEmbed_flg($flg);

    $fieldCnt = ($headerSeq + 7);

    $objUp->setElementsValue($model->sickdiv[$model->field["SICK"]]."-".sprintf("%02d", $i), $headerData[$fieldCnt], $key);
    foreach ($row as $rowKey => $val) {
        $csv[] = $val;
    }
    $objUp->addCsvValue($csv);
}

//最終日取得
function getFinalDay($db, $month, $semester, $garade)
{
    $year = CTRL_YEAR;
    if ($month != "" && $month < "04") {
        $year = CTRL_YEAR + 1;
    }

    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year ));
    $semeday = $db->getRow(knjc033kQuery::selectSemesGradeAll($semester, $garade), DB_FETCHMODE_ASSOC);
    //学期マスタの最終日より大きい場合
    if (sprintf('%02d', $semeday["E_MONTH"]) == $month &&
        $semeday["E_DAY"] < $lastday) {
        $lastday = $semeday["E_DAY"];
    }
    return $lastday;
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{
    $link  = REQUESTROOT."/C/KNJC033K/knjc033kindex.php?";
    $link .= "cmd=replace&SCHREGNO=".$model->schregno."&MONTHCD=".$model->field["MONTHCD"];
    $link .= "&SUBCLASSCD=".$model->field["SUBCLASSCD"]."&CHAIRCD=".$model->field["CHAIRCD"];
    //一括更新ボタン
    $arg["btn_replace"] = createBtn($objForm, "btn_replace", "一括更新", "onclick=\"Page_jumper('$link');\"");
    //保存ボタン
    $arg["btn_update"] = createBtn($objForm, "btn_udpate", "更 新", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    $arg["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeMsg();\"");
    //印刷
    $arg["btn_print"] = CreateBtn($objForm, "btn_print", "印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");

    //セキュリティーチェック
    $securityCnt = $db->getOne(knjc033kQuery::getSecurityHigh());
    $csvSetName = "ＣＳＶ処理";
    if ($model->Properties["useXLS"]) {
        $csvSetName = "エクセル処理";
    }
    if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
        //ＣＳＶ処理ボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_C033K/knjx_c033kindex.php?SEND_PRGID=KNJC033K&SEND_AUTH={$model->auth}&SEND_SUBCLASSCD={$model->field["SUBCLASSCD"]}&SEND_CHAIRCD={$model->field["CHAIRCD"]}&SEND_MONTHCD={$model->field["MONTHCD"]}&SEND_SICK={$model->field["SICK"]}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $csvSetName, $extra);
    }
}

//hidden作成
function makeHidden(&$objForm, &$arg, $std_appointed_day, $std_num, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("APPOINTED_DAY", $std_appointed_day));
    $objForm->ae(createHiddenAe("std_num", $std_num - 1));
    $objForm->ae(createHiddenAe("chikokuHyoujiFlg", $model->Properties["chikokuHyoujiFlg"]));
    $objForm->ae(createHiddenAe("useCurriculumcd", $model->Properties["useCurriculumcd"]));
    $objForm->ae(createHiddenAe("useSchool_KindField" ,$model->Properties["useSchool_KindField"]));
    $objForm->ae(createHiddenAe("SCHOOLCD" ,sprintf("%012d", SCHOOLCD)));
    $objForm->ae(createHiddenAe("SCHOOL_KIND" ,SCHOOLKIND));
    
    $objForm->ae(createHiddenAe("DBNAME" ,DB_DATABASE));
    $objForm->ae(createHiddenAe("PRGID" ,"KNJC033K"));
    $objForm->ae(createHiddenAe("CTRL_DATE" ,CTRL_DATE));
    $objForm->ae(createHiddenAe("CTRL_SEMESTER" ,CTRL_SEMESTER));
    $objForm->ae(createHiddenAe("CTRL_YEAR" ,CTRL_YEAR));

    $objForm->ae(createHiddenAe("HIDDEN_SUBCLASSCD"));
    $objForm->ae(createHiddenAe("HIDDEN_CHAIRCD"));
    $objForm->ae(createHiddenAe("HIDDEN_MONTHCD"));
    $objForm->ae(createHiddenAe("HIDDEN_SICK"));
    $objForm->ae(createHiddenAe("HIDDEN_LESSON"));

    knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
    knjCreateHidden($objForm, "HIDDEN_COURSE_MAJOR");
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//テキスト作成
function createText(&$objForm, $name, $value, $extra, $size, $maxlen)
{
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "extrahtml" => $extra,
                        "value"     => $value));
    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}
?>
