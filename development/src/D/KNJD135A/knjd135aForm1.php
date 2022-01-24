<?php

require_once('for_php7.php');

class knjd135aForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd135aindex.php", "", "edit");
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $query = knjd135aQuery::getTrainRow($model->schregno);
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);

            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        /************/
        /* 生徒情報 */
        /************/
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        /******************/
        /* テキストエリア */
        /******************/
        //通信欄
        $extra = "style=\"height:77px;\"";
        $arg["data"]["COMMUNICATION"] = KnjCreateTextArea($objForm, "COMMUNICATION", 5, 61, "soft", $extra, $row["COMMUNICATION"]);
        $arg["data"]["COMMUNICATION_TITLE"] = $model->Properties["tutisyoSyokenTitle"] ? $model->Properties["tutisyoSyokenTitle"] : '通信欄';

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へ
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');
        //取消
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //ＣＳＶ処理
        $fieldSize  = "COMMUNICATION=458";
        //ＣＳＶ出力
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX135A/knjx135aindex.php?FIELDSIZE={$fieldSize}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd135aForm1.html", $arg);
    }
}
?>
