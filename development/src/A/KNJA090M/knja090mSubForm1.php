<?php

require_once('for_php7.php');

class knja090mSubForm1
{
    function main(&$model)
    {
        $db             = Query::dbCheckOut();

        if ($model->year_seme == "") {
            $model->year_seme = $model->control["new_year"]."-".$model->control["new_semes"];
        }
        $tmp = explode("-",$model->year_seme);
        $model->control["new_year"] = $tmp[0];
        $model->control["new_semes"] = $tmp[1];

        $gakka_query = knja090mQuery::GetMajorCoursecd($model->control["new_year"],$db);
        $course1 = knja090mQuery::GetCourse($model->control["new_year"],$db);
        
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        //コースコードが設定されていない場合はメッセージを表示して画面を戻す
        } elseif ($course1[0]=="") {
            $arg["jscript"] = "ShowMessage()";
        //課程学科コードが設定されていない場合はメッセージを表示して画面を戻す
        } elseif ($gakka_query[0]=="") {
            $arg["jscript"] = "ShowMessage2()";
        }

        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knja090mindex.php", "", "sel");

        $opt_left = $opt_right = array();

        $result = $db->query(knja090mQuery::GetStudent($model));
        $array = explode(",", $model->selectdata);
        //リストが空であれば置換処理選択時の生徒を加える
        if ($array[0]=="") $array[0] = $model->schregno;

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if (!in_array($row["SCHREGNO"], $array)){
                $opt_right[] = array("label" => $row["SCHREGNO"]."　".$row["ATTENDNO"]."　".$row["NAME"], 
                                     "value" => $row["SCHREGNO"]);
            } else {
                $opt_left[] = array("label" => $row["SCHREGNO"]."　".$row["ATTENDNO"]."　".$row["NAME"], 
                                     "value" => $row["SCHREGNO"]);
            }
        }
        $result->free();
        
        //課程学科
        $arg["data"]["COURSEMAJORCD1"] = knjCreateCombo($objForm, "COURSEMAJORCD1", $model->coursemajorcd1, $gakka_query, $extra, 1);
        
        //コース1
        $objForm->ae( array("type"        => "select",
                            "name"        => "course1",
                            "size"        => "1",
                            "value"       => $model->course1,
                            "extrahtml"   => "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                            "options"     => $course1)); 

        $arg["data"]["COURSE1"] = $objForm->ge("course1");
        
        //年組名
        $class_name = $db->getOne(knja090mQuery::getHR_Name($model));

        Query::dbCheckIn($db);
        $objForm->ae( array("type"           => "button",
                            "name"        => "btn_update",
                            "value"       => "置 換",
                            "extrahtml"   => "onclick=\"return doSubmit()\"" ) );
        //戻るボタン
        $link = REQUESTROOT."/A/KNJA090M/knja090mindex.php?cmd=list";
        $objForm->ae( array("type"           => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"parent.location.href='$link&year_seme=$model->year_seme';\"" ) );
                    
        $arg["data"]["BUTTONS"] = $objForm->ge("btn_update")."　　".$objForm->ge("btn_back");

        //対象生徒
        $objForm->ae( array("type"        => "select",
                            "name"        => "left_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"",
                            "options"     => $opt_left)); 
                    
        //その他の生徒
        $objForm->ae( array("type"        => "select",
                            "name"        => "right_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"",
                            "options"     => $opt_right));  
                    
                    
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','left_select','right_select',1);\"" ) ); 
                                        
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("left_select"),
                                   "RIGHT_PART"  => $objForm->ge("right_select"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    

        $arg["year"]["VAL"]    = "";
        $class_arr = explode("-",$model->grade_class);
        $arg["info"]    = array("TOP"        => $model->control["new_year"]
                                                ."年度　".$model->control["学期名"][$model->control["new_semes"]]
                                                ."　対象クラス　".$class_name,
                                "LEFT_LIST"  => "対象者一覧",
                                "RIGHT_LIST" => "生徒一覧");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year_seme",
                            "value"     => $model->year_seme) );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja090mSubForm1.html", $arg); 
    }
}
?>

