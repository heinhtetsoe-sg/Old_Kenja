<?php

require_once('for_php7.php');

class adoptiondiv
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("ado", "POST", "knjz020kindex.php", "", "ado");

        $db     = Query::dbCheckOut();

        //試験区分コンボ
        $opt = array();
        $result      = $db->query(knjz020kQuery::getName($model->year,"L003"));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $model->adotestdiv = (isset($model->adotestdiv))? $model->adotestdiv : $opt[0]["value"] ;
        $objForm->ae( array("type"        => "select",
                            "name"        => "ADOTESTDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('change_testdiv')\";",
                            "value"       => $model->adotestdiv,
                            "options"     => $opt) );
        $arg["data"]["ADOTESTDIV"] = $objForm->ge("ADOTESTDIV");

        //アラカルト科目一覧
        $result = $db->query(knjz020kQuery::getAdoSubclass($model));
        $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_left[] = array("label" => htmlspecialchars($row["TESTSUBCLASSCD"]." ".$row["NAME1"]),
                                "value" => $row["TESTSUBCLASSCD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "L_SUBCLASS",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%; HEIGHT:100px;\" ondblclick=\"move('right','L_SUBCLASS','R_SUBCLASS',1)\"",
#                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%; HEIGHT:100px;\" ondblclick=\"move1('left')\"",
                            "options"     => $opt_left));

        //科目一覧
        $result = $db->query(knjz020kQuery::getSubclass($model));
        $opt_right = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_right[] = array("label" => htmlspecialchars($row["TESTSUBCLASSCD"]." ".$row["NAME1"]),
                                 "value" => $row["TESTSUBCLASSCD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "R_SUBCLASS",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%; HEIGHT:100px;\" ondblclick=\"move('left','L_SUBCLASS','R_SUBCLASS',1)\"",
#                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%; HEIGHT:100px;\" ondblclick=\"move1('left')\"",
                            "options"     => $opt_right));

        //追加削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','L_SUBCLASS','R_SUBCLASS',1);\"" ) );
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','L_SUBCLASS','R_SUBCLASS',1);\"" ) );
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','L_SUBCLASS','R_SUBCLASS',1);\"" ) );
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','L_SUBCLASS','R_SUBCLASS',1);\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("L_SUBCLASS"),
                                   "RIGHT_PART"  => $objForm->ge("R_SUBCLASS"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        $result->free();
        Query::dbCheckIn($db);

        //修正
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_udpate",
                            "value"     => "更 新",
                            "extrahtml" => "onclick=\"return btn_submit('adoupdate');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //クリア
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('adoreset')\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"return btn_submit('end')\"" ) );
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        $arg["IFRAME"] = VIEW::setIframeJs();

        //hiddenを作成する
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "cmd" ) );
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "selectdata" ) );

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "adoptiondiv.html", $arg);
    }
}
?>
