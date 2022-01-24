<?php

require_once('for_php7.php');

class knjd104bForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjd104bindex.php", "", "edit");
        $arg["YEAR"] = CTRL_YEAR;
        $arg["PROGRAMID"] =PROGRAMID;

       //デフォルト値
        if($model->semester ==""){
            $model->semester = CTRL_SEMESTER;
        }
       //デフォルト値
        if($model->coursecode ==""){
            $row = knjd104bQuery::getFirst_CouseKey($model->semester);
            $model->coursecode = $row["COURSECODE"];
            $model->coursecd   = $row["COURSECD"];
            $model->majorcd    = $row["MAJORCD"];
            $model->grade      = $row["GRADE"];
            $model->coursename="";
        }

       //デフォルト値
        if($model->testkindcd ==""){
            $row = knjd104bQuery::getFirst_TestKey($model->semester);
            $model->testkindcd    = $row["TESTKINDCD"];
            $model->testitemcd    = $row["TESTITEMCD"];
            $model->testname="";
        }


        //学期コンボ設定
        $db        = Query::dbCheckOut();
        $opt_seme  = array();
        $result    = $db->query(knjd104bQuery::getSemester());   
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_seme[] = array("label" => $row["SEMESTERNAME"], 
                                "value" => $row["SEMESTER"]);

        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"      => "select",
                            "name"      => "SEMESTER",
                            "size"      => "1",
                            "value"     => $model->semester,
                            "extrahtml" => "onchange=\"btn_submit('coursename');\"",
                            "options"   => $opt_seme ));

        $arg["SEMESTER"] = $objForm->ge("SEMESTER");


        //テストコンボ設定
        $db        = Query::dbCheckOut();
        $opt_test  = array();
        $result    = $db->query(knjd104bQuery::getTestName($model->semester));   
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_test[] = array("label" => $row["TESTKINDCD"].$row["TESTITEMCD"]." ".$row["TESTITEMNAME"], 
                                "value" => $row["TESTKINDCD"]." ".$row["TESTITEMCD"]);

        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTNAME",
                            "size"        => "1",
                            "value"       => $model->testname,
                            "extrahtml"   => "onchange=\"btn_submit('coursename');\"",
                            "options"     => $opt_test ));

        $arg["TESTNAME"] = $objForm->ge("TESTNAME");

        //コースコンボ作成
        $db = Query::dbCheckOut();
        $opt = array();
        $result = $db->query(knjd104bQuery::getCouseName($model->semester)); 
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $course_majaorne = $row["COURSENAME"].$row["MAJORNAME"];
            if(mbereg("[｡-ﾟ]",$course_majaorne)){
                $ct2 = (integer)(substr_count(mbereg_replace("[｡-ﾟ]","0",$course_majaorne),"0"));
                $space_count = 29-(((integer)((strlen($course_majaorne))/3)*2)+((integer)strlen($course_majaorne))%3)+$ct2;
            }else{
                $ct = (integer)(substr_count(mbereg_replace("[ｱ-ﾝ0-9A-Za-z -~]","0",$course_majaorne),"0")/3);
                $space_count = 29-(((integer)((strlen($course_majaorne))/3)*2)+((integer)strlen($course_majaorne))%3)-$ct;
            }

            if ($space_count < 0) {
                $space_count = 0;
            }

            $name = ($row["COURSENAME"].
                     $row["MAJORNAME"].
                     str_repeat("&nbsp;",$space_count));

            $opt[] = array("label" => ltrim($row["GRADE"],'0')."学年&nbsp;".
                                      "(".$row["COURSECD"].$row["MAJORCD"].")&nbsp;".
                                      $name."&nbsp;".
                                      "(".$row["COURSECODE"].")&nbsp;"
                                      .$row["COURSECODENAME"], 
                           "value" => $row["COURSECODE"]." ".$row["COURSECD"]." ".$row["MAJORCD"]." ".$row["GRADE"]);
        }


        //コース名
        $objForm->ae( array("type"      => "select",
                           "name"       => "COURSENAME",
                           "size"       => "1",
                           "value"      => $model->coursename,
                           "extrahtml"  => "onchange=\"btn_submit('coursename');\"",
                           "options"    => $opt
                           ));

        $arg["COURSENAME"] = $objForm->ge("COURSENAME");


        //コース一覧取得
        //教育課程用
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
        } else {
            $arg["NoCurriculumcd"] = "1";
        }
        $result = $db->query(knjd104bQuery::getList($model)); 
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["FOOTNOTE"] = str_replace("\r\n", "<BR>", $row["FOOTNOTE"]);
            $arg["data"][] = $row;
        }
        $result->free();
        
        Query::dbCheckIn($db);


        //コピーボタンを作成
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_copy",
                            "value"     => "前年度からコピー",
                            "extrahtml" => " onclick=\"return btn_submit('copy');\"" 
                            ) );
        $arg["btn_copy"] = $objForm->ge("btn_copy");


        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );


        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "coursename"){
            $arg["reload"] = "window.open('knjd104bindex.php?cmd=edit','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd104bForm1.html", $arg);
    }
}
?>
