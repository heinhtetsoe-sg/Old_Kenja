<?php

require_once('for_php7.php');


class knjz175Model extends Model {
    var $cmd;
    var $staffcd;               //ログインユーザーID保持
    var $sec_competence;        //権限保持
    var $control_data;          //処理年度保持
    var $selectdata;

    function knjz175Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = common::SecurityCheck($this->staffcd, "KNJZ175");

       if (VARS::get("PROGRAMID")) {
           $this->pgroupcd   = "";
           $this->pgroupname = "";
           $this->plessoncnt = "";
           $this->pframecnt  = "";
       }

        $this->cmd  = VARS::request("cmd");

        if(VARS::get("PGROUPCD")!=""){
            $this->pgroupcd   = VARS::get("PGROUPCD");
        }
        if(VARS::get("PGROUPNAME")!=""){
            $this->pgroupname = VARS::get("PGROUPNAME");
        }
#        if(VARS::get("PLESSONCNT")!=""){
#            $this->plessoncnt = VARS::get("PLESSONCNT");
#        }else{
#            $this->plessoncnt = "";
#        }
#        if(VARS::get("PFRAMECNT")!=""){
#            $this->pframecnt = VARS::get("PFRAMECNT");
#        }else{
#            $this->pframecnt  = "";
#        }

        $tmpl = VARS::get("PLESSONCNT");
        if(isset($tmpl)) {
            $this->plessoncnt = $tmpl;
        }
        $tmpf = VARS::get("PFRAMECNT");
        if(isset($tmpf)){
            $this->pframecnt = $tmpf;
        }

        //子群
        $this->selectdata = VARS::post("selectdata");
    }

    //更新前チェック
    function validateInput()
    {
        if($this->pgroupcd == ""){
            $this->setWarning("MSG304");
            return false;
        }
        if($this->selectdata != ""){

            $db = Query::dbCheckOut();

            $result = $db->query(knjz175Query::cntCgrp($this));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if(($this->plessoncnt < $row["MAXLESSON"]) || ($this->pframecnt < $row["MAXFRAME"])){
                    $this->setWarning('MSG901',"親群の授業回数、連続枠数を越える値が設定されています。");
                    return false;
                }
            }
            Query::dbCheckIn($db);
        }
        return true;
    }

    //前年度からのコピーのモデルを返します
    function &getCopyModel(){
        $cntelectclass = knjz175Query::cntElectRela(CTRL_YEAR);
        if($cntelectclass > 0){
            $this->setWarning('MSG203',"データが存在します。");
            return false;
        } else {
            $result = knjz175Query::getCopyQuery($this);
        }
    }

    //更新用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage('MSG201');
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        knjz175Query::getUpdateQuery($this);
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
