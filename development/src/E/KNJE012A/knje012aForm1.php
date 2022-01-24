<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje012aForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje012aindex.php", "", "edit");
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->cmd != 'reload3') {
            $query = knje012aQuery::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row = $model->field;
        }

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;
        
        /******************/
        /* テキストエリア */
        /******************/
        //総合的な学習の時間の記録
        $extra = "style=\"height:118px;\"";
        $arg["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ($model->totalstudyval_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["TOTALSTUDYVAL"]);
        $arg["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
        //特別活動の記録
        $extra = "style=\"height:118px;\"";
        $arg["SPECIALACTREC"] = KnjCreateTextArea($objForm, "SPECIALACTREC", $model->specialactrec_gyou, ($model->specialactrec_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["SPECIALACTREC"]);
        $arg["SPECIALACTREC_TYUI"] = "(全角{$model->specialactrec_moji}文字{$model->specialactrec_gyou}行まで)";
        
        //行動の記録
        $extra = "style=\"height:90px;\"";
        $arg["BEHAVEREC_REMARK"] = KnjCreateTextArea($objForm, "BEHAVEREC_REMARK", $model->behaverec_remark_gyou, ($model->behaverec_remark_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["BEHAVEREC_REMARK"]);
        $arg["BEHAVEREC_REMARK_TYUI"] = "(全角{$model->behaverec_remark_moji}文字{$model->behaverec_remark_gyou}行まで)";
        //総合所見及び指導上参考となる諸事項
        $extra = "style=\"height:90px;\"";
        $arg["TRIN_REF"] = KnjCreateTextArea($objForm, "TRIN_REF", $model->trin_ref_gyou, ($model->trin_ref_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["TRIN_REF"]);
        $arg["TRIN_REF_TYUI"] = "(全角{$model->trin_ref_moji}文字{$model->trin_ref_gyou}行まで)";
        
        //健康の記録
        $extra = "style=\"height:90px;\"";
        $arg["HEALTHREC"] = KnjCreateTextArea($objForm, "HEALTHREC", $model->healthrec_gyou, ($model->healthrec_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["HEALTHREC"]);
        $arg["HEALTHREC_TYUI"] = "(全角{$model->healthrec_moji}文字{$model->healthrec_gyou}行まで)";
        //出欠の記録
        $extra = "style=\"height:60px;\"";
        $arg["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_gyou, ($model->attendrec_remark_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["ATTENDREC_REMARK"]);
        $arg["ATTENDREC_REMARK_TYUI"] = "(全角{$model->attendrec_remark_moji}文字{$model->attendrec_remark_gyou}行まで)";

        /**********/
        /* ボタン */
        /**********/
        //特別な活動～ボタンを作成する
        $extra = "onclick=\"return btn_submit('form2_first');\" style=\"width:250px\"";
        $title = '指導要録所見　参照';
        $arg["btn_form2"] = knjCreateBtn($objForm, "btn_form2", $title, $extra);
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
        View::toHTML($model, "knje012aForm1.html", $arg);
    }
}
?>
