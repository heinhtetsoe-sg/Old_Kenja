<?php

require_once('for_php7.php');

class knja090_2Form1
{
    function main(&$model)
    {


    //権限チェック
    if (AUTHORITY != DEF_UPDATABLE){
        $arg["jscript"] = "OnAuthError();";
    }

    $objForm = new form;
    
    //フォーム作成
    $arg["start"]   = $objForm->get_start("sel", "POST", "index.php", "", "sel");
    $db             = Query::dbCheckOut();

     //新年度、学期の算出
    if (CTRL_SEMESTER == $model->control["学期数"]) { //最終学期のとき
        $model->control["new_year"] = ((int)CTRL_YEAR+1);
        $model->control["new_semes"] = 1;
    } else {
        $model->control["new_year"] = CTRL_YEAR;
        $model->control["new_semes"] = ((int)CTRL_SEMESTER+1);
    }

    //学期名取得
    $model->control["new_semes_name"] = $db->getOne(knja090_2Query::Getsemes_name($model->control["new_year"], $model->control["new_semes"]));

    $opt_left = $opt_right = array();
    

    $result = $db->query(knja090_2Query::GetStudent($model));
    $array = explode(",", $model->selectdata);
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
    $row = $db->getRow(knja090_2Query::GetStudent_Course($model),DB_FETCHMODE_ASSOC);
    Query::dbCheckIn($db);

    
    //コース1
    $objForm->ae( array("type"        => "select",
                        "name"        => "course1",
                        "size"        => "1",
                        "value"       => $row["COURSECODE"],
                        "extrahtml"   => "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                        "options"     => knja090_2Query::GetCourse($model->control["new_year"]))); 

    $arg["data"]["COURSE1"] = $objForm->ge("course1");

/*    //コース2
    $objForm->ae( array("type"        => "select",
                        "name"        => "course2",
                        "size"        => "1",
                        "value"       => $row["COURSECODE2"],
                        "extrahtml"   => "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                        "options"     => knja090_2Query::GetCourse("B112",$model->year))); 

    $arg["data"]["COURSE2"] = $objForm->ge("course2");

    //コース3
    $objForm->ae( array("type"        => "select",
                        "name"        => "course3",
                        "size"        => "1",
                        "value"       => $row["COURSECODE3"],
                        "extrahtml"   => "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                        "options"     => knja090_2Query::GetCourse("B113",$model->year))); 

    $arg["data"]["COURSE3"] = $objForm->ge("course3");
*/

    $objForm->ae( array("type"           => "button",
                        "name"        => "btn_update",
                        "value"       => "置 換",
                        "extrahtml"   => "onclick=\"return doSubmit()\"" ) );
    //戻るボタン
    $link = REQUESTROOT."/A/KNJA090/knja090index.php?cmd=list&grade_class=".$model->grade."-".sprintf("%02d",$model->hr_class);
    $objForm->ae( array("type"           => "button",
                        "name"        => "btn_back",
                        "value"       => "戻 る",
                        "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );
                    
    $arg["data"]["BUTTONS"] = $objForm->ge("btn_update")."　　".$objForm->ge("btn_back");

    //対象生徒
    $objForm->ae( array("type"        => "select",
                        "name"        => "classyear",
                        "size"        => "20",
                        "value"       => "left",
                        "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"",
                        "options"     => $opt_left)); 
                    
    //その他の生徒
    $objForm->ae( array("type"        => "select",
                        "name"        => "classmaster",
                        "size"        => "20",
                        "value"       => "left",
                        "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"",
                        "options"     => $opt_right));  
                    
    //追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_add_all",
                        "value"       => "≪",
                        "extrahtml"   => "onclick=\"return move('sel_add_all');\"" ) );

    //追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_add",
                        "value"       => "＜",
                        "extrahtml"   => "onclick=\"return move('left');\"" ) );

    //削除ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_del",
                        "value"       => "＞",
                        "extrahtml"   => "onclick=\"return move('right');\"" ) );

    //削除ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_del_all",
                        "value"       => "≫",
                        "extrahtml"   => "onclick=\"return move('sel_del_all');\"" ) ); 
                                        
    $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("classyear"),
                               "RIGHT_PART"  => $objForm->ge("classmaster"),
                               "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                               "SEL_ADD"     => $objForm->ge("sel_add"),
                               "SEL_DEL"     => $objForm->ge("sel_del"),
                               "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    
    
    $arg["year"]["VAL"]    = "";                                  

    $arg["info"]    = array("TOP"        => $model->control["new_year"] ."年度　".$model->control["new_semes_name"]."　対象クラス　".$model->grade."年".(integer)$model->hr_class."組",
                            "LEFT_LIST"  => "対象者一覧",
                            "RIGHT_LIST" => "生徒一覧");
/*
    $arg["info"]    = array("TOP"        => $model->year ."年度　".$model->semester_name."　対象クラス　".$model->grade."年".(integer)$model->hr_class."組",
                            "LEFT_LIST"  => "対象者一覧",
                            "RIGHT_LIST" => "生徒一覧");
*/

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );  

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "selectdata"
                        ) );  

    $arg["finish"]  = $objForm->get_finish();

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knja090_2Form1.html", $arg); 
    }
}
?>

