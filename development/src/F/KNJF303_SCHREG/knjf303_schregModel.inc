<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf303_schregModel extends Model {
    var $field      = array();
    var $select_data     = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $school_name;    //学校名

    /**
     * メンバ変数の初期化
     */
    function knjf303_schregModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                            //職員コード
    }

    function init()
    {
        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        if (VARS::get("SEND_PRGRID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
            $this->getfield = array();
            unset($this->suspend_direct_date);
            unset($this->total_div);
            unset($this->diseasecd);

            common::GetControlMaster_Fnc($this->control);
            
            $this->getPrgId = VARS::get("SEND_PRGRID");
            $this->sendAuth = VARS::get("SEND_AUTH");

            $this->getfield["EDBOARD_SCHOOLCD"] = VARS::get("SEND_EDBOARD_SCHOOLCD");
            $this->getfield["YEAR"]             = VARS::get("SEND_YEAR");
            //履歴データ
            $this->getfield["DATA_DIV"]         = VARS::get("SEND_DATA_DIV");

            list($this->suspend_direct_date, $this->total_div, $this->diseasecd) = explode(':', $this->getfield["DATA_DIV"]);
        }

        $this->selectdata   = VARS::post("selectdata");
        $this->cmd          = VARS::request("cmd");
        
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput() {

        return true;
    }


    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        if ($this->validateInput()) {
            knjf303_schregQuery::getUpdateQuery($this, $db);
            $this->setMessage("MSG201");
        }
        
        Query::dbCheckIn($db);
    }

}
?>
