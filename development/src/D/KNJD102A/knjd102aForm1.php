<?php

require_once('for_php7.php');

class knjd102aForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd102aindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学期表示
        $arg["data"]["SEMESTERNAME"] = $model->control["学期名"][$model->exp_semester];

        //テスト種別コンボ
        $opt_kind = array();
        $query = knjd102aQuery::getTest($model);
        $result = $db->query($query);
        $test_flg = true;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_kind[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
            if( $model->field["TESTKINDCD"] == $row["VALUE"] ) $test_flg = false;
        }
        if($test_flg) $model->field["TESTKINDCD"] = $opt_kind[0]["value"];
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTKINDCD",
                            "size"       => "1",
                            "value"      => $model->field["TESTKINDCD"],
                            "extrahtml"  => "onChange=\"btn_submit('edit');\"",
                            "options"    => $opt_kind));

        $arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)){
            $row = $db->getRow(knjd102aQuery::getRecordRemark($model),DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;

        } else {
            $row =& $model->field;
        }

        //連絡欄
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "REMARK1",
                            "cols"        => 61,
                            "rows"        => 5,
                            "extrahtml"   => "style=\"height:75px;\"",
                            "value"       => $row["REMARK1"] ));
        $arg["data"]["REMARK1"] = $objForm->ge("REMARK1");

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');

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

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno
                            ));

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd102aForm1.html", $arg);
    }
}
?>
