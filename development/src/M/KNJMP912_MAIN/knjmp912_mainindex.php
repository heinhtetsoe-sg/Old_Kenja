<?php

require_once('for_php7.php');

require_once('knjmp912_mainModel.inc');
require_once('knjmp912_mainQuery.inc');

class knjmp912_mainController extends Controller {
    var $ModelClassName = "knjmp912_mainModel";
    var $ProgramID      = "KNJMP912_MAIN";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                case "knjmp912_main":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjmp912_mainModel();        //コントロールマスタの呼び出し
                    $this->callView("knjmp912_mainForm1");
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
$knjmp912_mainCtl = new knjmp912_mainController;
//var_dump($_REQUEST);
?>

