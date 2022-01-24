<?php

require_once('for_php7.php');

class knjh020_2Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh020_2index.php", "", "edit");

        if (!isset($model->warning))
        {
            if($model->relano){
                $Row_relative = knjh020_2Query::getRow_relative($model);
            }
        } else {
               $Row_relative =& $model->field;
        }
        global $sess;
        //テキストエリア
        $objForm->ae( array("type"       => "text",
                            "name"        => STUCD,
                            "size"        => 10,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $model->stucd));
        $arg["data"]["STUCD"] = $objForm->ge("STUCD");
        //検索ボタンを作成する
        $objForm->ae( array("type"       => "button",
                           "name"           => "btn_stucd",
                           "value"          => "兄弟姉妹検索",
                           "extrahtml"      => "style=\"width:140px\"onclick=\"loadwindow('./knjh020_2SubForm1.php?cmd=search&MODE=reflect&CD=$model->schregno&SCHREGNO='+document.forms[0]['STUCD'].value+'&STUCD_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 600, 300)\"") );
        $arg["data"]["BTN_STUCD"] = $objForm->ge("btn_stucd");
        //反映ボタンを作成する
        $objForm->ae( array("type"       => "button",
                            "name"       => "btn_apply",
                            "value"      => "反映",
                            "extrahtml"  => "onclick=\"return btn_submit('apply');\"") );
        $arg["data"]["BTN_APPLY"] = $objForm->ge("btn_apply");

        //親族氏名
        $objForm->ae( array("type"        => "text",
                            "name"        => "RELANAME",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_relative["RELANAME"] ));

        $arg["data"]["RELANAME"] = $objForm->ge("RELANAME");

        //親族氏名かな
        $objForm->ae( array("type"        => "text",
                            "name"        => "RELAKANA",
                            "size"        => 40,
                            "maxlength"   => 80,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_relative["RELAKANA"] ));

        $arg["data"]["RELAKANA"] = $objForm->ge("RELAKANA");

        //性別
        $db     = Query::dbCheckOut();
        $query  = knjh020_2Query::getNameMst_data("Z002");
        $result = $db->query($query);
        //性別コンボボックスの中身を作成------------------------------
        $opt_sex   = array();
        $opt_sex[] = array("label" => "","value" => "0");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_sex[] = array("label" => $row["NAMECD2"]."：".htmlspecialchars($row["NAME2"]),"value" => $row["NAMECD2"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "RELASEX",
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_relative["RELASEX"],
                            "options"     => $opt_sex));

        $arg["data"]["RELASEX"] = $objForm->ge("RELASEX");

        //生年月日カレンダーコントロール
        $arg["data"]["RELABIRTHDAY"] = View::popUpCalendar($objForm, "RELABIRTHDAY", str_replace("-","/",$Row_relative["RELABIRTHDAY"]),"");

        //続柄
        $query  = knjh020_2Query::getNameMst_data("H201");
        $result = $db->query($query);
        //続柄コンボボックスの中身を作成------------------------------
        $opt_relat   = array();
        $opt_relat[] = array("label" => "","value" => "00");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_relat[] = array("label" => $row["NAMECD2"]."：".htmlspecialchars($row["NAME1"]),"value" => $row["NAMECD2"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "RELATIONSHIP",
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_relative["RELATIONSHIP"],
                            "options"     => $opt_relat));

        $arg["data"]["RELATIONSHIP"] = $objForm->ge("RELATIONSHIP");

        //職業または学校
        $objForm->ae( array("type"        => "text",
                            "name"        => "OCCUPATION",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_relative["OCCUPATION"] ));

        $arg["data"]["OCCUPATION"] = $objForm->ge("OCCUPATION");

        if($model->rela_schregno){$Row_relative["RELA_SCHREGNO"] = $model->rela_schregno;}

/*        //兄弟姉妹学籍番号
        $arg["data"]["RELA_SCHREGNO"] = View::popUpStuCode( $objForm,                       //objForm
                                                            "RELA_SCHREGNO",                //エレメント名
                                                            $Row_relative["RELA_SCHREGNO"], //値
                                                            "兄弟姉妹検索"                  //ボタン表示名
                                                           ); */
        //兄弟姉妹検索ボタン
        //$arg["data"]["RELA_SCHREGNO"] = View::popUpStuCode( $objForm,"RELA_SCHREGNO","2",$model->schregno,$Row_relative["RELA_SCHREGNO"]);

        //テキストエリア
        $objForm->ae( array("type"       => "text",
                            "name"        => RELA_SCHREGNO,
                            "size"        => 10,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row_relative["RELA_SCHREGNO"]));
        $arg["data"]["RELA_SCHREGNO"] = $objForm->ge("RELA_SCHREGNO");
        //検索ボタンを作成する
        $objForm->ae( array("type"       => "button",
                           "name"           => "btn_stucd2",
                           "value"          => "兄弟姉妹検索",
                           "extrahtml"      => "style=\"width:140px\"onclick=\"loadwindow('./knjh020_2SubForm1.php?cmd=search&MODE=set&CD=$model->schregno&SCHREGNO='+document.forms[0]['RELA_SCHREGNO'].value+'&STUCD_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 600, 300)\"") );
        
        $arg["data"]["BTN_STUCD2"] = $objForm->ge("btn_stucd2");

        //同居区分
        $query  = knjh020_2Query::getNameMst_data("H200");
        $result = $db->query($query);

        //同居区分コンボボックスの中身を作成------------------------------
        $opt_relat   = array();
        $opt_relat[] = array("label" => "","value" => "00");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_relat[] = array( "label" => $row["NAMECD2"]."：".htmlspecialchars($row["NAME1"]),
                                  "value" => $row["NAMECD2"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "REGIDENTIALCD",
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_relative["REGIDENTIALCD"],
                            "options"     => $opt_relat));

        $arg["data"]["REGIDENTIALCD"] = $objForm->ge("REGIDENTIALCD");

        //備考
        $objForm->ae( array("type"        => "text",
                            "name"        => "REMARK",
                            "size"        => 30,
                            "maxlength"   => 30,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_relative["REMARK"] ));

        $arg["data"]["REMARK"] = $objForm->ge("REMARK");

        $result->free();
        Query::dbCheckIn($db);

        //更 新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => " onclick=\"return btn_submit('update');\""));

        $arg["btn_update"] = $objForm->ge("btn_update");

        //追 加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => " onclick=\"return btn_submit('add');\""));

        $arg["btn_add"] = $objForm->ge("btn_add");

        //取 消ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_can",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\""));

        $arg["btn_can"] = $objForm->ge("btn_can");

        //削 除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => " onclick=\"return btn_submit('delete');\""));

        $arg["btn_del"] = $objForm->ge("btn_del");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\""));

        $arg["btn_end"] = $objForm->ge("btn_end");

        //保護者情報へボタンを作成する
        $link1 = REQUESTROOT."/H/KNJH020/knjh020index.php?cmd=edit";
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_jump",
                            "value"     => "保護者情報へ",
                            "extrahtml" => "onclick=\" Page_jumper('".$link1."','".$model->schregno."');\""));

        $arg["btn_jump"] = $objForm->ge("btn_jump");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "RELANO",
                            "value"     => $model->relano));

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row_relative["UPDATED"]));

        $arg["finish"]  = $objForm->get_finish();

        //更新後リスト画面をリロードする
        if ($model->isMessage()){
            $arg["reload"] = "window.open('knjh020_2index.php?cmd=main','top_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh020_2Form2.html", $arg);
    }
}
?>
