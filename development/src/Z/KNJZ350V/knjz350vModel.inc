<?php

require_once('for_php7.php');

//定数定義

class knjz350vModel extends Model {
    var $field = array();
    var $cmd;
    var $year;  //年度
    var $selectdata;
    var $selectdata2Left;
    var $selectdata2Right;
    var $schoolKindArray = array();
    var $selectdata2;
    var $selectdata3;
    var $selectdataJview;

    //メンバ変数の初期化
    function knjz350vModel()
    {
    }
    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
        }
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

        $this->year             = CTRL_YEAR;                        //現在処理年度を初期値にする
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdata2Left  = VARS::post("selectdata2Left");
        $this->selectdata2Right = VARS::post("selectdata2Right");
        $this->selectdata2      = VARS::post("selectdata2");
        $this->selectdata3      = VARS::post("selectdata3");
        $this->selectdataJview  = VARS::post("selectdataJview");
        $this->selectdataMypDp  = VARS::post("selectdataMypDp");
        $this->cmd              = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::post("SCHOOL_KIND")) {
            $this->setSchoolKind = VARS::post("SCHOOL_KIND");
        }

    }
    //入力項目チェック
    function validateInput()
    {
        if (!is_numeric($this->year)){
            $this->warning = "対象年度を選択してください。";
            return false;
        }
        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        //パーツ
        if ($this->cmd == 'update') {
            $result = knjz350vQuery::getUpdateQuery($this);
        //管理者コントロール
        } else if ($this->cmd == 'update2') {
            $result = knjz350vQuery::getUpdateControlQuery($this);
        //出欠・実力テスト・観点
        } else if ($this->cmd == 'update3' || $this->cmd == 'update4' || $this->cmd == 'updateJview' || $this->cmd == 'updateMypDp') {
            $result = knjz350vQuery::getUpdateAttendProficiencyQuery($this);
        }
        return $result;
    }

    /**
     * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel() {
        $result = knjz350vQuery::getCopyQuery($this);
    }
}
?>
