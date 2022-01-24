<?php

require_once('for_php7.php');

//定数定義
class knjp702_detailModel extends Model {
    var $field = array();
    var $cmd;
    var $year;
    var $exp_lcd;       //会計科目コード
    var $exp_mcd;       //会計項目コード
    var $schoolKind;
    var $tokusyu_cd;

    //メンバ変数の初期化
    function knjp702_detailModel()
    {
    }

    function init()
    {
        if (VARS::request("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->auth);
            unset($this->year);
            unset($this->exp_lcd);
            unset($this->exp_mcd);
            unset($this->schoolKind);
            common::GetControlMaster_Fnc($this->control);
        }

        if (VARS::get("SEND_PRGRID")) {
            $this->getPrgId = VARS::get("SEND_PRGRID");
            $this->auth     = VARS::get("SEND_AUTH");
            
            $this->year     = VARS::get("SEND_YEAR");
            $this->exp_lcd  = VARS::get("SEND_COLLECT_L_CD");
            $this->exp_mcd  = VARS::get("SEND_COLLECT_M_CD");
            $this->schoolKind = VARS::get("SEND_SCHOOLKIND");
        }

        $namevalue_array = explode(',',$this->data["NAMEVALUE"]);
        foreach($namevalue_array as $key => $namevalue) {
            $this->data["TOKUSYU_VAL".$namevalue]     = VARS::post("TOKUSYU_VAL".$namevalue);
        }

        $this->cmd = VARS::request("cmd");
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
    }
    //更新処理
    function &getUpdateModel() {

        $db = Query::dbCheckOut();

        $namevalue_array = explode(',',$this->data["NAMEVALUE"]);
        foreach($namevalue_array as $key => $namevalue) {
            //更新
            if ($this->data["TOKUSYU_VAL".$namevalue] != "") {
                knjp702_detailQuery::getUpdateQuery($db, $namevalue, $this->data["TOKUSYU_VAL".$namevalue], $this);
            //削除
            } else {
                knjp702_detailQuery::getDeleteQuery($db, $namevalue, $this);
            }
        }

        $this->setMessage("MSG201");
        Query::dbCheckIn($db);

        return;
    }
    
    //変数をクリアします
    function clean()
    {
    }
}
?>
