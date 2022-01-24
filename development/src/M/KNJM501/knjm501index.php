<?php

require_once('for_php7.php');

require_once('knjm501Model.inc');
require_once('knjm501Query.inc');

class knjm501Controller extends Controller {
    var $ModelClassName = "knjm501Model";
    var $ProgramID      = "KNJM501";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm501":                         //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjm501Model();   //コントロールマスタの呼び出し
                    $this->callView("knjm501Form1");
                    exit;
                case "clschange":
                    $sessionInstance->knjm501Model();
                    $this->callView("knjm501Form1");
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
$knjm501Ctl = new knjm501Controller;
var_dump($_REQUEST);
?>
