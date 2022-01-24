<?php

require_once('for_php7.php');

class knjz023Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz023index.php", "", "main");
        $db = Query::dbCheckOut();

        $row = array();

        //処理年度
        $arg['YEAR'] = CTRL_YEAR;

        //学年コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjz023Query::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);

        //課程学科コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjz023Query::getCourseMajor($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["COURSE"], "COURSE", $extra, 1);

        //課程学科コンボ（コピー元）
        $extra = "";
        $query = knjz023Query::getCourseMajor($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["COPY_COURSE"], "COPY_COURSE", $extra, 1);

        //checkbox
        $extra = "id=\"ALL_CHECK\" onClick=\"allCheck(this)\"";
        $arg["ALL_CHECK"] = knjCreateCheckBox($objForm, "ALL_CHECK", "1", $extra);

        //クラス別設定データチェック
        $query = knjz023Query::getAttendSemesLessonDat($model);
        $getData = $db->getOne($query);
        if ($getData > 0) {
            $arg["Lesson_data"] = '1';
        }

        if ($model->Properties["useFi_Hrclass"] == '1') {
            //クラス区分ラジオボタン 1:法定 2:複式
            $opt = array(1, 2);
            $model->field["HR_CLASS_DIV"] = ($model->field["HR_CLASS_DIV"] == "") ? "1" : $model->field["HR_CLASS_DIV"];
            $extra = array("id=\"HR_CLASS_DIV1\"", "id=\"HR_CLASS_DIV2\"");
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_DIV", $model->field["HR_CLASS_DIV"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg[$key] = $val;
            $arg["hr_class_div"] = 1;
        } else {
            $model->field["HR_CLASS_DIV"] = ($model->field["HR_CLASS_DIV"] == "") ? "1" : $model->field["HR_CLASS_DIV"];
            knjCreateHidden($objForm, "HR_CLASS_DIV", 1);
            $arg["no_hr_class_div"] = 1;
        }

        //校種取得
        $schoolkind = $db->getOne(knjz023Query::getSchoolKind(CTRL_YEAR, $model->field["GRADE"]));

        //クラス別設定のコメント表示
        $arg["show_comment"] = ($model->Properties["useSchool_KindField"] == "1") ? 1 : "";

        //ボタン作成
        makeButton($objForm, $arg, $schoolkind);

        //リスト表示
        makeList($objForm, $arg, $db, $model);

        //hidden
        makeHidden($objForm);

        $arg["finish"] = $objForm->get_finish();
        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz023Form1.html", $arg); 
    }
}

