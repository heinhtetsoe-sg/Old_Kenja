<?php

class knjh815Model extends Model {

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

    function knjh815Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }
 
    function init()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $this->cmd      = VARS::request("cmd");
        $this->knjid = VARS::request("GAKUSEKI") != "" ? VARS::request("GAKUSEKI") : $this->knjid;
        if($this->cmd == ""){
            $this->knjid = "";
        }
        if(VARS::request("GAKUSEKI") != ""){
            $this->year = CTRL_YEAR;
        }else if(VARS::post("YEAR") != ""){
            $this->year = VARS::post("YEAR");
        }
        $this->field = array("KUBUN"        => VARS::post("KUBUN") != "" ? VARS::post("KUBUN") : 0,
                             
                             "KYOKA"        =>  VARS::post("KYOKA"),
                             
                             "KAMOKU"       =>  VARS::post("KAMOKU"),
                             
                             "TEST_KIND"    =>  VARS::post("TEST_KIND"),
                             
                             "F_KAISU"      =>  VARS::post("F_KAISU"),
                             
                             "T_KAISU"      =>  VARS::post("T_KAISU"),
                             
                             "TESTCNT"      =>  VARS::post("TESTCNT") != "" ? VARS::post("TESTCNT") : "1",
                             
                             );
        
        
        //表示
        if($this->cmd == "hyouzi"){
            $this->mode = 1;
        }else if($this->cmd == "change" || $this->cmd == "" || $this->cmd == "subclasscd" || $this->cmd == "chaircd"){
            $this->mode = 0;
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
        //対象生徒氏名
        $query = knjh815Query::getName($this->knjid, $this->year);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $contents .= $row["GRADE"]."年".$row["HR_CLASS"]."組".$row["ATTENDNO"]."番,".$row["NAME"]."\n";
        
        $semester = $row["SEMESTER"];
        
        //3行目
        //教科名
        $query = knjh815Query::getKyouka($this->field["KYOKA"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $contents .= $row["LABEL"];
        $cnm = ",";
        
        if($this->field["KAMOKU"] != ""){
            //科目
            $query = knjh815Query::getKamoku($this->year, $this->field["KYOKA"], $this->field["KAMOKU"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $contents .= $cnm.$row["LABEL"];
        }
        
        if($this->field["TEST_KIND"] != ""){
            //テスト種別
            $query = knjh815Query::getTestLevel($this->year, $this->field["KYOKA"], $this->field["KAMOKU"], $this->field["TEST_KIND"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $contents .= $cnm.$row["LABEL"];
            $cnm = ",";
        }
        $contents .= "\n";
        
        //4行目
        //選択区分
        if($this->field["KUBUN"] == "1"){
            $contents .= "大分野別";
            $type = "大分野別";   //出力ファイル名称に使う
        }else{
            $contents .= "小分野別";
            $type = "小分野別";
        }
        if($this->field["F_KAISU"] != ""){
            $contents .= ",第".$this->field["F_KAISU"]."回";
        }
        if($this->field["F_KAISU"] != $this->field["T_KAISU"]){
            $contents .= " ～ ";
        }
        if($this->field["T_KAISU"] != ""){
            $contents .= "第".$this->field["T_KAISU"]."回";
        }
        $contents .= "\n";
        
        
        //件数カウント
        $query = knjh815Query::getIndData($this->year, $this->knjid, $this->field, "1");
        $eCnt = $db->getOne($query);
        if($eCnt == 0){
            $this->setMessage("対象のデータがありません。");
            Query::dbCheckIn($db);
            return false;
        }else if($eCnt > 5000){
            $this->setMessage("件数が多いので条件を絞ってください。");
            Query::dbCheckIn($db);
            return false;
        } else {
            //変換用配列    共通で使いたい
            $partNo = array("POINT", "SCHREG_SCORE", "GRADE_MAX", "AVG", "GRADE_AVG");
            $partChange = array("POINT"          =>  "配点",
                                "SCHREG_SCORE"   =>  "個人得点",
                                "AVG"            =>  "クラス平均点",
                                "GRADE_MAX"      =>  "学年最高点",
                                "GRADE_AVG"      =>  "学年平均点");
            
            //個人のデータを先に取得して個人の受験データから表を作成する
            $query = knjh815Query::getTestScregData($this->year, $this->knjid, $this->field);
            $scResult = $db->query($query);

            $dataArray = array();   //実施回をキーにしてデータを保持→TEST_IDをキーにしないと重複する
            $takeArray = array();   //実施回のみの配列
            $testArray = array();   //TEST_IDのみの配列
            $sql = "";  //学年平均・学年最高点取得用where条件
            $cnm = "";
            $takeCnt = 0;
            $allPoint = 0;  //左上の得点率計算のためにすべてのテストの配点を合計する
            while($scRow = $scResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $dataArray[$scRow["TEST_ID"]] = $scRow;
                $takeArray[$takeCnt] = $scRow["TAKE_TURN"];
                $testArray[$takeCnt] = $scRow["TEST_ID"];
                
                $allPoint = $allPoint + $scRow["POINT"];
                
                $sql .= $cnm."'".$scRow["TEST_ID"]."'";
                $cnm = ",";
                $takeCnt++;
            }

            //実施回と配点クラス平均・クラス最高点取得
            $query = knjh815Query::getTestClass($this->year, $semester, $this->knjid, $this->field, $sql);
            $cResult = $db->query($query);
            while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $dataArray[$cRow["TEST_ID"]] += $cRow;
            }
            //学年平均・学年最高点取得
            $query = knjh815Query::getTestGrade($this->year, $semester, $this->knjid, $this->field, $sql);
            $gResult = $db->query($query);
            while($gRow = $gResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $dataArray[$gRow["TEST_ID"]] += $gRow;
            }
            //選択した生徒の成績
            $query = knjh815Query::getTestScregData($this->year, $this->knjid, $this->field, $sql);
            $scResult = $db->query($query);
            while($scRow = $scResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $dataArray[$scRow["TEST_ID"]] += $scRow;
            }
            //得点率
            $rhperArray = array();
            foreach($testArray as $testid){
                foreach($partNo as $val){
                    if($val != "POINT"){
                        $percent = $dataArray[$testid][$val] / $dataArray[$testid]["POINT"] * 100;
                        $percent = round($percent, 1);
                        $rhperArray[$testid][$val] = $percent;
                    }else{
                        $rhperArray[$testid][$val] = "";
                    }
                }
            }
            
            $allArray = array();
            //テスト関係なく人毎に計算してデータ取得
            //クラス平均とクラス最高点
            $query = knjh815Query::getAllClass($this->year, $semester, $this->knjid, $this->field, $sql);
            $aRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $allArray = array();
            $allArray = $aRow;
            
            //学年平均と学年最高点
            $query = knjh815Query::getAllGrade($this->year, $semester, $this->knjid, $this->field, $sql);
            $agRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $allArray += $agRow;
            
            //個人得点
            $query = knjh815Query::getAllSchreg($this->year, $this->knjid, $this->field, $sql);
            $asRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $allArray += $asRow;
            
            //得点率計算
            $allArray["POINT"] = $allPoint;     //右上で計算してきたやつ
            $hxperArray = array();
            foreach($partNo as $val){
                if($val != "POINT"){
                    $percent = $allArray[$val] / $allArray["POINT"] * 100;
                    $percent = round($percent, 1);
                    $hxperArray[$val] = $percent;
                }else{
                    $hxperArray[$val] = "";
                }
            }
            
            $contents .= "実施回,";
            $contents .= "計,得点率(%),";
            $cnm = "";
            for($i=0;$i<$takeCnt;$i++){
                $contents .= $cnm."第".$takeArray[$i]."回 ".$dataArray[$testArray[$i]]["TEST_KIND_NAME"];
                $contents .= ",得点率(%)";
                $cnm = ",";
            }
            $contents .= "\n";
            
            foreach($partNo as $partKey => $partVal){
                $contents .= $partChange[$partVal].",";
                
                if(preg_match("/AVG/", $partVal)){
                    $allArray[$partVal] = sprintf("%0.2f", $allArray[$partVal]);
                }else{
                    $allArray[$partVal] = floor($allArray[$partVal]);
                }
                
                $contents .= $allArray[$partVal].",".$hxperArray[$partVal].",";
                $cnm = "";
                foreach($testArray as $val){
                    if(preg_match("/AVG/", $partVal)){
                        $dataArray[$val][$partVal] = sprintf("%0.2f", $dataArray[$val][$partVal]);
                    }else{
                        $dataArray[$val][$partVal] = floor($dataArray[$val][$partVal]);
                    }
                    $contents .= $cnm.$dataArray[$val][$partVal].",".$rhperArray[$val][$partVal];
                    $cnm = ",";
                }
                $contents .= "\n";
            }

            if($this->field["KUBUN"] == "1"){
                //大分野ごとの名称と全体のテストの配点、クラス平均、クラス最高点を取得
                $query = knjh815Query::getPartClass($this->year, $semester, $this->knjid, $this->field, $sql);
                $pcResult = $db->query($query);
                $partArray = array();   //名称保持
                $partNoArray = array();
                $partCnt = 0;
                while($pcRow = $pcResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $partArray[$pcRow["FIELDNO"]] = $pcRow;
                    $partNoArray[$partCnt] = $pcRow["FIELDNO"];
                    $partCnt++;
                }
                
                //大分野ごとに全体のテストの学年平均・最高点を取得
                $query = knjh815Query::getPartGrade($this->year, $semester, $this->knjid, $this->field, $sql);
                $pgResult = $db->query($query);
                while($pgRow = $pgResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $partArray[$pgRow["FIELDNO"]] += $pgRow;
                }
                
                //大分野ごとの全体の個人得点取得
                $query = knjh815Query::getPartSchreg($this->year, $this->knjid, $this->field, $sql);
                $psResult = $db->query($query);
                while($psRow = $psResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $partArray[$psRow["FIELDNO"]] += $psRow;
                }

                //大分野ごと、TEST_IDごとにクラス平均とクラス最高点取得
                $query = knjh815Query::getPartClassPoint($this->year, $semester, $this->knjid, $this->field, $sql);
                $pcResult = $db->query($query);
                $ppArray = array();
                $Avg = array();
                
                while($pcRow = $pcResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $ppArray[$pcRow["FIELDNO"]][$pcRow["TEST_ID"]] = $pcRow;
                }
                //大分野ごと、TEST_IDごとに学年平均と学年最高点取得
                $query = knjh815Query::getPartGradePoint($this->year, $semester, $this->knjid, $this->field, $sql);
                $pgResult = $db->query($query);
                while($pgRow = $pgResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $ppArray[$pgRow["FIELDNO"]][$pgRow["TEST_ID"]] += $pgRow;
                }
                
                //大分野ごと、TEST_IDごとに個人成績取得
                $query = knjh815Query::getPartSchregPoint($this->year, $this->knjid, $this->field, $sql);
                $psResult = $db->query($query);
                while($psRow = $psResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $ppArray[$psRow["FIELDNO"]][$psRow["TEST_ID"]] += $psRow;
                    
                }
                
                //左下用に大分野ごとの配点の合計点を計算したい
                $partAllPoint = array();
                foreach($partNoArray as $val){
                    foreach($testArray as $test){
                        if($partAllPoint[$val] == ""){
                            $partAllPoint[$val] = 0;
                        }
                        $partAllPoint[$val] = $partAllPoint[$val] + $ppArray[$val][$test]["POINT"];
                    }
                }

                //右下得点率計算
                $ppPerArray = array();
                foreach($partNoArray as $val){
                    foreach($testArray as $test){
                        foreach($partNo as $part){
                            if($part != "POINT"){
                                if($ppArray[$val][$test]["POINT"] != ""){
                                    $percent = $ppArray[$val][$test][$part] / $ppArray[$val][$test]["POINT"] * 100;
                                    $percent = round($percent, 1);
                                    $ppPerArray[$val][$test][$part] = $percent;
                                }else{
                                    $ppPerArray[$val][$test][$part] = "";
                                }
                            }else{
                                $ppPerArray[$val][$test][$part] = "";
                            }
                        }
                    }
                }

                //左下得点率計算
                $lbperArray = array();
                foreach($partNoArray as $val){
                    foreach($partNo as $part){
                        if($part != "POINT"){
                            $percent = $partArray[$val][$part] / $partAllPoint[$val] * 100;
                            $percent = round($percent, 1);
                            $lbperArray[$val][$part] = $percent;
                        }else{
                            $partArray[$val]["POINT"] = $partAllPoint[$val];    //配点合計を入れる
                            $lbperArray[$val][$part] = "";
                        }
                    }
                }
                

                
                $cnm = "";
                foreach($partNoArray as $key => $val){
                    //大分野名称
                    $contents .= $partArray[$val]["THIRD_NAME"]."\n";
                    $cnm = "";
                    //右側
                    foreach($partNo as $partKey => $partVal){
                        if(preg_match("/AVG/", $partVal)){
                            $partArray[$val][$partVal] = sprintf("%0.2f", $partArray[$val][$partVal]);
                        }else{
                            $partArray[$val][$partVal] = floor($partArray[$val][$partVal]);
                        }
                        $contents .= $partChange[$partVal].",".$partArray[$val][$partVal].",".$lbperArray[$val][$partVal];

                        $cnm = ",";

                        foreach($testArray as $test){   //TEST_IDまわす
                            if($ppArray[$val][$test][$partVal] != ""){
                                if(preg_match("/AVG/", $partVal)){
                                    $ppArray[$val][$test][$partVal] = sprintf("%0.2f", $ppArray[$val][$test][$partVal]);
                                }else{
                                    $ppArray[$val][$test][$partVal] = floor($ppArray[$val][$test][$partVal]);
                                }
                                $contents .= $cnm.$ppArray[$val][$test][$partVal].$cnm.$ppPerArray[$val][$test][$partVal];
                            }else{
                                $contents .= $cnm."-,-";
                            }
                        }
                        $contents .= "\n";

                        $header_data = true;


                    }
                }
            }else{
                //大分野名称と大分野に付く小分野の個数を取得
                $query = knjh815Query::getPartCnt($this->year, $semester, $this->knjid, $this->field, $sql);
                $pResult = $db->query($query);
                $pthArray = array();
                $partNoArray = array();
                $partCnt = 0;
                
                while($pRow = $pResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $pthArray[$pRow["PART_FIELDNO"]] = $pRow;
                    
                    $partNoArray[$partCnt] = $pRow["PART_FIELDNO"];
                    $partCnt++;
                }
                //小分野の名称とFIELDNOを取得
                $query = knjh815Query::getQuestNo($this->year, $semester, $this->knjid, $this->field, $sql);
                $qResult = $db->query($query);
                $qthArray = array();
                $questNoArray = array();
                $questCnt = 0;
                $bfrPart = "";
                
                while($qRow = $qResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    if($bfrPart != $qRow["PART_FIELDNO"]){
                        $questCnt = 0;
                    }
                    $qthArray[$qRow["PART_FIELDNO"]][$qRow["QUESTION_FIELDNO"]] = $qRow;
                    
                    $questNoArray[$qRow["PART_FIELDNO"]][$questCnt] = $qRow["QUESTION_FIELDNO"];
                    $questCnt++;
                    
                    $bfrPart = $qRow["PART_FIELDNO"];
                }
                
                //小分野ごとのクラス平均・クラス最高点を取得
                $query = knjh815Query::getQuestClass($this->year, $semester, $this->knjid, $this->field, $sql);
                $qcResult = $db->query($query);
                
                while($qcRow = $qcResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $qthArray[$qcRow["PART_FIELDNO"]][$qcRow["QUESTION_FIELDNO"]] += $qcRow;
                }
                
                //小分野ごとの学年平均・学年最高点を取得
                $query = knjh815Query::getQuestGrade($this->year, $semester, $this->knjid, $this->field, $sql);
                $qgResult = $db->query($query);
                while($qgRow = $qgResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $qthArray[$qgRow["PART_FIELDNO"]][$qgRow["QUESTION_FIELDNO"]] += $qgRow;
                }
                
                //小分野ごとの個人得点を取得
                $query = knjh815Query::getQuestSchreg($this->year, $this->knjid, $this->field, $sql);
                $qsResult = $db->query($query);
                while($qsRow = $qsResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $qthArray[$qsRow["PART_FIELDNO"]][$qsRow["QUESTION_FIELDNO"]] += $qsRow;
                }
                
                //小分野ごと、TEST_IDごとにクラス平均・クラス最高点取得
                $query = knjh815Query::getQuestClassPoint($this->year, $semester, $this->knjid, $this->field, $sql);
                $qcResult = $db->query($query);
                $qpArray = array();
                $Avg = array();
                
                while($qcRow = $qcResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $qpArray[$qcRow["PART_FIELDNO"]][$qcRow["QUESTION_FIELDNO"]][$qcRow["TEST_ID"]] = $qcRow;
                }
                //小分野ごと、TEST_IDごとに学年平均・学年最高点取得
                $query = knjh815Query::getQuestGradePoint($this->year, $semester, $this->knjid, $this->field, $sql);
                $qgResult = $db->query($query);
                while($qgRow = $qgResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $qpArray[$qgRow["PART_FIELDNO"]][$qgRow["QUESTION_FIELDNO"]][$qgRow["TEST_ID"]] += $qgRow;
                }
                
                //小分野ごと、TEST_IDごとの個人得点取得
                $query = knjh815Query::getQuestSchregPoint($this->year, $this->knjid, $this->field, $sql);
                $qsResult = $db->query($query);
                while($qsRow = $qsResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $qpArray[$qsRow["PART_FIELDNO"]][$qsRow["QUESTION_FIELDNO"]][$qsRow["TEST_ID"]] += $qsRow;
                }
                
                //左下計算用配点合計
                $questAllPoint = array();
                foreach($partNoArray as $partNoVal){
                    foreach($questNoArray[$partNoVal] as $questNo){
                        foreach($testArray as $test){
                            if($questAllPoint[$partNoVal][$questNo] == ""){
                                $questAllPoint[$partNoVal][$questNo] = 0;
                            }
                            $questAllPoint[$partNoVal][$questNo] = $questAllPoint[$partNoVal][$questNo] + $qpArray[$partNoVal][$questNo][$test]["POINT"];
                        }
                    }
                }
                
                //右下用得点率計算
                $qpPerArray = array();
                foreach($partNoArray as $partNoVal){
                    foreach($questNoArray[$partNoVal] as $questNo){
                        foreach($testArray as $test){
                            foreach($partNo as $part){
                                if($part != "POINT"){
                                    if($qpArray[$partNoVal][$questNo][$test]["POINT"] != ""){
                                        $percent = $qpArray[$partNoVal][$questNo][$test][$part] / $qpArray[$partNoVal][$questNo][$test]["POINT"] * 100;
                                        $percent = round($percent, 1);
                                        $qpPerArray[$partNoVal][$questNo][$test][$part] = $percent;
                                    }else{
                                        $qpPerArray[$partNoVal][$questNo][$test][$part] = "";
                                    }
                                }else{
                                    $qpPerArray[$partNoVal][$questNo][$test][$part] = "";
                                }
                            }
                        }
                    }
                }

                //左下得点率計算
                $lbPerArray = array();
                foreach($partNoArray as $partNoVal){
                    foreach($questNoArray[$partNoVal] as $questNo){
                        foreach($partNo as $part){
                            if($part != "POINT"){
                                $percent = $qthArray[$partNoVal][$questNo][$part] / $qthArray[$partNoVal][$questNo]["POINT"] * 100;
                                $percent = round($percent, 1);
                                $lbPerArray[$partNoVal][$questNo][$part] = $percent;
                            }else{
                                $qthArray[$partNoVal][$questNo][$part] = $questAllPoint[$partNoVal][$questNo];
                                $lbPerArray[$partNoVal][$questNo][$part] = "";
                            }
                        }
                    }
                }


                foreach($partNoArray as $pKey => $pVal){

                    $contents .= $pthArray[$pVal]["THIRD_NAME"]."\n";
                    
                    foreach($qthArray[$pVal] as $qKey => $qVal){
                        foreach($partNo as $partKey => $partVal){
                            if($partKey == 0){
                                $qVal[$partVal] = floor($qVal[$partVal]);
                                $contents .= $qVal["FOURTH_NAME"].",".$qVal[$partVal].",";
                            }else{
                                if(preg_match("/AVG/", $partVal)){
                                    $qVal[$partVal] = sprintf("%0.2f", $qVal[$partVal]);
                                }else{
                                    $qVal[$partVal] = floor($qVal[$partVal]);
                                }
                                $contents .= $partChange[$partVal].",".$qVal[$partVal].",".$lbPerArray[$pVal][$qKey][$partVal];
                            }

                            $cnm = ",";
                            foreach($testArray as $tVal){
                                
                                if($qpArray[$pVal][$qKey][$tVal][$partVal] != ""){
                                    if(preg_match("/AVG/", $partVal)){
                                        $qpArray[$pVal][$qKey][$tVal][$partVal] = sprintf("%0.2f", $qpArray[$pVal][$qKey][$tVal][$partVal]);
                                    }else{
                                        $qpArray[$pVal][$qKey][$tVal][$partVal] = floor($qpArray[$pVal][$qKey][$tVal][$partVal]);
                                    }
                                    
                                    $contents .= $cnm.$qpArray[$pVal][$qKey][$tVal][$partVal].$cnm.$qpPerArray[$pVal][$qKey][$tVal][$partVal];
                                    
                                }else{
                                    $contents .= $cnm."-,-";
                                }
                            }
                            $contents .= "\n";

                            $header_data = true;

                        }
                    }
                }
            }
            
            
            
            Query::dbCheckIn($db);
            //ＣＳＶ出力
            if ($header_data) {
                $name = "CBT個人成績_".$this->knjid."_".$type;
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
