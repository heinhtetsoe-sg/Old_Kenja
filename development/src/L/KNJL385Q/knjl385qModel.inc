<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl385qModel extends Model {
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
    function knjl385qModel()
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
                             
                             "AMOUNT3"      =>      VARS::post("AMOUNT3"),
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル

/*
        //年度データチェックに行く
        if($this->cmd == "nendoChange" || $this->cmd == ""){
            $db = Query::dbCheckOut();
            
            $query = knjl385qQuery::getSatInfo($this->field["NENDO"], "1");
            $cntRow = $db->getOne($query);
            if($cntRow > 0){
                //年度データ存在するとき
                $this->updateflg = 1;
                $query = knjl385qQuery::getSatInfo($this->field["NENDO"]);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                
                $this->field = array("NENDO"        =>      $row["YEAR"],
                                     "EXAM_DATE"    =>      $row["EXAM_DATE"],
                                     "AMOUNT1"      =>      $row["EXAM_AMOUNT1"],
                                     "AMOUNT2"      =>      $row["EXAM_AMOUNT2"],
                                    );
            }else{
                $this->updateflg = 0;
                $this->field = array("NENDO"        =>      VARS::post("NENDO") != "" ? VARS::post("NENDO") : CTRL_YEAR,
                                     "EXAM_DATE"    =>      "",
                                     "AMOUNT1"      =>      "",
                                     "AMOUNT2"      =>      "",
                                    );
            }
            
            Query::dbCheckIn($db);
        }
*/

    }
    function &getMainModel()
    {
    }


    //INSERT
    function InsertModel()
    {
        $db = Query::dbCheckOut();
        
        $this->insert["REGISTERCD"] = STAFFCD;
        $this->insert["UPDATED"] = "sysdate()";
        $this->insert["YEAR"] = CTRL_YEAR;
        
        
        $this->insert["YEAR"] = $this->field["NENDO"];
        
        $this->insert["EXAM_DATE"] = str_replace("/", "-", $this->field["EXAM_DATE"]);
        
        $this->insert["EXAM_AMOUNT1"] = $this->field["AMOUNT1"];
        $this->insert["EXAM_AMOUNT2"] = $this->field["AMOUNT2"];
        $this->insert["EXAM_AMOUNT3"] = $this->field["AMOUNT3"];
        
        common::_update("SAT_INFO_MST", "", "", $this->insert, $db, "2");
        
        $this->setMessage("追加しました。");
        
        $this->insert = array();
        
        $this->updateflg = 1;
        
        Query::dbCheckIn($db);
        
    }
    
    //UPDATE
    function UpdateModel()
    {
        $db = Query::dbCheckOut();
        
        
        $this->update["REGISTERCD"] = STAFFCD;
        $this->update["UPDATED"] = "sysdate()";
        
        $this->update["EXAM_DATE"] = str_replace("/", "-", $this->field["EXAM_DATE"]);
        
        $this->update["EXAM_AMOUNT1"] = $this->field["AMOUNT1"];
        $this->update["EXAM_AMOUNT2"] = $this->field["AMOUNT2"];
        $this->update["EXAM_AMOUNT3"] = $this->field["AMOUNT3"];
        
        $where = " WHERE YEAR = '".$this->field["NENDO"]."' ";
        
        common::_update("SAT_INFO_MST", "", $where, $this->update, $db, "1");
        
        $this->setMessage("更新しました。");
        
        $this->update = array();
        
        Query::dbCheckIn($db);
        
    }
    
    //DELETE
    function DeleteModel()
    {
        
        $db = Query::dbCheckOut();
        
        $delquery = knjl385qQuery::DeleteSat($this->field["NENDO"]);
        $db->query($delquery);
        
        $this->setMessage("削除しました。");
        
        Query::dbCheckIn($db);
        
        
    }
    
    
    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString();

        //データの整合性チェック
        if(empty($this->schregerr)){
            if ( $this->IsDataMatched($this->data_arr) ) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knjl385qQuery::insertQueryCsvHead($this, $this->csvHeadData);
                    $cnt = knjl385qQuery::insertQueryCsv($this, $this->data_arr);
                } else {
                    $cnt = knjl385qQuery::DeleteQueryCsv($this, $this->data_arr);
                }
                $db = Query::dbCheckOut();
                $errcnt = $db->getOne("SELECT COUNT(*) FROM W_CSVMSG_DAT");
                Query::dbCheckIn($db);

                $this->setMessage("MSG202","処理済件数　：".$cnt."件\\nエラー件数    ：".$errcnt."件");
            }
        }else{
            $mess = "";
            $br = "";
            foreach($this->schregerr as $key => $val){
                $mess .= $br.$val;
                $br = "\\n";
            }
            $this->setMessage("学籍番号エラー\\n".$mess);
        }
    }


}
//var_dump($_REQUEST);
?>
