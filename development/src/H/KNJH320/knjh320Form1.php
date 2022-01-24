<?php

require_once('for_php7.php');

class knjh320form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh320index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();
        
        //グラフ用配列
        $model->bar_graph = array();
        $model->bar_graph_avg = "";     //平均の凡例を入れるためのもの
        $model->radar_graph = array();
        $model->bar = array();
        $model->bar_avg = array();
        $model->radar = array();
        for($i=0;$i<$model->graphno;$i++){
            $model->bar_graph_k{$i} = array();
        }
        $model->graphno = 0;

        //グラフ用
        $model->hancnm = "";
        $model->hanCnt = 0;
        $model->ssum = array();
        $model->dsum = array();
        //subclasscdをSQLでinに使えるように
        $model->subclasscd = "";
        $model->subclasscd_array = array();
        
        
        //学籍基礎マスタより名前を取得
        $nameArray = $db->getRow(knjh320Query::getName($model), DB_FETCHMODE_ASSOC);
        $arg["NAME"] = $model->year."年度　".$nameArray["HR_NAME"]."　".$nameArray["ATTENDNO"]."番　氏名：".$nameArray["NAME"];

        //ボタン表示用ラジオ
        $opt = array(1, 2, 3, 4);
        //$onclick = " onClick=\"btn_submit('change');\"";
        $onclick = "";
        $extra = array("id=\"RADIO1\" {$onclick}", "id=\"RADIO2\" {$onclick}", "id=\"RADIO3\" {$onclick}", "id=\"RADIO4\" {$onclick}");
        $label = array("RADIO1" => "学年平均", "RADIO2" => "クラス平均", "RADIO3" => "コース平均", "RADIO4" => "平均なし");
        $radioArray = knjCreateRadio($objForm, "RADIO", $model->Radio, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>";
        
        if($model->Radio == "1"){
            $model->avg_div = "01";
            $model->grade = "";
            $model->limit = "";
        }else if($model->Radio == "2"){
            $model->avg_div = "02";
            $model->grade = $nameArray["GRADE"];
            $model->limit = $nameArray["HR_CLASS"];
        }else if($model->Radio == "3"){
            $model->avg_div = "03";
            $model->grade = $nameArray["GRADE"];
            $model->limit = $nameArray["COURSECODE"];
        }else{
            $arg["ONE"] = "1";
            $model->avg_div = 9;
            $model->grade = "";
            $model->limit = "";
        }
        if($model->cmd == "radar"){
            $arg["ONE"] = "1";
            //$model->avg_div = 9;
        }

        //オール選択チェックボックス作成
        //makeAllCheck($objForm, $arg, "TEST_ALL_CHECK", "test");
        //明細ヘッダデータ作成
        $subclass = makeHead($objForm, $arg, $db, $model);

        //明細データ作成
        $testAry = makeMeisai($objForm, $arg, $db, $model, $subclass, $nameArray["GRADE"]);

        //リンク作成
        makeLink($arg, $model->lastyear, $model->lastseme, "LINKLAST", $model->schregno);
        makeLink($arg, $model->nextyear, $model->nextseme, "LINKNEXT", $model->schregno);

        //ボタン作成
        makeButton($objForm, $arg, $model);
        
        //グラフ用
        $model->radar_graph["cnt"] = $model->hanCnt;
        
        //グラフ用
        if($model->cmd != "radar"){
            $arg["cssclass"] = "holder";
            $arg["height"] = 300;
            
            $model->bar_graph["YMIN"] = 0;
            $model->bar_graph["tooltip"] = "label";
            if($model->avg_div != 9){
                $model->bar_graph["TITLE"] = $label["RADIO{$model->avg_div}"];
                $model->bar_graph["cnt"] = $model->hanCnt * 2;
                $model->bar_graph["hanrei"] = $model->bar_graph["hanrei"].",".$model->bar_graph_avg;
                $model->bar_graph += array_merge($model->bar,$model->bar_avg);
                
                $hanreiCnt = $model->bar_graph["cnt"] /2;
            }else{
                $model->bar_graph["TITLE"] = $label["RADIO4"];
                $model->bar_graph["cnt"] = $model->hanCnt;

                $model->bar_graph += array_merge($model->bar);
                
                $hanreiCnt = $model->bar_graph["cnt"];
            }
            $model->bar_graph["type"] = "";
            $model->bar_graph["LineType"] = "";
            $tcnm = "";
            for($i=0;$i<$hanreiCnt;$i++){
                $model->bar_graph["type"] .= $tcnm."bar";
                $model->bar_graph["LineType"] .= $tcnm."1";
                $tcnm = ",";
            }
            if($model->avg_div != 9){
                for($i=0;$i<$hanreiCnt;$i++){
                    $model->bar_graph["type"] .= $tcnm."line";
                    $model->bar_graph["LineType"] .= $tcnm."0";
                }
            }
            
            if(!empty($ssum)){
                $max = ceil(max($ssum));
                if($max > 100){
                    if($max%50 != 0){
                        $model->bar_graph["YMAX"] = $max + 50-($max%50);
                    }else{
                        $model->bar_graph["YMAX"] = $max;
                    }
                }else{
                    $model->bar_graph["YMAX"] = 100;
                }
            }else{
                $model->bar_graph["YMAX"] = 100;
            }
            
            $arg["GRAPH_TYPE"] = "bar";
            if($model->Radio == "4"){
                //グラフ用hidden作成
                graph::CreateDataHidden($objForm,$model->bar_graph,0);
            }else{
                $model->bar_graph["hanrei"] = "得点,".$label["RADIO{$model->Radio}"];
                
                //色
                $color1 = array("255/150/0","220/20/60","65/105/225","0/143/35","138/43/226","250/10/250");
                $color2 = array("0/10/115","0/10/115","0/10/115","0/10/115","0/10/115","0/10/115");
                
                for($graph = 0;$graph<$model->hanCnt;$graph++){
                    $graphno["GRAPHNO"] = $graph;
                    $model->bar_graph_k{$graph}["YMAX"] = $model->bar_graph["YMAX"];
                    $model->bar_graph_k{$graph}["YMIN"] = $model->bar_graph["YMIN"];
                    $model->bar_graph_k{$graph}["label"] = $model->bar_graph["label"];
                    $model->bar_graph_k{$graph}["hanrei"] = $model->bar_graph["hanrei"];
                    $model->bar_graph_k{$graph}["tooltip"] = $model->bar_graph["tooltip"];
                    
                    //色
                    $model->bar_graph_k{$graph}["color"] = $color1[$graph].",".$color2[$graph];
                    
                    //$model->bar_graph{$graph}[0] = $model->bar{$graph};
                    //$model->bar_graph{$graph}[1] = $model->bar_avg{$graph};
                    //グラフ用hidden作成
                    graph::CreateDataHidden($objForm,$model->bar_graph_k{$graph},$graph);
                    $arg["MLT"][] = $graphno;
                }
                //jsでループする用
                knjCreateHidden($objForm, "GRAPH", $graph);
            }
            
            knjCreateHidden($objForm, "type", "bar");
            knjCreateHidden($objForm, "maxTicksLimit", 11);
        }else{
            $arg["cssclass"] = "holder2";
            $arg["height"] = 500;
            
            $model->radar_graph["TITLE"] = "偏差値";
            $model->radar_graph += array_merge($model->radar);
            
            $model->radar_graph["YMIN"] = 0;
            $model->radar_graph["YMAX"] = 80;
            
            $arg["GRAPH_TYPE"] = "radar";

            //グラフ用hidden作成
            graph::CreateDataHidden($objForm,$model->radar_graph,0);
            
            knjCreateHidden($objForm, "type", "radar");
            knjCreateHidden($objForm, "maxTicksLimit", 6);
        }
        
        
        //hidden
        //makeHidden($objForm, $model, $testAry["SCORE"], $testAry["DEVIATION"]);
        knjCreateHidden($objForm, "cmd", $model->cmd);
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "radio", $model->Radio);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        //View::toHTML($model, "knjh320Form1.html", $arg);
        //$jsplugin = "chart.js|Chart.min.js|graph.js";
        //$jsplugin = "chart.js|Chart.min.js|graph.js";
        $jsplugin = "Chart.min.js|graph.js";
        View::toHTML6($model, "knjh320Form1.html", $arg, $jsplugin, $cssplugin);
    }
}

