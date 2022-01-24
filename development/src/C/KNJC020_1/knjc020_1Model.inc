<?php

require_once('for_php7.php');

class knjc020_1Model extends Model
{
    var $cmd;
    var $chaircd;
    var $t_Staffcd;
    var $t_carcd;
    var $priod;
    var $executedate;
    var $semester;
    var $mode;
    var $staffcd;
    var $executed_flg;
    var $chenge_flg;
    var $update_type;
    var $classCheck;
    var $hrClasses = array();

    function getMicrotime(){
        list($usec, $sec) = explode(" ",microtime());
        return ((float)$sec + (float)$usec);
    }

    //コンストラクタ
    function knjc020_1Model()
    {
    }

    function init()
    {
        $this->cmd = VARS::post("cmd");
        $this->update_type = VARS::request("update_type");
        $this->chaircd = VARS::post("chaircombo");
        $this->executed_flg = VARS::post("executed_flg");
        $this->chenge_flg = VARS::request("chg");
        $this->classCheck = VARS::post("CLASS_CHECK");
        if(VARS::get("datekey")) $this->executedate  = VARS::get("datekey");
        if(VARS::get("semes"))   $this->semester     = VARS::get("semes");
        if(VARS::get("stfcd"))   $this->t_Staffcd    = VARS::get("stfcd");
        if(VARS::get("carcd"))   $this->t_carcd      = VARS::get("carcd");
        if(VARS::get("md"))      $this->mode         = VARS::get("md");
        if(!is_null(VARS::get("pd"))) $this->priod   = VARS::get("pd");

        //セキュリティーチェック
        $this->staffcd = STAFFCD;

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            if ($this->cmd == "update") {
                if($this->update()){
                    $this->setMessage("MSG201");
                }
            } else {
                if($this->updExecute()){
                    $this->setMessage("MSG201");
                }
            }
        }
        return;
    }

    function &updExecute()
    {
        //編集対象データの書込み処理
        $result = knjc020_1Query::getUpdateSchChrQuery($this);
        return $result;
    }

    function &update()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if($this->chaircd == "" || is_null($this->chaircd)){
            $this->chaircd = $this->t_carcd;
        }

        $query = knjc020_1Query::getSchChrCountflgQuery($this);
        $result = $db->query($query);
        $lessonModeArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $lessonModeArray[$row["GRADE"].$row["HR_CLASS"]] = $row["LESSON_MODE"];
        }
        $result->free();

        $query = knjc020_1Query::getDeleteQuery($this);
        $db->query($query);

        foreach ($this->hrClasses as $key => $val) {
            list($grade, $hrClass) = preg_split("/-/", $val);
            $query = knjc020_1Query::getInsertQuery($this, $grade, $hrClass, $lessonModeArray[$grade.$hrClass]);
            $db->query($query);
        }

        if (is_array($this->classCheck)) {
            foreach ($this->classCheck as $key => $val) {
                $query = knjc020_1Query::getUpdateQuery($this, $val);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //年度内の処理か？チェック
    function checkCtrlDay($day)
    {
        $day = str_replace("/","-",$day);

        //年度内の処理に限る
        if((CTRL_YEAR."-04-01" <= $day && $day < (CTRL_YEAR + 1)."-04-01"))
        {
            return true;
        }else{
            $this->setWarning("MSG915","(処理年度範囲外)");
            return false;
        }
        
    }
}
?>
