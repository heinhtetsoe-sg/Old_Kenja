<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl031cModel extends Model {
    var $cmd;
    var $selectdata;
    var $ObjYear;
    var $staffcd;        //職員コード
    /**
     * メンバ変数の初期化
     */
    function knjl031cModel()
    {
    }

    function init()
    {       
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                                //職員コード
        $this->ObjYear = CTRL_YEAR+1;

        $this->field = array("YEAR"    => VARS::post("YEAR"),    //入試年度
                             "APDIV"   => VARS::post("APDIV"),   //入試制度
                             "TESTDIV" => VARS::post("TESTDIV"), //入試区分
                             "DBNAME"  => VARS::post("DBNAME")   //DB変更に備えて
                            );

        $this->selectdata = VARS::post("selectdata");
        $this->cmd        = VARS::request("cmd");

    }
    //入力項目チェック
    function validateInput()
    {
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $this->update();
        } 
    }
         
    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        $result = knjl031cQuery::getUpdateQuery($this);
        if ( !isset($model->warning)){
            $this->setWarning('MSG201');
        }
        return $result;
    }
}
?>
