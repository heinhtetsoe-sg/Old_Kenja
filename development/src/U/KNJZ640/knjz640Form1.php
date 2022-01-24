<?php

require_once('for_php7.php');

class knjz640Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz640index.php", "", "edit");

        $db = Query::dbCheckOut();
        if($model->req_field["MOCKCD"] != ""){
            $arg["HYOUZI"] = 1;
        }else{
            $arg["HYOUZI"] = "";
        }
        
        //グラフ用
        $model->btm_graph = array();
        $lcnm = "";
        $hcnm = "";
        $graphdata = array();
        $hanrei = array();

//エラーが出て面倒なので上から変数を受け取ったら中身作るようにする
if($arg["HYOUZI"] == 1){        
        //チェックボックス
        //教科名取得
        $kyoukaQuery = knjz640Query::getKyouka($model->req_field["MOCKCD"], $model->req_field["KATA"]);
        $kyoukaResult = $db->query($kyoukaQuery);
        
        //設定した初期値が含まれているかのチェック
        $ex_kyouka = array();
        while($kyouka = $kyoukaResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $ex_kyouka[$kyouka["MOCK_SUBCLASS_CD"]] = $kyouka["SUBCLASS_ABBV"];
        }

        $existflg = 0;
        if(!empty($model->bottom_field["CHECK"])){
            foreach($model->bottom_field["CHECK"] as $key => $value){
                if(array_key_exists($value, $ex_kyouka)){
                    $existflg = 1;
                }else{
                    unset($model->bottom_field["CHECK"][$key]);
                }
            }
            if($existflg != 1){
                $model->bottom_field["CHECK"] = array();
            }
        }
        //配列番号振りなおす
        $merge = array_merge($model->bottom_field["CHECK"]);
        $model->bottom_field["CHECK"] = array();
        $model->bottom_field["CHECK"] = $merge;
        

        $opt3 = array();
        $k=0;
        //教科名取得
        $kyoukaQuery = knjz640Query::getKyouka($model->req_field["MOCKCD"], $model->req_field["KATA"]);
        $kyoukaResult = $db->query($kyoukaQuery);
        
        while($kyoukaRow = $kyoukaResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt3[] = array("label"     =>  $kyoukaRow["SUBCLASS_ABBV"],
                            "value"     =>  $kyoukaRow["MOCK_SUBCLASS_CD"]);
                            
            if($model->cmd == "bottom_all"){
                $model->bottom_field["CHECK"][$k] = $kyoukaRow["MOCK_SUBCLASS_CD"];
                $k++;
            }
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
            if(!empty($model->bottom_field["CHECK"]) && $emptyflg != 1){
                if(in_array($val["value"], $model->bottom_field["CHECK"])){
                    $extra .= " checked";
                    //グラフ用
                    $model->btm_graph["label"] .= $lcnm.$val["label"];
                    $lcnm = ",";
                }
            }else{
                $extra .= " checked";
                //グラフ用
                $model->btm_graph["label"] .= $lcnm.$val["label"];
                $lcnm = ",";
                $model->bottom_field["CHECK"][$i-1] = $val["value"];
                $emptyflg = 1;
            }
            $arg["CHECK"] .= "\n<td>".knjCreateCheckBox($objForm, "CHECK[]", $val["value"], $extra)."<LABEL for=".$key.">".$val["label"]."</LABEL></td>";
            if($i%10==0){
                $arg["CHECK"] .= "</tr>\n";
            }
        }
        if($i%10!=0){
            $arg["CHECK"] .= "</tr>";
        }
        $arg["CHECK"] .= "</table>";

        //表の1行目のデータ作成
        //教科名
        foreach($opt3 as $key => $val){
            $gaku[$val["value"]] = $val["label"];
        }
        $kyouka = "";
        $cnm = "";
        foreach($model->bottom_field["CHECK"] as $key => $val){
            $thkyouka["kyouka"] = $gaku[$val];
            
            $arg["data"][] = $thkyouka;
            
            $kyouka .= $cnm.$val;
            $cnm = "','";
        }
        
        //模試データ取得しに行くために選択教科をSQL用に
        $choicekyouka = "";
        $ccnm = "";
        foreach($model->bottom_field["CHECK"] as $key => $val){
            $choicekyouka .= $ccnm.$val;
            $ccnm = "','";
        }
        
        $count = get_count($model->bottom_field["CHECK"]);
        $hyou["th"] = "偏差値(人数)";
        
        $single = "";
        $total = "";
        
        $hyou["td"] = "";
        for($i=0;$i<$count;$i++){
            $single = "単純";
            $total = "累積";
            
            $hyou["td"] .= "<td class=\"no_search\" style=\"text-align:center;\" nowrap>".$single."</td>";
            $hyou["td"] .= "<td class=\"no_search\" style=\"text-align:center;\" nowrap>".$total."</td>";
        }
        
        $arg["data2"][] = $hyou;
        
        $hensati_from = 80;
        $hensati_to = "";
        $sum = array();
        $deviation = array();
        
        //上位分析のときは偏差値50まででいい
        if($model->req_field["ANALYSIS"] != 2){
            $minhensa = 20;
        }else{
            $minhensa = 40;
        }
        
        //偏差値ごとにデータ取得
        for($hensati_from = 80; $hensati_from > $minhensa; $hensati_from=$hensati_from-10){
            $deviation[$hensati_from] = array();
            
            if($hensati_from == 30){
                $hensati_from = "";
            }
            $devQuery = knjz640Query::getDevData($model->req_field, $choicekyouka,  $hensati_from, $hensati_to);
            $devResult = $db->query($devQuery);

            if($hensati_from == ""){
                $hensati_from = 30;
            }
            
            while($devRow = $devResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $deviation[$hensati_from][$devRow["MOCK_SUBCLASS_CD"]] = $devRow["COUNT"];
            }
            
            $hensati_to = $hensati_from;
        }
        
        
        
        $hyou = array();
        foreach($model->bottom_field["CHECK"] as $key => $val){
            $sum[$val] = 0;
        }
        $hcnt = 0;
        for($hensati_from = 80; $hensati_from > $minhensa; $hensati_from=$hensati_from-10){
            $hyou["td"] = "";
            
            //th部分先に作る
            if($hensati_to != "" && $hensati_from != 80 && $hensati_from != 30){
                $hyou["th"] = $hensati_from;
            }else if($hensati_from == 80){
                $hyou["th"] = $hensati_from."～";
            }else if($hensati_from == 30){
                $hyou["th"] = "～40";
            }
            
            $hanrei[$hcnt] = $hyou["th"];
            $hcnt++;
            
            foreach($model->bottom_field["CHECK"] as $key => $val){
                //$deviationにデータがあるか
                if(array_key_exists($val, $deviation[$hensati_from])){
                    
                    $hyou["td"] .= "<td style=\"text-align:center;\" nowrap>".$deviation[$hensati_from][$val]."</td>";
                    
                    if($graphdata[$hensati_from] != ""){
                        $graphdata[$hensati_from] .= ",".$deviation[$hensati_from][$val];
                    }else{
                        $graphdata[$hensati_from] = $deviation[$hensati_from][$val];
                    }
                    
                    $sum[$val] = $sum[$val] + $deviation[$hensati_from][$val];
                    
                    $hyou["td"] .= "<td style=\"text-align:center;\" nowrap>".$sum[$val]."</td>";
                    
                }else{
                    $hyou["td"] .= "<td style=\"text-align:center;\" nowrap> </td>";
                    
                    if($graphdata[$hensati_from] != ""){
                        $graphdata[$hensati_from] .= ", ";
                    }else{
                        $graphdata[$hensati_from] = " ";
                    }
                    
                    if($sum[$val] > 0){
                        $hyou["td"] .= "<td style=\"text-align:center;\" nowrap>".$sum[$val]."</td>";
                    }else{
                        $hyou["td"] .= "<td style=\"text-align:center;\" nowrap></td>";
                    }
                }
            }
            
            $arg["data2"][] = $hyou;
        }
        
        //平均偏差値取得
        $avgQuery = knjz640Query::getAverage($model->req_field, $choicekyouka);
        $avgResult = $db->query($avgQuery);
        $basyo = 0;
        while($avgRow = $avgResult->fetchRow(DB_FETCHMODE_ASSOC)){
            //データの個数合わせる
            $arraykey = array_search($avgRow["MOCK_SUBCLASS_CD"], $model->bottom_field["CHECK"]);
            if($arraykey > $basyo){
                for($i=$basyo;$i<$arraykey;$i++){
                    $avg["average"] = " ";
                    
                    $arg["data3"][] = $avg;
                }
                $basyo = $arraykey;
            }
            
            $avg["average"] = sprintf("%.1f",round($avgRow["AVG"], 1));
            
            $arg["data3"][] = $avg;
            $basyo++;
        }
        if($basyo != $count){
            for($i=$basyo;$i<$count;$i++){
                $avg["average"] = " ";
                
                $arg["data3"][] = $avg;
            }
        }
        
        //グラフ用データキー変更
        $sort = 0;
        $sortdata = array();
        foreach($graphdata as $key => $val){
            $sortdata[$sort] = $val;
            $sort++;
        }
        //並び替える
        $scnt = get_count($sortdata);
        $sort = 0;
        for($s=$scnt;$s>0;$s--){
            $model->btm_graph[$sort] = $sortdata[$s-1];
            $sort++;
        }
        //グラフ用凡例並び替え
        $hancnt = get_count($hanrei);
        $hcnm = "";
        for($h=$hancnt;$h>0;$h--){
            $model->btm_graph["hanrei"] .= $hcnm.$hanrei[$h-1];
            $hcnm = ",";
        }
        
        //グラフ
        $model->btm_graph["cnt"] = $hancnt;
        
        $model->btm_graph["TITLE"] = "単純分布グラフ";

        //人数の最大値と最小値取得
        if($basyo != 0){
            $max = ceil(max($sum));
            
            if($max%50 != 0){
                $model->btm_graph["YMAX"] = $max + 50-($max%50);
            }else{
                $model->btm_graph["YMAX"] = $max;
            }
        }else{
                $model->btm_graph["YMAX"] = 50;
        }
        
        $model->btm_graph["YMIN"] = 0;


        
        //グラフ用hidden作成
        graph::CreateDataHidden($objForm,$model->btm_graph,0);
}
        
        //$arg["link"] = REQUESTROOT."/Z/KNJz640_2/knjz640_2index.php";
        Query::dbCheckIn($db);
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd
                            ) );
        
        //hidden作成
        makeHidden($objForm, $model);
        
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        //View::toHTML($model, "knjz640Form1.html", $arg);
        
        
        $jsplugin = "chart.js|Chart.min.js|graph.js";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjz640Form1.html", $arg, $jsplugin, $cssplugin);
    }
} 
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //英国数
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('bottom_sanka');\"";
    $arg["button"]["btn_sanka"] = knjCreateBtn($objForm, "btn_sanka", "英国数", $extra);
    //英国数
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('bottom_all');\"";
    $arg["button"]["btn_all"] = knjCreateBtn($objForm, "btn_all", "全選択", $extra);
    //再表示
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('bottom_reappear');\"";
    $arg["button"]["btn_reappear"] = knjCreateBtn($objForm, "btn_reappear", "再表示", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "type", "stacked");
    knjCreateHidden($objForm, "maxTicksLimit", 11);
}
?>
