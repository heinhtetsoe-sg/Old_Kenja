<?php

require_once('for_php7.php');
class knjz020form1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("main", "POST", "knjz020index.php", "", "main");
        $db = Query::dbCheckOut();
        if ($model->dataBaseinfo === '2') {
            $db2 = Query::dbCheckOut2();
        }
        $arg["Closing"] = "";

        //セキュリティーチェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["Closing"] = " closing_window(1); ";
        }

        //起動時チェック
        if (!knjz020Query::checkControlMst($db) || !knjz020Query::checkAssessMst($db)) {
            $arg["Closing"] = " closing_window(2);";
        }

        if ($model->Properties["use_school_detail_gcm_dat"] != "1") {
            $arg["unuse_school_detail_gcm_dat"] = "1";
        }

        //学期数取得
        $semester = $db->getOne(knjz020Query::getSemester($model, "max"));
        knjCreateHidden($objForm, "MAX_SEMESTER", $semester);

        $month_syusu = array();
        if ($model->Properties["use_Month_Syusu"] == "1") {
            //月別週数データ取得
            $query = knjz020Query::getAttendSyusuMst($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $month_syusu["HOUTEI_SYUSU_MONTH_".$row["SEMESTER"]."_".$row["MONTH"]] = $row["SYUSU"];
            }
            $result->free();
        }

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
        }
        $query = knjz020Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('changeKind');\"";
        makeCmb2($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);

        //年度設定
        $result    = $db->query(knjz020Query::selectYearQuery($model));
        $opt       = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"],
                           "value" => $row["YEAR"]);
        }
        $result->free();

        //年度コンボボックス
        $objForm->ae(array("type"        => "select",
                            "name"        => "year",
                            "size"        => "1",
                            "value"       => $model->year,
                            "extrahtml"   => "onchange=\"return btn_submit('');\"",
                            "options"     => $opt));

        $objForm->ae(array("type"        => "text",
                            "name"        => "year_add",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => "" ));

        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_year_add",
                            "value"       => "年度追加",
                            "extrahtml"   => "onclick=\"return add('');\"" ));

        $arg["year"] = array( "VAL"       => $objForm->ge("year")."&nbsp;&nbsp;".
                                             $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));

        //学校名称2表示
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $info = $db->getRow(knjz020Query::selectQuery($model, CTRL_YEAR), DB_FETCHMODE_ASSOC);
            $arg["SCH_NAME2"] = (strlen($info["SCHOOLNAME2"]) > 0) ? "<<".$info["SCHOOLNAME2"].">>" : "";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $info = $db->getRow(knjz020Query::selectQuery($model, CTRL_YEAR), DB_FETCHMODE_ASSOC);
            $arg["SCH_NAME2"] = (strlen($info["SCHOOLNAME2"]) > 0) ? "<<".$info["SCHOOLNAME2"].">>" : "";
        }

        //学校情報取得
        if (!isset($model->warning) && $model->cmd != "changeJisuDiv") {
            $result = $db->query(knjz020Query::selectQuery($model, $model->year));
            $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
            foreach ($month_syusu as $key => $val) {
                $row[$key] = $val;
            }
        } else {
            $row =& $model->field;
            if ($model->cmd == "changeJisuDiv") {
                //法定時数の学期制授業週数がNULLの場合再セット（実時数の時、フィールドがなくなるため）
                $query = knjz020Query::selectQuery($model, $model->year);
                $HouteiRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                for ($sem = 1; $sem <= $semester; $sem++) {
                    if (!$row["HOUTEI_SYUSU_SEMESTER".$sem]) {
                        $row["HOUTEI_SYUSU_SEMESTER".$sem] = $HouteiRow["HOUTEI_SYUSU_SEMESTER".$sem];
                    }
                }
            }
        }

        //創立年度
        $objForm->ae(array("type"        => "text",
                            "name"        => "FOUNDEDYEAR",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "value"       => $row["FOUNDEDYEAR"],
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ));

        $arg["data"]["FOUNDEDYEAR"] = $objForm->ge("FOUNDEDYEAR");

        //現在期
        $objForm->ae(array("type"        => "text",
                            "name"        => "PRESENT_EST",
                            "value"       => $row["PRESENT_EST"],
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ));

        $arg["data"]["PRESENT_EST"] = $objForm->ge("PRESENT_EST");

        //学校種別
        $objForm->ae(array("type"        => "text",
                            "name"        => "CLASSIFICATION",
                            "value"       => $row["CLASSIFICATION"],
                            "size"        => 4,
                            "maxlength"   => 6,
                            "extrahtml"   => "" ));

        $arg["data"]["CLASSIFICATION"] = $objForm->ge("CLASSIFICATION");

        //学校名称１
        $objForm->ae(array("type"        => "text",
                            "name"        => "SCHOOLNAME1",
                            "value"       => $row["SCHOOLNAME1"],
                            "size"        => 60,
                            "maxlength"   => 90,
                            "extrahtml"   => "" ));

        $arg["data"]["SCHOOLNAME1"] = $objForm->ge("SCHOOLNAME1");

        //学校名称２
        $objForm->ae(array("type"        => "text",
                            "name"        => "SCHOOLNAME2",
                            "value"       => $row["SCHOOLNAME2"],
                            "size"        => 60,
                            "maxlength"   => 90,
                            "extrahtml"   => "" ));

        $arg["data"]["SCHOOLNAME2"] = $objForm->ge("SCHOOLNAME2");

        //学校名称３
        $objForm->ae(array("type"        => "text",
                            "name"        => "SCHOOLNAME3",
                            "value"       => $row["SCHOOLNAME3"],
                            "size"        => 60,
                            "maxlength"   => 90,
                            "extrahtml"   => "" ));

        $arg["data"]["SCHOOLNAME3"] = $objForm->ge("SCHOOLNAME3");

        //学校名称英字
        $objForm->ae(array("type"        => "text",
                            "name"        => "SCHOOLNAME_ENG",
                            "value"       => $row["SCHOOLNAME_ENG"],
                            "size"        => 60,
                            "maxlength"   => 60,
                            "extrahtml"   => "" ));

        $arg["data"]["SCHOOLNAME_ENG"] = $objForm->ge("SCHOOLNAME_ENG");

        //郵便番号
        $arg["data"]["SCHOOLZIPCD"] = View::popUpZipCode($objForm, "SCHOOLZIPCD", $row["SCHOOLZIPCD"], "SCHOOLADDR1");

        //住所１
        $objForm->ae(array("type"        => "text",
                            "name"        => "SCHOOLADDR1",
                            "value"       => $row["SCHOOLADDR1"],
                            "size"        => 50,
                            "maxlength"   => 90,
                            "extrahtml"   => "" ));

        $arg["data"]["SCHOOLADDR1"] = $objForm->ge("SCHOOLADDR1");

        //住所２
        $objForm->ae(array("type"        => "text",
                            "name"        => "SCHOOLADDR2",
                            "value"       => $row["SCHOOLADDR2"],
                            "size"        => 50,
                            "maxlength"   => 90,
                            "extrahtml"   => "" ));

        $arg["data"]["SCHOOLADDR2"] = $objForm->ge("SCHOOLADDR2");

        //住所１英字
        $objForm->ae(array("type"        => "text",
                            "name"        => "SCHOOLADDR1_ENG",
                            "value"       => $row["SCHOOLADDR1_ENG"],
                            "size"        => 60,
                            "maxlength"   => 70,
                            "extrahtml"   => "" ));

        $arg["data"]["SCHOOLADDR1_ENG"] = $objForm->ge("SCHOOLADDR1_ENG");

        //住所２英字
        $objForm->ae(array("type"        => "text",
                            "name"        => "SCHOOLADDR2_ENG",
                            "value"       => $row["SCHOOLADDR2_ENG"],
                            "size"        => 60,
                            "maxlength"   => 70,
                            "extrahtml"   => "" ));

        $arg["data"]["SCHOOLADDR2_ENG"] = $objForm->ge("SCHOOLADDR2_ENG");

        //電話番号
        $objForm->ae(array("type"        => "text",
                            "name"        => "SCHOOLTELNO",
                            "value"       => $row["SCHOOLTELNO"],
                            "size"        => 20,
                            "maxlength"   => 14,
                            "extrahtml"   => "" ));

        $arg["data"]["SCHOOLTELNO"] = $objForm->ge("SCHOOLTELNO");

        //FAX番号
        $objForm->ae(array("type"        => "text",
                            "name"        => "SCHOOLFAXNO",
                            "value"       => $row["SCHOOLFAXNO"],
                            "size"        => 20,
                            "maxlength"   => 14,
                            "extrahtml"   => "" ));

        $arg["data"]["SCHOOLFAXNO"] = $objForm->ge("SCHOOLFAXNO");

        //メールアドレス
        $objForm->ae(array("type"        => "text",
                            "name"        => "SCHOOLMAIL",
                            "value"       => $row["SCHOOLMAIL"],
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "" ));

        $arg["data"]["SCHOOLMAIL"] = $objForm->ge("SCHOOLMAIL");

        //ホームページ
        $objForm->ae(array("type"        => "text",
                            "name"        => "SCHOOLURL",
                            "value"       => $row["SCHOOLURL"],
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "" ));

        $arg["data"]["SCHOOLURL"] = $objForm->ge("SCHOOLURL");

        //学校区分
        $result = $db->query(knjz020Query::getSchooldiv());
        $opt = array();
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label"  => $row2["NAMECD2"].":".$row2["NAME1"],
                           "value"  => $row2["NAMECD2"]);
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "SCHOOLDIV",
                            "size"        => 1,
                            "extrahtml"   => "",
                            "value"       => $row["SCHOOLDIV"],
                            "options"     => $opt));

        $arg["data"]["SCHOOLDIV"] = $objForm->ge("SCHOOLDIV");

        //学期制
        $objForm->ae(array("type"        => "text",
                            "name"        => "SEMESTERDIV",
                            "value"       => $row["SEMESTERDIV"],
                            "size"        => 5,
                            "maxlength"   => 1,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ));

        $arg["data"]["SEMESTERDIV"] = $objForm->ge("SEMESTERDIV");

        //学年数上限
        $objForm->ae(array("type"        => "text",
                            "name"        => "GRADE_HVAL",
                            "value"       => $row["GRADE_HVAL"],
                            "size"        => 5,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ));

        $arg["data"]["GRADE_HVAL"] = $objForm->ge("GRADE_HVAL");

        //入学日付
        $arg["data"]["ENTRANCE_DATE"] = View::popUpCalendar(
            $objForm,
            "ENTRANCE_DATE",
            str_replace("-", "/", $row["ENTRANCE_DATE"]),
            ""
        );

        //卒業日付
        $arg["data"]["GRADUATE_DATE"] = View::popUpCalendar(
            $objForm,
            "GRADUATE_DATE",
            str_replace("-", "/", $row["GRADUATE_DATE"]),
            ""
        );

        //卒業単位数
        $objForm->ae(array("type"        => "text",
                            "name"        => "GRAD_CREDITS",
                            "value"       => $row["GRAD_CREDITS"],
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ));

        $arg["data"]["GRAD_CREDITS"] = $objForm->ge("GRAD_CREDITS");

        //卒業履修単位数
        $objForm->ae(array("type"        => "text",
                            "name"        => "GRAD_COMP_CREDITS",
                            "value"       => $row["GRAD_COMP_CREDITS"],
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ));

        $arg["data"]["GRAD_COMP_CREDITS"] = $objForm->ge("GRAD_COMP_CREDITS");

        //学期評価区分
        $opt = array();
        $result = $db->query(knjz020Query::getAssesscd1());

        $opt[] = array("label" => "0:標準（100段階）", "value" => "0");
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label"  => $row2["ASSESSCD"].":".$row2["ASSESSMEMO"]."(".$row2["ASSESSLEVELCNT"]."段階)",
                           "value"  => $row2["ASSESSCD"]);
        }

        $objForm->ae(array("type"        => "select",
                            "name"        => "SEMES_ASSESSCD",
                            "size"        => 1,
                            "extrahtml"   => "",
                            "value"       => $row["SEMES_ASSESSCD"],
                            "options"     => $opt));

        $arg["data"]["SEMES_ASSESSCD"] = $objForm->ge("SEMES_ASSESSCD");

        //学期保留値
        $objForm->ae(array("type"        => "text",
                            "name"        => "SEMES_FEARVAL",
                            "value"       => $row["SEMES_FEARVAL"],
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ));

        $arg["data"]["SEMES_FEARVAL"] = $objForm->ge("SEMES_FEARVAL");

        //学年評価区分
        $arg["data"]["GRADE_ASSESSCD"] = $db->getOne(knjz020Query::getAssesscd2());

        //学年保留値
        $objForm->ae(array("type"        => "text",
                            "name"        => "GRADE_FEARVAL",
                            "value"       => $row["GRADE_FEARVAL"],
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ));

        $arg["data"]["GRADE_FEARVAL"] = $objForm->ge("GRADE_FEARVAL");


        //評定計算方法
        $result = $db->query(knjz020Query::getGvalCalc());
        $optGvalCalc = array();
        $optGvalCalc[] = array("label"  => "", "value"  => "");
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optGvalCalc[] = array("label"  => $row2["NAME1"],
                                   "value"  => $row2["NAMECD2"]);
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "GVAL_CALC",
                            "size"        => 1,
                            "extrahtml"   => "",
                            "value"       => $row["GVAL_CALC"],
                            "options"     => $optGvalCalc));
        $arg["data"]["GVAL_CALC"] = $objForm->ge("GVAL_CALC");

        //欠席に含める（休学）
        $checked = strlen($row["SEM_OFFDAYS"]) ? "checked" : "";
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "SEM_OFFDAYS",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["SEM_OFFDAYS"] = $objForm->ge("SEM_OFFDAYS");

        $query = knjz020Query::getC001();
        $result = $db->query($query);
        $c001 = array();
        while ($rowC001 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $c001[$rowC001["NAMECD2"]] = $rowC001["NAME1"];
        }
        $result->free();

        //欠課に含める（休学）
        $checked = strlen($row["SUB_OFFDAYS"]) ? "checked" : "";
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "SUB_OFFDAYS",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["SUB_OFFDAYS"] = $objForm->ge("SUB_OFFDAYS");

        //欠課に含める（忌引）
        $checked = strlen($row["SUB_MOURNING"]) ? "checked" : "";
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "SUB_MOURNING",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["SUB_MOURNING"] = $objForm->ge("SUB_MOURNING");

        //欠課に含める（出停）
        $checked = strlen($row["SUB_SUSPEND"]) ? "checked" : "";
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "SUB_SUSPEND",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["SUB_SUSPEND"] = $objForm->ge("SUB_SUSPEND");

        $arg["data"]["SUB_SUSPEND_NAME"] = "出停";
        if ($model->koudome == 'true') {
            $arg["data"]["SUB_SUSPEND_NAME"] = $c001["2"];
        }

        //欠課に含める（出停伝染病）
        $checked = strlen($row["SUB_VIRUS"]) ? "checked" : "";
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "SUB_VIRUS",
                            "extrahtml"  => $checked,
                            "value"      => "1"));

        if ($model->virus == 'true') {
            $arg["data"]["SUB_VIRUS"] = $objForm->ge("SUB_VIRUS");
            $arg["data"]["SUB_VIRUS_NAME"] = $c001["19"];
            $arg["data"]["SPASE"] = '&nbsp;&nbsp;&nbsp;';
            $arg["data"]["SPASE2"] = '<BR>';
        } else {
            $arg["data"]["SPASE"] = '';
            $arg["data"]["SPASE2"] = '&nbsp;&nbsp;&nbsp;';
        }

        //欠課に含める（交止）
        $checked = strlen($row["SUB_KOUDOME"]) ? "checked" : "";
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "SUB_KOUDOME",
                            "extrahtml"  => $checked,
                            "value"      => "1"));

        if ($model->koudome == 'true') {
            $arg["data"]["SUB_KOUDOME"] = $objForm->ge("SUB_KOUDOME");
            $arg["data"]["SUB_KOUDOME_NAME"] = $c001["25"]."&nbsp;";
        }

        //欠課に含める（公欠）
        $checked = strlen($row["SUB_ABSENT"]) ? "checked" : "";
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "SUB_ABSENT",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["SUB_ABSENT"] = $objForm->ge("SUB_ABSENT");

        //１日出欠席の判定方法
        $checked = strlen($row["SYUKESSEKI_HANTEI_HOU"]) ? "checked" : "";
        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "SYUKESSEKI_HANTEI_HOU",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["SYUKESSEKI_HANTEI_HOU"] = $objForm->ge("SYUKESSEKI_HANTEI_HOU");


        $absent_cov_late = $row["ABSENT_COV_LATE"];
        $row["AMARI_KURIAGE"]   = ($row["AMARI_KURIAGE"] == '99') ? '' : $row["AMARI_KURIAGE"];
        $amari_kuriage   = $row["AMARI_KURIAGE"];
        //欠課数換算コンボ
        $opt = array();
        $opt[] = array("label" => "0：換算なし",        "value" => 0);
        $opt[] = array("label" => "1：学期ごとに清算",  "value" => 1);
        $opt[] = array("label" => "2：年間で清算",      "value" => 2);
        $opt[] = array("label" => "3：学期ごとに清算(小数点あり)",  "value" => 3);
        $opt[] = array("label" => "4：年間で清算(小数点あり)",      "value" => 4);
        $opt[] = array("label" => "5：年間で清算(余りで換算)",      "value" => 5);
        $objForm->ae(array("type"        => "select",
                            "name"        => "ABSENT_COV",
                            "size"        => 1,
                            "extrahtml"   => "onchange=\"return change_absent('{$absent_cov_late}', '{$amari_kuriage}');\"",
                            "value"       => $row["ABSENT_COV"],
                            "options"     => $opt));
        $arg["data"]["ABSENT_COV"] = $objForm->ge("ABSENT_COV");

        //欠課数換算テキスト
        $disabled = ($row["ABSENT_COV"] == '0') ? "disabled" : "";
        $objForm->ae(array("type"        => "text",
                            "name"        => "ABSENT_COV_LATE",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "value"       => $row["ABSENT_COV_LATE"],
                            "extrahtml"   => "$disabled . onblur=\"this.value=toInteger(this.value)\";" ));
        $arg["data"]["ABSENT_COV_LATE"] = $objForm->ge("ABSENT_COV_LATE");

        //欠課数繰上テキスト
        $disabled = ($row["ABSENT_COV"] != '5') ? "disabled" : "";
        $objForm->ae(array("type"        => "text",
                            "name"        => "AMARI_KURIAGE",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "value"       => $row["AMARI_KURIAGE"],
                            "extrahtml"   => "$disabled . onblur=\"this.value=toInteger(this.value)\";" ));
        $arg["data"]["AMARI_KURIAGE"] = $objForm->ge("AMARI_KURIAGE");

        //特活欠課数換算
        $opt = array();
        $opt[] = array("label" => "0：換算なし", "value" => 0);
        $opt[] = array("label" => "1：二捨三入", "value" => 1);
        $opt[] = array("label" => "2：四捨五入", "value" => 2);
        $opt[] = array("label" => "3：切り上げ", "value" => 3);
        $opt[] = array("label" => "4：切り捨て", "value" => 4);

        $extra = "";
        $arg["data"]["TOKUBETU_KATUDO_KANSAN"] = knjCreateCombo($objForm, "TOKUBETU_KATUDO_KANSAN", $row["TOKUBETU_KATUDO_KANSAN"], $opt, $extra, 1);

        //授業数管理区分
        $opt = array();
        $opt[] = array('label' => '', 'value' => '');
        //V_名称マスタ「Z042」が1件でもあれば、名称マスタを表示する。
        $query = knjz020Query::getVNameMstZ042($model);
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row2["LABEL"],
                           'value' => $row2["VALUE"]);
        }
        //なければ、固定で表示する。
        if (get_count($opt) == 1) {
            $opt[] = array('label' => '1:法定時数', 'value' => '1');
            $opt[] = array('label' => '2:実時数',   'value' => '2');
        }
        $extra = "onchange=\"return btn_jisuchange('changeJisuDiv');\"";
        $arg["data"]["JUGYOU_JISU_FLG"] = knjCreateCombo($objForm, "JUGYOU_JISU_FLG", $row["JUGYOU_JISU_FLG"], $opt, $extra, 1);
        //授業数管理区分
        $opt = array();
        $opt[] = array('label' => '', 'value' => '');
        $opt[] = array('label' => '1:四捨五入', 'value' => '1');
        $opt[] = array('label' => '2:切り上げ', 'value' => '2');
        $opt[] = array('label' => '3:切り捨て', 'value' => '3');
        $opt[] = array('label' => '4:実数',     'value' => '4');
        if (strlen($row["JOUGENTI_SANSYUTU_HOU"])) {
            $value = $row["JOUGENTI_SANSYUTU_HOU"];
        } else {
            if (preg_match('/[3-4]/', $row["ABSENT_COV"])) {
                $value = 4;
            } else {
                $value = 3;
            }
        }
        $extra = "";
        $arg["data"]["JOUGENTI_SANSYUTU_HOU"] = knjCreateCombo($objForm, "JOUGENTI_SANSYUTU_HOU", $value, $opt, $extra, 1);
        //都道府県
        $opt = array();
        $opt[] = array('label' => '', 'value' => '');
        $value_flg = false;
        $query = knjz020Query::getPrefCd();
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row2["LABEL"],
                           'value' => $row2["VALUE"]);
            if ($row["PREF_CD"] == $row2["VALUE"]) {
                $value_flg = true;
            }
        }
        $row["PREF_CD"] = ($row["PREF_CD"] && $value_flg) ? $row["PREF_CD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["PREF_CD"] = knjCreateCombo($objForm, "PREF_CD", $row["PREF_CD"], $opt, $extra, 1);
        //成績入力の評価・評定計算方法
        $opt = array();
        $opt[] = array('label' => '', 'value' => '');
        $opt[] = array('label' => '1:四捨五入', 'value' => '1');
        $opt[] = array('label' => '2:切り上げ', 'value' => '2');
        $opt[] = array('label' => '3:切り捨て', 'value' => '3');
        $extra = "";
        $arg["data"]["PARTS_HYOUKA_HYOUTEI_KEISAN"] = knjCreateCombo($objForm, "PARTS_HYOUKA_HYOUTEI_KEISAN", $row["PARTS_HYOUKA_HYOUTEI_KEISAN"], $opt, $extra, 1);

        if ($model->Properties["use_school_Detail_009"] === '1') {
            $arg['use_school_Detail_009'] = '1';
        }
        //警告点区分
        $opt = array();
        $opt[] = array('label' => '', 'value' => '');
        $result = $db->query(knjz020Query::getKeikokutenKubun($model));
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row2["LABEL"]==':'?'':$row2["LABEL"],
                           'value' => $row2["VALUE"]);
        }
        $extra = " onchange='changeKeikokutenKubun()'";
        $arg["data"]["KEIKOKUTEN_KUBUN"] = knjCreateCombo($objForm, "KEIKOKUTEN_KUBUN", $row["KEIKOKUTEN_KUBUN"], $opt, $extra, 1);

        //類型平均
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["RUIKEIHEIKIN_BUNSI"] = knjCreateTextBox($objForm, $row["RUIKEIHEIKIN_BUNSI"], "RUIKEIHEIKIN_BUNSI", 4, 4, $extra);
        $arg["data"]["RUIKEIHEIKIN_BUNBO"] = knjCreateTextBox($objForm, $row["RUIKEIHEIKIN_BUNBO"], "RUIKEIHEIKIN_BUNBO", 4, 4, $extra);

        /********************/
        /* テキストボックス */
        /********************/
        //履修上限値(分子) (通常)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RISYU_BUNSI"] = knjCreateTextBox($objForm, $row["RISYU_BUNSI"], "RISYU_BUNSI", 2, 2, $extra);
        //履修上限値(分母) (通常)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RISYU_BUNBO"] = knjCreateTextBox($objForm, $row["RISYU_BUNBO"], "RISYU_BUNBO", 2, 2, $extra);
        //修得上限値(分子) (通常)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SYUTOKU_BUNSI"] = knjCreateTextBox($objForm, $row["SYUTOKU_BUNSI"], "SYUTOKU_BUNSI", 2, 2, $extra);
        //修得上限値(分母) (通常)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SYUTOKU_BUNBO"] = knjCreateTextBox($objForm, $row["SYUTOKU_BUNBO"], "SYUTOKU_BUNBO", 2, 2, $extra);
        //履修上限値(分子) (特活)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RISYU_BUNSI_SPECIAL"] = knjCreateTextBox($objForm, $row["RISYU_BUNSI_SPECIAL"], "RISYU_BUNSI_SPECIAL", 2, 2, $extra);
        //特活上限値(分母) (特活)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RISYU_BUNBO_SPECIAL"] = knjCreateTextBox($objForm, $row["RISYU_BUNBO_SPECIAL"], "RISYU_BUNBO_SPECIAL", 2, 2, $extra);
        //修得上限値(分母) (特活)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SYUTOKU_BUNBO_SPECIAL"] = knjCreateTextBox($objForm, $row["SYUTOKU_BUNBO_SPECIAL"], "SYUTOKU_BUNBO_SPECIAL", 2, 2, $extra);
        //授業時間(分)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["JITU_JIFUN"] = knjCreateTextBox($objForm, $row["JITU_JIFUN"], "JITU_JIFUN", 3, 3, $extra);
        //特活授業時間(分)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["JITU_JIFUN_SPECIAL"] = knjCreateTextBox($objForm, $row["JITU_JIFUN_SPECIAL"], "JITU_JIFUN_SPECIAL", 3, 3, $extra);
        //授業週数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["JITU_SYUSU"] = knjCreateTextBox($objForm, $row["JITU_SYUSU"], "JITU_SYUSU", 3, 3, $extra);
        //欠席日数注意(分子)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["KESSEKI_WARN_BUNSI"] = knjCreateTextBox($objForm, $row["KESSEKI_WARN_BUNSI"], "KESSEKI_WARN_BUNSI", 2, 2, $extra);
        //欠席日数注意(分母)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["KESSEKI_WARN_BUNBO"] = knjCreateTextBox($objForm, $row["KESSEKI_WARN_BUNBO"], "KESSEKI_WARN_BUNBO", 2, 2, $extra);
        //欠席日数超過(分子)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["KESSEKI_OUT_BUNSI"] = knjCreateTextBox($objForm, $row["KESSEKI_OUT_BUNSI"], "KESSEKI_OUT_BUNSI", 2, 2, $extra);
        //欠席日数超過(分母)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["KESSEKI_OUT_BUNBO"] = knjCreateTextBox($objForm, $row["KESSEKI_OUT_BUNBO"], "KESSEKI_OUT_BUNBO", 2, 2, $extra);
        //教育委員会統計用学校番号
        //ABBV1=2の時のみ、教育員会DBを参照
        if ($model->dataBaseinfo === '2') {
            $arg["KYOUIKU_IINKAI_SCHOOLCD"] = '1';

            $opt = array();
            $value_flg = false;
            $query = knjz020Query::getEdboardSchoolcd();
            $result = $db2->query($query);
            while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row2["LABEL"],
                               'value' => $row2["VALUE"]);
                if ($row["KYOUIKU_IINKAI_SCHOOLCD"] == $row2["VALUE"]) {
                    $value_flg = true;
                }
            }
            $row["KYOUIKU_IINKAI_SCHOOLCD"] = ($row["KYOUIKU_IINKAI_SCHOOLCD"] && $value_flg) ? $row["KYOUIKU_IINKAI_SCHOOLCD"] : $opt[0]["value"];
            $extra = "";
            $arg["data"]["KYOUIKU_IINKAI_SCHOOLCD"] = knjCreateCombo($objForm, "KYOUIKU_IINKAI_SCHOOLCD", $row["KYOUIKU_IINKAI_SCHOOLCD"], $opt, $extra, 1);
        }

        //法定授業週数
        $tmp = array();
        if ($row["JUGYOU_JISU_FLG"] === '1' && $model->Properties["hibiNyuuryokuNasi"] === '1') {
            if ($model->Properties["use_Month_Syusu"] == "1") {
                //月一覧取得
                $m_data = array();
                $query = knjz020Query::getMonthList($model);
                $result = $db->query($query);
                while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $m_data[$row2["NAMECD2"]] = $row2["NAME1"];
                }
                $result->free();

                //月別週数
                for ($dcnt = 0; $dcnt < get_count($model->semMonthInfo); $dcnt++) {
                    for ($i = $model->semMonthInfo[$dcnt]["S_MONTH"]; $i <= $model->semMonthInfo[$dcnt]["E_MONTH"]; $i++) {
                        $month = $i;
                        if ($i > 12) {
                            $month = $i - 12;
                        }
                        $month = sprintf('%02d', $month);

                        if (strlen($m_data[$month]) > 0) {
                            $extra = "onblur=\"this.value=toInteger(this.value)\"";
                            $tmp["HOUTEI_SYUSU_MONTH"] = knjCreateTextBox($objForm, $row["HOUTEI_SYUSU_MONTH_".$model->semMonthInfo[$dcnt]["SEMESTER"]."_".$month], "HOUTEI_SYUSU_MONTH_".$model->semMonthInfo[$dcnt]["SEMESTER"]."_".$month, 3, 3, $extra);
                            $tmp["SYUSU_LABEL"] = $m_data[$month]." (".$model->semMonthInfo[$dcnt]["SEMESTERNAME"].") 週数";
                            $arg["m_syusu"][] = $tmp;
                        }
                    }
                }
            } else {
                //学期別週数
                $query = knjz020Query::getSemester($model);
                $result = $db->query($query);
                while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $extra = "onblur=\"this.value=toInteger(this.value)\"";
                    $tmp["HOUTEI_SYUSU_SEMESTER"] = knjCreateTextBox($objForm, $row["HOUTEI_SYUSU_SEMESTER".$row2["SEMESTER"]], "HOUTEI_SYUSU_SEMESTER".$row2["SEMESTER"], 3, 3, $extra);
                    $tmp["SEMESTER_NAME"] = "※".$row2["SEMESTERNAME"];
                    $arg["syusu"][] = $tmp;
                }
            }
        }

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onClick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onClick=\"return showConfirm();\"";
        $arg["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "取 消", $extra);
        //終了
        $extra = "onClick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //他条件設定
        //$extra = "onClick=\" wopen('".REQUESTROOT."/Z/KNJZ021/knjz021index.php?year={$model->year}&SENDSCHKIND={$model->field["SCHKIND"]}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        //$arg["btn_detail"] = knjCreateBtn($objForm, "btn_detail", "他条件設定", $extra);
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            //Z024
            $extra = "onClick=\" wopen('".REQUESTROOT."/Z/KNJZ024/knjz024index.php?year={$model->year}&SENDSCHKIND={$model->field["SCHKIND"]}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["btn_z024"] = knjCreateBtn($objForm, "btn_z024", "授業時数設定", $extra);
        }
        //グループウェア
        if ($model->Properties["useGroupware"] === '1') {
            $url = REQUESTROOT."/Z/KNJZ022/knjz022index.php?year={$model->year}&SENDSCHKIND={$model->sendSchkind}";
            $extra = "onClick=\"openScreen('{$url}');\"";
            $arg["btn_groupware"] = knjCreateBtn($objForm, "btn_groupware", "グループウェア", $extra);
        }

        $arg["show_csv"] = "ON";
        //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
        $opt_shubetsu = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
        $click = " onclick=\"return changeRadio(this);\"";
        $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ファイルからの取り込み
        $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

        //ヘッダ有チェックボックス
        $check_header = "checked id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["data"]["CSV_XLS_NAME"] = "ＣＳＶ出力<BR>／ＣＳＶ取込";

        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $row["UPDATED"]);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJZ020");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "YEAR_HIDDEN", $model->year);
        knjCreateHidden($objForm, "dataBaseinfo", $model->dataBaseinfo);

        Query::dbCheckIn($db);
        if ($model->dataBaseinfo === '2') {
            Query::dbCheckIn($db2);
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz020Form1.html", $arg);
    }
}
//コンボ作成
function makeCmb2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
