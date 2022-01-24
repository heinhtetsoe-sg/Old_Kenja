<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd214aModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
    public $testTable;
    public $selectdata;
    public $schoolName;
    public $del_check = array();
    public $motoTestcdSem12Avg;

    /**
     * メンバ変数の初期化
     */
    public function knjd214aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    public function init()
    {
        $this->field = array("KARI_DIV"         =>  VARS::post("KARI_DIV"),         //仮評定フラグ
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
        //学校名取得
        $db = Query::dbCheckOut();
        $query = knjd214aQuery::getSchoolName();
        $this->schoolName = $db->getOne($query);
        Query::dbCheckIn($db);

        //実行履歴データ削除
        $this->del_check = array();
        $this->del_check = VARS::post("DEL_CHECK");
        //熊本の時、コピー元になる評価に「１・２学期の平均(19900,29900の平均)」を追加
        $this->motoTestcdSem12Avg = "29999";
    }

    public function validateInput()
    {
        //コピー元になる評価が「9-9900（SCORE）」を選択した時は、ここでは処理しない。
        //プロパティuseProvFlg=1の場合は、ここでは処理しない。（熊本改修）
        if ($this->field["TESTKINDCD"] == "99900" || $this->Properties["useProvFlg"] == '1') {
            return true;
        }

        $db = Query::dbCheckOut();
        //NULLではないデータが１件でもあれば、エラーメッセージを表示。
        $query  = knjd214aQuery::getClearExists($this, CTRL_YEAR, CTRL_SEMESTER);
        $dataCnt = $db->getOne($query);
        Query::dbCheckIn($db);
        if (0 < $dataCnt) {
            $this->setWarning("MSG306", "クリア処理が行われていません。");
            return false;
        }

        return true;
    }

    public function &getCopyModel()
    {
        if ($this->validateInput()) {
            knjd214aQuery::executeRireki($this);
            knjd214aQuery::getCopyQuery($this);
            knjd214aQuery::getCombinedCopyQuery($this);
            knjd214aQuery::getProvCopyQuery($this); //履歴
            $this->setMessage("MSG201");
        }
    }

    public function &getClearModel()
    {
        knjd214aQuery::executeRireki($this);
        knjd214aQuery::getClearQuery($this);
        $this->setMessage("MSG201");
    }

    public function &getDeleteRirekiModel()
    {
        if (get_count($this->del_check)) {
            knjd214aQuery::deleteRireki($this);
            $this->setMessage("MSG201");
        }
    }
}
