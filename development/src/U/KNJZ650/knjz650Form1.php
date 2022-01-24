<?php

require_once('for_php7.php');

class knjz650Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz650index.php", "", "edit");

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
        $kyoukaQuery = knjz650Query::getKyouka($model->req_field["MOCKCD"], $model->req_field["KATA"]);
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
        $kyoukaQuery = knjz650Query::getKyouka($model->req_field["MOCKCD"], $model->req_field["KATA"]);
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
                    $graph_kyouka[] = $val["label"];
                    //$lcnm = ",";
                    $kyouka["KYOUKA"] = $val["label"];
                    $arg["data4"][] = $kyouka;
                }
            }else{
                $extra .= " checked";
                //グラフ用
                $graph_kyouka[] = $val["label"];
                //$lcnm = ",";
                $kyouka["KYOUKA"] = $val["label"];
                $arg["data4"][] = $kyouka;
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
        //学校名取得
        $gakunameQuery = knjz650Query::getGakName($model->req_field["MOCKCD"]);
        $gakuResult = $db->query($gakunameQuery);
        $model->btm_graph["label"] = "";
        $cnm = "";
        $gakkoCnt = 0;
        
        while($gakuRow = $gakuResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $thgakko["gakko"] = $gakuRow["GAKKONAME"];
            
            $arg["data"][] = $thgakko;
            
            $model->btm_graph["label"] .= $cnm.$gakuRow["GAKKONAME"];
            $cnm = ",";
            
            $gakkoCnt++;
            
        }
        
        $jj=0;
        //表作成ループ
        $graphNo = 0;
        foreach($model->bottom_field["CHECK"] as $chkkey => $chkval){
            
            $hyou["th"] = "偏差値";
            
            $single = "";
            $total = "";
            
            $hyou["td"] = "";
            for($i=0;$i<$gakkoCnt;$i++){        //学校分繰り返す
                $single = "単純";
                $total = "累積";
                
                $hyou["td"] .= "<td class=\"no_search\" style=\"text-align:center;\" nowrap>".$single."</td>";
                $hyou["td"] .= "<td class=\"no_search\" style=\"text-align:center;\" nowrap>".$total."</td>";
            }
            
            $arg["data2"][] = $hyou;
            
            
            //教科のコードで対象の模試のデータ取得
            $dataQuery = knjz650Query::getData($model->req_field["MOCKCD"], $chkval);
            $dataResult = $db->query($dataQuery);
            while($dataRow = $dataResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $data[] = $dataRow;
            }

            //上位分析のときは偏差値50まででいい
            if($model->req_field["ANALYSIS"] != 2){
                $minhensa = 10;
            }else{
                $minhensa = 40;
            }
            
            $hcnt = 0;
            //ymax用
            $sum = array();
            for($i=80;$i>$minhensa;$i=$i-10){
                //th部分先に作る
                if($i != 80 && $i != 20){
                    $hyou["th"] = $i;
                }else if($i == 80){
                    $hyou["th"] = "80～";
                }else if($i == 20){
                    $hyou["th"] = "～30";
                }
                //グラフ凡例
                $hanrei[$hcnt] = $hyou["th"];
                $hcnt++;
                
                $hyou["td"] = "";
                
                $graphdata[$i] = "";
                $dcnm = "";
                
                
                for($j=0;$j<$gakkoCnt;$j++){
                    $hyou["td"] .= "<td style=\"text-align:center;\" nowrap>".$data[$j]["SIMPLE_{$i}"]."</td><td style=\"text-align:center;\" nowrap>".$data[$j]["TOTAL_{$i}"]."</td>";
                    
                    if($model->req_field["ANALYSIS"] != "2"){
                        $graphdata[$i] .= $dcnm.$data[$j]["SIMPLE_{$i}"];
                        
                        $sum[] = $data[$j]["SIMPLE_{$i}"];
                    }else{
                        $graphdata[$i] .= $dcnm.$data[$j]["TOTAL_{$i}"];
                        
                        $sum[] = $data[$j]["TOTAL_{$i}"];
                    }
                    $dcnm = ",";
                }
                $arg["data2"][] = $hyou;
            }
            //平均偏差値
            $avg = "";
            for($j=0;$j<$gakkoCnt;$j++){
                $avg["average"] = sprintf("%.1f",$data[$j]["AVG_DEVIATION"]);
                
                $arg["data3"][] = $avg;
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
            for($s=0;$s<$scnt;$s++){
                $model->btm_graph[$sort] = $sortdata[$s];
                $sort++;
            }
            //グラフ用凡例並び替え
            $hancnt = get_count($hanrei);
            $hcnm = "";
            for($h=0;$h<$hancnt;$h++){
                $model->btm_graph["hanrei"] .= $hcnm.$hanrei[$h];
                $hcnm = ",";
            }
            
            //グラフ
            $model->btm_graph["cnt"] = $hancnt;
            
            if($model->req_field["ANALYSIS"] != "2"){
                $model->btm_graph["TITLE"] = $graph_kyouka[$graphNo]."　単純分布グラフ";
            }else{
                $model->btm_graph["TITLE"] = $graph_kyouka[$graphNo]."　累積分布グラフ";
            }

            //人数の最大値取得
            $max = ceil(max($sum));

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
            
            //htmlでの表示用arg
            $arg["data4"][$jj]["data"] = $arg["data"];
            $arg["data4"][$jj]["data2"] = $arg["data2"];
            $arg["data4"][$jj]["data3"] = $arg["data3"];
            $jj++;
            
            //作ってきたやつリセット
            $arg["data2"] = array();
            $arg["data3"] = array();
            $data = array();
            $model->btm_graph["hanrei"] = "";
        }
        
        //jsで何回ループさせるかに使う
        knjCreateHidden($objForm, "graph", $graphNo);
        
        $arg["cheight"] = 250;
        $arg["cwidth"] = 750;

}
        
        //$arg["link"] = REQUESTROOT."/Z/KNJz650_2/knjz650_2index.php";
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
        //View::toHTML($model, "knjz650Form1.html", $arg);
        
        
        $jsplugin = "chart.js|Chart.min.js|graph.js";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjz650Form1.html", $arg, $jsplugin, $cssplugin);
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
    knjCreateHidden($objForm, "type", "bar");
    knjCreateHidden($objForm, "maxTicksLimit", 19);
}
?>
