<?php

require_once('for_php7.php');

//定数定義

class knjz350kModel extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;
    var $selectdata2;
    var $kansansaki;

    //メンバ変数の初期化
    function knjz350kModel()
    {
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->selectdata);
            unset($this->selectdata2);
            unset($this->selectdata3);
            unset($this->kansansaki);
        }

        $this->year             = CTRL_YEAR;                        //現在処理年度を初期値にする
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdata2      = VARS::post("selectdata2");
        $this->selectdata3      = VARS::post("selectdata3");
        $this->kansansaki       = VARS::post("KANSANSAKI");
        $this->cmd              = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み

        //学校判定
        $db = Query::dbCheckOut();
        $z010 = $db->getRow(knjz350kQuery::getNameMst("Z010", "00"), DB_FETCHMODE_ASSOC);
        $this->isKumamoto = ($z010["NAME1"] == 'kumamoto') ? true : false;
        Query::dbCheckIn($db);
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
        $result = knjz350kQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
