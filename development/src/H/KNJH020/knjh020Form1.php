<?php

require_once('for_php7.php');

class knjh020Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh020index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning)){
/*
            //データを取得
            if(AUTHORITY >= DEF_UPDATE_RESTRICT){
*/
                $Row_parents = knjh020Query::getRow_parents($model);
/*
            }
*/
        }else{
            $Row_parents =& $model->field;
        }
        //ヘッダー部作成
        $Row_himself = knjh020Query::getRow_himself($model);
        if($model->schregno){
            //学籍番号
            $arg["header"]["SCHREGNO"] = $model->schregno;
            //生徒氏名
            $arg["header"]["NAME_SHOW"] = $Row_himself["NAME_SHOW"];
            //生年月日
            $birth_day = array();
            $birth_day = explode("-",$Row_himself["BIRTHDAY"]);
            $arg["header"]["BIRTHDAY"] = $birth_day[0]."年".$birth_day[1]."月".$birth_day[2]."日";
        }else{
            //学籍番号
            $arg["header"]["SCHREGNO"] = "　　　　";
            //生徒氏名
            $arg["header"]["NAME_SHOW"] = "　　";
            //生年月日
            $arg["header"]["BIRTHDAY"] = "　年　月　日";
        }

        //兄弟姉妹検索ボタン
        //$arg["data"]["J_STUCD"] = View::popUpStuCode( $objForm,"J_STUCD","1",$model->schregno);

        global $sess;
        //テキストエリア
        $objForm->ae( array("type"       => "text",
                            "name"        => "J_STUCD",
                            "size"        => 10,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => ""));
 	$arg["data"]["J_STUCD"] = $objForm->ge("J_STUCD");
        //検索ボタンを作成する
        $objForm->ae( array("type"       => "button",
                           "name"           => "btn_stucd",
                           "value"          => "兄弟姉妹検索",
                           "extrahtml"      => "style=\"width:140px\"onclick=\"loadwindow('./knjh020SubForm1.php?cmd=search&CD=$model->schregno&SCHREGNO='+document.forms[0]['J_STUCD'].value+'&STUCD_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 600, 300)\"") );
 	$arg["data"]["BTN_STUCD"] = $objForm->ge("btn_stucd");
        //反映ボタンを作成する
        $objForm->ae( array("type"       => "button",
                            "name"       => "btn_apply",
                            "value"      => "反映",
                            "extrahtml"  => "onclick=\"hiddenWin('./knjh020SubForm1.php?cmd=apply&CD=$model->schregno&stucd='+document.forms[0]['J_STUCD'].value+'&frame='+getFrameName(self))\"") );
 	$arg["data"]["BTN_APPLY"] = $objForm->ge("btn_apply");

        //リンク先
//        $link1 = REQUESTROOT."/H/KNJH020_2/knjh020_2index.php?AUTH=$model->auth& ";	//NO001
//        $link1 = REQUESTROOT."/H/KNJH020_2/knjh020_2index.php?AUTH=".$model->auth."&SCHREGNO=".$model->schregno;	//NO001
		if ($model->auth){
	        $link1 = REQUESTROOT."/H/KNJH020_2/knjh020_2index.php?AUTH=".$model->auth."&SCHREGNO=".$model->schregno."&DAMMY=DAMMY";
		}else {
	        $link1 = REQUESTROOT."/H/KNJH020_2/knjh020_2index.php?AUTH=".AUTHORITY."&SCHREGNO=".$model->schregno."&DAMMY=DAMMY";
		}

        //親族情報へボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_add",
                            "value"     => "親族情報へ",