//ALLチェックボックス作成
function makeAllCheck(&$objForm, &$arg, $name, $cmd)
{
    $arg[$name] = createCheckBox($objForm, $name, "ON", " onClick=\"allCheck('".$cmd."', this)\";", "1");
}

//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, &$model)
{
    $width = 20;

    $head  = "<th align=\"center\" width=* colspan=2 nowrap >科目（講座）</th> ";
    $subclass[0] = array("code"  => "*", "value" => "HEAD");
    $i = 1;
    $result = $db->query(knjh320Query::getTestSubclass($model));
    //グラフ用データ
    $hcnm = "";
    
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $head .= "<th width=38 align=\"center\" nowrap >".$row["LABEL"]."</th> ";
        
        $subclass[$i] = array("code"  => $row["VALUE"], "value" => $row["LABEL"]);
        $i++;
        $width += 4;
        
        $model->bar_graph["label"] .= $hcnm.$row["LABEL"];
        $model->radar_graph["label"] .= $hcnm.$row["LABEL"];
        $model->subclasscd .= $hcnm."'".$row["VALUE"]."'";
        $model->subclasscd_array[] = $row["VALUE"];
        $hcnm = ",";
    }
    $result->free();
    $arg["WIDTH"] = $width."%";
    $arg["HEAD"] = $head;
    $arg["COL"]  = $i;
    //スクロールバーの表示
    if ($i > 20) {
        $arg["over"] = $i;
    }
    return $subclass;
}

