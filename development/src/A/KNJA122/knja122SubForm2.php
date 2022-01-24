<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja122SubForm2 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("subform2", "POST", "knja122index.php", "", "subform2");
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;
        //DB接続
        $db = Query::dbCheckOut();
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row  = knja122Query::getTrainRow($model, $db);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row  =& $model->field2;
        }

        /******************/
        /* テキストエリア */
        /******************/
        //学級活動
        $extra = "style=\"height:80px;width:230px;\"";
        $arg["data"]["CLASSACT"]    = knjCreateTextArea($objForm, "CLASSACT",    5, 14, "hard", $extra, $row["CLASSACT"]);
        //生徒会活動
        $extra = "style=\"height:80px;width:230px;\"";
        $arg["data"]["STUDENTACT"]  = knjCreateTextArea($objForm, "STUDENTACT",  5, 14, "hard", $extra, $row["STUDENTACT"]);
        //クラブ活動
        $extra = "style=\"height:80px;width:230px;\"";
        $arg["data"]["CLUBACT"]     = knjCreateTextArea($objForm, "CLUBACT",     5, 14, "hard", $extra, $row["CLUBACT"]);
        //学校行事
        $extra = "style=\"height:80px;width:230px;\"";
        $arg["data"]["SCHOOLEVENT"] = knjCreateTextArea($objForm, "SCHOOLEVENT", 5, 14, "hard", $extra, $row["SCHOOLEVENT"]);

        /**********/
        /* ボタン */
        /**********/
        //戻る
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻る', "onclick=\"return parent.closeit()\"");
        //更新
        $extra = "onclick=\"return btn_submit('update2');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);
        //取消
        $extra = "onclick=\"return btn_submit('clear2');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja122SubForm2.html", $arg);
    }
}
?>
