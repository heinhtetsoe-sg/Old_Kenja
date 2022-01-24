<?php

require_once('for_php7.php');

class knjd428Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd428index.php", "", "edit");

        $arg["fep"] = $model->Properties["FEP"];

        //DB接続
        $db = Query::dbCheckOut();

        //グループ情報
        $getGroupRow = array();
        $getGroupRow = $db->getRow(knjd428Query::getViewGradeKindSchreg($model, "set"), DB_FETCHMODE_ASSOC);
        if ($model->schregno) {
            $getGroupName = $db->getOne(knjd428Query::getGroupcd($model, $getGroupRow));
            if ($getGroupName) {
                $groupName = '履修科目グループ:'.$getGroupName;
            } else {
                $groupName = '履修科目グループ未設定';
            }
            $getConditionName = $db->getOne(knjd428Query::getConditionName($model, $getGroupRow["CONDITION"]));
            $conditionName = ($getConditionName) ? '('.$getConditionName.')' : "";
        }

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name."　".$conditionName."　".$groupName;

        //備考取得
        $row1 = $db->getRow(knjd428Query::getHreportremarkDetailDat($model), DB_FETCHMODE_ASSOC);
        if (!isset($row1)) {
            $row1 = array();
        }
        $row2 = $db->getRow(knjd428Query::getHreportremarkDat($model), DB_FETCHMODE_ASSOC);
        if (!isset($row2)) {
            $row2 = array();
        }
        $row  = array_merge($row1, $row2);

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $row =& $model->field;
        }

        if($model->schregno == ""){
            $disabled = "disabled";
        } else {
            $disabled = "";
        }

        /******************/
        /* コンボボックス */
        /******************/
        //学期コンボ
        $query = knjd428Query::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");

        //校種
        $query = knjd428Query::getSchoolKind($model);
        $schoolKind = $db->getOne($query);

        //行動の記録
        $query = knjd428Query::getReportCondition($model, $schoolKind, "108");
        $remark1 = $db->getOne($query);

        if ($remark1 != '1') {
            $arg["remark_show"] = '1';
            //行動の記録ボタン
            if ($model->printPattern == "C") {
                $arg["data"]["BTN_F1COLSPN"] = "2";
            } else {
                $arg["data"]["BTN_F1COLSPN"] = "1";
            }
            $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_SD/knjd_behavior_sdindex.php?CALL_PRG=".KNJD428."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->semester."&SCHREGNO=".$model->schregno."&send_knjdBehaviorsd_UseText=".$model->Properties["knjdBehaviorsd_UseText"]."',0,0,600,500);\"";
            $arg["button"]["btn_form1"] = KnjCreateBtn($objForm, "btn_form1", "行動の記録", $extra);
        } else {
            $arg["remark_show"] = '';
        }

        /********************/
        /* テキストボックス */
        /********************/
        if ($model->printPattern == "B" || $model->printPattern == "C") {
            $arg["PATTERN_BorC"] = "1";
        }
        //年間目標１
        $extra = " id=\"REMARK1\" onkeyup=\"charCount(this.value, {$model->remark1_gyou}, ({$model->remark1_moji} * 2), true);\"";
        $arg["REMARK1"] = knjCreateTextArea($objForm, "REMARK1", $model->remark1_gyou, ($model->remark1_moji * 2), "soft", $extra, $row["REMARK1"]);
        $arg["REMARK1_COMMENT"] = "(全角".$model->remark1_moji."文字X".$model->remark1_gyou."行まで)";
        knjCreateHidden($objForm, "REMARK1_KETA", $model->remark1_moji * 2);
        knjCreateHidden($objForm, "REMARK1_GYO", $model->remark1_gyou);
        KnjCreateHidden($objForm, "REMARK1_STAT", "statusarea3");

        if ($model->printPattern == 'C') {
            //年間目標２
            $extra = " id=\"REMARK2\" onkeyup=\"charCount(this.value, {$model->remark2_gyou}, ({$model->remark2_moji} * 2), true);\"";
            $arg["REMARK2"] = knjCreateTextArea($objForm, "REMARK2", $model->remark2_gyou, ($model->remark2_moji * 2), "soft", $extra, $row["REMARK2"]);
            $arg["REMARK2_COMMENT"] = "(全角".$model->remark2_moji."文字X".$model->remark2_gyou."行まで)";
            knjCreateHidden($objForm, "REMARK2_KETA", $model->remark2_moji * 2);
            knjCreateHidden($objForm, "REMARK2_GYO", $model->remark2_gyou);
            KnjCreateHidden($objForm, "REMARK2_STAT", "statusarea4");
        }

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
        } else if (get_count($model->warning) != 0 || $model->cmd == "reset") {
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
        View::toHTML5($model, "knjd428Form1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
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
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled="") {
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "syukketsukiroku") {   //出欠の記録参照
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}&DIV=1',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
?>
