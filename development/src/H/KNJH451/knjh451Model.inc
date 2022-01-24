<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh451Model extends Model {
    var $field      = array();
    var $cmd;
    var $subclasscd;        //primary
    var $year_code;
    var $isload;
    var $tmp;				//NO001
    var $year;				//NO001
    
    /**
     * メンバ変数の初期化
     */
    function knjh451Model()
    {
    }
    function init()
    {              
        $this->cmd      = VARS::request("cmd");
        
        $this->req_field = array("ANALYSIS"     =>  VARS::request("ANALYSIS") != "" ? VARS::request("ANALYSIS") : $this->req_field["ANALYSIS"],
    
                                 "SPORTS"       =>  VARS::request("SPORTS") != "" ? VARS::request("SPORTS") : $this->req_field["SPORTS"],
                                 
                                 "MOCKCD"       =>  VARS::request("MOCKCD") != "" ? VARS::request("MOCKCD") : $this->req_field["MOCKCD"],
                                 
                                 "KATA"         =>  VARS::request("KATA") != "" ? VARS::request("KATA") : $this->req_field["KATA"],
                                 
                                 "YEAR"         =>  VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->req_field["YEAR"],
                                );
        
        
        $this->bottom_field = array("CHECK"         =>   VARS::post("CHECK") != "" ? VARS::post("CHECK") : array(),
                                    
                                    );
        if($this->cmd == "edit"){     //最初に開いたときだけ
            $this->req_field = array();
            $this->bottom_field = array();
        }
        if($this->cmd == "bottom_sanka" || empty($this->bottom_field["CHECK"])){
            if($this->req_field["KATA"] == "1"){
                $this->bottom_field["CHECK"] = array("100101", "100201", "100401");
            }else{
                if($this->req_field["GYOUSYA"] != "2"){
                    $this->bottom_field["CHECK"] = array("202001", "202101");
                }else{
                    $this->bottom_field["CHECK"] = array();
                }
            }
        }
    }
    
    
    //CSV出力
    function execCsv()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";
        
        //選択している上フレームの状態を表示したい
        //分析対象
        $analysis = array("", "全体分析", "上位分析");
        $contents .= $analysis[$this->req_field["ANALYSIS"]];
        
        //除外対象
        $query = knjh451Query::getJogai($this->req_field["SPORTS"]);
        $contents .= ",".$db->getOne($query);
        
        //期
        $query = knjh451Query::getNendo($this->req_field["YEAR"]);
        $period = $db->getOne($query)."期";
        $contents .= ",".$period;
        
        //年
        $grade = substr($this->req_field["MOCKCD"], 6, 1)."年";
        $contents .= ",".$grade;
        
        //業者名と模試名
        $query = knjh451Query::getMockName($this->req_field["MOCKCD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $mockname = $row["SINROSIDOU_MOSI_NAME"];
        $contents .= ",".$row["NAME1"].",".$row["SINROSIDOU_MOSI_NAME"];
        
        //型
        $kata = array("", "教科", "受験型");
        $contents .= ",".$kata[$this->req_field["KATA"]];
        
        $contents .= "\n\n";
        
        //教科名取得
        $kyoukaQuery = knjh451Query::getKyouka($this->req_field["MOCKCD"], $this->req_field["KATA"]);
        $kyoukaResult = $db->query($kyoukaQuery);
        
        while($kyoukaRow = $kyoukaResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $gaku[$kyoukaRow["MOCK_SUBCLASS_CD"]] = $kyoukaRow["SUBCLASS_ABBV"];
        }
        //データ1行目
        $contents .= "コース・教科,";
        $cnm = "";
        foreach($this->bottom_field["CHECK"] as $key => $val){
            $contents .= $cnm.$gaku[$val];
            
            $cnm = ",,";
        }
        $contents .= "\n";
        
        //データ2行目
        //模試データ取得しに行くために選択教科をSQL用に
        $choicekyouka = "";
        $ccnm = "";
        foreach($this->bottom_field["CHECK"] as $key => $val){
            $choicekyouka .= $ccnm.$val;
            $ccnm = "','";
        }
        
        $count = get_count($this->bottom_field["CHECK"]);
        $contents .= "偏差値(人数),";
        
        $cnm = "";
        for($i=0;$i<$count;$i++){
            $contents .= $cnm."単純";
            $contents .= ",累積";
            
            $cnm = ",";
        }
        $contents .= "\n";
        
        //データ3行目以降　データ
        $hensati_to = "";
        $sum = array();
        $deviation = array();
        
        //上位分析のときは偏差値50まででいい
        if($this->req_field["ANALYSIS"] != 2){
            $maxhensa = 80;
            $minhensa = 20;
            $range = 10;
        }else{
            $gyousyacd = mb_substr($this->req_field["MOCKCD"], 5, 1);
            if($gyousyacd == 2){
                $maxhensa = 70;
            }else{
                $maxhensa = 80;
            }
            $minhensa = 45;
            $range = 5;
        }

        //偏差値ごとにデータ取得
        for($hensati_from = $maxhensa; $hensati_from > $minhensa; $hensati_from=$hensati_from-$range){
            $deviation[$hensati_from] = array();
            
            if($hensati_from == 30){
                $hensati_from = "";
            }
            $devQuery = knjh451Query::getDevData($this->req_field, $choicekyouka,  $hensati_from, $hensati_to);
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
        foreach($this->bottom_field["CHECK"] as $key => $val){
            $sum[$val] = 0;
        }
        $hcnt = 0;
        for($hensati_from = $maxhensa; $hensati_from > $minhensa; $hensati_from=$hensati_from-$range){
            
            if($hensati_to != "" && $hensati_from != $maxhensa && $hensati_from != 30){
                $contents .= $hensati_from;
            }else if($hensati_from == $maxhensa){
                $contents .= $hensati_from."～";
            }else if($hensati_from == 30){
                $contents .= "～40";
            }
            $cnm = ",";
            
            foreach($this->bottom_field["CHECK"] as $key => $val){
                //$deviationにデータがあるか
                if(array_key_exists($val, $deviation[$hensati_from])){
                    
                    $contents .= $cnm.$deviation[$hensati_from][$val];
                    
                    $sum[$val] = (int)$sum[$val] + (int)$deviation[$hensati_from][$val];
                    
                    $contents .= $cnm.$sum[$val];
                    
                }else{
                    $contents .= $cnm;
                    
                    $contents .= $cnm.$sum[$val];
                }
            }
            
            $contents .= "\n";
        }
        
        //データ最終行
        $contents .= "平均偏差値,";
        //平均偏差値取得
        $avgQuery = knjh451Query::getAverage($this->req_field, $choicekyouka);
        $avgResult = $db->query($avgQuery);
        $basyo = 0;
        $cnm = "";
        while($avgRow = $avgResult->fetchRow(DB_FETCHMODE_ASSOC)){
            //データの個数合わせる
            $arraykey = array_search($avgRow["MOCK_SUBCLASS_CD"], $this->bottom_field["CHECK"]);
            if($arraykey > $basyo){
                for($i=$basyo;$i<$arraykey;$i++){
                    $contents .= $cnm;
                    $cnm = ",,";
                }
                $basyo = $arraykey;
            }
            
            $contents .= $cnm.sprintf("%.1f",round($avgRow["AVG"], 1));
            
            $cnm = ",,";
            
            $basyo++;
        }
        if($basyo != $count){
            for($i=$basyo;$i<$count;$i++){
                    $contents .= $cnm;
                    $cnm = ",,";
            }
        }
        
        $header_data = true;
        
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $name = "教科間分布_".$period.$grade.$mockname;
            $filename = sprintf("%s.csv",$name);
            common::downloadContents($contents, $filename);
            
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
    
}
?>
