<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjxgtreForm1
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
        $pre_lev = 0;
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

    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "index.php", "", "main");

        $db = Query::dbCheckOut();

        //科目ツリー用SQL作成
        $query = knjxgtreQuery::selectQuerySubclass($model);
        $result = $db->query($query);

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $title = "[" . $row["CLASSCD"]."　". htmlspecialchars($row["CLASSNAME"]) ."]-[" .$row["SUBCLASSCD"]."　".htmlspecialchars($row["SUBCLASSNAME"]) ."]";

            $cd1 = $row["CLASSCD"];
            $cd2 = $row["CLASSCD"] .$row["SUBCLASSCD"];
            $cd3 = $row["CLASSCD"] .$row["SUBCLASSCD"] .$row["TESTKINDCD"].$row["TESTITEMCD"] ;
            //教科
            if ($pre_cd1 != $cd1){
                $this->makeTree(1, $row["CLASSCD"]."　".htmlspecialchars($row["CLASSNAME"]));
            }
            //科目            
            if ($pre_cd2 != $cd2){
                if ($model->disp == "TEST" ){
                    $text = $row["SUBCLASSCD"]."　".htmlspecialchars($row["SUBCLASSNAME"]);
                }else{
                    $text = $row["SUBCLASSCD"]."　";
                    $text .= View::alink("index.php",
                                        htmlspecialchars($row["SUBCLASSNAME"]),
                                        "target=\"right_frame\"",
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

                $text = View::alink("index.php",
                                    htmlspecialchars($row["TESTITEMNAME"]),
                                    "target=\"right_frame\"",
                                    array("cmd"             => "main",
                                          "YEAR"            => $model->year,
                                          "SEMESTER"        => $model->semester,
                                          "CLASSCD"         => $row["CLASSCD"],
                                          "SUBCLASSCD"      => $row["SUBCLASSCD"],
                                          "TESTKINDCD"      => $row["TESTKINDCD"],
                                          "TESTITEMCD"      => $row["TESTITEMCD"],
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
        $opt = array();
        if ($model->auth == DEF_UPDATABLE || $model->auth == DEF_REFERABLE){     //更新可
            //学期を取得
            $query = knjxgtreQuery::selectQuerySemester();
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt[] = array("label" => $row["YEAR"] .'年度　' .$row["SEMESTERNAME"],
                               "value" => $row["SEMESTER"]
                               );
            }
        }else{
            $opt[]= array("label" => sprintf("%d年度&nbsp;%s", CTRL_YEAR, CTRL_SEMESTERNAME),
                          "value" => CTRL_SEMESTER
                         );
        }
        Query::dbCheckIn($db);
        $objForm->ae( array("type"       => "select",
                            "name"       => "SEMESTER",
                            "size"       => "1",
                            "value"      => $model->semester,
                            "extrahtml"  => "onChange=\"return btn_submit('tree');\"",
                            "options"    => $opt));

        $arg["SEMESTER"] = $objForm->ge("SEMESTER");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjxgtreForm1.html", $arg);
    }
}
?>