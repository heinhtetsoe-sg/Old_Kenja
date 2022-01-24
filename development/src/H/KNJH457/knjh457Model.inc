<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh457Model extends Model {
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
    function knjh457Model()
    {
    }
    function init()
    {              
        $this->getPropertiesAll(); //プロパティファイル

        $this->cmd      = VARS::request("cmd");
        
        $this->req_field = array("ANALYSIS"     =>  VARS::request("ANALYSIS") != "" ? VARS::request("ANALYSIS") : $this->req_field["ANALYSIS"],
    
                                 "SPORTS"       =>  VARS::request("SPORTS") != "" ? VARS::request("SPORTS") : $this->req_field["SPORTS"],
                                 
                                 "GYOUSYA"       =>  VARS::request("GYOUSYA") != "" ? VARS::request("GYOUSYA") : $this->req_field["GYOUSYA"],
                                 
                                 "KATA"         =>  VARS::request("KATA") != "" ? VARS::request("KATA") : $this->req_field["KATA"],
                                 
                                 "YEAR"         =>  VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->req_field["YEAR"],
                                );
        
        
        $this->bottom_field = array("KYOUKA"         =>  VARS::post("KYOUKA"),
                                    
                                    "SYUBETU"       =>  VARS::post("SYUBETU") != "" ? VARS::post("SYUBETU") : 0,
                                    
                                    );
        if($this->bottom_field["KYOUKA"] == ""){
            if($this->req_field["KATA"] == "1"){
                $this->bottom_field["KYOUKA"] = "100101";
            }else{
                if($this->req_field["GYOUSYA"] != "00000001"){
                    $this->bottom_field["KYOUKA"] = "202001";
                }else{
                    $this->bottom_field["KYOUKA"] = "";
                }
            }
        }
        if($this->cmd == "edit"){     //最初に開いたときだけ
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
        $query = knjh457Query::getJogai($this->req_field["SPORTS"]);
        $contents .= ",".$db->getOne($query);
        
        //期
        $query = knjh457Query::getNendo($this->req_field["YEAR"]);
        $selperiod = $db->getOne($query)."期";
        $contents .= ",".$selperiod;
        
        //業者名と模試名
        $query = knjh457Query::getGyousya($this->req_field["GYOUSYA"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $gyousyaName = $row["NAME1"];
        $contents .= ",".$row["NAME1"];
        
        //型
        $kata = array("", "教科", "受験型");
        $contents .= ",".$kata[$this->req_field["KATA"]];
        
        //選択した教科名
        $query = knjh457Query::getKyoukaName($this->bottom_field["KYOUKA"]);
        $kyouka = $db->getOne($query);
        $contents .= ",".$kyouka;
        
        $contents .= "\n\n";


        //期を取得したい
        $bfryear = (int)$this->req_field["YEAR"] - 2;
        $nextyear = (int)$this->req_field["YEAR"] + 2;
        
        $periodQuery = knjh457Query::getPeriod($this->Properties, $bfryear, $nextyear);
        $periodResult = $db->query($periodQuery);
        
        $period = array();
        $periodCnt = 0;
        $hcnm = "";
        while($periodRow = $periodResult->fetchRow(DB_FETCHMODE_ASSOC)){
            $period[$periodCnt]["YEAR"] = $periodRow["YEAR"];
            $period[$periodCnt]["EST"] = $periodRow["PRESENT_EST"];
            
            $periodCnt++;
        }

        //模試取得
        $gyousya = substr($this->req_field["GYOUSYA"], -1);
        $mosiQuery = knjh457Query::getMockName($this->req_field, $this->req_field["YEAR"], $gyousya, $this->bottom_field["SYUBETU"], $this->bottom_field["KYOUKA"],$minhensa);
        $mosiResult = $db->query($mosiQuery);
        $kamoku = array();
        $lcnm = "";
        $mcnt = 0;
        $mockname = "";
        while($mosiRow = $mosiResult->fetchRow(DB_FETCHMODE_ASSOC)){
            //模試の表示順に配列に入れる
            $mockorder[$mcnt]["MOCKCD"] = $mosiRow["MOCKCD"];
            $mockorder[$mcnt]["MOCKNAME"] = $mosiRow["GRADE"]."年<BR>".$mosiRow["SINROSIDOU_MOSI_NAME"];
            
            //表用
            $mockname .= ",".$mosiRow["GRADE"]."年 ".$mosiRow["SINROSIDOU_MOSI_NAME"];
            
            $mcnt++;
        }
        
        $gyousya = substr($this->req_field["GYOUSYA"],-1);
        
        
        //偏差値の幅設定
        if($this->req_field["ANALYSIS"] != "2"){
            if($this->req_field["GYOUSYA"] != "00000001"){
                $maxhensa = 70;
                $minhensa = 40;
                $range = 10;
            }else{
                $maxhensa = 60;
                $minhensa = 30;
                $range = 10;
            }
        }else{
            if($this->req_field["GYOUSYA"] != "00000001"){
                $maxhensa = 80;
                $minhensa = 45;
                $range = 5;
            }else{
                $maxhensa = 70;
                $minhensa = 45;
                $range = 5;
            }
        }
        
        $cnm = "";
        //偏差値ごとに人数取得
        for($i=$maxhensa;$i>=$minhensa;$i=$i-$range){
            $contents .= $cnm;
            //データ1行目
            if($i != $minhensa){
                $contents .= $i."以上累積数";
            }else{
                $contents .= "平均偏差値";
            }
            $contents .= "\n";
            
            //データ2行目(模試名)
            $contents .= $mockname."\n";
            
            $pCnt = 0;
            foreach($period as $key => $val){   //期ごと
                $gdcnm = "";
                
                $contents .= $val["EST"]."期";

                if($i != $minhensa){
                    $cntQuery = knjh457Query::getMosi($this->req_field, $val["YEAR"], $gyousya, $this->bottom_field["SYUBETU"], $this->bottom_field["KYOUKA"], $i);
                }else{
                    $cntQuery = knjh457Query::getAverage($this->req_field, $val["YEAR"], $gyousya, $this->bottom_field["SYUBETU"], $this->bottom_field["KYOUKA"]);
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

                if(!empty($mockorder)){
                    foreach($mockorder as $mockkey => $mockval){
                        if(is_array($cntData[$val["EST"]]) && array_key_exists($mockval["MOCKCD"], $cntData[$val["EST"]])){
                            $contents .= ",".$cntData[$val["EST"]][$mockval["MOCKCD"]];
                        }else{
                            $contents .= ",";
                        }
                    }
                }

                $pCnt++;
                $contents .= "\n";
            }
            
            $cnm = "\n\n";
        }

        $header_data = true;
        
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($header_data) {
            $name = "各期の推移の比較_".$selperiod.$gyousyaName."_".$kyouka;
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
