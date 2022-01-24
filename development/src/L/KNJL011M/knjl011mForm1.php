<?php

require_once('for_php7.php');

class knjl011mForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl011mindex.php", "", "main");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            //データを取得
            $Row = knjl011mQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl011mQuery::get_edit_data($model);
                }
                $model->examno = $Row["EXAMNO"];
            }
            $disabled = "";
            if (!is_array($Row)) {
                $disabled = "disabled";
                if ($model->cmd == 'reference') {
                    $model->setWarning("MSG303");
                }
            }
        } else {
            $Row =& $model->field;
        }
        if ($model->cmd == 'fsarea') {
            $Row =& $model->field;
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        $db = Query::dbCheckOut();

        //------------------------------志願者情報-------------------------------------

        //受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\" style='ime-mode:inactive;' ",
                            "value"       => preg_replace('/^0*/', '', $model->examno) ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //氏名(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "onchange=\"change_flg()\" style='ime-mode:active;' ",
                            "value"       => $Row["NAME"] ));
        $arg["data"]["NAME"] = $objForm->ge("NAME");

        //フリガナ(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME_KANA",
                            "size"        => 40,
                            "maxlength"   => 120,
                            "extrahtml"   => "onchange=\"change_flg()\" style='ime-mode:active;' ",
                            "value"       => $Row["NAME_KANA"] ));
        $arg["data"]["NAME_KANA"] = $objForm->ge("NAME_KANA");

        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl011mQuery::get_calendarno($model->year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($calno == "") {
                $calno = $row["NAMECD2"];
                $spare = $row["NAMESPARE1"];
                $spare2 = $row["NAMESPARE2"];
                $spare3 = $row["NAMESPARE3"];
            } else {
                $calno.= "," . $row["NAMECD2"];
                $spare.= "," . $row["NAMESPARE1"];
                $spare2.= "," . $row["NAMESPARE2"];
                $spare3.= "," . $row["NAMESPARE3"];
            }
            $arg["data2"][] = array("eracd" => $row["NAMECD2"], "wname" => $row["NAME1"]);
        }
        //生年月日元号
        $objForm->ae( array("type"        => "text",
                            "name"        => "ERACD",
                            "size"        => 1,
                            "maxlength"   => 1,
                            "extrahtml"   => "STYLE=\"text-align: center; ime-mode:inactive;\" onblur=\" toDatecheck(0, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onchange=\"change_flg()\"",
                            "value"       => strlen($Row["ERACD"]) ? $Row["ERACD"] : "4" ));
        $arg["data"]["ERACD"] = $objForm->ge("ERACD");
        //和暦名
        if (isset($Row["NAME1"])) {
            $name1 = $Row["NAME1"];
        } else if(isset($Row["WNAME"])) {
            $name1 = str_replace("&nbsp;", "", $Row["WNAME"]);
        } else {
            $name1 = "平成";
        }
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "WNAME",
                            "value"     => $name1) );
        $arg["data"]["WNAME"] = $name1;
        //生年月日年
        $objForm->ae( array("type"        => "text",
                            "name"        => "BIRTH_Y",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center; ime-mode:inactive;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["BIRTH_Y"] ));
        $arg["data"]["BIRTH_Y"] = $objForm->ge("BIRTH_Y");
        //生年月日月
        $objForm->ae( array("type"        => "text",
                            "name"        => "BIRTH_M",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center; ime-mode:inactive;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["BIRTH_M"] ));
        $arg["data"]["BIRTH_M"] = $objForm->ge("BIRTH_M");
        //生年月日日
        $objForm->ae( array("type"        => "text",
                            "name"        => "BIRTH_D",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center; ime-mode:inactive;\" onblur=\" toDatecheck(3, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["BIRTH_D"] ));
        $arg["data"]["BIRTH_D"] = $objForm->ge("BIRTH_D");


        //
        global $sess;


        //------------------------------出身学校情報-------------------------------------
        //出身学校コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "FS_CD",
                            "size"        => 7,
                            "maxlength"   => 7,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" onchange=\"change_flg()\" style='ime-mode:inactive;' ",
                            "value"       => $Row["FS_CD"] ));

        $arg["data"]["FS_CD"] = $objForm->ge("FS_CD");

        //出身学校名
        $objForm->ae( array("type"        => "text",
                            "name"        => "FS_NAME",
                            "size"        => 30,
                            "maxlength"   => 30,
                            "extrahtml"   => "onchange=\"change_flg()\" style='ime-mode:active;' ",
                            "value"       => $Row["FS_NAME"] ));

        $arg["data"]["FS_NAME"] = $objForm->ge("FS_NAME");

        //国公立区分コンボ
        $extra = "style=\"width:80px\" ";
        $optNatpubpri = array();
        $optNatpubpri[] = array("label" => "", "value" => "");
        $result = $db->query(knjl011mQuery::getNatpubpriMst());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $optNatpubpri[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            $extra = "";
        }
        $result->free();
        $objForm->ae( array("type"        => "select",
                            "name"        => "FS_NATPUBPRIDIV",
                            "size"        => "1",
                            "extrahtml"   => $extra ."onChange=\"change_flg(); return btn_submit('fsarea');\"",
                            "value"       => $Row["FS_NATPUBPRIDIV"],
                            "options"     => $optNatpubpri ) );
        $arg["data"]["FS_NATPUBPRIDIV"] = $objForm->ge("FS_NATPUBPRIDIV");

        //所在地区分コンボ
        $extra = "style=\"width:80px\" ";
        $optAreaDiv = array();
        $optAreaDiv[] = array("label" => "", "value" => "");
        $result = $db->query(knjl011mQuery::getAreaDivMst($Row["FS_NATPUBPRIDIV"]));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $optAreaDiv[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            $extra = "";
        }
        $result->free();
        $objForm->ae( array("type"        => "select",
                            "name"        => "FS_AREA_DIV",
                            "size"        => "1",
                            "extrahtml"   => $extra ."onChange=\"change_flg(); return btn_submit('fsarea');\"",
                            "value"       => $Row["FS_AREA_DIV"],
                            "options"     => $optAreaDiv ) );
        $arg["data"]["FS_AREA_DIV"] = $objForm->ge("FS_AREA_DIV");

        if ($Row["FS_AREA_DIV"] == '99' || $Row["FS_AREA_CD"] == '99') {
            $extra = "style='ime-mode:active;'";
        } else {
            $extra = "disabled";
            $Row["REMARK1"] = '';
        }
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 40, 20, $extra);

        //所在地コンボ
        $extra = "style=\"width:80px\" ";
        $optArea = array();
        $optArea[] = array("label" => "", "value" => "");
        $result = $db->query(knjl011mQuery::getAreaMst($Row["FS_NATPUBPRIDIV"], $Row["FS_AREA_DIV"]));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $optArea[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            $extra = "";
        }
        $result->free();
        $objForm->ae( array("type"        => "select",
                            "name"        => "FS_AREA_CD",
                            "size"        => "1",
                            "extrahtml"   => $extra ."onChange=\"change_flg(); return btn_submit('fsarea');\"",
                            "value"       => $Row["FS_AREA_CD"],
                            "options"     => $optArea ) );
        $arg["data"]["FS_AREA_CD"] = $objForm->ge("FS_AREA_CD");


        //------------------------------保護者情報-------------------------------------


        //氏名(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GNAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "onchange=\"change_flg()\" style='ime-mode:active;' ",
                            "value"       => $Row["GNAME"] ));
        $arg["data"]["GNAME"] = $objForm->ge("GNAME");

        //フリガナ(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GKANA",
                            "size"        => 40,
                            "maxlength"   => 120,
                            "extrahtml"   => "onchange=\"change_flg()\" style='ime-mode:active;' ",
                            "value"       => $Row["GKANA"] ));
        $arg["data"]["GKANA"] = $objForm->ge("GKANA");



        Query::dbCheckIn($db);



        //-------------------------------- ボタン作成 ------------------------------------



        //新規ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_addnew",
                            "value"     => "新 規",
                            "extrahtml" => "onclick=\"return btn_submit('addnew');\""  ) );
        $arg["button"]["btn_addnew"] = $objForm->ge("btn_addnew");
        //検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reference",
                            "value"     => "検 索",
                            "extrahtml" => "onclick=\"return btn_submit('reference');\"" ) );
        $arg["button"]["btn_reference"] = $objForm->ge("btn_reference");
        //カナ検索ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_kana_reference",
                            "value"       => "カナ検索",
                            "extrahtml"   => "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011M/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
        $arg["button"]["btn_kana_reference"] = $objForm->ge("btn_kana_reference");
        //前の志願者検索ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => " << ",
                            "extrahtml"   => "style=\"width:32px\" onClick=\"btn_submit('back1');\"" ) );
        //次の志願者検索ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => " >> ",
                            "extrahtml"   => "style=\"width:32px\" onClick=\"btn_submit('next1');\"" ) );
        $arg["button"]["btn_back_next"] = $objForm->ge("btn_back").$objForm->ge("btn_next");
        //画面クリアボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_clear",
                            "value"     => "画面クリア",
                            "extrahtml" => "style=\"width:80px\" onclick=\"return btn_submit('disp_clear');\"" ) );
        $arg["button"]["btn_clear"] = $objForm->ge("btn_clear");
        //追加ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_add",
                            "value"     => "追 加",
                            "extrahtml" => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");
        //更新ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_udpate",
                            "value"     => "更 新",
                            "extrahtml" => "$disabled onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");
        //更新ボタン(更新後前の志願者)
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の志願者",
                            "extrahtml" => "$disabled style=\"width:150px\" onclick=\"return btn_submit('back');\"" ) );
        //更新ボタン(更新後次の志願者)
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の志願者",
                            "extrahtml" =>  "$disabled style=\"width:150px\" onclick=\"return btn_submit('next');\"" ) );
        $arg["button"]["btn_up_next"] = $objForm->ge("btn_up_pre") . $objForm->ge("btn_up_next");
        //削除ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_del",
                            "value"     => "削 除",
                            "extrahtml" => "$disabled onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");
        //取消ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('reset');\""  ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");
        //終了ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ctrl_year",
                            "value"     => CTRL_YEAR) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ctrl_semester",
                            "value"     => CTRL_SEMESTER) );
        //削除権限
        $delFlg = (AUTHORITY == DEF_UPDATABLE) ? "1" : "";
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DELFLG",
                            "value"     => $delFlg) );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl011mForm1.html", $arg);
    }
}
?>