<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm434wModel extends Model {
    var $field      = array();
    var $cmd;
    var $selectdata;

    /**
     * メンバ変数の初期化
     */
    function knjm434wModel() {
    }

    function init() {
        $this->field = array("SOTUGYO_YOTEI"    =>  VARS::post("SOTUGYO_YOTEI"),    //卒業予定のみ
                             "OUTPUT_DIV"       =>  VARS::post("OUTPUT_DIV"),       //対象
                             "SUBCLASS_FLG"     =>  VARS::post("SUBCLASS_FLG"),     //対象科目
                             "CLEAR1"           =>  VARS::post("CLEAR1"),           //クリア（評定）
                             "CLEAR2"           =>  VARS::post("CLEAR2"),           //クリア（履修単位数）
                             "CLEAR3"           =>  VARS::post("CLEAR3"),           //クリア（修得単位数）
                             "COPY1"            =>  VARS::post("COPY1"),            //コピー（評定）
                             "COPY2"            =>  VARS::post("COPY2"),            //コピー（履修単位数）
                             "COPY3"            =>  VARS::post("COPY3"),            //コピー（修得単位数）
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
        $query  = knjm434wQuery::getClearSql($this, CTRL_YEAR, CTRL_SEMESTER);
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
            knjm434wQuery::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
    }

    function &getClearModel() {
        knjm434wQuery::getClearQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
