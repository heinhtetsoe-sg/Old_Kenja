<?php

require_once('for_php7.php');


class knjm390SubForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm390SubForm1", "POST", "knjm390index.php", "", "knjm390SubForm1");

        //更新用データ
        if ($model->cmd != 'read2' && $model->cmd != 'reset'){
            $model->sch = VARS::get("SCHREGNOSUB");
            $model->chir = VARS::get("CHAIRSUB");
            $model->date = VARS::get("DATESUB");
            $model->peri = VARS::get("PERIODSUB");
            $model->stf = VARS::get("STAFFSUB");
            $model->crs = VARS::get("COURSESUB");
            $model->seq = VARS::get("SEQSUB");
        }
        //データ取得
        $db = Query::dbCheckOut();
        $result = $db->query(knjm390Query::getsubremark($model));
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);

        //名称設定
        $arg["SCHSUB"] = $row["SCHREGNO"];
        $arg["SCHSUBNAME"] = $row["NAME_SHOW"];

        //備考
        $extra = "style=\"height:35px;\"";
        $arg["RMKSUB"] = knjCreateTextArea($objForm, "REMARKSUB", "2", "20", "", $extra, $row["REMARK"]);

        $result->free();
        Query::dbCheckIn($db);

        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_up"] = knjCreateBtn($objForm, "btn_up", "更新", $extra);

        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取消", $extra);

        //戻るボタンを作成する
        $extra = "onclick=\"return btn_submit('');\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "終了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm390SubForm1.html", $arg);
    }
}
?>
