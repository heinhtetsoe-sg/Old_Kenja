<?php

require_once('for_php7.php');


class knjd620vForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"]   = $objForm->get_start("knjd620vForm1", "POST", "knjd620vindex.php", "", "knjd620vForm1");

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => CTRL_YEAR));
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $ga = CTRL_SEMESTER;
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"      => CTRL_SEMESTER));
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        $db = Query::dbCheckOut();

        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $arg["use_school_detail_gcm_dat"] = 1;
            $extra = "aria-label=\"課程学科\" id=\"COURSE_MAJOR\" onChange=\"current_cursor('COURSE_MAJOR');btn_submit('knjd620v')\";";
            $query = knjd620vQuery::getCourseMajor($model);
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1, "");
        }

        $z010 = $db->getOne(knjd620vQuery::getZ010());
        $query = knjd620vQuery::getSubclassSaki($model);
        $result = $db->query($query);
        $subclasscdSaki = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclasscdSaki[] = $row["SUBCLASSCD"];
        }
        $result->free();

        $opt_subcd = array();
        $query = knjd620vQuery::selectSubclassQuery($model);
        $result = $db->query($query);
        $selectSchoolKind = "";
        $schoolKind = array();
        $isJ = false;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subclasscd = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
                if (!in_array($row["SCHOOL_KIND"], $schoolKind)) {
                    $schoolKind[] = $row["SCHOOL_KIND"];
                }
                if ($subclasscd == $model->field["SUBCLASSCD"]) {
                    $selectSchoolKind = $row["SCHOOL_KIND"];
                }
                $saki = (in_array($subclasscd, $subclasscdSaki)) ? "●" : "　";
                $opt_subcd[] = array('label' => $saki . $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]."：".$row["SUBCLASSNAME"],
                                     'value' => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]);
            }
            $isJ = "J" == $selectSchoolKind || get_count($schoolKind) == 1 && in_array("J", $schoolKind);
            if (!($z010 == 'fukuiken' && $isJ)) {
                $arg["showPrintKekka0"] = "1";
            }
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subclasscd = $row["SUBCLASSCD"];
                $saki = (in_array($subclasscd, $subclasscdSaki)) ? "●" : "　";
                $opt_subcd[] = array('label' => $saki . $row["SUBCLASSCD"] ."：" .$row["SUBCLASSNAME"],
                                     'value' => $row["SUBCLASSCD"]);
            }
        }
        $result->free();

        if (!isset($model->field["SUBCLASSCD"])) {
            $model->field["SUBCLASSCD"] = $opt_subcd[0]["value"];
        }

        $objForm->ae(array("type"       => "select",
                            "name"       => "SUBCLASSCD",
                            "size"       => "1",
                            "value"      => $model->field["SUBCLASSCD"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjd620v'),AllClearList();\"",
                            "options"    => $opt_subcd));
        $arg["data"]["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");

        $opt_chrcd = array();
        $query = knjd620vQuery::selectChairQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chrcd[] = array('label' => $row["CHAIRCD"] ."：" .$row["CHAIRNAME"],
                                 'value' => $row["CHAIRCD"]);
        }
        $result->free();

        //出欠集計日付作成
        if ($model->field["SDATE"] == "") {
            $model->field["SDATE"] = $db->getOne(knjd620vQuery::getSemesterSdate());
        }
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", str_replace("-", "/", $model->field["SDATE"]));

        //出欠集計日付作成
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        $objForm->ae(array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => $opt_chrcd));
        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");


        $objForm->ae(array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('right')\"",
                            "size"       => "20",
                            "options"    => array()));
        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


        $objForm->ae(array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ));
        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        $objForm->ae(array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ));
        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        $objForm->ae(array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ));
        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        $objForm->ae(array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ));
        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //年度日付範囲
        $query = knjd620vQuery::getSemester9();
        $result = $db->query($query);
        $yearSdate = CTRL_DATE;
        $yearEdate = CTRL_DATE;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $yearSdate = $row["SDATE"];
            $yearEdate = $row["EDATE"];
        }
        $result->free();

        //備考考査コード
        $opt_testcd = array();
        $query = knjd620vQuery::selectTestcdQuery($model, $model->field["SUBCLASSCD"], '00');
        $result = $db->query($query);
        $model->field["REMARK_TESTCD"] = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_testcd[] = array('label' => $row["TESTCD"] ."：" .$row["TESTNAME"],
                                 'value' => $row["TESTCD"]);
            if ($row["SEMESTER"] == CTRL_SEMESTER && $model->field["REMARK_TESTCD"] == '') {
                $model->field["REMARK_TESTCD"] = $row["TESTCD"];
            }
        }
        $result->free();
        if (get_count($opt_testcd) == 0) {
            $query = knjd620vQuery::selectTestcdQuery($model, $model->field["SUBCLASSCD"]);
            $result = $db->query($query);
            $model->field["REMARK_TESTCD"] = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_testcd[] = array('label' => $row["TESTCD"] ."：" .$row["TESTNAME"],
                                     'value' => $row["TESTCD"]);
                if ($row["SEMESTER"] == CTRL_SEMESTER && $model->field["REMARK_TESTCD"] == '') {
                    $model->field["REMARK_TESTCD"] = $row["TESTCD"];
                }
            }
        }
        $objForm->ae(array("type"       => "select",
                            "name"       => "REMARK_TESTCD",
                            "size"       => "1",
                            "value"      => $model->field["REMARK_TESTCD"],
                            "extrahtml"  => "",
                            "options"    => $opt_testcd));
        $arg["data"]["REMARK_TESTCD"] = $objForm->ge("REMARK_TESTCD");

        //備考考査コンボ表示　考査種別マスタの備考入力「あり」
        if ($model->Properties["useRemarkFlg"] == '1') {
            $optRmkFlgTestcd = array();
            $query = knjd620vQuery::selectTestcdQuery($model, $model->field["SUBCLASSCD"], '00', "REMARK_FLG");
            $result = $db->query($query);
            $model->field["REMARK_FLG_TESTCD"] = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $optRmkFlgTestcd[] = array('label' => $row["TESTCD"] ."：" .$row["TESTNAME"],
                                     'value' => $row["TESTCD"]);
                if ($row["SEMESTER"] == CTRL_SEMESTER && $model->field["REMARK_FLG_TESTCD"] == '') {
                    $model->field["REMARK_FLG_TESTCD"] = $row["TESTCD"];
                }
            }
            $result->free();
            if (get_count($optRmkFlgTestcd) == 0) {
                $query = knjd620vQuery::selectTestcdQuery($model, $model->field["SUBCLASSCD"], "", "REMARK_FLG");
                $result = $db->query($query);
                $model->field["REMARK_FLG_TESTCD"] = "";
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $optRmkFlgTestcd[] = array('label' => $row["TESTCD"] ."：" .$row["TESTNAME"],
                                         'value' => $row["TESTCD"]);
                    if ($row["SEMESTER"] == CTRL_SEMESTER && $model->field["REMARK_FLG_TESTCD"] == '') {
                        $model->field["REMARK_FLG_TESTCD"] = $row["TESTCD"];
                    }
                }
            }
            $arg["data"]["REMARK_FLG_TESTCD"] = knjCreateCombo($objForm, "REMARK_FLG_TESTCD", $model->field["REMARK_FLG_TESTCD"], $optRmkFlgTestcd, "", 1);
            $arg["useRemarkFlg"] = 1;
        }

        //出力選択ラジオ
        $radioValue = array(1, 2);
        if ($model->field["PRINT_DIV"] == '') {
            $model->field["PRINT_DIV"] = '1';
        }
        $radioArray = knjCreateRadio($objForm, "PRINT_DIV", $model->field["PRINT_DIV"], $extra, $radioValue, get_count($radioValue));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /********************/
        /* チェックボックス */
        /********************/
        //欠課時数を0表記する
        $extra  = ($model->field["PRINT_KEKKA0"] == "1") ? "checked='checked' " : "";
        $extra .= " id='PRINT_KEKKA0'";
        $arg["data"]["PRINT_KEKKA0"] = knjCreateCheckBox($objForm, "PRINT_KEKKA0", "1", $extra);

        //教科正担任をすべて出力（最大4名まで）
        $extra  = ($model->field["PRINT_TANNIN"] == "1") ? "checked='checked' " : "";
        $extra .= " id='PRINT_TANNIN'";
        $arg["data"]["PRINT_TANNIN"] = knjCreateCheckBox($objForm, "PRINT_TANNIN", "1", $extra);

        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ));
        $arg["button"]["btn_print"] = $objForm->ge("btn_print");


        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        knjCreateHidden($objForm, "STAFF", STAFFCD);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD620V");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COUNTFLG", $model->testTable);

        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);

        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);
        knjCreateHidden($objForm, "YEAR_SDATE", str_replace('-', '/', $yearSdate));
        knjCreateHidden($objForm, "YEAR_EDATE", str_replace('-', '/', $yearEdate));
        knjCreateHidden($objForm, "PRINT_JUGYO_JISU", "1"); //帳票に授業時数欄を追加
        knjCreateHidden($objForm, "useRemarkFlg", $model->Properties["useRemarkFlg"]);
        knjCreateHidden($objForm, "useMikomiFlg", $model->Properties["useMikomiFlg"]);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "useChairStaffOrder", $model->Properties["useChairStaffOrder"]);
        knjCreateHidden($objForm, "useFormNameKNJD620V", $model->Properties["useFormNameKNJD620V"]);
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        knjCreateHidden($objForm, "PRINT_SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "PRINT_SCHOOLKIND", SCHOOLKIND);

        $arg["finish"]  = $objForm->get_finish();

        Query::dbCheckIn($db);

        View::toHTML($model, "knjd620vForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "blank") {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
