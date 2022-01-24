    <?php

require_once('for_php7.php');

class knjz415Model extends Model {
    var $field      = array();
    var $cmd;
    var $l_groupcd;        //primary
    var $selectdata;

    //メンバ変数の初期化
    function knjz415Model() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->l_groupcd);
        }

        $this->field = array("L_GROUPCD"        =>  VARS::post("L_GROUPCD"),
                             "L_GROUPNAME"      =>  VARS::post("L_GROUPNAME")
                             );

        if (VARS::get("L_GROUPCD")) {
            $this->l_groupcd = VARS::get("L_GROUPCD");
        }

        $this->selectdata   = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput() {

        //キーのチェック
        if (trim($this->field["L_GROUPCD"]) == "") {
            $this->setWarning('MSG301',"(大分類グループコード)");
            return false;
        }

        if($this->cmd == "update"){
            //キーのチェック
            if (trim($this->field["L_GROUPNAME"]) == "") {
                $this->setWarning('MSG301',"(大分類グループ名称)");
                return false;
            }

            if (strlen($this->field["L_GROUPCD"]) > 2) {
                $this->setWarning('MSG914', "(大分類グループコード) 2バイトまで");
                return false;
            } elseif (strlen($this->field["L_GROUPNAME"])>90) {
                $this->setWarning('MSG914', "(大分類グループ名称) 90バイトまで");
                return false;
            }
        } else {
            //１レコード取得
            $db = Query::dbCheckOut();
            $query = knjz415Query::getLgroupMst($this->field["L_GROUPCD"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);

            if (!is_array($row)) {
                $this->setWarning('MSG303');
                return;
            }
        }
        return true;
    }

    // 更新用のモデルを返します
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjz415Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
            $this->l_groupcd = $this->field["L_GROUPCD"];
            return;
        }
    }
    // 削除用のモデルを返します
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjz415Query::getDeleteQuery($this);
            $this->setMessage('MSG201');
        }
    }
}
?>