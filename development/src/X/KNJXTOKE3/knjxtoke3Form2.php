<?php

require_once('for_php7.php');

class knjxtoke3form2
{
    function main(&$model){
        function add_quote($str){
            return "'" .$str ."'";
        }
        $objForm = new form;        
        $arg["target"] = $model->target;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjxtoke3form2", "POST", "knjxtoke3index.php", "", "knjxtoke3");
        
        $db = Query::dbCheckOut();
        
        if (is_numeric($model->year) && is_numeric($model->semester)){
            $model->semi_start = $model->control["学期開始日付"][$model->semester];     //学期開始日付
            $model->semi_end   = $model->control["学期終了日付"][$model->semester];     //学期終了日付

            //教科、科目、クラス取得
            $query = knjxtoke3Query::SQLGet_Main($model);
            @$result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $level1[add_quote($row["CLASSCD"])] = htmlspecialchars($row["CLASSNAME"]);
                //学期末成績処理
                if ($model->disp != "TEST"){
                    $title = "[" . $row["CLASSCD"]."　".htmlspecialchars($row["CLASSNAME"]) ."]";
                    
                    $level1[add_quote($row["CLASSCD"])]  = htmlspecialchars($row["CLASSCD"])."　";
                    $level1[add_quote($row["CLASSCD"])] .= View::alink("knjxtoke3index.php",
                                                        htmlspecialchars($row["CLASSNAME"]),
                                                        "target=right_frame",
                                                        array("cmd"             => "main",
                                                              "YEAR"            => $model->year,
                                                              "SEMESTER"        => $model->semester,
                                                              "CLASSCD"         => $row["CLASSCD"],
                                                              "SUBCLASSCD"      => $model->subclasscd,
                                                              "TITLE"           => $title
                                                       ));
                }else{
                    $row["SUBCLASSNAME"] = htmlspecialchars($row["SUBCLASSNAME"]);
                }
                //$level2[add_quote($row["CLASSCD"])][add_quote($row["SUBCLASSCD"])] = $row["SUBCLASSCD"]."　".$row["SUBCLASSNAME"];
            }
            //得点入力
            if ($model->disp == "TEST"){
                //教科、科目、クラス取得
                $query = lztoke3Query::SQLGet_Test($model);
                @$result = $db->query($query);
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            
                    $classcd            = add_quote($row["CLASSCD"]);
                    $subclasscd         = add_quote($row["SUBCLASSCD"]);
        
                    $title = "[" .$level1[$classcd] ."]-[" .$level2[$classcd][$subclasscd]."]-[" .$row["TESTITEMNAME"] ."]";
        
                    $row["TESTITEMNAME"] = View::alink("index.php",
                                                        htmlspecialchars($row["TESTITEMNAME"]),
                                                        "target=right_frame",
                                                        array("cmd"             => "main",
                                                              "YEAR"            => $model->year,
                                                              "SEMESTER"        => $model->semester,
                                                              "TESTKINDCD"      => $row["TESTKINDCD"],
                                                              "TESTITEMCD"      => $row["TESTITEMCD"],
                                                              "CLASSCD"         => $row["CLASSCD"],
                                                              "SUBCLASSCD"      => $row["SUBCLASSCD"],
                                                              "TITLE"           => $title
                                                       ));
        
                    $level3[$classcd][$subclasscd][] = $row["TESTITEMNAME"];
        
                }
            }
            Query::dbCheckIn($db);
            if (is_array($level1)){
                //ツリー表示クラス
                $t = new TreeView();
                //得点入力以外
                if ($model->disp != "TEST"){
                    $t->tree = array_merge_recursive((array)$level1, (array)$level2);
                }else{
                    $t->tree = array_merge_recursive((array)$level1, (array)$level2, (array)$level3);
                }
                $t->go_through_tree();
                $arg["tree"] = $t->outp;
            }
        $arg["REQUESTROOT"] = REQUESTROOT;
        }
        //$year = common::DateConv1($model->control["学籍処理日"], 12);
        $year = $model->control["年度"];
        $opt = array();
//        if ($model->auth == DEF_UPDATABLE){     //更新可
            if (is_numeric($model->control["学期数"])){
                //年度,学期コンボの設定
                for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ ){
                    $opt[]= array("label" => $year."年度&nbsp;" .$model->control["学期名"][$i+1],
                                  "value" => sprintf("%d,%d", $year, $i+1)
                                 );
                }
            }
//        }else{
//            $opt[]= array("label" => sprintf("%d年度&nbsp;%s", $year, $model->control["学期名"][$model->semester]),
//                          "value" => sprintf("%d,%d", $year, $model->semester)
//                         );
//        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "SEL_SEMI",
                            "size"       => "1",
                            "value"      => $model->year ."," .$model->semester,
                            "extrahtml"  => "onChange=\"return btn_submit('read');\"",
                            "options"    => $opt));
        
        $arg["semester"] = $objForm->ge("SEL_SEMI");
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
        View::toHTML($model, "knjxtoke3Form2.html", $arg);		//2004/03/24 nakamoto add
//2004/03/24 nakamoto del	View::t_include("knjxtoke3Form2.html", $arg);
    }
}
?>