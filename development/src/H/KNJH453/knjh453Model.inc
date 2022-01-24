<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh453Model extends Model {
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
    function knjh453Model()
    {
    }
    function init()
    {              
        $this->cmd      = VARS::request("cmd");
        
        $this->req_field = array("ANALYSIS"     =>  VARS::request("ANALYSIS") != "" ? VARS::request("ANALYSIS") : $this->req_field["ANALYSIS"],
    
                                 "SPORTS"       =>  VARS::request("SPORTS") != "" ? VARS::request("SPORTS") : $this->req_field["SPORTS"],
                                 
                                 "GYOUSYA"       =>  VARS::request("GYOUSYA") != "" ? VARS::request("GYOUSYA") : $this->req_field["GYOUSYA"],
                                 
                                 "KATA"         =>  VARS::request("KATA") != "" ? VARS::request("KATA") : $this->req_field["KATA"],
                                 
                                 "YEAR"         =>  VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->req_field["YEAR"],
                                );
        
        
        if(VARS::post("CHECK") != ""){
            $this->bottom_field["CHECK"] = VARS::request("CHECK");
        }else{
            if($this->req_field["KATA"] == "1"){
                $this->bottom_field["CHECK"] = array("100101", "100201", "100401");
            }else{
                if($this->req_field["GYOUSYA"] != "00000001"){
                    $this->bottom_field["CHECK"] = array("202001", "202101");
                }else{
                    $this->bottom_field["CHECK"] = "";
                }
            }
        }
        $this->bottom_field["SYUBETU"] = VARS::post("SYUBETU") != "" ? VARS::post("SYUBETU") : 0;

        if($this->cmd == "bottom_sanka" || $this->cmd == "open"){
            if($this->req_field["KATA"] == "1"){
                $this->bottom_field["CHECK"] = array("100101", "100201", "100401");
            }else{
                if($this->req_field["GYOUSYA"] != "00000001"){
                    $this->bottom_field["CHECK"] = array("202001", "202101");
                }else{
                    $this->bottom_field["CHECK"] = "";
                }
            }
        }else if($this->cmd == "edit"){     //最初に開いたときだけ
            $this->req_field = array();
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
        $query = knjh453Query::getJogai($this->req_field["SPORTS"]);
        $contents .= ",".$db->getOne($query);
        
        //期
        $query = knjh453Query::getNendo($this->req_field["YEAR"]);
        $selperiod = $db->getOne($query)."期";
        $contents .= ",".$selperiod;
        
        //業者名と模試名
        $query = knjh453Query::getGyousya($this->req_field["GYOUSYA"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $gyousyaName = $row["NAME1"];
        $contents .= ",".$row["NAME1"];
        
        //型
        $kata = array("", "教科", "受験型");
        $contents .= ",".$kata[$this->req_field["KATA"]];
        
        $contents .= "\n\n";


        //配列のキー作成
        if($this->req_field["ANALYSIS"] != "2"){
            if($this->req_field["GYOUSYA"] != "00000002"){ //駿台は60から40
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
            if($this->req_field["GYOUSYA"] != "00000002"){ //駿台は70から50
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
        
        //教科名取得
        $kyoukaQuery = knjh453Query::getKyouka($this->req_field["YEAR"],$this->req_field["GYOUSYA"], $this->bottom_field["SYUBETU"], $this->req_field["KATA"], $minhensa);
        
        $kyoukaResult = $db->query($kyoukaQuery);
        
        $kyouka = array();
        while($kyoukaRow = $kyoukaResult->fetchRow(DB_FETCHMODE_ASSOC)){
            if(in_array($kyoukaRow["MOCK_SUBCLASS_CD"], $this->bottom_field["CHECK"])){
                $kyouka[$kyoukaRow["MOCK_SUBCLASS_CD"]] = $kyoukaRow["SUBCLASS_ABBV"];
            }
        }
        

        $gyousya = substr($this->req_field["GYOUSYA"],-1);
        $hensatisum = array();
        $mockorder = array();
        
        $kyoukacnm = "";
        foreach($this->bottom_field["CHECK"] as $chkval){
            //データ1行目(教科名)
            $contents .= $kyoukacnm.$kyouka[$chkval]."\n";
            
            //データ2行目
            //模試取得したい
            $mosiQuery = knjh453Query::getMosi($this->req_field, $gyousya, $this->bottom_field["SYUBETU"], $chkval,"","",$minhensa);
            $mosiResult = $db->query($mosiQuery);
            $kamoku = array();
            $mosiCnt = 0;
            while($mosiRow = $mosiResult->fetchRow(DB_FETCHMODE_ASSOC)){
                //模試の表示順に配列に入れる
                $mockorder[$chkval][] = $mosiRow["MOCKCD"];
                $contents .= ",".$mosiRow["GRADE"]."年 ".$mosiRow["SINROSIDOU_MOSI_NAME"].",";
                $mosiCnt++;
            }
            $contents .= "\n";
            
            //データ3行目
            $contents .= "偏差値";
            for($i=0;$i<$mosiCnt;$i++){
                $contents .= ",単純";
                $contents .= ",累積";
            }
            $contents .= "\n";
            //偏差値ごとに人数取得
            $mosiData = array();
            $sum = array();
            for($i=$maxhensa;$i>$minhensa;$i=$i-$range){
                $from = $i;
                if($i != $maxhensa){
                    $to = $i+(int)$range;
                }else{
                    $to = "";
                }
                $cntQuery = knjh453Query::getMosi($this->req_field, $gyousya, $this->bottom_field["SYUBETU"], $chkval, $from, $to);
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
                //データ4行目以降
                $contents .= $key;
                while($cntRow = $cntResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $mosiData[$cntRow["MOCKCD"]][$i] = $cntRow["COUNT"];
                }
                foreach($mockorder[$chkval] as $val){
                    if(array_key_exists($val, $mosiData)){
                        $contents .= ",".$mosiData[$val][$i];
                        
                        $sum[$val] = (int)$sum[$val] + (int)$mosiData[$val][$i];
                        
                        $contents .= ",".$sum[$val];
                        
                    }else{
                        $contents .= ",";
                        
                        $contents .= ",".$sum[$val];
                        
                    }
                }
                $contents .= "\n";
            }
            
            
            //平均偏差値取得
            $avgQuery = knjh453Query::getAverage($this->req_field, $gyousya, $this->bottom_field["SYUBETU"], $chkval);
            $avgResult = $db->query($avgQuery);
            $contents .= "平均偏差値";
            while($avgRow = $avgResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $contents .= ",".sprintf("%0.1f", round($avgRow["AVG"],1)).",";
            }
            $contents .= "\n";
            
            $kyoukacnm = "\n\n";
        }


        $header_data = true;
        
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $name = "過回比較_".$selperiod.$gyousyaName;
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
