<?php

require_once('for_php7.php');

class knjm390eSubForm1 {

    function main(&$model) {

        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjm390eSubForm1", "POST", "knjm390eindex.php", "", "knjm390eSubForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //更新用データ
        if ($model->cmd != 'read2' && $model->cmd != 'reset') {
            $model->sch     = VARS::get("SCHREGNOSUB");
            $model->chir    = VARS::get("CHAIRSUB");
            $model->date    = VARS::get("DATESUB");
            $model->peri    = VARS::get("PERIODSUB");
            $model->stf     = VARS::get("STAFFSUB");
            $model->crs     = VARS::get("COURSESUB");
            $model->time    = VARS::get("RECEIPTTIMESUB");
        }

        //データ取得
        $result = $db->query(knjm390eQuery::getsubremark($model));
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);

        //名称設定
        $arg["SCHSUB"] = $row["SCHREGNO"];
        $arg["SCHSUBNAME"] = $row["NAME_SHOW"];

        //備考
        $extra = "style=\"height:35px;\"";
        $arg["RMKSUB"] = KnjCreateTextArea($objForm, "REMARKSUB", 2, 20, "soft", $extra, $row["REMARK"]);

        $result->free();

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_up"] = knjCreateBtn($objForm, "btn_up", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $extra = "onclick=\"return btn_submit('');\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm390eSubForm1.html", $arg);
    }
}
?>
