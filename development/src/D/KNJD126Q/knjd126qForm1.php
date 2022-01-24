<?php

require_once('for_php7.php');

class knjd126qForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd126qindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $setNameCd = "Z009";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        $query = knjd126qQuery::getNameMst($setNameCd);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //hidden
        knjCreateHidden($objForm, "H_SEMESTER");

        //学期(観点データ以外用)
        $model->field["SEMESTER2"] = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //年組コンボ作成
        $query = knjd126qQuery::getHrClass($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_GRADE_HR_CLASS");

        //校種
        $query = knjd126qQuery::getSchoolKind($model);
        $model->schoolKind = $db->getOne($query);

        //パターン
        $query = knjd126qQuery::selectPatternData($model);
        $result = $db->query($query);
        $model->viewPerfect = array();
        $model->viewLevel = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!$model->viewPerfect[$row["VIEWCD"]]) {
                knjCreateHidden($objForm, "PERFECT_".$row["VIEWCD"], $row["PERFECT"]);
            }
            $model->viewPerfect[$row["VIEWCD"]] = $row["PERFECT"];
            $model->viewLevel[$row["VIEWCD"]][$row["ASSESSLEVEL"]] = $row;
        }
        $result->free();

        //科目コンボ作成
        $query = knjd126qQuery::getSubclassMst($model->field["GRADE_HR_CLASS"], $model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_SUBCLASSCD");

        //管理者コントロール
        $admin_key = array();
        $query = knjd126qQuery::getAdminContol($model);
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
        $query = knjd126qQuery::selectViewcdQuery($model);
        $result = $db->query($query);
        $isPerfectNoSet = false;
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
        }
        for ($i=0; $i < ($maxCnt - get_count($view_key)); $i++) $view_html .= "<th width=\"60\">&nbsp;</th>";

        $arg["view_html"] = $view_html;
        //評定用観点コード
        if ($view_cnt > 0) $view_key[9] = substr($model->field["SUBCLASSCD"], 0, 2)."99";

        //選択教科
        $electdiv = $db->getrow(knjd126qQuery::getClassMst($model->field["SUBCLASSCD"], $model->field["GRADE_HR_CLASS"], $model), DB_FETCHMODE_ASSOC);

        //選択教科と同様に英字で入力する科目コード取得
        if ($db->getOne(knjd126qQuery::getNameMstD065($model)) > 0) {
            $electdiv["ELECTDIV"] = 1;
        }

        //初期化
        $model->data = array();
        $counter = 0;
        $disable = "disabled";

        //一覧表示
        $query = knjd126qQuery::selectQuery($model, $view_key);
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
            $query = knjd126qQuery::getTransfer($model, $row["SCHREGNO"]);
            $transCnt = $db->getOne($query);
            $setColor = "#ffffff";
            if ($transCnt > 0) {
                $setColor = "#ffff00";
            }

            //各項目を作成
            foreach ($view_key as $code => $col) {
                $score = $row["SCORE".$code];
                $row["SCORE".$code] = $score;

                //管理者コントロール
                if (in_array($model->field["SEMESTER"], $admin_key) && DEF_UPDATE_RESTRICT <= AUTHORITY) {
                    //各観点コードを取得
                    $model->data["SCORE"][$code] = $col;
                    if ($model->Properties["displayHyoutei"] != "1") {
                        $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"checkPerfect(this, '{$col}');\" onPaste=\"return showPaste(this);\" id=\"SCORE{$code}-{$counter}\"";
                        $row["SCORE".$code] = knjCreateTextBox($objForm, $row["SCORE".$code], "SCORE".$code."-".$counter, 3, 3, $extra);
                    } else {
                        if ($code != "9") {
                            $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"checkPerfect(this, '{$col}');\" onPaste=\"return showPaste(this);\" id=\"SCORE{$code}-{$counter}\"";
                            $row["SCORE".$code] = knjCreateTextBox($objForm, $row["SCORE".$code], "SCORE".$code."-".$counter, 3, 3, $extra);
                        } else {
                            $row["SCORE".$code] = $row["SCORE".$code];
                            knjCreateHidden($objForm, "SCORE".$code."-".$counter, $row["SCORE".$code]);
                        }
                    }

                    if ($code == "9") {
                        knjCreateHidden($objForm, "SCORE".$code."-".$counter."-HIDDEN");
                    }

                    //更新ボタンのＯＮ／ＯＦＦ
                    $disable = "";

                //ラベルのみ
                } else {
                    $row["SCORE".$code] = "<font color=\"#000000\">".$row["SCORE".$code]."</font>";
                }
            }

            $row["COLOR"] = $setColor;

            $counter++;
            $arg["data"][] = $row;
        }

        //更新ボタン
        $disabled = "";
        if ($model->field["SUBCLASSCD"] && $isPerfectNoSet) {
            $disabled = " disabled ";
        } else if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $disabled = $disable;
        } else {
            $disabled = " disabled ";
        }
        $extra = " onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
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
        knjCreateHidden($objForm, "PRGID", "KNJD126Q");
        knjCreateHidden($objForm, "CTRL_Y", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_S", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_D", CTRL_DATE);
        knjCreateHidden($objForm, "ELECTDIV", $electdiv["ELECTDIV"]);

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

        //DB切断
        Query::dbCheckIn($db);

        if ($model->field["SUBCLASSCD"] && $isPerfectNoSet) {
            $arg["close_win"] = "alert('満点設定等が未完了です。');";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd126qForm1.html", $arg);
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
