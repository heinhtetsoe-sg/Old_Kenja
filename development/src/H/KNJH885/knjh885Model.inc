<?php

class knjh885Model extends Model {

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

    function knjh885Model() {
        common::GetControlMaster_Fnc($this->control_data);
    }
 
    function init()
    {
        $this->getPropertiesAll(); //プロパティファイル

        //DB接続
        $db = Query::dbCheckOut();

        $this->cmd      = VARS::request("cmd");
        $this->gakuseki = VARS::request("GAKUSEKI") != "" ? VARS::request("GAKUSEKI") : $this->gakuseki;
        
        $this->check = VARS::request("CHECK");

        $this->field = array(
                             "KUBUN"            =>  VARS::post("KUBUN"),
                             
                             "G_HR"             =>  VARS::post("G_HR"),
                             
                             "GHR_CHOICE"       =>  VARS::post("GHR_CHOICE"),
                             
                             "CHECKALL"         =>  VARS::post("CHECKALL"),
                             
                             "CHECK"            =>  VARS::post("CHECK") != "" ? VARS::post("CHECK") : array(),
                             
                             "SELECTID"         =>  VARS::post("SELECTID"),
                             );
        //学年かクラスかを切り替えたとき
        if($this->cmd == "change"){
            $this->field["GHR_CHOICE"] = "";
            $this->field["SELECTID"] = "";
            $this->field["CHECK"] = array();
            $this->field["CHECKALL"] = "";
        }else if($this->cmd == "change_sec"){
            $this->field["CHECK"] = array();
            $this->field["SELECTID"] = "";
            $this->field["CHECKALL"] = "";
        }
        
        //DB切断
        Query::dbCheckIn($db);

    }
    
    //UPDATE
    function getUpdateModel()
    {
        //DB接続
        $db = Query::dbCheckOut();
        
        $this->insert = array();
        $this->insert["REGISTERCD"] = STAFFCD;
        $this->insert["UPDATED"] = "sysdate()";
        
        //割り当てたKNJID
        $knjid = explode(",", $this->field["SELECTID"]);
        
        foreach($this->field["CHECK"] as $key => $val){
            if($this->field["KUBUN"] == "1"){
                $this->insert["STAFFCD"] = $val;
                $table = "PV_STAFF_MST";
            }else{
                $this->insert["SCHREGNO"] = $val;
                $table = "PV_SCHREG_MST";
            }
            
            $this->insert["KNJID"] = $knjid[$key];
            
            if($this->insert["KNJID"] != ""){
                common::_update($table, "", "", $this->insert, $db, "2");
            }
        }
        
        //LOGINIDをPV_CBT_USER_DATからUPDATEする
        $query = knjh885Query::LoginUpdate($table);
        $db->query($query);
        
        //DB切断
        Query::dbCheckIn($db);
        
        $this->setMessage("更新しました。");
        
        $this->field["CHECK"] = array();
        $this->field["SELECTID"] = "";
        $this->field["CHECKALL"] = "";

        return true;
    }
    

}
?>
