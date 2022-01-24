<?php

require_once('for_php7.php');

class knja110_2Form2
{
    function main(&$model)
    {
        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knja110_2index.php", "", "edit");
        $arg["reload"] = "";
        $db = Query::dbCheckOut();
        
        if(!$model->isWarning()){
            $row1 = $db->getRow(knja110_2Query::getRow_address($model->schregno,$model->issuedate),DB_FETCHMODE_ASSOC);
            //保護者情報の取得
            $row2 = $db->getRow(knja110_2Query::getGuardianAddr($model->schregno),DB_FETCHMODE_ASSOC);

            //緊急連絡先情報の取得(2005/10/20 ADD)
//            if($model->issuedate != ""){
                $row3 = $db->getRow(knja110_2Query::getEmergencyInfo($model->schregno),DB_FETCHMODE_ASSOC);
//            }else{
//                $row3 = array();
//            }
            $row  = array_merge((array)$row1,(array)$row2,(array)$row3);
        }else{
            $row =& $model->field;
        }

        //有効期間開始日付
        $ISSUEDATE = View::popUpCalendar($objForm, "ISSUEDATE", str_replace("-","/",$row["ISSUEDATE"]),"") ;
        //有効期間開始日付
        $EXPIREDATE = View::popUpCalendar($objForm, "EXPIREDATE", str_replace("-","/",$row["EXPIREDATE"]),"") ;

        //郵便番号
        $ZIPCD = View::popUpZipCode($objForm, "ZIPCD", $row["ZIPCD"],"ADDR1");

        //地区コード
        $result = $db->query(knja110_2Query::getV_name_mst());
        $opt = array();
        $opt[] = array("label" => "","value" => "0");

        while($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                           "value" => $row3["NAMECD2"]);
        }

        $objForm->ae( array("type"      => "select",
                            "name"      => "AREACD",
                            "extrahtml" => "",
                            "value"     => $row["AREACD"],
                            "options"   => $opt));
        //住所
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "ADDR1",
                            "size"      =>  "50",
                            "maxlength" =>  "90",
                            "extrahtml" =>  "",
                            "value"     =>  $row["ADDR1"]));
        //方書き(アパート名等)
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "ADDR2",
                            "size"      =>  "50",
                            "maxlength" =>  "90",
                            "extrahtml" =>  "",
                            "value"     =>  $row["ADDR2"]));
        //(英字)住所
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "ADDR1_ENG",
                            "size"      =>  "50",
                            "maxlength" =>  "70",
                            "extrahtml" =>  "",
                            "value"     =>  $row["ADDR1_ENG"]));
        //(英字)方書き(アパート名等)
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "ADDR2_ENG",
                            "size"      =>  "50",
                            "maxlength" =>  "70",
                            "extrahtml" =>  "",
                            "value"     =>  $row["ADDR2_ENG"]));
        //電話番号
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "TELNO",
                            "size"      =>  "16",
                            "maxlength" =>  "14",
                            "extrahtml" =>  "",
                            "value"     =>  $row["TELNO"]));
        //Fax番号
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "FAXNO",
                            "size"      =>  "16",
                            "maxlength" =>  "14",
                            "extrahtml" =>  "",
                            "value"     =>  $row["FAXNO"]));
        //E-mail
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "EMAIL",
                            "size"      =>  "25",
                            "maxlength" =>  "20",
                            "value"     =>  $row["EMAIL"]));
/*
        //急用連絡先
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "EMERGENCYCALL",
                            "size"      =>  "40",
                            "maxlength" =>  "60",
                            "extrahtml" =>  "",
                            "value"     =>  $row["EMERGENCYCALL"]));
        //急用電話番号
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "EMERGENCYTELNO",
                            "size"      =>  "16",
                            "maxlength" =>  "14",
                            "extrahtml" =>  "",
                            "value"     =>  $row["EMERGENCYTELNO"]));
*/

### 2005/10/20 緊急連絡先1,2追加

        //急用連絡先
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "EMERGENCYCALL",
                            "size"      =>  "40",
                            "maxlength" =>  "60",
                            "extrahtml" =>  "",
                            "value"     =>  $row["EMERGENCYCALL"]));

        //急用連絡氏名
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "EMERGENCYNAME",
                            "size"      =>  "40",
                            "maxlength" =>  "60",
                            "extrahtml" =>  "",
                            "value"     =>  $row["EMERGENCYNAME"]));

        //急用連絡続柄名
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "EMERGENCYRELA_NAME",
                            "size"      =>  "22",
                            "maxlength" =>  "30",
                            "extrahtml" =>  "",
                            "value"     =>  $row["EMERGENCYRELA_NAME"]));

        //急用電話番号
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "EMERGENCYTELNO",
                            "size"      =>  "16",
                            "maxlength" =>  "14",
                            "extrahtml" =>  "",
                            "value"     =>  $row["EMERGENCYTELNO"]));

        //急用連絡先２
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "EMERGENCYCALL2",
                            "size"      =>  "40",
                            "maxlength" =>  "60",
                            "extrahtml" =>  "",
                            "value"     =>  $row["EMERGENCYCALL2"]));

        //急用連絡氏名２
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "EMERGENCYNAME2",
                            "size"      =>  "40",
                            "maxlength" =>  "60",
                            "extrahtml" =>  "",
                            "value"     =>  $row["EMERGENCYNAME2"]));

        //急用連絡続柄名２
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "EMERGENCYRELA_NAME2",
                            "size"      =>  "22",
                            "maxlength" =>  "30",
                            "extrahtml" =>  "",
                            "value"     =>  $row["EMERGENCYRELA_NAME2"]));

        //急用電話番号２
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "EMERGENCYTELNO2",
                            "size"      =>  "16",
                            "maxlength" =>  "14",
                            "extrahtml" =>  "",
                            "value"     =>  $row["EMERGENCYTELNO2"]));


