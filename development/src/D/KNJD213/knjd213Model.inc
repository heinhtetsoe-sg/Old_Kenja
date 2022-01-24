<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd213Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $testTable;
    var $selectdata;

    /**
     * メンバ変数の初期化
     */
    function knjd213Model()
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
                             "SUBCLASS_REMARK0" =>  VARS::post("SUBCLASS_REMARK0"), //対象科目(通年)
                             "SUBCLASS_REMARK1" =>  VARS::post("SUBCLASS_REMARK1"), //対象科目(1学期)
                             "SUBCLASS_REMARK2" =>  VARS::post("SUBCLASS_REMARK2"), //対象科目(2学期)
                             "SUBCLASS_REMARK3" =>  VARS::post("SUBCLASS_REMARK3"), //対象科目(3学期)
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
        $db = Query::dbCheckOut();
        //NULLではないデータが１件でもあれば、エラーメッセージを表示。
        $query  = knjd213Query::getClearExists($this, CTRL_YEAR, CTRL_SEMESTER);
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
            knjd213Query::getCopyQuery($this);
            knjd213Query::getCombinedCopyQuery($this);
            $this->setMessage("MSG201");
        }
    }

    function &getClearModel()
    {
        knjd213Query::getClearQuery($this);
        $this->setMessage("MSG201");
    }
}
?>
