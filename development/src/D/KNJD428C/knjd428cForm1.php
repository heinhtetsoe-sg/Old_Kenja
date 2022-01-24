<?php

require_once('for_php7.php');
class knjd428cForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd428cindex.php", "", "edit");

        $arg["fep"] = $model->Properties["FEP"];

        //DB接続
        $db = Query::dbCheckOut();

        //グループ情報
        $getGroupRow = array();
        $getGroupRow = $db->getRow(knjd428cQuery::getViewGradeKindSchreg($model, "set"), DB_FETCHMODE_ASSOC);
        if ($model->schregno) {
            $getGroupName = $db->getOne(knjd428cQuery::getGroupcd($model, $getGroupRow));
            if ($getGroupName) {
                $groupName = '履修科目グループ:'.$getGroupName;
            } else {
                $groupName = '履修科目グループ未設定';
            }
            $getConditionName = $db->getOne(knjd428cQuery::getConditionName($model, $getGroupRow["CONDITION"]));
            $conditionName = ($getConditionName) ? '('.$getConditionName.')' : "";
        }

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name."　".$conditionName."　".$groupName;

        //備考取得
        $row1 = $db->getRow(knjd428cQuery::getHreportremarkDetailDat($model), DB_FETCHMODE_ASSOC);
        if (!isset($row1)) {
            $row1 = array();
        }
        $row2 = $db->getRow(knjd428cQuery::getHreportremarkDat($model), DB_FETCHMODE_ASSOC);
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
        $query = knjd428cQuery::getSemester($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");

        //校種
        $query = knjd428cQuery::getSchoolKind($model);
        $schoolKind = $db->getOne($query);

        //行動の記録
        $query = knjd428cQuery::getReportCondition($model, $schoolKind, "108");
        $remark1 = $db->getOne($query);

        if ($remark1 != '1') {
            $arg["remark_show"] = '1';
            //行動の記録ボタン
            if ($model->printPattern == "C") {
                $arg["data"]["BTN_F1COLSPN"] = "2";
            } else {
                $arg["data"]["BTN_F1COLSPN"] = "1";
            }
            $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_SD/knjd_behavior_sdindex.php?CALL_PRG=".KNJD428C."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->semester."&SCHREGNO=".$model->schregno."&send_knjdBehaviorsd_UseText=".$model->Properties["knjdBehaviorsd_UseText"]."',0,0,600,500);\"";
            $arg["button"]["btn_form1"] = KnjCreateBtn($objForm, "btn_form1", "行動の記録", $extra);
        } else {
            $arg["remark_show"] = '';
        }

        //学校より 表示/非表示
        $query = knjd428cQuery::getReportCondition($model, $schoolKind, "111");
        $communication = $db->getOne($query);
        $arg["communication"] = true;
        if($communication == '1'){
            $arg["communication"] = false;
        }

        /********************/
        /* テキストボックス */
        /********************/
        //出欠の備考
        $extra = " id=\"ATTENDREC_REMARK\" ";
        $arg["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_gyou, ($model->attendrec_remark_moji * 2), "soft", $extra, $row["ATTENDREC_REMARK"]);
        $arg["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";

        //出欠の記録参照ボタン
        $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", "出欠の記録参照", "ATTENDREC_REMARK", $disabled);

        //学校より
        $extra = " id=\"COMMUNICATION\" ";
        $arg["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION", $model->communication_gyou, ($model->communication_moji * 2), "soft", $extra, $row["COMMUNICATION"]);
        $arg["COMMUNICATION_COMMENT"] = "(全角".$model->communication_moji."文字X".$model->communication_gyou."行まで)";

        //担任名デフォルト
        $defaultStaff = array();
        $query = knjd428cQuery::getSchregRegdHdat($model);
        $result = $db->query($query);
        while ($stfRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $defaultStaff["REMARK1"] = $stfRow["REMARK1"];
            $defaultStaff["REMARK2"] = $stfRow["REMARK2"];
            $defaultStaff["REMARK3"] = $stfRow["REMARK3"];
        }
        if($model->exp_year){
            $model->staff_year = $model->exp_year;
        } else {
            $model->staff_year = CTRL_YEAR;
        }

        //担任名
        $query = knjd428cQuery::getStaffList($model);
        $result = $db->query($query);
        $opt = array();
        $opt[] = array("label" => "", "value" => "");

        for($i = 1; $i <= 6; $i++){
            if($model->schregno){
                $model->seq = $i;
                if ($model->cmd == 'defaultStf') {
                    $model->field["REMARK".$i] = $defaultStaff["REMARK".$i];
                } else {
                    $model->field["REMARK".$i] = $db->getOne(knjd428cQuery::getHreportStaffDat($model,true));
                }
            }
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => htmlspecialchars($row["LABEL"]), "value" => $row["VALUE"]);
            }
            if ($model->field["REMARK".$i] == "") {
                $model->field["REMARK".$i] = $opt[0]["value"];
            }
            $name = "REMARK".$i;
            $extra = "";
            $arg["data"][$name] = knjCreateCombo($objForm, $name, $model->field["REMARK".$i], $opt, $extra, 1);
        }

        /**********/
        /* ボタン */
        /**********/
        //ボタン
        $extra = "onclick=\"return btn_submit('defaultStf');\"";
        $arg["button"]["btn_defaultStf"] = knjCreateBtn($objForm, "btn_defaultStf", "HR担任読込", $extra);

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
        View::toHTML5($model, "knjd428cForm1.html", $arg);
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
