<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja142eModel extends Model {
    var $field      = array();
    var $cmd;
	var $control;		//コントロールマスタ
	var $staffcd;		//職員コード
    var $selectleft;
    var $selectleftval;
    var $schoolName;

    /**
     * メンバ変数の初期化
     */
    function knja142eModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD; //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),             //年度
                             "GAKKI"                =>  VARS::post("GAKKI"),            //学期
                             "SCHOOL_KIND"          =>  VARS::post("SCHOOL_KIND"),      //校種
                             "DISP"                 =>  VARS::post("DISP"),             //表示選択 クラス指定・個人指定
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "OUTPUT"               =>  VARS::post("OUTPUT"),           //出力帳票 表面・裏面
                             "SDATE"	            =>  VARS::post("SDATE"),            //発行日付
                             "EDATE"	            =>  VARS::post("EDATE"),            //有効期限
                             "PRINT_IMAGE"          =>  VARS::post("PRINT_IMAGE"),      //顔写真を表示
                             "SEND_ADDR"            =>  VARS::post("SEND_ADDR"),        //その他(送付先)
                             "MAISUU"               =>  VARS::post("MAISUU"),           //出力枚数
                             "DBNAME"               =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $db = Query::dbCheckOut();
        //学校名取得
        $query = knja142eQuery::getNameMstZ010();
        $this->schoolName = $db->getOne($query);
        Query::dbCheckIn($db);

        $this->cmd              = VARS::request("cmd");
        $this->selectleft       = VARS::post("selectleft");
        $this->selectleftval    = VARS::post("selectleftval");
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
