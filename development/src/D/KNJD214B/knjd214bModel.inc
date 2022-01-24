<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd214bModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $testTable;
    var $selectdata;

    /**
     * メンバ変数の初期化
     */
    function knjd214bModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト
                             "SHORI"            =>  VARS::post("SHORI")             //クリア処理とコピー処理のラジオ
                     );
        $this->selectdata = VARS::post("selectdata");

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG_NEW";
    }

    function validateInput()
    {
        //変数
        $year   = CTRL_YEAR;
        $sem    = $this->field["SEMESTER"];
        $kind   = "99";
        $item   = "00";
        $div    = "00";
        //ＤＢ接続
        $db = Query::dbCheckOut();
        $query  = knjd214bQuery::getClearSql($this, $year, $sem, $kind, $item, $div, "count");
        $dataCnt = $db->getOne($query);
        Query::dbCheckIn($db);
        //NULLではないデータが１件でもあれば、エラーメッセージを表示。
        if (0 < $dataCnt) {
            $this->setWarning("MSG306", "クリア処理が行われていません。");
            return false;
        }

        return true;
    }

    function &getCopyModel()
    {
        if ($this->validateInput()){
            knjd214bQuery::getCopyQuery($this);
            $this->setMessage("MSG201");
        }
    }

    function &getClearModel()
    {
        knjd214bQuery::getClearQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
