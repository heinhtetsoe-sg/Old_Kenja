<?php

require_once('for_php7.php');

class knjz230Form1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz230index.php", "", "list");

        //権限チェック
        if(AUTHORITY != DEF_UPDATABLE) $arg["jscript"] = "OnAuthError();";

        //DB open
        $db = Query::dbCheckOut();

        //教科コンボボックス設定
        $opt    = array();
        $result = $db->query(knjz230Query::GetClassData(CTRL_YEAR));

        //教科コンボボックスオプション作成
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => "  ".$row["CLASSCD"]."  ".$row["CLASSNAME"], 
                           "value" => $row["CLASSCD"]);
        }

        //教科コンボボックスの初期値
        if ($model->cmb_class == "") $model->cmb_class = $opt[0]["value"];

        //教科コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "CMB_CLASS",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('list')\"",
                            "value"       => $model->cmb_class,
                            "options"     => $opt ));

        //ヘッダー作成
        $arg["HEADERS"] = array( "YEAR"      => "対象年度：".CTRL_YEAR." 年度",
//                                 "SEMES"     => "  ".CTRL_SEMESTER." 学期",
                                 "SEMES"     => "  ".CTRL_SEMESTERNAME." ",
                                 "CMB_CLASS" => "教科：".$objForm->ge("CMB_CLASS") );

        $result   = $db->query(knjz230Query::getReplaced_val($model->cmb_class));
        $countReplace = array();
        //データ整理
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $countReplace[$row["GRADING_CHAIRCD"]][$row["REPLACECD"]] = $row["COUNT"];
        }

        //リストデータ作成
        $result   = $db->query(knjz230Query::GetListValue($model->cmb_class));
        $rowValue = $targetclass = array();

        //データ整理
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //リンク作成
            array_walk($row, "htmlspecialchars_array");
            if(!isset($countReplace[$row["CHAIRCD"]][2]) ){
                $row["LINK"]  = View::alink("knjz230index.php", 
                                             $row["CHAIRCD"]."  ".$row["CHAIRNAME"],
                                             "target=right_frame", 
                                              array( "cmd"      => "sel", 
                                                     "CHAIRCD"  => $row["CHAIRCD"],
                                                     "SUBCLSCD" => substr($model->cmb_class,0,2),
                                                     "GRADE"    => $row["GRADE"],
                                                     "HR_CLASS" => $row["HR_CLASS"]));
                $color["START"] = "";
                $color["END"] = "";
            }else{
                $row["LINK"] = $row["CHAIRCD"]."  ".$row["CHAIRNAME"];
                $color["START"] = "<FONT COLOR=\"#CCCCCC\">";
                $color["END"] = "</FONT>";
                //$row["LINK"] = "<FONT COLOR=\"#CCCCCC\">".$row["CHAIRCD"]."  ".$row["CHAIRNAME"]."</FONT>";
            }

            if(isset($targetclass[$row["CHAIRCD"]])){
                if( strlen($targetclass[$row["CHAIRCD"]]) == strlen($row["GRCL"])){
                    $targetclass[$row["CHAIRCD"]] .= "*" ; 
                 }
            }else{
                $targetclass[$row["CHAIRCD"]] = $row["GRCL"]; 
            }

            $rowValue[$row["CHAIRCD"]] = array( "MAINCLASS" => (isset($countReplace[$row["CHAIRCD"]][1]))? "*" : "&nbsp;" ,
                                                "LINK"      => $row["LINK"],
                                                "CLSNAME"   => $row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],
                                                "STAFF"     => $row["STAFFCD"]." ".$row["STAFFNAME_SHOW"],
                                                "TARGETCLS" => $targetclass[$row["CHAIRCD"]],
                                                "COLOR_S"   => $color["START"],
                                                "COLOR_E"   => $color["END"]
                                                );
        }

        foreach($rowValue as $key => $val){
            $arg["data"][]  = $rowValue[$key];
        }

        //DB close
        $result->free();
        Query::dbCheckIn($db);

        //hidden作成
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        if(VARS::get("cmd") != "list"){
            $arg["reload"] = "window.open('knjz230index.php?cmd=sel&init=1', 'right_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz230Form1.html", $arg); 
    }
}
?>