//                            "extrahtml" => "onclick=\" Page_jumper('".$link1."','".$model->schregno."','1');\""));
                            "extrahtml" => "onclick=\" Page_jumper('$link1');\""));

        $arg["button2"]["btn_add"] = $objForm->ge("btn_add");


        /** *************************保護者情報************************************ */
        //氏名(漢字)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARD_NAME",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["GUARD_NAME"] ));
        $arg["data"]["GUARD_NAME"] = $objForm->ge("GUARD_NAME");

        //氏名(カナ)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARD_KANA",
                            "size"        => 80,
                            "maxlength"   => 80,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["GUARD_KANA"] ));
        $arg["data"]["GUARD_KANA"] = $objForm->ge("GUARD_KANA");

        //性別
        $db     = Query::dbCheckOut();
        $query  = knjh020Query::getNameMst_data("Z002");
        $result = $db->query($query);

        //性別コンボボックスの中身を作成------------------------------
        $opt_sex  = array();
        $info_sex = array();
        $opt_sex[]  = array("label"=>"","value"=>"0");
        $info_sex[] = array("label"=>"","value"=>"0");

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $namecd2 = substr($row["NAMECD2"],0,1);
            $opt_sex[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME2"]),
                                "value" => $row["NAMECD2"]);
            $info_sex[$row["NAMECD2"]] = $row["NAME2"];
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "GUARD_SEX",
                            "size"        => 1,
                            "maxlength"   => 10,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["GUARD_SEX"],
                            "options"     => $opt_sex ));
        $arg["data"]["GUARD_SEX"] = $objForm->ge("GUARD_SEX");

        //生年月日カレンダーコントロール
        $arg["data"]["GUARD_BIRTHDAY"] = View::popUpCalendar($objForm,"GUARD_BIRTHDAY",str_replace("-","/",$Row_parents["GUARD_BIRTHDAY"]));

        //続柄
        $query  = knjh020Query::getNameMst_data("H201");
        $result = $db->query($query);

        //続柄コンボボックスの中身を作成------------------------------
        $opt_relat = array();
        $info_relat = array();
        $opt_relat[]  = array("label"=>"","value"=>"0");
        $info_relat[] = array("label"=>"","value"=>"0");

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $namecd2 = substr($row["NAMECD2"],0,2);
            $opt_relat[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME1"]),
                                  "value" => $row["NAMECD2"]);
            $info_relat[$row["NAMECD2"]] = $row["NAME1"];
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "RELATIONSHIP",
                            "size"        => 1,
                            "maxlength"   => 10,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["RELATIONSHIP"],
                            "options"     => $opt_relat ));
        $arg["data"]["RELATIONSHIP"] = $objForm->ge("RELATIONSHIP");

        //保護者情報郵便番号
        $arg["data"]["J_ZIPCD"] = View::popUpZipCode($objForm, "J_ZIPCD", $Row_parents["GUARD_ZIPCD"],"GUARD_ADDR1");

        //住所１
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARD_ADDR1",
                            "size"        => 50,
                            "maxlength"   => 90,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["GUARD_ADDR1"] ));
        $arg["data"]["GUARD_ADDR1"] = $objForm->ge("GUARD_ADDR1");

        //住所２
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARD_ADDR2",
                            "size"        => 50,
                            "maxlength"   => 90,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["GUARD_ADDR2"] ));
        $arg["data"]["GUARD_ADDR2"] = $objForm->ge("GUARD_ADDR2");

        //電話番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARD_TELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "onblur=\"this.value=toTelNo(this.value)\"",
                            "value"       => $Row_parents["GUARD_TELNO"] ));
        $arg["data"]["GUARD_TELNO"] = $objForm->ge("GUARD_TELNO");

        //ＦＡＸ番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARD_FAXNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "onblur=\"this.value=toTelNo(this.value)\"",
                            "value"       => $Row_parents["GUARD_FAXNO"] ));
        $arg["data"]["GUARD_FAXNO"] = $objForm->ge("GUARD_FAXNO");

        //Ｅ－ＭＡＩＬ
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARD_E_MAIL",
                            "size"        => 20,
                            "maxlength"   => 20,
                            "extrahtml"  => "onblur=\"this.value=checkEmail(this.value)\"",
                            "value"       => $Row_parents["GUARD_E_MAIL"] ));
        $arg["data"]["GUARD_E_MAIL"] = $objForm->ge("GUARD_E_MAIL");

        //勤務先名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARD_WORK_NAME",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "value"       => $Row_parents["GUARD_WORK_NAME"] ));
        $arg["data"]["GUARD_WORK_NAME"] = $objForm->ge("GUARD_WORK_NAME");

        //職種コード
        $query  = knjh020Query::getNameMst_data("H202");
        $result = $db->query($query);

        //職種コンボボックスの中身を作成------------------------------
        $opt_jobcd   = array();
        $opt_jobcd[] = array("label" => "","value" => "00");

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $namecd2 = substr($row["NAMECD2"],0,2);
            $opt_jobcd[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME1"]),
                                  "value" => $row["NAMECD2"]
                                );
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "GUARD_JOBCD",
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["GUARD_JOBCD"],
                            "options"      => $opt_jobcd ));
        $arg["data"]["GUARD_JOBCD"] = $objForm->ge("GUARD_JOBCD");

        //勤務先電話番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARD_WORK_TELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "onblur=\"this.value=toTelNo(this.value)\"",
                            "value"       => $Row_parents["GUARD_WORK_TELNO"] ));
        $arg["data"]["GUARD_WORK_TELNO"] = $objForm->ge("GUARD_WORK_TELNO");

        /* ********************************************保証人情報********************************************** */
        //保証人氏名(漢字)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARANTOR_NAME",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["GUARANTOR_NAME"] ));

        $arg["data"]["GUARANTOR_NAME"] = $objForm->ge("GUARANTOR_NAME");

        //保証人氏名(カナ)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARANTOR_KANA",
                            "size"        => 80,
                            "maxlength"   => 80,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["GUARANTOR_KANA"] ));

        $arg["data"]["GUARANTOR_KANA"] = $objForm->ge("GUARANTOR_KANA");

        //保証人性別
        $db     = Query::dbCheckOut();
        $query  = knjh020Query::getNameMst_data("Z002");
        $result = $db->query($query);

        //性別コンボボックスの中身を作成------------------------------
        $opt_sex  = array();
        $info_sex = array();
        $opt_sex[]  = array("label" => "","value" => "0");
        $info_sex[] = array("label" => "","value" => "0");

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $namecd2 = substr($row["NAMECD2"],0,1);
            $opt_sex[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME2"]),
                                "value" => $row["NAMECD2"]);
            $info_sex[$row["NAMECD2"]] = $row["NAME2"];
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "GUARANTOR_SEX",
                            "size"        => 1,
                            "maxlength"   => 10,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["GUARANTOR_SEX"],
                            "options"     => $opt_sex));
        $arg["data"]["GUARANTOR_SEX"] = $objForm->ge("GUARANTOR_SEX");

        //保証人生年月日カレンダーコントロール
        $arg["data"]["GUARANTOR_BIRTHDAY"] = View::popUpCalendar( $objForm,
                                                            "GUARANTOR_BIRTHDAY",
                                                            str_replace("-","/",$Row_parents["GUARANTOR_BIRTHDAY"]),
                                                            ""
                                                                );

        //保証人続柄
        $query  = knjh020Query::getNameMst_data("H201");
        $result = $db->query($query);

        //続柄コンボボックスの中身を作成------------------------------
        $opt_relat  = array();
        $info_relat = array();
        $opt_relat[]  = array("label" => "","value" => "00");
        $info_relat[] = array("label" => "","value" => "00");

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $namecd2 = substr($row["NAMECD2"],0,2);
            $opt_relat[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME1"]),
                                  "value" => $row["NAMECD2"]);
            $info_relat[$row["NAMECD2"]] = $row["NAME1"];
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "GUARANTOR_RELATIONSHIP",
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["GUARANTOR_RELATIONSHIP"],
                            "options"     => $opt_relat));
        $arg["data"]["GUARANTOR_RELATIONSHIP"] = $objForm->ge("GUARANTOR_RELATIONSHIP");

        //保証人情報郵便番号
        $arg["data"]["J_GUARANTOR_ZIPCD"] = View::popUpZipCode($objForm, "J_GUARANTOR_ZIPCD", $Row_parents["GUARANTOR_ZIPCD"],"GUARANTOR_ADDR1");

        //保証人住所１
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARANTOR_ADDR1",
                            "size"        => 50,
                            "maxlength"   => 90,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["GUARANTOR_ADDR1"] ));
        $arg["data"]["GUARANTOR_ADDR1"] = $objForm->ge("GUARANTOR_ADDR1");

        //保証人住所２
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARANTOR_ADDR2",
                            "size"        => 50,
                            "maxlength"   => 90,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["GUARANTOR_ADDR2"] ));
        $arg["data"]["GUARANTOR_ADDR2"] = $objForm->ge("GUARANTOR_ADDR2");

        //保証人電話番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "GUARANTOR_TELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "onblur=\"this.value=toTelNo(this.value)\"",
                            "value"       => $Row_parents["GUARANTOR_TELNO"] ));
        $arg["data"]["GUARANTOR_TELNO"] = $objForm->ge("GUARANTOR_TELNO");


        //職種コード
        $query  = knjh020Query::getNameMst_data("H202");
        $result = $db->query($query);

        //職種コンボボックスの中身を作成------------------------------
        $opt_jobcd   = array();
        $opt_jobcd[] = array("label" => "","value" => "00");

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $namecd2 = substr($row["NAMECD2"],0,2);
            $opt_jobcd[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME1"]),
                                  "value" => $row["NAMECD2"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "GUARANTOR_JOBCD",
                            "size"        => 1,
                            "maxlength"   => 10,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["GUARANTOR_JOBCD"],
                            "options"      => $opt_jobcd ));
        $arg["data"]["GUARANTOR_JOBCD"] = $objForm->ge("GUARANTOR_JOBCD");

        //兼ねている公職
        $objForm->ae( array("type"        => "text",
                            "name"        => "PUBLIC_OFFICE",
                            "size"        => 20,
                            "maxlength"   => 20,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_parents["PUBLIC_OFFICE"] ));
        $arg["data"]["PUBLIC_OFFICE"] = $objForm->ge("PUBLIC_OFFICE");

        //更新ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');

        //削除ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\""));
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //取消ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\""));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\""));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row_parents["UPDATED"]));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STAFFNAME",
                            "value"     => $Row["STAFFNAME"]));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STAFFKANA",
                            "value"     => $Row["STAFFKANA"]));


        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh020Form1.html", $arg);
    }
}
?>
