<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja121aForm2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("form2", "POST", "knja121aindex.php", "", "form2");

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        $db = Query::dbCheckOut();

        //記録の取得
        $Row = array();
        $result = $db->query(knja121aQuery::getBehavior($model));

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $scd = $row["DIV"] .$row["CODE"];

            $Row["RECORD"][$scd] = $row["RECORD"];  //記録
        }

        $result->free();
        Query::dbCheckIn($db);

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if(isset($model->warning)){
            $Row =& $model->record;
            $row =& $model->field;
        }

        //行動の記録
        for($i=1; $i<11; $i++)
        {
            $ival = "1" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";

            //記録
            $objForm->ae( array("type"      => "checkbox",
                                "name"      => "RECORD".$ival,
                                "value"     => "1",
                                "extrahtml" => $check1) );

            $arg["RECORD".$ival] = $objForm->ge("RECORD".$ival);
        }

        //特別活動の記録
        for($i=1; $i<4; $i++)
        {
            $ival = "2" . sprintf("%02d", $i);
            $check2 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";

            //記録
            $objForm->ae( array("type"      => "checkbox",
                                "name"      => "RECORD".$ival,
                                "value"     => "1",
                                "extrahtml" => $check2) );

            $arg["RECORD".$ival] = $objForm->ge("RECORD".$ival);
        }


        //更新ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_udpate",
                            "value"     => "更 新",
                            "extrahtml" => "onclick=\"return btn_submit('update2');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //クリアボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('clear')\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"return top.main_frame.right_frame.closeit()\"" ));

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
                                                
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja121aForm2.html", $arg);
    }
}
?>