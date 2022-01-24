<?php

require_once('for_php7.php');

require_once('knjf060Model.inc');
require_once('knjf060Query.inc');

class knjf060Controller extends Controller {
    var $ModelClassName = "knjf060Model";
    var $ProgramID      = "KNJF060";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjf060":								//メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID);
					$sessionInstance->knjf060Model();		//コントロールマスタの呼び出し
                    $this->callView("knjf060Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjf060Ctl = new knjf060Controller;
var_dump($_REQUEST);
?>
