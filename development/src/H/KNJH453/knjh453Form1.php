<?php

require_once('for_php7.php');

class knjh453Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh453index.php", "", "edit");

        $db = Query::dbCheckOut();
        if($model->req_field["GYOUSYA"] != ""){
            $arg["HYOUZI"] = 1;
        }else{
            $arg["HYOUZI"] = "";
        }
        
        //グラフ用
        $model->kmk_graph = array();
        $model->hns_graph = array();
        $lcnm = "";
        $hcnm = "";
        $graphdata = array();
        $hanrei = array();
        $kmk_graph = array();   //教科ごとグラフ用
        $hns_graph = array();   //偏差値ごとグラフ用
        
//エラーが出て面倒なので上から変数を受け取ったら中身作るようにする
if($arg["HYOUZI"] == 1){        

        //配列のキー作成
        if($model->req_field["ANALYSIS"] != "2"){
            if($model->req_field["GYOUSYA"] != "00000002"){ //駿台は60から40
                $maxhensa = 60;
                $minhensa = 30;
            }else{  //ベネッセは70から50
                $maxhensa = 70;
                $minhensa = 40;
            }
            $hrange = range($minhensa+10, $maxhensa-10, 10);
            krsort($hrange);
            $order1 = array("MOCKNM", "偏差値", "{$maxhensa}～");
            $order = array_merge($order1, $hrange);
            $range = 10;
        }else{
            if($model->req_field["GYOUSYA"] != "00000002"){ //駿台は70から50
                $maxhensa = 70;
                $minhensa = 45;
            }else{  //ベネッセは80から50
                $maxhensa = 80;
                $minhensa = 45;
            }
            $hrange = range($minhensa+5, $maxhensa-5, 5);
            krsort($hrange);
            $order1 = array("MOCKNM", "偏差値", "{$maxhensa}～");
            $order = array_merge($order1, $hrange);
            $range = 5;
        }
        
        //模試種別
        $syubetuQuery = knjh453Query::getSyubetu($model->req_field["GYOUSYA"]);
        $syubetuResult = $db->query($syubetuQuery);
        $opt[] = array();
        $extra = " onchange=\"btn_submit('open');\"";
        //初期値
        $opt[0] = array("label"  => "全種別",
                       "value"  => "0");
        while($syubetu = $syubetuResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label"  => $syubetu["NAME1"],
                           "value"  => $syubetu["NAMECD2"]);
        }
        $arg["SYUBETU"] = knjCreateCombo($objForm, "SYUBETU", $model->bottom_field["SYUBETU"], $opt, $extra, 1);

        
        //チェックボックス
        //教科名取得
        $kyoukaQuery = knjh453Query::getKyouka($model->req_field["YEAR"],$model->req_field["GYOUSYA"], $model->bottom_field["SYUBETU"], $model->req_field["KATA"], $minhensa);
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
        //教科名取得
        $kyoukaQuery = knjh453Query::getKyouka($model->req_field["YEAR"],$model->req_field["GYOUSYA"], $model->bottom_field["SYUBETU"], $model->req_field["KATA"], $minhensa);
        
        $kyoukaResult = $db->query($kyoukaQuery);
        
        $opt3 = array();
        $k=0;
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
        //$emptyflg = 0;  //CHECKが空だったら全部につけるためのフラグ
        if(empty($model->bottom_field["CHECK"])){
            $emptyflg = 0;  //20170210CHECKからだったら最初の一個だけにつけるように変更
        }else{
            $emptyflg = 1;
        }
        foreach($opt3 as $key => $val){
            $i++;
            if($i%10==1){
                $arg["CHECK"] .= "<tr>";
            }
            $extra = " id=\"".$key."\"";
            if(!empty($model->bottom_field["CHECK"]) && $emptyflg == 1){
                if(in_array($val["value"], $model->bottom_field["CHECK"])){
                    $extra .= " checked";
                    //偏差値ごとグラフの凡例
                    $hns_graph["hanrei"][] = $val["label"];
                    //$lcnm = ",";
                    $kyouka["KYOUKA"] = $val["label"];
                    $arg["data4"][] = $kyouka;
                }
            }else if(empty($model->bottom_field["CHECK"]) && $emptyflg == 0){   //20170210最初の一個目だけ通るように
                $extra .= " checked";
                //偏差値ごとグラフの凡例
                $hns_graph["hanrei"][] = $val["label"];
                //$lcnm = ",";
                $kyouka["KYOUKA"] = $val["label"];
                $arg["data4"][] = $kyouka;
                $model->bottom_field["CHECK"][$i-1] = $val["value"];
                //$emptyflg = 1;
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

        $jj=0;
        //表作成ループ
        $graphNo = 0;
        $gyousya = substr($model->req_field["GYOUSYA"],-1);
        $hensatisum = array();
        $mockorder = array();
        
        foreach((array)$model->bottom_field["CHECK"] as $chkkey => $chkval){
            $kyoukasum = array();
            //模試取得したい
            $mosiQuery = knjh453Query::getMosi($model->req_field, $gyousya, $model->bottom_field["SYUBETU"], $chkval,"","",$minhensa);
            $mosiResult = $db->query($mosiQuery);
            $kamoku = array();
            while($mosiRow = $mosiResult->fetchRow(DB_FETCHMODE_ASSOC)){
                //模試の表示順に配列に入れる
                $mockorder[$chkval][] = $mosiRow["MOCKCD"];
                $kamoku[$mosiRow["MOCKCD"]][$order[0]] = $mosiRow["GRADE"]."年<BR>".$mosiRow["SINROSIDOU_MOSI_NAME"];
            }
            
            //偏差値ごとに人数取得
            for($i=$maxhensa;$i>$minhensa;$i=$i-$range){
                $from = $i;
                if($i != $maxhensa){
                    $to = $i+(int)$range;
                }else{
                    $to = "";
                }
                $cntQuery = knjh453Query::getMosi($model->req_field, $gyousya, $model->bottom_field["SYUBETU"], $chkval, $from, $to);
                $cntResult = $db->query($cntQuery);
                if($i == $maxhensa){
                    $key = $i."～";
                    $bfr = "";
                }else{
                    $key = $i;
                    $bfr = $i+(int)$range;
                    if($bfr == $maxhensa){
                        $bfr = $bfr."～";
                    }
                }
                while($cntRow = $cntResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    //単純人数
                    $kamoku[$cntRow["MOCKCD"]][$key]["SINGLE"] = $cntRow["COUNT"];
                    
                    //累積人数
                    if($bfr != ""){
                        if($kamoku[$cntRow["MOCKCD"]][$bfr]["TOTAL"] != ""){
                            if($cntRow["COUNT"] != ""){
                                $kamoku[$cntRow["MOCKCD"]][$key]["TOTAL"] = (int)$kamoku[$cntRow["MOCKCD"]][$bfr]["TOTAL"] + (int)$cntRow["COUNT"];
                            }else{
                                $kamoku[$cntRow["MOCKCD"]][$key]["TOTAL"] = $kamoku[$cntRow["MOCKCD"]][$bfr]["TOTAL"];
                            }
                        }else{
                            $bbfr = (int)$bfr+(int)$range;
                            if($bbfr == $maxhensa){
                                $bbfr = $bbfr."～";
                            }
                            $kamoku[$cntRow["MOCKCD"]][$bfr]["TOTAL"] = $kamoku[$cntRow["MOCKCD"]][$bbfr]["TOTAL"];
                            
                            if($cntRow["COUNT"] != ""){
                                $kamoku[$cntRow["MOCKCD"]][$key]["TOTAL"] = (int)$kamoku[$cntRow["MOCKCD"]][$bfr]["TOTAL"] + (int)$cntRow["COUNT"];
                            }else{
                                $kamoku[$cntRow["MOCKCD"]][$key]["TOTAL"] = $kamoku[$cntRow["MOCKCD"]][$bfr]["TOTAL"];
                            }
                        }
                    }else{
                        $kamoku[$cntRow["MOCKCD"]][$key]["TOTAL"] = $cntRow["COUNT"];
                    }
                }
            }
            
            //表作成
            $model->kmk_graph = array();
            $kmk_graph["hanrei"] = array();
            $kmk_graph["label"] = array();
            foreach($order as $key => $val){
                if($key != 0){
                    $hyou["th"] = $val;
                    $hyou["td"] = "";
                }
                if($key >1){
                    $kmk_graph["hanrei"][] = $val;
                }
                if(!empty($mockorder[$chkval])){
                    foreach($mockorder[$chkval] as $key2 => $val2){
                        if($key == 0){
                            //模試名
                            $thmock["mockname"] = $kamoku[$val2][$val];
                            
                            $arg["data"][] = $thmock;
                            
                            //グラフ用
                            $kmk_graph["label"][] = str_replace("<BR>", " ", $kamoku[$val2][$val]);
                        }else if($key == 1){
                            $single = "単純";
                            $total = "累積";
                            
                            $hyou["td"] .= "<td class=\"no_search\" style=\"text-align:center;\" nowrap>".$single."</td>";
                            $hyou["td"] .= "<td class=\"no_search\" style=\"text-align:center;\" nowrap>".$total."</td>";
                        }else{
                            //表データ
                            $hyou["td"] .= "<td bgcolor=\"#ffffff\" style=\"text-align:center;\" nowrap>".$kamoku[$val2][$val]["SINGLE"]."</td>";
                            $hyou["td"] .= "<td bgcolor=\"#ffffff\" style=\"text-align:center;\" nowrap>".$kamoku[$val2][$val]["TOTAL"]."</td>";
                            
                            //グラフ用データ
                            $kmk_graph[$val][$key2] = $kamoku[$val2][$val]["TOTAL"];
                            
                            $kyoukasum[] = $kamoku[$val2][$val]["TOTAL"];
                        }
                    }
                }
                if($key != 0){
                    $arg["data2"][] = $hyou;
                }
            }
            
            
            
            //平均偏差値取得
            $avgQuery = knjh453Query::getAverage($model->req_field, $gyousya, $model->bottom_field["SYUBETU"], $chkval);
            $avgResult = $db->query($avgQuery);
            while($avgRow = $avgResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $avg["average"] = sprintf("%0.1f", round($avgRow["AVG"],1));
                $arg["data3"][] = $avg;
            }
            
            //教科ごとグラフ用凡例
            $hancnt = get_count($kmk_graph["hanrei"]);
            $hcnm = "";
            for($h=0;$h<$hancnt;$h++){
                $model->kmk_graph["hanrei"] .= $hcnm.$kmk_graph["hanrei"][$h];
                $hcnm = ",";
            }
            
            //ラベル
            $labelcnt = get_count($kmk_graph["label"]);
            $lcnm = "";
            for($h=0;$h<$labelcnt;$h++){
                $model->kmk_graph["label"] .= $lcnm.$kmk_graph["label"][$h];
                $lcnm = ",";
            }
            
            //データ
            $cnt = 0;
            for($i=$maxhensa;$i>$minhensa;$i=$i-$range){
                if($i==$maxhensa){
                    $key = $i."～";
                }else{
                    $key = $i;
                }
                $sort=0;
                $scnm = "";
                $data[$cnt] = "";
                for($j=0;$j<$labelcnt;$j++){
                    if($kmk_graph[$key][$j] != ""){
                        $data[$cnt] .= $scnm.$kmk_graph[$key][$j];
                    }else{
                        $data[$cnt] .= $scnm."0";
                    }
                    $scnm = ",";
                    $sort++;
                }
                $model->kmk_graph[$cnt] = $data[$cnt];
                $cnt++;
            }
            
            $model->kmk_graph["cnt"] = $hancnt;
            $model->kmk_graph["type"] = "";
            $tcnm = "";
            for($t=0;$t<$hancnt;$t++){
                $model->kmk_graph["type"] = $tcnm."line";
                $tcnm = ",";
            }
            
            $model->kmk_graph["TITLE"] = $hns_graph["hanrei"][$graphNo]."　累積分布グラフ";

            //人数の最大値取得
            if(!empty($kyoukasum)){
                $max = ceil(max($kyoukasum));
            }else{
                $max = 50;
            }
            if($max == 0){
                $max = 10;
            }

            if($max >= 100){
                if($max%50 != 0){
                    $model->kmk_graph["YMAX"] = $max + 50-($max%50);
                }else{
                    $model->kmk_graph["YMAX"] = $max;
                }
            }else{
                if($max%10 != 0){
                    $model->kmk_graph["YMAX"] = $max + 10-($max%10);
                }else{
                    $model->kmk_graph["YMAX"] = $max;
                }
            }
            
            $model->kmk_graph["YMIN"] = 0;
        
            //グラフ用hidden作成
            graph::CreateDataHidden($objForm,$model->kmk_graph,$graphNo);
            
            $grhno["GRAPHNO"] = $graphNo;
            $arg["data5"][] = $grhno;
            $graphNo++;
            
            //htmlでの表示用arg
            $arg["data4"][$jj]["data"] = $arg["data"];
            $arg["data4"][$jj]["data2"] = $arg["data2"];
            $arg["data4"][$jj]["data3"] = $arg["data3"];
            $jj++;
            
            //作ってきたやつリセット
            $arg["data"] = array();
            $arg["data2"] = array();
            $arg["data3"] = array();

        }
        
        //偏差値ごとグラフ作成
        //先に科目を','つなぎにしておく
        $choice = "";
        $ccnm = "";
        foreach((array)$model->bottom_field["CHECK"] as $chkkey => $chkval){
            $choice .= $ccnm.$chkval;
            $ccnm = "','";
        }
        //教科ごとグラフ用凡例
        $model->hns_graph["hanrei"] = "";
        $hancnt = get_count($hns_graph["hanrei"]);
        $hcnm = "";
        for($h=0;$h<$hancnt;$h++){
            $model->hns_graph["hanrei"] .= $hcnm.$hns_graph["hanrei"][$h];
            $hcnm = ",";
        }
        $gcnt = 0;
        for($i=$maxhensa;$i>$minhensa;$i=$i-$range){
            
            //模試名取得(ラベル)
            $mockQuery = knjh453Query::getMock($model->req_field, $i,$gyousya, $model->bottom_field["SYUBETU"],$model->req_field["YEAR"],$choice,"1");
            $mockResult = $db->query($mockQuery);
            
            $model->hns_graph["label"] = "";
            $lcnm = "";
            $roopMock = array();
            while($mockRow = $mockResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $model->hns_graph["label"] .= $lcnm.$mockRow["GRADE"]."年 ".$mockRow["SINROSIDOU_MOSI_NAME"];
                $lcnm = ",";
                
                $roopMock[] = $mockRow["MOCKCD"];
            }
            
            //偏差値ごとにデータ取得
            $dataQuery = knjh453Query::getMock($model->req_field, $i,$gyousya, $model->bottom_field["SYUBETU"],$model->req_field["YEAR"],$choice,"2");
            $dataResult = $db->query($dataQuery);
            
            $mdata = array();
            while($dataRow = $dataResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $mdata[$dataRow["MOCK_SUBCLASS_CD"]][$dataRow["MOCKCD"]] = $dataRow["COUNT"];
            }
            
            
            //データ作成
            $kmkCnt = 0;
            $hensatisum = array();
            foreach((array)$model->bottom_field["CHECK"] as $chkkey => $chkval){
                $model->hns_graph[$kmkCnt] = "";
                
                $hcnm = "";
                foreach($roopMock as $mkey => $mval){
                    if(is_array($mdata[$chkval]) && array_key_exists($mval, $mdata[$chkval])){
                        $model->hns_graph[$kmkCnt] .= $hcnm.$mdata[$chkval][$mval];
                        $hcnm = ",";
                        
                        $hensatisum[] = $mdata[$chkval][$mval];
                    }else{
                        if(!empty($mockorder[$chkval]) && in_array($mval, $mockorder[$chkval])){
                            $model->hns_graph[$kmkCnt] .= $hcnm."0";
                        }else{
                            $model->hns_graph[$kmkCnt] .= $hcnm;
                        }
                        $hcnm = ",";
                    }
                }
                $kmkCnt++;
            }
            
            
            $model->hns_graph["cnt"] = $hancnt;
            $model->hns_graph["type"] = "";
            $tcnm = "";
            for($t=0;$t<$hancnt;$t++){
                $model->hns_graph["type"] = $tcnm."line";
                $tcnm = ",";
            }
            $model->hns_graph["TITLE"] = str_replace("～","",$kmk_graph["hanrei"][$gcnt])."以上　累積過回比較";

            //人数の最大値取得
            if(!empty($hensatisum)){
                $max = ceil(max($hensatisum));
            }else{
                $max = 10;
            }

            if($max >= 100){
                if($max%50 != 0){
                    $model->hns_graph["YMAX"] = $max + 50-($max%50);
                }else{
                    $model->hns_graph["YMAX"] = $max;
                }
            }else{
                if($max%10 != 0){
                    $model->hns_graph["YMAX"] = $max + 10-($max%10);
                }else{
                    $model->hns_graph["YMAX"] = $max;
                }
            }
            
            $model->hns_graph["YMIN"] = 0;
        
            //グラフ用hidden作成
            graph::CreateDataHidden($objForm,$model->hns_graph,$graphNo);
            
            $grhno["GRAPHNO"] = $graphNo;
            $arg["data5"][] = $grhno;
            $graphNo++;
            $gcnt++;
        }
        
        //jsで何回ループさせるかに使う
        knjCreateHidden($objForm, "graph", $graphNo);
        
        $arg["cheight"] = 400;
        $arg["cwidth"] = 1200;
        
        if($model->req_field["ANALYSIS"] != 2){
            $arg["height"] = "210";
        }else{
            $arg["height"] = "310";
        }

}
        
        //$arg["link"] = REQUESTROOT."/Z/KNJh453_2/knjh453_2index.php";
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
        //View::toHTML($model, "knjh453Form1.html", $arg);
        
        
        $jsplugin = "chart.js|Chart.min.js|graph.js";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML6($model, "knjh453Form1.html", $arg, $jsplugin, $cssplugin);
    }
} 
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //英数国
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('bottom_sanka');\"";
    $arg["button"]["btn_sanka"] = knjCreateBtn($objForm, "btn_sanka", "英数国", $extra);
    //全選択
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('bottom_all');\"";
    $arg["button"]["btn_all"] = knjCreateBtn($objForm, "btn_all", "全選択", $extra);
    //再表示
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('bottom_reappear');\"";
    $arg["button"]["btn_reappear"] = knjCreateBtn($objForm, "btn_reappear", "再表示", $extra);
    
    //CSV出力
    $extra = " style=\"font-size:110%;\"";
    $extra .= " onclick=\"btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "type", "line");
    knjCreateHidden($objForm, "maxTicksLimit", 19);
}
?>