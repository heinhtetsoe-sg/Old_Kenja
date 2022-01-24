<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjc180tModel extends Model {
    var $field      = array();
    var $select_data      = array();
    var $opt_subclass      = array();
    var $month;
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjc180tModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("TEMPLATE"        	=>  VARS::post("TEMPLATE"),      	//テンプレート
                            "GAKKI"         	=>  VARS::post("GAKKI"),     		//学期
                            "CLASS"         	=>  VARS::post("CLASS"),     		//クラス
                            "GRADE"         	=>  VARS::post("GRADE"),     		//学年
                            "DBNAME"          	=>  VARS::post("DBNAME")       		//DB変更に備えて
                     );

        $this->month       = VARS::post("month");
        $this->cmd         = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

}
?>
