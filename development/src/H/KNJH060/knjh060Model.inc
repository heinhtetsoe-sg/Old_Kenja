<?php

require_once('for_php7.php');
class knjh060Model extends Model {

    var $cmd;
    var $schregno = "";
    var $semester_arr = array();
    var $field = array();
    var $field2 = array();
    
    function knjh060Model()
    {
    }

    function init()
    {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
        }

        $this->field2 = array("CTRL_YEAR"       =>  VARS::post("CTRL_YEAR"),
							  "CTRL_SEMESTER"   =>	VARS::post("CTRL_SEMESTER"),
							  "CTRL_DATE"		=>	VARS::post("CTRL_DATE"),
                              "SCHREGNO"        =>  VARS::post("SCHREGNO")
                             );
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $caution = "";
        $admonition = "";

        $this->cmd = VARS::request("cmd");

        for ($i = 0; $i<get_count($this->semester_arr); $i++) {
        
            $caution    = (VARS::post("caution_check".$this->semester_arr[$i]) == "1") ? "1" : "0";
            $admonition = (VARS::post("admonition_check".$this->semester_arr[$i]) == "1") ? "1" : "0";
  
            $this->field[$this->semester_arr[$i]] = array("caution"    => $caution,
                                                          "admonition" => $admonition);
        }

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
        }
    }


    //入力項目チェック
    function validateInput()
    {
        if ($this->schregno=="") {
            $this->setWarning("リストから生徒を選択してください");
            return false;
        }

        return true;
    }


    function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        
        $db = Query::dbCheckOut();

        for ($i = 0; $i<get_count($this->semester_arr); $i++) {
        
            //制限付更新可の時は処理学期のみ更新
            if (AUTHORITY == DEF_UPDATE_RESTRICT && $this->semester_arr[$i] != CTRL_SEMESTER) {
                continue; 
            }

            knjh060Query::getDeleteQuery($db,$this->schregno,$this->semester_arr[$i]);

            //チェックボックスにチェックが入っていればインサートする
            if (in_array("1",$this->field[$this->semester_arr[$i]])) {
                knjh060Query::getInsertQuery($db,$this,$this->semester_arr[$i]);
            }
        }

        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }
}
?>
