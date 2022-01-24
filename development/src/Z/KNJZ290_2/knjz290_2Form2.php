<?php

require_once('for_php7.php');

class knjz290_2Form2
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz290_2index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->staffcd))
        {   
            if ($model->cmd === 'change') {
                $Row =& $model->field;
                $Row["JOBCD"] = $model->field["JOBNAME"];
                $Row["CHARGECLASSCD"] = $model->field["CHARGECLASS"];
                $Row["SECTIONCD"] = $model->field["SECTIONABBV"];
                $Row["DUTYSHARECD"] = $model->field["SHARENAME"];
                $Row["DUTYSHARECD2"] = $model->field["SHARENAME2"];
                $Row["STAFFSEX"] = $model->field["NAME2"];
            } else {
                $Row = knjz290_2Query::getRow($model, $model->staffcd);
            }
        } else {
            $Row =& $model->field;
            $Row["JOBCD"] = $model->field["JOBNAME"];
            $Row["CHARGECLASSCD"] = $model->field["CHARGECLASS"];
            $Row["SECTIONCD"] = $model->field["SECTIONABBV"];
            $Row["DUTYSHARECD"] = $model->field["SHARENAME"];
            $Row["DUTYSHARECD2"] = $model->field["SHARENAME2"];
            $Row["STAFFSEX"] = $model->field["NAME2"];
        }
        
        //画面表示セット
        $arg["data"]["SET_COLSPAN"] = "3";
        $arg["data"]["SET_BGCOLOR"] = 'bgcolor="#00BFFF"';
        $arg["data"]["SET_BGCOLOR2"]   = 'bgcolor="#99eaff"';

        //担当保健室表示セット
        if ($model->Properties["useNurseoffRestrict"] == '1') {
            $arg["data"]["SET_COLWITH"] = 'width="30%"';
            $arg["useNurse"] = "1";
        } else {
            $arg["data"]["SET_COLWITH"] = 'colspan="3"';
        }

        //職名コンボボックス
        $db         = Query::dbCheckOut();
        $optnull    = array("label" => "","value" => "");   //初期値：空白項目
        $result     = $db->query(knjz290_2Query::getJOBNAME());   
        $opt        = array();
        $opt[]      = $optnull;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["JOBCD"]."  ".$row["JOBNAME"], 
                           "value" => $row["JOBCD"]);
        }
        $result->free();

        //所属コンボボックス
        $result = $db->query(knjz290_2Query::getSECTION());
        $opt2   = array();
        $opt2[] = $optnull;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt2[] = array("label" => $row["SECTIONCD"]."  ".$row["SECTIONABBV"], 
                            "value" => $row["SECTIONCD"]);
        }
        $result->free();

        //校務分掌部コンボボックス
        $result = $db->query(knjz290_2Query::getDUTYSHARE());
        $opt3   = array();
        $opt3[] = $optnull;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt3[] = array("label" => $row["DUTYSHARECD"]."  ".$row["SHARENAME"], 
                            "value" => $row["DUTYSHARECD"]);
        }
        $result->free();

        //授業受持ちコンボボックス
        $opt4   = array();
        $opt4[] = $optnull;
        $opt4[] = array("label" => "0 無し", "value" => "0");
        $opt4[] = array("label" => "1 有り", "value" => "1");
        
        //職員性別コンボボックス
        $result = $db->query(knjz290_2Query::getSTAFFSEX());
        $opt5   = array();
        $opt5[] = $optnull;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt5[] = array("label" => $row["NAMECD2"]."  ".$row["NAME2"],
                            "value" => $row["NAMECD2"]);
        }
        $result->free();
        
        //職員コード
        $setsize = "";
        //STAFFCDフィールドサイズ変更対応
        if ($model->Properties["useStaffcdFieldSize"] === '10') {
            $setsize = 10;
        } else {
            $setsize = 8;
        }
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["STAFFCD"] = knjCreateTextBox($objForm, $Row["STAFFCD"], "STAFFCD", $setsize, $setsize, $extra);

        //職員氏名
        $objForm->ae( array("type"        => "text",
                            "name"        => "STAFFNAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "",
                            "value"       => $Row["STAFFNAME"] ));

        $arg["data"]["STAFFNAME"] = $objForm->ge("STAFFNAME");

        //職員氏名表示用
        $objForm->ae( array("type"        => "text",
                            "name"        => "STAFFNAME_SHOW",
                            "size"        => 10,
                            "maxlength"   => 15,
                            "extrahtml"   => "",
                            "value"       => $Row["STAFFNAME_SHOW"] ));

        $arg["data"]["STAFFNAME_SHOW"] = $objForm->ge("STAFFNAME_SHOW");
    
        //職員氏名かな
        $objForm->ae( array("type"        => "text",
                            "name"        => "STAFFNAME_KANA",
                            "size"        => 80,
                            "maxlength"   => 120,
                            "extrahtml"   => "",
                            "value"       => $Row["STAFFNAME_KANA"] ));

        $arg["data"]["STAFFNAME_KANA"] = $objForm->ge("STAFFNAME_KANA");

        //職員氏名英字
        $objForm->ae( array("type"        => "text",
                            "name"        => "STAFFNAME_ENG",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "onblur=\"this.value=toAlphanumeric(this.value)\"",
                            "value"       => $Row["STAFFNAME_ENG"] ));

        $arg["data"]["STAFFNAME_ENG"] = $objForm->ge("STAFFNAME_ENG");

        //戸籍氏名
        $extra = "";
        $arg["data"]["STAFFNAME_REAL"] = knjCreateTextBox($objForm, $Row["STAFFNAME_REAL"], "STAFFNAME_REAL", 80, 120, $extra);

        //奇跡氏名かな
        $extra = "";
        $arg["data"]["STAFFNAME_KANA_REAL"] = knjCreateTextBox($objForm, $Row["STAFFNAME_KANA_REAL"], "STAFFNAME_KANA_REAL", 120, 120, $extra);

        //職名コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "JOBNAME",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $Row["JOBCD"],
                            "options"     => $opt ));

        $arg["data"]["JOBNAME"] = $objForm->ge("JOBNAME");

        //所属コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "SECTIONABBV",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $Row["SECTIONCD"],
                            "options"     => $opt2 ));

        $arg["data"]["SECTIONABBV"] = $objForm->ge("SECTIONABBV");

        //所属コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "SHARENAME",
                            "size"        => "1",
                            "extrahtml"   => " onchange=\"return chkSelSameValue();\" ",
                            "value"       => $Row["DUTYSHARECD"],
                            "options"     => $opt3 ));

        $arg["data"]["SHARENAME"] = $objForm->ge("SHARENAME");

        $objForm->ae( array("type"        => "select",
                            "name"        => "SHARENAME2",
                            "size"        => "1",
                            "extrahtml"   => " onchange=\"return chkSelSameValue();\" ",
                            "value"       => $Row["DUTYSHARECD2"],
                            "options"     => $opt3 ));

        $arg["data"]["SHARENAME2"] = $objForm->ge("SHARENAME2");

        //授業受持ちコンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "CHARGECLASS",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $Row["CHARGECLASSCD"],
                            "options"     => $opt4 ));

        $arg["data"]["CHARGECLASS"] = $objForm->ge("CHARGECLASS");

        //職員性別コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "NAME2",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $Row["STAFFSEX"],
                            "options"     => $opt5 ));

        $arg["data"]["NAME2"] = $objForm->ge("NAME2");

        //肩書き1
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $result = $db->query(knjz290_2Query::getPositionCd($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        $extra = "onchange=\"return btn_submit('change');\"";
        $arg["data"]["POSITIONCD1"] = knjCreateCombo($objForm, "POSITIONCD1", $Row["POSITIONCD1"], $opt, $extra, 1);
        
        //肩書き2
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $result = $db->query(knjz290_2Query::getPositionCd($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        $extra = "onchange=\"return btn_submit('change');\"";
        $arg["data"]["POSITIONCD2"] = knjCreateCombo($objForm, "POSITIONCD2", $Row["POSITIONCD2"], $opt, $extra, 1);
        
        //肩書き3
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $result = $db->query(knjz290_2Query::getPositionCd($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        $extra = "onchange=\"return btn_submit('change');\"";
        $arg["data"]["POSITIONCD3"] = knjCreateCombo($objForm, "POSITIONCD3", $Row["POSITIONCD3"], $opt, $extra, 1);

        //担当保健室コンボ
        $query = knjz290_2Query::getVNameMst("Z043", $model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "CHARGENURSEOFF", $Row["CHARGENURSEOFF"], $extra, 1, "BLANK");

        //学年主任　学年コンボ
        if ($Row["POSITIONCD1"] === '0200' || $Row["POSITIONCD1"] === '1050') {
            $arg["MANAGER1_SET"] = "1";
            $opt = array();
            $opt[] = array('label' => "", 'value' => "");
            if ($Row["POSITIONCD1"] === '0200') {
                $result = $db->query(knjz290_2Query::getGrade($model));
            } else {
                $result = $db->query(knjz290_2Query::getClass($model));
            }
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
            $result->free();
            $extra = "";
            $arg["data"]["POSITIONCD1_MANAGER"] = knjCreateCombo($objForm, "POSITIONCD1_MANAGER", $Row["POSITIONCD1_MANAGER"], $opt, $extra, 1);
        }
        
        //教科主任　教科コンボ
        if ($Row["POSITIONCD2"] === '0200' || $Row["POSITIONCD2"] === '1050') {
            $arg["MANAGER2_SET"] = "1";
            $opt = array();
            $opt[] = array('label' => "", 'value' => "");
            if ($Row["POSITIONCD2"] === '0200') {
                $result = $db->query(knjz290_2Query::getGrade($model));
            } else {
                $result = $db->query(knjz290_2Query::getClass($model));
            }
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
            $result->free();
            $extra = "";
            $arg["data"]["POSITIONCD2_MANAGER"] = knjCreateCombo($objForm, "POSITIONCD2_MANAGER", $Row["POSITIONCD2_MANAGER"], $opt, $extra, 1);
        }

        //教科主任　教科コンボ
        if ($Row["POSITIONCD3"] === '0200' || $Row["POSITIONCD3"] === '1050') {
            $arg["MANAGER3_SET"] = "1";
            $opt = array();
            $opt[] = array('label' => "", 'value' => "");
            if ($Row["POSITIONCD3"] === '0200') {
                $result = $db->query(knjz290_2Query::getGrade($model));
            } else {
                $result = $db->query(knjz290_2Query::getClass($model));
            }
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
            $result->free();
            $extra = "";
            $arg["data"]["POSITIONCD3_MANAGER"] = knjCreateCombo($objForm, "POSITIONCD3_MANAGER", $Row["POSITIONCD3_MANAGER"], $opt, $extra, 1);
        }

        //職員生年月日
        $Row["STAFFBIRTHDAY"] = str_replace("-","/",$Row["STAFFBIRTHDAY"]);
        $arg["data"]["STAFFBIRTHDAY"] = View::popUpCalendar($objForm, "STAFFBIRTHDAY" ,$Row["STAFFBIRTHDAY"]);

        //出身学校郵便番号
        $arg["data"]["STAFFZIPCD"] = View::popUpZipCode($objForm, "STAFFZIPCD", $Row["STAFFZIPCD"],"STAFFADDR1");

        //職員住所１
        $objForm->ae( array("type"        => "text",
                            "name"        => "STAFFADDR1",
                            "size"        => 50,
                            "maxlength"   => 90,
                            "extrahtml"   => "",
                            "value"       => $Row["STAFFADDR1"] ));

        $arg["data"]["STAFFADDR1"] = $objForm->ge("STAFFADDR1");
    
        //職員住所2
        $objForm->ae( array("type"        => "text",
                            "name"        => "STAFFADDR2",
                            "size"        => 50,
                            "maxlength"   => 90,
                            "extrahtml"   => "",
                            "value"       => $Row["STAFFADDR2"] ));

        $arg["data"]["STAFFADDR2"] = $objForm->ge("STAFFADDR2");

        //職員電話番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "STAFFTELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "",
                            "value"       => $Row["STAFFTELNO"] ));

        $arg["data"]["STAFFTELNO"] = $objForm->ge("STAFFTELNO");

        //職員FAX番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "STAFFFAXNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "",
                            "value"       => $Row["STAFFFAXNO"] ));

        $arg["data"]["STAFFFAXNO"] = $objForm->ge("STAFFFAXNO");

        //職員E-Mailアドレス
        $objForm->ae( array("type"        => "text",
                            "name"        => "STAFFE_MAIL",
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "onblur=\"this.value=toAlphanumeric(this.value)\"",
                            "value"       => $Row["STAFFE_MAIL"] ));

        $arg["data"]["STAFFE_MAIL"] = $objForm->ge("STAFFE_MAIL");

        //disabled
        $disable = ($model->staffcd) ? "" : " disabled";

        //資格教科登録ボタン
        $extra = "onclick=\"return btn_submit('subform1');\"";
        $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "資格教科登録", $extra.$disable);

        //資格科教科表示
        $query = knjz290_2Query::getStaffClass($model);
        $arg["data"]["STF_SUBCLASS"] = implode(',',$db->getCol($query));

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ290/knjz290index.php";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );

        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //校長履歴登録
        $extra = "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $subdata = "onClick=\"wopen('".REQUESTROOT."/Z/KNJZ290S1/knjz290s1index.php?&cmd=&SEND_PRGID=KNJZ290_2&SEND_AUTH=".AUTHORITY.$extra."\"";
        $arg["button"]["btn_prihist"] = knjCreateBtn($objForm, "btn_prihist", "校長履歴登録", $subdata);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );
        //hiddenを作成する(js側で変数として利用)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_DUTYSHARECD",
                            "value"     => $Row["DUTYSHARECD"]
                            ) );
        //hiddenを作成する(js側で変数として利用)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HID_DUTYSHARECD_2",
                            "value"     => $Row["DUTYSHARECD2"]
                            ) );


    
        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz290_2index.php?cmd=list&shori=add';";
        }

        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz290_2Form2.html", $arg);
    }
} 
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
