<?php

require_once('for_php7.php');

require_once('knja143fModel.inc');
require_once('knja143fQuery.inc');

class knja143fController extends Controller {
    var $ModelClassName = "knja143fModel";
    var $ProgramID      = "KNJA143F";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja143f":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja143fModel();        //コントロールマスタの呼び出し
                    $this->callView("knja143fForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja143fCtl = new knja143fController;
//var_dump($_REQUEST);
?>

