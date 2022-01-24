<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh400_kyoukaModel extends Model {
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
    function knjh400_kyoukaModel()
    {
    }
    function init()
    {              
        $this->cmd      = VARS::request("cmd");
        $this->GAKUSEKI = VARS::request("SCHREGNO") != "" ? VARS::request("SCHREGNO") : $this->GAKUSEKI;
        //$this->GAKUSEKI = '13100020';

        $this->year = VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->year;
        
        //画面左側
        $this->left_field = array("NENDO"   =>  VARS::post("NENDO") != "" ? VARS::post("NENDO") : $this->year ,
        
                                  "GYOUSYA" =>  VARS::post("GYOUSYA") != "" ? VARS::post("GYOUSYA") : "" ,
                                  
                                  "MOCKCD" => VARS::post("MOCKCD") != "" ? VARS::post("MOCKCD") : "" ,
                                  
                                  "CHECK"  =>   VARS::post("CHECK"),
                                  
                                  "KATA"   =>  VARS::post("KATA") != "" ? VARS::post("KATA") : 0,
                                  
                                 );
        
        //三教科ボタン押したとき
        if($this->cmd == "left_sanka"){
            if($this->left_field["KATA"] == "0"){
                $this->left_field["CHECK"] = array("100101", "100201", "100401");
            }else{
                if($this->left_field["GYOUSYA"] != "00000001"){
                    $this->left_field["CHECK"] = array("202001", "202101");
                }else{
                    $this->left_field["CHECK"] = "";
                }
            }
        }else if($this->cmd == "left_appear"){
            $this->left_field["CHECK"] = "";
        }
        
        
        //画面右側
        $this->right_field = array("NENDO"   =>  VARS::post("NENDO") != "" ? VARS::post("NENDO") : $this->year ,
        
                                  "GYOUSYA" =>  VARS::post("GYOUSYA") != "" ? VARS::post("GYOUSYA") : "" ,
                                  
                                  "MOCKCD" => VARS::post("MOCKCD") != "" ? VARS::post("MOCKCD") : "" ,
                                  
                                  "CHECK"  =>   VARS::post("CHECK"),
                                  
                                  "KATA"   =>  VARS::post("KATA") != "" ? VARS::post("KATA") : 0,
                                  
                                 );
        
        //三教科ボタン押したとき
        if($this->cmd == "right_sanka"){
            if($this->right_field["KATA"] == "0"){
                $this->right_field["CHECK"] = array("100101", "100201", "100401");
            }else{
                if($this->left_field["GYOUSYA"] != "00000001"){
                    $this->right_field["CHECK"] = array("202001", "202101");
                }else{
                    $this->right_field["CHECK"] = "";
                }
            }
        }else if($this->cmd == "right_appear"){
            $this->right_field["CHECK"] = "";
        }
    }
    
    /*挿入*/
    function getInsertModel()
    {
        unset($this->update);
        unset($this->grpup);
        unset($this->indup);
        
        //GROUPが選択されてるかのエラーチェック
        if($this->selmode == ""){
            if(!is_array($this->field["SELECT_GROUP"])){
                $this->setMessage("対象のグループを選択してください");
                $this->error = "1";
                return false;
            }else{
                $this->error = "";
            }
        }else{
            if($this->selectdata == ""){
                $this->setMessage("対象の個人を選択してください");
                $this->error = "1";
                return false;
            }else{
                $this->error = "";
            }
        }
        $db = Query::dbCheckOut();
        
        //OSHIRASE_TBL挿入
        //OSHIRASE_TBLのDATA_NOの最大値取得
        $maxQuery = knjh400_kyoukaQuery::getMax("TBL");
        $tblMax = $db->getOne($maxQuery);
        $tblMax = $tblMax + 1;
        
        $this->update["DATA_NO"] = $tblMax;
        $this->update["ENTRY_DATE"] = date("Y-m-d");
        $this->update["ANNOUNCE"] = $this->field["ANNOUNCE"];
        $this->update["START_DATE"] = str_replace("/", "-", $this->field["FROM"]);
        $this->update["END_DATE"] = str_replace("/", "-", $this->field["TO"]);
        $this->update["STAFFCD"] = STAFFCD;
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] ="sysdate()";
        
        common::_update("OSHIRASE_TBL", "", "", $this->update, $db, "2");
        
        if($this->selmode == ""){
            //OSHIRASE_GRPの挿入
            //先に共通のもの入れておく
            $this->grpup["OSHIRASE_NO"] = $tblMax;
            $this->grpup["REGISTERCD"] = STAFFCD;
            $this->grpup["UPDATED"] = "sysdate()";
            
            //OSHIRASE_GRPのDATA_NOの最大値取得
            $maxQuery = knjh400_kyoukaQuery::getMax("GRP");
            $grpMax = $db->getOne($maxQuery);
            $grpMax = $grpMax + 1;

            //選択されたグループを1個ずつ更新
            foreach($this->field["SELECT_GROUP"] as $key => $val){
                $this->grpup["DATA_NO"] = $grpMax + $key;
                
                $this->grpup["GROUP_CD"] = $val;

                common::_update("OSHIRASE_GRP", "", "", $this->grpup, $db, "2");
            }
        }else{
            //OSHIRASE_INDの挿入
            $this->indup["OSHIRASE_NO"] = $tblMax;
            $this->indup["REGISTERCD"] = STAFFCD;
            $this->indup["UPDATED"] = "sysdate()";
            
            //OSHIRASE_INDのDATA_NOの最大値取得
            $maxQuery = knjh400_kyoukaQuery::getMax("IND");
            $indMax = $db->getOne($maxQuery);
            $indMax = $indMax + 1;
            
            //選択された個人を配列にする
            $selindiv = explode(",", $this->selectdata);

            //配列で重複している物を削除する
            $unique = array_unique($selindiv);

            //キーが飛び飛びになっているので、キーを振り直す
            $new_selindiv = array_values($unique);


            //1こずつ更新
            foreach($new_selindiv as $key => $val){
                $this->indup["DATA_NO"] = $indMax + $key;
                
                $this->indup["STAFFCD"] = $val;
                
                common::_update("OSHIRASE_IND", "", "", $this->indup, $db, "2");
            }
        }
        
        $this->setMessage("追加しました");
        
        Query::dbCheckIn($db);
        
        return true;
    }
    
    /*更新*/
    function getUpdateModel()
    {
        unset($this->update);
        unset($this->grpup);
        unset($this->indup);
        
        //GROUPが選択されてるかのエラーチェック
        if($this->selmode == ""){
            if(!is_array($this->field["SELECT_GROUP"])){
                $this->setMessage("対象のグループを選択してください");
                $this->error = "1";
                return false;
            }else{
                $this->error = "";
            }
        }else{
            if($this->selectdata == ""){
                $this->setMessage("対象の個人を選択してください");
                $this->error = "1";
                return false;
            }else{
                $this->error = "";
            }
        }
        $db = Query::dbCheckOut();
        
        //OSHIRASE_TBLの更新
        $this->update["ANNOUNCE"] = $this->field["ANNOUNCE"];
        $this->update["START_DATE"] = str_replace("/", "-", $this->field["FROM"]);
        $this->update["END_DATE"] = str_replace("/", "-", $this->field["TO"]);
        
        $this->update["ENTRY_DATE"] = date("Y-m-d");
        $this->update["STAFFCD"] = STAFFCD;
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] = "sysdate()";
        
        $where = " WHERE DATA_NO = '".$this->seldata."' ";
        
        common::_update("OSHIRASE_TBL", "", $where, $this->update, $db, "1");
        
        
        if($this->selmode == ""){
            //OSHIRASE_GRPの更新
            
            //元が個人対象だったらOSHIRASE_IND削除したい
            $cntQuery = knjh400_kyoukaQuery::getCount($this->seldata, "IND");
            $cnt = $db->getOne($cntQuery);
            
            if($cnt > 0){   //OSHIRASE_INDにあったらOSHIRASE_GRPにはないはず
                $delQuery = knjh400_kyoukaQuery::delete($this->seldata, "IND");
                $db->query($delQuery);
            }else{
                $delQuery = knjh400_kyoukaQuery::delete($this->seldata, "GRP");
                $db->query($delQuery);
            }
            
            //消した後でOSHIRASE_GRPのDATA_NOの最大値を取得
            $maxQuery = knjh400_kyoukaQuery::getMax("GRP");
            $maxNo = $db->getOne($maxQuery);
            $maxNo = $maxNo + 1;
            
            //選択されたグループを1個ずつ更新
            foreach($this->field["SELECT_GROUP"] as $key => $val){
                $this->grpup["DATA_NO"] = $maxNo + $key;
                $this->grpup["OSHIRASE_NO"] = $this->seldata;
                
                $this->grpup["GROUP_CD"] = $val;
                
                $this->grpup["REGISTERCD"] = STAFFCD;
                $this->grpup["UPDATED"] = "sysdate()";
                
                common::_update("OSHIRASE_GRP", "", "", $this->grpup, $db, "2");
            }
        }else{
            //OSHIRASE_INDの更新

            //元がグループ対象だった可能性があるのでOSHIRASE_GRPも削除したい
            $cntQuery = knjh400_kyoukaQuery::getCount($this->seldata, "GRP");
            $cnt = $db->getOne($cntQuery);
            
            if($cnt > 0){   //OSHIRASE_GRPにあったらOSHIRASE_INDにはないはず
                $delQuery = knjh400_kyoukaQuery::delete($this->seldata, "GRP");
                $db->query($delQuery);
            }else{
                $delQuery = knjh400_kyoukaQuery::delete($this->seldata, "IND");
                $db->query($delQuery);
            }
            
            //消した後でOSHIRASE_INDのDATA_NOの最大値を取得
            $maxQuery = knjh400_kyoukaQuery::getMax("IND");
            $maxNo = $db->getOne($maxQuery);
            $maxNo = $maxNo + 1;
            
            //選択された個人を配列にする
            $selindiv = explode(",", $this->selectdata);

            //配列で重複している物を削除する
            $unique = array_unique($selindiv);

            //キーが飛び飛びになっているので、キーを振り直す
            $new_selindiv = array_values($unique);


            //1こずつ更新
            foreach($new_selindiv as $key => $val){
                $this->indup["DATA_NO"] = $maxNo + $key;
                $this->indup["OSHIRASE_NO"] = $this->seldata;
                
                $this->indup["STAFFCD"] = $val;
                $this->indup["REGISTERCD"] = STAFFCD;
                $this->indup["UPDATED"] = "sysdate()";
                
                common::_update("OSHIRASE_IND", "", "", $this->indup, $db, "2");
            }
        }
        $this->setMessage("更新しました");
        
        Query::dbCheckIn($db);
        
        return true;
    }
    
    /*削除*/
    function getDeleteModel()
    {
        $db = Query::dbCheckOut();

        //DATA_NOでOSHIRASE_TBL, OSHIRASE_GRP, OSHIRASE_GRPを削除する
        $tblQuery = knjh400_kyoukaQuery::delete($this->seldata, "TBL");
        $db->query($tblQuery);
        
        //とりあえずOSHIRASE_GRPの件数をカウントしよう
        $cntQuery = knjh400_kyoukaQuery::getCount($this->seldata, "GRP");
        $cnt = $db->getOne($cntQuery);
        
        if($cnt > 0){
            $grpQuery = knjh400_kyoukaQuery::delete($this->seldata, "GRP");
            $db->query($grpQuery);
        }else{
            $grpQuery = knjh400_kyoukaQuery::delete($this->seldata, "IND");
            $db->query($grpQuery);
        }
        
        $this->setMessage("削除しました");
        
        Query::dbCheckIn($db);
    }
}
?>
