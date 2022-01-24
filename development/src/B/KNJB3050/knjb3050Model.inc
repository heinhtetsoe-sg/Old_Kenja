<?php

require_once('for_php7.php');

class knjb3050Model extends Model {
    var $cmd;
    var $selectdata;
    var $selectdata2;

    //メンバ変数の初期化
    function knjb3050Model() {}

    function init() {
        $this->cmd          = VARS::request("cmd");
        $this->ObjYear      = CTRL_YEAR + 1;
        
        $this->lcFlag = VARS::post("LCFLAG");
        $this->chairCd1 = VARS::post("CHAIRCD1");
        $this->chairCd2 = VARS::post("CHAIRCD2");

        $this->selectdata = array();
        $this->selectdata2 = array();
        $this->chairListMaxCnt = VARS::post("CHAIRLIST_MAX_CNT");
        $this->chairList2MaxCnt = VARS::post("CHAIRLIST2_MAX_CNT");
        for($i=0;$i<$this->chairListMaxCnt;$i++){
            if(VARS::post("CHAIRLIST_VALUE_".$i)){
                $this->selectdata[$i]=explode(',',VARS::post("CHAIRLIST_VALUE_".$i));
            } else {
                $this->selectdata[$i]=array();
            }
        }
        for($i=0;$i<$this->chairList2MaxCnt;$i++){
            if(VARS::post("CHAIRLIST2_VALUE_".$i)){
                $this->selectdata2[$i]=explode(',',VARS::post("CHAIRLIST2_VALUE_".$i));
            } else {
                $this->selectdata2[$i]=array();
            }
        }
        
        $this->date = VARS::post("SELECT_DATE");
        
        if (VARS::get("year")) {
            $this->getParam["year"] = VARS::get("year");
            $this->getParam["semester"] = VARS::get("semester");
            $this->getParam["chaircd"] = VARS::get("chaircd");
            $this->getParam["groupcd"] = VARS::get("groupcd");
            $this->getParam["staffcd"] = VARS::get("staffcd");
            $this->getParam["authority"] = VARS::get("authority");
        }
        $this->getPropertiesAll();
    }

    //更新
    function &getUpdateModel() {
        knjb3050Query::getUpdateQuery($this);
        $this->setMessage("MSG202");
        return;
    }
    
}
?>
