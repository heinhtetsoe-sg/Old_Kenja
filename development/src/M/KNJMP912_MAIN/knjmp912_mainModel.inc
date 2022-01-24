<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjmp912_mainModel extends Model {
    var $field      = array();
    var $cmd;
    var $getAuth;
    var $getPrgId;
    var $getYear;
    var $getHenkinLcd;
    var $getHenkinMcd;
    var $getHenkinScd;
    var $getHenkinLMScd;
    var $getRequestNo;
    var $auth;
    var $year;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $selectStudent;
    var $selectStudentLabel;

    /**
     * メンバ変数の初期化
     */
    function knjmp912_mainModel()
    {
    }

    function init()
    {
        if (VARS::get("SEND_AUTH")) {
            $this->getAuth         = VARS::get("SEND_AUTH");
            $this->getYear         = VARS::get("SEND_YEAR");
            $this->getPrgId        = VARS::get("SEND_PRGID");
            $this->getHenkinLcd    = VARS::get("SEND_HENKIN_L_CD");
            $this->getHenkinMcd    = VARS::get("SEND_HENKIN_M_CD");
            $this->getHenkinScd    = VARS::get("SEND_HENKIN_S_CD");
            $this->getHenkinLMScd  = VARS::get("SEND_HENKIN_L_M_S_CD");
            $this->getRequestNo    = VARS::get("SEND_REQUEST_NO");
        }

        $this->auth = $this->getAuth ? $this->getAuth : AUTHORITY;
        $this->year = $this->getYear ? $this->getYear : CTRL_YEAR;

        $this->field = array("HENKIN_L_M_S_CD"      =>  VARS::post("HENKIN_L_M_S_CD"),      //支出細目
                             "HENKIN_GK"            =>  VARS::post("HENKIN_GK"),            //返金額(一人当たり)
                             "HENKIN_CNT"           =>  VARS::post("HENKIN_CNT"),           //人数
                             "TOTAL_HENKIN_GK"      =>  VARS::post("TOTAL_HENKIN_GK"),      //合計返金額
                             "COLLECT_L_M_S_CD"     =>  VARS::post("COLLECT_L_M_S_CD"),     //収入項目(細目)
                             "HR_CLASS_HYOUJI_FLG"  =>  VARS::post("HR_CLASS_HYOUJI_FLG"),  //年組番表示
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                     );
        //年組表示を初期値とする
        if (VARS::get("SEND_AUTH")) {
            $this->field["HR_CLASS_HYOUJI_FLG"] = "1";
        }
        $this->selectStudent = VARS::post("selectStudent");
        $this->selectStudentLabel = VARS::post("selectStudentLabel");

        $this->cmd         = VARS::request("cmd");
    }
    
    //入力項目チェック
    function validateInput($db)
    {
        //必須チェック
        if ($this->field["HENKIN_L_M_S_CD"] == "") {
            $this->setWarning("MSG301", "( 品名等 )");
            return false;
        }
        //桁数チェック
        if (strlen($this->field["HENKIN_GK"]) > 6) {
            $this->setWarning("MSG901", "( 返金額 ) 6バイトまで");
            return false;
        }
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        //入力チェック
        if ($this->validateInput($db)) {
            //新規の場合は伝票番号を発行
            if (!$this->getRequestNo) {
                $getMaxRequestNo = $db->getOne(knjmp912_mainQuery::getMaxRequestNo($this));
                //対象年度の伝票番号がない場合
                if (!$getMaxRequestNo) {
                    $this->getRequestNo = $this->getYear.'F00001';
                } else {
                    $setNo = sprintf("%05d", intval(substr($getMaxRequestNo, 5, 5)) + 1);
                    $this->getRequestNo = $this->getYear.'F'.$setNo;
                }
            }
            knjmp912_mainQuery::getUpdateQuery($db, $this);

            //KEYの更新
            $henkinlcd = substr($this->field["HENKIN_L_M_S_CD"], 0, 2);
            $henkinmcd = substr($this->field["HENKIN_L_M_S_CD"], 2, 2);
            $henkinscd = substr($this->field["HENKIN_L_M_S_CD"], 4, 2);

            $this->getHenkinLcd            = $henkinlcd;
            $this->getHenkinMcd            = $henkinmcd;
            $this->getHenkinScd            = $henkinmcd;
            $this->getHenkinLMScd          = $this->field["HENKIN_L_M_S_CD"];
            
            $this->setMessage("MSG201");
        }
        Query::dbCheckIn($db);

        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        $db = Query::dbCheckOut();
        
        knjmp912_mainQuery::getDeleteQuery($db, $this);
        
        $this->getHenkinLcd  = "";
        $this->getHenkinMcd  = "";
        $this->getHenkinScd = "";
        $this->getHenkinLMScd = "";
        $this->getRequestNo  = "";

        $this->setMessage("MSG201");
        
        Query::dbCheckIn($db);

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

