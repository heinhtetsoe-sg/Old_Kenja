<?php

require_once('for_php7.php');

class knjm210m_3Form3
{
    function main(&$model)
    {    
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjm210m_3index.php", "", "edit");
        $arg["data"] = array();
        
        $db = Query::dbCheckOut();
        
        //学籍基礎マスタより学籍番号と名前を取得
//        $model->schregno = "20031935";
        $Row         = $db->getRow(knjm210m_3Query::getSchregno_name($model->schregno),DB_FETCHMODE_ASSOC);
        $arg["NAME"] = "　　　学籍番号：".$Row["SCHREGNO"]."　　　氏名：".$Row["NAME"];

        //学籍住所データよりデータを取得
        if($model->schregno)
        {
            $result = $db->query(knjm210m_3Query::getAddress_all($model->schregno));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $name1 = $db->getOne(knjm210m_3Query::List_AreaCd($row["AREACD"]));
                $row["AREA_CD"]    = $row["AREACD"].":".$name1;
                $row["ISSUEDATE"]  = str_replace("-","/",$row["ISSUEDATE"]);
                $row["EXPIREDATE"] = str_replace("-","/",$row["EXPIREDATE"]);
                $arg["data"][] = $row;
            }
        }

        //保護者情報の取得
        $row = $db->getRow(knjm210m_3Query::getGuardianAddr($model->schregno),DB_FETCHMODE_ASSOC);

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
        $result = $db->query(knjm210m_3Query::get_name_mst());
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

        $arg["data2"] = array("GUARD_ZIPCD"  => View::popUpZipCode($objForm, "GUARD_ZIPCD", $row["GUARD_ZIPCD"],"GUARD_ADDR1"),
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

        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "clear",
                            "value"     => "0"));

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm210m_3Form3.html", $arg);
    }
}
?>