### 2005/10/20 おわり









        //コピーボタン 
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_copy1",
                            "value"     => "保護者よりコピー",
                            "extrahtml" => "style=\"width:130px\"onclick=\"copy(1);\""));

#        //本籍住所1
#        $objForm->ae( array("type"      =>  "text",
#                            "name"      =>  "PERMANENTADDR1",
#                            "size"      =>  "50",
#                            "maxlength" =>  "75",
#                            "extrahtml" =>  "",
#                            "value"     =>  $row["PERMANENTADDR1"]));
#        //本籍住所２
#        $objForm->ae( array("type"      =>  "text",
#                            "name"      =>  "PERMANENTADDR2",
#                            "size"      =>  "50",
#                            "maxlength" =>  "75",
#                            "extrahtml" =>  "",
#                            "value"     =>  $row["PERMANENTADDR2"]));

        $arg["data"] = array("COPY"           =>  $objForm->ge("btn_copy1"),
                             "ISSUEDATE"      =>  $ISSUEDATE,
                             "EXPIREDATE"     =>  $EXPIREDATE,
                             "ZIPCD"          =>  $ZIPCD,
                             "AREACD"         =>  $objForm->ge("AREACD"),
                             "ADDR1"          =>  $objForm->ge("ADDR1"),
                             "ADDR2"          =>  $objForm->ge("ADDR2"),
                             "ADDR1_ENG"      =>  $objForm->ge("ADDR1_ENG"),
                             "ADDR2_ENG"      =>  $objForm->ge("ADDR2_ENG"),
                             "TELNO"          =>  $objForm->ge("TELNO"),
                             "FAXNO"          =>  $objForm->ge("FAXNO"),
                             "EMAIL"          =>  $objForm->ge("EMAIL"),

                             "EMERGENCYCALL"        =>  $objForm->ge("EMERGENCYCALL"),
                             "EMERGENCYNAME"        =>  $objForm->ge("EMERGENCYNAME"),
                             "EMERGENCYRELA_NAME"   =>  $objForm->ge("EMERGENCYRELA_NAME"),
                             "EMERGENCYTELNO"       =>  $objForm->ge("EMERGENCYTELNO"),

                             "EMERGENCYCALL2"       =>  $objForm->ge("EMERGENCYCALL2"),
                             "EMERGENCYNAME2"       =>  $objForm->ge("EMERGENCYNAME2"),
                             "EMERGENCYRELA_NAME2"  =>  $objForm->ge("EMERGENCYRELA_NAME2"),
                             "EMERGENCYTELNO2"      =>  $objForm->ge("EMERGENCYTELNO2")
                             );

