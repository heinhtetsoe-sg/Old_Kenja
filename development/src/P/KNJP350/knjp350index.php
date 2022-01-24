<?php

require_once('for_php7.php');

require_once('knjp350Model.inc');
require_once('knjp350Query.inc');

class knjp350Controller extends Controller {
    var $ModelClassName = "knjp350Model";
    var $ProgramID      = "KNJp350";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjp350":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjp350Model();		//コントロールマスタの呼び出し
                    $this->callView("knjp350Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjp350Ctl = new knjp350Controller;
var_dump($_REQUEST);
?>
