<?php

require_once('for_php7.php');

class knjb0031Form1
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjb0031index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        if ($model->keikokutenFlag) {
            $arg["KEIKOKUTEN_FLAG"] = '1';
            $arg["divWidth"] = 2935;
        } else {
            $arg["KEIKOKUTEN_NON_FLAG"] = '1';
            $arg["divWidth"] = 2485;
        }
        if ($model->Properties["chairRetsuMeisho_Hyouji"]) {
            $arg["chairRetsuMeisho_Hyouji"] = '1';
            $arg["divWidth"] += 350;
        }

        //最終学期かを判定
        $isLastSemester = (CTRL_SEMESTER == $model->control["学期数"]) ? true : false;

        //対象年度
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjb0031Query::getSemesterMst($isLastSemester);
        makeCmb($objForm, $arg, $db, $query, $model->term, "term", $extra, 1, "");

        //コース
        $courseList = makeCmbCourse($objForm, $arg, $db, $model);

        //単位マスタの件数
        $creditCnt = $db->getOne(knjb0031Query::checkCreditMst($model, substr($model->term, 0, 4)));

        //科目コード
        $extra = "onChange=\"btn_submit('change');\"";
        $query = knjb0031Query::getSubclassMst($model, substr($model->term, 0, 4), $creditCnt);

        $subclassList = makeCmbSubclass($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1, "BLANK");
        //科目略称・・・講座名称(自動)：受講クラス選択画面でも使用
        $subclassRow = array();
        $subclassRow = $db->getRow(knjb0031Query::getSubclassabbv($model), DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "SUBCLASSNAME", $subclassRow["SUBCLASSNAME"]);
        knjCreateHidden($objForm, "SUBCLASSABBV", $subclassRow["SUBCLASSABBV"]);

        //群ラジオ 1:教科 2:群
        $opt = array(1, 2);
        $model->group = ($model->group == "") ? "1" : $model->group;
        $extra = "onclick=\"btn_submit('change');\" ";
        $extra = array($extra."id=\"GROUP1\"", $extra."id=\"GROUP2\"");
        $radioArray = knjCreateRadio($objForm, "GROUP", $model->group, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //追加件数初期値取得
        $kensuu_default = ($model->subclasscd == "") ? "" : $db->getOne(knjb0031Query::getKensuuDefault($model));

        //追加件数
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value);\"";
        $kensuu = ($model->kensuu == "") ? $kensuu_default : $model->kensuu;
        $arg["KENSUU"] = knjCreateTextBox($objForm, $kensuu, "KENSUU", 3, 2, $extra);

        $chairList = array();

        if (!isset($model->warning) && $model->subclasscd != "") {
            //講座一覧
            $query = knjb0031Query::getChairList($model, substr($model->term, 0, 4), substr($model->term, 5));
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $chairList[] = $row;
            }
            $result->free();

            //確定クリック
            if ($model->cmd == "search") {
                if ($model->z010name1 == "hirogaku") {
                    //広島国際
                    //1:教科(学年)、群(学年+5)
                    //2:学科連番
                    //3:コース連番
                    //45:科目連番
                    $chaircd5 = getHiro($db, $model, $courseList, $subclassList);
                } else {
                    //科目コード(頭5桁)
                    $chaircd5 = substr($model->subclasscd, -6, 5);
                }
                //67:講座連番
                //連番(初期値)
                $renban67 = 0;
                //MAX講座コード
                $chaircdMax = $db->getOne(knjb0031Query::getMaxChaircd($model, substr($model->term, 0, 4), substr($model->term, 5), $chaircd5));
                if (strlen($chaircdMax)) {
                    $renban67 = (int) substr($chaircdMax, -2);
                }
                for ($k = 0; $k < $model->kensuu; $k++) {
                    $Row = array();

                    //連番(1～99)
                    if ($renban67 < 99) {
                        $renban67++;
                    }
                    //講座コード(自動)：科目コード(頭5桁)+連番(MAX+1)
                    $Row["CHAIRCD"] = $chaircd5 . sprintf("%02d", $renban67);
                    //講座名称(自動)：科目略称(+年組略称+年組略称)・・・年組略称は受講クラス選択画面にて
                    $Row["CHAIRNAME"] = $subclassRow["SUBCLASSNAME"];
                    $Row["CHAIRABBV"] = $subclassRow["SUBCLASSABBV"];

                    $chairList[] = $Row;
                }
            }
        }

        //更新・削除時のチェックでエラーの場合、画面情報をセット
        if (isset($model->warning)) {
            //講座一覧 + 追加件数
            for ($counter = 0; $counter < $model->data_cnt; $counter++) {
                $Row = array();
                foreach ($model->fields as $key => $val) {
                    $Row[$key] = $val[$counter];
                }
                $chairList[] = $Row;
            }
        }

        //初期化
        //$model->data = array();

        //講座一覧の表示件数
        knjCreateHidden($objForm, "DATA_CNT", get_count($chairList));

        //講座一覧を表示
        foreach ($chairList as $counter => $Row) {
            //KEY
            //$model->data["KEY"][] = $Row["CHAIRCD"];

            //レコード取得（その他）
            if (!isset($model->warning)) {
                //使用施設
                $opt_lab = $opt_val = array();
                $result = $db->query(knjb0031Query::getFac($model->term, $Row["CHAIRCD"]));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt_lab[] = $row["FACILITYABBV"];
                    $opt_val[] = $row["FACCD"];
                }
                $Row_Fac["FACILITYABBV"] = implode(",", $opt_lab);
                $Row_Fac["FACCD"] = implode(",", $opt_val);
                //教科書
                $opt_lab = $opt_val = array();
                $result = $db->query(knjb0031Query::getTextbook($model->term, $Row["CHAIRCD"]));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt_lab[] = $row["TEXTBOOKABBV"];
                    $opt_val[] = $row["TEXTBOOKCD"];
                }
                $Row_Textbook["TEXTBOOKABBV"] = implode(",", $opt_lab);
                $Row_Textbook["TEXTBOOKCD"] = implode(",", $opt_val);
                //科目担任
                $opt_lab = $opt_lab1 = $opt_val = $opt_val2 = $opt_val3 = array();
                $result = $db->query(knjb0031Query::getStaff($model->term, $Row["CHAIRCD"]));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($row["CHARGEDIV"]=="1") {
                        $opt_lab1[]  = $row["STAFFNAME_SHOW"]; //正
                    } else {
                        $opt_lab[]  = $row["STAFFNAME_SHOW"]; //副
                    }
                    $opt_val[]  = $row["STAFFCD"];
                    $opt_val2[] = $row["CHARGEDIV"];
                    $opt_val3[] = $row["STAFFCD"]."-".$row["CHARGEDIV"]; //ダイアログで正副を判断するために使用
                }
                $Row_Staff["STAFFNAME_SHOW1"] = implode(",", $opt_lab1);
                $Row_Staff["STAFFNAME_SHOW"]  = implode(",", $opt_lab);
                $Row_Staff["STAFFCD"]         = implode(",", $opt_val);
                $Row_Staff["CHARGEDIV"]       = implode(",", $opt_val2);
                $Row_Staff["STF_CHARGE"]      = implode(",", $opt_val3);
                //受講クラス
                $opt_lab = $opt_val = array();
                $result = $db->query(knjb0031Query::getGradeClass($model, $model->term, $Row["CHAIRCD"], $Row["GROUPCD"]));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt_lab[] = $row["HR_NAMEABBV"];
                    $opt_val[] = $row["GRADE_CLASS"];
                }
                $Row_GradeClass["HR_NAMEABBV"]  = implode(",", $opt_lab);
                $Row_GradeClass["GRADE_CLASS"]  = implode(",", $opt_val);
            } else {
                $Row_Fac["FACILITYABBV"]        = $model->fields["FACILITYABBV"][$counter];
                $Row_Fac["FACCD"]               = $model->fields["FACCD"][$counter];
                $Row_Textbook["TEXTBOOKABBV"]   = $model->fields["TEXTBOOKABBV"][$counter];
                $Row_Textbook["TEXTBOOKCD"]     = $model->fields["TEXTBOOKCD"][$counter];
                $Row_Staff["STAFFNAME_SHOW1"]   = $model->fields["STAFFNAME_SHOW1"][$counter];
                $Row_Staff["STAFFNAME_SHOW"]    = $model->fields["STAFFNAME_SHOW"][$counter];
                $Row_Staff["STAFFCD"]           = $model->fields["STAFFCD"][$counter];
                $Row_Staff["CHARGEDIV"]         = $model->fields["CHARGEDIV"][$counter];
                $Row_Staff["STF_CHARGE"]        = $model->fields["STF_CHARGE"][$counter];
                $Row_GradeClass["HR_NAMEABBV"]  = $model->fields["HR_NAMEABBV"][$counter];
                $Row_GradeClass["GRADE_CLASS"]  = $model->fields["GRADE_CLASS"][$counter];
            }
            //授業回数と受講クラス選択ボタンの使用可・不可
            //群ラジオ 1:教科 2:群
            if ($model->group == '2') {
                $read_lesson = "STYLE=\"background-color:darkgray\" readonly";
                $dis_subform1 = "disabled";
            } else {
                $read_lesson = "";
                $dis_subform1 = "";
            }



            //削除
            $extra = ($Row["DEL_FLG"] == "1") ? "checked" : "";
            $setData["DEL_FLG"] = knjCreateCheckBox($objForm, "DEL_FLG"."-".$counter, "1", $extra);

            //講座コード
            $extra = "onblur=\"this.value=toInteger(this.value);\"";
            if (strlen($Row["UPDATED"])) {
                //登録済みレコードの時、講座コードは変更不可とする
                $extra .= " STYLE=\"background-color:darkgray\" "."readonly";
            }
            $setData["CHAIRCD"] = knjCreateTextBox($objForm, $Row["CHAIRCD"], "CHAIRCD"."-".$counter, 8, 7, $extra);

            //講座名称
            $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
            $setData["CHAIRNAME"] = knjCreateTextBox($objForm, $Row["CHAIRNAME"], "CHAIRNAME"."-".$counter, 31, 30, $extra);

            //講座略称
            $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
            $setData["CHAIRABBV"] = knjCreateTextBox($objForm, $Row["CHAIRABBV"], "CHAIRABBV"."-".$counter, 16, 15, $extra);

            //列名称
            $retuQuery = knjb0031Query::getRetumei($model);
            $extra = "";
            $setData["SEQ004_REMARK1"] = makeCmbReturn($objForm, $arg, $db, $retuQuery, $Row["SEQ004_REMARK1"], "SEQ004_REMARK1"."-".$counter, $extra, 1, "BLANK");

            //スモールクラス名称
            $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
            $setData["SEQ004_REMARK2"] = knjCreateTextBox($objForm, $Row["SEQ004_REMARK2"], "SEQ004_REMARK2"."-".$counter, 16, 15, $extra);

            //習熟度クラス名称
            $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
            $setData["SEQ004_REMARK3"] = knjCreateTextBox($objForm, $Row["SEQ004_REMARK3"], "SEQ004_REMARK3"."-".$counter, 16, 15, $extra);

            //履修期間区分
            $extra = "";
            $query = knjb0031Query::getTakesemes(substr($model->term, 0, 4));
            $setData["TAKESEMES"] = makeCmbReturn($objForm, $arg, $db, $query, $Row["TAKESEMES"], "TAKESEMES"."-".$counter, $extra, 1, "");

            //週授業回数
            $extra = "onblur=\"this.value=toInteger(this.value);\"".$read_lesson;
            $setData["LESSONCNT"] = knjCreateTextBox($objForm, $Row["LESSONCNT"], "LESSONCNT"."-".$counter, 3, 2, $extra);

            //連続枠数
            $extra = "onblur=\"this.value=toInteger(this.value);\"".$read_lesson;
            $setData["FRAMECNT"] = knjCreateTextBox($objForm, $Row["FRAMECNT"], "FRAMECNT"."-".$counter, 3, 2, $extra);

            //使用施設
            $extra = "STYLE=\"WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\""."readonly";
            $setData["FACILITYABBV"] = knjCreateTextBox($objForm, $Row_Fac["FACILITYABBV"], "FACILITYABBV"."-".$counter, 31, 30, $extra);
            knjCreateHidden($objForm, "FACCD"."-".$counter, $Row_Fac["FACCD"]);

            //教科書
            $extra = "STYLE=\"WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\""."readonly";
            $setData["TEXTBOOKABBV"] = knjCreateTextBox($objForm, $Row_Textbook["TEXTBOOKABBV"], "TEXTBOOKABBV"."-".$counter, 31, 30, $extra);
            knjCreateHidden($objForm, "TEXTBOOKCD"."-".$counter, $Row_Textbook["TEXTBOOKCD"]);

            //科目担任（正）
            //科目担任（副）
            $extra = "STYLE=\"WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\""."readonly";
            $setData["STAFFNAME_SHOW1"] = knjCreateTextBox($objForm, $Row_Staff["STAFFNAME_SHOW1"], "STAFFNAME_SHOW1"."-".$counter, 31, 30, $extra);
            $setData["STAFFNAME_SHOW"] = knjCreateTextBox($objForm, $Row_Staff["STAFFNAME_SHOW"], "STAFFNAME_SHOW"."-".$counter, 31, 30, $extra);
            knjCreateHidden($objForm, "STAFFCD"."-".$counter, $Row_Staff["STAFFCD"]);
            knjCreateHidden($objForm, "CHARGEDIV"."-".$counter, $Row_Staff["CHARGEDIV"]);
            knjCreateHidden($objForm, "STF_CHARGE"."-".$counter, $Row_Staff["STF_CHARGE"]);

            //受講クラス
            $extra = "STYLE=\"WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\""."readonly";
            $setData["HR_NAMEABBV"] = knjCreateTextBox($objForm, $Row_GradeClass["HR_NAMEABBV"], "HR_NAMEABBV"."-".$counter, 31, 30, $extra);
            knjCreateHidden($objForm, "GRADE_CLASS"."-".$counter, $Row_GradeClass["GRADE_CLASS"]);

            //群コード
            $extra = "";
            $query = knjb0031Query::getGroup($model, substr($model->term, 0, 4));
            $setData["GROUPCD"] = makeCmbReturn($objForm, $arg, $db, $query, $Row["GROUPCD"], "GROUPCD"."-".$counter, $extra, 1, "");

            //集計フラグ 1:集計する 0:集計しない
            $extra = ($Row["COUNTFLG"] == "1") ? "checked" : "";
            $setData["COUNTFLG"] = knjCreateCheckBox($objForm, "COUNTFLG"."-".$counter, "1", $extra);

            //受講人数
            $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
            $setData["STD_CNT"] = knjCreateTextBox($objForm, $Row["STD_CNT"], "STD_CNT"."-".$counter, 3, 3, $extra);

            //警告点(素点)
            $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
            $setData["KEIKOKUTEN_SOTEN"] = knjCreateTextBox($objForm, $Row["KEIKOKUTEN_SOTEN"], "KEIKOKUTEN_SOTEN"."-".$counter, 5, 5, $extra);

            //警告点(評価)
            $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
            $setData["KEIKOKUTEN_HYOUKA"] = knjCreateTextBox($objForm, $Row["KEIKOKUTEN_HYOUKA"], "KEIKOKUTEN_HYOUKA"."-".$counter, 5, 5, $extra);

            //警告点(評定)
            $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
            $setData["KEIKOKUTEN_HYOUTEI"] = knjCreateTextBox($objForm, $Row["KEIKOKUTEN_HYOUTEI"], "KEIKOKUTEN_HYOUTEI"."-".$counter, 5, 5, $extra);

            //受講クラス選択ボタンを作成する
            $extra = "onclick=\"return btn_submit_subform('subform1', '{$counter}');\" style=\"width:110px\"".$dis_subform1;
            $setData["btn_subform1"] = knjCreateBtn($objForm, "btn_subform1", "受講クラス選択", $extra);

            //科目担任選択ボタンを作成する
            $extra = "onclick=\"return btn_submit_subform('subform2', '{$counter}');\" style=\"width:110px\"";
            $setData["btn_subform2"] = knjCreateBtn($objForm, "btn_subform2", "科目担任選択", $extra);

            //使用施設選択ボタンを作成する
            $extra = "onclick=\"return btn_submit_subform('subform3', '{$counter}');\" style=\"width:110px\"";
            $setData["btn_subform3"] = knjCreateBtn($objForm, "btn_subform3", "使用施設選択", $extra);

            //教科書選択ボタンを作成する
            $extra = "onclick=\"return btn_submit_subform('subform4', '{$counter}');\" style=\"width:110px\"";
            $setData["btn_subform4"] = knjCreateBtn($objForm, "btn_subform4", "教科書選択", $extra);

            $arg["data"][] = $setData;

            knjCreateHidden($objForm, "UPDATED"."-".$counter, $Row["UPDATED"]);
        } //foreach





        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjb0031Form1.html", $arg);
    }
}
//広島国際：講座コード(頭5桁)
function getHiro($db, $model, $courseList, $subclassList)
{
    //コースコンボ
    list($grade, $coursecd, $majorcd, $coursecode) = explode("-", $model->grade_course);
    //1:教科(学年)、群(学年+5)
    $renban1 = 0;
    $renban1 = ($model->group == "2") ? (int) $grade + 5 : (int) $grade;
    //2:学科連番
    $renban2 = 0;
    $cdCnt = 0;
    $query = knjb0031Query::getMajorMst(substr($model->term, 0, 4));
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $cdCnt++;
        $value = $row["COURSECD"]."-".$row["MAJORCD"];
        if ($coursecd == $row["COURSECD"] && $majorcd == $row["MAJORCD"]) {
            $renban2 = $cdCnt;
            break;
        }
    }
    $result->free();
    //3:コース連番
    $renban3 = 0;
    $cdKeep = "";
    $cdCnt = 0;
    $courseArray = array();
    foreach ($courseList as $key => $row) {
        if ($cdKeep !== $row["GRADE"]."-".$row["COURSECD"]."-".$row["MAJORCD"]) {
            $cdKeep = $row["GRADE"]."-".$row["COURSECD"]."-".$row["MAJORCD"];
            $cdCnt = 0;
        }
        $cdCnt++;
        $value = $row["GRADE"]."-".$row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"];
        if ($model->grade_course == $value) {
            $renban3 = $cdCnt;
            break;
        }
    }
    //45:科目連番
    $renban45 = 0;
    $cdCnt = 0;
    foreach ($subclassList as $key => $row) {
        $cdCnt++;
        if ($model->subclasscd == $row["VALUE"]) {
            $renban45 = $cdCnt;
            break;
        }
    }
    $chaircd5 = $renban1 . $renban2 . $renban3 . sprintf("%02d", $renban45);

    return $chaircd5;
}
//コースコンボ作成
function makeCmbCourse(&$objForm, &$arg, $db, &$model)
{
    $year = substr($model->term, 0, 4);
    $semester = substr($model->term, 5);

    //学籍在籍データ件数
    $regdCnt = $db->getOne(knjb0031Query::checkRegdDat($model, $year, $semester));

    //コース配列
    $courseList = array();

    //各名称のMAX値取得
    $max_grade_len = 0;
    $max_major_len = 0;
    $result = $db->query(knjb0031Query::getGradeCouse($model, $regdCnt, $year, $semester));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $courseList[] = $row;
        $max_grade_len = ($max_grade_len < mb_strwidth($row["GRADE_NAME1"])) ? mb_strwidth($row["GRADE_NAME1"]) : $max_grade_len;
        $max_major_len = ($max_major_len < mb_strwidth($row["COURSENAME"].$row["MAJORNAME"])) ? mb_strwidth($row["COURSENAME"].$row["MAJORNAME"]) : $max_major_len;
    }
    $result->free();

    //コースコンボ作成
    $opt = array();
    foreach ($courseList as $key => $row) {
        //学年、課程学科名称の桁数取得
        $grade_len = mb_strwidth($row["GRADE_NAME1"]);
        $major_len = mb_strwidth($row["COURSENAME"].$row["MAJORNAME"]);
        //学年、課程学科名称の空埋め数
        $grade_spcnt = $max_grade_len - $grade_len;
        $major_spcnt = $max_major_len - $major_len;

        $opt[] = array("label" => $row["GRADE_NAME1"].str_repeat("&nbsp;", $grade_spcnt)."&nbsp;".
                                  "(".$row["COURSECD"].$row["MAJORCD"].")&nbsp;".
                                  $row["COURSENAME"].$row["MAJORNAME"].str_repeat("&nbsp;", $major_spcnt)."&nbsp;".
                                  "(".$row["COURSECODE"].")&nbsp;".$row["COURSECODENAME"],
                       "value" => $row["GRADE"]."-".$row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"]);
    }

    if (!strlen($model->grade_course)) {
        $model->grade_course = $opt[0]["value"];
    }

    $extra = "onChange=\"btn_submit('change');\"";
    $arg["GRADE_COURSE"] = knjCreateCombo($objForm, "GRADE_COURSE", $model->grade_course, $opt, $extra, 1);

    list($grade, $coursecd, $majorcd, $courseCode) = preg_split("/-/", $model->grade_course);
    $query = knjb0031Query::getSchoolKind($year, $grade);
    $model->schoolKind = "";
    $model->schoolKind = $db->getOne($query);

    return $courseList;
}
//コンボ作成
function makeCmbSubclass(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                        "value" => "");
    }

    //科目配列
    $subclassList = array();

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $subclassList[] = $row;
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "term") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR. "-" .CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    return $subclassList;
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                        "value" => "");
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

    if ($name == "term") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR. "-" .CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成
