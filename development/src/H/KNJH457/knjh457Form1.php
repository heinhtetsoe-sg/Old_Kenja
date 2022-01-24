<?php

require_once('for_php7.php');

class knjh457Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh457index.php", "", "edit");

        $db = Query::dbCheckOut();
        if($model->req_field["GYOUSYA"] != ""){
            $arg["HYOUZI"] = 1;
        }else{
            $arg["HYOUZI"] = "";
        }
        if($arg["HYOUZI"] != "" && $model->cmd != "syubetu_change" && $model->bottom_field["KYOUKA"] != ""){
            $arg["HYOUZI2"] = 1;
        }else{
            $arg["HYOUZI2"] = "";
        }
        
        //グラフ用
        $model->btm_graph = array();
        $lcnm = "";
        $hcnm = "";
        $graphdata = array();
        $hanrei = array();
        $kmk_graph = array();   //教科ごとグラフ用
        $hns_graph = array();   //偏差値ごとグラフ用
        
//エラーが出て面倒なので上から変数を受け取ったら中身作るようにする
if($arg["HYOUZI"] == 1){        

        //偏差値の幅設定
        if($model->req_field["ANALYSIS"] != "2"){
            if($model->req_field["GYOUSYA"] != "00000001"){
                $maxhensa = 70;
                $minhensa = 40;
                $range = 10;
            }else{
                $maxhensa = 60;
                $minhensa = 30;
                $range = 10;
            }
        }else{
            if($model->req_field["GYOUSYA"] != "00000001"){
                $maxhensa = 80;
                $minhensa = 45;
                $range = 5;
            }else{
                $maxhensa = 70;
                $minhensa = 45;
                $range = 5;
            }
        }

        //模試種別
        $syubetuQuery = knjh457Query::getSyubetu($model->req_field["GYOUSYA"]);
        $syubetuResult = $db->query($syubetuQuery);
        $opt[] = array();
        $extra = " onchange=\"btn_submit('syubetu_change');\"";
        //初期値
        $opt[0] = array("label"  => "全種別",
                       "value"  => "0");
        while($syubetu = $syubetuResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label"  => $syubetu["NAME1"],
                           "value"  => $syubetu["NAMECD2"]);
        }
        $arg["SYUBETU"] = knjCreateCombo($objForm, "SYUBETU", $model->bottom_field["SYUBETU"], $opt, $extra, 1);

        
        //コンボボックス
        //教科名取得
        $kyoukaQuery = knjh457Query::getKyouka($model->req_field["YEAR"],$model->req_field["GYOUSYA"], $model->bottom_field["SYUBETU"], $model->req_field["KATA"], $minhensa);
        $kyoukaResult = $db->query($kyoukaQuery);
        $opt2[] = array();
        $extra = "";
        while($kyoukaRow = $kyoukaResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt2[] = array("label"     =>  $kyoukaRow["SUBCLASS_ABBV"],
                            "value"     =>  $kyoukaRow["MOCK_SUBCLASS_CD"]);
        }
        $arg["KYOUKA"] = knjCreateCombo($objForm, "KYOUKA", $model->bottom_field["KYOUKA"], $opt2, $extra, 1);
        

    if($arg["HYOUZI2"] != ""){
        //期を取得したい
        $bfryear = (int)$model->req_field["YEAR"] - 2;
        $nextyear = (int)$model->req_field["YEAR"] + 2;
        
        $periodQuery = knjh457Query::getPeriod($model->Properties, $bfryear, $nextyear);
        $periodResult = $db->query($periodQuery);
        
        $period = array();
        $periodCnt = 0;
        $model->btm_graph["hanrei"] = "";
        $hcnm = "";
        while($periodRow = $periodResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $period[$periodCnt]["YEAR"] = $periodRow["YEAR"];
            $period[$periodCnt]["EST"] = $periodRow["PRESENT_EST"];
            
            //グラフ凡例
            $model->btm_graph["hanrei"] .= $hcnm.$periodRow["PRESENT_EST"]."期";
            $hcnm = ",";
            
            $periodCnt++;
        }
        //グラフ用
        $model->btm_graph["cnt"] = $periodCnt;
        $model->btm_graph["type"] = "";
        $tcnm = "";
        for($i=0;$i<$periodCnt;$i++){
            $model->btm_graph["type"] .= $tcnm."line";
            $tcnm = ",";
        }
        
        //模試取得
        $gyousya = substr($model->req_field["GYOUSYA"], -1);
        $mosiQuery = knjh457Query::getMockName($model->req_field, $model->req_field["YEAR"], $gyousya, $model->bottom_field["SYUBETU"], $model->bottom_field["KYOUKA"],$minhensa);
        $mosiResult = $db->query($mosiQuery);
        $kamoku = array();
        $model->btm_graph["label"] = "";
        $lcnm = "";
        $mcnt = 0;
        while($mosiRow = $mosiResult->fetchRow(DB_FETCHMODE_ASSOC)){
            //模試の表示順に配列に入れる
            $mockorder[$mcnt]["MOCKCD"] = $mosiRow["MOCKCD"];
            $mockorder[$mcnt]["MOCKNAME"] = $mosiRow["GRADE"]."年<BR>".$mosiRow["SINROSIDOU_MOSI_NAME"];
            
            //表用
            $thmock["mockname"] = $mosiRow["GRADE"]."年<BR>".$mosiRow["SINROSIDOU_MOSI_NAME"];
            
            $arg["data"][] = $thmock;
            
            //グラフ用
            $model->btm_graph["label"] .= $lcnm.$mosiRow["GRADE"]."年 ".$mosiRow["SINROSIDOU_MOSI_NAME"];
            $lcnm = ",";
            
            $mcnt++;
        }
        
        
        
        $jj=0;
        //表作成ループ
        $graphNo = 0;
        $gyousya = substr($model->req_field["GYOUSYA"],-1);
        
        //foreach($model->bottom_field["CHECK"] as $chkkey => $chkval){
            
            //偏差値ごとに人数取得
            for($i=$maxhensa;$i>=$minhensa;$i=$i-$range){
                $sum = array();
                //このプログラムのときだけthから全部入れる
                if($i != $minhensa){
                    $hyou["th1"] = "<th class=\"no_search\"  align=\"center\" nowrap rowspan=\"{$model->btm_graph["cnt"]}\">".$i."以上<BR>累積数</th>";
                }else{
                    $hyou["th1"] = "<th class=\"no_search\"  align=\"center\" nowrap rowspan=\"{$model->btm_graph["cnt"]}\">平均<BR>偏差値</th>";
                }
                $pCnt = 0;
                foreach($period as $key => $val){   //期ごと
                    $model->btm_graph[$pCnt] = "";
                    $gdcnm = "";
                    
                    $hyou["th2"] = "<th class=\"no_search\"  align=\"center\" nowrap>".$val["EST"]."期</th>";
                    $hyou["td"] = "";
                    if($i != $minhensa){
                        $cntQuery = knjh457Query::getMosi($model->req_field, $val["YEAR"], $gyousya, $model->bottom_field["SYUBETU"], $model->bottom_field["KYOUKA"], $i);
                    }else{
                        $cntQuery = knjh457Query::getAverage($model->req_field, $val["YEAR"], $gyousya, $model->bottom_field["SYUBETU"], $model->bottom_field["KYOUKA"]);
                    }
                    $cntResult = $db->query($cntQuery);
                    while($cntRow = $cntResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        if($i != $minhensa){
                            //人数
                            $cntData[$val["EST"]][$cntRow["MOCKCD"]] = $cntRow["COUNT"];
                        }else{
                            //平均偏差値
                            $cntData[$val["EST"]][$cntRow["MOCKCD"]] = sprintf("%0.1f", round($cntRow["AVG"],2));
                        }
                    }

                    
                    //表のデータとグラフのデータを作る
                    if(!empty($mockorder)){
                        foreach($mockorder as $mockkey => $mockval){
                            if(is_array($cntData[$val["EST"]]) && array_key_exists($mockval["MOCKCD"], $cntData[$val["EST"]])){
                                $hyou["td"] .= "<td bgcolor=\"#ffffff\" style=\"text-align:center;\" nowrap>".$cntData[$val["EST"]][$mockval["MOCKCD"]]."</td>";
                                $sum[] = $cntData[$val["EST"]][$mockval["MOCKCD"]];
                                $model->btm_graph[$pCnt] .= $gdcnm.$cntData[$val["EST"]][$mockval["MOCKCD"]];
                            }else{
                                $hyou["td"] .= "<td bgcolor=\"#ffffff\" style=\"text-align:center;\" nowrap> </td>";
                                
                                $model->btm_graph[$pCnt] .= $gdcnm."0";
                            }
                            $gdcnm = ",";
                        }
                    }
                    //表のデータ入れる
                    $arg["data2"][] = $hyou;
                    //最初だけ作りたいから消してみる
                    $hyou["th1"] = "";
                    $pCnt++;
                }
                
                //表作成
                //htmlでの表示用arg
                $arg["data4"][$jj]["data"] = $arg["data"];
                $arg["data4"][$jj]["data2"] = $arg["data2"];
                $jj++;
                $model->btm_graph["TITLE"] = $i."以上累積数";

                
                //グラフ作成
                if($i != $minhensa){
                    //人数の最大値取得
                    if(!empty($sum)){
                        $max = ceil(max($sum));
                    }else{
                        $max = 10;
                    }
                    if($max == 0){
                        $max = 10;
                    }

                    if($max >= 100){
                        if($max%50 != 0){
                            $model->btm_graph["YMAX"] = $max + 50-($max%50);
                        }else{
                            $model->btm_graph["YMAX"] = $max;
                        }
                    }else{
                        if($max%10 != 0){
                            $model->btm_graph["YMAX"] = $max + 10-($max%10);
                        }else{
                            $model->btm_graph["YMAX"] = $max;
                        }
                    }
                    
                    $model->btm_graph["YMIN"] = 0;
                
                    //グラフ用hidden作成
                    graph::CreateDataHidden($objForm,$model->btm_graph,$graphNo);
                    
                    $grhno["GRAPHNO"] = $graphNo;
                    $arg["data5"][] = $grhno;
                    $graphNo++;
                }
                
                //作ってきたやつリセット
                $arg["data2"] = array();
            }

        //}
        //jsで何回ループさせるかに使う
        knjCreateHidden($objForm, "graph", $graphNo);
        $arg["cheight"] = 400;
        $arg["cwidth"] = 1200;
    }
}
        
        //$arg["link"] = REQUESTROOT."/Z/KNJh457_2/knjh457_2index.php";
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
        //View::toHTML($model, "knjh457Form1.html", $arg);
        
        
        $jsplugin = "chart.js|Chart.min.js|graph.js";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML6($model, "knjh457Form1.html", $arg, $jsplugin, $cssplugin);
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
