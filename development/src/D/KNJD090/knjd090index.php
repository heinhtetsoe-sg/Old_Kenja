<?php

require_once('for_php7.php');

require_once('knjd090Model.inc');
require_once('knjd090Query.inc');

class knjd090Controller extends Controller {
    var $ModelClassName = "knjd090Model";
    var $ProgramID      = "KNJD090";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd090":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd090Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd090Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd090Ctl = new knjd090Controller;
var_dump($_REQUEST);
?>
