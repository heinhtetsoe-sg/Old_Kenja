<?php
//定数定義

class knjz071kModel extends Model {
    var $cmd;
    var $expense_m_cd;  //中分類コード
    var $selectdata;

    //メンバ変数の初期化
    function knjz071kModel()
    {
    }
    function init()
    {
        $this->selectdata        = VARS::post("selectdata");
        $this->cmd               = VARS::request("cmd");
        $this->expense_m_cd      = VARS::request("EXPENSE_M_CD");
    }

    //入力項目チェック
    function validateInput()
    {
        if (!$this->expense_m_cd){
            $this->warning = "中分類コードを選択してください。";
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
        $result = knjz071kQuery::getUpdateQuery($this);
        return $result;
    }
}
?>
