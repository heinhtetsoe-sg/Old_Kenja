<?php

require_once('for_php7.php');

require_once('knjf070Model.inc');
require_once('knjf070Query.inc');

class knjf070Controller extends Controller {
    var $ModelClassName = "knjf070Model";
    var $ProgramID      = "KNJF070";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf070":								//メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
					$sessionInstance->knjf070Model();		//コントロールマスタの呼び出し
                    $this->callView("knjf070Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
    }
}
$knjf070Ctl = new knjf070Controller;
//var_dump($_REQUEST);
?>
