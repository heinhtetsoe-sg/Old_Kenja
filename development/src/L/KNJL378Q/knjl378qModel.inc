<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl378qModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjl378qModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("NENDO"        =>      VARS::post("NENDO") != "" ? VARS::post("NENDO") : CTRL_YEAR,
                             
                             "EXAM_DATE"    =>      VARS::post("EXAM_DATE"),
                             
                             "AMOUNT1"      =>      VARS::post("AMOUNT1"),
                             
                             "AMOUNT2"      =>      VARS::post("AMOUNT2"),
                             
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル


    }
    function &getMainModel()
    {
    }


    //実行
    function getExecModel()
    {
        $db = Query::dbCheckOut();
        
        $this->countS = 0;//受験者
        $this->values = array();//偏差値を求めるための合計点をいれておく
        
        
        //得点の確定（SAT_EXAM_DATのSCORE_TOTALを更新）
        $scoreQuery = knjl378qQuery::ScoreUpdate();
        $db->query($scoreQuery);
        
        //3教科の平均点計算(SAT_EXAM_DATのAVERAGEを更新)
        $query = knjl378qQuery::getScoreAll();
        $result = $db->query($query);
        $update = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $cnt = 0;
            $total = 0;
            
            if($row["SCORE_ENGLISH"] != ""){
                $cnt++;
                $total = $total + $row["SCORE_ENGLISH"];
            }
            if($row["SCORE_MATH"] != ""){
                $cnt++;
                $total = $total + $row["SCORE_MATH"];
            }
            if($row["SCORE_JAPANESE"] != ""){
                $cnt++;
                $total = $total + $row["SCORE_JAPANESE"];
            }


            if($cnt > 0){
                $avrage = $total / $cnt;
                $ten = pow(10, 5);
                
                $avg = floor( $avrage * $ten ) / $ten ;
                
                $update["AVERAGE"] = $avg;
                
                $where = " WHERE SAT_NO = '".$row["SAT_NO"]."' AND YEAR = '".CTRL_YEAR."' ";
                
                common::_update("SAT_EXAM_DAT", "", $where, $update, $db, "1");
            }
            
            if($cnt == 3){//3教科受験してたら
                $this->countS ++;
                $this->values[] = $total;
            }
            
            
            $update = array();
        }
        
        //3教科偏差値を更新
        
        //標準偏差の計算
        $standard_deviation = $this->getStandard_deviation($this->values);
        
        //echo "sd=".round($standard_deviation,2)."<BR>";
        //echo "av=".round($this->ave,2)."<BR>";
        //exit;
        
        
        $devquery = knjl378qQuery::getStudentsDev();
        $devresult = $db->query($devquery);
        while($devrow = $devresult->fetchRow(DB_FETCHMODE_ASSOC)){
            
            $deviation = (($devrow["SCORE_TOTAL"] - $this->ave) / $standard_deviation) * 10 + 50;
            
            $devupdate["DEVIATION"] = $deviation;
            
            $devwhere = " WHERE SAT_NO = '".$devrow["SAT_NO"]."' AND YEAR = '".CTRL_YEAR."' ";
            
            common::_update("SAT_EXAM_DAT", "", $devwhere, $devupdate, $db, "1");
            
        }
        
        //順位確定（SAT_EXAM_DATのAREA_RANK_教科とALL_RANK_教科を更新）
        $rankQuery = knjl378qQuery::RankUpdate();
        $db->query($rankQuery);
        
        //一応SAT_AREA_RECORD_DATのデータを削除したい
        $cntQuery = knjl378qQuery::CntAreaRecord();
        $cnt = $db->getOne($cntQuery);
        if($cnt > 0){
            //DELETE
            $delQuery = knjl378qQuery::DelAreaRecord();
            $db->query($delQuery);
        }
        
        //地区ごとの教科平均点・標準偏差・人数確定（SAT_AREA_RECORD_DATにデータ挿入）
        $areaQuery = knjl378qQuery::AreaInsert();
        $db->query($areaQuery);
        
        //SAT_AREA_DIST_DATを更新したい
        //とりあえず削除
        $delAreaQuery  = knjl378qQuery::delAreaDist();
        $db->query($delAreaQuery);
        
        //データ取得
        $distQuery = knjl378qQuery::getDistScore();
        $distReslut = $db->query($distQuery);
        
        //海外
        $outCountry = array();
        //県外(県内扱い含む)
        $outPref = array();
        //県内
        $inPref = array();
        
        $maxData = 0;
        $minData = 300;
        
        while($distRow = $distReslut->fetchRow(DB_FETCHMODE_ASSOC)){
                if($distRow["OUT_COUNTRY"] != ""){
                    //海外
                    $outCountry[$distRow["SEX"]][] = $distRow["OUT_COUNTRY"];
                    $score = $distRow["OUT_COUNTRY"];
                }else if($distRow["OUT_PREF"] != ""){
                    //県外
                    $outPref[$distRow["SEX"]][] = $distRow["OUT_PREF"];
                    $score = $distRow["OUT_PREF"];
                }else{
                    //県内
                    $inPref[$distRow["SEX"]][] = $distRow["IN_PREF"];
                    $score = $distRow["IN_PREF"];
                }
                
                //最大値から最小値まででRANGをとるなら使う
                if($maxData < $score){
                    $maxData = $score;
                }
                if($score < $minData){
                    $minData = $score;
                }
        }

        
        //最大値と最小値から幅を決めるときには$maxから$minまでで出来るように。
        $max = $maxData - ($maxData%5);
        $min = $minData - ($minData%5);
        
        $range = 1; //SAT_AREA_DIST_DATのRANGE用
        
        $this->areaUpdate = array();
        $this->areaUpdate["REGISTERCD"] = STAFFCD;
        $this->areaUpdate["UPDATED"] = "sysdate()";
        $this->areaUpdate["YEAR"] = CTRL_YEAR;
        
        for($i=$max;$i>=$min;$i=$i-5){
            for($j=0;$j<2;$j++){
                $sex = $j+1;
                //海外
                
                if(get_count($outCountry) > 0){
                    $outCount[$j] = 0;
                    $outCnt = 0;
                    foreach($outCountry[$sex] as $key => $val){
                        if($val >= $i){
                            $outCnt = $outCnt+1;
                        }
                    }
                    $outCount[$j] = $outCnt;
                }

                //県外
                if(get_count($outPrefCnt) > 0){
                    $outPrefCnt[$j] = 0;
                    $outPCnt = 0;
                    foreach($outPref[$sex] as $key1 => $val1){
                        if($val1 >= $i){
                            $outPCnt = $outPCnt+1;
                        }
                    }
                    $outPrefCnt[$j] = $outPCnt;
                }

                //県内
                if(get_count(inPref) > 0){
                    $inPrefCnt[$j] = 0;
                    $inPCnt = 0;
                    foreach($inPref[$sex] as $key2 => $val2){
                        if($val2 >= $i){
                            $inPCnt = $inPCnt+1;
                        }
                    }
                    $inPrefCnt[$j] = $inPCnt;
                }
            }
            
            //データ更新
            $this->areaUpdate["RANGE"] = $range;
            $this->areaUpdate["SCORE"] = $i;
            
            $this->areaUpdate["IN_PREF_MALE"]   = $inPrefCnt[0];
            $this->areaUpdate["IN_PREF_FEMALE"] = $inPrefCnt[1];
            
            $this->areaUpdate["OUT_PREF_MALE"]   = $outPrefCnt[0];
            $this->areaUpdate["OUT_PREF_FEMALE"] = $outPrefCnt[1];
            
            $this->areaUpdate["OUT_COUNTRY_MALE"]   = $outCount[0];
            $this->areaUpdate["OUT_COUNTRY_FEMALE"] = $outCount[1];
            
            common::_update("SAT_AREA_DIST_DAT", "", "", $this->areaUpdate, $db, "2");

            $range++;
        }
        
        
        //SAT_HOPE_RANK_DAT作成
        //最初に対象の年度データ削除
        $query = knjl378qQuery::delSatHopeRank();
        $db->query($query);
        
        //SAT_APP_FORM_MSTとSAT_EXAM_DATからHOPECDとSCORE_TOTALをSAT_HOPE_RANK_DATに挿入
        $query = knjl378qQuery::updateToHopeRank();
        $db->query($query);
        
        //SAT_HOPE_RANK_DATにRANKとCNTを更新
        $query = knjl378qQuery::updateHopeRank();
        $db->query($query);
        
        
        //SAT_HOPE_DIST_DAT作成
        //年度データ削除
        $query = knjl378qQuery::delSatHopeDist();
        $db->query($query);
        
        //SAT_AREA_DIST_DATからSAT_HOPE_DIST_DATを作成
        $query = knjl378qQuery::updateHopeDist();
        $db->query($query);
        
        Query::dbCheckIn($db);
        
        $this->setMessage("更新しました。");
        
    }
    

    //平均値を求める
    function getAverage($values)
    {
        return (float) (array_sum($values) /get_count($values));
    }

    //分散を求める
    function getVariance($values)
    {
        // 平均値を求める
        $ave = round($this->getAverage($values),1);
        $this->ave = $ave;
        
        $variance = 0.0;
        foreach ($values as $val) {
            $variance += pow($val - $ave, 2);
        }
        // 標本抽出の場合はget_count($values) ⇒get_count($values) - 1
        return (float) ($variance /get_count($values));
    }

    //標準偏差を求める
    function getStandard_deviation($values)
    {
        // 分散を求める
        $variance = round($this->getVariance($values),1);
 
        // 分散の平方根
        return (float) sqrt($variance);
    }


}
//var_dump($_REQUEST);
?>
