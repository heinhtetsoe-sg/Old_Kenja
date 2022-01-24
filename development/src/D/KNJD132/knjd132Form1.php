<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd132Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd132index.php", "", "edit");
        $db = Query::dbCheckOut();

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        /******************/
        /* コンボボックス */
        /******************/
        //学期
        $opt = array();
        $query = knjd132Query::getSemester();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $extra = "onChange=\"btn_submit('edit');\"";
        $arg["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        /**********/
        /* 学期名 */
        /**********/
        $query = knjd132Query::getSemesterName($model->field["SEMESTER"]);
        $semesterName = $db->getOne($query);
        $arg["SEMESTERNAME"] = $semesterName;

        /********************/
        /* テキストボックス */
        /********************/
        //備考1
        if (!isset($model->warning)) {
            $query = knjd132Query::selectRemark1($model);
            $remark = $db->getOne($query);
            $arg["NOT_WARNING"] = 1;
        } else {
            $remark = $model->field["REMARK1"];
        }
        $extra = " onPaste=\"return showPaste(this);\"";
        $arg["REMARK1"] = knjCreateTextBox($objForm, $remark, "REMARK1", 80, 80, $extra);

        //備考2
        if (!isset($model->warning)) {
            $query = knjd132Query::selectRemark2($model);
            $remark = $db->getOne($query);
            $arg["NOT_WARNING"] = 1;
        } else {
            $remark = $model->field["REMARK2"];
        }
        if ($model->field["SEMESTER"] == '9') {
            $extra = " disabled='disabled' ";
        } else {
            $extra = " onPaste=\"return showPaste(this);\"";
        }
        $arg["REMARK2"] = knjCreateTextBox($objForm, $remark, "REMARK2", 80, 80, $extra);

        /**********/
        /* ボタン */
        /**********/
        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
        //取消しボタンを作成する
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "nextURL", $model->nextURL);
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "mode", $model->mode);
        knjCreateHidden($objForm, "PROGRAMID", PROGRAMID);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if (get_count($model->warning) == 0 && $model->cmd != "reset") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "reset") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd132Form1.html", $arg);
    }
}
?>
