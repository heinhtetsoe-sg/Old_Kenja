<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh454Model extends Model {
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
    function knjh454Model()
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
        
        
        if(VARS::post("CHECK") != ""){
            $this->bottom_field["CHECK"] = VARS::request("CHECK");
        }else{
            if($this->req_field["KATA"] == "1"){
                $this->bottom_field["CHECK"] = array("100101", "100201", "100401");
            }else{
                $this->bottom_field["CHECK"] = array("202001", "202101");
            }
        }
                                    
        if($this->cmd == "bottom_sanka"){
            if($this->req_field["KATA"] == "1"){
                $this->bottom_field["CHECK"] = array("100101", "100201", "100401");
            }else{
                $this->bottom_field["CHECK"] = array("202001", "202101");
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
        $query = knjh454Query::getJogai($this->req_field["SPORTS"]);
        $contents .= ",".$db->getOne($query);
        
        //期
        $query = knjh454Query::getNendo($this->req_field["YEAR"]);
        $selperiod = $db->getOne($query)."期";
        $contents .= ",".$selperiod;
        
        //年
        $grade = substr($this->req_field["MOCKCD"], 6, 1)."年";
        $contents .= ",".$grade;
        
        //業者名と模試名
        $query = knjh454Query::getMockName($this->req_field["MOCKCD"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $mockname = $row["SINROSIDOU_MOSI_NAME"];
        $contents .= ",".$row["NAME1"].",".$row["SINROSIDOU_MOSI_NAME"];
        
        //型
        $kata = array("", "教科", "受験型");
        $contents .= ",".$kata[$this->req_field["KATA"]];
        
        $contents .= "\n\n";


        //教科名取得
        $kyoukaQuery = knjh454Query::getKyouka($this->req_field["MOCKCD"], $this->req_field["KATA"]);
        $kyoukaResult = $db->query($kyoukaQuery);
        
        $kyouka = array();
        while($kyoukaRow = $kyoukaResult->fetchRow(DB_FETCHMODE_ASSOC)){
            if(in_array($kyoukaRow["MOCK_SUBCLASS_CD"], $this->bottom_field["CHECK"])){
                $kyouka[$kyoukaRow["MOCK_SUBCLASS_CD"]] = $kyoukaRow["SUBCLASS_ABBV"];
            }
        }

        //表の1行目のデータ作成
        //学校名取得
        $gakunameQuery = knjh454Query::getGakName($this->req_field["MOCKCD"]);
        $gakuResult = $db->query($gakunameQuery);
        $cnm = "";
        $gakkoCnt = 0;
        
        //位置調整用にSCHOOLCDを保持
        $gakucd = array();
        $gakkoName = "";
        while($gakuRow = $gakuResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $gakkoName .= ",".$gakuRow["GAKKONAME"].",";
            
            $gakucd[$gakkoCnt] = $gakuRow["GAKKOCODE"];
            
            $gakkoCnt++;
            
        }
        //表の2行目用データ
        $th_hensa = "偏差値";
        for($i=0;$i<$gakkoCnt;$i++){        //学校分繰り返す
            $th_hensa .= ",単純";
            $th_hensa .= ",累積";
        }
        $contents .= "\n";
        //上位分析のときは偏差値50まででいい
        if($this->req_field["ANALYSIS"] != 2){
            $minhensa = 10;
            $range = 10;
        }else{
            $minhensa = 45;
            $range = 5;
        }
        
        $kyoukacnm = "";
        foreach($this->bottom_field["CHECK"] as $chkkey => $chkval){
            //データ1行目(教科名)
            $contents .= $kyoukacnm.$kyouka[$chkval]."\n";
            
            //データ2行目(学校名)
            $contents .= $gakkoName."\n";
            
            //データ3行目(偏差値)
            $contents .= $th_hensa."\n";
            
            
            //教科のコードで対象の模試のデータ取得
            $dataQuery = knjh454Query::getData($this->req_field["MOCKCD"], $this->req_field["ANALYSIS"], $chkval);
            $dataResult = $db->query($dataQuery);
            $getDataCnt = 0;
            while($dataRow = $dataResult->fetchRow(DB_FETCHMODE_ASSOC)){
                //表示位置を調整するために配列番号を学校名の順番とそろえる
                if($gakucd[$getDataCnt] != $dataRow["GAKKOCODE"]){
                    $gakuPlace = array_search($dataRow["GAKKOCODE"], $gakucd);
                    for($g=$getDataCnt; $g<$gakuPlace; $g++){
                        $data[$g] = array();
                    }
                    $getDataCnt = $g;
                }
                $data[$getDataCnt] = $dataRow;
                $getDataCnt++;
            }

            
            $hcnt = 0;
            for($i=80;$i>$minhensa;$i=$i-$range){
                //th部分先に作る
                if($i != 80 && $i != 20){
                    $contents .= $i;
                }else if($i == 80){
                    $contents .= "80～";
                }else if($i == 20){
                    $contents .= "～30";
                }
                
                
                for($j=0;$j<$gakkoCnt;$j++){
                    $contents .= ",".$data[$j]["SIMPLE_{$i}"].",".$data[$j]["TOTAL_{$i}"];
                    
                }
                $contents .= "\n";
            }
            
            //平均偏差値
            $contents .= "平均偏差値";
            for($j=0;$j<$gakkoCnt;$j++){
                if($data[$j]["AVG_DEVIATION"] != ""){
                    $contents .= ",".sprintf("%.1f",$data[$j]["AVG_DEVIATION"]).",";
                }else{
                    $contents .= ",,";
                }
            }
            $cotents .= "\n";
            
            $kyoukacnm = "\n\n";
        }

        $header_data = true;
        
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $name = "他校比較_".$selperiod.$grade.$mockname;
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
