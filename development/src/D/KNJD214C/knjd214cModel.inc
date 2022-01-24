<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd214cModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $testTable;
    var $selectdata;
    var $calcMethod;
    var $del_check = array();

    /**
     * メンバ変数の初期化
     */
    function knjd214cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("COPY_SAKI_DIV"    =>  VARS::post("COPY_SAKI_DIV"),    //コピー先
                             "KARI_DIV"         =>  VARS::post("KARI_DIV"),         //仮評定フラグ
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "SOUGAKU"          =>  VARS::post("SOUGAKU"),          //総学等も含む
                             "CONVERT"          =>  VARS::post("CONVERT"),          //評定換算する
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト
                             "SHORI"            =>  VARS::post("SHORI")             //クリア処理とコピー処理のラジオ
                     );
        $this->selectdata = VARS::post("selectdata");

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG_NEW";
        //学期評価・学年評価を算出
        //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
        $rtnRow = knjd214cQuery::getCalcMethod();
        $this->calcMethod = $rtnRow["SCHOOL_REMARK1"];

        //実行履歴データ削除
        $this->del_check = array();
        $this->del_check = VARS::post("DEL_CHECK");
    }

    function validateInput()
    {
        $db = Query::dbCheckOut();
        //NULLではないデータが１件でもあれば、エラーメッセージを表示。
        $query  = knjd214cQuery::getClearExists($this, CTRL_YEAR, $this->field["SEMESTER"]);
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
            knjd214cQuery::executeRireki($this);
            knjd214cQuery::getCopyQuery($this);
            //処理対象 1:学期末評価 2:学年評定
            if ($this->field["COPY_SAKI_DIV"] == '2') {
                knjd214cQuery::getCombinedCopyQuery($this);
                knjd214cQuery::getProvCopyQuery($this); //履歴
            }
            $this->setMessage("MSG201");
        }
    }

    function &getClearModel()
    {
        knjd214cQuery::executeRireki($this);
        knjd214cQuery::getClearQuery($this);
        $this->setMessage("MSG201");
    }

    function &getDeleteRirekiModel() {
        if (get_count($this->del_check)) {
            knjd214cQuery::deleteRireki($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
