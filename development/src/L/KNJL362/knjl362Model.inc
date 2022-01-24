<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl362Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $staffcd;        //職員コード
    var $ObjYear;
    var $isGojouOnly;
    var $isGojou;
    var $isCollege;
    var $isWakayama;

    /**
     * メンバ変数の初期化
     */
    function knjl362Model()
    {
    }

    function init() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //職員コード
        $this->ObjYear      = CTRL_YEAR+1;
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),         //年度
                             "POROW"            =>  VARS::post("POROW"),        //行
                             "POCOL"            =>  VARS::post("POCOL"),        //列
                             "APPLICANTDIV"     =>  VARS::post("APPLICANTDIV"), //入試制度
                             "TESTDIV"          =>  VARS::post("TESTDIV"),      //入試区分
                             "FINSCHOOLDIV"     =>  VARS::post("FINSCHOOLDIV"), //区分
                             "DBNAME"           =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        //智辯フラグ
        $db = Query::dbCheckOut();
        $query = knjl362Query::getZ010();
        $name1 = $db->getOne($query);
        $this->isChiben = ($name1 == 'CHIBEN') ? "1" : "";

        //学校コードを取得：五条・和歌山を判断するため
        $rtnRow = knjl362Query::getNameMstZ010();
        $this->isGojouOnly = $rtnRow["NAME2"] == "30290053001" ? true : false;
        $this->isGojou = $rtnRow["NAME2"] == "30290053001" || $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->isCollege = $rtnRow["NAME2"] == "30290086001" ? true : false;
        $this->isWakayama = $rtnRow["NAME2"] == "30300049001" ? true : false;

        Query::dbCheckIn($db);

    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
