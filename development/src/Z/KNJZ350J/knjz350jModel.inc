<?php

require_once('for_php7.php');

//定数定義

class knjz350jModel extends Model {
    var $cmd;
    var $year;  //年度
    var $selectdata;
    var $selectdata2;
    var $selectdata3;

    //メンバ変数の初期化
    function knjz350jModel()
    {
    }
    function init()
    {
        $this->year             = CTRL_YEAR;                        //現在処理年度を初期値にする
        $this->selectdata       = VARS::post("selectdata");
        $this->selectdata2      = VARS::post("selectdata2");
        $this->selectdata3      = VARS::post("selectdata3");
        $this->cmd              = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイルの読込み
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
        $result = knjz350jQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
