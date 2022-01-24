<?php

require_once('for_php7.php');

class knjp040kForm2 
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp040kindex.php", "", "edit");
        $arg["reload"] = "";

        if(!$model->isWarning()){
            $Row = knjp040kQuery::getRow($model);
        }else{
            $Row =& $model->field;
        }
        $db = Query::dbCheckOut();
        
        //生徒名
        $row = $db->getRow(knjp040kQuery::getStudentName($model->schregno),DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $row["SCHREGNO"];
        $arg["NAME_SHOW"] = $row["NAME_SHOW"];

        //銀行コード
        $opt = array();
        $result = $db->query(knjp040kQuery::getBankcd());
        # 2005/12/01 
        $opt[] = array("label" => "",
                       "value" => "");

        $i = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //支店コンボの初期値に、先頭の銀行コードの支店コードをセットするため
            if ($i == 0) $bankcd = $row["BANKCD"];
            
            $opt[] = array("label" => $row["BANKCD"]."：".$row["BANKNAME"],
                                           "value" => $row["BANKCD"]);
            $i++;
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "BANKCD",
                            "size"        => 1,
                            "value"       => $Row["BANKCD"] ,
                            "extrahtml"   => "OnChange=\"SetBranch(this);\"",
                            "options"     => $opt ));

        $arg["data"]["BANKCD"] = $objForm->ge("BANKCD");
        //支店コード
        $opt = array();
        $result = $db->query(knjp040kQuery::getBranchcd($Row["BANKCD"]));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["BRANCHCD"]."：".$row["BRANCHNAME"], "value" => $row["BRANCHCD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "BRANCHCD",
                            "size"        => 1,
                            "value"       => $Row["BRANCHCD"],
                            "options"     => $opt ));
        
        $arg["data"]["BRANCHCD"] = $objForm->ge("BRANCHCD");

        //預金種目
        $opt = array();
        $result = $db->query(knjp040kQuery::nameGet("G203"));

        # 2005/12/01 
        $opt[] = array("label" => "",
                       "value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
               $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"],
                              "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "DEPOSIT_ITEM",
                            "size"        => 1,
                            "value"       => $Row["DEPOSIT_ITEM"] ,
                            "options"     => $opt ));
        $arg["data"]["DEPOSIT_ITEM"] = $objForm->ge("DEPOSIT_ITEM");

        //続柄
        $opt = array();
        $opt[] = array("label" => "",
                       "value" => "");
        $opt[] = array("label" => "00:本人", "value" => "00");
        $result = $db->query(knjp040kQuery::nameGet("H201"));

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
               $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"],
                              "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "RELATIONSHIP",
                            "size"        => 1,
                            "value"       => ($model->relation ? $model->relation : $Row["RELATIONSHIP"]),
                            "options"     => $opt ));
        $arg["data"]["RELATIONSHIP"] = $objForm->ge("RELATIONSHIP");

        $result->free();
        Query::dbCheckIn($db);

        //口座番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "ACCOUNTNO",
                            "size"        => 7,
                            "maxlength"   => 7,
                            "extrahtml"   => "onBlur=\"this.value=toInteger(this.value);\"",
                            "value"       => $Row["ACCOUNTNO"] ));

        $arg["data"]["ACCOUNTNO"] = $objForm->ge("ACCOUNTNO");

        //口座名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "ACCOUNTNAME",
                            "size"        => 32,
                            "maxlength"   => 48,
                            "value"       => ($model->name ? mb_convert_kana($model->name, "KVC") : $Row["ACCOUNTNAME"])));

        $arg["data"]["ACCOUNTNAME"] = $objForm->ge("ACCOUNTNAME");

        //更新ボタンを作成
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成
        $objForm->ae( array("type" => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"return closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();
        
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.top_frame.location.href='knjp040kindex.php?cmd=list';";
        }

        View::toHTML($model, "knjp040kForm2.html", $arg);
    }
}
?>
