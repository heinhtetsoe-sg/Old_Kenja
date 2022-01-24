<?php

require_once('for_php7.php');

//メニュー画面
class knjm250eModel extends model {
    var $cmd;                   //一覧表示時のcmd
    var $ObjYear;               //年度
    var $sub;                   //講座コード+科目コード
    var $repcntall;             //回数
    var $repstartcnt;           //開始回数
    var $setdata = array();     //表示データ配列
    var $kaisu = array();       //回数
    var $error_flg = false;     //入力エラーフラグ

    function knjm250eModel() {
        $this->ObjYear = CTRL_YEAR;
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->ObjYear);
            unset($this->sub);
            unset($this->repcntall);
            unset($this->repstartcnt);
            $this->setdata = array();
        }
        $this->cmd = VARS::request("cmd");

        $this->ObjYear = VARS::request("YEAR");

        $this->sub = VARS::post("SELSUB");

        $this->coursemajor = VARS::post("COURSEMAJOR");

        if (VARS::post("REPSTARTCNT")) {
            $this->repstartcnt = VARS::post("REPSTARTCNT");
        }
        if (VARS::post("REPCNTALL")) {
            $this->repcntall = VARS::post("REPCNTALL");
        }

        if ($this->cmd != "reset" && $this->cmd != "chg_subclass"){
            for ($ini = $this->repstartcnt ; $ini < ($this->repstartcnt + $this->repcntall) ; $ini++){
                $this->setdata["REPDIV"][$ini]  = VARS::post("REPDIV".$ini);
                $this->setdata["ENDDAY"][$ini]  = VARS::post("ENDDAY".$ini);
                $this->setdata["BASEDAY"][$ini] = VARS::post("BASEDAY".$ini);
                $this->setdata["RETRYDAY"][$ini] = VARS::post("RETRYDAY".$ini);
                $this->setdata["REMARK"][$ini]  = VARS::post("REMARK".$ini);
            }
        } else {
            $this->setdata = array();
        }
        $this->getPropertiesAll();  //プロパティファイル
    }

    //入力項目チェック
    function validateInput() {
        $this->error_flg = false;
        $checdata = array();
        $checcnt  = array();
        $checary  = array();
        $s = 0;

        for ($i = $this->repstartcnt; $i < ($this->repstartcnt + $this->repcntall) ; $i++){

            $this->setdata["REPDIV"][$i] = VARS::post("REPDIV".$i);
            $this->setdata["ENDDAY"][$i] = VARS::post("ENDDAY".$i);
            $this->setdata["BASEDAY"][$i] = VARS::post("BASEDAY".$i);
            $this->setdata["RETRYDAY"][$i] = VARS::post("RETRYDAY".$i);
            $this->setdata["REMARK"][$i] = VARS::post("REMARK".$i);
            if ($this->setdata["REPDIV"][$i] == "" || $this->setdata["BASEDAY"][$i] == ""){
                $this->setWarning("レポート区分、提出基準日は必須です。");
                return false;
            }
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            $this->delete();
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    function &update() {
        $result = knjm250eQuery::getUpdateQuery($this);
        return $result;
    }

    function &delete() {
        $result = knjm250eQuery::getDeleteQuery($this);
        return $result;
    }
}
?>
