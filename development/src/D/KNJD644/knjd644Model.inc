<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knjd644Model extends Model {
    var $cmd;
    var $selectdata;
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjd644Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }
    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),         //年度
                            "SEMESTER"          =>  VARS::post("SEMESTER"),     //学期
                            "BU_CD"             =>  VARS::post("BU_CD"),        //学部コード
                            "KA_CD"             =>  VARS::post("KA_CD"),        //学科コード
                            "WISH_RANK"         =>  VARS::post("WISH_RANK"),    //希望順位
                            "DBNAME"            =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");

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
            $this->update2();
        } 
    }
         
    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {

        knjd644Query::getUpdateQuery($this);
        return $result;

    }
    function &update2()
    {

        knjd644Query::getUpdateQuery2($this);
        if ( !isset($model->warning)){
            $this->setWarning('MSG201');
        }
        return $result;


    }
}
?>