//明細
function makeMeisai(&$objForm, &$arg, $db, $model, $subclass, $grade)
{

    //学校マスタ
    $knjSchoolMst = array();
    $query = knjh320Query::getSchoolMst();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($row as $key => $val) {
            $knjSchoolMst[$key] = $val;
        }
    }
    $result->free();

    $i = 0;
    $barH = 24;    //1行の高さ
    $overwith = $barH * 2 + 25;
    $result = $db->query(knjh320Query::getSemester($model));
    while ($semeData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $testAry = setMeisai($objForm, $arg, $db, $model, $scoreArr, $deviationArr, $subclass, $semeData, $i, $grade);
        //setAbsent($arg, $db, $model, $semeData, $subclass, $knjSchoolMst);
    }
    $overwith += ($barH * (get_count($testAry) + (get_count($testAry["SCORE"]) * 3)));
    $arg["OVERWITH"] = $overwith;
    $result->free();
    return $testAry;
}

//明細設定
function setMeisai(&$objForm, &$arg, $db, $model, &$scoreArr, &$deviationArr, $subclass, $semeData, &$i, $grade)
{
    $result = $db->query(knjh320Query::getTestKind($model, $semeData["SEMESTER"]));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //$checkBox = createCheckBox($objForm, "CHK_TEST".$i, "ON", "id=\"CHK_TEST{$i}\"", "1");
        //$head  = "<tr bgcolor=white> <th align=\"left\" width=* nowrap rowspan=3 bgcolor=white>".$checkBox.$row["TESTITEMNAME"]."</th> ";
        $head  = "<tr bgcolor=white> <th align=\"left\" width=* nowrap rowspan=3 bgcolor=white>&nbsp;".$semeData["SEMESTERNAME"]." ".$row["TESTITEMNAME"]."</th> ";
        $model->dcnm = "";
        $head .= setMeisaiData($db, $model, $scoreArr,     $i, $row, "得点",   $subclass, "SCORE",     "black", $grade);
        $model->dcnm = "";
        $head .= setMeisaiData($db, $model, $deviationArr, $i, $row, "偏差値", $subclass, "DEVIATION", "blue", $grade);
        $head .= setMeisaiData($db, $model, $dummyAry,     0,  $row, "順位",   $subclass, "RANK",      "black", $grade);
        $arg["data"][]["MEISAI"] = $head;
        $i++;
        
        //グラフ用
        $model->bar_graph["hanrei"] .= $model->hancnm.$semeData["SEMESTERNAME"]." ".$row["TESTITEMNAME"];
        $model->bar_graph_avg .= $model->hancnm.$semeData["SEMESTERNAME"]." ".str_replace("考査","平均",$row["TESTITEMNAME"]);
        $model->radar_graph["hanrei"] .= $model->hancnm.$semeData["SEMESTERNAME"]." ".$row["TESTITEMNAME"];
        $model->hancnm = ",";
        $model->hanCnt++;
        

        //考査ごとグラフ用
        $model->bar_graph_k{$model->graphno}["cnt"] = 2;
        $model->bar_graph_k{$model->graphno}["TITLE"] = $semeData["SEMESTERNAME"]." ".$row["TESTITEMNAME"];
        $model->bar_graph_k{$model->graphno}["type"] = "bar,line";
        $model->bar_graph_k{$model->graphno}["LineType"] = "1,0";
        $model->graphno++;
    }
    
    $result->free();
    $testAry["SCORE"] = $scoreArr;
    $testAry["DEVIATION"] = $deviationArr;
    return $testAry;
}

