<?php

require_once('for_php7.php');
require_once('knjp920_meisaiModel.inc');
require_once('knjp920_meisaiQuery.inc');

class knjp920_meisaiController extends Controller {
    var $ModelClassName = "knjp920_meisaiModel";
    var $ProgramID      = "KNJP920_MEISAI";

    function main()
    {
        $sessionInstance =& Model::getModel($this);
        while ( true ) {
            switch (trim($sessionInstance->cmd)) {
                case "":
                case "edit":
                case "main":
                case "knjp920_meisai":                                //メニュー画面もしくはSUBMITした場合
                    $sessionInstance->knjp920_meisaiModel();        //コントロールマスタの呼び出し
                    $this->callView("knjp920_meisaiForm1");
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
$knjp920_meisaiCtl = new knjp920_meisaiController;
//var_dump($_REQUEST);
?>

