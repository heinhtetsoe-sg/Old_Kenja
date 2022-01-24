<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjmp801Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $select_opt = array();  //セレクトデータ
    var $selectleft;
    var $selectleftval;

    /**
     * メンバ変数の初期化
     */
    function knjmp801Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "COLLECT_GRP_CD"   =>  VARS::post("COLLECT_GRP_CD"),   //会計グループCD
                             "OUTPUTDATE"       =>  VARS::post("OUTPUTDATE"),       //納入期限
                             "OUTPUT_NO"        =>  VARS::post("OUTPUT_NO")         //帳票NO
                             );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        $this->selectleftval = VARS::post("selectleftval");
        if ($this->cmd != 'change_class' && $this->cmd != 'read' && $this->cmd != 'update') {
            $this->select_opt = array();
        }
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput()
    {
        if ($this->selectleft === ''){
            $this->setWarning("MSG916");
            return false;
        }
        if ($this->field["OUTPUT_NO"] === '3' && $this->field["OUTPUTDATE"] === ''){
            $this->setWarning("納入期限を指定してください");
            return false;
        }
        return true;
    }


    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        if ($this->validateInput()){
            knjmp801Query::insertQuery($db, $this);
        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        //$this->setMessage("MSG201");
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
