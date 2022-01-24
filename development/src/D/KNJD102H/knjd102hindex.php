<?php

require_once('for_php7.php');

require_once('knjd102hModel.inc');
require_once('knjd102hQuery.inc');

class knjd102hController extends Controller {
    var $ModelClassName = "knjd102hModel";
    var $ProgramID      = "KNJD102H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                case "knjd102h":								//メニュー画面もしくはSUBMITした場合
					$sessionInstance->knjd102hModel();		//コントロールマスタの呼び出し
                    $this->callView("knjd102hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
			}
        }
            
    }
}
$knjd102hCtl = new knjd102hController;
//var_dump($_REQUEST);
?>
