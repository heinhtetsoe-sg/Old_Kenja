<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm500Model extends Model {
    var $field = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $select_data = array();
    var $select_opt  = array();    //セレクトデータ
    var $selectleft;

    /**
     * メンバ変数の初期化
     */
    function knjm500Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),  //クラス
                             "RKIJUN"            =>  VARS::post("RKIJUN"),          //レポート提出基準日
                             "SKIJUN"            =>  VARS::post("SKIJUN"),          //スクーリング提出基準日
                             "GRADVAL_PRINT"     =>  VARS::post("GRADVAL_PRINT"),   //評定印字
                             "COMMENT1"          =>  VARS::post("COMMENT1"),        //コメント1
                             "COMMENT2"          =>  VARS::post("COMMENT2"),        //コメント2
                             "COMMENT3"          =>  VARS::post("COMMENT3"),        //コメント3
                             "COMMENT4"          =>  VARS::post("COMMENT4"),        //コメント4
                             "COMMENT5"          =>  VARS::post("COMMENT5"),        //コメント5
                             "COMMENT6"          =>  VARS::post("COMMENT6"),        //コメント6
                             "DBNAME"            =>  VARS::post("DBNAME"));         //DB変更に備えて

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->selectleft = VARS::post("selectleft");

        $this->cmd         = VARS::request("cmd");

        if ($this->cmd != 'read') {
            $this->select_opt = array();
        }
        $this->getPropertiesAll(); //プロパティファイルの読込み

    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
    
    //入力項目チェック
    function validateInput()
    {
        if (strlen($this->field["COMMENT1"]) > 150) {
            $this->setWarning("MSG915",strlen($this->field["COMMENT1"])."(備考は150バイトまでです)");
            return false;
        }
        if (strlen($this->field["COMMENT2"]) > 150) {
            $this->setWarning("MSG915",strlen($this->field["COMMENT2"])."(備考は150バイトまでです)");
            return false;
        }
        if (strlen($this->field["COMMENT3"]) > 150) {
            $this->setWarning("MSG915",strlen($this->field["COMMENT3"])."(備考は150バイトまでです)");
            return false;
        }
        if (strlen($this->field["COMMENT4"]) > 150) {
            $this->setWarning("MSG915",strlen($this->field["COMMENT4"])."(備考は150バイトまでです)");
            return false;
        }
        if (strlen($this->field["COMMENT5"]) > 150) {
            $this->setWarning("MSG915",strlen($this->field["COMMENT5"])."(備考は150バイトまでです)");
            return false;
        }
        if (strlen($this->field["COMMENT6"]) > 150) {
            $this->setWarning("MSG915",strlen($this->field["COMMENT6"])."(備考は150バイトまでです)");
            return false;
        }
        return true;
    }
    
    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $tmpdata = array();
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
            for ($upcnt = 1;$upcnt <= 6;$upcnt++) {
                $this->update($upcnt);
            }
        }
    }
    function &delete()
    {
        $result = knjm500Query::getDeleteQuery($this);

        return $result;
    }

    function &update($cnt)
    {
        $result = knjm500Query::getUpdateQuery($this,$cnt);

        return $result;
    }

}
?>
