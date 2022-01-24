<?php
class knjvexpForm1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");

        $db     = Query::dbCheckOut();
        //年度と学期
        if (!isset($model->exp_year)) $model->exp_year = CTRL_YEAR ."-" .CTRL_SEMESTER;
        $arg["EXP_YEAR"] = "&nbsp;年度：" .CTRL_YEAR ."&nbsp;&nbsp;学期：" .CTRL_SEMESTERNAME;
        
        //学籍番号テキストボックス
        $extra = "";
        $arg["GAKUSEKINO"] = knjCreateTextBox($objForm, $model->field["GAKUSEKINO"], "GAKUSEKINO", 10, 10, $extra);

        //学年コンボボックス
        $opt = array();
        $opt[] = array("label"  => '',
                        "value" => '');
        $result = $db->query(knjvexpQuery::GetHr_Class($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"  => $row["HR_NAME"],
                           "value" => $row["GRADE"]."-".$row["HR_CLASS"]);
            if (!isset($model->grade)) $model->grade = $row["GRADE"] ."-" .$row["HR_CLASS"];
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "GRADE",
                            "size"        => "1",
                            //"extrahtml"   => "Onchange=\"btn_submit('chg_grade')\"",
                            "extrahtml"   => "",
                            "value"       => $model->field["GRADE"],
                            "options"     => $opt ));

        $arg["GRADE"] = $objForm->ge("GRADE");
        
        //コース
        $query = knjvexpQuery::GetCourseCode();
        $result = $db->query($query);
        /*$course[1] = "国立文系";
        $course[2] = "国立理系";
        $course[3] = "私立文系";
        $course[4] = "私立理系";*/
        
        $opt2 = array();
        $opt2[] = array("label"  => '',
                        "value" => '');
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt2[] = array("label" => $row["COURSECODENAME"],
                            "value" => $row["COURSECODE"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "COURSE",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $model->field["COURSE"],
                            "options"     => $opt2 ));

        $arg["COURSE"] = $objForm->ge("COURSE");
        
        //氏名
        $extra = "";
        $arg["NAME"] = knjCreateTextBox($objForm, $model->field["NAME"], "NAME", 20,10,$extra);
        
        //氏名かな
        $extra = "";
        $arg["NAME_KANA"] = knjCreateTextBox($objForm, $model->field["NAME_KANA"], "NAME_KANA", 20,10,$extra);
        
        
        //検索結果
        $search = array();
        if($model->cmd == "search"){
            $query = knjvexpQuery::getStudent($model, $model->field, "1");
            $cnt = $db->getOne($query);
            if($cnt == 0){
                $model->setMessage('対象のデータはありません。');
                
            }else{
                $query = knjvexpQuery::getStudent($model, $model->field);
                $result = $db->query($query);
                
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $search["GAKUNEN"] = $row["GRADE"]."-".$row["HR_CLASS"]."-".$row["ATTENDNO"];
                    $programid = mb_strtolower($model->programid);
                    $url = REQUESTROOT."/H/{$model->programid}/{$programid}index.php?cmd=change&GAKUSEKI=".$row["KNJID"];
                    $search["NAME"] = View::alink($url, htmlspecialchars("{$row["NAME"]}"), "target=\"right_frame\";");
                    
                    $arg["data"][] = $search;
                }
            }
        }
        /*$search["GAKUNEN"] = "1-1-001";
        $search["NAME"] = View::alink("knjv1010index.php?cmd=edit&GAKUSEKI=", htmlspecialchars("アルプ太郎"), "target=\"left_frame\";");
        $arg["data"][] = $search;
        
        $search["GAKUNEN"] = "1-1-002";
        $search["NAME"] = View::alink("#", htmlspecialchars("アルプ花子"), "target=\"left_frame\";");
        
        $arg["data"][] = $search;
        */
        
        //検索ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_search",
                            "value"       => "検索",
                            "extrahtml"   => " onclick=\"btn_submit('search');\""));
        $arg["btn_search"] = $objForm->ge("btn_search");

        //閉じるボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["btn_end"] = $objForm->ge("btn_end");

        //生徒表示
        /*$result = $db->query(knjvexpQuery::GetStudents($model));
        $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
        $i = 0;
        $linkCnt = 0;
        $schregno = array();
        list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);
        list($exp_year, $exp_semester) = explode("-", $model->exp_year);

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $schregno[] = $row["SCHREGNO"];
            $a = array("cmd"    => $cmd,
                      "SCHREGNO"    => $row["SCHREGNO"],
                      "mode"        => $model->mode,
                      "EXP_YEAR"    => $exp_year,
                      "EXP_SEMESTER"=> $exp_semester,
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
        $result->free();*/
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd
                            ) );

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
                            

        //hidden
        knjCreateHidden($objForm, "changeFlg");
        knjCreateHidden($objForm, "setOrder");

        $arg["finish"]  = $objForm->get_finish();
        $arg["jscript"] = "";

        /*if($model->cmd=="search" && $model->button[$model->programid] != 2){
            $arg["jscript"] .= "document.forms[0].GRADE.value = '" .$model->search["GRADE"] ."'\n";
        }*/
        View::toHTML($model, "knjvexpForm1.html", $arg);
    }
}
?>
