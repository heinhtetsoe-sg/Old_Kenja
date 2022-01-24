<?php

require_once('for_php7.php');

require_once('knjz340aModel.inc');
require_once('knjz340aQuery.inc');

class knjz340aController extends Controller {
    var $ModelClassName = "knjz340aModel";
    var $ProgramID      = "KNJZ340A";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjz340a":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjz340aModel();		//コントロールマスタの呼び出し
                    $this->callView("knjz340aForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjz340aCtl = new knjz340aController;
var_dump($_REQUEST);
?>
