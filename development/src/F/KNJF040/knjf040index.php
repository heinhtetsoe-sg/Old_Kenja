<?php

require_once('for_php7.php');

require_once('knjf040Model.inc');
require_once('knjf040Query.inc');

class knjf040Controller extends Controller {
    var $ModelClassName = "knjf040Model";
    var $ProgramID        = "KNJF040";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "read":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjf040Model();		//コントロールマスタの呼び出し
                    $this->callView("knjf040Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjf040Ctl = new knjf040Controller;
var_dump($_REQUEST);
?>
