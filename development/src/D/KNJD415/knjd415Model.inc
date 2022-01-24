<?php

require_once('for_php7.php');
class knjd415Model extends Model {
    var $cmd;
    var $ctrl;
    var $year;
    var $condition = "";
    var $groupcd = "";
    var $groupname = "";
    var $field;
    var $selectdata = "";
    var $selectdata2 = "";

    function knjd415Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    function init() {
        //プロパティファイル
        $this->getPropertiesAll();

        if (VARS::get("PROGRAMID") != "") {
            unset($this->year);
            unset($this->condition);
            unset($this->groupcd);
            unset($this->groupname);
            $this->field = array();
        }
        if (VARS::post("YEAR") != "") {
            $this->year = VARS::request("YEAR");
        }
        if (VARS::get("CONDITION") != ""){
            $this->condition = VARS::get("CONDITION");
        }
        if (VARS::get("CLASSGROUP_CD") != "") {
            $this->groupcd = VARS::get("CLASSGROUP_CD");
        }
        if (VARS::get("CLASSGROUP_NAME") != "") {
            $this->groupname = VARS::get("CLASSGROUP_NAME");
        }

        $this->field = array("CONDITION"            =>  VARS::post("CONDITION"),              //状態区分
                             "CLASSGROUP_CD"        =>  (VARS::post("CLASSGROUP_CD") == "") ? "" : VARS::post("CLASSGROUP_CD"),     //グループコード
                             "CLASSGROUP_NAME"      =>  VARS::post("CLASSGROUP_NAME"),        //グループ名
                             "selectdata2"          =>  VARS::post("selectdata2"),            //対象教科一覧
                            );

        $this->selectdata = VARS::post("selectdata");
        $this->getFieldSize();

        $this->cmd = VARS::request("cmd");
    }


    //入力項目チェック
    function validateInput() {
    
        $db = Query::dbCheckOut();
        if($this->cmd != "copy"){
            //必須入力チェック
            if (!$this->field["CLASSGROUP_CD"]) {
                $this->setWarning("MSG301", "グループコード");
                return false;
            }
            if (!$this->field["CLASSGROUP_NAME"]) {
                $this->setWarning("MSG301", "教科グループ名");
                return false;
            }
            //文字数チェック
            if (strlen($this->field["CLASSGROUP_CD"]) > 2 ) {
                $this->setWarning("MSG915", "(グループコード) 2バイトまで");
                return false;
            }
            if (strlen($this->field["CLASSGROUP_NAME"]) > 30 ) {
                $this->setWarning("MSG915", "(教科グループ名) 全角10文字まで");
                return false;
            }
        }
        if ($this->cmd == "update" || $this->cmd == "add") {
            //対象教科チェック
            if(!$this->selectdata){
                $this->selectdata = $this->field["selectdata2"];
                if(!$this->selectdata){
                    $this->setWarning("MSG304", "対象教科一覧");
                    return false;
                }
            }
            if($this->cmd == "add"){
                //存在チェック
                $getData = $db->getOne(knjd415Query::getList($this, $this->field["CONDITION"], $this->field["CLASSGROUP_CD"]));
                if ($getData != 0) {
                    $this->setWarning("既にデータが存在します。");
                    return false;
                }
            }
            if($this->cmd == "update"){
                //更新チェック
                if ($this->groupcd != $this->field["CLASSGROUP_CD"] || $this->condition != $this->field["CONDITION"]) {
                    $getData = $db->getOne(knjd415Query::getList($this, $this->field["CONDITION"], $this->field["CLASSGROUP_CD"]));
                    if ($getData == 0) {
                        $this->setWarning("MSG303");
                        return false;
                    }
                }
            }
        } else if ($this->cmd == "copy") {
            //存在チェック（前年度）
            $cnt_preYM = $db->getOne(knjd415Query::checkCourseOfStudyMst($this, intval($this->year - 1)));
            $cnt_preD  = $db->getOne(knjd415Query::checkCourseOfStudyDat($this, intval($this->year - 1)));
            if ($cnt_preYM == 0 || $cnt_preD == 0) {
                $this->setWarning("MSG917","対象データが存在しません。");
                return false;
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    function getFieldSize() {
        //グループコード文字数
        $this->cd_moji = 2; //デフォルトの値

        //教科グループ名
        $this->name_moji = 10; //デフォルトの値
    }

    //追加処理
    function &getInsertModel() {
        //入力チェック
        if ($this->validateInput()) {
            knjd415Query::getInsertQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjd415Query::UpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput()) {
            $pre_year = $this->year - 1;
            knjd415Query::copyInsertQueryMst($this, $pre_year);
            knjd415Query::copyInsertQueryDat($this, $pre_year);
            $this->setMessage("MSG201");
        }
        return;
    }

    //削除処理
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjd415Query::DeleteQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>
