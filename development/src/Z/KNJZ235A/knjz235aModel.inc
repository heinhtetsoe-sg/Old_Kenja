<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz235aModel extends Model {
    var $field = array();
    var $cmd;
    var $control;   //コントロールマスタ

    /**
     * メンバ変数の初期化
     */
    function knjz235aModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("SEMESTER"             =>  VARS::post("SEMESTER"),     //学期
                             "GRADE"                =>  VARS::post("GRADE"),        //学年
                             "DBNAME"               =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->selectdata = VARS::post("selectdata");
        $this->selectdataLabel = VARS::post("selectdataLabel");
        $this->cmd        = VARS::request("cmd");

    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        $result = knjz235aQuery::getUpdateQuery($this);
        if ( !isset($model->warning)){
            $this->setWarning('MSG201');
            unset($this->selectdata);
            unset($this->selectdataLabel);
        }
        return $result;
    }

    //前年度からコピーのモデルを返します
    function &getCopyModel(){
        $cntselected = knjz235aQuery::cntSelectTest();
        $cntlastorder = knjz235aQuery::cntLastOrder();
        if($cntselected > 0){
            $this->setWarning('MSG203',"データが存在します。");
            return false;
        } else if ($cntlastorder == 0){
            $this->setWarning('MSG303');
            return false;
        } else {
            $result = knjz235aQuery::getCopyQuery();
            if ( !isset($model->warning)){
                $this->setWarning('MSG201',"（前年度からコピー）");
            }
            return $result;
        }
    }

}
?>
