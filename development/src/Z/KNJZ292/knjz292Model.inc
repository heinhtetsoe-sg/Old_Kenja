<?php

require_once('for_php7.php');

class knjz292Model extends Model {
    var $field      = array();
    var $cmd;
    var $year;
    var $staffcd;
    var $auth;
    var $prgid;

    function knjz292Model()
    {
    }

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->staffcd);

            unset($this->sendAuth);
            unset($this->sendPrgid);
            unset($this->sendSubmit);
        }

        $this->field = array("IDOU_DIV"             =>  VARS::post("IDOU_DIV"),
                             "IDOU_DATE"            =>  str_replace("/","-",VARS::post("IDOU_DATE")),
                             "ASSIGNMENT_DATE"      =>  str_replace("/","-",VARS::post("ASSIGNMENT_DATE")),
                             "TO_FINSCHOOLCD"       =>  VARS::post("TO_FINSCHOOLCD"),
                             "UPDATED"              =>  VARS::post("UPDATED"),
                             );

        //プロパティファイル
        $this->getPropertiesAll();

        $this->cmd = VARS::request("cmd");

        if (VARS::get("mode") == "1"){
            unset($this->staffcd);
        }

        if (VARS::get("STAFFCD")){
            $this->staffcd  = VARS::get("STAFFCD");
        }

        if (VARS::get("SEND_PRGID")) {
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->sendPrgid    = VARS::get("SEND_PRGID");
            $this->sendSubmit   = VARS::get("SEND_SUBMIT");
        }

        $this->auth = ($this->sendAuth == "") ? AUTHORITY : $this->sendAuth;
        $this->prgid = ($this->sendPrgid == "") ? PROGRAMID : $this->sendPrgid;
    }

   //入力項目チェック
    function validateInput() {

        //DB接続
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $setsize = "";
        $zero_ume = "";
        //STAFFCDフィールドサイズ変更対応
        if ($this->Properties["useStaffcdFieldSize"] === '10') {
            $setsize = 10;
            $zero_ume = "%010d";
        } else {
            $setsize = 8;
            $zero_ume = "%08d";
        }

        //キーのチェック
        if ($this->staffcd  == "" && $this->cmd == "update") {
            $this->setWarning('MSG308');
            return false;
        }

        //１レコード取得
        if ($this->cmd == "update") {
            if ($this->field["IDOU_DIV"] == "") {
                $this->setWarning('MSG301', "(異動区分)");
                return false;
            }
            if ($this->field["IDOU_DATE"] == "") {
                $this->setWarning('MSG301', "(異動日)");
                return false;
            }
            //転出の時
            if ($this->field["IDOU_DIV"] === '2') {
                if ($this->field["ASSIGNMENT_DATE"] == "") {
                    $this->setWarning('MSG304', "(転入日)");
                    return false;
                }
                if ($this->field["TO_FINSCHOOLCD"] == "") {
                    $this->setWarning('MSG304', "(転出先学校)");
                    return false;
                }
            }
        }

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return true;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage('MSG201');
        }    
    }

    //更新
    function &update() {
        knjz292Query::getUpdateQuery($this, $this->field);
    }

    //削除処理
    function &getDeleteModel() {
        //キーの更新チェック
        if ($this->staffcd == "") {
            $this->setWarning('MSG308');
            return false;
        }
        
        //削除
        knjz292Query::getDeleteQuery($this, $this->staffcd);
        $this->setMessage('MSG201');
        $this->staffcd = "";
    }

}
?>
