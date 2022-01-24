<?php

require_once('for_php7.php');

class knjz620Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }


        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjz620index.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        //グラフ作成用
        $model->data = array();
        $data = array();
        $dcnm = "";
        $hcnm = "";

        //年度
        $arg["CTRLYEAR"] = $model->year."年度";
        $ctrlyear = $model->year;
        //学年
        //SCHREG_REGD_BASE_MSTから取得
        $schregQuery = knjz620Query::getSchreg($model->year, $model->GAKUSEKI);
        $schregRow = $db->getRow($schregQuery, DB_FETCHMODE_ASSOC);
        
        $model->gakunen = number_format($schregRow["GRADE"]);

        $arg["GAKUNEN"] = $model->gakunen."年生";
        //名前
        $arg["NAME"] = $schregRow["HR_CLASS"]."組　".$schregRow["ATTENDNO"]."番　".$schregRow["NAME_SHOW"];
        
        //データ
        //$model->GAKUSEKI = "13100020";
        //$ctrlyear = CTRL_YEAR;
        //$ctrlyear = '2016';

        //先に表示パーツ作成
        //業者
        $extra = " style=\"font-size:110%;\" onchange=\"btn_submit('sanka')\"";
        $gyousyaQuery = knjz620Query::getGyousya();
        /*$array = array("0" => "全表示",
                       "1" => "ベネッセ",
                       "2" => "駿台");
        $opt = array();
        foreach($array as $key => $val){
            $opt[] = array("label"  => $val,
                           "value"  => $key);
        }
        //$model->field["GYOUSYA"] = 0;
        $arg["GYOUSYA"] = knjCreateCombo($objForm, "GYOUSYA", $model->field["GYOUSYA"], $opt, $extra, 1);*/
        makeCmb($objForm, $arg, $db, $gyousyaQuery, $model->field["GYOUSYA"], "GYOUSYA", $extra, 1, "GYOUSYA");
        
        //型
        $extra = " style=\"font-size:110%;\" onchange=\"btn_submit('sanka')\"";
        $array = array("0" => "教科",
                       "1" => "受験型");
        $opt1 = array();
        foreach($array as $key => $val){
            $opt1[] = array("label"  => $val,
                           "value"  => $key);
        }
        //$model->field["KATA"] = 0;
        $arg["KATA"] = knjCreateCombo($objForm, "KATA", $model->field["KATA"], $opt1, $extra, 1);
        
        //模試種別
        $extra = " style=\"font-size:110%;\" onchange=\"btn_submit('syubetu_change')\"";
        $syubetuQuery = knjz620Query::getSyubetu();
        
        /*$array = array("0" => "全模試",
                       "1" => "マーク",
                       "2" => "記述");
        $opt2 = array();
        foreach($array as $key => $val){
            $opt2[] = array("label"  => $val,
                           "value"  => $key);
        }
        //$model->field["SYUBETU"] = 0;
        $arg["SYUBETU"] = knjCreateCombo($objForm, "SYUBETU", $model->field["SYUBETU"], $opt2, $extra, 1);*/
        makeCmb($objForm, $arg, $db, $syubetuQuery, $model->field["SYUBETU"], "SYUBETU", $extra, 1, "SYUBETU");

        //チェックボックス
        //教科名取得
        $kyoukaQuery = knjz620Query::getKyouka($model->GAKUSEKI,$model->field["KATA"], $model->field["GYOUSYA"],$model->field["SYUBETU"]);
        $kyoukaResult = $db->query($kyoukaQuery);
        
        //設定した初期値が含まれているかのチェック
        $ex_kyouka = array();
        while($kyouka = $kyoukaResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $ex_kyouka[$kyouka["MOCK_SUBCLASS_CD"]] = $kyouka["SUBCLASS_ABBV"];
        }

        $existflg = 0;
        if(!empty($model->field["CHECK"])){
            foreach($model->field["CHECK"] as $key => $value){
                if(array_key_exists($value, $ex_kyouka)){
                    $existflg = 1;
                }else{
                    unset($model->field["CHECK"][$key]);
                }
            }
            if($existflg != 1){
                $model->field["CHECK"] = array();
            }
        }
        
        $opt3 = array();
        
        //教科名取得
        $kyoukaQuery = knjz620Query::getKyouka($model->GAKUSEKI,$model->field["KATA"], $model->field["GYOUSYA"],$model->field["SYUBETU"]);
        $kyoukaResult = $db->query($kyoukaQuery);
        while($kyoukaRow = $kyoukaResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt3[] = array("label"     =>  $kyoukaRow["SUBCLASS_ABBV"],
                            "value"     =>  $kyoukaRow["MOCK_SUBCLASS_CD"]);
        }
        $arg["CHECK"] = "<table>";
        $i = 0;
        $emptyflg = 0;  //CHECKが空だったら全部につけるためのフラグ
        foreach($opt3 as $key => $val){
            $i++;
            if($i%10==1){
                $arg["CHECK"] .= "<tr>";
            }
            $extra = " id=\"".$key."\"";
            if(!empty($model->field["CHECK"]) && $emptyflg != 1){
                if(in_array($val["value"], $model->field["CHECK"])){
                    $extra .= " checked";
                    //グラフ用
                    $model->data["hanrei"] .= $hcnm.$val["label"];
                    $hcnm = ",";
                }
            }else{
                $extra .= " checked";
                //グラフ用
                $model->data["hanrei"] .= $hcnm.$val["label"];
                $hcnm = ",";
                $model->field["CHECK"][$i-1] = $val["value"];
                $emptyflg = 1;
            }

            $arg["CHECK"] .= "\n<td nowrap>".knjCreateCheckBox($objForm, "CHECK[]", $val["value"], $extra)."<LABEL for=".$key.">".$val["label"]."</LABEL></td>";
            if($i%10==0){
                $arg["CHECK"] .= "</tr>\n";
            }
        }
        if($i%10!=0){
            $arg["CHECK"] .= "</tr>";
        }
        $arg["CHECK"] .= "</table>";

            
        //グラフ用
        $model->data["cnt"] = get_count($model->field["CHECK"]);
        
        if(get_count($model->field["CHECK"]) > 8){
            $arg["width"] = 100;
        }else{
            $arg["width"] = 80;
        }

        //業者
        if($model->field["GYOUSYA"] == "0"){
            $gyousya = "";
        }else{
            $gyousya = sprintf("%08d", $model->field["GYOUSYA"]);
        }
        
        //表作りたい
        //th部分
        foreach($opt3 as $key => $val){
            $gaku[$val["value"]] = $val["label"];
        }
        $kyouka = "";
        $cnm = "";
        foreach($model->field["CHECK"] as $key => $val){
            $thkyouka["kyouka"] = $gaku[$val];
            
            $arg["data"][] = $thkyouka;
            
            $kyouka .= $cnm.$val;
            $cnm = "','";
        }
        
        //該当する模試を取得
        $mockQuery = knjz620Query::getMock($model->GAKUSEKI, $gyousya, $model->field["SYUBETU"], "1");
        $mockCnt = $db->getOne($mockQuery);
        if($mockCnt > 0){
                
            $kyoukaCnt = get_count($model->field["CHECK"]);
            
            $mockQuery = knjz620Query::getMock($model->GAKUSEKI, $gyousya, $model->field["SYUBETU"]);
            $mockResult = $db->query($mockQuery);
            $mock = array();
            $deviation = array();
            $dcnt = 0;
                
            $kyoukaCnt = get_count($model->field["CHECK"]);
            while($mockRow = $mockResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $tdCnt = 1;
                $mock["MOCKNAME"] = "<th class=\"no_search\"  align=\"center\" nowrap>".$mockRow["MOCKNAME1"]."</th>";
                
                //グラフ用
                $model->data["label"] .= $dcnm.$mockRow["MOCKNAME1"];
                $dcnm = ",";
                
                //模試ごとの成績を取得
                $seisekiQuery = knjz620Query::getSeiseki($mockRow["MOCKCD"], $model->GAKUSEKI, $model->field["KATA"], $kyouka, $model->field["SYUBETU"]);
                $seisekiResult = $db->query($seisekiQuery);
                $mock["HENSATI"] = "";
                
                $number = 0;
                
                while($seisekiRow = $seisekiResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    if($model->field["CHECK"][$number] == $seisekiRow["MOCK_SUBCLASS_CD"]){
                        //グラフ用
                        if($data[$seisekiRow["MOCK_SUBCLASS_CD"]] != ""){
                            $data[$seisekiRow["MOCK_SUBCLASS_CD"]] .= ",".$seisekiRow["DEVIATION"];
                        }else{
                            $data[$seisekiRow["MOCK_SUBCLASS_CD"]] = $seisekiRow["DEVIATION"] != "" ? $seisekiRow["DEVIATION"] : " ";
                        }
                    }else{
                        $in = array_search($seisekiRow["MOCK_SUBCLASS_CD"], $model->field["CHECK"]);
                        for($i=$number; $i<$in;$i++){
                            $mock["HENSATI"] .= "<td> </td>";
                            //グラフ用
                            if($data[$model->field["CHECK"][$i]] != ""){
                                $data[$model->field["CHECK"][$i]] .= ", ";
                            }else{
                                $data[$model->field["CHECK"][$i]] = " ";
                            }
                            $tdCnt++;
                        }
                        $number = $i;
                        if($data[$seisekiRow["MOCK_SUBCLASS_CD"]] != ""){
                            $data[$seisekiRow["MOCK_SUBCLASS_CD"]] .= ",".$seisekiRow["DEVIATION"];
                        }else{
                            $data[$seisekiRow["MOCK_SUBCLASS_CD"]] = $seisekiRow["DEVIATION"] != "" ? $seisekiRow["DEVIATION"] : " ";
                        }
                    }
                    
                    //最大値最小値取得用(nullは入れない)
                    if($seisekiRow["DEVIATION"] != ""){
                        $deviation[$dcnt] = $seisekiRow["DEVIATION"];
                        $dcnt++;
                    }
                    //作ったtdの数カウント
                    $tdCnt++;
                    $number++;

                    $mock["HENSATI"] .= "<td>".$seisekiRow["DEVIATION"]."</td>";
                    
                }
                if($tdCnt < $kyoukaCnt+1){
                    //作ったtdの数が足りなかったら足りない分空のtd作成
                    for($i=$tdCnt-1;$i<$kyoukaCnt;$i++){
                        $mock["HENSATI"] .= "<td></td>";
                        //グラフ用
                        if($data[$model->field["CHECK"][$i]] != ""){
                            $data[$model->field["CHECK"][$i]] .= ",";
                        }else{
                            $data[$model->field["CHECK"][$i]] = " ";
                        }
                    }
                }
                $arg["data2"][] = $mock;
            }
            if(!empty($deviation)){
                //偏差値の最大値と最小値取得
                $max = ceil(max($deviation));
                $min = floor(min($deviation));
            }else{
                $max = 50;
                $min = 20;
            }
            if($max%5 != 0){
                $model->data["YMAX"] = $max + 5-($max%5);
            }else{
                $model->data["YMAX"] = $max;
            }
            if($min%5 != 0){
                $model->data["YMIN"] = $min - ($min%5);
            }else{
                $model->data["YMIN"] = $min;
            }
            $sa = $model->field["MAX"] - $model->field["MIN"];
            if($sa > 24 && $sa < 55){
                $model->field["maxTicks"] = 11;
            }else{
                $model->field["maxTicks"] = 16;
            }
            
        }else{
            //該当データがない場合
            $model->field["MAX"] = 50;
            $model->field["MIN"] = 0;
            $model->field["maxTicks"] = 11;
            $colsp = $model->data["cnt"]+1;
            $mock["MOCKNAME"] .= "<td colspan=\"{$colsp}\" align=\"center\">該当データはありません</td>";
            $arg["data2"][] = $mock;
        }

        //データの配列番号を0からにする
        $sort = 0;
        foreach($data as $key => $val){
            $model->data[$sort] = $val;
            $sort++;
        }

        $model->data["TITLE"] = "";
        
        //グラフ用hidden作成
        graph::CreateDataHidden($objForm,$model->data,0);


        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        //View::toHTML($model, "knjz620Form1.html", $arg); 
        
        $jsplugin = "chart.js|Chart.min.js|graph.js";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjz620Form1.html", $arg, $jsplugin, $cssplugin);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    if ($blank == "GYOUSYA") {
        $opt[] = array ("label" => "全業者",
                        "value" => "0");
    }else if($blank == "SYUBETU"){
        $opt[] = array ("label" => "全模試",
                        "value" => "0");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　　
function makeCombo(&$objForm, &$arg, $array, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }


    foreach($array as $key => $val){
        //$val = str_pad($val,2,"0",STR_PAD_LEFT);
        $opt[] = array ("label" => $val,
                        "value" => $key);
        if ($value == $key) $value_flg = true;
    }

    //$value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //再表示
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('reappear');\"";
    $arg["button"]["btn_reappear"] = knjCreateBtn($objForm, "btn_reappear", "再表示", $extra);
    //英国数
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('sanka');\"";
    $arg["button"]["btn_sanka"] = knjCreateBtn($objForm, "btn_sanka", "英国数", $extra);
    //終了
    $extra = "onclick=\"closecheck();return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    
    knjCreateHidden($objForm, "type", "line");
    knjCreateHidden($objForm, "maxTicksLimit", $model->field["maxTicks"]);
}
?>
