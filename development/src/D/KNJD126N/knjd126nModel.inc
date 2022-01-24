<?php

require_once('for_php7.php');

class knjd126nModel extends Model {

    var $cmd;
    var $control_data = array();    //コントロールのデータを保持
    var $field = array();
    var $auth;                      //権限
    var $setupFlg;                  //編集フラグ 1:編集中

    function init() {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")) {
            common::GetControlMaster_Fnc($this->control_data);
            $this->field = array();
            unset($this->auth);
            unset($this->setupFlg);
            unset($this->unitarry);
            unset($this->unitvals);
        }

        $this->cmd = VARS::request("cmd");
        $this->auth = AUTHORITY;
        $this->setupFlg = VARS::post("setupFlg");

        $this->field["GRADE_HR_CLASS"]  = VARS::post("GRADE_HR_CLASS");
        $this->field["SUBCLASSCD"]      = VARS::post("SUBCLASSCD");
        $this->field["SEMESTER"]        = VARS::post("SEMESTER");
        
        if ($this->cmd == "calc" || $this->cmd == "update") {
            $this->unitarry = array();
            $this->unitarry = explode(",", VARS::post("HID_UNITINFO"));
            foreach($this->unitarry as $val) {
                if ($val == "") {
                    continue;
                }
                $valwk = array();
                $valwk = explode("_", $val);
                $this->unitvals[$valwk[2]][$valwk[0]][$valwk[1]] = VARS::post("OMOMI".$val);
            }
        } else {
            if ($this->cmd != "changeSemester") {
                $this->unitarry = array();
            }
        }
        $db = Query::dbCheckOut();

        $query = knjd126nQuery::getD086();
        $this->d086 = $db->getOne($query);

        $db->commit();
        Query::dbCheckIn($db);
    }

    /* 入力項目チェック */
    function validateInput() {
        $retflg = true;
        foreach($this->unitarry as $val) {
            if ($val == "") {
                continue;
            }
            $valwk = array();
            $valwk = explode("_", $val);
            //空文字はチェック対象外
            $chkStr = $this->unitvals[$valwk[2]][$valwk[0]][$valwk[1]];
            if ($chkStr !== "" && $chkStr !== null) {
                //整数の数字か?
                if (!preg_match('/^[0-9]+$/', $chkStr) || ($chkStr > 0 && 100 < $chkStr)) {
                    $this->setMessage("MSG907");
                    $retflg = false;
                }
            };
        }
        return $retflg;
    }

    /* 更新処理 */
    function &getUpdateModel() {
        if ($this->validateInput()){
            $this->getomomiUpdate();
            if ($this->cmd == "update") {
                $this->update();
                $this->setMessage("MSG201");
                $this->setupFlg = ""; //編集中フラグOFF
            }
        } else {
            $this->setupFlg = ""; //編集中フラグOFF
        }
        return;
    }

    function &getomomiUpdate() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjd126nQuery::getUpdateWeightQuery($db, $this);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    /* 編集対象データを更新 */
    function &update() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        knjd126nQuery::getUpdateQuery($db, $this);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
