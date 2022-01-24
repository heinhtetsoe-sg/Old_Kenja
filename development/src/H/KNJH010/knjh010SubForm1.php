<?php

require_once('for_php7.php');

class knjh010SubForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjh010index.php", "", "sel");

        $Row = knjh010Query::getSchreg_Envir_Dat($model);

        $db = Query::dbCheckOut();
        //こづかい(コンボ)
        $opt3 = array();
        $opt3[] = array("label" => "","value" => "0");
        $result = $db->query(knjh010Query::getV_name_mst("H104",$model));
        while($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt3[] =array("label"     => $row3["NAMECD2"]."：".$row3["NAME1"],
                             "value" => $row3["NAMECD2"]);
        }
        $result->free();
        $objForm->ae( array("type"     => "select",
                            "name"     => "POCKETMONEYCD",
                            "value"     => $Row["POCKETMONEYCD"],
                            "options"=> $opt3));
        $arg["data"]["POCKETMONEYCD"] = $objForm->ge("POCKETMONEYCD");
        //既往の疾患
        $objForm->ae( array("type"        => "text",
                            "name"        => "DISEASE",
                            "value"        => $Row["DISEASE"],
                            "size"        => "20",
                            "maxlength" => "10"));
        $arg["data"]["DISEASE"] = $objForm->ge("DISEASE");
        //現在の健康状態
        $objForm->ae( array("type"        => "text",
                            "name"        => "HEALTHCONDITION",
                            "value"         => $Row["HEALTHCONDITION"],
                            "size"        => "20",
                            "maxlength" => "10"));
        $arg["data"]["HEALTHCONDITION"] = $objForm->ge("HEALTHCONDITION");
        //こづかい
        $objForm->ae( array("type"        => "text",
                            "name"        => "POCKETMONEY",
                            "value"        => $Row["POCKETMONEY"],
                            "size"        => "4",
                            "maxlength" => "4",
                            "extrahtml" => "onBlur=\"return to_Integer(this);\""));
        $arg["data"]["POCKETMONEY"] = $objForm->ge("POCKETMONEY");
        //睡眠 就寝時間
        $objForm->ae( array("type"        => "text",
                            "name"        => "BEDTIME_HOURS",
                            "value"        => $Row["BEDTIME_HOURS"],
                            "size"        => "2",
                            "maxlength" => "2",
                            "extrahtml" => "onBlur=\" return onBlur=to_Integer(this);\""));
        $arg["data"]["BEDTIME_HOURS"] = $objForm->ge("BEDTIME_HOURS");
        //睡眠 就寝分
        $objForm->ae( array("type"        => "text",
                            "name"        => "BEDTIME_MINUTES",
                            "value"        => $Row["BEDTIME_MINUTES"],
                            "size"        => "2",
                            "maxlength" => "2",
                            "extrahtml" => "onBlur=\" return onBlur=to_Integer(this);\""));
        $arg["data"]["BEDTIME_MINUTES"] = $objForm->ge("BEDTIME_MINUTES");
        //睡眠 起床時間
        $objForm->ae( array("type"        => "text",
                            "name"        => "RISINGTIME_HOURS",
                            "value"        => $Row["RISINGTIME_HOURS"],
                            "size"        => "2",
                            "maxlength" => "2",
                            "extrahtml" => "onBlur=\"return onBlur=to_Integer(this);\""));
        $arg["data"]["RISINGTIME_HOURS"] = $objForm->ge("RISINGTIME_HOURS");
        //睡眠 起床時間
        $objForm->ae( array("type"        => "text",
                            "name"        => "RISINGTIME_MINUTES",
                            "value"        => $Row["RISINGTIME_MINUTES"],
                            "size"        => "2",
                            "maxlength" => "2",
                            "extrahtml" => "onBlur=\"return onBlur=to_Integer(this);\""));
        $arg["data"]["RISINGTIME_MINUTES"] = $objForm->ge("RISINGTIME_MINUTES");
        //テレビの視聴時間(コンボ)
        $opt7 = array();
        $opt7[] = array("label" => "","value" => "0");
        $result = $db->query(knjh010Query::getV_name_mst("H105",$model));
        while($row7 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt7[] =array("label" => $row7["NAMECD2"]."：".$row7["NAME1"],
                             "value" => $row7["NAMECD2"]);
        }
        $result->free();
        $objForm->ae( array("type"     => "select",
                            "name"     => "TVVIEWINGHOURSCD",
                            "value"     => $Row["TVVIEWINGHOURSCD"],
                            "options"=> $opt7));
        $arg["data"]["TVVIEWINGHOURSCD"] = $objForm->ge("TVVIEWINGHOURSCD");
        //主に見るテレビ
        $objForm->ae( array("type"        => "text",
                            "name"        => "TVPROGRAM",
                            "value"        => $Row["TVPROGRAM"],
                            "size"        => "20",
                            "maxlength" => "10"));
        $arg["data"]["TVPROGRAM"] = $objForm->ge("TVPROGRAM");
        //パソコン時間(コンボ)
        $opt8 = array();
        $opt8[] = array("label" => "","value" => "0");
        $result = $db->query(knjh010Query::getV_name_mst("H105",$model));
        while($row8 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt8[] =array("label"     => $row8["NAMECD2"]."：".$row8["NAME1"],
                             "value" => $row8["NAMECD2"]);
        }
        $result->free();
        $objForm->ae( array("type"     => "select",
                            "name"     => "PC_HOURS",
                            "value"     => $Row["PC_HOURS"],
                            "options"=> $opt8));
        $arg["data"]["PC_HOURS"] = $objForm->ge("PC_HOURS");

        Query::dbCheckIn($db);

        //更新ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_up",
                            "value"     => "更新",
                            "extrahtml" => "onclick=\"return btn_submit('update2');\"" ) );

        $arg["btn_up"] = $objForm->ge("btn_up");

        //取消ボタンを作成する
        $objForm->ae( array("type"      => "button",
                               "name"      => "btn_reset",
                               "value"     => "取消",
                               "extrahtml" => "onclick=\"return btn_submit('reset2');\"" ) );
        $arg["btn_reset"] = $objForm->ge("btn_reset");

        //プログラム判定
        if (VARS::get("PRG") == "KNJH160"){
            $model->prg = "KNJH160";
        }

        //戻るボタン
        if ($model->prg == "KNJH160"){
            $link = REQUESTROOT."/H/KNJH010/knjh010index.php?cmd=back2&ini2=1&PRG=KNJH160";
            $model->prg = "KNJH160";
        }else {
            $link = REQUESTROOT."/H/KNJH010/knjh010index.php?cmd=back&ini2=1";
        }
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"window.open('$link','_self');\"" ) );
                    
        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
           View::toHTML($model, "knjh010SubForm1.html", $arg);
    }
}
?>
