<?php

require_once('for_php7.php');

//定数定義

class knjm540Model extends Model {
    var $cmd;
    var $sub;  //科目
    var $selectdata;
    var $selectdata2;

    //メンバ変数の初期化
    function knjm540Model()
    {
    }
    function init()
    {
        $this->selectdata = VARS::post("selectdata");
        $this->sub = VARS::post("SELSUB");
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput()
    {
        $db = Query::dbCheckOut();
        $queryc = knjm540Query::getChairCheckQuery($this);
        $rowc = $db->getRow($queryc, DB_FETCHMODE_ASSOC);
        if (!is_array($rowc)){
            $this->setWarning("年度の講座が作成されていないので、年度追加処理はできません。");
            return false;
        }
        Query::dbCheckIn($db);

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
        $result = knjm540Query::getUpdateQuery($this);
        return $result;
    }

    //COPYのモデルを返します
    function &getCopyModel()
    {
        $this->copy();
        $this->setMessage("MSG201");
    }

    //アップデート時の内部メソッド
    function &copy()
    {
        $result = knjm540Query::getCopyQuery($this);
        return $result;
    }

}
?>
