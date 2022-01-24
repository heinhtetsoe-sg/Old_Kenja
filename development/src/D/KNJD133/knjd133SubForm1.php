<?php

require_once('for_php7.php');

class knjd133SubForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]  = $objForm->get_start("sel", "POST", "knjd133index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度・学期・年組・テスト情報
        $year = CTRL_YEAR.'年度';
        $chairname = $db->getOne(knjd133Query::getChairName($model));
        $arg["INFO"] = $year.'　　'.$chairname;

        //生徒リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //学習内容、評価のテキストエリア表示
        if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] == '1') {
            $s_checkno = "2";
        } else {
            $arg["usetext"] = 1;
            $s_checkno = "0";
        }

        //チェックボックス
        for ($i=0; $i<6; $i++) {
            $extra = ($i == 5) ? "onClick=\"return check_all(this, '".$s_checkno."');\"" : "";
            $checked = ($model->replace_data["check"][$i] == "1") ? " checked" : "";
            $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra.$checked, "");
        }

        //学習内容テキストエリア
        $moji = $model->totalStudyText["TOTALSTUDYACT"]["moji"];
        $gyou = $model->totalStudyText["TOTALSTUDYACT"]["gyou"];
        $height = (int)$gyou * 13.5 + ((int)$gyou - 1) * 3 + 5;

        $extra = "style=\"height:{$height};\" id=\"TOTALSTUDYACT\"";

        if ($gyou == 1) {
            $arg["data"]["TOTALSTUDYACT"] = knjCreateTextBox($objForm, $model->replace_data["field"]["TOTALSTUDYACT"], "TOTALSTUDYACT", ((int)$moji * 2), ((int)$moji * 2), $extra);
            $comment = "(全角で {$moji}文字)";
        } else {
            $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $gyou, ((int)$moji * 2 + 1), "soft", $extra, $model->replace_data["field"]["TOTALSTUDYACT"]);
            $comment = "(全角で {$moji}文字X{$gyou}行)";
        }
        $arg["data"]["TOTALSTUDYACT_COMMENT"] = $comment;

        //評価テキストエリア
        $moji = $model->totalStudyText["TOTALSTUDYTIME"]["moji"];
        $gyou = $model->totalStudyText["TOTALSTUDYTIME"]["gyou"];
        $height = (int)$gyou * 13.5 + ((int)$gyou - 1) * 3 + 5;

        $extra = "style=\"height:{$height};\"";

        if ($gyou == 1) {
            $arg["data"]["TOTALSTUDYTIME"] = knjCreateTextBox($objForm, $model->replace_data["field"]["TOTALSTUDYTIME"], "TOTALSTUDYTIME", ((int)$moji * 2), ((int)$moji * 2), $extra);
            $comment = "(全角で {$moji}文字)";
        } else {
            $arg["data"]["TOTALSTUDYTIME"] = KnjCreateTextArea($objForm, "TOTALSTUDYTIME", $gyou, ((int)$moji * 2 + 1), "soft", $extra, $model->replace_data["field"]["TOTALSTUDYTIME"]);
            $comment = "(全角で {$moji}文字X{$gyou}行)";
        }
        $arg["data"]["TOTALSTUDYTIME_COMMENT"] = $comment;

        //定型文選択
        if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] != '1') {
            if ($model->Properties["tutisyoTeikei_Button_Hyouji"] == "1") {
                $extra  = "onclick=\"return showTeikeiWindow('TOTALSTUDYACT', '03');\"";
                $arg["btn_teikei"] = "<br>".knjCreateBtn($objForm, "btn_teikei", "定型文選択", $extra);

                if ($model->Properties["useTotalstudyTime"] == "1") {
                    if ($model->isKomazawa == "1") {
                        $extra = " ";
                        $extra = "onblur=\"setTeikeiTotalstudyTime(this);setHyouteiRank(this)\"";
                        $arg["btn_teikei2"] = "<br>".knjCreateTextBox($objForm, "", "btn_teikei2", 5, 4, $extra);
                    } else {
                        $extra = "onclick=\"return showTeikeiWindow('TOTALSTUDYTIME', '04');\"";
                        $arg["btn_teikei2"] = "<br>".knjCreateBtn($objForm, "btn_teikei2", "定型文選択", $extra);
                    }
                }
            }
        }

        //学年評定テキストボックス
        $extra = " onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
        $arg["data"]["GRAD_VALUE"] = knjCreateTextBox($objForm, $model->replace_data["field"]["GRAD_VALUE"], "GRAD_VALUE", 3, 2, $extra);

        //履修単位テキストボックス
        $extra = " onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
        $arg["data"]["COMP_CREDIT"] = knjCreateTextBox($objForm, $model->replace_data["field"]["COMP_CREDIT"], "COMP_CREDIT", 3, 2, $extra);

        //修得単位テキストボックス
        $extra = " onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
        $arg["data"]["GET_CREDIT"] = knjCreateTextBox($objForm, $model->replace_data["field"]["GET_CREDIT"], "GET_CREDIT", 3, 2, $extra);

        // 駒沢大学
        if ($model->isKomazawa) {
            $gradeList = array();
            //学年
            $query = knjd133Query::getGrade($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (!in_array($row['VALUE'], $gradeList)) {
                    $gradeList[] = $row['VALUE'];
                }
            }
            // TODO : 複数の学年がある場合の対応
            if (get_count($gradeList) > 0) {
                knjCreateHidden($objForm, "GRADE", $gradeList[0]);
            }
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
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        knjCreateHidden($objForm, "CHAIRCD", $model->chaircd);

        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd133SubForm1.html", $arg);
    }
}

//リストtoリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{

    //学籍処理日が学期範囲外の場合、学期終了日を使用する。
    $sdate = str_replace("/", "-", $model->control["学期開始日付"][CTRL_SEMESTER]);
    $edate = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);
    if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
        $execute_date = CTRL_DATE;  //初期値
    } else {
        $execute_date = $edate;     //初期値
    }

    //対象者リストを作成する
    $query = knjd133Query::getStudent($model, $execute_date);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["ATTENDNO"]."　".$row["NAME_SHOW"],
                        'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('left')\"";
    $arg["main_part"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt1, $extra, 20);

    //生徒一覧リストを作成する//
    $query = knjd133Query::getStudent($model, $execute_date, "1");
    $result = $db->query($query);
    $opt2 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt2[] = array('label' => $row["ATTENDNO"]."　".$row["NAME_SHOW"],
                        'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('right')\"";
    $arg["main_part"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt2, $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:35px\" onclick=\"moves('right');\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:35px\" onclick=\"moves('left');\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:35px\" onclick=\"move1('right');\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:35px\" onclick=\"move1('left');\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{

    //更新ボタンを作成する
    $extra = "onclick=\"return doSubmit()\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //戻るボタンを作成する
    $extra = "onclick=\"return btn_submit('back');\"";
    $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
