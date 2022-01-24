<?php

require_once('for_php7.php');

class knja110_2Form1
{
    function main(&$model)
    {    
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knja110_2index.php", "", "edit");
        $arg["close"] = "";
        $arg["data"] = array();
        
        $db = Query::dbCheckOut();
        
        //起動チェック
        if (!knja110_2Query::ChecktoStart($db)) {
            $link = REQUESTROOT."/A/KNJA110/knja110index.php?cmd=edit&schregno=".$model->schregno;
            $arg["close"] = "closing_window('$link');";
        }

        //学籍基礎マスタより学籍番号と名前を取得
        $Row         = $db->getRow(knja110_2Query::getSchregno_name($model),DB_FETCHMODE_ASSOC);
        $arg["NO"]   = $Row["SCHREGNO"];
        $arg["NAME"] = $Row["NAME"];

        //学籍住所データよりデータを取得
        if($model->schregno)
        {
           //緊急連絡先情報の取得(2005/10/20 ADD)
            $row2 = $db->getRow(knja110_2Query::getEmergencyInfo($model->schregno),DB_FETCHMODE_ASSOC);
            $result = $db->query(knja110_2Query::getAddress_all($model));

            while($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $row  = array_merge((array)$row1,(array)$row2);

                $name1 = $db->getOne(knja110_2Query::List_AreaCd($row["AREACD"]));
                $row["AREA_CD"]    = $row["AREACD"].":".$name1;
                $row["ISSUEDATE"]  = str_replace("-","/",$row["ISSUEDATE"]);
                $row["EXPIREDATE"] = str_replace("-","/",$row["EXPIREDATE"]);
                $arg["data"][] = $row;
            }

        }
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "clear",
                            "value"     => "0"));

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja110_2Form1.html", $arg);
    }
}
?>
