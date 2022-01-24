<?php

require_once('for_php7.php');


class knjb060Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb060Form1", "POST", "knjb060index.php", "", "knjb060Form1");

        //年度コンボ作成
        $db = Query::dbCheckOut();
        $query = knjb060Query::getYear();
        $extra = "onchange=\"return btn_submit('knjb060');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);
        Query::dbCheckIn($db);

        //ラジオボタンを作成//時間割種別（基本時間割/通常時間割）
        $opt=array();
        $opt[0]=1;
        $opt[1]=2;
        for ($i = 1; $i <= 2; $i++) {
            $name = "RADIO".$i;
            $objForm->ae(array("type"       => "radio",
                                "name"       => "RADIO",
                                "value"      => isset($model->field["RADIO"])?$model->field["RADIO"]:"1",
                                "extrahtml"  => "onclick=\"jikanwari(this);\" id=\"$name\"",
                                "multiple"   => $opt));

            $arg["data"][$name] = $objForm->ge("RADIO", $i);
        }

        if ($model->field["RADIO"] == 2) {     //通常時間割選択時
            $dis_jikan = "disabled";                //時間割選択コンボ使用不可
            $dis_date  = "";                        //指定日付テキスト使用可
            $arg["Dis_Date"]  = " dis_date(false); " ;
        } else {                                //基本時間割選択時
            $dis_jikan = "";                        //時間割選択コンボ使用可
            $dis_date  = "disabled";                //指定日付テキスト使用不可
            $arg["Dis_Date"]  = " dis_date(true); " ;
        }


        //時間割選択コンボボックスを作成
        $row2 = knjb060Query::getBscHdQuery($model);
        $objForm->ae(array("type"       => "select",
                            "name"       => "TITLE",
                            "size"       => "1",
                            "value"      => $model->field["TITLE"],
                            "options"    => isset($row2)?$row2:array(),
                            "extrahtml"  => "$dis_jikan "));
        $arg["data"]["TITLE"] = $objForm->ge("TITLE");
        //時間割選択コンボボックス2
        $objForm->ae(array("type"       => "select",
                            "name"       => "TITLE2",
                            "size"       => "1",
                            "value"      => $model->field["TITLE2"],
                            "options"    => isset($row2)?$row2:array(),
                            "extrahtml"  => "$dis_jikan "));
        $arg["data"]["TITLE2"] = $objForm->ge("TITLE2");

        //Ａ週・Ｂ週出力
        $extra  = ($model->field["PRINT_CHECK"] == "on") ? "checked" : "";
        $extra .= " id=\"PRINT_CHECK\" " . $dis_jikan;
        $arg["data"]["PRINT_CHECK"] = knjCreateCheckBox($objForm, "PRINT_CHECK", "on", $extra);

        //２週間出力
        $extra  = ($model->field["PRINT_CHECK2"] == "on") ? "checked" : "";
        $extra .= " id=\"PRINT_CHECK2\" " . $dis_date;
        $arg["data"]["PRINT_CHECK2"] = knjCreateCheckBox($objForm, "PRINT_CHECK2", "on", $extra);

        //指定日付テキストボックスを作成
        if ($model->field["RADIO"] == 2) {
            if (!isset($model->field["DATE"])) {
                $model->field["DATE"] = $model->control["学籍処理日"];
            }
            //指定日を含む指定週の開始日(月曜日)と終了日(土曜日)を取得
            common::DateConv2($model->field["DATE"], $OutDate1, $OutDate2, 1);
            $model->field["DATE2"] = $OutDate2;
        } else {
            $model->field["DATE"] = "";
            $model->field["DATE2"] = "";
        }
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"], "reload=true");

        $objForm->ae(array("type"       => "text",
                            "name"       => "DATE2",
                            "size"       => "12",
                            "value"      => $model->field["DATE2"],
                            "extrahtml"  => "disabled"));

        $arg["data"]["DATE2"] = $objForm->ge("DATE2");


        //ラジオボタンを作成//出力区分（職員／学級／生徒）//////////////////////////////////////////////////////////////////
        $opt1[0]=1;
        $opt1[1]=2;
        $opt1[2]=3;
        $opt1[3]=4;
        for ($i = 1; $i <= 4; $i++) {
            $name = "KUBUN".$i;
            $objForm->ae(array("type"       => "radio",
                                "name"       => "KUBUN",
                                "value"      => isset($model->field["KUBUN"])?$model->field["KUBUN"]:"1",
                                "extrahtml"  => "onclick=\"shutu_kubun(this);\" id=\"$name\"",
                                "multiple"   => $opt1));

            $arg["data"][$name] = $objForm->ge("KUBUN", $i);
        }

        if (isset($model->field["KUBUN"])) {
            switch ($model->field["KUBUN"]) {
                case 1:
                    $dis_name1 = "";
                    $dis_name2 = "";
                    $dis_cla1 = "disabled";
                    $dis_cla2 = "disabled";
                    $disCourse = "disabled";
                    $dis_cla3 = "disabled";
                    $dis_cla4 = "disabled";
                    $dis_faccd = "disabled";
                    break;
                case 2:
                    $dis_name1 = "disabled";
                    $dis_name2 = "disabled";
                    $dis_cla1 = "";
                    $dis_cla2 = "";
                    $disCourse = "";
                    $dis_cla3 = "disabled";
                    $dis_cla4 = "disabled";
                    $dis_faccd = "disabled";
                    break;
                case 3:
                    $dis_name1 = "disabled";
                    $dis_name2 = "disabled";
                    $dis_cla1 = "disabled";
                    $dis_cla2 = "disabled";
                    $disCourse = "disabled";
                    $dis_cla3 = "";
                    $dis_cla4 = "";
                    $dis_faccd = "disabled";
                    break;
                case 4:
                    $dis_name1 = "disabled";
                    $dis_name2 = "disabled";
                    $dis_cla1 = "disabled";
                    $dis_cla2 = "disabled";
                    $disCourse = "disabled";
                    $dis_cla3 = "disabled";
                    $dis_cla4 = "disabled";
                    $dis_faccd = "";
                    break;
            }
        } else {
            $dis_name1 = "";
            $dis_name2 = "";
            $dis_cla1 = "disabled";
            $dis_cla2 = "disabled";
            $disCourse = "disabled";
            $dis_cla3 = "disabled";
            $dis_cla4 = "disabled";
            $dis_faccd = "disabled";
        }

        //所属選択コンボボックスを作成
        $row1 = knjb060Query::getSectQuery($model->control["年度"]);      //04/04/21  yamauchi

        $objForm->ae(array("type"       => "select",
                            "name"       => "SECTION_CD_NAME1",
                            "size"       => "1",
                            "value"      => $model->field["SECTION_CD_NAME1"],
                            "extrahtml"  =>$dis_name1,
                            "options"    => isset($row1)?$row1:array()));

        $objForm->ae(array("type"       => "select",
                            "name"       => "SECTION_CD_NAME2",
                            "size"       => "1",
                            "value"      => isset($model->field["SECTION_CD_NAME2"])?$model->field["SECTION_CD_NAME2"]:$row1[get_count($row1)-1]["value"],
                            "extrahtml"  =>$dis_name2,
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["SECTION_CD_NAME1"] = $objForm->ge("SECTION_CD_NAME1");
        $arg["data"]["SECTION_CD_NAME2"] = $objForm->ge("SECTION_CD_NAME2");


        //クラス選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query = knjb060Query::getHrclass($model->control["年度"], $model->control["学期"], $model);
        for ($clsNo = 1; $clsNo <= 4; $clsNo++) {
            $dis_cla = "dis_cla".$clsNo;
            $extra = $$dis_cla;
            if ($clsNo <= 2) {
                $extra .= " onchange=\"return btn_submit('knjb060');\"";
            }
            makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS".$clsNo, $model->field["GRADE_HR_CLASS".$clsNo], $extra, 1);
        }
        Query::dbCheckIn($db);

        //課程学科コースコンボ作成
        $db = Query::dbCheckOut();
        $query = knjb060Query::getCourse($model);
        $extra = $disCourse;
        makeCmb($objForm, $arg, $db, $query, "COURSE", $model->field["COURSE"], $extra, 1, "BLANK");
        Query::dbCheckIn($db);


        //施設選択コンボボックスを作成
        $row4 = $query = knjb060Query::getFacility();
        $objForm->ae(array("type"       => "select",
                            "name"       => "FACCD_NAME1",
                            "size"       => "1",
                            "value"      => $model->field["FACCD_NAME1"],
                            "extrahtml"  => $dis_faccd,
                            "options"    => isset($row4)?$row4:array()));

        $objForm->ae(array("type"       => "select",
                            "name"       => "FACCD_NAME2",
                            "size"       => "1",
                            "value"      => isset($model->field["FACCD_NAME2"])?$model->field["FACCD_NAME2"]:$row4[get_count($row4)-1]["value"],
                            "extrahtml"  => $dis_faccd,
                            "options"    => isset($row4)?$row4:array()));

        $arg["data"]["FACCD_NAME1"] = $objForm->ge("FACCD_NAME1");
        $arg["data"]["FACCD_NAME2"] = $objForm->ge("FACCD_NAME2");


        /**********/
        /* ラジオ */
        /**********/
        //出力項目(上段)
        $opt = array(1, 2); //1:科目名 2:講座名
        $model->field["SUBCLASS_CHAIR_DIV"] = ($model->field["SUBCLASS_CHAIR_DIV"] == "") ? "1" : $model->field["SUBCLASS_CHAIR_DIV"];
        $extra = array("id=\"SUBCLASS_CHAIR_DIV1\"", "id=\"SUBCLASS_CHAIR_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "SUBCLASS_CHAIR_DIV", $model->field["SUBCLASS_CHAIR_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }


        /********************/
        /* チェックボックス */
        /********************/
        //授業が無い「校時」を詰める、詰めないのチェックボックス---2005.07.04
        $check = ($model->field["CHECK"] == "on") ? "checked" : "" ;
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => "CHECK",
                            "value"     => "on",
                            "extrahtml" => $check." id=\"CHECK\"" ));
        $arg["data"]["CHECK"] = $objForm->ge("CHECK");
        //「テスト時間割のみ出力」チェックボックス---2006/11/01
        $t_check = ($model->field["TEST_CHECK"] == "on") ? "checked" : "" ;
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => "TEST_CHECK",
                            "value"     => "on",
                            "extrahtml" => $t_check." id=\"TEST_CHECK\"" ));
        $arg["data"]["TEST_CHECK"] = $objForm->ge("TEST_CHECK");
        //職員は正担任（MAX職員番号）のみ出力
        $staff_check = ($model->field["STAFF_CHECK"] == "on") ? "checked" : "" ;
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => "STAFF_CHECK",
                            "value"     => "on",
                            "extrahtml" => $staff_check." id=\"STAFF_CHECK\"" ));
        $arg["data"]["STAFF_CHECK"] = $objForm->ge("STAFF_CHECK");
        //クラス名は出力しない
        $no_class_check = ($model->field["NO_CLASS_CHECK"] == "on") ? "checked" : "" ;
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => "NO_CLASS_CHECK",
                            "value"     => "on",
                            "extrahtml" => $no_class_check." id=\"NO_CLASS_CHECK\"" ));
        $arg["data"]["NO_CLASS_CHECK"] = $objForm->ge("NO_CLASS_CHECK");

        //「科目名は必履修区分を出力しない」チェックボックス
        $extra  = ($model->field["NO_REQUIRE_FLG_CHECK"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"NO_REQUIRE_FLG_CHECK\" ";
        $arg["data"]["NO_REQUIRE_FLG_CHECK"] = knjCreateCheckBox($objForm, "NO_REQUIRE_FLG_CHECK", "on", $extra);

        //「科目名または講座名を略称名で出力する」チェックボックス
        $extra  = ($model->field["SUBCLASS_ABBV_CHECK"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"SUBCLASS_ABBV_CHECK\" ";
        $arg["data"]["SUBCLASS_ABBV_CHECK"] = knjCreateCheckBox($objForm, "SUBCLASS_ABBV_CHECK", "on", $extra);

        //「教員名を略称名で出力する」チェックボックス
        $extra  = ($model->field["STAFF_ABBV_CHECK"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"STAFF_ABBV_CHECK\" ";
        $arg["data"]["STAFF_ABBV_CHECK"] = knjCreateCheckBox($objForm, "STAFF_ABBV_CHECK", "on", $extra);
        
        //教員名は出力しない
        $no_staff_check = ($model->field["NO_STAFF_CHECK"] == "on") ? "checked" : "" ;
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => "NO_STAFF_CHECK",
                            "value"     => "on",
                            "extrahtml" => $no_staff_check." id=\"NO_STAFF_CHECK\"" ));
        $arg["data"]["NO_STAFF_CHECK"] = $objForm->ge("NO_STAFF_CHECK");

        //土曜欄無しのフォームで出力する
        $heijitsu = ($model->field["HEIJITSU_CHECK"] == "on") ? "checked" : "" ;
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => "HEIJITSU_CHECK",
                            "value"     => "on",
                            "extrahtml" => $heijitsu." id=\"HEIJITSU_CHECK\"" ));
        $arg["data"]["HEIJITSU_CHECK"] = $objForm->ge("HEIJITSU_CHECK");


        //印刷ボタンを作成する
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ));

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");


        //終了ボタンを作成する
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //ＣＳＶボタン
        $extra = " onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJB060"
                            ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"
                            ));

        //JavaScriptで参照するため
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "GAKUSEKI",
                            "value"      => $model->control["学籍処理日"]
                            ));

        //学校区分---2005.05.19
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SCHOOLDIV",
                            "value"     => knjb060Query::getSchooldiv($model),
                            ));

        knjCreateHidden($objForm, "T_YEAR");
        knjCreateHidden($objForm, "T_BSCSEQ");
        knjCreateHidden($objForm, "T_SEMESTER");
        knjCreateHidden($objForm, "T_YEAR2");
        knjCreateHidden($objForm, "T_BSCSEQ2");
        knjCreateHidden($objForm, "T_SEMESTER2");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useProficiency", $model->Properties["useProficiency"]);
        knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
        knjCreateHidden($objForm, "notShowStaffcd", $model->Properties["notShowStaffcd"]);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "knjb060NoCheckTanisei", $model->Properties["knjb060NoCheckTanisei"]);
        knjCreateHidden($objForm, "SCHOOL_NAME", $model->schoolName);
        knjCreateHidden($objForm, "useTestFacility", $model->Properties["useTestFacility"]);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb060Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array('label' => "",
                       'value' => "");
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
    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR+1;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
