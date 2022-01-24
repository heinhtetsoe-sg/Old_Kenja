<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh45msearchModel extends Model {
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
    function knjh45msearchModel()
    {
    }
    function init()
    {              
        $this->cmd      = VARS::request("cmd");
        
        $this->prgid = VARS::request("PROGRAMID") != "" ? VARS::request("PROGRAMID") : $this->prgid;
        
        $this->top_field = array("ANALYSIS"     => VARS::post("ANALYSIS"),
        
                                 "SPORTS"       => VARS::post("SPORTS"),
                                 
                                 "NENDO"        => VARS::post("NENDO"),
                                 
                                 "GAKUNEN"      => VARS::post("GAKUNEN"),
                                 
                                 "GYOUSYA"      => VARS::post("GYOUSYA"),
                                 
                                 "MOCKCD"       => VARS::post("MOCKCD"),
                                 
                                 "KATA"         => VARS::post("KATA"),
                                 
                                );
    }
    
    
}
?>