//結果設定
function setAbsent(&$arg, $db, $model, $semeData, $subclass, $knjSchoolMst)
{
    $head  = "<tr bgcolor=white> <th align=\"center\" width=* nowrap bgcolor=white>".$semeData["SEMESTERNAME"]."</th> ";
    $head .= setMeisaiData($db, $model, $dummyAry, 0, $semeData, "欠課", $subclass, "ABSENT", "black", "", $knjSchoolMst);
    $arg["data"][]["MEISAI"] = $head;
}

//明細データ作成
function setMeisaiData($db, $model, &$testAry, $i, $row, $subTitle, $subclass, $scoreDiv, $color, $grade, $knjSchoolMst = array())
{
    $kousacnm = "";
    $head .= "<th align=\"left\" width=* nowrap bgcolor=white>".$subTitle."</th> ";
    //for ($subcnt = 1; $subcnt < get_count($subclass); $subcnt++) {
        if ($model->recordTable == "KIN_RECORD_DAT" && $scoreDiv == "DEVIATION") {
            //$score = "";
            for($subcnt = 1; $subcnt < get_count($subclass); $subcnt++){
                $head .= "<th width=30 align=\"center\" bgcolor=\"white\" nowrap ><font color=\"".$color."\"> </font></th> ";
                
                if($scoreDiv == "SCORE"){
                    //平均なしグラフ
                    $model->bar[$i] .= $model->dcnm;
                    $model->bar_avg[$i] .= $model->dcnm;
                    //考査ごとグラフ
                    $model->bar_graph_k{$model->graphno}[0] .= $kousacnm;
                    $model->bar_graph_k{$model->graphno}[1] .= $kousacnm;
                    
                    $model->dcnm = ",";
                    $model->ssum[] = $score;
                    $kousacnm = ",";
                }else if($scoreDiv == "DEVIATION"){
                    $model->radar[$i] .= $model->dcnm;
                    $model->dcnm = ",";
                    $model->dsum[] = $score;
                }
            }
            
        } else {
            $query = makeScoreSql($row, $subclass[$subcnt]["code"], $model, $scoreDiv, $grade, $knjSchoolMst);
            //$score = $db->getOne($query);
            $result = $db->query($query);
            while($scoreRow = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $data[$scoreRow["SUBCLASSCD"]]["SCORE"] = $scoreRow["SCORE"];
                if($scoreRow["AVG"] != ""){
                    $data[$scoreRow["SUBCLASSCD"]]["AVG"] = sprintf("%0.1f", round($scoreRow["AVG"], 1));
                }else{
                    $data[$scoreRow["SUBCLASSCD"]]["AVG"] = $scoreRow["AVG"];
                }
            }
            foreach($model->subclasscd_array as $key => $val){
                if(!empty($data) && array_key_exists($val, $data)){
                    $head .= "<th width=30 align=\"center\" bgcolor=\"white\" nowrap ><font color=\"".$color."\">".$data[$val]["SCORE"]."</font></th> ";
                    
                    if($scoreDiv == "SCORE"){
                        $model->bar[$i] .= $model->dcnm.$data[$val]["SCORE"];
                        $model->bar_avg[$i] .= $model->dcnm.$data[$val]["AVG"];
                        //考査ごとグラフ
                        $model->bar_graph_k{$model->graphno}[0] .= $kousacnm.$data[$val]["SCORE"];
                        $model->bar_graph_k{$model->graphno}[1] .= $kousacnm.$data[$val]["AVG"];
                    
                        $model->dcnm = ",";
                        $model->ssum[] = $data[$val]["SCORE"];
                        $kousacnm = ",";
                   }else if($scoreDiv == "DEVIATION"){
                        $model->radar[$i] .= $model->dcnm.$data[$val]["SCORE"];
                        $model->dcnm = ",";
                        $model->dsum[] = $data[$val]["SCORE"];
                    }
                }else{
                    $head .= "<th width=30 align=\"center\" bgcolor=\"white\" nowrap ><font color=\"".$color."\"></font></th> ";
                    
                    if($scoreDiv == "SCORE"){
                        $model->bar[$i] .= $model->dcnm;
                        $model->bar_avg[$i] .= $model->dcnm;
                        //考査ごとグラフ
                        $model->bar_graph_k{$model->graphno}[0] .= $kousacnm;
                        $model->bar_graph_k{$model->graphno}[1] .= $kousacnm;
                    
                        $model->dcnm = ",";
                        //$model->ssum[] = $data[$val]["SCORE"];
                        $kousacnm = ",";
                    }else if($scoreDiv == "DEVIATION"){
                        $model->radar[$i] .= $model->dcnm;
                        $model->dcnm = ",";
                        //$model->dsum[] = $data[$val]["SCORE"];
                    }
                }
            }
        }
        
        //$testCom = ($subcnt == get_count($subclass) - 1) ? "" : ",";
        //$testAry[$i] .= $row["TESTITEMNAME"]."-".$subclass[$subcnt]["value"]."-".$score.$testCom;
    //}
    $head .= ($scoreDiv == "RANK" || $scoreDiv == "ABSENT") ? "</tr>" : "</tr> <tr>";
    return $head;
}

