<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh452Model extends Model {
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
    function knjh452Model()
    {
    }
    function init()
    {              
        $this->getPropertiesAll(); //プロパティファイル

        $this->cmd      = VARS::request("cmd");
        
        $this->req_field = array("ANALYSIS"     =>  VARS::request("ANALYSIS") != "" ? VARS::request("ANALYSIS") : $this->req_field["ANALYSIS"],
    
                                 "SPORTS"       =>  VARS::request("SPORTS") != "" ? VARS::request("SPORTS") : $this->req_field["SPORTS"],
                                 
                                 "MOCKCD"       =>  VARS::request("MOCKCD") != "" ? VARS::request("MOCKCD") : $this->req_field["MOCKCD"],
                                 
                                 "KATA"         =>  VARS::request("KATA") != "" ? VARS::request("KATA") : $this->req_field["KATA"],
                                 
                                 "YEAR"         =>  VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->req_field["YEAR"],
                                );
        
        if(VARS::post("CHECK") != ""){
            $this->bottom_field["CHECK"] = VARS::request("CHECK");
        }else{
            if($this->req_field["KATA"] == "1"){
                $this->bottom_field["CHECK"] = array("100101", "100201", "100401");
            }else{
                if($this->req_field["GYOUSYA"] != "2"){
                    $this->bottom_field["CHECK"] = array("202001", "202101");
                }else{
                    $this->bottom_field["CHECK"] = "";
                }
            }
        }
        
        if($this->cmd == "bottom_sanka"){
            if($this->req_field["KATA"] == "1"){
                $this->bottom_field["CHECK"] = array("100101", "100201", "100401");
            }else{
                if($this->req_field["GYOUSYA"] != "2"){
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
        $query = knjh452Query::getJogai($this->req_field["SPORTS"]);
        $contents .= ",".$db->getOne($query);
        
        //期
        $query = knjh452Query::getNendo($this->req_field["YEAR"]);
        $selperiod = $db->getOne($query)."期";
        $contents .= ",".$selperiod;
        
        //年
        $grade = substr($this->req_field["MOCKCD"], 6, 1)."年";
        $contents .= ",".$grade;
        
        //業者名と模試名
        $query = knjh452Query::getMockName($this->req_field["MOCKCD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $mockname = $row["SINROSIDOU_MOSI_NAME"];
        $contents .= ",".$row["NAME1"].",".$row["SINROSIDOU_MOSI_NAME"];
        
        //型
        $kata = array("", "教科", "受験型");
        $contents .= ",".$kata[$this->req_field["KATA"]];
        
        $contents .= "\n\n";

        //教科名取得
        //4年度分のデータに含まれる教科全部出したい
        $mockBfr = substr($this->req_field["MOCKCD"], 0, 1);
        $mockYear = substr($this->req_field["MOCKCD"], 1, 4);
        $mockAft = substr($this->req_field["MOCKCD"],5,4);
        $cnm = "";
        $mockSearchYear = "";
        for($i=0;$i<4;$i++){
            $mockSearchYear .= $cnm.$mockBfr.($mockYear - $i).$mockAft;
            $cnm = "','";
        }
        $kyoukaQuery = knjh452Query::getKyouka($mockSearchYear, $this->req_field);
        $kyoukaResult = $db->query($kyoukaQuery);
        
        $kyouka = array();
        while($kyoukaRow = $kyoukaResult->fetchRow(DB_FETCHMODE_ASSOC)){
            if(in_array($kyoukaRow["MOCK_SUBCLASS_CD"], $this->bottom_field["CHECK"])){
                $kyouka[$kyoukaRow["MOCK_SUBCLASS_CD"]] = $kyoukaRow["SUBCLASS_ABBV"];
            }
        }
        
        //全教科共通で使うもの
        //期取得
        $fromprd = (int)$this->req_field["YEAR"] - 3;
        $periodQuery = knjh452Query::getPeriod($this->Properties, $fromprd, $this->req_field["YEAR"]);
        $periodResult = $db->query($periodQuery);
        
        $c_period = "";
        $cnm = "";
        //期の下の1行も同時に作る
        $th_hensa = "偏差値";
        while($periodRow = $periodResult->fetchRow(DB_FETCHMODE_ASSOC)){
            //期の一行
            $c_period .= $cnm.$periodRow["PRESENT_EST"]."期";
            
            $cnm = ",,";
            
            $th_hensa .= ",単純";
            $th_hensa .= ",累積";
        }
        
        $yto = mb_substr($this->req_field["MOCKCD"], 1, 4);
        $yfrom = (int)$yto - 3;
        
        for($i=0;$i<4;$i++){
            $period[$i] = (int)$yto - $i;
        }
        
        //偏差値の最大最小
        $gyousyacd = mb_substr($this->req_field["MOCKCD"], 5, 1);
        //上位分析のときは偏差値50まででいい
        if($this->req_field["ANALYSIS"] != 2){
            if($gyousyacd == 1){    //駿台のときは70から30
                $maxhensa = 70;
                $minhensa = 10;
            }else{                  //ベネッセは80から40
                $maxhensa = 80;
                $minhensa = 20;
            }
            $min = $minhensa+10;
            $range = 10;
        }else{
            if($gyousyacd == 1){    //駿台のときは70から50
                $maxhensa = 70;
            }else{                  //ベネッセは80から50
                $maxhensa = 80;
            }
            $minhensa = 45;
            $min = "";
            $range = 5;
        }
        $maxh = $maxhensa;
        
        $kyoukacnm = "";    //教科の前後用

        foreach($kyouka as $kyoukakey => $kyoukaval){
            $cnm = "";  //データ用
            
            //データ1行目(教科名)
            $contents .= $kyoukacnm.$kyoukaval."\n";
            
            //データ2行目(期)
            $contents .= ",".$c_period."\n";
            
            //データ3行目(偏差値)
            $contents .= $th_hensa."\n";
            
            
            $jj=0;
            //表作成ループ
                
            //ymax用
            $sum = array();
            $deviation = array();
            for($i=$maxhensa;$i>$minhensa;$i=$i-$range){
                
                $devfrom = $i;
                $devto = $devfrom + $range;
                
                //教科のコードで対象の模試のデータ取得
                $mockcd = mb_substr($this->req_field["MOCKCD"], 5, 4);
                $dataQuery = knjh452Query::getData($this->req_field, $mockcd, $kyoukakey, $yfrom, $yto, $devfrom, $devto, $min,$maxh);
                $dataResult = $db->query($dataQuery);
                while($dataRow = $dataResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $deviation[$devfrom][$dataRow["YEAR"]] = $dataRow["COUNT"];
                }
            }

            for($j=$maxhensa;$j>$minhensa;$j=$j-$range){
                //th部分先に作る
                if($j != $maxhensa && ($j != $minhensa+$range || $range != 10)){
                    $contents .= $j;
                }else if($j == $maxhensa){
                    $contents .= $maxhensa."～";
                }else if($j == $minhensa+$range && $range == 10){
                    $last = $minhensa+$range*2;
                    $contents .= "～".$last;
                }
                
                $perCnt = get_count($deviation);
                foreach($period as $key => $val){
                    //$deviationにデータがあるか
                    if(is_array($deviation[$j]) && array_key_exists($val, $deviation[$j])){
                        
                        $contents .= ",".$deviation[$j][$val];
                        
                        $sum[$val] = (int)$sum[$val] + (int)$deviation[$j][$val];
                    
                    }else{
                        $contents .= ",";
                    }
                    
                    $contents .= ",".$sum[$val];
                }
                
                
                $contents .= "\n";

            }
            
            //平均偏差値
            //年度ごとに平均値取得
            $avgQuery = knjh452Query::getAverage($this->req_field, $mockcd, $kyoukakey, $yfrom, $yto);
            $avgResult = $db->query($avgQuery);
            $avg = "";
            $average = array();
            while($avgRow = $avgResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $average[$avgRow["YEAR"]] = sprintf("%0.1f",round($avgRow["AVG"], 1));
            }
            $contents .= "平均偏差値";
            foreach($period as $key => $val){
                if(array_key_exists($val, $average)){
                    $contents .= ",".$average[$val].",";
                }else{
                    $contents .= ",,";
                }
            }
            $contents .= "\n";
            
            $kyoukacnm = "\n\n";
        }


        $header_data = true;
        
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $name = "過年度比較_".$selperiod.$grade.$mockname;
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
