<?php

require_once('for_php7.php');

class knjd126pForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("form1", "POST", "knjd126pindex.php", "", "form1");

        /* Add by Kaung for PC-Talker 2020-01-20 start */
        $arg["TITLE"] = "学年別観点入力のマウス入力画面";
        /* Add by Kaung for PC-Talker 2020-01-31 end */

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        if ($model->Properties["useConversionFlg"] == "1") {
            $arg["useConversionFlg"] = 1;
        } else {
            $arg["not_useConversionFlg"] = 1;
        }

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //校種一覧取得
            $schoolkind = $db->getCol(knjd126pQuery::getSchoolKindList($model, "cnt"));
            knjCreateHidden($objForm, "H_SCHOOL_KIND");
            knjCreateHidden($objForm, "SCHOOL_KIND_CNT", get_count($schoolkind));
            if (get_count($schoolkind) > 1) {
                //校種コンボ
                $query = knjd126pQuery::getSchoolKindList($model, "list");
                /* Edit by Kaung for PC-Talker 2020-01-20 start */
                $extra = "aria-label=\"校種\" id = \"SCHOOL_KIND\" onChange=\"current_cursor('SCHOOL_KIND');btn_submit('form1')\";";
                /* Edit by Kaung for PC-Talker 2020-01-31 end */
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

        //学期コンボ作成
        $setNameCd = "Z009";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->field["SCHOOL_KIND"]."09";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        $query = knjd126pQuery::getNameMst($setNameCd);
        /* Edit by Kaung for PC-Talker 2020-01-20 start */
        $extra = "aria-label=\"学期\" id = \"SEMESTER\" onchange=\"current_cursor('SEMESTER');return btn_submit('form1')\"";
        /* Edit by Kaung for PC-Talker 2020-01-31 end */
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //hidden
        knjCreateHidden($objForm, "H_SEMESTER");

        //学期(観点データ以外用)
        $model->field["SEMESTER2"] = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //年組コンボ作成
        $query = knjd126pQuery::getHrClass($model);
        /* Edit by Kaung for PC-Talker 2020-01-20 start */
        $extra = "aria-label=\"年組\" id = \"GRADE_HR_CLASS\" onchange=\"current_cursor('GRADE_HR_CLASS');return btn_submit('form1')\"";
        /* Edit by Kaung for PC-Talker 2020-01-31 end */
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_GRADE_HR_CLASS");

        //科目コンボ作成
        $query = knjd126pQuery::getSubclassMst($model->field["GRADE_HR_CLASS"], $model);
        /* Edit by Kaung for PC-Talker 2020-01-20 start */
        $extra = "aria-label=\"科目\" id = \"SUBCLASSCD\" onchange=\"current_cursor('SUBCLASSCD');return btn_submit('form1')\"";
        /* Edit by Kaung for PC-Talker 2020-01-31 end */
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_SUBCLASSCD");

        //入力選択ラジオボタン 1:マウス入力 2:手入力
        $opt_select = array(1, 2);
        $model->select = ($model->select == "") ? "1" : $model->select;
        /* Edit by Kaung for PC-Talker 2020-01-20 start */
        $extra = array("id=\"SELECT1\" onclick =\"current_cursor('SELECT1'); return btn_submit('select1');\"", "id=\"SELECT2\" onclick =\"current_cursor('SELECT2');return btn_submit('select2');\"");
        /* Edit by Kaung for PC-Talker 2020-01-31 end */
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

        //入力値選択ラジオボタン
        $query = knjd126pQuery::getKantenHyouka($model);
        $result = $db->query($query);
        $kantenArray = array();
        $opt_data = array();
        $kantenCnt = 1;
        $extra = array();
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
                $opt_data[] = $kantenCnt;
                $radioname = $kanten["NAME1"];
                $extra[] = "aria-label=\"値選択の".$radioname."\" id=\"TYPE_DIV{$kantenCnt}\"";
                $arg["TYPE_SHOW{$kantenCnt}"] = $kanten["NAME1"];
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
            $opt_data[] = $kantenCnt;
            $extra[] = "aria-label=\"値選択のF\" id=\"TYPE_DIV{$kantenCnt}\"";
            $arg["TYPE_SHOW{$kantenCnt}"] = "F";
        }
        $model->type_div = ($model->type_div == "") ? "1" : $model->type_div;
        $radioArray = knjCreateRadio($objForm, "TYPE_DIV", $model->type_div, $extra, $opt_data, get_count($opt_data));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        if (get_count($kantenArray) == 0) {
            if ($model->field["SEMESTER"] === '9') {
                $arg["close_win"] = "close_window1();";
            } else {
                $arg["close_win"] = "close_window2();";
            }
        }

        //管理者コントロール
        $admin_key = array();
        $result = $db->query(knjd126pQuery::getAdminContol($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[] = $row["CONTROL_CODE"];
        }
        $result->free();

        //観点コード(MAX5または6)
        $view_key = array();
        $view_cnt = 0;
        $view_html = "";
        if ($model->Properties["kantenHyouji"] !== '6') {
            $arg["kantenHyouji_5"] = "1";
            $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤");
            $result = $db->query(knjd126pQuery::selectViewcdQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $view_cnt++;
                if ($view_cnt > 5) {
                    break;
                }   //MAX5
                $view_key[$view_cnt] = $row["VIEWCD"];  //1,2,3,4,5
                //チップヘルプ
                $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
                knjCreateHidden($objForm, "VIEWCD".$view_cnt, $row["VIEWNAME"]);
            }
            $result->free();

            for ($i=0; $i<(5-get_count($view_key)); $i++) {
                $view_html .= "<th width=\"60\">&nbsp;</th>";
            }
        } else {
            $arg["kantenHyouji_6"] = "1";
            $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤","6" => "⑥");
            $result = $db->query(knjd126pQuery::selectViewcdQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $view_cnt++;
                if ($view_cnt > 6) {
                    break;
                }   //MAX6
                $view_key[$view_cnt] = $row["VIEWCD"];  //1,2,3,4,5,6
                //チップヘルプ
                $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
                knjCreateHidden($objForm, "VIEWCD".$view_cnt, $row["VIEWNAME"]);
            }
            $result->free();

            for ($i=0; $i<(6-get_count($view_key)); $i++) {
                $view_html .= "<th width=\"60\">&nbsp;</th>";
            }
        }
        $arg["view_html"] = $view_html;
        //評定用観点コード
        if ($view_cnt > 0) {
            $view_key[9] = substr($model->field["SUBCLASSCD"], 0, 2)."99";
        }

        //選択教科
        $electdiv = $db->getrow(knjd126pQuery::getClassMst($model->field["SUBCLASSCD"], $model->field["GRADE_HR_CLASS"], $model), DB_FETCHMODE_ASSOC);

        //選択教科と同様に英字で入力する科目コード取得
        if ($db->getOne(knjd126pQuery::getNameMstD065($model)) > 0) {
            $electdiv["ELECTDIV"] = 1;
        }

        //データ取得(JVIEWSTAT_RECORD_DAT)
        $arrJviewSchNo = array();
        $result = $db->query(knjd126pQuery::getJviewstatRecordDat($model, $view_key));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arrJviewSchNo[] = $row["SCHREGNO"];
        }

        //初期化
        $model->data = array();
        $counter = 0;
        $disable = "disabled";

        //一覧表示
        $result = $db->query(knjd126pQuery::selectQuery($model, $view_key));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //クラス-出席番(表示)
            $row["ATTENDNO"] = $row["HR_NAME"]."-".$row["ATTENDNO"]."番";

            //名前
            $row["NAME_SHOW"] = $row["SCHREGNO"]." ".$row["NAME_SHOW"];
            /* Add by Kaung for PC-Talker 2020-01-20 start */
            $inputname = $row["NAME_SHOW"];
            /* Add by Kaung for PC-Talker 2020-01-31 end */

            //異動情報
            $query = knjd126pQuery::getTransfer($model, $row["SCHREGNO"]);
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
                if ($code == "9" && $electdiv["ELECTDIV"] != "0") {
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

                if ($model->cmd == "conversion" && $code != "9") {
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
                            /* Edit by Kaung for PC-Talker 2020-01-20 start */
                            $extra = "aria-label=\"".$inputname."の評価\" STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this, '".$electdiv["ELECTDIV"]."');\"";
                            /* Edit by Kaung for PC-Talker 2020-01-31 end */
                            if ($model->maxValue == '0') {
                                $extra .= " disabled";
                            }
                            $len = $model->Properties["useHyoukaHyouteiFlg"] == '1' ? 3 : strlen($model->maxValue);
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
                    } else {
                        /* Edit by Kaung for PC-Talker 2020-01-20 start */
                        $extra = "aria-label=\"".$inputname."の観点別学習状況の".$code."\" STYLE=\"text-align: center\" readonly=\"readonly\" onClick=\"kirikae(this, 'STATUS".$code."-".$counter."','".$counter."');\" oncontextmenu=\"kirikae2(this, 'STATUS".$code."-".$counter."')\"; ";
                        /* Edit by Kaung for PC-Talker 2020-01-31 end */
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

        $dataArray = array();
        foreach ($kantenArray as $key => $val) {
            $dataArray[] = array("VAL"  => "\"javascript:setClickValue('".$key."')\"",
                                 "NAME" => $val["VAL"]);
        }

        $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999');\"");
        $arg["menuTitle"]["CLICK_VAL"] = "javascript:setClickValue('999')";
        foreach ($dataArray as $key => $val) {
            $setData["CLICK_NAME"] = $val["NAME"];
            $setData["CLICK_VAL"] = $val["VAL"];
            $arg["menu"][] = $setData;
        }

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

        //変換表取込ボタン
        $extra = "aria-label = \"変換表取込\" id = \"btn_conversion\" onclick=\"current_cursor('btn_conversion');return btn_submit('conversion');\"".$disable;
        $arg["btn_conversion"] = knjCreateBtn($objForm, "btn_conversion", "変換表取込", $extra);
        //更新ボタン
        $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? $disable : "disabled";
        /* Edit by Kaung for PC-Talker 2020-01-20 start */
        $extra = "aria-label = \"更新\" id = \"btn_update\" onclick=\"current_cursor('btn_update');return btn_submit('update', '".$electdiv["ELECTDIV"]."');\"".$disable;
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "aria-label = \"取消\" id = \"btn_reset\" onclick=\"current_cursor('btn_reset');return btn_submit('reset');\"".$disable;
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "aria-label = \"終了\" id = \"btn_back\" onclick=\"current_cursor('btn_back');closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
        //印刷ボタン
        $extra = "aria-label = \"印刷\" id = \"btn_print\" onclick=\"current_cursor('btn_print');return newwin('" . SERVLET_URL . "');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
        /* Edit by Kaung for PC-Talker 2020-01-31 end */

        //hidden作成
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJD126P");
        knjCreateHidden($objForm, "CTRL_Y", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_S", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_D", CTRL_DATE);
        knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
        knjCreateHidden($objForm, "MAXVALUE", $model->maxValue);

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
        //教科コード
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass = explode("-", $model->field["SUBCLASSCD"]);
            knjCreateHidden($objForm, "CLASSCD", $subclass[0].'-'.$subclass[1]);
        } else {
            knjCreateHidden($objForm, "CLASSCD", substr($model->field["SUBCLASSCD"], 0, 2));
        }
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "Z009", $setNameCd);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "useHyoukaHyouteiFlg", $model->Properties["useHyoukaHyouteiFlg"]);
        knjCreateHidden($objForm, "useRecordDat", $model->Properties["useRecordDat"]);

        if ($model->field["SEMESTER"] == "9") {
            knjCreateHidden($objForm, "useJviewStatus_NotHyoji", $model->Properties["useJviewStatus_NotHyoji_D028"]);
        } else {
            knjCreateHidden($objForm, "useJviewStatus_NotHyoji", $model->Properties["useJviewStatus_NotHyoji_D029"]);
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd126pForm1.html", $arg);
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
        $result = $db->query(knjd126pQuery::selectJviewnameAssessMst($model, $row["STATUS9"], $code));
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
