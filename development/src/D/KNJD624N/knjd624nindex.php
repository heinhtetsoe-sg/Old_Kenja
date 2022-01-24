<?php

require_once('for_php7.php');

require_once('knjd624nModel.inc');
require_once('knjd624nQuery.inc');

class knjd624nController extends Controller {
    var $ModelClassName = "knjd624nModel";
    var $ProgramID      = "KNJD624N";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjd624n":                             //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjd624nModel();       //コントロールマスタの呼び出し
                    $this->callView("knjd624nForm1");
                    exit;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }

    }
}
$knjd624nCtl = new knjd624nController;
//var_dump($_REQUEST);
?>
