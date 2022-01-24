<?php

require_once('for_php7.php');

class knjxexp_guiForm1
{
    function main(&$model){
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");

        $db = Query::dbCheckOut();
        
        //年度、学期
        $arg["EXP_YEAR"] = "&nbsp;年度：" .CTRL_YEAR ."&nbsp;&nbsp;学期：" .CTRL_SEMESTERNAME;
        
        //学年コンボボックス
        $opt = array();
        $result = $db->query(knjxexp_guiQuery::GetGrade($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"  => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($model->grade == "") $model->grade = $row["VALUE"];
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "GRADE",
                            "size"        => "1",
                            "extrahtml"   => "Onchange=\"btn_submit('chg_grade')\"",
                            "value"       => $model->grade,
                            "options"     => $opt ));

        $arg["GRADE"] = "学年：" .$objForm->ge("GRADE");
        $arg["SEARCH_A"] = "1";

        if (($model->cmd == "list") || $model->cmd == "search") {
            //生徒表示
            $result = $db->query(knjxexp_guiQuery::GetStudents($model));
            $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
            $i = 0;
            $linkCnt = 0;
            $schregno = array();
            list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);

            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $schregno[] = $row["SCHREGNO"];
                $a = array("cmd"    => $cmd,
                          "SCHREGNO"    => $row["SCHREGNO"],
                          "mode"        => $model->mode,
                          "EXP_YEAR"    => CTRL_YEAR,
                          "EXP_SEMESTER"=> CTRL_SEMESTER,
                          "GRADE"       => $row["GRADE"],
                          "HR_CLASS"    => $row["HR_CLASS"],
                          "ATTENDNO"    => $row["ATTENDNO"],
                          "NAME"        => $row["NAME_SHOW"]);
                $row["NAME_SHOW"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME_SHOW"]), "target=" .$model->target[$model->programid] ." onclick=\"Link(this)\"",$a);
                $row["IMAGE"] = $image[($row["SEX"]-1)];
                $row["ATTENDNO"] = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
                $arg["data"][] = $row;
                $i++;
            }
            $arg["CLASS_SUM"] = $i;
            $result->free();
        }
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GRADE2") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GRD_YEAR") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "HR_CLASS") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "COURSECODE") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "NAME") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "NAME_SHOW") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "NAME_KANA") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "NAME_ENG") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEX") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DATE") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "mode",
                            "value"     => $model->mode
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
                            
        if (is_array($schregno)){
            //hidden
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "SCHREGNO",
                                "value"     => implode(",", $schregno)
                                ) );
        }

        //hidden
        knjCreateHidden($objForm, "changeFlg");
        knjCreateHidden($objForm, "setOrder");

        $arg["finish"]  = $objForm->get_finish();
        $arg["jscript"] = "";

        View::toHTML($model, "knjxexp_guiForm1.html", $arg);
    }
}
?>
