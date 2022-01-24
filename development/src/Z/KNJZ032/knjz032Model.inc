<?php

require_once('for_php7.php');

class knjz032Model extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $field = array();
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjz032Model()
    {
    }

    function init() {
        //年度・学期
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->year);
            unset($this->grade);
        }
        if(VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
            $this->year     = VARS::get("SEND_YEAR");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;
        $this->programid = $this->getPrgId ? $this->getPrgId : PROGRAMID;

        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        for ($i = 1; $i <= $this->semescount; $i++) {
            $this->SEMESTER["$i"]             =  VARS::post("SEMESTER".$i);
            $this->SEMESTERNAME["$i"]         =  VARS::post("SEMESTERNAME".$i);
            $this->SDATE["$i"]                =  VARS::post("SDATE".$i);
            $this->EDATE["$i"]                =  VARS::post("EDATE".$i);
        }
        
        //学年パラメータセット用
        if (VARS::request("GRADE") != "") {
            $this->grade = VARS::request("GRADE");
        }
        //履歴用GETパラメータ
        if (VARS::get("UPD_FLG") != "") {
            $this->getUpdFlg = VARS::get("UPD_FLG");
        }
    }
    //入力項目チェック
    function validateInput() {
        //学年チェック
        if ($this->grade == "") {
            $this->setWarning("MSG301","学年");
            return false;
        }
        //年度チェック
        if ($this->year == "") {
            $this->setWarning("MSG301","年度");
            return false;
        }
        for ($i = 1; $i <= $this->semescount; $i++) {
            //学期名称チェック
            if (!($this->SEMESTERNAME["$i"])) {
                $this->setWarning("MSG903","項目：学期名称");
                return false;
            }
            //学期名称文字数チェック
            if (strlen($this->SEMESTERNAME["$i"]) > 15) {
                $this->setWarning("MSG915","学期名称 15バイトまで");
                return false;
            }
            
            //日付チェック
            if ($this->SEMESTER["$i"] != 9) {      
                $j = $i + 1;
                if ($this->SDATE["$i"] >= $this->EDATE["$i"]) {
                    $this->setWarning("MSG916",$this->SEMESTER["$i"].
                                        "学期の開始日付と終了日付が正しくありません。");
                    return false;
                
                } else if ($this->SEMESTER["$j"]!="9" && ($this->SDATE["$j"] <= $this->EDATE["$i"])) {
                    $this->setWarning("MSG916",$this->SEMESTER["$i"]."学期と".
                                                $this->SEMESTER["$j"]."学期の日付の前後関係が正しくありません。");
                    return false;
                } 
                
                //日付が対象年度内かチェック
                if (strtotime($this->SDATE["$i"]) < strtotime($this->year."/04/01")
                    || strtotime($this->EDATE["$i"]) > strtotime(($this->year+1)."/03/31")) {
                    $this->setWarning("MSG916","学期コード ".$this->SEMESTER["$i"]." が対象年度内の日付ではありません。");
                    return false;
                }
            }
        }
        return true;
    }

    //更新
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput())
        {

        $maxsemester = $this->semescount - 1;   //max学期

            for($i=1; $i <= $this->semescount; $i++) {
                $this->semester = $this->SEMESTER["$i"];
                $this->semestername = $this->SEMESTERNAME["$i"];

                if($this->semester=="9")
                {
                    $this->sdate = $this->SDATE["1"];
                    $this->edate = $this->EDATE[$maxsemester];
                } else {
                    $this->sdate = $this->SDATE["$i"];
                    $this->edate = $this->EDATE["$i"];
                }
                knjz032Query::getUpdateQuery($this);
            }
            $this->setMessage("MSG201");   
        }
    }
}
?>