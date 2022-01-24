<?php

require_once('for_php7.php');
class knjd130hModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $field = array();
    var $gakki;

    function knjd130hModel()
    {
    }

    function init()
    {
       if (VARS::get("PROGRAMID")!="") {
           unset($this->schregno);
       }
       if (VARS::get("PROGRAMID")!="") {
           unset($this->name);
            unset($this->gakki);
       }
        $this->field = array("TOTALSTUDYTIME"   =>  VARS::post("TOTALSTUDYTIME")        //総合的な学習の時間
                            ,"COMMUNICATION"    =>  VARS::post("COMMUNICATION")         //通信欄
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }

        $this->grade = VARS::get("GRADE");
        $this->hr_class = VARS::get("HR_CLASS");
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->gakki);
        }
        if (VARS::post("GAKKI")!="") {
            $this->gakki = VARS::post("GAKKI");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->virus     = ($this->Properties["useVirus"] == 'true') ? true : false;
        $this->koudome   = ($this->Properties["useKoudome"] == 'true') ? true : false;
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["COMMUNICATION"]) > 258) {
            $this->setWarning("MSG915","( 通信欄は258バイトまでです )");
            return false;
        }
        return true;
    }

    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    function &update()
    {
        knjd130hQuery::getDeleteQuery($this->schregno, $this->gakki);
        knjd130hQuery::getInsertQuery($this->field,$this->schregno, $this->gakki);
        return $result;
    }
}
?>
