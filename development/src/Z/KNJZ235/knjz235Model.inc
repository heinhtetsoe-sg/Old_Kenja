<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz235Model extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjz235Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),         //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),     //学期コード
                             "GRADE"                =>  VARS::post("GRADE"),        //学年
                             "DBNAME"               =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->selectdata = VARS::post("selectdata");
        $this->cmd        = VARS::request("cmd");

    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        $result = knjz235Query::getUpdateQuery($this);
        if ( !isset($model->warning)){
            $this->setWarning('MSG201');
        }
        return $result;
    }

    //前年度からコピーのモデルを返します
    function &getCopyModel(){
        $cntselected = knjz235Query::cntSelectTest();
        $cntlastorder = knjz235Query::cntLastOrder();
        if($cntselected > 0){
            $this->setWarning('MSG203',"データが存在します。");
            return false;
        } else if ($cntlastorder == 0){
            $this->setWarning('MSG303');
            return false;
        } else {
            $result = knjz235Query::getCopyQuery();
            if ( !isset($model->warning)){
                $this->setWarning('MSG201',"（前年度からコピー）");
            }
            return $result;
        }
    }

}
?>
