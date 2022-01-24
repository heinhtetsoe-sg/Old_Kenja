<?php

require_once('for_php7.php');

class knjb0110Model extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjb0110Model() {}

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->fields = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field = array("YEAR"             =>  VARS::request("YEAR"),          //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "JOBCD"            =>  VARS::post("JOBCD"),            //職名
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        $this->cmd     = VARS::request("cmd");
        $this->counter = VARS::get("Counter");
        if ($this->cmd === 'main') {
            $this->fields = array();
        }
        //プロパティファイルの読込み
        $this->getPropertiesAll();

    }

    //入力項目チェック
    function validateInput() {
        
        $db = Query::dbCheckOut();

        //必須入力チェック
        if ($this->field["YEAR"] == "") {
            $this->setWarning("MSG304", "( 年度 )");
            return false;
        }
        if ($this->field["SEMESTER"] == "") {
            $this->setWarning("MSG304", "( 学期 )");
            return false;
        }
        if ($this->field["JOBCD"] == "") {
            $this->setWarning("MSG304", "( 職名 )");
            return false;
        }

        if (get_count($this->data["STAFFCD"]) == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //桁数チェック
        //STAFFCDフィールドサイズ変更対応
        if ($this->Properties["useStaffcdFieldSize"] === '10') {
            $setSize = "10";
        } else {
            $setSize = "8";
        }
        for ($i = 0; $i < get_count($this->data["STAFFCD"]); $i++) {
            if (strlen($this->fields["UP_STAFFCD"][$i]) > $setSize) {
                $this->setWarning("MSG915","(職員コードは".$setSize."桁までです)");
                return false;
            }
            //対象の職員コードが存在するかをチェック
            if ($this->fields["UP_STAFFCD"][$i]) {
                $staffcdCnt = $db->getOne(knjb0110Query::checkStaffcd($this, $this->fields["UP_STAFFCD"][$i]));
                if ($staffcdCnt == 0) {
                    $this->setWarning("MSG203","(指定年度に入力した職員コードは登録されていません。(".$this->fields["UP_STAFFCD"][$i]."))");
                    return false;
                }
            }
        }
        Query::dbCheckIn($db);
        
        return true;
    }

    function &getUpdateModel() {
        //初期化
        $this->fields = array();
        for ($i = 0; $i < get_count($this->data["STAFFCD"]); $i++) {
            $term_name = "UP_STAFFCD".$i;
            $this->fields["UP_STAFFCD"][] = VARS::post($term_name);
        }

        if ($this->validateInput()) {
            $notUpchaircd = knjb0110Query::update($this);
            if ($notUpchaircd == "") {
                $this->setMessage("MSG201");
                $this->fields = array();
            } else {
                $this->setMessage("MSG201", "更新先の職員が既に同一講座を持っていたため、変更できなかったデータがあります。(講座コード:".$notUpchaircd.")");
            }
        }
    }
}
//var_dump($_REQUEST);
?>
