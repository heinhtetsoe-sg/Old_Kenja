<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz650Model extends Model {
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
    function knjz650Model()
    {
    }
    function init()
    {              
        $this->cmd      = VARS::request("cmd");
        
        $this->req_field = array("ANALYSIS"     =>  VARS::request("ANALYSIS") != "" ? VARS::request("ANALYSIS") : $this->req_field["ANALYSIS"],
    
                                 "SPORTS"       =>  VARS::request("SPORTS") != "" ? VARS::request("SPORTS") : $this->req_field["SPORTS"],
                                 
                                 "MOCKCD"       =>  VARS::request("MOCKCD") != "" ? VARS::request("MOCKCD") : $this->req_field["MOCKCD"],
                                 
                                 "KATA"         =>  VARS::request("KATA") != "" ? VARS::request("KATA") : $this->req_field["KATA"],
                                 
                                 "YEAR"         =>  VARS::request("YEAR") != "" ? VARS::request("YEAR") : $this->req_field["YEAR"],
                                );
        
        
        $this->bottom_field = array("CHECK"         =>  VARS::post("CHECK") != "" ? VARS::request("CHECK") : array("110010", "110020", "110030", "110039"),
                                    
                                    );
        if($this->cmd == "bottom_sanka"){
            if($this->req_field["KATA"] == "1"){
                $this->bottom_field["CHECK"] = array("110010", "110020", "110030", "110039");
            }else{
                if($this->req_field["GYOUSYA"] != "2"){
                    $this->bottom_field["CHECK"] = array("120001", "120002");
                }else{
                    $this->bottom_field["CHECK"] = "";
                }
            }
        }else if($this->cmd == "edit"){     //最初に開いたときだけ
            $this->req_field = array();
        }
    }
    
}
?>
