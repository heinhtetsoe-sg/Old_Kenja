<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knjf165Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $selectleft;
    var $selectright;

    /**
     * メンバ変数の初期化
     */
    function knjf165Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                            "GAKKI"                 =>  VARS::post("GAKKI"),                //学期コード
                            "KUBUN"                 =>  VARS::post("KUBUN"),                //クラス・個人表示指定
                            "GRADE"                 =>  VARS::post("GRADE"),                //学年
                            "GRADE_HR_CLASS"        =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                            "DBNAME"                =>  VARS::post("DBNAME")                //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        $this->selectright = VARS::post("selectright");
        $this->getPropertiesAll();
    }

    //入力項目チェック
    function validateInput($kind) {
        return ture;
    }

    //更新処理
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput($this->cmd)) {
            knjf165Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
        return;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
