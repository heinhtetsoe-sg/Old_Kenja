<?php

require_once('for_php7.php');


class knja063s1Model extends Model {
 
    var $cmd;
    var $year;
    var $semester;
    var $ghr_cd;
    var $s_id;
    var $sort;
    var $sorttype;
    var $attendno = array();
    var $control = array();

    function knja063s1Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("SEND_PRGID") != "") {
            //KNJA063から起動のデフォルト
            $this->prgid = VARS::get("SEND_PRGID");
            $this->auth = VARS::get("SEND_AUTH");
            $this->year = VARS::get("SEND_YEAR");
            $this->semester = VARS::get("SEND_SEMESTER");
            $this->ghr_cd = VARS::get("SEND_GHR_CD");
            //起動時のデフォルトでソートを（複式出席番号順）の昇順にする
            $this->s_id = "3";
            $this->sort["1"] = "0";
            $this->sort["2"] = "0";
            $this->sort["3"] = "1";
            $this->sort["4"] = "0";
        } elseif (VARS::get("sort1") != "" || VARS::get("sort2") != "" || VARS::get("sort3") != "" || VARS::get("sort4") != "") {
            $this->sort["1"] = (VARS::get("sort1") != "") ? VARS::get("sort1") : "0";
            $this->sort["2"] = (VARS::get("sort2") != "") ? VARS::get("sort2") : "0";
            $this->sort["3"] = (VARS::get("sort3") != "") ? VARS::get("sort3") : "0";
            $this->sort["4"] = (VARS::get("sort4") != "") ? VARS::get("sort4") : "0";
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("s_id") != "") {
            $this->s_id = VARS::get("s_id");
        }

        $this->attendno    = VARS::post("GHR_ATTENDNO");
    }

    //入力項目チェック
    function validateInput()
    {
        //クラスコンボチェック
        if (!$this->ghr_cd) {
            $this->setWarning("MSG304","(対象複式クラス)");
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
            if (knja063s1Query::getUpdateQuery($this)) {
                $this->setMessage("MSG201");
            }
        }
    }

}
?>
