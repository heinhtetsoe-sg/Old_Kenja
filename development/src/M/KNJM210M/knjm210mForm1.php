<?php

require_once('for_php7.php');

class knjm210mForm1
{   
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjm210mindex.php", "", "main");

        //学籍番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHREGNO",
                            "size"        => 8,
                            "maxlength"   => 8,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";",
                            "value"       => $model->field["SCHREGNO"] ));
        //氏名（漢字）
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "value"       => $model->field["NAME"] ));
        //氏名（かな）
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME_KANA",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "value"       => $model->field["NAME_KANA"] ));

        $arg["data"] = array("SCHREGNO"  => $objForm->ge("SCHREGNO"),
                             "NAME"      => $objForm->ge("NAME"),
                             "NAME_KANA" => $objForm->ge("NAME_KANA"),
                             "GRADE"     => $objForm->ge("GRADE") );  

        //検索結果
        if ($model->field["SCHREGNO"] != "" || 
            $model->field["NAME"] != "" || 
            $model->field["NAME_KANA"] != "" || 
            $model->field["GRADE"] != "" ) {
            $db = Query::dbCheckOut();
            $result = $db->query(knjm210mQuery::getSearch($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                array_walk($row, "htmlspecialchars_array");

                $row["CLASS_NAME"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
                //リンク設定
                $js = "wopen('../KNJM210M_2/knjm210m_2index.php?SCHNO={$row["SCHREGNO"]}&PROGRAMID=PROGRAMID','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
                $row["NAME"] = View::alink("#", htmlspecialchars($row["NAME"]), "onClick=\"$js\"");
                $arg["data2"][]  = $row;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_ok",
                            "value"       => "検 索",
                            "extrahtml"   => "onclick=\"return btn_submit('execute');\"" ));

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_cancel",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"] = array("BTN_OK"     => $objForm->ge("btn_ok"),
                               "BTN_CLEAR"  => $objForm->ge("btn_cancel") );  
        
        //HIDDEN
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm210mForm1.html", $arg); 
    }
}
?>
