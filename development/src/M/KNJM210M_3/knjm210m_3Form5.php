<?php

require_once('for_php7.php');

class knjm210m_3Form5
{
    function main(&$model)
    {    
        $objForm = new form;
        $db = Query::dbCheckOut();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjm210m_3index.php", "", "edit");
        $arg["data"] = array();
        
        //学籍基礎マスタより学籍番号と名前を取得
//        $model->schregno = "20031935";
        $Row         = $db->getRow(knjm210m_3Query::getSchregno_name($model->schregno),DB_FETCHMODE_ASSOC);
        $arg["NAME"] = "　　　学籍番号：".$Row["SCHREGNO"]."　　　氏名：".$Row["NAME"];

        /* ********************************************その他情報********************************************** */
        $Row_others  = knjm210m_3Query::getRow_others($model->schregno);

        //その他氏名(漢字)
        $objForm->ae( array("type"        => "text",
                            "name"        => "SEND_NAME",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_others["SEND_NAME"] ));

        $arg["data"]["SEND_NAME"] = $objForm->ge("SEND_NAME");

        //その他氏名(カナ)
        $objForm->ae( array("type"        => "text",
                            "name"        => "SEND_KANA",
                            "size"        => 80,
                            "maxlength"   => 80,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_others["SEND_KANA"] ));

        $arg["data"]["SEND_KANA"] = $objForm->ge("SEND_KANA");

        //その他性別
        $db     = Query::dbCheckOut();
        $query  = knjm210m_3Query::getNameMst_data("Z002");
        $result = $db->query($query);

        //性別コンボボックスの中身を作成------------------------------
        $opt_sex  = array();
        $info_sex = array();
        $opt_sex[]  = array("label" => "","value" => "0");
        $info_sex[] = array("label" => "","value" => "0");

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd2 = substr($row["NAMECD2"],0,1);
            $opt_sex[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME2"]),
                                "value" => $row["NAMECD2"]);
            $info_sex[$row["NAMECD2"]] = $row["NAME2"];
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "SEND_SEX",
                            "size"        => 1,
                            "maxlength"   => 10,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_others["SEND_SEX"],
                            "options"     => $opt_sex));
        $arg["data"]["SEND_SEX"] = $objForm->ge("SEND_SEX");

        //その他生年月日カレンダーコントロール
        $arg["data"]["SEND_BIRTHDAY"] = View::popUpCalendar($objForm,
                                                                 "SEND_BIRTHDAY",
                                                                 str_replace("-","/",$Row_others["SEND_BIRTHDAY"]),
                                                                 ""
                                                                );

        //その他続柄
        $query  = knjm210m_3Query::getNameMst_data("H201");
        $result = $db->query($query);

        //続柄コンボボックスの中身を作成------------------------------
        $opt_relat  = array();
        $info_relat = array();
        $opt_relat[]  = array("label" => "","value" => "00");
        $info_relat[] = array("label" => "","value" => "00");

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $namecd2 = substr($row["NAMECD2"],0,2);
            $opt_relat[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME1"]),
                                  "value" => $row["NAMECD2"]);
            $info_relat[$row["NAMECD2"]] = $row["NAME1"];
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "SEND_RELATIONSHIP",
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_others["SEND_RELATIONSHIP"],
                            "options"     => $opt_relat));
        $arg["data"]["SEND_RELATIONSHIP"] = $objForm->ge("SEND_RELATIONSHIP");

        //その他情報郵便番号
        $arg["data"]["J_SEND_ZIPCD"] = View::popUpZipCode($objForm, "J_SEND_ZIPCD", $Row_others["SEND_ZIPCD"],"SEND_ADDR1");

        //地区コード
        $result = $db->query(knjm210m_3Query::getNameMstA020());
        $opt = array();
        $opt[] = array("label" => "","value" => "00");
        while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                           "value" => $row3["NAMECD2"]);
        }
        $arg["data"]["SEND_AREACD"] = knjCreateCombo($objForm, "SEND_AREACD", $Row_others["SEND_AREACD"], $opt, "", 1);

        //その他住所１
        $objForm->ae( array("type"        => "text",
                            "name"        => "SEND_ADDR1",
                            "size"        => 50,
                            "maxlength"   => 90,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_others["SEND_ADDR1"] ));
        $arg["data"]["SEND_ADDR1"] = $objForm->ge("SEND_ADDR1");

        //その他住所２
        $objForm->ae( array("type"        => "text",
                            "name"        => "SEND_ADDR2",
                            "size"        => 50,
                            "maxlength"   => 90,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_others["SEND_ADDR2"] ));
        $arg["data"]["SEND_ADDR2"] = $objForm->ge("SEND_ADDR2");

        //その他方書きを住所1とする
        $extra = $Row_others["SEND_ADDR_FLG"] == "1" ? " checked " : "";
        $arg["data"]["SEND_ADDR_FLG"] = knjCreateCheckBox($objForm, "SEND_ADDR_FLG", "1", $extra);

        //その他電話番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "SEND_TELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "onblur=\"this.value=toTelNo(this.value)\"",
                            "value"       => $Row_others["SEND_TELNO"] ));
        $arg["data"]["SEND_TELNO"] = $objForm->ge("SEND_TELNO");

        //その他電話番号２
        $objForm->ae( array("type"        => "text",
                            "name"        => "SEND_TELNO2",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "onblur=\"this.value=toTelNo(this.value)\"",
                            "value"       => $Row_others["SEND_TELNO2"] ));
        $arg["data"]["SEND_TELNO2"] = $objForm->ge("SEND_TELNO2");


        //職種コード
        $query  = knjm210m_3Query::getNameMst_data("H202");
        $result = $db->query($query);
        
        //職種コンボボックスの中身を作成------------------------------
        $opt_jobcd   = array();
        $opt_jobcd[] = array("label" => "","value" => "00");

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $namecd2 = substr($row["NAMECD2"],0,2);
            $opt_jobcd[] = array( "label" => $namecd2.":".htmlspecialchars($row["NAME1"]),
                                  "value" => $row["NAMECD2"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "SEND_JOBCD",
                            "size"        => 1,
                            "maxlength"   => 10,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_others["SEND_JOBCD"],
                            "options"     => $opt_jobcd ));
        $arg["data"]["SEND_JOBCD"] = $objForm->ge("SEND_JOBCD");

        //兼ねている公職
        $objForm->ae( array("type"        => "text",
                            "name"        => "PUBLIC_OFFICE2",
                            "size"        => 20,
                            "maxlength"   => 20,
                            "extrahtml"   => "onChange=\"\"",
                            "value"       => $Row_others["PUBLIC_OFFICE"] ));
        $arg["data"]["PUBLIC_OFFICE2"]    = $objForm->ge("PUBLIC_OFFICE2");


        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row_parents["UPDATED"]);
        knjCreateHidden($objForm, "STAFFNAME", $Row["STAFFNAME"]);
        knjCreateHidden($objForm, "STAFFKANA", $Row["STAFFKANA"]);

        knjCreateHidden($objForm, "CHECK_GUARD_ISSUEDATE",  $Row_parents["GUARD_ISSUEDATE"]);
        knjCreateHidden($objForm, "CHECK_GUARD_EXPIREDATE", $Row_parents["GUARD_EXPIREDATE"]);
        knjCreateHidden($objForm, "CHECK_RELATIONSHIP",     $Row_parents["RELATIONSHIP"]);
        knjCreateHidden($objForm, "CHECK_GUARD_NAME",       $Row_parents["GUARD_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARD_KANA",       $Row_parents["GUARD_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARD_REAL_NAME",  $Row_parents["GUARD_REAL_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARD_REAL_KANA",  $Row_parents["GUARD_REAL_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARD_SEX",        $Row_parents["GUARD_SEX"]);
        knjCreateHidden($objForm, "CHECK_GUARD_BIRTHDAY",   $Row_parents["GUARD_BIRTHDAY"]);

        knjCreateHidden($objForm, "REQUESTROOT", REQUESTROOT);
        knjCreateHidden($objForm, "E_APPDATE");
        knjCreateHidden($objForm, "RELATIONSHIP_FLG");
        knjCreateHidden($objForm, "GUARD_NAME_FLG");
        knjCreateHidden($objForm, "GUARD_KANA_FLG");
        knjCreateHidden($objForm, "GUARD_REAL_NAME_FLG");
        knjCreateHidden($objForm, "GUARD_REAL_KANA_FLG");
        knjCreateHidden($objForm, "GUARD_SEX_FLG");
        knjCreateHidden($objForm, "GUARD_BIRTHDAY_FLG");

        knjCreateHidden($objForm, "CHECK_GUARANTOR_ISSUEDATE",     $Row_parents["GUARANTOR_ISSUEDATE"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_EXPIREDATE",    $Row_parents["GUARANTOR_EXPIREDATE"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_RELATIONSHIP",  $Row_parents["GUARANTOR_RELATIONSHIP"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_NAME",          $Row_parents["GUARANTOR_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_KANA",          $Row_parents["GUARANTOR_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_REAL_NAME",     $Row_parents["GUARANTOR_REAL_NAME"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_REAL_KANA",     $Row_parents["GUARANTOR_REAL_KANA"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_SEX",           $Row_parents["GUARANTOR_SEX"]);

        knjCreateHidden($objForm, "E_APPDATE2");
        knjCreateHidden($objForm, "GUARANTOR_RELATIONSHIP_FLG");
        knjCreateHidden($objForm, "GUARANTOR_NAME_FLG");
        knjCreateHidden($objForm, "GUARANTOR_KANA_FLG");
        knjCreateHidden($objForm, "GUARANTOR_REAL_NAME_FLG");
        knjCreateHidden($objForm, "GUARANTOR_REAL_KANA_FLG");
        knjCreateHidden($objForm, "GUARANTOR_SEX_FLG");

        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "clear",
                            "value"     => "0"));

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm210m_3Form5.html", $arg);
    }
}
?>
