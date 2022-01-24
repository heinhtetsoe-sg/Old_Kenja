<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjxsearch8Form1.php 56591 2017-10-22 13:04:39Z maeshiro $
class knjxsearch8Form1
{
    function main(&$model){

        //権限チェック
        //$auth = common::SecurityCheck(STAFFCD, $model->programid);
        //if ($auth != DEF_UPDATABLE){
        //    $arg["jscript"] = "OnAuthError();";
        //}
    
        $objForm = new form;
        $arg["start"] = $objForm->get_start("knjxsearch8Form1", "POST", "index.php", "", "knjxsearch8Form1");
    
        $db     = Query::dbCheckOut();
    
        //検索ボタン
        $objForm->ae( array("type" 		=> "button",
                            "name"      => "SEARCH_BTN",
                            "value"     => "検索条件入力",
                            "extrahtml" => "onclick=\"wopen('index.php?cmd=search_view','knjxSearch8',0,0,500,250);\""));
    
        $arg["SEARCH_BTN"] = $objForm->ge("SEARCH_BTN");
    
        //検索結果表示
        if ($model->cmd == "search") {
            $result = $db->query(knjxsearch8Query::SearchStudent($model->search_fields));
            $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
            $i =0;
            list($path, $cmd) = explode("?cmd=", $model->path);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                 array_walk($row, "htmlspecialchars_array");
                 $row["GRADUATE_CLASS"] = $row["HR_NAME"].$row["ATTENDNO"]."番";
                 $row["IMAGE"]    = $image[($row["SEXNUM"]-1)];
                 $row["BIRTHDAY"] = str_replace("-","/",$row["BIRTHDAY"]);
                 $arg["data"][]   = $row;
                 $i++;
            }
            $arg["RESULT"] = "結果　".$i."件";
            $result->free();
            if ($i == 0) {
                $arg["search_result"] = "SearchResult();";
            }
        }
    
        Query::dbCheckIn($db);
    
        //hidden(検索条件値を格納する)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHOOL_NAME") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "BUNAME") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "KANAME") );
    
        $arg["finish"]  = $objForm->get_finish();
    
        if(VARS::post("cmd")==""){
            $arg["reload"] ="wopen('index.php?cmd=search_view','knjxSearch8',0,0,500,250);";
        }
        View::toHTML($model, "knjxsearch8Form1.html", $arg);
    }
}
?>
