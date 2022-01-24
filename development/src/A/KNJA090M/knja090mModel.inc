<?php

require_once('for_php7.php');


class knja090mModel extends Model {
 
    var $cmd;
    var $grade_class;
    var $s_id;
    var $sort;
    var $sorttype;	//NO001
    var $selectdata;
    var $attendno = array();
    var $control = array();
    var $course1; //コース設定で選択されたコース
    var $coursemajorcd1; //課程学科

    function knja090mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
         //新年度、学期の算出
        if (CTRL_SEMESTER == $this->control["学期数"]) { //最終学期のとき
            $this->control["new_year"] = ((int)CTRL_YEAR+1);
            $this->control["new_semes"] = 1;
        } else {
            $this->control["new_year"] = CTRL_YEAR;
            $this->control["new_semes"] = ((int)CTRL_SEMESTER+1);
        }

        if (VARS::get("PROGRAMID") != "") {
            //起動時のデフォルトでソートを（性別＋かな）の昇順にする
            $this->s_id = "1";
            $this->sort["1"] = "1";
            $this->sort["2"] = "0";
            $this->sort["3"] = "0";
            $this->sort["4"] = "0";
            $this->sort["5"] = "0";
            $this->grade_class = "";
        } elseif (VARS::get("sort1") != "" || VARS::get("sort2") != "" || VARS::get("sort3") != "" 
               || VARS::get("sort4") != "" || VARS::get("sort5") != "") {
            $this->sort["1"] = (VARS::get("sort1") != "" ) ? VARS::get("sort1") : "0";
            $this->sort["2"] = (VARS::get("sort2") != "" ) ? VARS::get("sort2") : "0";
            $this->sort["3"] = (VARS::get("sort3") != "" ) ? VARS::get("sort3") : "0";
            $this->sort["4"] = (VARS::get("sort4") != "" ) ? VARS::get("sort4") : "0";
            $this->sort["5"] = (VARS::get("sort5") != "" ) ? VARS::get("sort5") : "0";
        }

        $this->cmd         = VARS::request("cmd");

        $this->year_seme = $this->control["new_year"]."-".$this->control["new_semes"];
        if (VARS::request("year_seme") != "") {
            $this->year_seme = VARS::request("year_seme");

            $tmp = explode("-",$this->year_seme);
            $this->control["new_year"] = $tmp[0];
            $this->control["new_semes"] = $tmp[1];
        }

        if (VARS::request("grade_class")!="") {
            $this->grade_class = VARS::request("grade_class");
        }

        if (VARS::get("s_id") != ""){
            $this->s_id     = VARS::get("s_id");
        }
        if (VARS::get("schregno") !="" ) {
            $this->schregno = VARS::get("schregno");
        }
        
        $this->attendno    = VARS::post("ATTENDNO");
        $this->selectdata  = VARS::post("selectdata");
        $this->course1     = VARS::request("course1");
        $this->coursemajorcd1     = VARS::request("COURSEMAJORCD1");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        //学年クラスコンボチェック
        if (!$this->grade_class) {
            $this->setWarning("MSG304","(対象クラス)");
            return false;
        }
        //重複チェック
        if (get_count(array_unique($this->attendno)) != get_count($this->attendno)) {
            $this->setWarning("MSG302","(出席番号)");
            return false;
        }

        return true;
    }
    
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            if (knja090mQuery::getUpdateQuery($this)) {
                $this->setMessage("MSG201");
            }
        }    
    }
    
    function &GetSubUpdateModel()
    {
        if(knja090mQuery::GetSubUpdateQuery($this)) {   
            $this->setMessage("MSG201");
        }
    }  

}
?>
