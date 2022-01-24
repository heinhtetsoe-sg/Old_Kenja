<?php

require_once('for_php7.php');

//定数定義

class knjh440Model extends Model {
    var $field = array();
    var $cmd;
    var $mockyear;  //年度
    var $selectdataMock;
    var $selectdataSub;

    //メンバ変数の初期化
    function knjh440Model()
    {
    }
    function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
        }
        $this->field["GRADE"] = VARS::post("GRADE");

        $this->mockyear         = CTRL_YEAR;
        $this->selectdataMock   = VARS::post("selectdataMock");
        $this->selectdataSub    = VARS::post("selectdataSub");
        $this->cmd              = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
    }
    //入力項目チェック
    function validateInput() {
        if (!strlen($this->mockyear)){
            $this->warning = "対象年度を選択してください。";
            return false;
        }
        if (!strlen($this->field["GRADE"])){
            $this->warning = "対象学年を選択してください。";
            return false;
        }
        return true;
    }
    //編集完了用のモデルを返します
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->setMessage("MSG201");
        }
    }

    //アップデート時の内部メソッド
    function &update() {
        if ($this->cmd == 'update') {
            $result = knjh440Query::getUpdateMock($this);
        } else if ($this->cmd == 'update2') {
            $result = knjh440Query::getUpdateSub($this);
        }
        return $result;
    }

    /**
     * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel() {
        if (!$this->copyCheck()) {
            return;
        }
        $result = knjh440Query::getCopyQuery($this);
        $this->setMessage("MSG201");
    }
    //コピーチェック
    function copyCheck() {
        //DB接続
        $db = Query::dbCheckOut();

        if (0 < $db->getOne(knjh440Query::getCopyCheck($this))) {
            $this->setWarning("対象年度のデータが既に存在しています。");
            return false;
        }
        if (0 < $db->getOne(knjh440Query::getCopyCheck2($this))) {
            $this->setWarning("対象年度のデータが既に存在しています。");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }
}
?>
