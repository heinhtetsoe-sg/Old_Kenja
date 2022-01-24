<?php

require_once('for_php7.php');

require_once('knjp912_mainModel.inc');
require_once('knjp912_mainQuery.inc');

class knjp912_mainController extends Controller {
    var $ModelClassName = "knjp912_mainModel";
    var $ProgramID      = "KNJP912_MAIN";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                case "knjp912_main":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp912_mainModel();        //コントロールマスタの呼び出し
                    $this->callView("knjp912_mainForm1");
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
$knjp912_mainCtl = new knjp912_mainController;
//var_dump($_REQUEST);
?>

