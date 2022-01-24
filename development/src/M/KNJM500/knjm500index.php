<?php

require_once('for_php7.php');

require_once('knjm500Model.inc');
require_once('knjm500Query.inc');

class knjm500Controller extends Controller {
    var $ModelClassName = "knjm500Model";
    var $ProgramID      = "KNJM500";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm500":                         //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjm500Model();   //コントロールマスタの呼び出し
                    $this->callView("knjm500Form1");
                    exit;
                case "clschange":
                    $sessionInstance->knjm500Model();
                    $this->callView("knjm500Form1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("read");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjm500Ctl = new knjm500Controller;
var_dump($_REQUEST);
?>