#                             "EMERGENCYCALL"  =>  $objForm->ge("EMERGENCYCALL"),
#                             "EMERGENCYTELNO" =>  $objForm->ge("EMERGENCYTELNO"));
#                             "PERMANENTZIPCD" =>  View::popUpZipCode($objForm, "PERMANENTZIPCD",$row["PERMANENTZIPCD"],"PERMANENTADDR1"),
#                             "PERMANENTADDR1" =>  $objForm->ge("PERMANENTADDR1"),
#                             "PERMANENTADDR2" =>  $objForm->ge("PERMANENTADDR2"));

        //追加ボタン 
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_add",
                            "value"     => "追加",
                            "extrahtml" => "onclick=\"return btn_submit('add');\""));

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //更新ボタン 
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_update",
                            "value"     =>  "更新",
                            "extrahtml" =>  "onclick=\"return btn_submit('update');\""));

        $arg["button"]["btn_update"]    = $objForm->ge("btn_update");

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');

        //削除ボタン 
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_del",
                            "value"     => "削除",
                            "extrahtml" => "onclick=\"return btn_submit('delete');\""));
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //取消ボタン 
        $objForm->ae( array("type"      =>  "reset",
                            "name"      =>  "btn_reset",
                            "value"     =>  "取消",
                            "extrahtml" =>  "onclick=\"return btn_submit('clear');\""));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン 
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終了",
                            "extrahtml" => "onclick=\"closeWin();\""));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        //hidden
        $objForm->ae( array("type"      =>  "hidden",
                            "name"      =>  "UPDATED",
                            "value"     =>  $row["UPDATED"]));
        //保護者氏名
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "GUARD_NAME",
                            "size"      =>  "40",
                            "maxlength" =>  "60",
                            "extrahtml" =>  "",
                            "value"     =>  $row["GUARD_NAME"]));
        //保護者かな
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "GUARD_KANA",
                            "size"      =>  "40",
                            "maxlength" =>  "120",
                            "extrahtml" =>  "",
                            "value"     =>  $row["GUARD_KANA"]));
        //住所
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "GUARD_ADDR1",
                            "size"      =>  "50",
                            "maxlength" =>  "90",
                            "extrahtml" =>  "",
                            "value"     =>  $row["GUARD_ADDR1"]));
        //方書き(アパート名等)
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "GUARD_ADDR2",
                            "size"      =>  "50",
                            "maxlength" =>  "90",
                            "extrahtml" =>  "",
                            "value"     =>  $row["GUARD_ADDR2"]));
        //電話番号
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "GUARD_TELNO",
                            "size"      =>  "16",
                            "maxlength" =>  "14",
                            "extrahtml" =>  "",
                            "value"     =>  $row["GUARD_TELNO"]));
        //Fax番号
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "GUARD_FAXNO",
                            "size"      =>  "16",
                            "maxlength" =>  "14",
                            "extrahtml" =>  "",
                            "value"     =>  $row["GUARD_FAXNO"]));
        //E-mail
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "GUARD_E_MAIL",
                            "size"      =>  "25",
                            "maxlength" =>  "20",
                            "value"     =>  $row["GUARD_E_MAIL"]));
        //勤務先
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "GUARD_WORK_NAME",
                            "size"      =>  "40",
                            "maxlength" =>  "60",
                            "extrahtml" =>  "",
                            "value"     =>  $row["GUARD_WORK_NAME"]));
        //勤務先電話番号
        $objForm->ae( array("type"      =>  "text",
                            "name"      =>  "GUARD_WORK_TELNO",
                            "size"      =>  "16",
                            "maxlength" =>  "14",
                            "extrahtml" =>  "",
                            "value"     =>  $row["GUARD_WORK_TELNO"]));

        //名称マスタよりコンボボックスのデータを取得
        $result = $db->query(knja110_2Query::get_name_mst());
        $opt = array();
        $opt["Z002"][] = array("label" => "","value" => "0");
        $opt["H201"][] = array("label" => "","value" => "00");
        $opt["H202"][] = array("label" => "","value" => "00");

        while($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[$row3["NAMECD1"]][] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                                             "value" => $row3["NAMECD2"]);
        }

        $objForm->ae( array("type"      => "select",
                            "name"      => "GUARD_SEX",
                            "extrahtml" => "",
                            "value"     => $row["GUARD_SEX"],
                            "options"   => $opt["Z002"]));

        $objForm->ae( array("type"      => "select",
                            "name"      => "RELATIONSHIP",
                            "extrahtml" => "",
                            "value"     => $row["RELATIONSHIP"],
                            "options"   => $opt["H201"]));

        $objForm->ae( array("type"      => "select",
                            "name"      => "GUARD_JOBCD",
                            "extrahtml" => "",
                            "value"     => $row["GUARD_JOBCD"],
                            "options"   => $opt["H202"]));
        //コピーボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_copy2",
                            "value"     => "生徒よりコピー",
#                            "extrahtml" => "onclick=\"copy('');\""));
                            "extrahtml" => "style=\"width:120px\"onclick=\"copy('');\""));

        $arg["data2"] = array("COPY"         =>  $objForm->ge("btn_copy2"),
                              "GUARD_ZIPCD"  => View::popUpZipCode($objForm, "GUARD_ZIPCD", $row["GUARD_ZIPCD"],"GUARD_ADDR1"),
                              "GUARD_NAME"   =>  $objForm->ge("GUARD_NAME"),
                              "GUARD_KANA"   =>  $objForm->ge("GUARD_KANA"),
                              "SEX"          =>  $objForm->ge("GUARD_SEX"),
                              "BIRTHDAY"     =>  View::popUpCalendar($objForm, "GUARD_BIRTHDAY",str_replace("-","/",$row["GUARD_BIRTHDAY"]),""),
                              "RELATIONSHIP" =>  $objForm->ge("RELATIONSHIP"),
                              "ADDR1"        =>  $objForm->ge("GUARD_ADDR1"),
                              "ADDR2"        =>  $objForm->ge("GUARD_ADDR2"),
                              "TELNO"        =>  $objForm->ge("GUARD_TELNO"),
                              "FAXNO"        =>  $objForm->ge("GUARD_FAXNO"),
                              "EMAIL"        =>  $objForm->ge("GUARD_E_MAIL"),
                              "WORK_NAME"    =>  $objForm->ge("GUARD_WORK_NAME"),
                              "JOBCD"        =>  $objForm->ge("GUARD_JOBCD"),
                              "WORK_TELNO"   =>  $objForm->ge("GUARD_WORK_TELNO"));

        $result->free();
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.top_frame.location.href='knja110_2index.php?cmd=list';";
        }

        View::toHTML($model, "knja110_2Form2.html", $arg);
    }
}
?>
