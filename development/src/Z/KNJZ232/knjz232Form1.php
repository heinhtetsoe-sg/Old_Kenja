<?php

require_once('for_php7.php');

class knjz232Form1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz232index.php", "", "list");

        //権限チェック
        if(AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $db = Query::dbCheckOut();
        //学年取得
        $opt2 = array();
        $result = $db->query(knjz232Query::GetGrade(CTRL_YEAR));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt2[] = array("label" => $row["GRADE"]."学年", 
                            "value" => $row["GRADE"]);
        }
        if ($model->grade == "") $model->grade = $opt2[0]["value"];
        
        //教科取得
        $opt = array();
        $result = $db->query(knjz232Query::GetClass(CTRL_YEAR, $model));
            //コンボボックスの一番上は空で全教科表示とする
            $opt[] = array("label" => "", 
                           "value" => "00");
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]."：".$row["CLASSNAME"],
                               "value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]);
            }
            if($model->classcd == ""){
                $model->classcd = ($row["CLASSCD"] != "")? $row["CLASSCD"].'-'.$row["SCHOOL_KIND"] : $opt[0]["value"];
            }
            if(strlen($model->classcd) == 2 && $model->classcd != '00'){
                if ($model->check_classcd != "" && $model->check_classcd != '00') {
                    $model->classcd = $model->classcd.'-'.$model->school_kind;
                } else {
                    $model->classcd = '00';
                }
            }
            //左画面の選択教科保持用の変数
            $model->check_classcd = $model->classcd;
        } else {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $row["CLASSCD"]."：".$row["CLASSNAME"],
                               "value" => $row["CLASSCD"]);
            }
            if($model->classcd == ""){
                $model->classcd = ($row["CLASSCD"] != "")? $row["CLASSCD"] : $opt[0]["value"];
            }
        }

        //科目リスト
        $cd = $row2 = array(); $cd_name;   //$rowはデータベース参照用、$row2は画面表示用

        $result = $db->query(knjz232Query::GetClasses(CTRL_YEAR, $model->grade, $model->classcd, $model));

        for ($i=0; $row=$result->fetchRow(DB_FETCHMODE_ASSOC); $i++)
        {
            if ($i == 0) {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $cd[] = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
                } else {
                    $cd[] = $row["SUBCLASSCD"];
                }
            }
            
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $Row["value"] = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
            } else {
                $Row["value"] = $row["SUBCLASSCD"];
            }
            //科目コードを比較
            if (in_array($Row["value"], $cd)) {
                if($row["NOLINK"] != 1) {
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $row2["LINK"]   = View::alink("knjz232index.php",
                                                      $Row["value"],
                                                      "target=right_frame",
                                                      array("cmd"           => "sel",
                                                            "CLASSCD"       => $row["CLASSCD"],
                                                            "SCHOOL_KIND"   => $row["SCHOOL_KIND"],
                                                            "CURRICULUM_CD" => $row["CURRICULUM_CD"],
                                                            "SUBCLASSCD"    => $row["SUBCLASSCD"],
                                                            "GRADE"         => $model->grade));
                    } else {
                        $row2["LINK"]   = View::alink("knjz232index.php",
                                                      $row["SUBCLASSCD"],
                                                      "target=right_frame",
                                                      array("cmd"        => "sel",
                                                            "SUBCLASSCD" => $row["SUBCLASSCD"],
                                                            "GRADE"      => $model->grade));
                    }
                } else {
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $row2["LINK"]   = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
                    } else {
                        $row2["LINK"]   = $row["SUBCLASSCD"];
                    }
                }
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $row2["SUBCLASSNAME"] = $row["SUBCLASSNAME"];
                    $row2["GRADING"]     .= ",".$row["GRADING_CLASSCD"]."-".$row["GRADING_SCHOOL_KIND"]."-".$row["GRADING_CURRICULUM_CD"]."-".$row["GRADING_SUBCLASSCD"]." ".$row["GRADING_SUBCLASSNAME"];  //追加
                } else {
                    $row2["SUBCLASSNAME"] = $row["SUBCLASSNAME"];
                    $row2["GRADING"]     .= ",".$row["GRADING_SUBCLASSCD"]." ".$row["GRADING_SUBCLASSNAME"];  //追加
                }
            } else {
                $row2["GRADING"]    = substr($row2["GRADING"],1);  //先頭のカンマを除く
                $arg["data"][] = $row2;

                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $cd[] = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
                } else {
                    $cd[] = $row["SUBCLASSCD"];  //科目コードを保存
                }
                $row2["GRADING"]  = "";      //クリア

                if($row["NOLINK"] != 1) {
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $row2["LINK"]   = View::alink("knjz232index.php",
                                                      $Row["value"],
                                                      "target=right_frame",
                                                      array("cmd"           => "sel",
                                                            "CLASSCD"       => $row["CLASSCD"],
                                                            "SCHOOL_KIND"   => $row["SCHOOL_KIND"],
                                                            "CURRICULUM_CD" => $row["CURRICULUM_CD"],
                                                            "SUBCLASSCD"    => $row["SUBCLASSCD"],
                                                            "GRADE"         => $model->grade));
                    } else {
                        $row2["LINK"]   = View::alink("knjz232index.php",
                                                      $row["SUBCLASSCD"],
                                                      "target=right_frame",
                                                      array("cmd"        => "sel",
                                                            "SUBCLASSCD" => $row["SUBCLASSCD"],
                                                            "GRADE"      => $model->grade));
                    }
                } else {
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $row2["LINK"]   = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
                    } else {
                        $row2["LINK"]   = $row["SUBCLASSCD"];
                    }
                }
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $row2["SUBCLASSNAME"] = $row["SUBCLASSNAME"];
                    $row2["GRADING"]     .= ",".$row["GRADING_CLASSCD"]."-".$row["GRADING_SCHOOL_KIND"]."-".$row["GRADING_CURRICULUM_CD"]."-".$row["GRADING_SUBCLASSCD"]." ".$row["GRADING_SUBCLASSNAME"];  //追加
                } else {
                    $row2["SUBCLASSNAME"] = $row["SUBCLASSNAME"];
                    $row2["GRADING"]     .= ",".$row["GRADING_SUBCLASSCD"]." ".$row["GRADING_SUBCLASSNAME"];  //追加
                }
            }

        }
        $row2["GRADING"]    = substr($row2["GRADING"],1);  //先頭のカンマを除く
        $arg["data"][] = $row2;

        $result->free();
        Query::dbCheckIn($db);

        //対象年度
        $arg["year"] = CTRL_YEAR;

        //学年コンボボック
        $objForm->ae( array("type"        => "select",
                            "name"        => "grade",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('list')\"",
                            "value"       => $model->grade,
                            "options"     => $opt2 ));
        $arg["grade"] = $objForm->ge("grade");

        //教科コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "classcd",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('list')\"",
                            "value"       => $model->classcd,
                            "options"     => $opt ));
        $arg["classcd"] = $objForm->ge("classcd");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz232Form1.html", $arg); 
    }
    
}
?>
