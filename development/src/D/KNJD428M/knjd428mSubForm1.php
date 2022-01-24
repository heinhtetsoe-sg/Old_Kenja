<?php

require_once('for_php7.php');
class knjd428mSubForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjd428mindex.php", "", "subform1");

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
        //校種
        $query = knjd428mQuery::getSchoolKind($model);
        $schoolKind = $db->getOne($query);

        /********************/
        /* テキストボックス */
        /********************/
        $arg["REMARK_TITLE"] = $db->getOne(knjd428mQuery::getTitleName($model));
        $remark_cnt = $db->getOne(knjd428mQuery::getCntRemark($model));
        for ($i = 1; $i <= $remark_cnt; $i++) {
            //年間目標
            $seq = sprintf("%03d", $i);
            //指導計画から取込
	        if($model->cmd == 'input'){
                $remark = $db->getOne(knjd428mQuery::getinputData($model,$i));
            } else {
                $remark = $db->getOne(knjd428mQuery::getRemark($model,$i));
            }
            $remark_name = $db->getOne(knjd428mQuery::getRemarkName($model,$seq));
            $extra = " id=\"REMARK$i\" onkeyup=\"charCount(this.value, {$model->remark_gyou}, ({$model->remark_moji} * 2), true);\"";
            $remark_text_area = knjCreateTextArea($objForm, "REMARK".$i, $model->remark_gyou, ($model->remark_moji * 2), "soft", $extra, $remark);
            $remark_comment = "(全角".$model->remark_moji."文字X".$model->remark_gyou."行まで)";
            $arg["REMARK"] .= "<tr align='center'><td class='no_search' align='center' nowrap width='100'>".$remark_name."</td><td bgcolor='#ffffff' align='left' valign='top' colspan='2'>&nbsp;$remark_text_area<br>&nbsp;<font size=2, color='red'>$remark_comment</font></td></tr>";
            knjCreateHidden($objForm, "REMARK_NAME".$i, $remark_name);
        }
        knjCreateHidden($objForm, "REMARK_CNT", $remark_cnt);

        /**********/
        /* ボタン */
        /**********/
        //指導計画から取込ボタン
        $extra = "onclick=\"return btn_submit('input');\"";
        $arg["button"]["btn_input"] = KnjCreateBtn($objForm, "btn_input", "指導計画から取込", $extra);

        //更新ボタン
        $extra = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update_remark')\"" : "disabled";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset_remark');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $extra = "onclick=\"return btn_submit('edit');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

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
        if ($model->cmd == "subform1") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd428mSubForm1.html", $arg);
    }
}
?>
