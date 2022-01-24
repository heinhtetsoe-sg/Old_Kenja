<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh100Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード

    /**
     * メンバ変数の初期化
     */
    function knjh100Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                            "GRADE_HR_CLASS"    =>  VARS::post("GRADE_HR_CLASS"),   //年組
                            "OUTPUT"            =>  VARS::post("OUTPUT"),           //名簿
                            "HYOSHO"            =>  VARS::post("HYOSHO"),           //賞罰区分（表彰実績区分）
                            "BATSU"             =>  VARS::post("BATSU"),            //賞罰区分（罰則区分）
//                          "SIKAKU"            =>  VARS::post("SIKAKU"),           //賞罰区分（資格特技区分）
                            "HS_KUBUN_FROM"     =>  VARS::post("HS_KUBUN_FROM"),    //表彰区分選択（FROM）
                            "HS_KUBUN_TO"       =>  VARS::post("HS_KUBUN_TO"),      //表彰区分選択（TO）
                            "BS_KUBUN_FROM"     =>  VARS::post("BS_KUBUN_FROM"),    //罰則区分選択（FROM）
                            "BS_KUBUN_TO"       =>  VARS::post("BS_KUBUN_TO"),      //罰則区分選択（TO）
//                          "ST_KUBUN_FROM"     =>  VARS::post("ST_KUBUN_FROM"),    //特技区分選択（FROM）
//                          "ST_KUBUN_TO"       =>  VARS::post("ST_KUBUN_TO"),      //資格特技区分選択（TO）
                            "GAKKI"             =>  VARS::post("GAKKI"),            //学期コード
                            "DBNAME"            =>  VARS::post("DBNAME")            //DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
