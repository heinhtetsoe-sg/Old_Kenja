<?php

require_once('for_php7.php');

require_once('knjb150oModel.inc');
require_once('knjb150oQuery.inc');

class knjb150oController extends Controller {
    var $ModelClassName = "knjb150oModel";
    var $ProgramID      = "KNJB150O";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjb150o":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjb150oModel();		//コントロールマスタの呼び出し
                    $this->callView("knjb150oForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjb150oCtl = new knjb150oController;
//var_dump($_REQUEST);
?>
