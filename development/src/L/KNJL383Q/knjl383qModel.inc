<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl383qModel extends Model {
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
    function knjl383qModel()
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


        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル


    }
    function &getMainModel()
    {
    }

    
    //UPDATE
    function UpdateModel()
    {
        //SAT_JUDGE_STANDARD_DATにCOMMENTNOを更新する
        if($this->JudgeStandardUpdate()){
            //先に教科コメントの点数のエラーチェックをする
            if($this->KyoukaErrorCheck()){
                //総合評価と成績コメントを更新
                $this->ExamJudgeUpdate();
                
                //教科ごとのコメントを更新
                $this->KyoukaCommentUpdate();
                
                $this->setMessage("更新しました。");
                return true;
            }else{
                $table = array("英語","国語","数学");
                $errorNo = explode(",", $this->ErrorNo);
                
                $message = "";
                $cnm = "";
                foreach($errorNo as $key => $val){
                    $message .= $cnm.$table[$val];
                    $cnm = " / ";
                }
                
                $this->setMessage("以下の教科のコメント適用点数範囲が重複しているため、\\n更新できませんでした。\\n\\n".$message);
                return false;
            }
            
        }else{
            //SAT_JUDGE_STANDARD_DATに得点が入ってるのにコメント番号がないものが存在したらエラー
            $this->setMessage("総合評価に対して、\\n成績評価コメントが存在しないデータがあるため、\\n成績評価を確定できませんでした。");
            return false;
        }
    }

    //SAT_JUDGE_STANDARD_DATにCOMMENTNOを更新
    function JudgeStandardUpdate()
    {
        $db = Query::dbCheckOut();
        
        //SAT_COMMENT_JUDGE_DATのデータを取得
        $judgeQuery = knjl383qQuery::getJudgeComment();
        $judgeResult = $db->query($judgeQuery);
        
        $this->JudgeCmt = array();
        $this->JudgeCmt["REGISTERCD"] = STAFFCD;
        $this->JudgeCmt["UPDATED"] = "sysdate()";
        
        //重複して更新したくないので
        $upNo = array();
        
        while($judgeRow = $judgeResult->fetchRow(DB_FETCHMODE_ASSOC)){
            if($judgeRow["COMMENTNO"] != ""){
                $this->JudgeCmt["COMMENTNO"] = $judgeRow["COMMENTNO"];
            }else{
                $this->JudgeCmt["COMMENTNO"] = " ";
            }
            
            $updateNo = $judgeRow["JUDGE"]."/".$judgeRow["INOUT_KUBUN"];
            
            if(!empty($upNo) && in_array($updateNo, $upNo)){
                //一度更新したデータはNULLで上書きするのを回避したい
                //（NULLで更新した後に数字で更新しようとしたときはそのまま更新する）
                if($this->JudgeCmt["COMMENTNO"] == " "){
                    continue;
                }
            }
            //更新した番号を配列に保持
            $upNo[] = $updateNo;

            
            $where = " WHERE JUDGE = '".$judgeRow["JUDGE"]."' AND INOUT = '".$judgeRow["INOUT_KUBUN"]."' AND YEAR = '".CTRL_YEAR."' ";
            
            common::_update("SAT_JUDGE_STANDARD_DAT", "", $where, $this->JudgeCmt, $db, "1");
            
        }
        
        //更新したらSAT_JUDGE_STANDARD_DATのSCORE_FROMが入っているのにCOMMENTNOがNULLのデータをカウントする。
        $JudgeCntQuery = knjl383qQuery::getJudgeNullCnt();
        $judgeCnt = $db->getOne($JudgeCntQuery);
        if($judgeCnt > 0){
            $flg = 1;
        }else{
            $flg = 0;
        }
        
        Query::dbCheckIn($db);
        
        if($flg != 0){
            return false;
        }else{
            return true;
        }
    }
    
    //総合判定と評価コメントの更新
    function ExamJudgeUpdate()
    {
        $db = Query::dbCheckOut();
        
        //SAT_JUDGE_STANDARD_DATを取得
        $satJudgeQuery = knjl383qQuery::getSatJudge();
        $satJudgeResult = $db->query($satJudgeQuery);
        
        while($satJudgeRow = $satJudgeResult->fetchRow(DB_FETCHMODE_ASSOC)){
            //SAT_EXAM_DATを更新したい 一度得点の範囲だけでCOMMENTNOを更新する
            $upSatExamQuery = knjl383qQuery::upSatExamJudge($satJudgeRow);
            $db->query($upSatExamQuery);
        }
        
        //1教科でも40点以下で平均点と20点以上差があるデータのCOMMENTNOを書き換える
        $upSatExamCmntQuery = knjl383qQuery::upSatExamJudegeComment();
        $db->query($upSatExamCmntQuery);
        
        Query::dbCheckIn($db);
    }
    
    //教科ごとのコメントを更新
    function KyoukaCommentUpdate()
    {
        $db = Query::dbCheckOut();
            
        
        //テーブルと教科の指定に使う
        $table = array("ENGLISH", "JAPANESE", "MATH");
        
        for($i=0;$i<3;$i++){
            //コメントデータ取得
            $kyoukaQuery = knjl383qQuery::getKyoukaComment($table[$i]);
            $kyoukaResult = $db->query($kyoukaQuery);
            
            while($kyoukaRow = $kyoukaResult->fetchRow(DB_FETCHMODE_ASSOC)){
                //SAT_EXAM_DATの更新
                $upSatExamQuery = knjl383qQuery::upSatExamComment($table[$i], $kyoukaRow);
                $db->query($upSatExamQuery);
            }
            
            
        }
        
        
        Query::dbCheckIn($db);
    }
    
    //教科コメントの点数が重複していないかをチェック
    function KyoukaErrorCheck()
    {
        $db = Query::dbCheckOut();
        
        $this->ErrorNo = "";
        $cnm = "";
        
        //テーブルと教科の指定に使う
        $table = array("ENGLISH", "JAPANESE", "MATH");
        
        for($i=0;$i<3;$i++){
            //データ取得
            $dataQuery = knjl383qQuery::getData($table[$i]);
            $dataResult = $db->query($dataQuery);
            $cnt = 0;
            $data = array();
            
            while($dataRow = $dataResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $data[$cnt]["MIN"] = $dataRow["SCORE_FROM"];
                $data[$cnt]["MAX"] = $dataRow["SCORE_TO"];
                
                $cnt++;
            }
            $errorCnt = 0;
            $count = get_count($data);
            $count = $count - 1;
            foreach($data as $key => $val){
                if($key < $count){
                    $k = $key + 1;
                    $max = $val["MAX"] + 1;
                    if($max != $data[$k]["MIN"]){
                        $errorCnt++;
                        break;
                    }
                }
            }
            
            if($errorCnt>0){
                $this->ErrorNo .= $cnm.$i;
                $cnm = ",";
            }
        
        }
        
        Query::dbCheckIn($db);
        
        if($this->ErrorNo != ""){
            return false;
        }else{
            return true;
        }
    }


}
//var_dump($_REQUEST);
?>
