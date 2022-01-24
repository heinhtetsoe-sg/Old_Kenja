<?php

require_once('for_php7.php');

class knjxexpkForm1
{
    function main(&$model){
/*       if ($model->usr_auth != DEF_UPDATABLE && $model->usr_auth != DEF_UPDATE_RESTRICT){
           $arg["jscript"] = "OnAuthError();";
       } */

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");

        $db     = Query::dbCheckOut();
        //年度と学期
        $arg["CTRL_YEAR"]       = CTRL_YEAR;
        $arg["CTRL_SEMESTER"]   = CTRL_SEMESTERNAME;

        //学年コンボボックス
        $opt = array();
        if ($model->cmd == "search" || 
            $model->cmd == "search2" ||
            $model->cmd == "search3" ||
            $model->cmd == "search4" ||
            $model->cmd == "search5" ||
            $model->cmd == "search6"
            ){
            $opt[] = array("label"  => '',
                            "value" => '00-000');
        }
        $result = $db->query(knjxexpkQuery::GetHr_Class($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
#minei                $opt[] = array("label"  => (int) $row["GRADE"]."年".$row["HR_CLASS"]."組",
            $opt[] = array("label"  => $row["HR_NAME"],
                            "value" => $row["GRADE"]."-".$row["HR_CLASS"]);

            if (!isset($model->grade)) $model->grade = $row["GRADE"] ."-" .$row["HR_CLASS"];
            if ($model->grade == $row["GRADE"] ."-" .$row["HR_CLASS"] && $model->cmd == "list")
                $staffname = "担任名：". $row["STAFFNAME_SHOW"] ."( " .$row["STAFFCD"] ." )";
        }
        //担任名
        $arg["STAFFNAME"] = $staffname;
        $objForm->ae( array("type"        => "select",
                            "name"        => "GRADE",
                            "size"        => "1",
                            "extrahtml"   => "Onchange=\"btn_submit('chg_grade')\"",
                            "value"       => $model->grade,
                            "options"     => $opt ));

        $arg["GRADE"] = "年組：" .$objForm->ge("GRADE");

        if ($model->cmd == "list" || $model->cmd == "search"
            || $model->cmd == "search2"
            || $model->cmd == "search3"
            || $model->cmd == "search4"
            || $model->cmd == "search5"
            || $model->cmd == "search6"
            ){
            //生徒表示
            if ($model->cmd == "search2"){
                $result = $db->query(knjxexpkQuery::GetStudents2($model));
            }else if ($model->cmd == "search3"){
                $result = $db->query(knjxexpkQuery::GetStudents3($model));
            }else if ($model->cmd == "search4"){
                $result = $db->query(knjxexpkQuery::GetStudents4($model));
            }else if ($model->cmd == "search5"){
                $result = $db->query(knjxexpkQuery::GetStudents5($model));
            }else if ($model->cmd == "search6"){
                $result = $db->query(knjxexpkQuery::GetStudents6($model));
            }else{
                $result = $db->query(knjxexpkQuery::GetStudents($model));
            }
            $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
            $i =0;
            list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);

            $next_flg = false;
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $a = array("cmd"    => $cmd,
                          "SCHREGNO"    => $row["SCHREGNO"],
                          "GRADE"       => $row["GRADE"],
                          "HR_CLASS"    => $row["HR_CLASS"],
                          "ATTENDNO"    => $row["ATTENDNO"],
                          "NAME"        => $row["NAME_SHOW"]);

#               $row["NAME_SHOW"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME_SHOW"]), "target=" .$model->target[$model->programid] ." onclick=\"Link(this)\"",$a);
                $row["NAME_SHOW"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME_SHOW"]), "target=" .$model->target[$model->programid],$a);
                $row["IMAGE"] = $image[($row["SEX"]-1)];
#                $row["ATTENDNO"] = $row["HR_NAMEABBV"]."-".str_replace(" ","&nbsp;",sprintf("%3d",$row["ATTENDNO"]));
                $row["ATTENDNO"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
                $arg["data"][] = $row;
                $i++;
            }
            //hidden
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "MAXROW",
                                "value"     => $i
                                ) );
            $arg["CLASS_SUM"] = $i;
            $result->free();
        }
        Query::dbCheckIn($db);

        if ($model->usr_auth == DEF_UPDATABLE || $model->usr_auth == DEF_REFERABLE){
            //在ボタンを作成する
            $objForm->ae( array("type"	=> "button",
                                "name"	=> "btn_ungrd",
                                "value"	=> " 在 ",
                                "extrahtml" => "onclick=\"showSearch()\""));

            $arg["btn_ungrd"] = $objForm->ge("btn_ungrd");
        }
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd
                            ) );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "path",
                            "value"     => REQUESTROOT .$model->path[$model->programid]
                            ) );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PROGRAMID",
                            "value"     => $model->programid
                            ) );
                            
                            
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ROW",
                            "value"     => $model->row
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DISP",
                            "value"     => $model->disp
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCROLLLEFT",
                            "value"     => $model->scrollleft
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCROLLTOP",
                            "value"     => $model->scrolltop
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        $arg["jscript"] = "";

        if($model->cmd=="search"){
            $arg["jscript"] .= "document.forms[0].GRADE.value = '" .$model->search["GRADE"] ."'\n";
        }
        if (is_numeric($model->scrollleft) && is_numeric($model->scrolltop)){
            $arg["jscript"] .= "window.scroll(".$model->scrollleft.",".$model->scrolltop.");\n";
        }
        View::toHTML($model, "knjxexpkForm1.html", $arg);
    }
}
?>
