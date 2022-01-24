<?php

require_once('for_php7.php');

class knjz237Form1 {
    function main(&$model) {
    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("list", "POST", "knjz237index.php", "", "edit");

    /**************/
    /* 年度を表示 */
    /**************/
    $arg["header"] = CTRL_YEAR;
    $db = Query::dbCheckOut();

    /**************************/
    /* コピーボタンを作成する */
    /**************************/
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_copy",
                        "value"       => "前年度からコピー",
                        "extrahtml"   => "style=\"width:130px\" onclick=\"return btn_submit('copy');\"" ) );
    $arg["btn_copy"] = $objForm->ge("btn_copy");

    /**************************/
    /* 学期コンボボックス作成 */
    /**************************/
    $opt   = array();
    $result = $db->query(knjz237Query::getSemester($model));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "SEMESTER",
                        "value"     => $model->semester,
                        "options"   => $opt,
                        "extrahtml" => "onChange=\"btn_submit('list_gakki');\""
                        ));
    $arg["SEMESTER"] = $objForm->ge("SEMESTER");

    /****************************/
    /* テストコンボボックス作成 */
    /****************************/
    $opt   = array();
    $opt[] = array("label" => "","value" => "");
    $flg99 = false;
    $query = knjz237Query::getTest($model->testTable, $model->semester);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        if ($model->semester == '9' && $model->testTable == "TESTITEM_MST_COUNTFLG") {
            if (preg_match("/^99/", $row["VALUE"])) {
                $flg99 = true;
                $opt[] = array("label" => $row["LABEL"],
                               "value" => $row["VALUE"]);
            }
        } else {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if (preg_match("/^99/", $row["VALUE"]) && $model->testTable == "TESTITEM_MST_COUNTFLG") {
                $flg99 = true;
            }
        }
    }
    $result->free();
    if (!$flg99 && $model->testTable == "TESTITEM_MST_COUNTFLG") {
        $opt[] = array("label" => "9900 評価成績",
                       "value" => "99:00");
    }
    $objForm->ae( array("type"      => "select",
                        "name"      => "TEST",
                        "value"     => $model->field["TEST"],
                        "options"   => $opt,
                        "extrahtml" => "onChange=\"btn_submit('list');\""
                        ));
    $arg["TEST"] = $objForm->ge("TEST");

    /****************/
    /* リスト内表示 */
    /****************/
    $query  = knjz237Query::getListdata($model);
    $result = $db->query($query);

    while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //権限チェック
        if($model->sec_competence == DEF_NOAUTH || $model->sec_competence == DEF_REFERABLE || $model->sec_competence == DEF_REFER_RESTRICT){
            break;
        }

         //レコードを連想配列のまま配列$arg[data]に追加していく。
         array_walk($row, "htmlspecialchars_array");
         //リンク作成
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
             $row["SUBCLASSNAME"] = View::alink("knjz237index.php", $row["SUBCLASSNAME"], "target=\"right_frame\"",
                                              array("cmd"           =>"edit",
                                                    "SEMESTER"      =>$row["SEMESTER"],
                                                    "TEST"          =>$row["TESTKINDCD"] . ':' .$row["TESTITEMCD"],
                                                    "CLASSCD"       =>$row["CLASSCD"],
                                                    "SCHOOL_KIND"   =>$row["SCHOOL_KIND"],
                                                    "CURRICULUM_CD" =>$row["CURRICULUM_CD"],
                                                    "SUBCLASSCD"    =>$row["SUBCLASSCD"],
                                                    "DIV"           =>$row["DIV"],
                                                    "GRADE"         =>$row["GRADE"],
                                                    "COURSECD"      =>$row["COURSECD"],
                                                    "MAJORCD"       =>$row["COURSECD"] . ':' . $row["MAJORCD"],
                                                    "COURSECODE"    =>$row["COURSECODE"],
                                                    "PERFECT"       =>$row["PERFECT"],
                                                    "PASS_SCORE"    =>$row["PASS_SCORE"]
                                                    ));
        } else {
            $row["SUBCLASSNAME"] = View::alink("knjz237index.php", $row["SUBCLASSNAME"], "target=\"right_frame\"",
                                              array("cmd"        =>"edit",
                                                    "SEMESTER"   =>$row["SEMESTER"],
                                                    "TEST"       =>$row["TESTKINDCD"] . ':' .$row["TESTITEMCD"],
                                                    "SUBCLASSCD" =>$row["SUBCLASSCD"],
                                                    "DIV"        =>$row["DIV"],
                                                    "GRADE"      =>$row["GRADE"],
                                                    "COURSECD"   =>$row["COURSECD"],
                                                    "MAJORCD"    =>$row["COURSECD"] . ':' . $row["MAJORCD"],
                                                    "COURSECODE" =>$row["COURSECODE"],
                                                    "PERFECT"    =>$row["PERFECT"],
                                                    "PASS_SCORE" =>$row["PASS_SCORE"]
                                                    ));
        }
         if ($row["GRADE"] == "00") {
            $row["GRADE"] = "";
         }
         if ($row["COURSECD"] == "0") {
            $row["COURSECD"] = "";
            $row["MAJORCD"] = "";
            $row["COURSECODE"] = "";
         } else {
            $row["COURSECD_MAJORCD"] = "({$row["COURSECD"]}{$row["MAJORCD"]})";
            $row["COURSECODE"] = "({$row["COURSECODE"]})";
         }

         $arg["data"][] = $row;
    }
    $result->free();

    /********************/
    /* hiddenを作成する */
    /********************/
    $objForm->ae( array("type"  => "hidden",
                        "name"  => "cmd"
                        ) );

    //権限
    if($model->sec_competence == DEF_NOAUTH || $model->sec_competence == DEF_REFERABLE || $model->sec_competence == DEF_REFER_RESTRICT){
        $arg["Closing"]  = " closing_window(); " ;
    }
    if ($model->cmd == "change" || VARS::post("cmd") == "list"){
        $arg["reload"] = "window.open('knjz237index.php?cmd=edit&SEMESTER={$model->semester}&TEST={$model->test}','right_frame');";
    }

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjz237Form1.html", $arg);
    }
}    
?>
