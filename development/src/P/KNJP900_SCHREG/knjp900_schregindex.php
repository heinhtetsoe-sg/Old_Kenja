<?php

require_once('for_php7.php');

require_once('knjp900_schregModel.inc');
require_once('knjp900_schregQuery.inc');

class knjp900_schregController extends Controller {
    var $ModelClassName = "knjp900_schregModel";
    var $ProgramID      = "KNJP900_SCHREG";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                case "knjp900_schreg":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp900_schregModel();        //コントロールマスタの呼び出し
                    $this->callView("knjp900_schregForm1");
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
$knjp900_schregCtl = new knjp900_schregController;
//var_dump($_REQUEST);
?>

