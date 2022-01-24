<?php

require_once('for_php7.php');

class knjd635Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd635index.php", "", "edit");

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学期コンボ
        $db = Query::dbCheckOut();
        $opt=$opt_seme=array();
        $query = knjd635Query::getSemesterQuery();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[]= array('label' => $row["SEMESTERNAME"],
                          'value' => $row["SEMESTER"]);
            $opt_seme[$row["SEMESTER"]] = $row["SEMESTERNAME"];
        }
        $result->free();
        Query::dbCheckIn($db);

        if (!isset($model->gakki)) $model->gakki = CTRL_SEMESTER;

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => $model->gakki,
                            "extrahtml"  => "onChange=\"btn_submit('gakki');\"",
                            "options"    => $opt));
        $arg["GAKKI"] = $objForm->ge("GAKKI");


        $db = Query::dbCheckOut();

        //評価の取得
        $Row = array();
        $result = $db->query(knjd635Query::getBehavior($model->schregno, $model->gakki));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cd = $row["CODE"];
            $Row["RECORD"][$cd] = $row["RECORD"];  //評価
        }

        $result->free();
        Query::dbCheckIn($db);

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if(isset($model->warning)){
            $Row =& $model->record;
        }

        //評価
        for($i=1; $i<11; $i++)
        {
            $ival = sprintf("%02d", $i);

        $objForm->ae( array("type"        => "text",
                            "name"        => "RECORD".$i,
                            "size"        => "3",
                            "maxlength"   => "1",
                            "extrahtml"   => "STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" ",
                            "value"       => $Row["RECORD"][$ival] ));

            $arg["RECORD".$i] = $objForm->ge("RECORD".$i);

            $arg["NUMBER".$i] = $i;  //連番
        }

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //取消ボタン
        $objForm->ae( array("type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno
                            ));


        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd635Form1.html", $arg);
    }
}
?>
