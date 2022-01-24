<?php

require_once('for_php7.php');

class knjtx001form1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjtx001index.php", "", "main");
        $db           = Query::dbCheckOut();
        $arg["Closing"] = "";

        //セキュリティーチェック
        if(AUTHORITY != DEF_UPDATABLE) {
            $arg["Closing"] = " closing_window(1); ";
        }

        //起動時チェック
        if (!knjtx001Query::checkControlMst($db) || !knjtx001Query::checkAssessMst($db)) {
           $arg["Closing"] = " closing_window(2);";
        }

        //年度設定
        $result    = $db->query(knjtx001Query::selectYearQuery());
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"],
                          "value" => $row["YEAR"]);
        }

        //年度コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "year",
                            "size"        => "1",
                            "value"       => $model->year,
                            "extrahtml"   => "onchange=\"return btn_submit('');\"",
                            "options"     => $opt));

        $objForm->ae( array("type"        => "text",
                            "name"        => "year_add",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => "" ));

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_year_add",
                            "value"       => "年度追加",
                            "extrahtml"   => "onclick=\"return add('');\"" ));

        $arg["year"] = array( "VAL"       => $objForm->ge("year")."&nbsp;&nbsp;".
                                             $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));

        //学校情報取得
        if (!isset($model->warning)) {
            $result = $db->query(knjtx001Query::selectQuery($model->year));
            $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        } else {
            $row =& $model->field;
        }

        //創立年度
        $objForm->ae( array("type"        => "text",
                            "name"        => "FOUNDEDYEAR",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "value"       => $row["FOUNDEDYEAR"],
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ) );

        $arg["data"]["FOUNDEDYEAR"] = $objForm->ge("FOUNDEDYEAR");

        //現在期
        $objForm->ae( array("type"        => "text",
                            "name"        => "PRESENT_EST",
                            "value"       => $row["PRESENT_EST"],
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ) );

        $arg["data"]["PRESENT_EST"] = $objForm->ge("PRESENT_EST");

        //学校種別
        $objForm->ae( array("type"        => "text",
                            "name"        => "CLASSIFICATION",
                            "value"       => $row["CLASSIFICATION"],
                            "size"        => 4,
                            "maxlength"   => 6,
                            "extrahtml"   => "" ) );

        $arg["data"]["CLASSIFICATION"] = $objForm->ge("CLASSIFICATION");

        //学校名称１
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHOOLNAME1",
                            "value"       => $row["SCHOOLNAME1"],
                            "size"        => 60,
                            "maxlength"   => 90,
                            "extrahtml"   => "" ) );

        $arg["data"]["SCHOOLNAME1"] = $objForm->ge("SCHOOLNAME1");

        //学校名称２
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHOOLNAME2",
                            "value"       => $row["SCHOOLNAME2"],
                            "size"        => 60,
                            "maxlength"   => 90,
                            "extrahtml"   => "" ) );

        $arg["data"]["SCHOOLNAME2"] = $objForm->ge("SCHOOLNAME2");

        //学校名称３
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHOOLNAME3",
                            "value"       => $row["SCHOOLNAME3"],
                            "size"        => 60,
                            "maxlength"   => 90,
                            "extrahtml"   => "" ) );

        $arg["data"]["SCHOOLNAME3"] = $objForm->ge("SCHOOLNAME3");

        //学校名称英字
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHOOLNAME_ENG",
                            "value"       => $row["SCHOOLNAME_ENG"],
                            "size"        => 60,
                            "maxlength"   => 60,
                            "extrahtml"   => "" ) );

        $arg["data"]["SCHOOLNAME_ENG"] = $objForm->ge("SCHOOLNAME_ENG");

        //郵便番号
        $arg["data"]["SCHOOLZIPCD"] = View::popUpZipCode($objForm, "SCHOOLZIPCD", $row["SCHOOLZIPCD"],"SCHOOLADDR1");

        //住所１
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHOOLADDR1",
                            "value"       => $row["SCHOOLADDR1"],
                            "size"        => 50,
                            "maxlength"   => 75,
                            "extrahtml"   => "" ) );

        $arg["data"]["SCHOOLADDR1"] = $objForm->ge("SCHOOLADDR1");

        //住所２
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHOOLADDR2",
                            "value"       => $row["SCHOOLADDR2"],
                            "size"        => 50,
                            "maxlength"   => 75,
                            "extrahtml"   => "" ) );

        $arg["data"]["SCHOOLADDR2"] = $objForm->ge("SCHOOLADDR2");

        //住所１英字
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHOOLADDR1_ENG",
                            "value"       => $row["SCHOOLADDR1_ENG"],
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "" ) );

        $arg["data"]["SCHOOLADDR1_ENG"] = $objForm->ge("SCHOOLADDR1_ENG");

        //住所２英字
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHOOLADDR2_ENG",
                            "value"       => $row["SCHOOLADDR2_ENG"],
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "" ) );

        $arg["data"]["SCHOOLADDR2_ENG"] = $objForm->ge("SCHOOLADDR2_ENG");

        //電話番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHOOLTELNO",
                            "value"       => $row["SCHOOLTELNO"],
                            "size"        => 20,
                            "maxlength"   => 14,
                            "extrahtml"   => "" ) );

        $arg["data"]["SCHOOLTELNO"] = $objForm->ge("SCHOOLTELNO");

        //FAX番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHOOLFAXNO",
                            "value"       => $row["SCHOOLFAXNO"],
                            "size"        => 20,
                            "maxlength"   => 14,
                            "extrahtml"   => "" ) );

        $arg["data"]["SCHOOLFAXNO"] = $objForm->ge("SCHOOLFAXNO");

        //メールアドレス
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHOOLMAIL",
                            "value"       => $row["SCHOOLMAIL"],
                            "size"        => 30,
                            "maxlength"   => 25,
                            "extrahtml"   => "" ) );

        $arg["data"]["SCHOOLMAIL"] = $objForm->ge("SCHOOLMAIL");

        //ホームページ
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHOOLURL",
                            "value"       => $row["SCHOOLURL"],
                            "size"        => 30,
                            "maxlength"   => 30,
                            "extrahtml"   => "" ) );

        $arg["data"]["SCHOOLURL"] = $objForm->ge("SCHOOLURL");

        //学校区分
        $result = $db->query(knjtx001Query::getSchooldiv());
        $opt = array();
        while($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label"  => $row2["NAMECD2"].":".$row2["NAME1"],
                           "value"  => $row2["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "SCHOOLDIV",
                            "size"        => 1,
                            "extrahtml"   => "",
                            "value"       => $row["SCHOOLDIV"],
                            "options"     => $opt));

        $arg["data"]["SCHOOLDIV"] = $objForm->ge("SCHOOLDIV");

        //学期制
        $objForm->ae( array("type"        => "text",
                            "name"        => "SEMESTERDIV",
                            "value"       => $row["SEMESTERDIV"],
                            "size"        => 5,
                            "maxlength"   => 1,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ) );

        $arg["data"]["SEMESTERDIV"] = $objForm->ge("SEMESTERDIV");

        //学年数上限
        $objForm->ae( array("type"        => "text",
                            "name"        => "GRADE_HVAL",
                            "value"       => $row["GRADE_HVAL"],
                            "size"        => 5,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ) );

        $arg["data"]["GRADE_HVAL"] = $objForm->ge("GRADE_HVAL");

        //入学日付
        $arg["data"]["ENTRANCE_DATE"] = View::popUpCalendar($objForm, "ENTRANCE_DATE",
                                                            str_replace("-","/",$row["ENTRANCE_DATE"]),"");

        //卒業日付
        $arg["data"]["GRADUATE_DATE"] = View::popUpCalendar($objForm, "GRADUATE_DATE",
                                                            str_replace("-","/",$row["GRADUATE_DATE"]),"");

        //卒業単位数
        $objForm->ae( array("type"        => "text",
                            "name"        => "GRAD_CREDITS",
                            "value"       => $row["GRAD_CREDITS"],
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ) );

        $arg["data"]["GRAD_CREDITS"] = $objForm->ge("GRAD_CREDITS");

        //卒業履修単位数
        $objForm->ae( array("type"        => "text",
                            "name"        => "GRAD_COMP_CREDITS",
                            "value"       => $row["GRAD_COMP_CREDITS"],
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ) );

        $arg["data"]["GRAD_COMP_CREDITS"] = $objForm->ge("GRAD_COMP_CREDITS");

        //学期評価区分
        $opt = array();
        $result = $db->query(knjtx001Query::GetAssesscd1());

        $opt[] = array("label" => "0:標準（100段階）", "value" => "0");
        while($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label"  => $row2["ASSESSCD"].":".$row2["ASSESSMEMO"]."(".$row2["ASSESSLEVELCNT"]."段階)",
                           "value"  => $row2["ASSESSCD"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "SEMES_ASSESSCD",
                            "size"        => 1,
                            "extrahtml"   => "",
                            "value"       => $row["SEMES_ASSESSCD"],
                            "options"     => $opt));

        $arg["data"]["SEMES_ASSESSCD"] = $objForm->ge("SEMES_ASSESSCD");

        //学期保留値
        $objForm->ae( array("type"        => "text",
                            "name"        => "SEMES_FEARVAL",
                            "value"       => $row["SEMES_FEARVAL"],
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ) );

        $arg["data"]["SEMES_FEARVAL"] = $objForm->ge("SEMES_FEARVAL");

        //学年評価区分
        $arg["data"]["GRADE_ASSESSCD"] = $db->getOne(knjtx001Query::GetAssesscd2());

        //学年保留値
        $objForm->ae( array("type"        => "text",
                            "name"        => "GRADE_FEARVAL",
                            "value"       => $row["GRADE_FEARVAL"],
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ) );

        $arg["data"]["GRADE_FEARVAL"] = $objForm->ge("GRADE_FEARVAL");


        //評定計算方法
        $result = $db->query(knjtx001Query::getGvalCalc());
        $optGvalCalc = array();
        $optGvalCalc[] = array("label"  => "", "value"  => "");
        while($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optGvalCalc[] = array("label"  => $row2["NAME1"],
                                   "value"  => $row2["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "GVAL_CALC",
                            "size"        => 1,
                            "extrahtml"   => "",
                            "value"       => $row["GVAL_CALC"],
                            "options"     => $optGvalCalc));
        $arg["data"]["GVAL_CALC"] = $objForm->ge("GVAL_CALC");

        //欠席に含める（休学）
        $checked = strlen($row["SEM_OFFDAYS"]) ? "checked" : "";
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "SEM_OFFDAYS",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["SEM_OFFDAYS"] = $objForm->ge("SEM_OFFDAYS");

        //欠課に含める（休学）
        $checked = strlen($row["SUB_OFFDAYS"]) ? "checked" : "";
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "SUB_OFFDAYS",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["SUB_OFFDAYS"] = $objForm->ge("SUB_OFFDAYS");

        //欠課に含める（忌引）
        $checked = strlen($row["SUB_MOURNING"]) ? "checked" : "";
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "SUB_MOURNING",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["SUB_MOURNING"] = $objForm->ge("SUB_MOURNING");

        //欠課に含める（出停）
        $checked = strlen($row["SUB_SUSPEND"]) ? "checked" : "";
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "SUB_SUSPEND",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["SUB_SUSPEND"] = $objForm->ge("SUB_SUSPEND");

        //欠課に含める（出停伝染病）
        $checked = strlen($row["SUB_VIRUS"]) ? "checked" : "";
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "SUB_VIRUS",
                            "extrahtml"  => $checked,
                            "value"      => "1"));

        if($model->virus == 'true') {
            $arg["data"]["SUB_VIRUS"] = $objForm->ge("SUB_VIRUS");
            $arg["data"]["SUB_VIRUS_NAME"] = '出停伝染病&nbsp;';
            $arg["data"]["Br1"] = '';
            $arg["data"]["Br2"] = '<br>';
        } else {
            $arg["data"]["Br1"] = '<br>';
            $arg["data"]["Br2"] = '';
        }

        //欠課に含める（公欠）
        $checked = strlen($row["SUB_ABSENT"]) ? "checked" : "";
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "SUB_ABSENT",
                            "extrahtml"  => $checked,
                            "value"      => "1"));
        $arg["data"]["SUB_ABSENT"] = $objForm->ge("SUB_ABSENT");

        //１日出欠席の判定方法
        $checked = strlen($row["SYUKESSEKI_HANTEI_HOU"]) ? "checked" : "";
        $objForm->ae( array("type"       => "checkbox",
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
        $objForm->ae( array("type"        => "select",
                            "name"        => "ABSENT_COV",
                            "size"        => 1,
                            "extrahtml"   => "onchange=\"return change_absent('{$absent_cov_late}', '{$amari_kuriage}');\"",
                            "value"       => $row["ABSENT_COV"],
                            "options"     => $opt));
        $arg["data"]["ABSENT_COV"] = $objForm->ge("ABSENT_COV");

        //欠課数換算テキスト
        $disabled = ($row["ABSENT_COV"] == '0') ? "disabled" : "";
        $objForm->ae( array("type"        => "text",
                            "name"        => "ABSENT_COV_LATE",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "value"       => $row["ABSENT_COV_LATE"],
                            "extrahtml"   => "$disabled . onblur=\"this.value=toInteger(this.value)\";" ) );
        $arg["data"]["ABSENT_COV_LATE"] = $objForm->ge("ABSENT_COV_LATE");

        //欠課数繰上テキスト
        $disabled = ($row["ABSENT_COV"] != '5') ? "disabled" : "";
        $objForm->ae( array("type"        => "text",
                            "name"        => "AMARI_KURIAGE",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "value"       => $row["AMARI_KURIAGE"],
                            "extrahtml"   => "$disabled . onblur=\"this.value=toInteger(this.value)\";" ) );
        $arg["data"]["AMARI_KURIAGE"] = $objForm->ge("AMARI_KURIAGE");

        Query::dbCheckIn($db);

        //特活欠課数換算
        $opt = array();
        $opt[] = array("label" => "1：二捨三入", "value" => 1);
        $opt[] = array("label" => "2：四捨五入", "value" => 2);
        $opt[] = array("label" => "3：切り上げ", "value" => 3);
        $opt[] = array("label" => "4：切り捨て", "value" => 4);
        $objForm->ae( array("type"        => "select",
                            "name"        => "TOKUBETU_KATUDO_KANSAN",
                            "size"        => 1,
                            "extrahtml"   => "",
                            "value"       => $row["TOKUBETU_KATUDO_KANSAN"],
                            "options"     => $opt));
        $arg["data"]["TOKUBETU_KATUDO_KANSAN"] = $objForm->ge("TOKUBETU_KATUDO_KANSAN");

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
        $extra = "onClick=\" wopen('".REQUESTROOT."/Z/KNJZ021/knjz021index.php?year={$model->year}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["btn_detail"] = knjCreateBtn($objForm, "btn_detail", "他条件設定", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $row["UPDATED"]);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjtx001Form1.html", $arg);
    }
}
?>
