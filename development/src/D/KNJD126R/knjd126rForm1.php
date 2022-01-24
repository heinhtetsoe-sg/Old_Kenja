<?php

require_once('for_php7.php');

class knjd126rForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd126rindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            //校種一覧取得
            $schoolkind = $db->getCol(knjd126rQuery::getSchoolKindList($model, "cnt"));
            if (get_count($schoolkind) > 1) {
                //校種コンボ
                $query = knjd126rQuery::getSchoolKindList($model, "list");
                $extra = "onChange=\"btn_submit('main')\";";
                makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
                knjCreateHidden($objForm, "H_SCHOOL_KIND");
                $arg["useSchoolKindCmb"] = 1;
            } else {
                $model->field["SCHOOL_KIND"] = $schoolkind[0];
                knjCreateHidden($objForm, "SCHOOL_KIND", $schoolkind[0]);
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $model->field["SCHOOL_KIND"] = SCHOOLKIND;
            knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
        } else {
            knjCreateHidden($objForm, "SCHOOL_KIND");
        }

        //学期コンボ(観点データ用)
        $model->field["SEMESTER"] = (!$model->field["SEMESTER"]) ? CTRL_SEMESTER : $model->field["SEMESTER"];//初期値
        $opt_semes = array();
        $result = $db->query(knjd126rQuery::selectNamemstQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_semes[] = array("label" => $row["NAME1"],"value" => $row["NAMECD2"]);
        }
        $result->free();
        $extra = "onChange=\"btn_submit('main')\";";
        $arg["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt_semes, $extra, 1);
        //hidden
        knjCreateHidden($objForm, "H_SEMESTER");

        //生徒を抽出する日付
        $sdate = str_replace("/","-",$model->control["学期開始日付"][$model->field["SEMESTER"]]);
        $edate = str_replace("/","-",$model->control["学期終了日付"][$model->field["SEMESTER"]]);
        $execute_date = ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) ? CTRL_DATE : $edate;

        //学期(観点データ以外用)
        $model->field["SEMESTER2"] = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];//初期値

        //教科コンボ
        $opt_sbuclass = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd126rQuery::selectSubclassQuery($model));
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_sbuclass[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]." ".$row["CLASSNAME"],"value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]);
            }
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_sbuclass[] = array("label" => $row["CLASSCD"]." ".$row["CLASSNAME"],"value" => $row["CLASSCD"]);
            }
        }
        $result->free();
        $extra = "onChange=\"btn_submit('main')\";";
        $arg["CLASSCD"] = knjCreateCombo($objForm, "CLASSCD", $model->field["CLASSCD"], $opt_sbuclass, $extra, 1);
        //hidden
        knjCreateHidden($objForm, "H_CLASSCD");

        //講座コンボ
        $opt_chair = array();
        $opt_chair[] = array("label" => "", "value" => "");
        $result = $db->query(knjd126rQuery::selectChairQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chair[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],"value" => $row["CHAIRCD"].':'.$row["SUBCLASS_VALUE"]);
        }
        $result->free();
        $extra = "onChange=\"btn_submit('main')\";";
        $arg["CHAIRCD"] = knjCreateCombo($objForm, "CHAIRCD", $model->field["CHAIRCD_SUBCLASS"], $opt_chair, $extra, 1);
        //hidden
        knjCreateHidden($objForm, "H_CHAIRCD");

        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
            $send_classcd          = $subclass_array[0];
            $send_school_kind      = $subclass_array[1];
            $send_curriculum_cd    = $subclass_array[2];
            $send_subclasscd       = $subclass_array[3];
        } else {
            $send_subclasscd       = $chaircd_subclass_array[1];
        }

        //一覧表示
        $query = knjd126rQuery::getFirstStudent($model, $execute_date);
        $model->grade = $db->getOne($query);

        //パターン
        $query = knjd126rQuery::selectPatternData($model);
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
        $sendViewData = "";
        $sep = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $view_cnt++;
            if ($view_cnt > $maxCnt) break;
            $view_key[$view_cnt] = $row["VIEWCD"];
            $sendViewData .= $sep.$row["VIEWCD"];
            $sep = ",";
            //チップヘルプ
            $view_html .= "<th width=\"60\" onMouseOver=\"ViewcdMousein(event, ".$view_cnt.")\" onMouseOut=\"ViewcdMouseout()\">".$view_html_no[$view_cnt]."</th>";
            knjCreateHidden($objForm, "VIEWCD".$view_cnt, $row["VIEWNAME"]);
            if (!$model->viewPerfect[$row["VIEWCD"]]) {
                $isPerfectNoSet = true;
            }
        }
        for ($i=0; $i < ($maxCnt - get_count($view_key)); $i++) $view_html .= "<th width=\"60\">&nbsp;</th>";
        //hidden
        knjCreateHidden($objForm, "SEND_VIEWDATA", $sendViewData);

        $arg["view_html"] = $view_html;
        //評定用観点コード
        if ($view_cnt > 0) $view_key[9] = substr($chaircd_subclass_array[1], 0, 2)."99";

        //初期化
        $model->data = array();
        $counter = 0;
        $disable = "disabled";

        //一覧表示
        $query = knjd126rQuery::selectQuery($model, $execute_date, $view_key);
        $result = $db->query($query);
        $checkGrade = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];
            $checkGrade[$row["GRADE"]] = $row["GRADE"];

            //クラス-出席番(表示)
            $row["ATTENDNO"] = $row["HR_NAME"]."-".$row["ATTENDNO"]."番";

            //名前
            $row["NAME"] = $row["SCHREGNO"]." ".$row["NAME"];

            knjCreateHidden($objForm, "SCH-".$counter, $row["ATTENDNO"]." ".$row["NAME"]);

            //異動情報
            $query = knjd126rQuery::getTransfer($model, $row["SCHREGNO"]);
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

        if (get_count($checkGrade) > 1) {
            $arg["jscript"] = "OnGradeError();";
        }

        //更新ボタン
        $disabled = "";
        if ($chaircd_subclass_array[1] && $isPerfectNoSet) {
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
        knjCreateHidden($objForm, "PRGID", "KNJD126R");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SEMESTER2", $model->field["SEMESTER2"]);

        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //観点数設定
        knjCreateHidden($objForm, "kantenHyouji", $model->Properties["kantenHyouji"]);
        knjCreateHidden($objForm, "kantenHyouji_5", $arg["kantenHyouji_5"]);
        knjCreateHidden($objForm, "kantenHyouji_6", $arg["kantenHyouji_6"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "GRADE", $model->grade);
        knjCreateHidden($objForm, "SEND_CHAIRCD", $chaircd_subclass_array[0]);
        knjCreateHidden($objForm, "SEND_CLASSCD", $send_classcd);
        knjCreateHidden($objForm, "SEND_SCHOOL_KIND", $send_school_kind);
        knjCreateHidden($objForm, "SEND_CURRICULUMCD", $send_curriculum_cd);
        knjCreateHidden($objForm, "SEND_SUBCLASSCD", $send_subclasscd);
        knjCreateHidden($objForm, "execute_date", $execute_date);

        //DB切断
        Query::dbCheckIn($db);

        if ($chaircd_subclass_array[1] && $isPerfectNoSet) {
            $arg["close_win"] = "alert('満点設定等が未完了です。');";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd126rForm1.html", $arg);
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
