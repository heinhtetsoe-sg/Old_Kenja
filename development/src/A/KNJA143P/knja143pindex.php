<?php

require_once('for_php7.php');

require_once('knja143pModel.inc');
require_once('knja143pQuery.inc');

class knja143pController extends Controller {
    var $ModelClassName = "knja143pModel";
    var $ProgramID      = "KNJA143P";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "change":
                case "knja143p":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja143pModel();        //コントロールマスタの呼び出し
                    $this->callView("knja143pForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja143pCtl = new knja143pController;
//var_dump($_REQUEST);
?>

