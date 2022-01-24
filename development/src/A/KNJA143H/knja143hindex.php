<?php

require_once('for_php7.php');

require_once('knja143hModel.inc');
require_once('knja143hQuery.inc');

class knja143hController extends Controller {
    var $ModelClassName = "knja143hModel";
    var $ProgramID      = "KNJA143H";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "output":
                case "knja143h":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knja143hModel();        //コントロールマスタの呼び出し
                    $this->callView("knja143hForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knja143hCtl = new knja143hController;
//var_dump($_REQUEST);
?>

