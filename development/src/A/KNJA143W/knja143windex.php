<?php

require_once('for_php7.php');

require_once('knja143wModel.inc');
require_once('knja143wQuery.inc');

class knja143wController extends Controller {
    var $ModelClassName = "knja143wModel";
    var $ProgramID      = "KNJA143W";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja143w":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja143wModel();        //コントロールマスタの呼び出し
                    $this->callView("knja143wForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja143wCtl = new knja143wController;
//var_dump($_REQUEST);
?>