//各項目のSQL作成
function makeScoreSql($row, $subclassCd, $model, $scoreDiv, $grade, $knjSchoolMst)
{
    if ($scoreDiv == "SCORE") {
        //$query = knjh320Query::getScore($row["SEMESTER"], $row["TESTKINDCD"], $subclassCd, $model);
        $query = knjh320Query::getScore($row["SEMESTER"], $row["PROFICIENCYCD"], $model->subclasscd, $model);
    } else if ($scoreDiv == "ABSENT") {
        //$query = knjh320Query::getAbsent($row["SEMESTER"], $subclassCd, $model, $knjSchoolMst);
        $query = knjh320Query::getAbsent($row["SEMESTER"], $model->subclasscd, $model, $knjSchoolMst);
    } else {
        //$query = knjh320Query::getRank($row["SEMESTER"], $row["TESTKINDCD"], $subclassCd, $model, $grade, $scoreDiv);
        $query = knjh320Query::getRank($row["SEMESTER"], $row["PROFICIENCYCD"], $model->subclasscd, $model, $grade, $scoreDiv);
    }
    return $query;
}

//リンク作成
function makeLink(&$arg, $year, $semester, $name, $schregno)
{
    if ($year != "") {
        $hash = "knjh320index.php?&cmd=yearChange&YEAR=".$year."&SEMESTER=".$semester."&SCHREGNO=".$schregno;
        $arg["HEADER_".$name] = "<a href=\"" .$hash. "\" target=\"_self\">";
        $arg["FOOTER_".$name] = "</a>";
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //折れ線グラフボタン
    //$url = REQUESTROOT. "/H/KNJH311/knjh311index.php";
    $arg["BAR"] = createBtn($objForm, "BAR", "得点(棒グラフ)", "onclick=\" btn_submit('bar');\"");
    //レーダーチャートボタン
    //$url = REQUESTROOT. "/H/KNJH311/knjh311index.php";
    $arg["RADAR"] = createBtn($objForm, "RADAR", "偏差値(レーダー)", "onclick=\" btn_submit('radar')\"");
    //終了ボタン
    $arg["BTN_END"] = createBtn($objForm, "BTN_END", "戻 る", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model, $score, $deviation)
{
/*    for ($i = 0; $i < get_count($score); $i++) {
        $objForm->ae(createHiddenAe("SCORE".$i, $score[$i]));
    }
    $objForm->ae(createHiddenAe("CHK_TEST_CNT", get_count($score)));

    for ($i = 0; $i < get_count($deviation); $i++) {
        $objForm->ae(createHiddenAe("DEVIATION".$i, $deviation[$i]));
    }
    $objForm->ae(createHiddenAe("CHK_DEVIATION_CNT", get_count($deviation)));*/

    //$objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
    return $objForm->ge($name);
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

?>
