<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl343Model extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl343Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {               
        //別画面からコール
        if (VARS::get("SEND_AUTH")) {
            $this->sendAuth = VARS::get("SEND_AUTH");
            $this->sendPrgid = VARS::get("SEND_PRGID");
        //メニューからコール
        } else if (VARS::get("PROGRAMID")) {
            unset($this->sendAuth);
            unset($this->sendPrgid);
        }
        $this->auth = $this->sendAuth ? $this->sendAuth : AUTHORITY;

        $this->ObjYear = CTRL_YEAR+1;
        $this->field = array("NOTICEDAY"        =>  VARS::post("NOTICEDAY"),     	//通知日付
							 "OUTPUT"   		=>  VARS::post("OUTPUT"),    		//出力範囲
							 "EXAMNO"   		=>  VARS::post("EXAMNO"),    		//受験番号
                     );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    function validateInput()
    {
        //クラス未確定が１件でもあるか
        $db = Query::dbCheckOut();
        $query = knjl343Query::getEntClassStd($this);
        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        if (0 < $cnt) {
            $this->setWarning("MSG305", "\\n入学クラスが未確定の人がいます。");
            return false;
        }
        return true;
    }

    function &getPrintCheck()
    {
        //入力チェック
        if ($this->validateInput()){
        }
        return;
    }
}
?>
