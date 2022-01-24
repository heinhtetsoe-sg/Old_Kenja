<?php

require_once('for_php7.php');

class knjh342Model extends Model {

    var $cmd;
    var $field = array();
    var $reset = array();

    function knjh342Model()
    {
        $this->field = array();
    }

    function init()
    {
        $this->cmd               = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        /* 編集対象データを取得 */
        $this->field["GRADE"]            = VARS::post("GRADE");             //学年
        $this->field["DATA_DIV"]         = VARS::post("DATA_DIV");          //データ種別
        $this->field["MOCK_TARGET"]      = VARS::post("MOCK_TARGET");       //テスト
        $this->field["subclassCd"]       = VARS::post("subclassCd");        //科目コード ':'区切り

        $subclassCds = explode(":", $this->field["subclassCd"]);
        $this->setData = array();
        if (is_array($subclassCds)) {
            foreach ($subclassCds as $key => $subclassCd) {
                $this->setData[$subclassCd] = array("AVG" => VARS::post("AVG-".$subclassCd));
            }
        }
    }

    /* 入力項目チェック */
    function validateInput()
    {
        return true;
    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            if($this->update()){
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    /* 編集対象データを更新 */
    function &update()
    {
        $result = knjh342Query::getUpdateQuery($this);
        return $result;
    }

}
?>
