<?php

require_once('for_php7.php');


class knjm120Form2
{

    function main(&$model)
    {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjm120index.php", "", "edit");

        //年度初期設定
        if (!$model->Year)  $model->Year = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //講座初期設定
        $opt_chair = array();
        $result = $db->query(knjm120Query::getAuth($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_chair[] = array("label" => $row["CHAIRNAME"],
                                 "value" => $row["CHAIRCD"]);
        }

        if (!$model->Chair){
            $model->Chair        = $opt_chair[0]["value"];
        }

        //講座名称
        $query = knjm120Query::getChairname($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["CHAIRCD_SHOW"] = $row["CHAIRNAME"];

        $query = knjm120Query::getAttendDate();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->upmindate = $row["ATTEND_CTRL_DATE"];

        //日付データ
        if (VARS::get("init") == 1 ){
            $model->Exedate = CTRL_DATE;
        }
        if (!$model->warning && $model->cmd != "edit2"){
            if ($model->Exedate == "") $model->Exedate = str_replace("-", "/", CTRL_DATE);
        }else {
            if ($model->field["EXEDATE2"] == ""){
                $model->Exedate = str_replace("-", "/", CTRL_DATE);
            }else {
                $model->Exedate = $model->field["EXEDATE2"];
            }
        }

        $arg["data"]["EXEDATE2"] = View::popUpCalendar($objForm ,"EXEDATE2" ,str_replace("-", "/", $model->Exedate));

        //校時設定
        $opt_peri = array();
        $result = $db->query(knjm120Query::selectPeriod($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_peri[] = array("label" => $row["NAME1"],
                                "value" => $row["NAMECD2"]);
        }
        if ($model->warning || $model->cmd == "edit2"){
            $model->Periodcd = $model->field["KOUJI2"];
        }
        $objForm->ae( array("type"      => "select",
                            "name"      => "KOUJI2",
                            "size"      => "1",
                            "value"     => $model->Periodcd,
                            "options"   => $opt_peri));

        $arg["data"]["KOUJI2"] = $objForm->ge("KOUJI2");

        $result->free();

        //追加ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_add",
                            "value"       => "追　加",
                            "extrahtml"   => " onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //更新ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "更　新",
                            "extrahtml"   => " onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削　除",
                            "extrahtml"   => " onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //取消ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_reset",
                            "value"       => "取　消",
                            "extrahtml"   => " onclick=\"return btn_submit('reset');\"") );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終　了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        //更新後データをそのまま表示させる為、edit2を使用
        if ($model->cmd == "edit2") $model->cmd = "edit";

        if (VARS::get("cmd") != "edit" && !isset($model->warning)){
            $arg["reload"]  = "window.open('knjm120index.php?cmd=list&ed=1','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm120Form2.html", $arg);
    }
}
?>
