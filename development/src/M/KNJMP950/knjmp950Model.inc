<?php

require_once('for_php7.php');

class knjmp950Model extends Model {
    var $field = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knjmp950Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->getYear);
            unset($this->getYosanDiv);
            unset($this->getYosanLcd);
            unset($this->getYosanLMcd);
            unset($this->getRequestNo);
        }
        $this->field = array("YEAR"              =>  VARS::post("YEAR"),             //年度
                             "SET_DIV"           =>  VARS::post("SET_DIV"),          //01:収入、03:支出
                             "YOSAN_L_CD"        =>  VARS::post("YOSAN_L_CD"),       //予算科目
                             "YOSAN_L_M_CD"      =>  VARS::post("YOSAN_L_M_CD"),     //予算項目
                             "REQUEST_DATE"      =>  VARS::post("REQUEST_DATE"),     //予算作成日
                             "REQUEST_GK"        =>  VARS::post("REQUEST_GK"),       //予算額
                             "BOFORE_REQUEST_GK" =>  VARS::post("BOFORE_REQUEST_GK"),//前年度予算額
                             "REQUEST_SAGAKU"    =>  VARS::post("REQUEST_SAGAKU"),   //差額
                             "REQUEST_REASON"    =>  VARS::post("REQUEST_REASON")    //摘要
                            );

        $this->cmd = VARS::request("cmd");
        //初期値設定
        if (!$this->field["YEAR"]) {
            $this->field["YEAR"] = CTRL_YEAR;
        }
        //履歴用GETパラメータ
        if (VARS::get("SEND_YEAR") != "") {
            $this->getYear = VARS::get("SEND_YEAR");
        }
        if (VARS::get("SEND_YOSAN_DIV") != "") {
            $this->getYosanDiv = VARS::get("SEND_YOSAN_DIV");
        }
        if (VARS::get("SEND_YOSAN_L_CD") != "") {
            $this->getYosanLcd = VARS::get("SEND_YOSAN_L_CD");
        }
        if (VARS::get("SEND_YOSAN_L_M_CD") != "") {
            $this->getYosanLMcd = VARS::get("SEND_YOSAN_L_M_CD");
        }
    }

    //入力項目チェック
    function validateInput($db)
    {
        //キーチェック
        if ($this->cmd == "update" || $this->cmd == "delete") {
            if ($this->field["YEAR"] != $this->getYear) {
                $this->setMessage("MSG309");
                return false;
            }
            if ($this->field["YOSAN_L_CD"] != $this->getYosanLcd) {
                $this->setMessage("MSG309");
                return false;
            }
            if ($this->field["YOSAN_L_M_CD"] != $this->getYosanLMcd) {
                $this->setMessage("MSG309");
                return false;
            }
        }
        //重複チェック
        if ($this->cmd == "insert") {
            $getDataCout = $db->getOne(knjmp950Query::getYosanDataCheck($db, $this));
            if ($getDataCout > 0) {
                $this->setMessage("MSG302");
                return false;
            }
        }
        //桁数チェック
        if (strlen($this->field["REQUEST_REASON"]) > 120) {
            $this->setWarning("MSG901", "( 摘要 ) 120バイトまで");
            return false;
        } else if (strlen($this->field["REQUEST_GK"]) > 7) {
            $this->setWarning("MSG901", "( 予算額 ) 7バイトまで");
            return false;
        }
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput($db)) {
            //新規の場合は伝票番号を発行
            if (!$this->getRequestNo) {
                $getMaxRequestNo = $db->getOne(knjmp950Query::getMaxRequestNo($this));
                //対象年度の伝票番号がない場合
                if (!$getMaxRequestNo) {
                    $this->getRequestNo = $this->field["YEAR"].'F00001';
                } else {
                    $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
                    $this->getRequestNo = $this->field["YEAR"].'F'.$setNo;
                }
            }
            knjmp950Query::getUpdateQuery($db, $this);
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }
    
    //削除用のモデルを返します
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput($db)) {
            knjmp950Query::getDeleteQuery($db, $this);
            
            unset($this->getYear);
            unset($this->getYosanDiv);
            unset($this->getYosanLcd);
            unset($this->getYosanLMcd);
            unset($this->getRequestNo);

            $this->setMessage("MSG201");
            
            Query::dbCheckIn($db);
        }
        Query::dbCheckIn($db);

        return;
    }
    
}
?>
