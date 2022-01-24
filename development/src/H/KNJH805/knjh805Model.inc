<?php

class knjh805Model extends Model {

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

    function knjh805Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }
 
    function init()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $this->getPropertiesAll(); //プロパティファイル

        $this->cmd      = VARS::request("cmd");
        $this->gakuseki = VARS::request("GAKUSEKI") != "" ? VARS::request("GAKUSEKI") : $this->gakuseki;
        
        //年度
        $this->topfield["EXP_YEAR"] = VARS::post("EXP_YEAR") != "" ? VARS::post("EXP_YEAR") : CTRL_YEAR;
        $this->topfield["SEMESTER"] = VARS::post("SEMESTER") != "" ? VARS::post("SEMESTER") : "CTRL_SEMETER";
        
        //科目コード
        $this->topfield["SUBCLASSCD"] = VARS::request("SUBCLASSCD");
        if ($this->cmd == "update" || $this->cmd == "calc") {
            $this->topfield["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
        }
        //講座コード
        if ($this->cmd!="subclasscd"&&VARS::request("CHAIRCD")) {
            $this->topfield["CHAIRCD"] = VARS::request("CHAIRCD");
        } else if ($this->cmd == "update" || $this->cmd == "calc") {
            $this->topfield["CHAIRCD"] = VARS::post("H_CHAIRCD");
        } else {
            $this->topfield["CHAIRCD"] = "";
        }
        $this->field = array(
                             "KYOKA"    =>  VARS::post("KYOKA"),
                             
                             "KAMOKU"   =>  VARS::post("KAMOKU"),
                             
                             "TEST_KIND"    =>  VARS::post("TEST_KIND"),
                             
                             "DATE"   =>      str_replace("/", "-", VARS::post("DATE")),
                             
                             "F_HOUR"   =>      VARS::post("F_HOUR"),
                             
                             "T_HOUR"   =>      VARS::post("T_HOUR"),
                             
                             "F_MIN"   =>      VARS::post("F_MIN"),
                             
                             "T_MIN"   =>      VARS::post("T_MIN"),
                             
                             
                             );
        //表示
        if($this->cmd == "hyouzi"){
            $this->mode = 1;
        }else if($this->cmd == "change" || $this->cmd == "" || $this->cmd == "subclasscd" || $this->cmd == "chaircd"){
            $this->mode = 0;
        }else if($this->cmd == "clear"){
            $this->mode = 0;
            $this->field = array();
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
        //対象生徒の科目
        $query = knjh805Query::getSubclassMst($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $contents .= $row["LABEL"].",";
        
        //対象生徒の講座
        $query = knjh805Query::selectChairQuery($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $contents .= $row["LABEL"]."\n";
        //出力ファイル用名称
        $kouza = $row["LABEL"];
        
        //3行目
        //教科名
        $query = knjh805Query::getKyouka($this->field["KYOKA"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $contents .= $row["LABEL"];
        $cnm = ",";
        
        if($this->field["KAMOKU"] != ""){
            //科目
            $query = knjh805Query::getKamoku($this->topfield["EXP_YEAR"], $this->field["KYOKA"], $this->field["KAMOKU"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $contents .= $cnm.$row["LABEL"];
        }
        
        if($this->field["TEST_KIND"] != ""){
            //テスト種別
            $query = knjh805Query::getTestLevel($this->topfield["EXP_YEAR"], $this->field["KYOKA"], $this->field["KAMOKU"], $this->field["TEST_KIND"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $contents .= $cnm.$row["LABEL"];
            $cnm = ",";
        }
        $contents .= "\n";
        
        //4行目
        //受験日時指定
        $cnm = "";
        if($this->field["DATE"] != ""){
            $contents .= $this->field["DATE"];
            $cnm = ",";
            
            if($this->field["F_HOUR"] != ""){
                $contents .= $cnm.sprintf("%02d", $this->field["F_HOUR"]).":".sprintf("%02d", $this->field["F_MIN"]);
                $cnm = ",";
            }
            if($this->field["T_HOUR"] != ""){
                $contents .= ",～";
                $cnm = ",";
                $contents .= $cnm.sprintf("%02d", $this->field["T_HOUR"]).":".sprintf("%02d", $this->field["T_MIN"]);
            }
            $contents .= "\n";
        }



        //件数カウント
        $query = knjh805Query::getKnjid($this->topfield, $this->field, "2");
        $cCnt = $db->getOne($query);
        if($cCnt > 5000){
            $this->setMessage("件数が多いので条件を絞ってください。");
            Query::dbCheckIn($db);
            return false;
        } else {


            //テストの実施回取得    回数のみ取得できるSQL
            $query = knjh805Query::getKnjid($this->topfield, $this->field, "1");
            $cResult = $db->query($query);
            
            //適当なデータ
            $contents .= "年,組,番,氏名,";
            $sql = "";
            $cnm = "";
            $takeTurn = array();
            $takeCnt = 0;
            while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $contents .= $cnm."第".$cRow["TAKE_TURN"]."回";
                $sql .= $cnm."'".$cRow["TEST_ID"]."'";
                $cnm = ",";
                $takeTurn[$takeCnt] = $cRow["TEST_ID"];
                $takeCnt++;
            }
            $contents .= "\n";

            //対象生徒取得
            //$query = knjh805Query::getKnjid($this->topfield,$this->field);
            $query = knjh805Query::getAllKnjid($this->topfield);      //対象のクラス全員を表示する
            $sResult = $db->query($query);
            while($sRow = $sResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $contents .= $sRow["GRADE"].",".$sRow["HR_CLASS"].",".$sRow["ATTENDNO"].",".$sRow["NAME"];
                
                //対象の生徒の対象回データ取得
                $query = knjh805Query::getScore($this->topfield, $this->field, $sRow["KNJID"], $sql);
                $tResult = $db->query($query);
                $count = 0;
                $t = 0;
                while($tRow = $tResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $key = array_search($tRow["TEST_ID"], $takeTurn);
                    for($t=$count;$t<$key;$t++){
                        $contents .= $cnm."-";
                        $cnm = ",";
                    }
                    $count = $t;
                    $contents .= $cnm.$tRow["GET_POINT"]."/".$tRow["POINT"]."(".$tRow["TAKEDATE"].")";
                    $cnm = ",";
                    $count++;
                
                }
                if($count < $takeCnt){
                    for($a = $count; $a < $takeCnt; $a++){
                        $contents .= $cnm."-";
                        $cnm = ",";
                    }
                }
                $contents .= "\n";
                $header_data = true;
            }


            Query::dbCheckIn($db);
            //ＣＳＶ出力
            if ($header_data) {
                $kouzaName = explode("：", $kouza);
                $name = "実戦演習CBT生徒受験履歴_".$kouzaName[1];
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
