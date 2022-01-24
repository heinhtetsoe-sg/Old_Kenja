<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl132rModel extends Model {
    var $cmd;
    var $year;
    var $applicantdiv;

    /**
     * メンバ変数の初期化
     */
    function knjl132rModel() {
    }

    function init()
    {
        $this->fixApplicantDiv = '1';
        if (SCHOOLKIND == 'J') {
            $this->fixApplicantDiv = '2';
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->applicantdiv = VARS::post("APPLICANTDIV");
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
