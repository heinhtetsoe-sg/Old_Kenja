<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd214mModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectdata;

    /**
     * メンバ変数の初期化
     */
    function knjd214mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト
                             "SHORI"            =>  VARS::post("SHORI")             //クリア処理とコピー処理のラジオ
                     );
        $this->selectdata = VARS::post("selectdata");

        $this->cmd         = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
    }

    function validateInput()
    {
        $db = Query::dbCheckOut();
        //NULLではないデータが１件でもあれば、エラーメッセージを表示。
        $query  = knjd214mQuery::getClearExists($this, CTRL_YEAR, CTRL_SEMESTER);
        $dataCnt = $db->getOne($query);
        Query::dbCheckIn($db);
        if (0 < $dataCnt) {
            $this->setWarning("MSG306", "クリア処理が行われていません。");
            return false;
        }

        return true;
    }

    function &getCopyModel()
    {
        if ($this->validateInput()){
            knjd214mQuery::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
    }

    function &getClearModel()
    {
        knjd214mQuery::getClearQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
