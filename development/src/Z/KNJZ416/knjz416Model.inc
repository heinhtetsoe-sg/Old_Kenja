    <?php

require_once('for_php7.php');

class knjz416Model extends Model {
    var $field      = array();
    var $cmd;
    var $m_groupcd;        //primary
    var $selectdata;

    //メンバ変数の初期化
    function knjz416Model() {}

    function init() {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->m_groupcd);
        }

        $this->field = array("M_GROUPCD"        =>  VARS::post("M_GROUPCD"),
                             "M_GROUPNAME"      =>  VARS::post("M_GROUPNAME"),
                             "INDUSTRY_LCD"     =>  VARS::post("INDUSTRY_LCD")
                             );

        if (VARS::get("M_GROUPCD")) {
            $this->m_groupcd = VARS::get("M_GROUPCD");
        }

        $this->selectdata   = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");
    }

    //入力項目チェック
    function validateInput() {

        //キーのチェック
        if (trim($this->field["M_GROUPCD"]) == "") {
            $this->setWarning('MSG301',"(中分類グループコード)");
            return false;
        }

        if ($this->cmd == "update"){
            //キーのチェック
            if (trim($this->field["M_GROUPNAME"]) == "") {
                $this->setWarning('MSG301',"(中分類グループ名称)");
                return false;
            }
            if (trim($this->field["INDUSTRY_LCD"]) == "") {
                $this->setWarning('MSG301',"(大分類コード)");
                return false;
            }

            if (strlen($this->field["M_GROUPCD"]) > 2 ) {
                $this->setWarning('MSG914', "(中分類グループコード) 2バイトまで");
                return false;
            } elseif (strlen($this->field["M_GROUPNAME"])>90) {
                $this->setWarning('MSG914', "(中分類グループ名称) 90バイトまで");
                return false;
            }
        } else {
            //１レコード取得
            $db = Query::dbCheckOut();
            $query = knjz416Query::getMgroupMst($this->field["M_GROUPCD"]);
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
            knjz416Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
            $this->m_groupcd = $this->field["M_GROUPCD"];
            return;
        }
    }
    // 削除用のモデルを返します
    function &getDeleteModel() {
        if ($this->validateInput()) {
            knjz416Query::getDeleteQuery($this);
            $this->setMessage('MSG201');
        }
    }
}
?>