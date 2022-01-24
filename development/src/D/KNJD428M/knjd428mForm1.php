<?php

require_once('for_php7.php');
class knjd428mForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd428mindex.php", "", "edit");

        $arg["fep"] = $model->Properties["FEP"];

        //DB接続
        $db = Query::dbCheckOut();

        //グループ情報
        $getGroupRow = array();
        $getGroupRow = $db->getRow(knjd428mQuery::getViewGradeKindSchreg($model, "set"), DB_FETCHMODE_ASSOC);
        if ($model->schregno) {
            $getGroupName = $db->getOne(knjd428mQuery::getGroupcd($model, $getGroupRow));
            if ($getGroupName) {
                $groupName = '履修科目グループ:'.$getGroupName;
            } else {
                $groupName = '履修科目グループ未設定';
            }
            $getConditionName = $db->getOne(knjd428mQuery::getConditionName($model, $getGroupRow["CONDITION"]));
            $conditionName = ($getConditionName) ? '('.$getConditionName.')' : "";
        }

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name."　".$conditionName."　".$groupName;

        if ($model->schregno == "") {
            $disabled = "disabled";
        } else {
            $disabled = "";
        }

        /******************/
        /* コンボボックス */
        /******************/
        //学期コンボ
        $query = knjd428mQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");

        //校種
        $query = knjd428mQuery::getSchoolKind($model);
        $schoolKind = $db->getOne($query);

        //年間目標
        $query = knjd428mQuery::getReportCondition($model, $schoolKind, "109");
        $remark1 = $db->getOne($query);

        if ($remark1 == '1') {
            $arg["subform_show"] = '1';
            $extra = "onclick=\"return btn_submit('subform1');\" $disabled";
            $arg["button"]["subform1"] = KnjCreateBtn($objForm, "subform1", "年間目標", $extra);
        } else {
            $arg["subform_show"] = '';
        }


        //行動の記録
        $query = knjd428mQuery::getReportCondition($model, $schoolKind, "108");
        $remark1 = $db->getOne($query);

        if ($remark1 == '1') {
            $arg["remark_show"] = '1';
            //行動の記録ボタン
            if ($model->printPattern == "C") {
                $arg["data"]["BTN_F1COLSPN"] = "2";
            } else {
                $arg["data"]["BTN_F1COLSPN"] = "1";
            }
            $extra = "$disabled onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_LM/knjd_behavior_lmindex.php?CALL_PRG=KNJD428M&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->semester."&SCHREGNO=".$model->schregno."&SCHOOL_KIND=".$schoolKind."&GRADE=".$model->grade."&send_knjdBehaviorsd_UseText_P=".$model->Properties["knjdBehaviorsd_UseText_P"]."',0,0,800,500);\"";
            $arg["button"]["btn_form1"] = KnjCreateBtn($objForm, "btn_form1", "行動の記録", $extra);
        } else {
            $arg["remark_show"] = '';
        }

        //印刷デフォルト値設定「主に使用する様式」が「文言評価(3枠)B」の場合
        if ($model->useJisshu) {
            $arg["jisshu_show"] = "1";
        }

        //所見取得
        $row = $db->getRow(knjd428mQuery::getHreportremarkDat($model), DB_FETCHMODE_ASSOC);
        if (!isset($row)) {
            $row = array();
        }

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $row =& $model->field;
        }

        /********************/
        /* テキストボックス */
        /********************/
        //出欠の備考
        $extra = " id=\"ATTENDREC_REMARK\" onkeyup=\"charCount(this.value, {$model->attendrec_remark_gyou}, ({$model->attendrec_remark_moji} * 2), true);\"";
        $arg["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_gyou, ($model->attendrec_remark_moji * 2), "soft", $extra, $row["ATTENDREC_REMARK"]);
        $arg["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";
        knjCreateHidden($objForm, "ATTENDREC_REMARK_KETA", $model->attendrec_remark_moji * 2);
        knjCreateHidden($objForm, "ATTENDREC_REMARK_GYO", $model->attendrec_remark_gyou);
        KnjCreateHidden($objForm, "ATTENDREC_REMARK_STAT", "statusarea1");

        //出欠の記録参照ボタン
        $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", "出欠の記録参照", "ATTENDREC_REMARK", $disabled);

        //学校より
        $extra = " id=\"COMMUNICATION\" onkeyup=\"charCount(this.value, {$model->communication_gyou}, ({$model->communication_moji} * 2), true);\"";
        $arg["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION", $model->communication_gyou, ($model->communication_moji * 2), "soft", $extra, $row["COMMUNICATION"]);
        $arg["COMMUNICATION_COMMENT"] = "(全角".$model->communication_moji."文字X".$model->communication_gyou."行まで)";
        knjCreateHidden($objForm, "COMMUNICATION_KETA", $model->communication_moji * 2);
        knjCreateHidden($objForm, "COMMUNICATION_GYO", $model->communication_gyou);
        KnjCreateHidden($objForm, "COMMUNICATION_STAT", "statusarea2");

        //記録１ 期日・実習先・実習内容1
        $extra = ($row["D02_01_REMARK3"] == "1") ? " disabled" : "";
        $arg["D02_01_REMARK1"] = getTextOrArea($objForm, "D02_01_REMARK1", $model->getPro["D02_01_REMARK1"]["moji"], $model->getPro["D02_01_REMARK1"]["gyou"], $row["D02_01_REMARK1"], $extra);
        setInputChkHidden($objForm, "D02_01_REMARK1", $model->getPro["D02_01_REMARK1"]["moji"], $model->getPro["D02_01_REMARK1"]["gyou"], $arg);

        //記録１ 所見1
        $extra = ($row["D02_01_REMARK3"] == "1") ? " disabled" : "";
        $arg["D02_01_REMARK2"] = getTextOrArea($objForm, "D02_01_REMARK2", $model->getPro["D02_01_REMARK2"]["moji"], $model->getPro["D02_01_REMARK2"]["gyou"], $row["D02_01_REMARK2"], $extra);
        setInputChkHidden($objForm, "D02_01_REMARK2", $model->getPro["D02_01_REMARK2"]["moji"], $model->getPro["D02_01_REMARK2"]["gyou"], $arg);

        //記録１ 斜線を入れるチェックボックス
        $extra  = ($row["D02_01_REMARK3"] == "1") ? "checked" : "";
        $extra .= " id=\"D02_01_REMARK3\" onclick=\"return checkSlash(this, 'D02_01_REMARK1', 'D02_01_REMARK2');\"";
        $arg["D02_01_REMARK3"] = knjCreateCheckBox($objForm, "D02_01_REMARK3", "1", $extra, "");

        //記録２ 期日・実習先・実習内容2
        $extra = ($row["D02_02_REMARK3"] == "1") ? " disabled" : "";
        $arg["D02_02_REMARK1"] = getTextOrArea($objForm, "D02_02_REMARK1", $model->getPro["D02_02_REMARK1"]["moji"], $model->getPro["D02_02_REMARK1"]["gyou"], $row["D02_02_REMARK1"], $extra);
        setInputChkHidden($objForm, "D02_02_REMARK1", $model->getPro["D02_02_REMARK1"]["moji"], $model->getPro["D02_02_REMARK1"]["gyou"], $arg);

        //記録２ 所見2
        $extra = ($row["D02_02_REMARK3"] == "1") ? " disabled" : "";
        $arg["D02_02_REMARK2"] = getTextOrArea($objForm, "D02_02_REMARK2", $model->getPro["D02_02_REMARK2"]["moji"], $model->getPro["D02_02_REMARK2"]["gyou"], $row["D02_02_REMARK2"], $extra);
        setInputChkHidden($objForm, "D02_02_REMARK2", $model->getPro["D02_02_REMARK2"]["moji"], $model->getPro["D02_02_REMARK2"]["gyou"], $arg);

        //記録２ 斜線を入れるチェックボックス
        $extra  = ($row["D02_02_REMARK3"] == "1") ? "checked" : "";
        $extra .= " id=\"D02_02_REMARK3\" onclick=\"return checkSlash(this, 'D02_02_REMARK1', 'D02_02_REMARK2');\"";
        $arg["D02_02_REMARK3"] = knjCreateCheckBox($objForm, "D02_02_REMARK3", "1", $extra, "");

        //ホームルーム活動
        $arg["D01_01_REMARK1"] = getTextOrArea($objForm, "D01_01_REMARK1", $model->getPro["D01_01_REMARK1"]["moji"], $model->getPro["D01_01_REMARK1"]["gyou"], $row["D01_01_REMARK1"]);
        setInputChkHidden($objForm, "D01_01_REMARK1", $model->getPro["D01_01_REMARK1"]["moji"], $model->getPro["D01_01_REMARK1"]["gyou"], $arg);

        //生徒会活動
        $arg["D01_02_REMARK1"] = getTextOrArea($objForm, "D01_02_REMARK1", $model->getPro["D01_02_REMARK1"]["moji"], $model->getPro["D01_02_REMARK1"]["gyou"], $row["D01_02_REMARK1"]);
        setInputChkHidden($objForm, "D01_02_REMARK1", $model->getPro["D01_02_REMARK1"]["moji"], $model->getPro["D01_02_REMARK1"]["gyou"], $arg);

        //部活動・その他
        $arg["D01_03_REMARK1"] = getTextOrArea($objForm, "D01_03_REMARK1", $model->getPro["D01_03_REMARK1"]["moji"], $model->getPro["D01_03_REMARK1"]["gyou"], $row["D01_03_REMARK1"]);
        setInputChkHidden($objForm, "D01_03_REMARK1", $model->getPro["D01_03_REMARK1"]["moji"], $model->getPro["D01_03_REMARK1"]["gyou"], $arg);

        /**********/
        /* ボタン */
        /**********/

        //更新ボタン
        $extra = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update')\"" : "disabled";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新後前後の生徒へ
        if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
        } else {
            $extra = "disabled style=\"width:130px\"";
            $arg["button"]["btn_up_pre"]  = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
            $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
        }

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if (get_count($model->warning) == 0 && $model->cmd != "reset") {
            $arg["next"] = "NextStudent2(0);";
        } elseif (get_count($model->warning) != 0 || $model->cmd == "reset") {
            $arg["next"] = "NextStudent2(1);";
        }
        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd428mForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
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
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled = "")
{
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "syukketsukiroku") {   //出欠の記録参照
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}&DIV=1',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $setExtra = "")
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $extra = "style=\"overflow-y:scroll\" id=\"".$name."\"".$setExtra;
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2), "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"".$setExtra;
        // $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), ($moji * 2), $extra);
    }
    return $retArg;
}
function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg)
{
    $arg[$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
}
function getTextAreaComment($moji, $gyo)
{
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}
