<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd131nForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd131nindex.php", "", "edit");
        $db = Query::dbCheckOut();

        if (!isset($model->warning)) {
            $query = knjd131nQuery::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row = $model->field;
        }

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;
        
        //学期名称を取得
        $model->SemesterName1 = $db->getOne(knjd131nQuery::getSemesterName("1"));
        $model->SemesterName2 = $db->getOne(knjd131nQuery::getSemesterName("2"));
        $model->SemesterName3 = $db->getOne(knjd131nQuery::getSemesterName("3"));

        $arg["SEMESTERNAME1"] = $model->SemesterName1;
        $arg["SEMESTERNAME2"] = $model->SemesterName2;
        $arg["SEMESTERNAME3"] = $model->SemesterName3;

        /******************/
        /* テキストエリア */
        /******************/
        //1学期中間
        $extra = "style=\"height:118px;\" onPaste=\"return showPaste(this);\"";
        $arg["SEM1_01_REMARK"] = KnjCreateTextArea($objForm, "SEM1_01_REMARK", $model->sem1_01_remark_gyou, ($model->sem1_01_remark_moji * 2 + 1), "soft", $extra, $row["SEM1_01_REMARK"]);
        $arg["SEM1_01_REMARK_TYUI"] = "(全角{$model->sem1_01_remark_moji}文字{$model->sem1_01_remark_gyou}行まで)";
        //1学期期末
        $extra = "style=\"height:118px;\" onPaste=\"return showPaste(this);\"";
        $arg["SEM1_02_REMARK"] = KnjCreateTextArea($objForm, "SEM1_02_REMARK", $model->sem1_02_remark_gyou, ($model->sem1_02_remark_moji * 2 + 1), "soft", $extra, $row["SEM1_02_REMARK"]);
        $arg["SEM1_02_REMARK_TYUI"] = "(全角{$model->sem1_02_remark_moji}文字{$model->sem1_02_remark_gyou}行まで)";
        
        //2学期中間
        $extra = "style=\"height:118px;\" onPaste=\"return showPaste(this);\"";
        $arg["SEM2_01_REMARK"] = KnjCreateTextArea($objForm, "SEM2_01_REMARK", $model->sem2_01_remark_gyou, ($model->sem2_01_remark_moji * 2 + 1), "soft", $extra, $row["SEM2_01_REMARK"]);
        $arg["SEM2_01_REMARK_TYUI"] = "(全角{$model->sem2_01_remark_moji}文字{$model->sem2_01_remark_gyou}行まで)";
        //2学期期末
        $extra = "style=\"height:118px;\" onPaste=\"return showPaste(this);\"";
        $arg["SEM2_02_REMARK"] = KnjCreateTextArea($objForm, "SEM2_02_REMARK", $model->sem2_02_remark_gyou, ($model->sem2_02_remark_moji * 2 + 1), "soft", $extra, $row["SEM2_02_REMARK"]);
        $arg["SEM2_02_REMARK_TYUI"] = "(全角{$model->sem2_02_remark_moji}文字{$model->sem2_02_remark_gyou}行まで)";
        
        //3学期期末
        $extra = "style=\"height:118px;\" onPaste=\"return showPaste(this);\"";
        $arg["SEM3_02_REMARK"] = KnjCreateTextArea($objForm, "SEM3_02_REMARK", $model->sem3_02_remark_gyou, ($model->sem3_02_remark_moji * 2 + 1), "soft", $extra, $row["SEM3_02_REMARK"]);
        $arg["SEM3_02_REMARK_TYUI"] = "(全角{$model->sem3_02_remark_moji}文字{$model->sem3_02_remark_gyou}行まで)";

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
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "PROGRAMID", PROGRAMID);
        knjCreateHidden($objForm, "SemesterName1", $model->SemesterName1);
        knjCreateHidden($objForm, "SemesterName2", $model->SemesterName2);
        knjCreateHidden($objForm, "SemesterName3", $model->SemesterName3);
        knjCreateHidden($objForm, "sem1_01_remark_gyou", $model->sem1_01_remark_gyou);
        knjCreateHidden($objForm, "sem1_01_remark_moji", $model->sem1_01_remark_moji);
        knjCreateHidden($objForm, "sem1_02_remark_gyou", $model->sem1_02_remark_gyou);
        knjCreateHidden($objForm, "sem1_02_remark_moji", $model->sem1_02_remark_moji);
        knjCreateHidden($objForm, "sem2_01_remark_gyou", $model->sem2_01_remark_gyou);
        knjCreateHidden($objForm, "sem2_01_remark_moji", $model->sem2_01_remark_moji);
        knjCreateHidden($objForm, "sem2_02_remark_gyou", $model->sem2_02_remark_gyou);
        knjCreateHidden($objForm, "sem2_02_remark_moji", $model->sem2_02_remark_moji);
        knjCreateHidden($objForm, "sem3_02_remark_gyou", $model->sem3_02_remark_gyou);
        knjCreateHidden($objForm, "sem3_02_remark_moji", $model->sem3_02_remark_moji);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if(get_count($model->warning)== 0 && $model->cmd !="reset") {
            $arg["next"] = "NextStudent2(0);";
        } elseif($model->cmd =="reset") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd131nForm1.html", $arg);
    }
}
?>
