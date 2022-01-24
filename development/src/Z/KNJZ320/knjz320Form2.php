<?php

require_once('for_php7.php');

class knjz320Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjz320index.php", "", "main");

        $db = Query::dbCheckOut();

        $query = knjz320Query::selectSchoolCd();
        $model->schoolCd = $db->getOne($query);

        $staffarr = array();
        $query = knjz320Query::GetGroup($model, CTRL_YEAR);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $staffarr[] = array("label" => $row["STAFFCD"]."　".$row["STAFFNAME"], 
                           "value" => $row["STAFFCD"]);
        }
        $result->free();

        if ($model->setstaff=="") 
            $model->setstaff = $staffarr[0]["value"];

        if ($model->cmd == "copy" && isset($model->refstaff)){
            $staffcd = $model->refstaff;
        } elseif (isset($model->setstaff)) {
            $staffcd = $model->setstaff;
        }

        if ($staffcd != "")
        {
            $result = $db->query(knjz320Query::GetProgramAuth($model, $model->menuid, $staffcd));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $autharray[$row["MENUID"]] = $row["USERAUTH"];
            }
        }
        if ($model->menuid!="")
        {

            $model->menuidarr = array();
            $result    = $db->query(knjz320Query::selectQuery($model, $model->menuid));

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $row["cellid0"]   = "0,".$row["MENUID"];
                $row["cellid1"]   = "1,".$row["MENUID"];
                $row["cellid2"]   = "2,".$row["MENUID"];
                $row["cellid3"]   = "3,".$row["MENUID"];
                $row["cellid9"]   = "9,".$row["MENUID"];
                $row["cellid7"]   = "7,".$row["MENUID"];

                if ($staffcd != "") {
                    if ( !isset($autharray[$row["MENUID"]])){ //データがない場合は未設定をオンにする

                        $row["CHECK7"] = "checked";
                        $row["bgcolor7"] = "#ccffcc";
                    } else {
                        $row["CHECK".$autharray[$row["MENUID"]]] = "checked";
                        $row["bgcolor".$autharray[$row["MENUID"]]] = "#ccffcc";
                    }
                }
               if ($row["PROCESSCD"] != "")
                {
                    $model->menuidarr[] = $row["MENUID"];
                    $arg["data"][] = $row; 
                }
            }
        }
        Query::dbCheckIn($db);

        //参照職員
        $objForm->ae( array("type"        => "select",
                            "name"        => "refstaff",
                            "size"        => "1",
                            "value"       => $model->refstaff,
                            "options"     => $staffarr));
        $arg["refstaff"] = $objForm->ge("refstaff");
                        
        //コピーボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_copy",
                            "value"       => "左の利用者の権限をコピー",
                            "extrahtml"   => "style=\"width:200px\"onclick=\"btn_submit('copy')\"") );
        $arg["btn_copy"] = $objForm->ge("btn_copy");

        //設定職員
        $objForm->ae( array("type"        => "select",
                            "name"        => "setstaff",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"btn_submit('main')\"",
                            "value"       => $model->setstaff,
                            "options"      => $staffarr));    
        $arg["setstaff"] = $objForm->ge("setstaff");
    
        //選択されたメニュー名の表示
        $arg["menuname"] = (isset($model->menuid)) ? $model->menuname : "";

        //職員別権限確認
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_authlist",
                            "value"       => "職員別権限確認",
                            "extrahtml"   => "style=\"width:130px\"onclick=\"window.open('".REQUESTROOT."/Z/KNJZ320/knjz320index.php?cmd=listauth', '_parent');\"" ) );

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"] = array("BTN_AUTHLIST" => $objForm->ge("btn_authlist"),
                               "BTN_OK"       => $objForm->ge("btn_keep"),
                               "BTN_CLEAR"    => $objForm->ge("btn_clear"),
                               "BTN_END"      => $objForm->ge("btn_end"));  

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"        => $model->cmd) );
    
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjz320Form2.html", $arg);
    }
}       
?>
