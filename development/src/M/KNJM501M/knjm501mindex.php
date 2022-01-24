<?php

require_once('for_php7.php');

require_once('knjm501mModel.inc');
require_once('knjm501mQuery.inc');

class knjm501mController extends Controller {
    var $ModelClassName = "knjm501mModel";
    var $ProgramID      = "KNJM501M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm501m":                         //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjm501mModel();   //コントロールマスタの呼び出し
                    $this->callView("knjm501mForm1");
                    exit;
                case "clschange":
                    $sessionInstance->knjm501mModel();
                    $this->callView("knjm501mForm1");
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
$knjm501mCtl = new knjm501mController;
var_dump($_REQUEST);
?>
