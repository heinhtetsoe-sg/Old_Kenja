<?php

class knjh800Model extends Model {

    var $cmd;
    var $cmdSub;
    var $schregno;
    var $delchk;
    var $dividecd;
    var $auth = array();
    var $taitleSort = array();
    var $sort;
    var $staffsort;
    var $datesort;
    var $titlesort;
    var $control_data = array();    //コントロールマスタデータを保持

    function knjh800Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }
 
    function init()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $this->cmd      = VARS::request("cmd");
        $this->gakuseki = VARS::request("GAKUSEKI") != "" ? VARS::request("GAKUSEKI") : $this->gakuseki;
        if($this->cmd == ""){
            $this->gakuseki = "";
        }
        if(VARS::request("GAKUSEKI") != ""){
            $this->year = CTRL_YEAR;
        }else if(VARS::post("YEAR") != ""){
            $this->year = VARS::post("YEAR");
        }
        
        $this->field = array("KIRIKAE"  =>  VARS::post("KIRIKAE") != "" ? VARS::post("KIRIKAE") : 0,
                             
                             "KYOKA"    =>  VARS::post("KYOKA"),
                             
                             "KAMOKU"   =>  VARS::post("KAMOKU"),
                             
                             "TEST_KIND"    =>  VARS::post("TEST_KIND"),
                             );
        
        //表示モードと切り替え
        if($this->cmd == "change" || $this->cmd == ""){
            $this->mode = 0;
        }else if($this->cmd == "hyouzi"){
            $this->mode = 1;
        }
        
        //DB切断
        Query::dbCheckIn($db);

    }
    
    //CSV出力
    function getCsvModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $contents = "";
        //1行目
        $contents .= CTRL_YEAR."年度,".CTRL_SEMESTER."学期";
        $contents .= "\n";
        
        //2行目
        $query = knjh800Query::getStudentName($this->gakuseki, $this->year);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
        $contents .= $row["GRADE"]."年,".$row["HR_CLASS"]."組,".$row["ATTENDNO"]."番,".$row["NAME"];
        $contents .= "\n";
        
        //出力ファイル名用
        $grade = $row["GRADE"]."年".$row["HR_CLASS"]."組".$row["ATTENDNO"]."番";
        
        //3行目
        $cnm = "";
        if($this->field["KYOKA"] != ""){
            //教科名
            $query = knjh800Query::getKyouka($this->field["KYOKA"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $contents .= $row["LABEL"];
            $cnm = ",";
            
            if($this->field["KAMOKU"] != ""){
                //科目
                $query = knjh800Query::getKamoku($this->year, $this->gakuseki, $this->field["KYOKA"], $this->field["KAMOKU"]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $contents .= $cnm.$row["LABEL"];
            }
            
        }
        if($this->field["TEST_KIND"] != ""){
            //テスト種別
            $query = knjh800Query::getTestLevel($this->year, $this->gakuseki, $this->field["KYOKA"], $this->field["KAMOKU"], $this->field["TEST_KIND"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $contents .= $cnm.$row["LABEL"];
            $cnm = ",";
        }
        //表示別
        $contents .= $cnm;
        if($this->field["KIRIKAE"] == "0"){
            $contents .= "受験回数別";
        }else if($this->field["KIRIKAE"] == "1"){
            $contents .= "大分野別";
        }else{
            $contents .= "小分野別";
        }
        $contents .= "\n";
        
        //件数カウント
        $query = knjh800Query::getTestId($this->year, $this->gakuseki, $this->field["KYOKA"], $this->field["KAMOKU"], $this->field["TEST_KIND"], "1");
        $cnt = $db->getOne($query);
        if($cnt > 5000){
            $this->setMessage("件数が多いので条件を絞ってください。");
            Query::dbCheckIn($db);
            return false;
        } else {
            //テストID取得
            $query = knjh800Query::getTestId($this->year, $this->gakuseki, $this->field["KYOKA"], $this->field["KAMOKU"], $this->field["TEST_KIND"]);
            $Result = $db->query($query);
            $k = 0;
            while($row = $Result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($k == 0){
                    $contents .= "回数,科目,配点,";
                    $cnm = "";
                    if($this->field["KIRIKAE"] == "0"){
                        //最大受験回数を取りたい
                        $query = knjh800Query::getTestId($this->year, $this->gakuseki, $this->field["KYOKA"], $this->field["KAMOKU"], $this->field["TEST_KIND"], "2");
                        $max = $db->getOne($query);
                        
                        for($i=1;$i<=$max;$i++){
                            $contents .= $cnm.$i."回目";
                            $cnm = ",";
                        }
                        $contents .= "\n";

                    }else if($this->field["KIRIKAE"] == "1"){
                        //大分野別の名称と得点率取得    最新の受験回数のデータ
                        $query = knjh800Query::getPercent($this->year, $this->gakuseki, $this->field["KYOKA"], $this->field["KAMOKU"], $this->field["TEST_KIND"]);
                        $pResult = $db->query($query);
                        $a = 0;
                        $percent = array();
                        while($pRow = $pResult->fetchRow(DB_FETCHMODE_ASSOC)){
                            $percent[$a]["PARTNAME"] = $pRow["THIRD_NAME"];
                            $percent[$a]["PARTCD"] = $pRow["PARTCD"];
                            $percent[$a]["PERCENT"] = sprintf("%0.1f", $pRow["PERCENT"]);
                            
                            $a++;
                        }
                        $max = $a;
                        
                        $nextcontents = "計,,得点率,";
                        
                        for($b=0;$b<$a;$b++){
                            $contents .= $cnm.$percent[$b]["PARTNAME"];
                            $nextcontents .= $cnm.$percent[$b]["PERCENT"]."%";
                            
                            $cnm = ",";
                        }
                        $contents .= "\n";
                        $contents .= $nextcontents."\n";
                        
                    }else{
                        //大分野名称と小分野個数取得
                        $query = knjh800Query::getQuestCnt($this->year, $this->gakuseki, $this->field["KYOKA"], $this->field["KAMOKU"], $this->field["TEST_KIND"]);
                        $qResult = $db->query($query);
                        $partArray = array();
                        $pCnt = 0;
                        while($qRow = $qResult->fetchRow(DB_FETCHMODE_ASSOC)){
                            $contents .= $cnm.$qRow["THIRD_NAME"];
                            $partArray[$pCnt] = $qRow["PARTCD"];
                            $pCnt++;
                            
                            $cnm = ",";
                            for($count=1;$count<$qRow["CNT"];$count++){
                                $contents .= ",";
                            }
                        }
                        $contents .= "\n";
                        
                        //小分野ごとの名称取得
                        $query = knjh800Query::getQuestName($this->year, $this->gakuseki, $this->field["KYOKA"], $this->field["KAMOKU"], $this->field["TEST_KIND"]);
                        $qResult = $db->query($query);
                        $questArray = array();
                        $qCnt = 0;
                        $contents .= ",,,";
                        $nextcontents = "計,,得点率,";
                        $cnm = "";
                        while($qRow = $qResult->fetchRow(DB_FETCHMODE_ASSOC)){
                            //大分野と小分野のつながりはチェックしてない
                            $percent = sprintf("%0.1f", $qRow["PERCENT"]);
                            $contents .= $cnm.$qRow["FOURTH_NAME"];
                            $nextcontents .= $cnm.$percent."%";     //得点率
                            $questArray[$qCnt] = $qRow["QUESTION_FIELDNO"];
                            $qCnt++;
                            
                            $cnm = ",";
                        }
                        $contents .= "\n";
                        $contents .= $nextcontents."\n";
                        
                        $max = $qCnt;
                        
                    }
                    $k++;
                }
                //指定したテストの教科等を取得
                $query = knjh800Query::getTestName($this->year, $this->gakuseki, $row["TEST_ID"]);
                $tRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $contents .= "第".$tRow["TAKE_TURN"]."回,".$tRow["SUBCLASSNAME"].",".$tRow["POINT"].",";
                
                if($this->field["KIRIKAE"] == "0"){
                    
                    //テストの回数データ取得
                    $query = knjh800Query::getTestData($this->year, $this->gakuseki, $row["TEST_ID"]);
                    $dResult = $db->query($query);
                    $i=0;
                    $cnm = "";
                    while($dRow = $dResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        $i++;
                        
                        $stDate = str_replace("-", "/", substr($dRow["START_DATE"], 0,10));
                        $contents .= $cnm.intval($dRow["GET_POINT"])."(".$stDate.")";
                        
                        $cnm = ",";
                    }
                    if($max > $i){
                        for($j=$i;$j<$max;$j++){
                            $contents .= $cnm."-";
                            $cnm = ",";
                        }
                    }
                    

                }else if($this->field["KIRIKAE"] == "2"){
                    
                    //小分野の配点と得点を取得  最新の受験回数のデータ
                    $query = knjh800Query::getQuestPoint($this->year, $this->gakuseki, $row["TEST_ID"]);
                    $pResult = $db->query($query);
                    
                    $count = 0;
                    $cnm = "";
                    while($pRow = $pResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        $key = array_search($pRow["QUESTCD"], $questArray);   //QUESTCDの場所を探す
                        for($s=$count;$s<$key;$s++){
                            $contents .= $cnm."-";
                            $cnm = ",";
                        }
                        $count = $s;
                        $contents .= $cnm." ".intval($pRow["SCORE"])."/".$pRow["POINT"];
                        $cnm = ",";
                        
                        $count++;
                    }
                    for($last=$count;$last<$qCnt; $last++){
                        $contents .= $cnm."-";
                        $cnm = ",";
                    }

                }else{
                    
                    //データ取得    最新の受験回数のデータ
                    $query = knjh800Query::getPartScore($this->year, $this->gakuseki, $row["TEST_ID"]);
                    $sResult = $db->query($query);
                    $count = 0;
                    $cnm = "";
                    $array = array_column($percent, "PARTCD");  //PARTCDのみの配列を作成
                    while($sRow = $sResult->fetchRow(DB_FETCHMODE_ASSOC)){
                        $key = array_search($sRow["PARTCD"], $array);   //PARTCDの場所を探す
                        for($s=$count;$s<$key;$s++){
                            $contents .= $cnm."-";
                            $cnm = ",";
                        }
                        $count = $s;
                        $contents .= $cnm." ".intval($sRow["PARTGET"])."/".$sRow["PARTPOINT"];
                        $cnm = ",";
                        
                        $count++;
                    }
                    for($last=$count;$last < $a; $last++){
                        $contents .= $cnm."-";
                        $cnm = ",";
                    }
                }
                
                $contents .= "\n";
                $k++;
                $header_data = true;
            }
        
            //$result->free();
            Query::dbCheckIn($db);
            //ＣＳＶ出力
            if ($header_data) {
                $name = "実戦演習CBT一覧_".$grade;
                $filename = sprintf("%s.csv",$name);
                common::downloadContents($contents, $filename);
                
                return true;
            } else {
                $this->warning = "該当データは、ありません。";
                return false;
            }
        }
    }

}
?>