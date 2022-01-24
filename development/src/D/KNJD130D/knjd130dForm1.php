<?php

require_once('for_php7.php');

class knjd130dForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd130dindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = knjd130dQuery::getTrainRow($model->schregno);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //特別活動の記録
        if ($model->tutisyoTokubetuKatudo == '1') {
            $extra = "style=\"height:48px;\"";
            $arg["data"]["SPECIALACTREMARK"] = KnjCreateTextArea($objForm, "SPECIALACTREMARK", 2, 43, "soft", $extra, $row["SPECIALACTREMARK"]);
        }
        //連絡事項
        $extra = "style=\"height:105px;\"";
        $arg["data"]["TOTALSTUDYTIME"] = KnjCreateTextArea($objForm, "TOTALSTUDYTIME", 7, 43, "soft", $extra, $row["TOTALSTUDYTIME"]);

        /**********/
        /* ボタン */
        /**********/
        if ($model->tutisyoTokubetuKatudo == '1') {
            //部活動参照
            $extra = "onclick=\"return btn_submit('subform1');\"";
            $arg["button"]["btn_club"] = knjCreateBtn($objForm, "btn_club", "部活動参照", $extra);
            //委員会参照
            $extra = "onclick=\"return btn_submit('subform2');\"";
            $arg["button"]["btn_committee"] = knjCreateBtn($objForm, "btn_committee", "委員会参照", $extra);
        }
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //一括更新ボタン
        $link  = REQUESTROOT."/D/KNJD130D/knjd130dindex.php?cmd=replace&SCHREGNO=".$model->schregno;
        $extra = "style=\"width:80px\" onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);
        //ＣＳＶ処理
        $fieldSize = "";
        if ($model->tutisyoTokubetuKatudo == '1') {
            $fieldSize  = "SPECIALACTREMARK=126,";
        }
        $fieldSize  .= "TOTALSTUDYTIME=441";
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX156/knjx156index.php?FIELDSIZE=".$fieldSize."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ処理", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd130dForm1.html", $arg);
    }
}
?>
