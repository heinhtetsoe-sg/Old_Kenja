<?php

require_once('for_php7.php');


class knjc160Model extends Model {
    
    var $cmd;
    var $year;
    var $month;
    var $limit_date;
    var $ctrl = array();
    var $cmd2;
    
    function knjc160Model()
    {        
        common::GetControlMaster_Fnc($this->ctrl);
    }
    
    function init()
    {  
        $this->year       = VARS::post("year");
        $this->month      = VARS::post("month");
        $this->cmd        = VARS::request("cmd");
        $this->cmd2       = VARS::post("cmd2");
        $this->OUTPUT       = VARS::post("OUTPUT"); //add  05/02/24  yamauchi
        $this->DBNAME     = VARS::post("DBNAME");   //add  05/01/24  yamauchi

        $this->getPropertiesAll(); //プロパティファイルの読込み

    }
    
    //入力項目チェック
    function validateInput()
    {
        return true;
    }
    
    //更新
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            
            $this->update();
            $this->setMessage("MSG201");
        }
    }
    
    function &update()
    {
        $result = knjc160Query::getUpdateQuery($this);
                    
        return $result;
    }
    
    function &ChangeAttendDate()
    {
        /*******************************************
            セッション変数の変更(出欠制御日付)
         *******************************************/        
        global $sess;
        //セッション変数に保存
        $session = new APP_Session($sess->id, 'SESS_CTRL');
        $session->unregister("ATTEND_CTRL_DATE");
        $session->register("ATTEND_CTRL_DATE", $this->limit_date);
    }
}
?>
