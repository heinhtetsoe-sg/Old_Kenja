<?php

require_once('for_php7.php');

require_once('knjm500mModel.inc');
require_once('knjm500mQuery.inc');

class knjm500mController extends Controller {
    var $ModelClassName = "knjm500mModel";
    var $ProgramID      = "KNJM500M";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "knjm500m":                         //メニュー画面もしくはSUBMITした場合
                case "read":
                    $sessionInstance->knjm500mModel();   //コントロールマスタの呼び出し
                    $this->callView("knjm500mForm1");
                    exit;
                case "clschange":
                    $sessionInstance->knjm500mModel();
                    $this->callView("knjm500mForm1");
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
$knjm500mCtl = new knjm500mController;
var_dump($_REQUEST);
?>
