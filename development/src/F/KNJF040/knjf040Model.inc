<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf040Model extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード

    /**
     * メンバ変数の初期化
     */
    function knjf040Model()
    {
		//コントロールマスタより情報を取得
		common::GetControlMaster_Fnc($this->control);
		$this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
		$this->staffcd = STAFFCD;							//職員コード
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("YEAR"				=>  VARS::post("YEAR"),			//年度
							"GAKKI"				=>	VARS::post("GAKKI"),		//学期コード
							"DATE"				=>	VARS::post("DATE"),		//
							"GRADE_HR_CLASS"	=>  VARS::post("GRADE_HR_CLASS"),	//年組
							"CHECK1"			=>  VARS::post("CHECK1"),		//検診票（一般）チェックボックス
							"OUTPUTA"			=>  VARS::post("OUTPUTA"),		//出力方法オプションボタン	// 2003/11/27 nakamoto
							"CHECK2"			=>  VARS::post("CHECK2"),		//検診票（歯口腔）チェックボックス
							"OUTPUTB"			=>  VARS::post("OUTPUTB"),		//出力方法オプションボタン	// 2003/11/27 nakamoto
							"CHECK3"			=>  VARS::post("CHECK3"),		//未検診生徒チェックボックス
							"CHECK4"			=>  VARS::post("CHECK4"),		//眼科受診チェックボックス
							"CHECK5"			=>  VARS::post("CHECK5"),		//検診結果チェックボックス
							"CHECK6"			=>  VARS::post("CHECK6"),		//健康診断結果チェックボックス
							"OUTPUT"			=>  VARS::post("OUTPUT"),		//出力方法オプションボタン
							"CHECK7"			=>  VARS::post("CHECK7"),		//定期健康診断チェックボックス
							"CHECK8"			=>  VARS::post("CHECK8"),		//内科検診チェックボックス
							"CHECK9"			=>  VARS::post("CHECK9"),		//異常者一覧チェックボックス
							"SELECT1"			=>  VARS::post("SELECT1"),		//一般条件リスト
							"SELECT2"			=>  VARS::post("SELECT2"),		//歯口腔条件リスト
							"ID"				=>  VARS::post("ID"),			//プログラムID
							"DBNAME"			=>  VARS::post("DBNAME")		//DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
