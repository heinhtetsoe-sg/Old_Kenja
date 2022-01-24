<?php

require_once('for_php7.php');

class knjxtoke2form2
{
    function main(&$model){
        function add_quote($str){
            return "'" .$str ."'";
        }
        $objForm = new form;        
        $arg["target"] = $model->target;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjxtoke2form1", "POST", "knjxtoke2index.php", "", "knjxtoke2");
        
        $db = Query::dbCheckOut();
        
        if (is_numeric($model->year) && is_numeric($model->semester)){
            $model->semi_start = $model->control["学期開始日付"][$model->semester];     //学期開始日付
            $model->semi_end   = $model->control["学期終了日付"][$model->semester];     //学期終了日付
        
            //教科、科目、クラス取得
            $query = knjxtoke2Query::SQLGet_Main($model);
            @$result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $level1[add_quote($row["CLASSCD"])] = $row["CLASSCD"]."　".htmlspecialchars($row["CLASSNAME"]);
                //学期末成績処理
                    $g_name = "";
                    if ($row["GRADINGCD"] == 1){         //合併の場合
                        $g_name = "--->" .htmlspecialchars($row["GROUPCLASSNAME"]) ."（合併）";
                    }elseif ($row["GRADINGCD"] == 2){    //分割の場合
                        $g_name = "<---" .htmlspecialchars($row["GROUPCLASSNAME"]) ."（分割）";
                    }
                    
		            $title = "[" . $row["CLASSCD"]."　". htmlspecialchars($row["CLASSNAME"]) ."]-[" .$row["SUBCLASSCD"]."　".htmlspecialchars($row["SUBCLASSNAME"]) ."]";
                    
        
                    $row["SUBCLASSNAME"] = View::alink("knjxtoke2index.php",
                                                        htmlspecialchars($row["SUBCLASSNAME"] .$g_name),
                                                        "target=right_frame",
                                                        array("cmd"             => "main",
                                                              "YEAR"            => $model->year,
                                                              "SEMESTER"        => $model->semester,
                                                              "CLASSCD"         => $row["CLASSCD"],
                                                              "SUBCLASSCD"      => $row["SUBCLASSCD"],
                                                              "GROUPCLASSCD"    => $row["GROUPCLASSCD"],
                                                              "SCHECLASSCD"     => $row["SCHECLASSCD"],
                                                              "GRADINGCD"       => $row["GRADINGCD"],
                                                              "TRUNCATECD"      => $row["TRUNCATECD"],
                                                              "TITLE"           => $title
                                                       ));
                $level2[add_quote($row["CLASSCD"])][add_quote($row["SUBCLASSCD"])] = $row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"];
            }
            Query::dbCheckIn($db);
            if (is_array($level1)){
                //ツリー表示クラス
                $t = new TreeView();
                //得点入力以外
                    $t->tree = array_merge_recursive((array)$level1, (array)$level2);
                $t->go_through_tree();
                $arg["tree"] = $t->outp;
            }
        $arg["REQUESTROOT"] = REQUESTROOT;
        }
        //$year = common::DateConv1($model->control["学籍処理日"], 12);
        $year = $model->control["年度"];
        $opt = array();
        if (is_numeric($model->control["学期数"])){
            //年度,学期コンボの設定
            for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ ){
                $opt[]= array("label" => $year."年度&nbsp;" .$model->control["学期名"][$i+1], 
                              "value" => sprintf("%d,%d", $year, $i+1)
                             );            
            }
        }
        
        if($model->programid == "LBA081") {
        $objForm->ae( array("type"       => "select",
                            "name"       => "SEL_SEMI",
                            "size"       => "1",
                            "extrahtml"  => "onChange=\"return btn_submit('read');\"",
                            "value"      => $model->year ."," .$model->semester,
                            "options"    => $opt));
        
        $arg["semester"] = $objForm->ge("SEL_SEMI");
        
        //ボタンを作成する
        //$objForm->ae( array("type" => "button",
        //                    "name"        => "btn_read",
        //                    "value"       => "読 込",
        //                    "extrahtml"   => "onClick=\"return btn_submit('read')\"" ));
        
        //$arg["btn_read"] = $objForm->ge("btn_read");
        }
        else {
        $objForm->ae( array("type"       => "select",
                            "name"       => "SEL_SEMI",
                            "size"       => "1",
                            "extrahtml"  => "onChange=\"return btn_submit('read');\"",
                            "value"      => $model->year ."," .$model->semester,
                            "options"    => $opt));
        
        $arg["semester"] = $objForm->ge("SEL_SEMI");
        
        //ボタンを作成する
        //$objForm->ae( array("type" => "button",
        //                    "name"        => "btn_read",
        //                    "value"       => "読 込",
        //                    "extrahtml"   => "onClick=\"return btn_submit('read')\"" ));
        
        //$arg["btn_read"] = $objForm->ge("btn_read");
        }
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd
                            ) );
        
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PROGRAMID",
                            "value"     => $model->programid
                            ) );
        
        //ターゲット名設定
        $arg["target"] = "right_frame";
        $arg["finish"]  = $objForm->get_finish();
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjxtoke2Form2.html", $arg);		//2004/03/24 nakamoto add
//2004/03/24 nakamoto del	View::t_include("knjxtoke2Form2.html", $arg);
    }
}
?>