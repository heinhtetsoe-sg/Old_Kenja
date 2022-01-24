<?php

require_once('for_php7.php');
class knjd130oModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $field = array();
    var $gakki;

    function knjd130oModel()
    {
    }

    function init()
    {
       if (VARS::get("PROGRAMID")!="") {
           unset($this->schregno);
       }
       if (VARS::get("PROGRAMID")!="") {
           unset($this->name);
            unset($this->gakki);//NO001
       }
        $this->field = array("TOTALSTUDYTIME"   =>  VARS::post("TOTALSTUDYTIME")        //総合的な学習の時間//NO002
                            ,"SPECIALACTREMARK"   =>  VARS::post("SPECIALACTREMARK")        //特別活動//NO002
                            ,"COMMUNICATION"    =>  VARS::post("COMMUNICATION")         //通信欄
                             );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
            $this->gakki = CTRL_SEMESTER;
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
            unset($this->gakki);//NO001
        }
        //NO001
        if (VARS::post("GAKKI")!="") {
            $this->gakki = VARS::post("GAKKI");
        }

        //プロパティファイル
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYTIME"]) > 128) {//NO002
            $this->setWarning("MSG915","( 総合的な学習の時間は128バイトまでです )");
            return false;
        }
        if (strlen($this->field["SPECIALACTREMARK"]) > 128) {//NO002
            $this->setWarning("MSG915","( 特別活動は128バイトまでです )");
            return false;
        }
        if (strlen($this->field["COMMUNICATION"]) > 128) {
            $this->setWarning("MSG915","( 所見は128バイトまでです )");
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
//        knjd130oQuery::getDeleteQuery($this->schregno);
//        knjd130oQuery::getInsertQuery($this->field,$this->schregno);
        knjd130oQuery::getDeleteQuery($this->schregno, $this->gakki);//NO001
        knjd130oQuery::getInsertQuery($this->field,$this->schregno, $this->gakki);//NO001
        knjd130oQuery::getDeleteQuery2($this->schregno);//NO002
        knjd130oQuery::getInsertQuery2($this->field,$this->schregno);//NO002
        return $result;
    }
}
?>
