<?php

require_once('for_php7.php');

require_once('knjp910_schregModel.inc');
require_once('knjp910_schregQuery.inc');

class knjp910_schregController extends Controller {
    var $ModelClassName = "knjp910_schregModel";
    var $ProgramID      = "KNJP910_SCHREG";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                case "knjp910_schreg":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp910_schregModel();        //コントロールマスタの呼び出し
                    $this->callView("knjp910_schregForm1");
                    exit;
                case "update":
                    $sessionInstance->getUpdateModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                case "delete":
                    $sessionInstance->getDeleteModel();
                    $sessionInstance->setCmd("main");
                    break 1;
                default:
                    $sessionInstance->setError(new PEAR_Error("未対応のアクション{$sessionInstance->cmd}です"));
                    $this->callView("error");
                    break 2;
            }
        }
            
    }
}
$knjp910_schregCtl = new knjp910_schregController;
//var_dump($_REQUEST);
?>

