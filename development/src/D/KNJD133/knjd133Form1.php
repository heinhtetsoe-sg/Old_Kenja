<?php

require_once('for_php7.php');


class knjd133Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        unset($arg["isKomazawa"]);
        if ($model->isKomazawa == "1") {
            $arg["isKomazawa"] = "1";
        }

        if ($model->cmd == "back") {
            $model->field["SUBCLASSCD"] = $model->subclasscd;
            $model->field["CHAIRCD"]    = $model->chaircd;
        }
        
        //名称マスタ D校種08チェック
        $model->che_school_kind = "D".$model->urlSchoolKind."08";
        $model->count = $db->getone(knjd133query::getNameMstche($model));
        
        //科目コンボ
        $opt_sbuclass = array();
        $opt_sbuclass[] = array("value" => "", "label" => "");
        $result = $db->query(knjd133Query::selectSubclassQuery($model, $model->gen_ed));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_sbuclass[] = array("value" => $row["SUBCLASSCD"], "label" => $row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"]);
        }
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $arg["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $model->field["SUBCLASSCD"], $opt_sbuclass, $extra, 1);

        //講座コンボ
        $opt_chair = array();
        $opt_chair[] = array("value" => "", "label" => "");
        $result = $db->query(knjd133Query::selectChairQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chair[] = array("value" => $row["CHAIRCD"], "label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"]);
        }
        $extra = "onChange=\"btn_submit('chaircd')\";";
        $arg["CHAIRCD"] = knjCreateCombo($objForm, "CHAIRCD", $model->field["CHAIRCD"], $opt_chair, $extra, 1);

        //ALLチェック(単位自動)
        $extra = "onClick=\"return check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra);

        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        $sdate = str_replace("/", "-", $model->control["学期開始日付"][CTRL_SEMESTER]);
        $edate = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);
        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
            $execute_date = CTRL_DATE;//初期値
        } else {
            $execute_date = $edate;//初期値
        }

        //定型文格納
        $tmpArray = array();
        $convert = array("A" => "5", "B" => "4", "C" => "3", "D" => "2", "E" => "1");
        $result = $db->query(knjd133Query::getHtrainremarkTempDat($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tmpArray[$row["GRADE"].'_'.$convert[$row["PATTERN_CD"]]] = $row["REMARK"];
            knjCreateHidden($objForm, "TMP-".$row["GRADE"]."-".$convert[$row["PATTERN_CD"]], $row["REMARK"]);
        }

        //初期化
        $model->data=array();
        $counter=0;
        //科目から校種を判定し、プロパティから文字数を取得する
        $model->totalStudyText = array();
        $model->totalStudyText["TOTALSTUDYACT"]["moji"] = 25;
        $model->totalStudyText["TOTALSTUDYACT"]["gyou"] = 2;
        $model->totalStudyText["TOTALSTUDYTIME"]["moji"] = 25;
        $model->totalStudyText["TOTALSTUDYTIME"]["gyou"] = 3;
        if ($model->field["SUBCLASSCD"]) {
            $subclass = preg_split("/-/", $model->field["SUBCLASSCD"]);
            $schoolKind = $subclass[1];
            //学習内容
            if ($model->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_".$schoolKind]) {
                list($moji, $gyou) = preg_split("/\*/", $model->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYACT_SIZE_".$schoolKind]);
                $model->totalStudyText["TOTALSTUDYACT"]["moji"] = (int)trim($moji);
                $model->totalStudyText["TOTALSTUDYACT"]["gyou"] = (int)trim($gyou);
            }
            //評価
            if ($model->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYTIME_SIZE_".$schoolKind]) {
                list($moji, $gyou) = preg_split("/\*/", $model->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYTIME_SIZE_".$schoolKind]);
                $model->totalStudyText["TOTALSTUDYTIME"]["moji"] = (int)trim($moji);
                $model->totalStudyText["TOTALSTUDYTIME"]["gyou"] = (int)trim($gyou);
            }
        }

        // テキストエリア(テキストボックス)が長すぎる場合、画面幅、値の幅は可変
        $allWidth = 1250;
        $defaultValueWidth = 600;
        $formWidthPerOneCharacter = 17.3;

        if ($model->totalStudyText["TOTALSTUDYACT"]["moji"] >= $model->totalStudyText["TOTALSTUDYTIME"]["moji"]) {
            $valueWidth = (int)$model->totalStudyText["TOTALSTUDYACT"]["moji"] * (int)$formWidthPerOneCharacter;
        } else {
            $valueWidth = (int)$model->totalStudyText["TOTALSTUDYTIME"]["moji"] * (int)$formWidthPerOneCharacter;
        }
        if ($defaultValueWidth >= $valueWidth) {
            $arg["VALUEWIDTH"] = $defaultValueWidth;
            $arg["ALLWIDTH"] = $allWidth;
        } else {
            $arg["VALUEWIDTH"] = $valueWidth;
            $arg["ALLWIDTH"] = (int)$allWidth + ($valueWidth - (int)$defaultValueWidth);
        }

        $gradeList = array();
        //一覧表示
        $colorFlg = false;
        $query = knjd133Query::selectQuery($model, $execute_date);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //氏名欄に学籍番号表記
            if ($model->Properties["use_SchregNo_hyoji"] == 1) {
                $row["SCHREGNO_SHOW"] = $row["SCHREGNO"] . "　";
            }

            //クラス-出席番(表示)
            if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
            }

            //学年格納
            knjCreateHidden($objForm, "GRADE-".$counter, $row["GRADE"]);
            if (!in_array($row["GRADE"], $gradeList)) {
                $gradeList[] = $row["GRADE"];
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            /*** テキストエリア ***/
            $moji = $model->totalStudyText["TOTALSTUDYACT"]["moji"];
            $gyou = $model->totalStudyText["TOTALSTUDYACT"]["gyou"];
            $height = (int)$gyou * 13.5 + ((int)$gyou - 1) * 3 + 5;
            //学習内容
            $model->data["TOTALSTUDYACT"."-".$counter] = $row["TOTALSTUDYACT"];
            if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] == '1') {
                $extra = "style=\"height:{$height};background-color:#D0D0D0;\" readonly";
            } else {
                $extra = "style=\"height:{$height};\" onPaste=\"return showPaste(this);\"";
                $row["TOTALSTUDYACT"] = $model->cmd != "csvInputMain" ? $row["TOTALSTUDYACT"] : $model->data_arr[$row["SCHREGNO"]]["TOTALSTUDYACT"];
            }
            $value = (!isset($model->warning)) ? $row["TOTALSTUDYACT"] : $model->fields["TOTALSTUDYACT"][$counter];

            if ($gyou == 1) {
                $row["TOTALSTUDYACT"] = knjCreateTextBox($objForm, $value, "TOTALSTUDYACT-".$counter, ((int)$moji * 2), ((int)$moji * 2), $extra);
                $comment = "(全角で {$moji}文字)";
            } else {
                $row["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT-".$counter, $gyou, ((int)$moji * 2 + 1), "soft", $extra, $value);
                $comment = "(全角で {$moji}文字X{$gyou}行)";
            }
            $row["TOTALSTUDYACT_COMMENT"] = $comment;

            $moji = $model->totalStudyText["TOTALSTUDYTIME"]["moji"];
            $gyou = $model->totalStudyText["TOTALSTUDYTIME"]["gyou"];
            $height = (int)$gyou * 13.5 + ((int)$gyou - 1) * 3 + 5;
            //評価
            $model->data["TOTALSTUDYTIME"."-".$counter] = $row["TOTALSTUDYTIME"];
            if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] == '1') {
                $extra = "style=\"height:{$height};background-color:#D0D0D0;\" readonly";
            } else {
                $extra = "style=\"height:{$height};\" onPaste=\"return showPaste(this);\"";
                $row["TOTALSTUDYTIME"] = $model->cmd != "csvInputMain" ? $row["TOTALSTUDYTIME"] : $model->data_arr[$row["SCHREGNO"]]["TOTALSTUDYTIME"];
            }
            $value = (!isset($model->warning)) ? $row["TOTALSTUDYTIME"] : $model->fields["TOTALSTUDYTIME"][$counter];

            if ($gyou == 1) {
                $row["TOTALSTUDYTIME"] = knjCreateTextBox($objForm, $value, "TOTALSTUDYTIME-".$counter, ((int)$moji * 2), ((int)$moji * 2), $extra);
                $comment = "(全角で {$moji}文字)";
            } else {
                $row["TOTALSTUDYTIME"] = KnjCreateTextArea($objForm, "TOTALSTUDYTIME-".$counter, $gyou, ((int)$moji * 2 + 1), "soft", $extra, $value);
                $comment = "(全角で {$moji}文字X{$gyou}行)";
            }
            $row["TOTALSTUDYTIME_COMMENT"] = $comment;

            /*** 定型文 ***/
            $extra = "onclick=\"return showTeikeiWindow('TOTALSTUDYACT-".$counter."', '03');\"";
            $row["TOTALSTUDYACT_TEIKEI"] = knjCreateBtn($objForm, "TOTALSTUDYACT_TEIKEI-".$counter, "定型文選択", $extra);
            if ($model->isKomazawa == "1") {
                $extra = "onblur=\"setTeikeiTotalstudyTime(this,".$counter.");setHyouteiRank(this,".$counter.");\"";
                $row["TOTALSTUDYTIME_TEIKEI"] = knjCreateTextBox($objForm, "", "TOTALSTUDYTIME_TEIKEI-".$counter, 5, 4, $extra);
            } else {
                $extra = "onclick=\"return showTeikeiWindow('TOTALSTUDYTIME-".$counter."', '04');\"";
                $row["TOTALSTUDYTIME_TEIKEI"] = knjCreateBtn($objForm, "TOTALSTUDYTIME_TEIKEI-".$counter, "定型文選択", $extra);
            }


            /*** チェックボックス ***/
            //単位自動・・・チェックありの場合、単位マスタの単位数をセットし更新
            if (isset($model->warning) && $model->fields["CHK_CALC_CREDIT"][$counter] == "on") {
                $extra = "checked";
            } else {
                $extra = "";
            }
            $row["CHK_CALC_CREDIT"] = knjCreateCheckBox($objForm, "CHK_CALC_CREDIT-".$counter, "on", $extra);

            /*** テキストボックス ***/
            //学年評定
            $model->data["GRAD_VALUE"."-".$counter] = $row["GRAD_VALUE"];
            $row["GRAD_VALUE"] = $model->cmd != "csvInputMain" ? $row["GRAD_VALUE"] : $model->data_arr[$row["SCHREGNO"]]["GRAD_VALUE"];
            $value = (!isset($model->warning)) ? $row["GRAD_VALUE"] : $model->fields["GRAD_VALUE"][$counter];
            $extra = " onBlur=\"return tmpSet(this);\" STYLE=\"text-align:right;\" onPaste=\"return showPaste(this);\"";
            $row["GRAD_VALUE"] = knjCreateTextBox($objForm, $value, "GRAD_VALUE-".$counter, 3, 2, $extra);

            //履修単位
            $model->data["COMP_CREDIT"."-".$counter] = $row["COMP_CREDIT"];
            $row["COMP_CREDIT"] = $model->cmd != "csvInputMain" ? $row["COMP_CREDIT"] : $model->data_arr[$row["SCHREGNO"]]["COMP_CREDIT"];
            $value = (!isset($model->warning)) ? $row["COMP_CREDIT"] : $model->fields["COMP_CREDIT"][$counter];
            $extra = " onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\" onPaste=\"return showPaste(this);\"";
            $row["COMP_CREDIT"] = knjCreateTextBox($objForm, $value, "COMP_CREDIT-".$counter, 3, 2, $extra);

            //修得単位
            $model->data["GET_CREDIT"."-".$counter] = $row["GET_CREDIT"];
            $row["GET_CREDIT"] = $model->cmd != "csvInputMain" ? $row["GET_CREDIT"] : $model->data_arr[$row["SCHREGNO"]]["GET_CREDIT"];
            $value = (!isset($model->warning)) ? $row["GET_CREDIT"] : $model->fields["GET_CREDIT"][$counter];
            $extra = " onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\" onPaste=\"return showPaste(this);\"";
            $row["GET_CREDIT"] = knjCreateTextBox($objForm, $value, "GET_CREDIT-".$counter, 3, 2, $extra);

            //背景色
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }
        knjCreateHidden($objForm, "recordCount", $counter);

        unset($arg["isTeikei"]);
        // タイトル 定型文ボタン
        if ($model->Properties["tutisyoTeikei_Button_Hyouji"] == "1") {
            $disabled = $model->field["CHAIRCD"] ? '': ' disabled ';
            $arg["isTeikei"] = "1";
            $extra  = "onclick=\"return showTeikeiWindow2('TOTALSTUDYACT', '03');\"";
            $arg["btn_teikei1"] = knjCreateBtn($objForm, "btn_teikei1", "学習内容の定型文選択", $extra.$disabled);
            if ($model->Properties["useTotalstudyTime"] == "1") {
                if ($model->isKomazawa == "1") {
                    $extra = "onblur=\"setTeikeiTotalstudyTimeAll(this);setHyouteiRankALL(this)\"";
                    $arg["label_teikei2"] = knjCreateTextBox($objForm, "", "label_teikei2", 5, 4, $extra.$disabled);
                } else {
                    $extra = "onclick=\"return showTeikeiWindow2('TOTALSTUDYTIME', '04');\"";
                    $arg["btn_teikei2"] = knjCreateBtn($objForm, "btn_teikei2", "評価の定型文選択", $extra.$disabled);
                }
            }
        }

        // 駒沢大学
        if ($model->isKomazawa) {
            $dataDivList = array('81', '82', '83', '84');
            $convert = array("A" => "5", "B" => "4", "C" => "3", "D" => "2", "E" => "1");

            for ($i=0; $i < get_count($gradeList); $i++) {
                $grade = $gradeList[$i];
                for ($j=0; $j < get_count($dataDivList); $j++) {
                    $dataDiv = $dataDivList[$j];

                    $query = knjd133Query::getHtrainRemarkTempDatIkkatsu($grade, $dataDiv);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $hiddenName = 'REMARK-TIME-'.$grade.'-'.$dataDiv.'-'.$convert[$row['PATTERN_CD']];
                        knjCreateHidden($objForm, $hiddenName, $row['REMARK']);
                    }
                    $result->free();
                }
            }

            $query = knjd133Query::getNameMst('D001', '');
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                knjCreateHidden($objForm, "RANK_".$row['NAME1'], $row['NAMECD2'].'_'.$row['NAMESPARE1'].'_'.$row['NAMESPARE2']);
            }
            $result->free();
        }

        //ファイル
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        Query::dbCheckIn($db);

        // ボタンの作成
        makeBtn($objForm, $arg, $model);
        // hidden作成
        makeHidden($objForm, $model);

        $arg["IFRAME"] = VIEW::setIframeJs();
        $arg["start"]    = $objForm->get_start("main", "POST", "knjd133index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd133Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{

    //ボタン
    $extra = "onclick=\"return btn_submit('csvInput');\"";
    $arg["btn_input"] = knjCreateBtn($objForm, "btn_input", "CSV取込", $extra);

    $extra = "onclick=\"return btn_submit('csvOutput');\"";
    $arg["btn_output"] = knjCreateBtn($objForm, "btn_output", "CSV出力", $extra);

    //一括更新ボタンを作成する
    $link = REQUESTROOT."/D/KNJD133/knjd133index.php?cmd=replace&CHAIRCD=".$model->field["CHAIRCD"]."&SUBCLASSCD=".$model->field["SUBCLASSCD"];
    $extra = ($model->field["CHAIRCD"]) ? "onclick=\"Page_jumper('$link');\"" : "disabled";
    $arg["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);

    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");

    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
    knjCreateHidden($objForm, "showTemp04", $model->Properties["showTemp04"]);
    knjCreateHidden($objForm, "isKomazawa", $model->isKomazawa);
}
