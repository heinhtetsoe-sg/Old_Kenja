<?php

require_once('for_php7.php');

class knjxtoke4form2
{
    var $tree = array();

    function knjxgtreForm1()
    {
        $this->tree = array();
    }
    function makeTree($lev, $text)
    {
        $this->tree[] = array("level" => $lev,
                              "text"  => $text
                    );
    }
    function init()
    {
        $html = "";
        for ($i = 0; $i < get_count($this->tree); $i++){
            $lev  = $this->tree[$i]["level"];
            $text = $this->tree[$i]["text"];
            if ($lev > $pre_lev){
                if ($lev == 3){
                    $html .= "<ul id=\"foldinglist\" style=\"display:none\">\n";
                }else{
                    $html .= "<ul id=\"foldinglist\">\n";
                }
            }else if ($lev < $pre_lev){
                $html .= str_repeat("</ul>\n", $pre_lev-$lev);
            }else if ($lev == $pre_lev){
            }
            if (isset($this->tree[$i+1]) && $this->tree[$i+1]["level"] > $lev){
                if ($lev == 1){
                    $html .= "<li id=\"foldheader\" style=\"list-style-image:url('" .REQUESTROOT ."/image/system/nav_open1.gif')\">" .$text ."\n";
                }else{
                    $html .= "<li id=\"foldheader\">" .$text ."\n";
                }
            }else{
                $html .= "<li>" .$text ."\n";
            }
            $pre_lev = $lev;
        }
        $html .= str_repeat("</ul>\n", $pre_lev);
        return $html;
    }

    function main(&$model){

        function add_quote($str){
            return "'" .$str ."'";
        }

        $objForm = new form;
        $arg["target"] = $model->target;
        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjxtoke4form2", "POST", "knjxtoke4index.php", "", "knjxtoke4");
        
        $db = Query::dbCheckOut();
        
        if (is_numeric($model->year) && is_numeric($model->semester)){
            $model->semi_start = $model->control["学期開始日付"][$model->semester];     //学期開始日付
            $model->semi_end   = $model->control["学期終了日付"][$model->semester];     //学期終了日付
        
            //教科、科目、クラス取得
//2004/04/01 $query = knjxtoke4Query::SQLGet_Test($model);
            $query = knjxtoke4Query::selectQuerySubclass($model);	//2004/04/01 処理時間の改善のためのＳＱＬ修正版
            
            $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $title = "[" . $row["CLASSCD"]."　". htmlspecialchars($row["CLASSNAME"]) ."]-[" .$row["SUBCLASSCD"]."　".htmlspecialchars($row["SUBCLASSNAME"]) ."]";

            $cd1 = $row["CLASSCD"];
            $cd2 = $row["CLASSCD"] .$row["SUBCLASSCD"];
            $cd3 = $row["CLASSCD"] .$row["SUBCLASSCD"] .$row["TESTITEMCD"] .$row["TESTKINDCD"];
            //教科
            if ($pre_cd1 != $cd1){
                $this->makeTree(1, $row["CLASSCD"]."　".htmlspecialchars($row["CLASSNAME"]));
            }
            //科目            
            if ($pre_cd2 != $cd2){
                if ($model->disp == "TEST" ){
                    $text = $row["SUBCLASSCD"]."　".htmlspecialchars($row["SUBCLASSNAME"]);
                }else{
                    $text  = $row["SUBCLASSCD"]."　";
                    $text .= View::alink("knjxtoke4index.php",
                                        htmlspecialchars($row["SUBCLASSNAME"]),
                                        "target=right_frame",
                                        array("cmd"             => "main",
                                              "YEAR"            => $model->year,
                                              "SEMESTER"        => $model->semester,
                                              "CLASSCD"         => $row["CLASSCD"],
                                              "SUBCLASSCD"      => $row["SUBCLASSCD"],
                                              "TITLE"           => $title
                                       ));

                }
                $this->makeTree(2, $text);
            }
            //テスト項目
            if ($model->disp == "TEST" && $pre_cd3 != $cd3 && $row["TESTITEMNAME"] != "" ){
                $title .= sprintf("-[%02d%02d　%s]",(int) $row["TESTKINDCD"], (int) $row["TESTITEMCD"], htmlspecialchars($row["TESTITEMNAME"]));

                $text = View::alink("knjxtoke4index.php",
                                    htmlspecialchars($row["TESTITEMNAME"]),
                                    "target=right_frame",
                                    array("cmd"             => "main",
                                          "YEAR"            => $model->year,
                                          "SEMESTER"        => $model->semester,
                                          "CLASSCD"         => $row["CLASSCD"],
                                          "SUBCLASSCD"      => $row["SUBCLASSCD"],
                                          "TESTITEMCD"      => $row["TESTITEMCD"],
                                          "TESTKINDCD"      => $row["TESTKINDCD"],
                                          "PROGRAMID"       => $model->programid,
                                          "TITLE"           => $title
                                   ));
                $this->makeTree(3, $text);
            }
            $pre_cd1 = $cd1;
            $pre_cd2 = $cd2;
            $pre_cd3 = $cd3;
        }
        //ツリー作成
        $arg["TREE"] = $this->init();
        $arg["REQUESTROOT"] = REQUESTROOT;
        }
        //$year = common::DateConv1($model->control["学籍処理日"], 12);
        $year = $model->control["年度"];
//        if ($model->auth == DEF_UPDATABLE){     //更新可
            $opt = array();
            if (is_numeric($model->control["学期数"])){
                //年度,学期コンボの設定
                for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ ){
                    $opt[]= array("label" => $year."年度&nbsp;" .$model->control["学期名"][$i+1],
                                  "value" => sprintf("%d,%d", $year, $i+1)
                                 );
                }
            }
            $objForm->ae( array("type"       => "select",
                                "name"       => "SEL_SEMI",
                                "size"       => "1",
                                "value"      => $model->year ."," .$model->semester,
                                "extrahtml"  => "onChange=\"return btn_submit('read');\"",
                                "options"    => $opt));
        
            $arg["semester"] = $objForm->ge("SEL_SEMI");
//        }else{
//            $arg["semester"] = sprintf("%d年度&nbsp;%s", $year, $model->control["学期名"][$model->semester]);
//        }
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
        View::toHTML($model, "knjxtoke4Form2.html", $arg);		//2004/03/24 nakamoto add
//2004/03/24 nakamoto del	View::t_include("knjxtoke4Form2.html", $arg);
    }
}
?>