function makeCmbReturn(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                        "value" => "");
    }
    /***/
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    /***/
    if ($name == "term") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR. "-" .CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //$btnSize = " style=\"height:35px;width:100px;text-align:center;\"";
    $btnSize = "";
    //確定
    $extra = "onclick=\"return btn_submit('search');\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "確 定", $extra.$btnSize);
    //削除
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$btnSize);
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$btnSize);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$btnSize);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra.$btnSize);
    //HRクラスの自動名簿生成
    $dis_group2 = ($model->group == "2") ? " disabled" : "";
    $extra = "onclick=\"return btn_submit('csv2');\"";
    $arg["button"]["btn_csv2"] = knjCreateBtn($objForm, "btn_csv2", "自動名簿生成", $extra.$dis_group2);
    //一括入力
    $prgid = "KNJB0031";
    $auth = AUTHORITY;
    $param = "?cmd=edit&prgid={$prgid}&auth={$auth}&term={$model->term}&GRADE_COURSE={$model->grade_course}&SUBCLASSCD={$model->subclasscd}&GROUP={$model->group}";
    //受講クラス
    $url = REQUESTROOT."/B/KNJB0031_1/knjb0031_1index.php";
    $extra = "style=\"background:pink\"; onClick=\"openKogamen('{$url}{$param}');\"";
    $arg["button"]["btn_class"] = knjCreateBtn($objForm, "btn_class", "一括入力", $extra.$dis_group2);
    //科目担任
    $url = REQUESTROOT."/B/KNJB0031_2/knjb0031_2index.php";
    $extra = "style=\"background:#ADFF2F\"; onClick=\"openKogamen('{$url}{$param}');\"";
    $arg["button"]["btn_staff"] = knjCreateBtn($objForm, "btn_staff", "一括入力", $extra);
    //使用施設
    $url = REQUESTROOT."/B/KNJB0031_3/knjb0031_3index.php";
    $extra = "style=\"background:#FFFF00\"; onClick=\"openKogamen('{$url}{$param}');\"";
    $arg["button"]["btn_fac"] = knjCreateBtn($objForm, "btn_fac", "一括入力", $extra);
}
//hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
}
