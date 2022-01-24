<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjp370Model extends Model {
    var $field       = array();
    var $select_opt  = array(); //セレクトデータ
    var $select_opt2 = array(); //セレクトデータ NO005
    var $cmd;
    var $control;               //コントロールマスタ
    var $selectleft;
    var $selectleft2;           //NO005
    var $output;                //NO001
    var $text1;                 //NO001
    var $text2;                 //NO001
    var $staffcd;   //職員コード
    var $date;      //取扱指定日

    /**
     * メンバ変数の初期化
     */
    function knjp370Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    function init()
    {
        $this->field = array("GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");
        $this->output      = VARS::post("OUTPUT");      //NO001
        $this->text1       = VARS::post("TEXT1");       //NO001
        $this->text2       = VARS::post("TEXT2");       //NO001
        $this->selectleft  = VARS::post("selectleft");
        $this->selectleft2 = VARS::post("selectleft2"); //NO005
        if ($this->cmd != 'change_class' && $this->cmd != 'read' && $this->cmd != 'update') {
            $this->select_opt  = array();
            $this->select_opt2 = array();   //NO005
        }

        if (VARS::post("STAFFCD")) $this->staffcd  = VARS::post("STAFFCD");
        if (VARS::post("DATE")) $this->date  = VARS::post("DATE");
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    //入力項目チェック NO001
    function validateInput()
    {
        if (strlen($this->text1) > 75) {
            $this->setWarning("MSG915",strlen($this->text1)."(備考は75バイトまでです)");
            return false;
        }
        if (strlen($this->text2) > 75) {
            $this->setWarning("MSG915",strlen($this->text2)."(備考は75バイトまでです)");
            return false;
        }
        return true;
    }
    
    // 編集完了用のモデルを返します NO001
    function &getUpdateModel()
    {
        //NO002
        if ($this->output == "1"){
            $this->update("");
        }else {
            //入力チェック
            if ($this->validateInput()){
                $this->delete();
                for ($upcnt = 1;$upcnt <= 2;$upcnt++){
                    $this->update($upcnt);
                }
            }
        }
    }
    function &delete()
    {
        
        //NO002
        if ($this->output == "1"){
            $result = knjp370Query::getDeleteQuery2($this);
        }else {
            $result = knjp370Query::getDeleteQuery($this);
        }

        return $result;
    }

    function &update($cnt)
    {

        if ($this->output == "2"){
            $result = knjp370Query::getUpdateQuery($this,$cnt);
        }

        return $result;
    }

}
?>
