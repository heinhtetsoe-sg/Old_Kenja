<?php

require_once('for_php7.php');

require_once('knja143gModel.inc');
require_once('knja143gQuery.inc');

class knja143gController extends Controller {
    var $ModelClassName = "knja143gModel";
    var $ProgramID      = "KNJA143G";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja143g":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja143gModel();        //コントロールマスタの呼び出し
                    $this->callView("knja143gForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja143gCtl = new knja143gController;
//var_dump($_REQUEST);
?>
