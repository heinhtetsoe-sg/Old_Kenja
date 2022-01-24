<?php

require_once('for_php7.php');

require_once('knjd220Model.inc');
require_once('knjd220Query.inc');

class knjd220Controller extends Controller {
    var $ModelClassName = "knjd220Model";
    var $ProgramID      = "KNJD220";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd220":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd220Model();		//コントロールマスタの呼び出し
                    $this->callView("knjd220Form1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd220Ctl = new knjd220Controller;
var_dump($_REQUEST);
?>
