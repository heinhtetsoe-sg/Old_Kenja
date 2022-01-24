<?php

require_once('for_php7.php');

class knjz231Form1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz231index.php", "", "list");

        //権限チェック
        if(AUTHORITY != DEF_UPDATABLE) $arg["jscript"] = "OnAuthError();";

        //DB open
        $db = Query::dbCheckOut();

        //教科コンボボックス設定
        $opt    = array();
        $result = $db->query(knjz231Query::GetClassData(CTRL_YEAR));

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
                                 "SEMES"     => "  ".CTRL_SEMESTER." 学期",
                                 "CMB_CLASS" => "教科：  ".$objForm->ge("CMB_CLASS") );
#
        $result   = $db->query(knjz231Query::getReplaced_val($model->cmb_class));
        $countReplace = array();
        //データ整理
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $countReplace[$row["ATTEND_CHAIRCD"]][$row["REPLACECD"]] = 1;
        }
#

        //リストデータ作成
        $result   = $db->query(knjz231Query::getListValue($model->cmb_class));
        $rowValue = $targetclass = array();
        //全て選択チェックボックス
        $objForm->ae(array("type"       => "checkbox",
                           "name"       => "ALLCHAIRCHK",
                           "value"      => "",
                           "checked"    => "",
                           "extrahtml"  => "onClick=\"return check_all(this);\""));
        $arg["ALLCHAIRCHK"] = $objForm->ge("ALLCHAIRCHK");
        //データ整理
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //リンク作成
            array_walk($row, "htmlspecialchars_array");
            if(!isset($countReplace[$row["CHAIRCD"]][1]) ){
                $row["LINK"] = $row["CHAIRCD"]."  ".$row["CHAIRNAME"];
                $objForm->ae(array("type"       => "checkbox",
                                   "name"       => "CHAIRCHK",
                                   "value"      => $row["CHAIRCD"],
                                   "checked"    => "",
                                   "extrahtml"  => "onClick=\"return reload(this);\""));
                $chk["CHAIRCHK"] = $objForm->ge("CHAIRCHK");
                $color["START"] = "";
                $color["END"] = "";
            }else{
                $row["LINK"] = $row["CHAIRCD"]."  ".$row["CHAIRNAME"];
                $chk["CHAIRCHK"] = "";
                $color["START"] = "<FONT COLOR=\"#CCCCCC\">";
                $color["END"] = "</FONT>";
            }
            if(isset($targetclass[$row["CHAIRCD"]])){
                if( strlen($targetclass[$row["CHAIRCD"]]) == strlen($row["GRCL"])){
                    $targetclass[$row["CHAIRCD"]] .= "*" ;
                 }
            }else{
                $targetclass[$row["CHAIRCD"]] = $row["GRCL"]; 
            }
            if(isset($countReplace[$row["CHAIRCD"]][2]))
            {
                $tag["MAINCLASS"] = "*";
            }else{
                $tag["MAINCLASS"] = "&nbsp;";
            }
            $rowValue[$row["CHAIRCD"]] = array( "MAINCLASS"     => $tag["MAINCLASS"],
                                                "CHAIRCHK"      => $chk["CHAIRCHK"],
                                                "LINK"          => $row["LINK"],
                                                "CLSNAME"       => $row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],
                                                "STAFF"         => $row["STAFFCD"]." ".$row["STAFFNAME_SHOW"],
                                                "TARGETCLS"     => $targetclass[$row["CHAIRCD"]],
                                                "COLOR_S"       => $color["START"],
                                                "COLOR_E"       => $color["END"]
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
            $arg["reload"] = "window.open('knjz231index.php?cmd=sel&init=1', 'right_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz231Form1.html", $arg);
    }
}
?>
