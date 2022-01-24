<?php
class knjh830Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh830index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();
        
        //生徒情報取得
        //$model->gakusekiを使って取得する
        if($model->gakuseki != ""){
            $query = knjh830Query::getStudentName($model->gakuseki, $model->year);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            $arg["GRADE"] = $row["GRADE"]."-".$row["HR_CLASS"]."-".$row["ATTENDNO"];
            $arg["NAME"] = $row["NAME"];

            $semester = $row["SEMESTER"];
        }

        
        //表示内容
        //データの区分
        $extra = " onchange=\"btn_submit('change');\"";
        $query = knjh830Query::getSite();
        $sresult = $db->query($query);
        $opt = array();
        while($row = $sresult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label"  => $row["NAME1"],
                           "value"  => $row["NAME2"]);
        }
        $arg["data"]["SITE"] = knjCreateCombo($objForm, "SITE", $model->field["SITE"], $opt, $extra, "1");
        
        //教科
        $extra = " onchange=\"btn_submit('change');\"";
        $query = knjh830Query::getKyouka($model->year, $model->field["SITE"]);
        $kresult = $db->query($query);
        $opt = array();
        //「すべて」はなくす予定
        $opt[0] = array("label" => "すべて",
                        "value" => "");
        while($row = $kresult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label"  => $row["LABEL"],
                           "value"  => $row["VALUE"]);
        }
        $arg["data"]["KYOKA"] = knjCreateCombo($objForm, "KYOKA", $model->field["KYOKA"], $opt, $extra, "1");
        
        //科目
        $extra = " onchange=\"btn_submit('change');\"";
        $opt2 = array();
        $opt2[0] = array("label" => "",
                         "value" => "");
        if($model->gakuseki != "" && $model->field["KYOKA"] != ""){
            //Queryで中身取得
            $query = knjh830Query::getKamoku($model->year, $model->field["SITE"], $model->gakuseki, $model->field["KYOKA"]);
            $Result = $db->query($query);
            while($row = $Result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt2[] = array("label"  => $row["LABEL"],
                                "value"  => $row["VALUE"]);
            }
        }
        $arg["data"]["KAMOKU"] = knjCreateCombo($objForm, "KAMOKU", $model->field["KAMOKU"], $opt2, $extra, "1");
        
        
        //切り替え
        $extra = " onchange=\"checkCng();\"";
        $change[0] = "受験回数別";
        $change[1] = "大分野別";
        $change[2] = "小分野別";
        
        $opt4 = array();
        foreach($change as $key => $val){
            $opt4[] = array("label"  => $val,
                            "value"  => $key);
        }
        $arg["data"]["KIRIKAE"] = knjCreateCombo($objForm, "KIRIKAE", $model->field["KIRIKAE"], $opt4, $extra, "1");
        
        if($model->mode != 0){
            //表示モードのとき一応データの有無を確認しておく。いらなければ削除。
            $query = knjh830Query::getTestId($model->year, $model->field["SITE"], $model->gakuseki, $model->field["KYOKA"], $model->field["KAMOKU"], "1");
            $cnt = $db->getOne($query);
            if($cnt == 0){
                $model->setMessage('対象のデータがありません');
                $model->mode = 0;
            }
        }
        
        if($model->gakuseki != ""){
            //前年度と来年度のデータがあるか確認して移動するボタンを作る。
            $arg["NOW_YEAR"] = $model->year."年度".$semester."学期";
            $backyear = $model->year - 1;
            $nextyear = $model->year + 1;
            $query = knjh830Query::getBackNextData($model->gakuseki, $backyear);
            $backCnt = $db->getOne($query);
            if($backCnt > 0){
                $extra = " onclick=\"yearChange('{$backyear}');\"";
                $arg["BACK_YEAR"] = createBtn($objForm, "btn_back", "前年度", $extra);
            }
            $query = knjh830Query::getBackNextData($model->gakuseki, $nextyear);
            $nextCnt = $db->getOne($query);
            if($nextCnt > 0){
                $extra = " onclick=\"yearChange('{$nextyear}');\"";
                $arg["NEXT_YEAR"] = createBtn($objForm, "btn_next", "次年度", $extra);
            }
        }
        
        if($model->mode != 0){
            $arg["SPACE"] = 1;
            //テストID取得
            $query = knjh830Query::getTestId($model->year, $model->field["SITE"], $model->gakuseki, $model->field["KYOKA"], $model->field["KAMOKU"]);
            $Result = $db->query($query);
            $k = 0;
            while($row = $Result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($model->field["KIRIKAE"] == "0"){
                    if($k == 0){
                        $k++;
                    }
                }
                //指定したテストの教科等を取得
                $query = knjh830Query::getTestName($model->year, $model->gakuseki, $row["TEACHING_CD"]);
                $tRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $tRow["TEACHING_NAME"] = str_replace("日本語 : ", "", $tRow["TEACHING_NAME"]);
                if(mb_strlen($tRow["TEACHING_NAME"]) > 10){
                    $result["KAISU"] = "<td nowrap onmouseover=\"tooltip.Schedule( this, event );\" tooltip=\"{$tRow["TEACHING_NAME"]}\">".mb_substr($tRow["TEACHING_NAME"], 0, 8)."…</td>";
                }else{
                    $result["KAISU"] = "<td nowrap>".$tRow["TEACHING_NAME"]."</td>";
                }
                if(mb_strlen($tRow["SUBCLASSNAME"]) > 7){
                    $result["NAIYO"] = "<td nowrap onmouseover=\"tooltip2.Schedule( this, event );\" tooltip=\"{$tRow["SUBCLASSNAME"]}\">".mb_substr($tRow["SUBCLASSNAME"], 0, 8)."…</td>";
                }else{
                    $result["NAIYO"] = "<td nowrap>".$tRow["SUBCLASSNAME"]."</td>";
                }
                $result["HAITEN"] = $tRow["ALL_CNT"];
                
                $resu["left_b"] = $result["KAISU"].$result["NAIYO"]."<td nowrap>".$result["HAITEN"]."</td>";
                
                if($model->field["KIRIKAE"] == "0"){
                    if($k == 1){
                        //最大受験回数を取りたい
                        $query = knjh830Query::getTestId($model->year, $model->field["SITE"], $model->gakuseki, $model->field["KYOKA"], $model->field["KAMOKU"], "2");
                        $max = $db->getOne($query);
                        
                        $arg["example"] = "得点(日付)";
                        $resu["right_h"] = "";
                        for($i=1;$i<=$max;$i++){
                            $resu["right_h"] .= "<th class=\"no_search\">".$i."回目</th>";
                        }
                        $right = $resu["right_h"];
                    }
                    
                    //テストの回数データ取得
                    $query = knjh830Query::getTestData($model->year, $model->gakuseki, $row["TEACHING_CD"]);
                    $dResult = $db->query($query);
                    $i=0;
                    $resu["right_b"] = "";
                    while($dRow = $dResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        $i++;
                        
                        $stDate = str_replace("-", "/", substr($dRow["START_DATE"], 0,10));
                        $result[$i] = "<span style=\"font-weight:bold;font-size:large;\">".$dRow["RIGHT_CNT"]."</span><span style=\"font-size:small;\">(".$stDate.")</span>";
                        
                        $resu["right_b"] .= "<td nowrap>".$result[$i]."</td>";
                    }
                    if($max > $i){
                        for($j=$i;$j<$max;$j++){
                            $resu["right_b"] .= "<td nowrap>-</td>";
                        }
                    }

                }else if($model->field["KIRIKAE"] == "2"){
                    if($k == 0){    //小分野のとき
                        $arg["example"] = "得点/配点";
                        
                        //大分野名称と小分野個数取得
                        $query = knjh830Query::getQuestCnt($model->year, $model->field["SITE"], $model->gakuseki, $model->field["KYOKA"], $model->field["KAMOKU"]);
                        $qResult = $db->query($query);
                        $resu["right_h"] = "";
                        $resu["right_f"] = "";
                        $partArray = array();
                        $pCnt = 0;
                        while($qRow = $qResult->fetchRow(DB_FETCHMODE_ASSOC)){
                            if(mb_strlen($qRow["THIRD_NAME"]) > 7){
                                $thirdname = "<th colspan=\"{$qRow["CNT"]}\" class=\"no_search\" onmouseover=\"tooltip.Schedule( this, event );\" tooltip=\"{$qRow["THIRD_NAME"]}\">".mb_substr($qRow["THIRD_NAME"],0,5)."…</th>";    //折り返さないようにカット
                            }else if($qRow["PARTCD"] != "0000"){
                                $thirdname = "<th colspan=\"{$qRow["CNT"]}\" class=\"no_search\" style=\"width:342px;\">".$qRow["THIRD_NAME"]."</th>";
                            }else{
                                $thirdname = "<th colspan=\"{$qRow["CNT"]}\" class=\"no_search\" style=\"width:342px;\">分野未設定</th>";
                            }
                            $resu["right_h"] .= $thirdname;
                            $partArray[$pCnt] = $qRow["PARTCD"];
                            $pCnt++;
                        }
                        
                        //小分野ごとの名称取得
                        $query = knjh830Query::getQuestName($model->year, $model->field["SITE"], $model->gakuseki, $model->field["KYOKA"], $model->field["KAMOKU"]);
                        $qResult = $db->query($query);
                        $questArray = array();
                        $qCnt = 0;
                        $resu["right_h"] .= "<tr>";
                        while($qRow = $qResult->fetchRow(DB_FETCHMODE_ASSOC)){
                            //大分野と小分野のつながりはチェックしてない
                            if(mb_strlen($qRow["FOURTH_NAME"]) > 7){
                                $fourthname = "<th class=\"no_search\" onmouseover=\"tooltip.Schedule( this, event );\" tooltip=\"{$qRow["FOURTH_NAME"]}\">".mb_substr($qRow["FOURTH_NAME"],0,5)."…</th>";    //折り返さないようにカット
                            }else if($qRow["QUEST_FIELDNO"] != "000000"){
                                if($qRow["FOURTH_NAME"] == ""){
                                    $qRow["FOURTH_NAME"] = "分野なし";
                                }
                                $fourthname = "<th class=\"no_search\">".$qRow["FOURTH_NAME"]."</th>";
                            }else{
                                $fourthname = "<th class=\"no_search\">分野未設定</th>";
                            }
                            $percent = sprintf("%0.1f", $qRow["PERCENT"]);
                            $resu["right_h"] .= $fourthname;
                            $resu["right_f"] .= "<td nowrap>".$percent."%</td>";     //得点率
                            $questArray[$qCnt] = $qRow["QUEST_FIELDNO"];
                            $qCnt++;
                        }
                        $max = $qCnt;
                        $right = $resu["right_h"];
                        $arg["right_f"] = $resu["right_f"];
                        $arg["right_f"] = $resu["right_f"];
                        
                        $resu["left_f"] = "<td nowrap>計</td><td nowrap colspan=\"2\">正答率</td>";
                        $arg["left_f"] = $resu["left_f"];
                        
                        $k++;
                    }
                    
                    //小分野の配点と得点を取得  最新の受験回数のデータ
                    $query = knjh830Query::getQuestPoint($model->year, $model->gakuseki, $row["TEACHING_CD"]);
                    $pResult = $db->query($query);
                    
                    $count = 0;
                    $resu["right_b"] = "";
                    while($pRow = $pResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        $key = array_search($pRow["QUESTCD"], $questArray);   //QUESTCDの場所を探す
                        for($s=$count;$s<$key;$s++){
                            $resu["right_b"] .= "<td nowrap>-</td>";
                        }
                        $count = $s;
                        $resu["right_b"] .= "<td nowrap>".$pRow["CNT"]."/".$pRow["ALLCNT"]."</td>";
                        
                        $count++;
                    }
                    for($last=$count;$last<$qCnt; $last++){
                        $resu["right_b"] .= "<td nowrap>-</td>";
                    }

                }else{
                    if($k == 0){    //大分野別のときだけ
                        //大分野別の名称と得点率取得    最新の受験回数のデータ
                        $query = knjh830Query::getPercent($model->year, $model->field["SITE"], $model->gakuseki, $model->field["KYOKA"], $model->field["KAMOKU"]);
                        $pResult = $db->query($query);
                        $a = 0;
                        $percent = array();
                        while($pRow = $pResult->fetchRow(DB_FETCHMODE_ASSOC)){
                            if(mb_strlen($pRow["THIRD_NAME"]) > 7){
                                $thirdname = "<th class=\"no_search\" onmouseover=\"tooltip.Schedule( this, event );\" tooltip=\"{$pRow["THIRD_NAME"]}\">".mb_substr($pRow["THIRD_NAME"],0,5)."…</th>";    //折り返さないようにカット
                            }else if($pRow["PARTCD"] != "0000"){
                                if($pRow["THIRD_NAME"] == ""){
                                    $pRow["THIRD_NAME"] = "分野なし";
                                }
                                $thirdname = "<th class=\"no_search\">".$pRow["THIRD_NAME"]."</th>";
                            }else{
                                $thirdname = "<th colspan=\"{$qRow["CNT"]}\" class=\"no_search\" style=\"width:342px;\">分野未設定</th>";
                            }
                            $percent[$a]["PARTNAME"] = $thirdname;
                            $percent[$a]["PARTCD"] = $pRow["PARTCD"];
                            $percent[$a]["PERCENT"] = sprintf("%0.1f", $pRow["PERCENT"]);
                            
                            $a++;
                        }
                        $max = $a;
                        $resu["right_h"] = "";
                        $resu["right_f"] = "";
                        for($b=0;$b<$a;$b++){
                            $resu["right_h"] .= $percent[$b]["PARTNAME"];
                            $resu["right_f"] .= "<td nowrap>".$percent[$b]["PERCENT"]."%</td>";
                        }
                        $right = $resu["right_h"];
                        $arg["right_f"] = $resu["right_f"];
                        
                        $resu["left_f"] = "<td nowrap>計</td><td nowrap colspan=\"2\">正答率</td>";
                        $arg["left_f"] = $resu["left_f"];
                        
                        $k++;
                    }
                    
                    //データ取得    最新の受験回数のデータ
                    $query = knjh830Query::getPartScore($model->year, $model->gakuseki, $row["TEACHING_CD"]);
                    $sResult = $db->query($query);
                    $count = 0;
                    $resu["right_b"] = "";
                    $array = array_column($percent, "PARTCD");  //PARTCDのみの配列を作成
                    while($sRow = $sResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        $key = array_search($sRow["PARTCD"], $array);   //PARTCDの場所を探す
                        for($s=$count;$s<$key;$s++){
                            $resu["right_b"] .= "<td nowrap>-</td>";
                        }
                        $count = $s;
                        $resu["right_b"] .= "<td nowrap>".$sRow["CNT"]."/".$sRow["ALLCNT"]."</td>";
                        
                        $count++;
                    }
                    for($last=$count;$last < $a; $last++){
                        $resu["right_b"] .= "<td nowrap>-</td>";
                    }
                }
                
                $arg["data2"][] = $resu;
                $k++;
            }
        }
            //#holizonの代わり
            $width = 114*$max;
            $arg["width"] = "style=\"width:".$width."px;\"";
            $arg["right_h"] = $right;
        
        //スクロールのjavascript部分
        if($model->field["KIRIKAE"] == "0"){
            $jsdata .= "\$E(\"right_body2\").onscroll=scroll;\n";
            $jsfunc .= "    \$E(\"right_header\").scrollLeft= \$E(\"right_body2\").scrollLeft;\n";    // 左右連動させる
            $jsfunc .= "    \$E(\"left_body2\").scrollTop = \$E(\"right_body2\").scrollTop;\n";    // 上下連動させる*/
            
            $arg["left_b_id"] = "left_body2";
            $arg["right_b_id"] = "right_body2";
        }else{
            $jsdata .= "\$E(\"right_body\").onscroll=scroll;\n";
            $jsfunc .= "    \$E(\"right_header\").scrollLeft = \$E(\"right_f\").scrollLeft = \$E(\"right_body\").scrollLeft;\n";    // 左右連動させる
            $jsfunc .= "    \$E(\"left_body\").scrollTop = \$E(\"right_body\").scrollTop;\n";    // 上下連動させる*/
            
            $arg["left_b_id"] = "left_body";
            $arg["right_b_id"] = "right_body";
        }
        
        //javascript
        $arg["java"] = "<script type=\"text/javascript\">\n";
        $arg["java"] .= "function \$E(id){ return document.getElementById(id); }\n";
        $arg["java"] .= "function scroll(){\n";
        $arg["java"] .= $jsfunc;
        $arg["java"] .= "}\n";
        $arg["java"] .= $jsdata;
        $arg["java"] .= "</script>\n";


        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjh830Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //表示
    $extra = " onclick=\"btn_submit('hyouzi');\"";
    $arg["button"]["btn_search"] = createBtn($objForm, "btn_search", "表 示", $extra);
    if($model->mode != 0){
        //CSV
        $extra = " onclick=\"btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "CSV出力", $extra);
    }
    //終了
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
    $objForm->ae(createHiddenAe("YEAR"));                           //年度移動ボタン押して年度を変える箱。jsで制御。
    $objForm->ae(createHiddenAe("BEFORE_YEAR", $model->year));      //年度移動ボタン押したときに表示不備エラーなら年度を戻すためのもの。現在表示している年度が入る。
    $objForm->ae(createHiddenAe("sort", $model->sort));
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
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
