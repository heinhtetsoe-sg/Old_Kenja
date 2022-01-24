<?php

class knjo154Model extends Model {

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

    function knjo154Model() {
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
        
        $this->field["YEAR"] = VARS::post("YEAR");
        if($this->schregno != "" && $this->field["YEAR"] == ""){
            $this->field["YEAR"] = "1";
        }
        
        if ($this->cmd == "") {
            unset($this->schregno);
        }
        
        
        //DB切断
        //Query::dbCheckIn($db);

    }

}
?>
