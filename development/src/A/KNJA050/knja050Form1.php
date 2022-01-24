<?php

require_once('for_php7.php');

class knja050Form1
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("main", "POST", "knja050index.php", "", "main");
        //更新する内容があった場合に日付を入力させる。
        if ($model->cmd == "execute") {
//                $arg["reload"] = "loadwindow(\"" .REQUESTROOT ."/A/KNJA050/knja050index.php?cmd=subForm1&WHICH_WAY={$model->field["WHICH_WAY"]}&GRD_NO={$model->field["GRD_NO"]}&GRADE={$model->grade}&MODE={$model->mode}&syoribi={$model->syoribi}\",0,0,600,350)";
        }

        $db = Query::dbCheckOut();

        //台帳用校種
        if ($model->Properties["useKnja050_select_schoolKind"] == "1") {
            $arg["useKnja050_select_schoolKind"] = "1";
        }
        $query = knja050Query::getSchoolKinds($model);
        $extra = "onchange=\"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->school_kind, "SCHOOL_KIND", $extra, 1, "BLANK");

        //処理学年コンボ
        $opt_grade = array();
        $opt_grade_name1 = array();
        $result = $db->query(knja050Query::getGrade($model, $model->next_year));
        $model->gradeInfo = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_grade[] = array("label" => "新 " .$row["GRADE_NAME1"],  "value" => $row["GRADE"]);
            $opt_grade_name1[$row["GRADE"]] = "新 " .$row["GRADE_NAME1"];
            $model->gradeInfo[$row["GRADE"]]["SCHOOL_KIND"] = $row["SCHOOL_KIND"];
        }
        $opt_grade[] = array("label" => "卒業生",  "value" => "99");
        if (!strlen($model->grade)) {
            $model->grade = $opt_grade[0]["value"];
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "GRADE",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('change_grade')\";",
                            "value"       => $model->grade,
                            "options"     => $opt_grade));
        $arg["data"]["GRADE"] = $objForm->ge("GRADE");

        //ラジオボタン初期状態
        $remember =& $model->field;
        if ($remember["WHICH_WAY"] !=2 && $remember["WHICH_WAY"] !=1) {
            $remember["WHICH_WAY"] = 1;
        }

        $row = array();
        $query = knja050Query::getEntranceDate($model->next_year);
        $result = $db->query($query);
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        //カレンダーで選んだ値を更新後に表示させる。
        if ($model->syoribi !="") {
//                $row["ENTRANCE_DATE"] = $model->syoribi;
        }

        $result->free();

        $arg["data"]["THIS_YEAR"]       = CTRL_YEAR;                //現在年度
        $arg["data"]["THIS_SEMESTER"]   = CTRL_SEMESTERNAME;        //処理学期
        $arg["data"]["NEXT_YEAR"]       = $model->next_year;        //設定年度

        //新入生入学日付
        if ($model->grade == '99') {
            $arg["data"]["SYORITITLE"] = "卒業生卒業日付";
            $schoolRow = $db->getRow(knja050Query::getEntranceDate(CTRL_YEAR), DB_FETCHMODE_ASSOC);
            if (!strlen($model->syoribi) || $model->change_grade == "on") {
                $model->syoribi = str_replace("-", "/", $schoolRow["GRADUATE_DATE"]);
            }
        } else {
            $arg["data"]["SYORITITLE"] = "新入生入学日付";
            if (!strlen($model->syoribi) || $model->change_grade == "on") {
                $model->syoribi = str_replace("-", "/", $row["ENTRANCE_DATE"]);
            }
        }
        $arg["data"]["syoribi"] = View::popUpCalendar($objForm, "syoribi", $model->syoribi);

        //新入生卒業日付
        if (in_array($model->grade, $model->sotugyouSinkyu)) {
            $arg["data"]["GRD_SYORITITLE"] = "前課程卒業日付";
            $schoolRow = $db->getRow(knja050Query::getEntranceDate(CTRL_YEAR), DB_FETCHMODE_ASSOC);
            $model->grd_syoribi = $model->grd_syoribi ? $model->grd_syoribi : str_replace("-", "/", $schoolRow["GRADUATE_DATE"]);
            $arg["data"]["grd_syoribi"] = View::popUpCalendar($objForm, "grd_syoribi", str_replace("-", "/", $model->grd_syoribi));
        }

        //卒業生台帳番号処理モード選択コンボ
        $opt[] = array("label" => "全卒業生採番",       "value" => 1);
        $opt[] = array("label" => "未採番者のみ採番",   "value" => 2);

        //初期設定
        if (!strlen($model->mode)) {
            $model->mode = 1;
        }

        $objForm->ae(array("type"        => "select",
                            "name"        => "MODE",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('main')\";",
                            "value"       => $model->mode,
                            "options"     => $opt));
        $arg["data"]["MODE"] = $objForm->ge("MODE");


        //台帳番号(grd_no)に文字が入っているか調べる
        $case_grd_no = $db->getOne(knja050Query::getCaseGrdno(CTRL_YEAR, $model->mode, $model));
        //台帳番号(grd_no)の最大値
        $max_grd_no = $db->getOne(knja050Query::getMaxGrdno(CTRL_YEAR, $model->mode, $model));

        $grd_no = ($case_grd_no > 0)? 1 : $max_grd_no+1;

        //台帳番号選択ラジオボタン
        $opt = array(1, 2);
        $extra = array("id=\"WHICH_WAY1\"", "id=\"WHICH_WAY2\"");
        $radioArray = knjCreateRadio($objForm, "WHICH_WAY", $remember["WHICH_WAY"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //卒業生台帳番号採番のソート順プロパティよりソート順名をセット
        $grdNoDaichouNoOrderName = "";
        if ($model->Properties["grdNo_DaichouNoOrder"] != "") {
            $aryGrdNoDaichouNoOrder = explode("-", $model->Properties["grdNo_DaichouNoOrder"]);
            $sep = "";
            foreach ($aryGrdNoDaichouNoOrder as $val) {
                if (isset($model->setGrdNoDaichouNoOrder[$val])) {
                    $grdNoDaichouNoOrderName .= $sep.$model->setGrdNoDaichouNoOrder[$val]["name"];
                    $sep = "＋";
                }
            }
        }

        //台帳番号取得順
        if ($model->schoolName != 'komazawa') {
            $arg["SORT_PATTERN1"] = '1';

            //卒業生台帳番号採番のソート順プロパティよりソート順名がセットされている場合
            if ($grdNoDaichouNoOrderName != "") {
                $arg["SORT_PATTERN1_1"] = '1';
                $defaultVal = "4";
                $checked = ($model->field["WHICH_WAY_DIV"] == "" || $model->field["WHICH_WAY_DIV"] == $defaultVal) ? " checked" : "";
                $arg["data"]["WHICH_WAY_DIV4"]  = "<input type=\"radio\" name=\"WHICH_WAY_DIV\" value=\"".$defaultVal."\" id=\"WHICH_WAY_DIV4\"".$checked.">";
                $arg["data"]["WHICH_WAY_DIV4"] .= "<label for=\"WHICH_WAY_DIV4\"> ".$grdNoDaichouNoOrderName."順</label>";
            } else {
                $defaultVal = "1";
            }

            $model->field["WHICH_WAY_DIV"] = ($model->field["WHICH_WAY_DIV"] == "") ? $defaultVal : $model->field["WHICH_WAY_DIV"];
            $opt = array(1, 2);
            $extra = array("id=\"WHICH_WAY_DIV1\"", "id=\"WHICH_WAY_DIV2\"");
            $radioArray = knjCreateRadio($objForm, "WHICH_WAY_DIV", $model->field["WHICH_WAY_DIV"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        } else {
            //駒沢仕様
            $arg["SORT_PATTERN2"] = '1';
            knjCreateHidden($objForm, "WHICH_WAY_DIV", "3");
        }


        //台帳番号連番開始番号テキストボックス
        $objForm->ae(array("type"        => "text",
                            "name"        => "GRD_NO",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $grd_no ));
        $arg["data"]["GRD_NO"] = $objForm->ge("GRD_NO");

        //------------------------- 各学年 -------------------------
        $result = $db->query(knja050Query::readQuery($model, $model->next_year, CTRL_YEAR));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["GRADE_NAME1"] = isset($opt_grade_name1[sprintf("%02d", $row["GRADEI"])]) ? $opt_grade_name1[sprintf("%02d", $row["GRADEI"])] : "";
            if ($row["STDCNT"] == '') {
                $row["STDCNT"]=0;
            }
            if ($row["HRCNT"] == '') {
                $row["HRCNT"]=0;
            }
            if ($row["ATCNT"] == '') {
                $row["ATCNT"]=0;
            }
            if ($row["CSCNT"] == '') {
                $row["CSCNT"]=0;
            }

            $arg["dataaa"][] = $row;
        }

        //------------------------- 卒業生 -------------------------
        $model->grd_student = $db->getOne(knja050Query::getGrdStudent($model, CTRL_YEAR));
        $arg["dataa"]["GRD"] = $model->grd_student;

        //起動条件処理
        if ($model->cmd == "") {
            //セキュリティーチェック
            if ($model->sec_competence != DEF_UPDATABLE) {
                $arg["close"] = "close_window();";
            }
        }

        $row_Next_semester   = $db->getOne(knja050Query::getNextSemester($model->next_year));
        $model->Freshman_dat = $db->getOne(knja050Query::getFreshmanDat($model));
        $row_Class_Formation = $db->getOne(knja050Query::getExClassFormation($model));
        $repet_schregno      = $db->getOne(knja050Query::schregnoExists($model));
        $still_grd_no        = $db->getOne(knja050Query::existsStillNo($model));

        //Max学期チェック
        if ($model->this_semester != $model->max_semester) {
            $max_semes_flg = $model->max_semestername;
        }
        //次年度の学期が設定されているかチェック
        if ($row_Next_semester == 0) {
            $next_semester_flg = true;
        }
        //新入生移行データ存在チェック
        if ($model->Freshman_dat == 0) {
            $freshman_dat_flg = true;
        } else {
            //新入生入学日付チェック
            $result = $db->query(knja050Query::getYearRange($model));
            $rows = $result->fetchRow(DB_FETCHMODE_ASSOC);
//                $arg["SDATE"] = str_replace("-","",$rows["SDATE"]);
//                $arg["EDATE"] = str_replace("-","",$rows["EDATE"]);
            $arg["SDATE"] = $model->next_year ."0401";
            $arg["EDATE"] = ($model->next_year + 1) ."0331";
            $freshman_date_flg = true;
        }
        //新入生移行データ、学籍番号重複チェック
        if ($repet_schregno > 0) {
            $repet_schregno_flg = true;
        }
        //CLASS_FORMATION_DAT未設定チェック
        if ($row_Class_Formation >= 1) {
            $row_class_formation_flg = true;
        }
        //卒業生存在チェック
        if ($model->grd_student == 0) {
            $grd_student_flg = true;
        }
        //卒業生台帳番号未採番者存在チェック
        if ($still_grd_no != 0) {
            $still_grd_no_flg = true;
        }

        //台帳番号だけ更新
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_up",
                            "value"       => "台帳番号採番処理",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));

        //実行
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_ok",
                            "value"       => "年度確定処理",
                            "extrahtml"   => "onclick=\"return btn_submit('subExecute');\"" ));

        //終了
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終  了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"] = array("BTN_UP"    => $objForm->ge("btn_up"),
                                "BTN_OK"    => $objForm->ge("btn_ok"),
                                "BTN_CLEAR" => $objForm->ge("btn_end") );

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "E_APPDATE"));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "MAX_SEMES_CL",
                            "value"     => $max_semes_flg));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "NEXT_SEMESTER",
                            "value"     => $next_semester_flg));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "FRESHMAN_DAT",
                            "value"     => $freshman_dat_flg));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "FRESHMAN_DATE",
                            "value"     => $freshman_date_flg));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "REPET_SCHREGNO",
                            "value"     => $repet_schregno_flg));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "CLASS_FORMATION",
                            "value"     => $row_class_formation_flg));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "GRD_STUDENT",
                            "value"     => $grd_student_flg));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "STILL_GRD_NO",
                            "value"     => $still_grd_no_flg));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "CHECK_GRADE",
                            "value"     => $model->grade));
        //処理学年コンボを変更した場合のフラグ
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "change_grade"));

        //校種使用
        knjCreateHidden($objForm, "useKnja050_select_schoolKind", $model->Properties["useKnja050_select_schoolKind"]);

        $query = knja050Query::getA023();
        $result = $db->query($query);
        $gnameArray = array("P" => 0, "J" => 0, "H" => 0);
        $setSinnyuG = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $gnameArray[$row["NAME1"]] = 1;
            knjCreateHidden($objForm, "CHECK_GRADE_".$row["NAME1"], $row["NAME2"]);
            $setSinnyuG = $row["NAME2"];
        }

        foreach ($gnameArray as $key => $val) {
            if ($val == 0) {
                knjCreateHidden($objForm, "CHECK_GRADE_".$key, $setSinnyuG);
            }
        }

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja050Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
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
