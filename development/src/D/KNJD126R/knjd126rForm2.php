<?php

require_once('for_php7.php');

class knjd126rForm2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd126rindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $setNameCd = "Z009";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        $query = knjd126rQuery::getNameMst($setNameCd);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //hidden
        knjCreateHidden($objForm, "H_SEMESTER");

        //学期(観点データ以外用)
        $model->field["SEMESTER2"] = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //教科コンボ
        $opt_subclass = $opt_electdiv = array();
        $opt_subclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd126rQuery::selectSubclassQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_subclass[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            //選択教科の保管
            $opt_electdiv[$row["VALUE"]] = $row["ELECTDIV"];
        }
        $result->free();
        $extra = "onChange=\"btn_submit('form2')\";";
        $arg["CLASSCD"] = knjCreateCombo($objForm, "CLASSCD", $model->field["CLASSCD"], $opt_subclass, $extra, 1);
        //hidden
        knjCreateHidden($objForm, "H_CLASSCD");

        //選択教科
        $electdiv["ELECTDIV"] = ($model->field["CLASSCD"]) ? $opt_electdiv[$model->field["CLASSCD"]] : "0";

        //講座コンボ
        $query = knjd126rQuery::selectChairQuery($model);
        $extra = "onChange=\"btn_submit('form2')\";";
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD_SUBCLASS"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_CHAIRCD");

        //選択教科と同様に英字で入力する科目コード取得
        if ($db->getOne(knjd126rQuery::getNameMstD065($model)) > 0) {
            $electdiv["ELECTDIV"] = 1;
        }
        
        //校種
        if($model->field["CLASSCD"] != ''){
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
            $model->schoolKind = $class_array[1];
        } else {
            $model->schoolKind = '';
        }

        //生徒を抽出する日付
        $sdate = str_replace("/","-",$model->control["学期開始日付"][$model->field["SEMESTER"]]);
        $edate = str_replace("/","-",$model->control["学期終了日付"][$model->field["SEMESTER"]]);
        $execute_date = ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) ? CTRL_DATE : $edate;//初期値
        
        $view_key = array();
        $query = knjd126rQuery::selectViewcdQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $view_cnt++;
            if ($view_cnt > $maxCnt) break;
            $view_key[$view_cnt] = $row["VIEWCD"];
        }
        //評定用観点コード
        if ($view_cnt > 0) $view_key[9] = $model->field["CLASSCD"]."99";


        $query = knjd126rQuery::selectQuery($model, $execute_date, $view_key);
        $result = $db->query($query);
        $grade = '';
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($grade == '') {
                $grade = $row['GRADE'];
            }
            if ($grade != $row['GRADE']) {
                $arg["jscript"] = "OnGradeError();";
            }
        }
        $model->grade = $grade;

        //パターン
        $query = knjd126rQuery::selectPatternData($model);
        $result = $db->query($query);
        $model->viewPerfect = array();
        $model->viewLevel = array();
        $model->viewLevelMarkArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!$model->viewPerfect[$row["VIEWCD"]]) {
                knjCreateHidden($objForm, "PERFECT_".$row["VIEWCD"], $row["PERFECT"]);
            }
            $model->viewPerfect[$row["VIEWCD"]] = $row["PERFECT"];
            $model->viewLevel[$row["VIEWCD"]][$row["ASSESSLEVEL"]] = $row;
            $model->viewLevelMarkArray[$row["VIEWCD"]][$row["ASSESSMARK"]] = $row;
        }
        $result->free();

        //入力選択ラジオボタン 1:マウス入力 2:手入力
        $opt_select = array(1, 2);
        $model->select = ($model->select == "") ? "1" : $model->select;
        $extra = array("id=\"SELECT1\" onclick =\" return btn_submit('select1');\"", "id=\"SELECT2\" onclick =\" return btn_submit('select2');\"");
        $radioArray = knjCreateRadio($objForm, "SELECT", $model->select, $extra, $opt_select, get_count($opt_select));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //管理者コントロール
        $admin_key = array();
        $query = knjd126rQuery::getAdminContol($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[] = $row["CONTROL_CODE"];
        }
        $result->free();

        //観点コード(MAX5または6)
        $view_key = array();
        $view_cnt = 0;
        $view_html = "";
        if ($model->Properties["kantenHyouji"] !== '6') {
            $maxCnt = 5;
            $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤");
        } else {
            $maxCnt = 6;
            $view_html_no = array("1" => "①","2" => "②","3" => "③","4" => "④","5" => "⑤","6" => "⑥");
        }
        $arg["kantenHyouji_".$maxCnt] = "1";
        $query = knjd126rQuery::selectViewcdQuery($model);
        $result = $db->query($query);
        $isPerfectNoSet = false;
        $setViewCdHidden = "";
        $setViewCdHiddenSep = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $view_cnt++;
            if ($view_cnt > $maxCnt) break;
            $view_key[$view_cnt] = $row["VIEWCD"];
            //チップヘルプ
            $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
            knjCreateHidden($objForm, "VIEWCD".$view_cnt, $row["VIEWNAME"]);
            if (!$model->viewPerfect[$row["VIEWCD"]]) {
                $isPerfectNoSet = true;
            }
            $setViewCdHidden .= $setViewCdHiddenSep.$row["VIEWCD"];
            $setViewCdHiddenSep = ",";
        }
        for ($i=0; $i < ($maxCnt - get_count($view_key)); $i++) $view_html .= "<th width=\"60\">&nbsp;</th>";

        //hidden
        knjCreateHidden($objForm, "ALL_VIEWCD", $setViewCdHidden);

        $arg["view_html"] = $view_html;
        //評定用観点コード
        if ($view_cnt > 0) $view_key[9] = $model->field["CLASSCD"]."99";

        //初期化
        $model->data = array();
        $counter = 0;
        $disable = "disabled";
        
        //一覧表示
        $query = knjd126rQuery::selectQuery($model, $execute_date, $view_key);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //クラス-出席番(表示)
            $row["ATTENDNO"] = $row["HR_NAME"]."-".$row["ATTENDNO"]."番";

            //名前
            $row["NAME_SHOW"] = $row["SCHREGNO"]." ".$row["NAME_SHOW"];

            knjCreateHidden($objForm, "SCH-".$counter, $row["ATTENDNO"]." ".$row["NAME_SHOW"]);

            //異動情報
            $query = knjd126rQuery::getTransfer($model, $row["SCHREGNO"]);
            $transCnt = $db->getOne($query);
            $setColor = "#ffffff";
            if ($transCnt > 0) {
                $setColor = "#ffff00";
            }

            //各項目を作成
            foreach ($view_key as $code => $col) {
                $status = $row["STATUS".$code];
                $row["STATUS".$code] = $status;

                //管理者コントロール
                if (in_array($model->field["SEMESTER"], $admin_key) && DEF_UPDATE_RESTRICT <= AUTHORITY) {
                    //各観点コードを取得
                    $model->data["STATUS"][$code] = $col;
                    if ($model->Properties["displayHyoutei"] != "1") {
                        if ($code != "9") {
                            $extra  = "STYLE=\"text-align: center\" onPaste=\"return showPaste(this);\" id=\"STATUS{$code}-{$counter}\"";
                        } else {
                            $extra = "style=\"text-align:center\" onPaste=\"return showPaste(this);\" onblur=\"this.value=toInteger(this.value);\"";
                        }
                        $row["STATUS".$code] = knjCreateTextBox($objForm, $row["STATUS".$code], "STATUS".$code."-".$counter, 4, 4, $extra);
                    } else {
                        if ($code != "9") {
                            $extra  = "STYLE=\"text-align: center\" onPaste=\"return showPaste(this);\" id=\"STATUS{$code}-{$counter}\"";
                            $row["STATUS".$code] = knjCreateTextBox($objForm, $row["STATUS".$code], "STATUS".$code."-".$counter, 4, 4, $extra);
                        } else {
                            $row["STATUS".$code] = $row["STATUS".$code];
                            knjCreateHidden($objForm, "STATUS".$code."-".$counter, $row["STATUS".$code]);
                        }
                    }

                    if ($code == "9") {
                        knjCreateHidden($objForm, "STATUS".$code."-".$counter."-HIDDEN");
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

        //右クリックリスト
        $query = knjd126rQuery::getClickList($model);
        $result = $db->query($query);
        $dataArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataArray[$row["VIEWCD"]][] = array("VAL"  => "\"javascript:setClickValue('{$row["ASSESSLEVEL"]}', '{$row["ASSESSMARK"]}')\"",
                                                 "NAME" => $row["ASSESSMARK"]);
        }
        $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999', '');\"");
        $arg["menuTitle"]["CLICK_VAL"] = "javascript:setClickValue('999')";
        foreach ($dataArray as $menuKey => $menuVal) {
            $setData["setID"] = $menuKey;
            $dataCnt = get_count($menuVal);
            for ($menuCnt = 0; $menuCnt < $dataCnt; $menuCnt++) {
                $val = $menuVal[$menuCnt];
                $setData["CLICK_NAME"] = $val["NAME"];
                $setData["CLICK_VAL"] = $val["VAL"];
                if ($menuCnt + 1 == $dataCnt) {
                    $setData["lastID"] = $menuKey;
                }
                $arg["menu"][] = $setData;
                $setData = array();
            }
        }
        $result->free();

        //更新ボタン
        $disabled = "";
        if ($model->field["CHAIRCD_SUBCLASS"] && $isPerfectNoSet) {
            $disabled = " disabled ";
        } else if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $disabled = $disable;
        } else {
            $disabled = " disabled ";
        }
        $extra = " onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('form2');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$disabled);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra.$disabled);

        //hidden作成
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJD126S");
        knjCreateHidden($objForm, "CTRL_Y", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_S", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_D", CTRL_DATE);
        knjCreateHidden($objForm, "ELECTDIV", $electdiv["ELECTDIV"]);
        knjCreateHidden($objForm, "SETVAL", $hiddenVal);
        knjCreateHidden($objForm, "SETSHOW", $hiddenShow);

        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //観点数設定
        knjCreateHidden($objForm, "kantenHyouji", $model->Properties["kantenHyouji"]);
        knjCreateHidden($objForm, "kantenHyouji_5", $arg["kantenHyouji_5"]);
        knjCreateHidden($objForm, "kantenHyouji_6", $arg["kantenHyouji_6"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "Z009", $setNameCd);

        //DB切断
        Query::dbCheckIn($db);

        if ($model->field["CHAIRCD_SUBCLASS"] && $isPerfectNoSet) {
            $arg["close_win"] = "alert('満点設定等が未完了です。');";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd126rForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
