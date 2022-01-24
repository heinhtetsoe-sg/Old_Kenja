<?php

require_once('for_php7.php');

class knjd428lForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd428lindex.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();


        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        if ($model->schregno != "") {
            $arg["DISPSELDAT"] = "1";

            $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : $model->semester;
            //学期コンボ
            $query = knjd428lQuery::getSemester($model);
            $extra = "onChange=\"return btn_submit('edit');\"";
            makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

            //生徒の校種・学年を取得
            $query = knjd428lQuery::getSchoolKindGrade($model);
            $gradeRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!$gradeRow["SCHOOL_KIND"]) {
                //取得できなかったら、以降の処理に影響が出るのでエラーにする。
                $model->setWarning("MSG303", '生徒に対応する校種が取得できませんでした。');
            }

            //特別な教科の入力欄表示有無
            $arg["DISP1"] = "";
            $arg["DISP2"] = "";
            $arg["DISP3"] = "";
            $arg["DISP4"] = "";
            $arg["DISP5"] = "";

            $section = array();
            if ($model->useSection[$gradeRow["SCHOOL_KIND"]][$gradeRow["GRADE_CD"]]) {
                $section = $model->useSection[$gradeRow["SCHOOL_KIND"]][$gradeRow["GRADE_CD"]];
            }
            if (get_count($section) <= 0) {
                //学年(GRADE_CD)で取得出来なかった場合は "00" を取得
                $section = $model->useSection[$gradeRow["SCHOOL_KIND"]]["00"];
            }

            if (isset($model->schregno) && !isset($model->warning) && $model->cmd != "check") {
                $query = knjd428lQuery::getRow($model);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($row) {
                    //特別の教科 道徳
                    $model->field["REMARK1_01"]       = $row["REMARK1_01"];
                    //外国語活動
                    $model->field["FOREIGNLANGACT"]   = $row["FOREIGNLANGACT"];
                    //総合的な学習の時間
                    $model->field["TOTALSTUDYTIME"]   = $row["TOTALSTUDYTIME"];
                    //特別活動
                    $model->field["SPECIALACTREMARK"] = $row["SPECIALACTREMARK"];
                    //自立活動
                    $model->field["ATTENDREC_REMARK"] = $row["ATTENDREC_REMARK"];

                    //出欠の備考
                    $model->field["COMMUNICATION"]    = $row["COMMUNICATION"];
                    //学校より
                    $model->field["REMARK1_02"]       = $row["REMARK1_02"];
                } else {
                    $model->field = array();
                }
            }

            //特別の教科 道徳
            if (in_array("1", $section)) {
                $arg["DISP1"] = "1";
                $title = $model->textLimit["REMARK1_01"]["title"];
                $moji = $model->textLimit["REMARK1_01"]["moji"];
                $gyou = $model->textLimit["REMARK1_01"]["gyou"];
                createText($objForm, $arg, "REMARK1_01", $model->field["REMARK1_01"], $title, $moji, $gyou);
            }
            //外国語活動
            if (in_array("2", $section)) {
                $arg["DISP2"] = "1";
                $title = $model->textLimit["FOREIGNLANGACT"]["title"];
                $moji = $model->textLimit["FOREIGNLANGACT"]["moji"];
                $gyou = $model->textLimit["FOREIGNLANGACT"]["gyou"];
                createText($objForm, $arg, "FOREIGNLANGACT", $model->field["FOREIGNLANGACT"], $title, $moji, $gyou);
            }
            //総合的な学習の時間
            if (in_array("3", $section)) {
                $arg["DISP3"] = "1";
                $title = $model->textLimit["TOTALSTUDYTIME"]["title"];
                $moji = $model->textLimit["TOTALSTUDYTIME"]["moji"];
                $gyou = $model->textLimit["TOTALSTUDYTIME"]["gyou"];
                createText($objForm, $arg, "TOTALSTUDYTIME", $model->field["TOTALSTUDYTIME"], $title, $moji, $gyou);
            }
            //特別活動
            if (in_array("4", $section)) {
                $arg["DISP4"] = "1";
                $title = $model->textLimit["SPECIALACTREMARK"]["title"];
                $moji = $model->textLimit["SPECIALACTREMARK"]["moji"];
                $gyou = $model->textLimit["SPECIALACTREMARK"]["gyou"];
                createText($objForm, $arg, "SPECIALACTREMARK", $model->field["SPECIALACTREMARK"], $title, $moji, $gyou);
            }
            //自立活動
            if (in_array("5", $section)) {
                $arg["DISP5"] = "1";
                $title = $model->textLimit["REMARK1_02"]["title"];
                $moji = $model->textLimit["REMARK1_02"]["moji"];
                $gyou = $model->textLimit["REMARK1_02"]["gyou"];
                createText($objForm, $arg, "REMARK1_02", $model->field["REMARK1_02"], $title, $moji, $gyou);
            }

            //行動の記録
            $arg["ReportCondition"] = '';
            $query = knjd428lQuery::getReportCondition($model, $gradeRow["SCHOOL_KIND"], "208");
            $remark1 = $db->getOne($query);
            if ($remark1 != '2') {
                $arg["ReportCondition"] = '1';
                //行動の記録ボタン
                $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_LM/knjd_behavior_lmindex.php?CALL_PRG=KNJD428L&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->semester."&SCHREGNO=".$model->schregno."&SCHOOL_KIND=".$gradeRow["SCHOOL_KIND"]."&GRADE=".$model->grade."&send_knjdBehaviorsd_UseText_P=1',0,0, 750, 500);\"";
                $arg["button"]["btn_form1"] = KnjCreateBtn($objForm, "btn_form1", "行動の記録", $extra);
            }

            //出欠の備考
            $title = $model->textLimit["ATTENDREC_REMARK"]["title"];
            $moji = $model->textLimit["ATTENDREC_REMARK"]["moji"];
            $gyou = $model->textLimit["ATTENDREC_REMARK"]["gyou"];
            createText($objForm, $arg, "ATTENDREC_REMARK", $model->field["ATTENDREC_REMARK"], $title, $moji, $gyou);
            //出欠の記録参照ボタン
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET=ATTENDREC_REMARK&DIV=1',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
            $arg["btn_attendrecRemark"] = knjCreateBtn($objForm, "btn_attendrecRemark", "出欠の記録参照", $extra);

            //学校より
            $title = $model->textLimit["COMMUNICATION"]["title"];
            $moji = $model->textLimit["COMMUNICATION"]["moji"];
            $gyou = $model->textLimit["COMMUNICATION"]["gyou"];
            createText($objForm, $arg, "COMMUNICATION", $model->field["COMMUNICATION"], $title, $moji, $gyou);

            $arg["NOT_WARNING"] = 1;
        }
        
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML5($model, "knjd428lForm1.html", $arg);
    }
}

//テキストボックスorテキストエリア作成
function createText(&$objForm, &$arg, $name, $val, $title, $moji, $gyou){
    $ext = "id=\"".$name."\" aria-label=\"".$title."\" ";
    $arg["data"][$name] = getTextOrArea($objForm, $name, $moji, $gyou, $val, $model, $ext);
    $arg["data"][$name."_COMMENT"] = "(全角".$moji."文字X".$gyou."行まで)";
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $ext_outstyle) {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = $gyou % 5;
            $minus = ($gyou / 5) > 1 ? ($gyou / 5) * 6 : 5;
        }
        $height = $gyou * 13.5 + ($gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\" ".$ext_outstyle;
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2), "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $result = $db->query($query);
    $defValue = '';
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($row["DEF_VALUE_FLG"] == '1') {
            $defValue = $row["VALUE"];
        }
    }

    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "" && $defValue) ? $defValue : ($value ? $value : $opt[0]["value"]);
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
