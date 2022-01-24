<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd214uModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $testTable;
    var $selectdata;
    var $selectdata2;
    var $selectdataKamoku;
    var $calcMethod;
    var $del_check = array();

    /**
     * メンバ変数の初期化
     */
    function knjd214uModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("COPY_SAKI_CD"     =>  "9990009",                      //コピー先(学年評定)
                             "KARI_DIV"         =>  VARS::post("KARI_DIV"),         //仮評定フラグ
                             "KARI_TESTCD"      =>  VARS::post("KARI_TESTCD"),      //保存先(仮評定)
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "CHOICE"           =>  VARS::post("CHOICE"),           //処理対象
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //クラス
                             "SOUGAKU"          =>  VARS::post("SOUGAKU"),          //総学等も含む
                             "CONVERT"          =>  VARS::post("CONVERT"),          //評定換算する
                             "KEEKA_OVER"       =>  VARS::post("KEEKA_OVER"),       //欠課時数オーバーは未履修にする
                             "SIDOU"            =>  VARS::post("SIDOU"),            //追指導は参照する
                             "TESTKINDCD"       =>  VARS::post("TESTKINDCD"),       //テスト
                             "SHORI"            =>  VARS::post("SHORI")             //クリア処理とコピー処理のラジオ
                     );
        $this->selectdata = VARS::post("selectdata");//20010003-003,20010004-004
        $this->selectdata2 = VARS::post("selectdata2");//20010003,20010004
        $this->selectdataKamoku = VARS::post("selectdataKamoku");

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->testTable = (strlen($this->Properties["useTestCountflg"])) ? $this->Properties["useTestCountflg"] : "TESTITEM_MST_COUNTFLG_NEW";
        //学期評価・学年評価を算出
        //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
        $rtnRow = knjd214uQuery::getCalcMethod();
        $this->calcMethod = $rtnRow["SCHOOL_REMARK1"];

        //実行履歴データ削除
        $this->del_check = array();
        $this->del_check = VARS::post("DEL_CHECK");
    }

    function validateInput()
    {
        $db = Query::dbCheckOut();
        //NULLではないデータが１件でもあれば、エラーメッセージを表示。
        $query  = knjd214uQuery::getClearExists($this, CTRL_YEAR);
        $dataCnt = $db->getOne($query);
        Query::dbCheckIn($db);
        if (0 < $dataCnt) {
            $this->setWarning("MSG306", "クリア処理が行われていません。");
            return false;
        }
        if ($this->field["KARI_TESTCD"] == $this->field["TESTKINDCD"]) {
            $this->setWarning("MSG300", "(コピー元になる評価)と(保存先になる仮評定)が同じです。");
            return false;
        }

        return true;
    }

    function &getCopyModel()
    {
        if ($this->validateInput()){
            knjd214uQuery::executeRireki($this);
            knjd214uQuery::getCopyQuery($this);
            knjd214uQuery::getCombinedCopyQuery($this);
            //保存先(仮評定)
            if (strlen($this->field["KARI_TESTCD"])) {
                knjd214uQuery::getProvCopyQuery($this);
            }
            $this->setMessage("MSG201");
        }
    }

    function &getClearModel()
    {
        knjd214uQuery::executeRireki($this);
        knjd214uQuery::getClearQuery($this);
        $this->setMessage("MSG201");
    }

    function &getDeleteRirekiModel() {
        if (get_count($this->del_check)) {
            knjd214uQuery::deleteRireki($this);
            $this->setMessage("MSG201");
        }
    }
}
?>
