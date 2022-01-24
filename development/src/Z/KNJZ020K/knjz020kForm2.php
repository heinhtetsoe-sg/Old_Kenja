<?php

require_once('for_php7.php');

class knjz020kForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz020kindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            $Row = knjz020kQuery::getRow($model,1);
        }else{
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //試験区分コンボ
#        $result      = $db->query(knjz020kQuery::getName($model->examyear,L003));
        $result      = $db->query(knjz020kQuery::getName($model->year,"L003"));
        $opt_testdiv = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_testdiv[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                                   "value" => $row["NAMECD2"]);
        }
        $result->free();

        //試験科目コンボ
#        $result      = $db->query(knjz020kQuery::getName($model->examyear,L009));
        $result      = $db->query(knjz020kQuery::getName($model->year,"L009"));
        $opt_subclass = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_subclass[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                                    "value" => $row["NAMECD2"]);
        }
        $result->free();

        Query::dbCheckIn($db);

        //試験区分コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTDIV",
                            "size"        => "1",
                            "value"       => $model->testdiv,
                            "options"     => $opt_testdiv));
        $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //試験科目コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTSUBCLASSCD",
                            "size"        => "1",
                            "value"       => $model->testsubclasscd,
                            "options"     => $opt_subclass));
        $arg["data"]["TESTSUBCLASSCD"] = $objForm->ge("TESTSUBCLASSCD");

        //満点
        $objForm->ae( array("type"        => "text",
                            "name"        => "A_PERFECT",
                            "size"        => 3,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["A_PERFECT"] ));
        $arg["data"]["A_PERFECT"] = $objForm->ge("A_PERFECT");

        //----------2005.08.13---alp---start----------
        //A配点集計フラグ
        $a_check = ($Row["A_TOTAL_FLG"] == "1")? "checked":"";
        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "A_TOTAL_FLG",
                            "value"       => $Row["A_TOTAL_FLG"],
                            "extrahtml"   => $a_check." onclick=\"a_check();\""));
        $arg["data"]["A_TOTAL_FLG"] = $objForm->ge("A_TOTAL_FLG");
        $arg["data"]["a_style"] = ($a_check != "checked")? "なし":"あり";
        //B配点満点
        $objForm->ae( array("type"        => "text",
                            "name"        => "B_PERFECT",
                            "size"        => 3,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["B_PERFECT"] ));
        $arg["data"]["B_PERFECT"] = $objForm->ge("B_PERFECT");
        //B配点集計フラグ
        $b_check = ($Row["B_TOTAL_FLG"] == "1")? "checked":"";
        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "B_TOTAL_FLG",
                            "value"       => $Row["B_TOTAL_FLG"],
                            "extrahtml"   => $b_check." onclick=\"b_check();\""));
        $arg["data"]["B_TOTAL_FLG"] = $objForm->ge("B_TOTAL_FLG");
        $arg["data"]["b_style"] = ($b_check != "checked")? "なし":"あり";
        //----------2005.08.13---alp---end----------

        if($Row["AUTOCALC"] == '1'){
            $check    = "checked";
            $disabled = "";
        }else{
            $check    = "";
            $disabled = "disabled";
        }
        //自動計算
        $check = ($Row["AUTOCALC"] == "1")? "checked":"";
        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "AUTOCALC",
                            "value"       => $Row["AUTOCALC"],
                            "extrahtml"   => $check." onclick=\"checkaut();\""));
        $arg["data"]["AUTOCALC"] = $objForm->ge("AUTOCALC");
        $arg["data"]["style"] = ($check != "checked")? "なし":"あり";

        //傾斜倍率
        $objForm->ae( array("type"        => "text",
                            "name"        => "INC_MAGNIFICATION",
                            "size"        => 3,
                            "maxlength"   => 3,
                            "extrahtml"   => "$disabled . onblur=\"this.value=toFloat(this.value)\"",
                            "value"       => ($Row["AUTOCALC"]=='1')? $Row["INC_MAGNIFICATION"] : "1.0" ));
        $arg["data"]["INC_MAGNIFICATION"] = $objForm->ge("INC_MAGNIFICATION");

        //アラカルト
        $arg["data"]["ADOPTIONDIV"] = ($Row["ADOPTIONDIV"] == '1')? "あり": "";

        //追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリア
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"] ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year ) );

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjz020kindex.php?cmd=list','left_frame');";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz020kForm2.html", $arg);
    }
}
?>
