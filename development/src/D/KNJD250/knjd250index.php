<?php

require_once('for_php7.php');

require_once('knjd250Model.inc');
require_once('knjd250Query.inc');

class knjd250Controller extends Controller {
    var $ModelClassName = "knjd250Model";
    var $ProgramID      = "KNJD250";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "read":
                case "knjd250":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd250Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd250Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd250Ctl = new knjd250Controller;
var_dump($_REQUEST);
?>
