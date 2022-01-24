<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjh400_medicalModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjh400_medicalModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }

        $this->cmd = VARS::request("cmd");
        if(VARS::request("SCHREGNO")){
            $this->schregno = VARS::request("SCHREGNO");
            $this->year = "";
        }
        
        $this->getPropertiesAll(); //プロパティファイル
        
        $this->btnRadio = VARS::post("btnRadio") != "" ? VARS::post("btnRadio") : "1";
        
        //年度移動できるように
        if($this->year == "" || $this->cmd == "general" || $this->cmd == "dental"){
            if($this->btnRadio == "1"){
                $table = "MEDEXAM_DET_DAT";
            }else{
                $table = "MEDEXAM_TOOTH_DAT";
            }
            
            //DB接続
            $db = Query::dbCheckOut();
            
            //データから最大年度を取得する
            $query = knjh400_medicalQuery::getYearMax($this->schregno, $table);
            $this->year = $db->getOne($query);
            
            //DB切断
            Query::dbCheckIn($db);
        }
        if($this->cmd == "next_year"){
            $this->year = $this->year + 1;
        }else if($this->cmd == "before_year"){
            $this->year = $this->year - 1;
        }
        
    }

}
//var_dump($_REQUEST);
?>
