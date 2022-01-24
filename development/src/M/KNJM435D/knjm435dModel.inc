<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm435dModel extends Model {
    var $field      = array();
    var $cmd;
    var $selectdata;

    /**
     * メンバ変数の初期化
     */
    function knjm435dModel() {
    }

    function init() {
        $this->field = array("PROV_FLG"         =>  VARS::post("PROV_FLG"),         //仮評定フラグ
                             "SELSUB"           =>  VARS::post("SELSUB"),           //対象科目
                             "SEMECMB"          =>  VARS::post("SEMECMB"),          //指定学期（春夏秋冬）
                             "SHORI"            =>  VARS::post("SHORI")             //クリア処理とコピー処理のラジオ
                     );

        $this->selectdata = VARS::post("selectdata");

        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }

    function validateInput() {
        $db = Query::dbCheckOut();
        //NULLではないデータが１件でもあれば、エラーメッセージを表示。
        $query  = knjm435dQuery::getClearSql($this, CTRL_YEAR, CTRL_SEMESTER);
        $dataCnt = get_count($db->getCol($query));
        Query::dbCheckIn($db);
        if (0 < $dataCnt) {
            $this->setWarning("MSG306", "クリア処理が行われていません。");
            return false;
        }
        return true;
    }

    function &getCopyModel() {
        if ($this->validateInput()) {
            knjm435dQuery::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
    }

    function &getClearModel() {
        knjm435dQuery::getClearQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
