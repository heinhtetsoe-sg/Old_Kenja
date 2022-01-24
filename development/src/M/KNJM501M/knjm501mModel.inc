<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm501mModel extends Model {
    var $field = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $select_data = array();
    var $select_opt  = array();    //セレクトデータ
    var $selectleft;

    /**
     * メンバ変数の初期化
     */
    function knjm501mModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("GAKKI"             =>  VARS::post("GAKKI"),           //学期
                             "GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),  //クラス
                             "RKIJUN"            =>  VARS::post("RKIJUN"),          //レポート提出基準日
                             "SKIJUN"            =>  VARS::post("SKIJUN"),          //スクーリング提出基準日
                             "TKIJUN"            =>  VARS::post("TKIJUN"),          //特別活動集計基準日
                             "OUTPUT"            =>  VARS::post("OUTPUT"),          //印刷日
                             "GRADVAL_PRINT"     =>  VARS::post("GRADVAL_PRINT"),   //評定印字
                             "COMMENT7"          =>  VARS::post("COMMENT7"),        //コメント1
                             "COMMENT8"          =>  VARS::post("COMMENT8"),        //コメント2
                             "COMMENT9"          =>  VARS::post("COMMENT9"),        //コメント3
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
        if (strlen($this->field["COMMENT7"]) > 150) {
            $this->setWarning("MSG915",strlen($this->field["COMMENT7"])."(備考は150バイトまでです)");
            return false;
        }
        if (strlen($this->field["COMMENT8"]) > 150) {
            $this->setWarning("MSG915",strlen($this->field["COMMENT8"])."(備考は150バイトまでです)");
            return false;
        }
        if (strlen($this->field["COMMENT9"]) > 150) {
            $this->setWarning("MSG915",strlen($this->field["COMMENT9"])."(備考は150バイトまでです)");
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
            for ($upcnt = 7;$upcnt <= 9;$upcnt++) {
                $this->update($upcnt);
            }
        }
    }
    function &delete()
    {
        $result = knjm501mQuery::getDeleteQuery($this);

        return $result;
    }

    function &update($cnt)
    {
        $result = knjm501mQuery::getUpdateQuery($this,$cnt);

        return $result;
    }

}
?>
