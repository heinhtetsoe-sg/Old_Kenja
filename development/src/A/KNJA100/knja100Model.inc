<?php

require_once('for_php7.php');


class knja100Model extends Model {

    var $cmd;
    var $semester;
    var $sec_competence;
    var $max_semester;
    var $next_semester;
    function knja100Model()
    {
    }
    
	function init()
    {   
    
        $db = Query::dbCheckOut();
        $query = knja100Query::getSemester_Range();
        $result = $db->query($query);
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $this->max_semester       = $row["MAX_SEMESTER"];      //MAX学期
        $query = knja100Query::getSchoolMst();
        $this->schooldiv = $db->getOne($query);
        Query::dbCheckIn($db);

        $this->next_semester=CTRL_SEMESTER+1;
        $this->semester     = VARS::post("semester");       //設定学期
        $this->cmd          = VARS::request("cmd");

        $this->sec_competence = common::SecurityCheck(STAFFCD,PROGRAMID);
    }
    
    //入力項目チェック
    function validateInput()     
    {
        //クラス編成データの存在チェック
        $db = Query::dbCheckOut();
        $cnt_record = $db->getOne(knja100Query::getExistsClassFormation($this));
        Query::dbCheckIn($db);
        if (0 < $cnt_record) {
        } else {
            $this->warning = "事前処理（HRクラス作成、HR割振り、出席番号・コース設定）が行われていません";
            return false;
        }
        return true;
    }
    
    //更新
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput())
        {
            if (knja100Query::getUpdateQuery($this))
            {
                $this->setMessage("MSG201");   //「データを更新しました。」
            } else{
            } 
        }
    }
    
}
?>
