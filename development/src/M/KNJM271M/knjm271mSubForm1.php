<?php

require_once('for_php7.php');


class knjm271mSubForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm271mSubForm1", "POST", "knjm271mindex.php", "", "knjm271mSubForm1");

        //更新用データ
        if ($model->cmd != 'read2' && $model->cmd != 'reset'){
            $model->sch  = VARS::get("SCHREGNOSUB");
            $model->sucl = VARS::get("SUBCLASSSUB");
            $model->date = VARS::get("DATESUB");
            $model->stsq = VARS::get("STSEQSUB");
            $model->rsq  = VARS::get("RSEQSUB");
            $model->gval = VARS::get("GRAD_VALUESUB");
            $model->chir = VARS::get("CHAIRSUB");
            $model->rdat = VARS::get("RECDAYSUB");
        }else if ($model->cmd == 'read2') {
            $model->gval = VARS::post("GVALSUB");
        }
        //データ取得
        $db = Query::dbCheckOut();
        $result = $db->query(knjm271mQuery::getsubSch($model));
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);

        //名称設定
        $arg["SCHSUB"] = $row["SCHREGNO"];
        $arg["SCHSUBNAME"] = $row["NAME_SHOW"];
        $result->free();

        $opt_grval = array();
        $result = $db->query(knjm271mQuery::getsubName());

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_grval[] = array ("label" => $row["NAME1"],
                                  "value" => $row["NAMECD2"]);
        }
        //評価
        $objForm->ae( array("type"      => "select",
                            "name"      => "GVALSUB",
                            "size"      => "1",
                            "value"     => $model->gval,
                            "extrahtml" => "",
                            "options"   => $opt_grval));
        $arg["datas"]["GVALSUB"] = $objForm->ge("GVALSUB");

        $result->free();
        Query::dbCheckIn($db);

        //更新ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_up",
                            "value"     => "更新",
                            "extrahtml" => "onclick=\"return btn_submit('update');\"" ) );

        $arg["btn_up"] = $objForm->ge("btn_up");

        //取消ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取消",
                            "extrahtml" => "onclick=\"return btn_submit('reset');\"" ) );

        $arg["btn_reset"] = $objForm->ge("btn_reset");

        //戻るボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "終了",
                            "extrahtml" => "onclick=\"return btn_submit('');\"" ) );

        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm271mSubForm1.html", $arg);
    }
}
?>