/********************************************** 以下関数 **********************************************/

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($name == "COURSE" || $name == "COPY_COURSE") {
        $opt[] = array("label" => "--全て--","value" => '0000');
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//リスト表示
function makeList(&$objForm, &$arg, $db, $model) {
    //初期化
    $model->data = array();
    $before_target_month = "";
    $sem_month_list = "";
    if ($model->cmd == "reset") unset($model->fields);
    //学期違いで同一月がある場合のカウント変数
    $jyuhuku_month = "13";

    //学期リスト
    $query = knjz023Query::getList(CTRL_YEAR);
    $result = $db->query($query);
    $monthData = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $defAllLesson = 0;
        for ($i = $row['SMONTH']; $i <= $row['EMONTH']; $i++) {
            if ($i > 12) {
                $target_month = $i - 12;
            } else {
                $target_month = $i;
            }
            $monthData[$target_month] = (int)$monthData[$target_month] + 1;
            //授業日数取得
            $query = knjz023Query::getLesson(CTRL_YEAR, $model->field["GRADE"], $model->field["COURSE"], sprintf('%02d',$target_month), $row['SEMESTER']);
            $lesson = $db->getOne($query);
            $defLesson = 0;
            $sem_month = $row['SEMESTER'].'-'.sprintf('%02d', $target_month);
            $sem_month_list .= ($sem_month_list) ? ",".$sem_month : $sem_month;
            $setStyle = "";
            //日数計算
            if ($model->cmd == "defAll" || $model->cmd == "defMonth") {
                $lesson = $model->fields["LESSON"][$sem_month];
                $feDate = getFdateEdate($db, $target_month, $row["SEMESTER"], $monthData[$target_month]);

                //開始日付・終了日付
                $setYear = ((int)$target_month * 1) < 4 ? CTRL_YEAR + 1 : CTRL_YEAR;
                $fDay = $setYear."-".sprintf('%02d', $target_month)."-".sprintf('%02d', $feDate["SDAY"]);
                $eDay = $setYear."-".sprintf('%02d', $target_month)."-".sprintf('%02d', $feDate["EDAY"]);

                //行事予定日数取得
                $query = knjz023Query::getHoliCnt($model, $model->field["GRADE"], $row["SEMESTER"], $fDay, $eDay);
                $holiCnt = $db->getOne($query);

                //日数計算（月毎）
                $defLesson = (int)$feDate["EDAY"] - (int)$feDate["SDAY"] + 1 - (int)$holiCnt;
                if ($model->fields["AUTO_CHECK"][$sem_month] == "1" && $model->cmd == "defMonth") {
                    $lesson = $defLesson;
                    $setStyle = " background-color : #ff0099 ";
                }
                //日数計算（学期の最終月）
                $defAllLesson += $defLesson;
                if ($model->fields["AUTO_CHECK"][$sem_month] == "1" && $model->cmd == "defAll" && $i == $row['EMONTH']) {
                    $lesson = $defAllLesson;
                    $setStyle = " background-color : #ff0099 ";
                }

            } else if ($model->cmd == "copy") {
                $query = knjz023Query::getLesson(CTRL_YEAR, $model->field["GRADE"], $model->field["COPY_COURSE"], sprintf('%02d',$target_month), $row['SEMESTER']);
                $lesson = $db->getOne($query);
            }

            //授業日数テキストボックス
            $model->fields["LESSON"][$sem_month] = $lesson;
            $extra = "onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align: right; {$setStyle}\"";
            $row["LESSON"] = knjCreateTextBox($objForm, $model->fields["LESSON"][$sem_month], "LESSON".$sem_month, 3, 3, $extra);

            //月の表示文字するをDBから取得
            $query = knjz023Query::getMonthName($row['YEAR'], sprintf('%02d',$target_month));
            $monthName = $db->getOne($query);
            $row["MONTHNAME"] = $monthName;

            //checkbox
            $extra = $model->fields["AUTO_CHECK"][$sem_month] == "1" ? " checked " : "";
            $row["AUTO_CHECK"] = knjCreateCheckBox($objForm, "AUTO_CHECK".$sem_month, "1", $extra);

            $arg["data"][] = $row;
            $before_target_month = $target_month;
        }
    }
    $result->free();

    knjCreateHidden($objForm, "sem_month_list", $sem_month_list);
}

function getFdateEdate($db, $month, $seme, $monthCnt) {
    $query = knjz023Query::getLessonDay($month, $seme);
    $lessonDay = $db->getRow($query, DB_FETCHMODE_ASSOC);
    if ($monthCnt == 1) {
        $sDay = 1;
    } else {
        $sDay = $lessonDay["SDAY"];
    }
    if ($lessonDay["EDAY_FLG"] == "1") {
        $eDay = $lessonDay["EDAY"];
    } else {
        $eDay = $lessonDay["DAY_MAX"];
    }

    return array("SDAY" => $sDay, "EDAY" => $eDay);
}

//ボタン作成
function makeButton(&$objForm, &$arg, $schoolkind) {
    //コピーボタン
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左からコピー", $extra);

    //デフォルト
    $extra = "onclick=\"return btn_submit('defAll');\"";
    $arg["button"]["DEF_MAX_BT"] = knjCreateBtn($objForm, "DEF_MAX_BT", "行事から\n日数計算(学期の最終月)", $extra);

    //デフォルト
    $extra = "onclick=\"return btn_submit('defMonth');\"";
    $arg["button"]["DEF_MONTH_BT"] = knjCreateBtn($objForm, "DEF_MONTH_BT", "行事から\n日数計算(月毎)", $extra);

    //クラス別設定ボタン
    $extra = "onClick=\" wopen('".REQUESTROOT."/Z/KNJZ174/knjz174index.php?&SEND_PRGID=KNJZ023&SEND_AUTH=".AUTHORITY."&SEND_SCHOOLKIND=".$schoolkind."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_class"] = knjCreateBtn($objForm, "btn_check1", "クラス別設定", $extra);

    //更新ボタン
    $arg["button"]["btn_updte"] = knjCreateBtn($objForm, "btn_updte", "保 存", "onclick=\"return btn_submit('update');\"");

    //取消ボタンを作成する
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", "onclick=\"return btn_submit('reset');\"");

    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"return closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm) {
        knjCreateHidden($objForm, "cmd");
}
?>
