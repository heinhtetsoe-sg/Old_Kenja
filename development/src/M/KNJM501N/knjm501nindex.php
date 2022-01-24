<?php

require_once('for_php7.php');

require_once('knjm501nModel.inc');
require_once('knjm501nQuery.inc');

class knjm501nController extends Controller {
    var $ModelClassName = "knjm501nModel";
    var $ProgramID      = "KNJM501N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "gakki":
                    $sessionInstance->knjm501nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm501nForm1");
                    exit;
                case "knjm501n":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->setAccessLogDetail("S", $ProgramID); 
                    $sessionInstance->knjm501nModel();        //コントロールマスタの呼び出し
                    $this->callView("knjm501nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
    }
}
$knjm501nCtl = new knjm501nController;
//var_dump($_REQUEST);
?>
