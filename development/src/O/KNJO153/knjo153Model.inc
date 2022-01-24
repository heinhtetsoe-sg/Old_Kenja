<?php

class knjo153Model extends Model {

    var $cmd;
    var $cmdSub;
    var $schregno;
    var $delchk;
    var $dividecd;
    var $auth = array();
    var $taitleSort = array();
    var $sort;
    var $staffsort;
    var $datesort;
    var $titlesort;
    var $control_data = array();    //コントロールマスタデータを保持

    function knjo153Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }
 
    function init()
    {
        //DB接続
        //$db = Query::dbCheckOut();

        $this->cmd      = VARS::request("cmd");

        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }
        
        $this->field["GRADE"] = VARS::post("GRADE");
        if($this->schregno != "" && $this->field["GRADE"] == ""){
            $this->field["GRADE"] = "1";
        }
        $this->field["TYPE"] = VARS::post("TYPE");
        if($this->field["TYPE"] == ""){
            $this->field["TYPE"] = "1";
        }
        $this->field["KYOUKA"] = VARS::post("KYOUKA");
        
        if ($this->cmd == "") {
            unset($this->schregno);
        }
        
        
        //DB切断
        //Query::dbCheckIn($db);

    }

}
?>
