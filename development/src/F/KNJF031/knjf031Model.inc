<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf031Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード
	var $select_opt  = array();	//セレクトデータ
    var $selectleft;

    /**
     * メンバ変数の初期化
     */
    function knjf031Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),			//年度
							"GAKKI"				=>	VARS::post("GAKKI"),		//学期コード
							"CHECK1"			=>  VARS::post("CHECK1"),		//検診票（一般）チェックボックス
							"CHECK2"			=>  VARS::post("CHECK2"),		//検診票（歯口腔）チェックボックス
							"OUTPUT"			=>	VARS::post("OUTPUT"),		//クラス・個人表示指定
							"OPT_OUT"			=>	VARS::post("OPT_OUT"),		//印刷指定
							"GRADE_HR_CLASS"	=>	VARS::post("GRADE_HR_CLASS"),	//クラス
							"DBNAME"			=>  VARS::post("DBNAME")		//DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft_test = VARS::post("selectleft_test");
        $this->selectleft = VARS::post("selectleft");
		if ($this->cmd != 'change_class') $this->select_opt = array();
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
