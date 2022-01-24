<?php

require_once('for_php7.php');

require_once('knjmp900_schregModel.inc');
require_once('knjmp900_schregQuery.inc');

class knjmp900_schregController extends Controller {
    var $ModelClassName = "knjmp900_schregModel";
    var $ProgramID      = "KNJMP900_SCHREG";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                case "knjmp900_schreg":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp900_schregModel();        //コントロールマスタの呼び出し
                    $this->callView("knjmp900_schregForm1");
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
$knjmp900_schregCtl = new knjmp900_schregController;
//var_dump($_REQUEST);
?>

