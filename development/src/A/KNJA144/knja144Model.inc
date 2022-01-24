<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja144Model extends Model {
    var $field      = array();
    var $cmd;
    var $selectdata;//NO002
    var $selectStudent;
    var $selectStudentLabel;

    /**
     * メンバ変数の初期化
     */
    function knja144Model() {}

    function init()
    {
        $this->field = array("YEAR"				=>  VARS::post("YEAR"),		        //年度
							 "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
							 "GAKKI"			=>	VARS::post("GAKKI"),	        //学期
							 "CHECK2"			=>	VARS::post("CHECK2"),	        //年齢---NO003
							 "CHECK1"			=>	VARS::post("CHECK1"),	        //出席番号---NO002
							 "TITLE"			=>	VARS::post("TITLE"),	        //タイトル---NO001
							 "TERM_SDATE"		=>	VARS::post("TERM_SDATE"),	    //発行日
							 "TERM_EDATE"		=>	VARS::post("TERM_EDATE"),	    //発行日
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->selectdata = VARS::post("selectdata");//NO002
        $this->selectStudent = VARS::post("selectStudent");
        $this->selectStudentLabel = VARS::post("selectStudentLabel");

        $this->cmd         = VARS::request("cmd");
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
