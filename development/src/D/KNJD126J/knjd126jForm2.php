<?php

require_once('for_php7.php');
class knjd126jForm2
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"]    = $objForm->get_start("form2", "POST", "knjd126jindex.php", "", "form2");
        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        if ($model->Properties["useConversionFlg"] == "1") {
            $arg["useConversionFlg"] = 1;
        } else {
            $arg["not_useConversionFlg"] = 1;
        }

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //校種一覧取得
            $schoolkind = $db->getCol(knjd126jQuery::getSchoolKindList($model, "cnt"));
            knjCreateHidden($objForm, "H_SCHOOL_KIND");
            knjCreateHidden($objForm, "SCHOOL_KIND_CNT", get_count($schoolkind));
            if (get_count($schoolkind) > 1) {
                //校種コンボ
                $query = knjd126jQuery::getSchoolKindList($model, "list");
                $extra = "onChange=\"btn_submit('form1')\";";
                makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
                $arg["useSchoolKindCmb"] = 1;
            } else {
                $model->field["SCHOOL_KIND"] = $schoolkind[0];
                knjCreateHidden($objForm, "SCHOOL_KIND", $schoolkind[0]);
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $model->field["SCHOOL_KIND"] = SCHOOLKIND;
            knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
        } else {
            knjCreateHidden($objForm, "SCHOOL_KIND");
        }

        //学期コンボ(観点データ用)
        $model->field["SEMESTER"] = (!$model->field["SEMESTER"]) ? CTRL_SEMESTER : $model->field["SEMESTER"];//初期値
        $opt_semes = array();
        $result = $db->query(knjd126jQuery::selectNamemstQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_semes[] = array("label" => $row["NAME1"],"value" => $row["NAMECD2"]);
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "SEMESTER",
                            "size"        => "1",
                            "value"       => $model->field["SEMESTER"],
                            "options"     => $opt_semes,
                            "extrahtml"   => "onChange=\"btn_submit('form2')\";"));
        $arg["SEMESTER"] = $objForm->ge("SEMESTER");
        //hidden
        knjCreateHidden($objForm, "H_SEMESTER");

        //学期(観点データ以外用)
        $model->field["SEMESTER2"] = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];//初期値

        //教科コンボ
        $opt_sbuclass = $opt_electdiv = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd126jQuery::selectSubclassQuery($model));
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_sbuclass[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]." ".$row["CLASSNAME"],"value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]);
                //選択教科の保管
                $opt_electdiv[$row["CLASSCD"].'-'.$row["SCHOOL_KIND"]] = $row["ELECTDIV"];
            }
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_sbuclass[] = array("label" => $row["CLASSCD"]." ".$row["CLASSNAME"],"value" => $row["CLASSCD"]);
                //選択教科の保管
                $opt_electdiv[$row["CLASSCD"]] = $row["ELECTDIV"];
            }
        }
        $result->free();
        $electdiv = ($model->field["CLASSCD"]) ? $opt_electdiv[$model->field["CLASSCD"]] : "0";
        $objForm->ae(array("type"        => "select",
                            "name"        => "CLASSCD",
                            "size"        => "1",
                            "value"       => $model->field["CLASSCD"],
                            "options"     => $opt_sbuclass,
                            "extrahtml"   => "onChange=\"btn_submit('form2')\";"));
        $arg["CLASSCD"] = $objForm->ge("CLASSCD");
        //hidden
        knjCreateHidden($objForm, "H_CLASSCD");

        //講座コンボ
        $opt_chair = array();
        $opt_chair[] = array("label" => "", "value" => "");
        $result = $db->query(knjd126jQuery::selectChairQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chair[] = array("label" => $row["CHAIRCD_SUBCLASS"]." ".$row["CHAIRNAME"],"value" => $row["CHAIRCD"].':'.$row["SUBCLASS_VALUE"]);
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "CHAIRCD",
                            "size"        => "1",
                            "value"       => $model->field["CHAIRCD_SUBCLASS"],
                            "options"     => $opt_chair,
                            "extrahtml"   => "onChange=\"btn_submit('form2')\";"
                           ));
        $arg["CHAIRCD"] = $objForm->ge("CHAIRCD");
        //hidden
        knjCreateHidden($objForm, "H_CHAIRCD");

        //選択教科と同様に英字で入力する科目コード取得
        if ($db->getOne(knjd126jQuery::getNameMstD065($model)) > 0) {
            $electdiv = 1;
        }

        //入力選択ラジオボタン 1:マウス入力 2:手入力
        $opt_select = array(1, 2);
        $model->select = ($model->select == "") ? "1" : $model->select;
        $extra = array("id=\"SELECT1\" onclick =\" return btn_submit('form1');\"", "id=\"SELECT2\" onclick =\" return btn_submit('form2');\"");
        $radioArray = knjCreateRadio($objForm, "SELECT", $model->select, $extra, $opt_select, get_count($opt_select));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //入力選択ラジオボタン 1:値選択 2:データクリア
        $opt_nyuryoku = array(1, 2);
        $model->nyuryoku = ($model->nyuryoku == "") ? "1" : $model->nyuryoku;
        $extra = array("id=\"NYURYOKU1\" onClick=\"myHidden()\"", "id=\"NYURYOKU2\" onClick=\"myHidden()\"");
        $radioArray = knjCreateRadio($objForm, "NYURYOKU", $model->nyuryoku, $extra, $opt_nyuryoku, get_count($opt_nyuryoku));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //入力値選択ラジオボタン 1:Ａ 2:Ｂ 3:Ｃ
        $opt_data = array(1, 2, 3);
        $model->type_div = ($model->type_div == "") ? "1" : $model->type_div;
        $extra = array("id=\"TYPE_DIV1\"", "id=\"TYPE_DIV2\"", "id=\"TYPE_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "TYPE_DIV", $model->type_div, $extra, $opt_data, get_count($opt_data));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //入力値選択ラジオボタン
        $query = knjd126jQuery::getKantenHyouka($model);
        $result = $db->query($query);
        $kantenArray = array();
        $kantenCnt = 1;
        $komoji = 0;
        $oomoji = 0;
        $model->nonVisualViewCd = "";
        while ($kanten = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //プロパティにセットされているコードは表示しない
            if (
                ($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"] == $kanten["NAMECD2"]) ||
                ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"] == $kanten["NAMECD2"])
            ) {
                $model->nonVisualViewCd = $kanten["ABBV1"];
            } else {
                $kantenArray[$kantenCnt]["VAL"] = $kanten["ABBV1"];
                if (preg_match("/^[a-z]+$/", $kanten["ABBV1"])) {
                    $komoji++;
                } else {
                    $oomoji++;
                }
                $kantenArray[$kantenCnt]["SHOW"] = $kanten["NAME1"];
                $kantenCnt++;
            }
        }
        $result->free();
        if (
            ($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"]) ||
            ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"])
        ) {
            $oomoji++;
            $kantenArray[$kantenCnt]["VAL"] = "F";
            $kantenArray[$kantenCnt]["SHOW"] = "F";
        }

        //管理者コントロール
        $admin_key = array();
        $result = $db->query(knjd126jQuery::selectContolCodeQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[] = $row["CONTROL_CODE"];
        }
        $result->free();

        //生徒を抽出する日付
        $sdate = str_replace("/", "-", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
        $edate = str_replace("/", "-", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
        $execute_date = ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) ? CTRL_DATE : $edate;//初期値

        //観点コード(MAX5または6)
        $view_key = array();
        $view_cnt = 0;
        $view_html = "";

        if ($model->Properties["kantenHyouji"] !== '6') {
            $arg["kantenHyouji_5"] = "1";
            $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤");
            $result = $db->query(knjd126jQuery::selectViewcdQuery($model, $execute_date));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $view_cnt++;
                if ($view_cnt > 5) {
                    break;
                }//MAX5
                $view_key[$view_cnt] = $row["VIEWCD"];//1,2,3,4,5
                //チップヘルプ
                $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
                $objForm->ae(array("type"      => "hidden",
                                    "name"      => "VIEWCD".$view_cnt,
                                    "value"     => $row["VIEWNAME"] ));
            }
            $result->free();
            for ($i=0; $i<(5-get_count($view_key)); $i++) {
                $view_html .= "<th width=\"60\">&nbsp;</th>";
            }
        } else {
            $arg["kantenHyouji_6"] = "1";
            $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤","6" => "⑥");
            $result = $db->query(knjd126jQuery::selectViewcdQuery($model, $execute_date));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $view_cnt++;
                if ($view_cnt > 6) {
                    break;
                }//MAX6
                $view_key[$view_cnt] = $row["VIEWCD"];//1,2,3,4,5,6
                //チップヘルプ
                $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
                $objForm->ae(array("type"      => "hidden",
                                    "name"      => "VIEWCD".$view_cnt,
                                    "value"     => $row["VIEWNAME"] ));
            }
            $result->free();
            for ($i=0; $i<(6-get_count($view_key)); $i++) {
                $view_html .= "<th width=\"60\">&nbsp;</th>";
            }
        }
        $arg["view_html"] = $view_html;

        //評定がこの画面で入力可能で、かつKNJD126J_useCtlHyouteiプロパティが立っている時のみ、表示する
        if ($model->Properties["KNJD126J_useCtlHyoutei"] == "1" && $model->Properties["displayHyoutei"] != "1" && $model->Properties["displayHyoutei"] != "2") {
            $arg["notReflectChk"] = "1";
            if ($model->Properties["useCurriculumcd"] == '1') {
                if ($view_cnt > 0) {
                    $view_key[8] = substr($model->field["CLASSCD"], 0, 2)."98";
                }
            } else {
                if ($view_cnt > 0) {
                    $view_key[8] = $model->field["CLASSCD"]."98";
                }//8
            }
        } else {
            $arg["ReflectChk"] = "1";
        }

        //評定用観点コード
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($view_cnt > 0) {
                $view_key[9] = substr($model->field["CLASSCD"], 0, 2)."99";
            }
        } else {
            if ($view_cnt > 0) {
                $view_key[9] = $model->field["CLASSCD"]."99";
            }//9
        }

        //データ取得(JVIEWSTAT_RECORD_DAT)
        $arrJviewSchNo = array();
        $result = $db->query(knjd126jQuery::getJviewstatRecordDat($model, $view_key));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arrJviewSchNo[] = $row["SCHREGNO"];
        }

        //初期化
        $model->data=array();
        $counter = 0;
        $disable = "disabled";

        //一覧表示
        $result = $db->query(knjd126jQuery::selectQuery($model, $execute_date, $view_key));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //D085に登録のない場合はデフォルトを5とする。
            if (strlen($model->maxValue[$row["GRADE"]]) == 0) {
                $model->maxValue[$row["GRADE"]] = "5";
            }
            //hidden
            knjCreateHidden($objForm, "CHECK_MAXVAL_{$counter}", $model->maxValue[$row["GRADE"]]);

            //クラス-出席番(表示)
            $row["ATTENDNO"] = $row["HR_NAME"]."-".$row["ATTENDNO"];

            //名前
            $row["NAME_SHOW"]   = $row["SCHREGNO"]." ".$row["NAME_SHOW"];

            //異動情報
            $query = knjd126jQuery::getTransfer($model, $row["SCHREGNO"]);
            $transCnt = $db->getOne($query);
            $setColor = "#ffffff";
            if ($transCnt > 0) {
                $setColor = "#ffff00";
            }

            //各項目を作成
            foreach ($view_key as $code => $col) {
                if ($code != "9") {
                    if (
                        ($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"]) ||
                        ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"])
                    ) {
                        if (in_array($row["SCHREGNO"], $arrJviewSchNo)) {
                            //JVIEWSTAT_RECORD_DATにデータがある人のみブランクを"F"に切り替え
                            $row["STATUS".$code] = ($row["STATUS".$code] == "") ? "F": $row["STATUS".$code];
                        }
                        //プロパティにセットされているコードは表示しない
                        $row["STATUS".$code] = ($row["STATUS".$code] == $model->nonVisualViewCd) ? "": $row["STATUS".$code];
                    }
                }

                //選択教科の評定は、A,B,Cに変換
                if ($code == "9" && $electdiv != "0") {
                    $status = $row["STATUS".$code];
                    if ($status == "11") {
                        $status = "A";
                    }
                    if ($status == "22") {
                        $status = "B";
                    }
                    if ($status == "33") {
                        $status = "C";
                    }
                    $row["STATUS".$code] = $status;
                }

                if ($model->cmd == "form2_conversion" && $code != "9") {
                    //変換表取込
                    conversion($model, $db, $row, $code, $counter);
                }

                //管理者コントロール
                if (in_array($model->field["SEMESTER"], $admin_key) && DEF_UPDATE_RESTRICT <= AUTHORITY) {
                    //各観点コードを取得
                    $model->data["STATUS"][$code] = $col;

                    if ($code == "9") {
                        //評定はプロパティが1または2以外のときは評定は入力可
                        if ($model->Properties["displayHyoutei"] != "1" && $model->Properties["displayHyoutei"] != "2") {
                            $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc';\" onblur=\"calc(this, '".$electdiv."', '".$row["GRADE"]."');\" onPaste=\"return showPaste(this);\" id=\"STATUS{$code}-{$counter}\"";
                            if ($model->maxValue[$row["GRADE"]] == '0') {
                                $extra .= " disabled";
                            }
                            $len = $model->Properties["useHyoukaHyouteiFlg"] == '1' ? 3 : strlen($model->maxValue[$row["GRADE"]]);
                            $row["STATUS".$code] = knjCreateTextBox($objForm, $row["STATUS".$code], "STATUS".$code."-".$counter, 3, $len, $extra);
                        } else {
                            if ($model->Properties["displayHyoutei"] == "2") {
                                //プロパティが2のとき、評定は非表示
                                $row["STATUS".$code] = "";
                            } else {
                                $row["STATUS".$code] = $row["STATUS".$code];
                                //hidden
                                knjCreateHidden($objForm, "STATUS".$code."-".$counter, $row["STATUS".$code]);
                            }
                        }
                    } elseif ($code == "8") {
                        $extra = " id=\"STATUS".$code."-".$counter."\"".($row["STATUS".$code] ? " checked" : "");
                        $row["STATUS".$code] = knjCreateCheckBox($objForm, "STATUS".$code."-".$counter, "1", $extra);
                    } else {
                        $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc';document.forms[0].elements['STATUS9-{$counter}'].value='';\" onblur=\"calc(this, '".$electdiv."', '".$row["GRADE"]."');\" onPaste=\"return showPaste(this);\" id=\"STATUS{$code}-{$counter}\"";
                        $row["STATUS".$code] = knjCreateTextBox($objForm, $row["STATUS".$code], "STATUS".$code."-".$counter, 3, 1, $extra);
                    }

                    //更新ボタンのＯＮ／ＯＦＦ
                    $disable = "";

                //ラベルのみ
                } else {
                    $row["STATUS".$code] = "<font color=\"#000000\">".$row["STATUS".$code]."</font>";
                }
            }

            $row["COLOR"] = $setColor;

            $counter++;
            $arg["data"][] = $row;
        }
        $result->free();

        // 評価・評定の文言
        if ($model->Properties["displayHyoutei"] == "2") {
            //プロパティが2のとき、評定は非表示
            $arg["HyoukaHyouteiWord"] = "";
        } else {
            $arg["HyoukaHyouteiWord"] = "評定";
            if ($model->field["SEMESTER"] != '9') {
                $arg["HyoukaHyouteiWord"] = "評価（5段階）";
            }
            if ($model->Properties["useHyoukaHyouteiFlg"] == '1') {
                $arg["HyoukaHyouteiWord"] = "評価（5段階）";
            } elseif ($model->Properties["useHyoukaHyouteiFlg"] == '2') {
                $arg["HyoukaHyouteiWord"] = "仮評定";
            } elseif ($model->field["SEMESTER"] != '9') {
                $arg["HyoukaHyouteiWord"] = "評価";
            }
        }

        //ボタン
        $extra = "onclick=\"return btn_submit('update', '".$electdiv."');\"".$disable;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);


        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset2');\"" ));
        $arg["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae(array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["btn_end"] = $objForm->ge("btn_end");

        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "印 刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ));
        $arg["btn_print"] = $objForm->ge("btn_print");
        //変換表取込ボタン
        $extra = "onclick=\"return btn_submit('form2_conversion');\"".$disable;
        $arg["btn_conversion"] = knjCreateBtn($objForm, "btn_conversion", "変換表取込", $extra);

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD126J" ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "CTRL_Y",
                            "value"     => CTRL_YEAR ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SEMESTER2",
                            "value"     => $model->field["SEMESTER2"] ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "CTRL_D",
                            "value"     => $execute_date ));

        //クリップボードの中身のチェック用
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "ELECTDIV",
                            "value"     => $electdiv ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "useTestCountflg",
                            "value"     => $model->Properties["useTestCountflg"] ));

        $hiddenSetVal = "";
        $hiddenSetShow = "";
        $hiddenSetCheck = "";
        $sep = "";
        foreach ($kantenArray as $key => $val) {
            $hiddenSetVal .= $sep.$key;
            $hiddenSetShow .= $sep.$val["VAL"];
            $sep = ",";
        }
        knjCreateHidden($objForm, "SETVAL", $hiddenSetVal);
        knjCreateHidden($objForm, "SETSHOW", $hiddenSetShow);
        if ($komoji > 0 && $oomoji > 0) {
            $setHenkan = "3";
        } elseif ($komoji > 0) {
            $setHenkan = "2";
        } else {
            $setHenkan = "1";
        }
        knjCreateHidden($objForm, "HENKAN_TYPE", $setHenkan);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //観点数設定
        knjCreateHidden($objForm, "kantenHyouji", $model->Properties["kantenHyouji"]);
        knjCreateHidden($objForm, "kantenHyouji_5", $arg["kantenHyouji_5"]);
        knjCreateHidden($objForm, "kantenHyouji_6", $arg["kantenHyouji_6"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "useHyoukaHyouteiFlg", $model->Properties["useHyoukaHyouteiFlg"]);

        if (is_array($model->maxValue)) {
            if (get_count($model->maxValue) > 1) {
                $errMessage = "";
                $errSep = "\n";
                foreach ($model->maxValue as $grade => $maxVal) {
                    $errMessage .= $errSep.$grade."学年は、「1～{$maxVal}」";
                }
            } else {
                $errMessage = "「1～{$maxVal}」";
            }
            $errMessage .= "を入力して下さい。";
            knjCreateHidden($objForm, "errMsg", $errMessage);
            foreach ($model->maxValue as $grade => $maxVal) {
                knjCreateHidden($objForm, "MAXVALUE_{$grade}", $maxVal);
            }
        }

        $setNameCd = "Z009";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->field["SCHOOL_KIND"]."09";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        knjCreateHidden($objForm, "Z009", $setNameCd);

        if ($model->field["SEMESTER"] == "9") {
            knjCreateHidden($objForm, "useJviewStatus_NotHyoji", $model->Properties["useJviewStatus_NotHyoji_D028"]);
        } else {
            knjCreateHidden($objForm, "useJviewStatus_NotHyoji", $model->Properties["useJviewStatus_NotHyoji_D029"]);
        }

        //エンター押下時の移動方向
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"MOVE_ENTER{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd126jForm2.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => "", 'value' => "");
    }
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

//変換表取込
function conversion($model, $db, &$row, $code, $counter)
{
    //初期化
    $row["STATUS".$code] = "";

    $row["STATUS9"] = $model->fields["STATUS9"][$counter];
    if ($row["STATUS9"] != "") {
        //変換表の取得
        $result = $db->query(knjd126jQuery::selectJviewnameAssessMst($model, $row["GRADE"], $row["STATUS9"], $code));
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $status = $row2["STATUS"];
            if ($status == "11") {
                $status = "A";
            }
            if ($status == "22") {
                $status = "B";
            }
            if ($status == "33") {
                $status = "C";
            }
            $row2["STATUS"] = $status;
            $row["STATUS".$code] = $row2["STATUS"];
        }
    }
}
