<?php

require_once('for_php7.php');

class knjz330form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz330index.php", "", "main");
        $grouparr        = knjz330Query::GetGroup($model);
        $db             = Query::dbCheckOut();
        $autharray      = array();
    
        if ($model->setgroup=="") 
            $model->setgroup = $grouparr[0]["value"];

        if ($model->cmd == "copy" && isset($model->refgroup)) {
            $groupcode = $model->refgroup;
        } elseif (isset($model->setgroup)) {
            $groupcode = $model->setgroup;
        }

        if ($groupcode != "")
        {    
            $result = $db->query(knjz330Query::GetProgramAuth($model, $model->menuid, $groupcode));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $autharray[$row["MENUID"]] = $row["GROUPAUTH"];
            }
        }

        if ($model->menuid!="")
        {
            $model->menuidarr = array();
            $result           = $db->query(knjz330Query::selectQuery($model, $model->menuid));

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {    
                $row["cellid0"]   = "0,".$row["MENUID"];
                $row["cellid1"]   = "1,".$row["MENUID"];
                $row["cellid2"]   = "2,".$row["MENUID"];
                $row["cellid3"]   = "3,".$row["MENUID"];
                $row["cellid9"]   = "9,".$row["MENUID"];
                $row["cellid7"]   = "7,".$row["MENUID"];
                                
                if ($groupcode != "" && !isset($autharray[$row["MENUID"]])){
                    $row["CHECK7"]   = "checked";
                    $row["bgcolor7"] = "#ccffcc";
                } else {
                    $row["CHECK".$autharray[$row["MENUID"]]]   = "checked";
                    $row["bgcolor".$autharray[$row["MENUID"]]] = "#ccffcc";
                }
                if ($row["PROCESSCD"] != "")
                {
                    $model->menuidarr[] = $row["MENUID"];
                    $arg["data"][] = $row; 
                }
            }
        }
        Query::dbCheckIn($db);

        //参照グループ
        $objForm->ae( array("type"        => "select",
                            "name"        => "refgroup",
                            "size"        => "1",
                            "value"       => $model->refgroup,
                            "options"      => $grouparr));
        $arg["refgroup"] = $objForm->ge("refgroup");
                        
        //コピーボタン
        $objForm->ae( array("type"         => "button",
                            "name"      => "btn_copy",
                            "value"     => "左のグループの権限をコピー",
                            "extrahtml" => "style=\"width:200px\"onclick=\"btn_submit('copy')\"") );
        $arg["btn_copy"] = $objForm->ge("btn_copy");

        //設定グループ
        $objForm->ae( array("type"        => "select",
                            "name"        => "setgroup",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"btn_submit('main')\"",
                            "value"       => $model->setgroup,
                            "options"      => $grouparr));    
        $arg["setgroup"] = $objForm->ge("setgroup");
    
        //選択されたメニュー名の表示
        $arg["menuname"] = (isset($model->menuid)) ? $model->menuname : "";
    
        //職員別権限確認
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_authlist",
                            "value"       => "グループ別権限確認",
                            "extrahtml"   => "style=\"width:150px\"onclick=\"window.open('".REQUESTROOT."/Z/KNJZ330/knjz330index.php?cmd=listauth', '_parent');\"" ) );

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        //取消ボタン
        $objForm->ae( array("type"        => "reset",
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

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"        => $model->cmd) );
    
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjz330Form2.html", $arg);
    }
}       
?>
